package com.example.securedocviewer.security;

import com.example.securedocviewer.config.ViewerProperties;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

/**
 * Derives values from a session id that are safe to expose, so the id
 * itself (the real credential) never leaves the server:
 * <ul>
 *   <li>{@link #tileBinding} goes into signed tile URLs. A tile request
 *       must present both the URL and the session cookie it derives from,
 *       so a leaked URL alone is useless.</li>
 *   <li>{@link #adminHandle} identifies a session in the admin UI so it can
 *       be revoked. It can't be turned back into the id or used to sign in.</li>
 * </ul>
 * Each uses a different HMAC context, so one can't stand in for the other.
 */
@Component
public class SessionKeys {

    private static final String HMAC_ALGO = "HmacSHA256";

    private final ViewerProperties properties;

    public SessionKeys(ViewerProperties properties) {
        this.properties = properties;
    }

    public String tileBinding(String sessionId) {
        return derive("tile-binding:", sessionId, 16);
    }

    public boolean tileBindingMatches(String sessionId, String binding) {
        return sessionId != null && binding != null && MessageDigest.isEqual(
                tileBinding(sessionId).getBytes(StandardCharsets.UTF_8),
                binding.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Crockford Base32 (digits and upper-case letters without I, L, O, U),
     * because its first characters are printed in the watermark as a trace
     * code and must survive being read off a screenshot or typed back in.
     */
    public String adminHandle(String sessionId) {
        return crockfordBase32(digest("admin-handle:", sessionId, 10));
    }

    private static final char[] CROCKFORD = "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray();

    static String crockfordBase32(byte[] bytes) {
        StringBuilder out = new StringBuilder();
        int buffer = 0;
        int bits = 0;
        for (byte b : bytes) {
            buffer = (buffer << 8) | (b & 0xff);
            bits += 8;
            while (bits >= 5) {
                out.append(CROCKFORD[(buffer >>> (bits - 5)) & 31]);
                bits -= 5;
            }
        }
        if (bits > 0) {
            out.append(CROCKFORD[(buffer << (5 - bits)) & 31]);
        }
        return out.toString();
    }

    private String derive(String context, String sessionId, int bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest(context, sessionId, bytes));
    }

    private byte[] digest(String context, String sessionId, int bytes) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(properties.getSigningSecret().getBytes(StandardCharsets.UTF_8), HMAC_ALGO));
            return Arrays.copyOf(mac.doFinal((context + sessionId).getBytes(StandardCharsets.UTF_8)), bytes);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to derive session key", e);
        }
    }
}
