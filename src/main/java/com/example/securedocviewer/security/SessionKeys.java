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

    public String adminHandle(String sessionId) {
        return derive("admin-handle:", sessionId, 9);
    }

    private String derive(String context, String sessionId, int bytes) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(properties.getSigningSecret().getBytes(StandardCharsets.UTF_8), HMAC_ALGO));
            byte[] digest = mac.doFinal((context + sessionId).getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(Arrays.copyOf(digest, bytes));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to derive session key", e);
        }
    }
}
