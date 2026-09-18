package com.example.securedocviewer.security;

import com.example.securedocviewer.config.ViewerProperties;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 * Addresses each account has recently signed in from successfully. A
 * recognised address is exempt from the account-wide lockout (but never from
 * the per-device one), so an attacker hammering a username from elsewhere
 * can't lock its owner out of their usual device.
 *
 * <p>Addresses are personal data: only a keyed hash is stored (IPv6 grouped
 * by /64, since privacy addresses rotate within a prefix), and entries
 * expire after 30 days. They are forgotten whenever the account's password
 * changes or it is disabled, so a device that once had the old password
 * doesn't keep its exemption.
 */
@Component
public class KnownDevices {

    static final Duration RETENTION = Duration.ofDays(30);

    private final JdbcTemplate jdbc;
    private final ViewerProperties properties;
    private final Clock clock;

    @org.springframework.beans.factory.annotation.Autowired
    public KnownDevices(JdbcTemplate jdbc, ViewerProperties properties) {
        this(jdbc, properties, Clock.systemUTC());
    }

    KnownDevices(JdbcTemplate jdbc, ViewerProperties properties, Clock clock) {
        this.jdbc = jdbc;
        this.properties = properties;
        this.clock = clock;
    }

    public boolean isRecognised(String username, String clientIp) {
        Integer count = jdbc.queryForObject("""
                        select count(*) from account_known_ip k join app_user u on u.id = k.user_id
                        where u.username = ? and k.ip_hash = ? and k.last_success_at > ?""",
                Integer.class, username, hash(clientIp), Timestamp.from(clock.instant().minus(RETENTION)));
        return count != null && count > 0;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void remember(String username, String clientIp) {
        List<Long> ids = jdbc.queryForList("select id from app_user where username = ?", Long.class, username);
        if (ids.isEmpty()) {
            return;
        }
        // Upsert: safe under concurrent sign-ins from the same device.
        jdbc.update("""
                        insert into account_known_ip (user_id, ip_hash, last_success_at) values (?, ?, ?)
                        on duplicate key update last_success_at = values(last_success_at)""",
                ids.get(0), hash(clientIp), Timestamp.from(clock.instant()));
    }

    /** After a password change/reset or when the account is disabled. */
    public void forget(String username) {
        jdbc.update("delete from account_known_ip where user_id = (select id from app_user where username = ?)", username);
    }

    @Scheduled(cron = "${secure-doc-viewer.known-device-purge-cron:0 45 3 * * *}")
    public void purgeExpired() {
        jdbc.update("delete from account_known_ip where last_success_at < ?",
                Timestamp.from(clock.instant().minus(RETENTION)));
    }

    /** Keyed hash of the address; IPv6 reduced to its /64 prefix first. */
    String hash(String clientIp) {
        String normalised = normalise(clientIp);
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(properties.getSigningSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(("known-device:" + normalised).getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(Arrays.copyOf(digest, 24));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to hash address", e);
        }
    }

    static String normalise(String clientIp) {
        if (clientIp == null || clientIp.indexOf(':') < 0) {
            return String.valueOf(clientIp);
        }
        try {
            byte[] address = InetAddress.getByName(clientIp).getAddress();
            if (address.length != 16) {
                return clientIp;
            }
            byte[] prefix = Arrays.copyOf(address, 8);
            StringBuilder out = new StringBuilder();
            for (int i = 0; i < 8; i += 2) {
                out.append(Integer.toHexString(((prefix[i] & 0xff) << 8) | (prefix[i + 1] & 0xff))).append(':');
            }
            return out.append(":/64").toString();
        } catch (UnknownHostException e) {
            return clientIp;
        }
    }
}
