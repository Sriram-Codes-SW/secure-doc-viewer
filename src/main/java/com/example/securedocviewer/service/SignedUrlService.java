package com.example.securedocviewer.service;

import com.example.securedocviewer.config.ViewerProperties;
import com.example.securedocviewer.exception.InvalidTokenException;
import com.example.securedocviewer.model.SignedTilePayload;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

/**
 * Issues and verifies HMAC-signed, short-lived tokens that grant access to
 * exactly one tile of one page of one document, for one session, until a
 * fixed expiry. This is the same shape as an S3/CloudFront presigned URL:
 * anyone who has the token can use it until it expires, but they cannot
 * forge a new one, extend it, or repurpose it for a different tile, because
 * the signature covers every field.
 *
 * Token format: base64url(payload) + "." + base64url(hmacSha256(payload))
 * where payload is the pipe-delimited canonical string from
 * {@link SignedTilePayload#canonicalString()}.
 */
@Service
public class SignedUrlService {

    private static final String HMAC_ALGO = "HmacSHA256";

    private final ViewerProperties properties;

    public SignedUrlService(ViewerProperties properties) {
        this.properties = properties;
    }

    public String issueToken(String documentId, int page, int row, int col, String sessionBinding) {
        long expiresAt = Instant.now().getEpochSecond() + properties.getUrlTtlSeconds();
        SignedTilePayload payload = new SignedTilePayload(documentId, page, row, col, sessionBinding, expiresAt);
        String payloadEncoded = base64Url(payload.canonicalString().getBytes(StandardCharsets.UTF_8));
        String signature = base64Url(hmac(payload.canonicalString()));
        return payloadEncoded + "." + signature;
    }

    /**
     * Verifies the signature and expiry of a token and returns its decoded
     * contents. Does NOT check that the request comes from the session the
     * token is bound to, or that the session is still live — TileController
     * does that separately (Spring Security plus SessionKeys), so the two
     * failure modes (tampered/expired token vs. wrong or revoked session)
     * stay distinguishable.
     */
    public SignedTilePayload verifyAndDecode(String token) {
        String[] parts = token.split("\\.", 2);
        if (parts.length != 2) {
            throw new InvalidTokenException("Malformed token");
        }

        String payloadEncoded = parts[0];
        String providedSignature = parts[1];

        byte[] payloadBytes;
        try {
            payloadBytes = Base64.getUrlDecoder().decode(payloadEncoded);
        } catch (IllegalArgumentException e) {
            throw new InvalidTokenException("Malformed token payload");
        }
        String canonical = new String(payloadBytes, StandardCharsets.UTF_8);

        String expectedSignature = base64Url(hmac(canonical));
        if (!constantTimeEquals(expectedSignature, providedSignature)) {
            throw new InvalidTokenException("Signature mismatch — token was tampered with or forged");
        }

        SignedTilePayload payload = parseCanonical(canonical);
        if (Instant.now().getEpochSecond() > payload.expiresAtEpochSeconds()) {
            throw new InvalidTokenException("Token expired at " + Instant.ofEpochSecond(payload.expiresAtEpochSeconds()));
        }

        return payload;
    }

    private SignedTilePayload parseCanonical(String canonical) {
        String[] fields = canonical.split("\\|");
        if (fields.length != 6) {
            throw new InvalidTokenException("Malformed token payload fields");
        }
        try {
            return new SignedTilePayload(
                    fields[0],
                    Integer.parseInt(fields[1]),
                    Integer.parseInt(fields[2]),
                    Integer.parseInt(fields[3]),
                    fields[4],
                    Long.parseLong(fields[5])
            );
        } catch (NumberFormatException e) {
            throw new InvalidTokenException("Malformed token payload numbers");
        }
    }

    private byte[] hmac(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(properties.getSigningSecret().getBytes(StandardCharsets.UTF_8), HMAC_ALGO));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to compute HMAC", e);
        }
    }

    private String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** Avoids leaking timing information about how much of the signature matched. */
    private boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8)
        );
    }
}
