package com.example.securedocviewer.service;

import com.example.securedocviewer.config.ViewerProperties;
import com.example.securedocviewer.exception.InvalidTokenException;
import com.example.securedocviewer.model.SignedTilePayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SignedUrlServiceTest {

    private ViewerProperties properties;
    private SignedUrlService service;

    @BeforeEach
    void setUp() {
        properties = new ViewerProperties();
        properties.setSigningSecret("unit-test-secret");
        properties.setUrlTtlSeconds(60);
        service = new SignedUrlService(properties);
    }

    @Test
    void issuedTokenRoundTripsToTheSamePayload() {
        String token = service.issueToken("doc-1", 2, 3, 4, "session-abc");

        SignedTilePayload payload = service.verifyAndDecode(token);

        assertEquals("doc-1", payload.documentId());
        assertEquals(2, payload.page());
        assertEquals(3, payload.row());
        assertEquals(4, payload.col());
        assertEquals("session-abc", payload.sessionBinding());
    }

    @Test
    void tamperedSignatureIsRejected() {
        String token = service.issueToken("doc-1", 0, 0, 0, "session-abc");
        String[] parts = token.split("\\.", 2);
        // Flip the last character of the signature — payload is untouched.
        char[] sig = parts[1].toCharArray();
        sig[sig.length - 1] = sig[sig.length - 1] == 'A' ? 'B' : 'A';
        String tampered = parts[0] + "." + new String(sig);

        assertThrows(InvalidTokenException.class, () -> service.verifyAndDecode(tampered));
    }

    @Test
    void tamperedPayloadIsRejectedEvenIfSignatureFormatLooksValid() {
        String token = service.issueToken("doc-1", 0, 0, 0, "session-abc");
        String[] parts = token.split("\\.", 2);
        // Re-issue a token for a different tile but splice in the original signature.
        String otherToken = service.issueToken("doc-1", 0, 0, 1, "session-abc");
        String otherPayload = otherToken.split("\\.", 2)[0];
        String frankenToken = otherPayload + "." + parts[1];

        assertThrows(InvalidTokenException.class, () -> service.verifyAndDecode(frankenToken));
    }

    @Test
    void expiredTokenIsRejected() {
        properties.setUrlTtlSeconds(-1); // already in the past the moment it's issued
        String token = service.issueToken("doc-1", 0, 0, 0, "session-abc");

        InvalidTokenException ex = assertThrows(InvalidTokenException.class, () -> service.verifyAndDecode(token));
        assertTrue(ex.getMessage().toLowerCase().contains("expired"));
    }

    @Test
    void malformedTokenIsRejected() {
        assertThrows(InvalidTokenException.class, () -> service.verifyAndDecode("not-a-real-token"));
    }
}
