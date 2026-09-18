package com.example.securedocviewer.security;

import com.example.securedocviewer.account.Role;
import com.example.securedocviewer.account.UserAccountService;
import com.example.securedocviewer.config.ViewerProperties;
import com.example.securedocviewer.exception.UsernameTakenException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

/** Recognised-device storage: expiry, credential-change reset (D1), IPv6 /64 grouping (D6), hashing (D7). */
@SpringBootTest
@ActiveProfiles("test")
class KnownDevicesTest {

    private static final String PASSWORD = "correct-horse-battery";

    @Autowired
    private KnownDevices knownDevices;

    @Autowired
    private UserAccountService accounts;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private ViewerProperties properties;

    private void user(String username) {
        try {
            accounts.create(username, PASSWORD, Role.READER, false);
        } catch (UsernameTakenException alreadyCreated) {
            // shared context
        }
    }

    @Test
    void aSuccessfulSignInMakesTheAddressRecognisedAndOnlyAHashIsStored() {
        user("kd-alice");
        knownDevices.remember("kd-alice", "198.51.100.20");

        assertTrue(knownDevices.isRecognised("kd-alice", "198.51.100.20"));
        assertFalse(knownDevices.isRecognised("kd-alice", "198.51.100.21"));
        assertFalse(knownDevices.isRecognised("someone-else", "198.51.100.20"));
        String stored = jdbc.queryForObject("""
                select k.ip_hash from account_known_ip k join app_user u on u.id = k.user_id
                where u.username = 'kd-alice'""", String.class);
        assertFalse(stored.contains("198.51.100"), "raw address stored: " + stored);
    }

    @Test
    void recognitionExpiresAfterThirtyDays() {
        user("kd-bob");
        Instant signedIn = Instant.parse("2026-01-01T00:00:00Z");
        new KnownDevices(jdbc, properties, Clock.fixed(signedIn, ZoneOffset.UTC)).remember("kd-bob", "198.51.100.30");

        KnownDevices day29 = new KnownDevices(jdbc, properties,
                Clock.fixed(signedIn.plus(KnownDevices.RETENTION).minusSeconds(60), ZoneOffset.UTC));
        KnownDevices day31 = new KnownDevices(jdbc, properties,
                Clock.fixed(signedIn.plus(KnownDevices.RETENTION).plusSeconds(60), ZoneOffset.UTC));
        assertTrue(day29.isRecognised("kd-bob", "198.51.100.30"));
        assertFalse(day31.isRecognised("kd-bob", "198.51.100.30"), "expiry must be enforced in the check itself");

        day31.purgeExpired();
        assertEquals(0, jdbc.queryForObject("""
                select count(*) from account_known_ip k join app_user u on u.id = k.user_id
                where u.username = 'kd-bob'""", Integer.class));
    }

    @Test
    void passwordResetPasswordChangeAndDisablingForgetRecognisedDevices() {
        user("kd-carol");
        knownDevices.remember("kd-carol", "198.51.100.40");
        accounts.resetPassword("kd-carol", "a-brand-new-password");
        assertFalse(knownDevices.isRecognised("kd-carol", "198.51.100.40"), "admin reset must forget devices");

        knownDevices.remember("kd-carol", "198.51.100.40");
        accounts.changeOwnPassword("kd-carol", "a-brand-new-password", "yet-another-password");
        assertFalse(knownDevices.isRecognised("kd-carol", "198.51.100.40"), "own change must forget devices");

        knownDevices.remember("kd-carol", "198.51.100.40");
        accounts.update("admin", "kd-carol", null, false);
        assertFalse(knownDevices.isRecognised("kd-carol", "198.51.100.40"), "disabling must forget devices");
    }

    @Test
    void ipv6AddressesAreGroupedByTheirSlash64() {
        user("kd-dave");
        knownDevices.remember("kd-dave", "2001:db8:1234:5678:aaaa::1");
        // A rotated privacy address in the same /64 is still the same device network.
        assertTrue(knownDevices.isRecognised("kd-dave", "2001:db8:1234:5678:ffff:eeee::9"));
        assertFalse(knownDevices.isRecognised("kd-dave", "2001:db8:1234:9999::1"));
        assertEquals(KnownDevices.normalise("2001:db8:1234:5678::1"), KnownDevices.normalise("2001:0db8:1234:5678:ffff::2"));
    }

    @Test
    void rememberingTheSameDeviceTwiceIsAnUpsert() {
        user("kd-erin");
        knownDevices.remember("kd-erin", "198.51.100.50");
        knownDevices.remember("kd-erin", "198.51.100.50");
        assertEquals(1, jdbc.queryForObject("""
                select count(*) from account_known_ip k join app_user u on u.id = k.user_id
                where u.username = 'kd-erin'""", Integer.class));
    }
}
