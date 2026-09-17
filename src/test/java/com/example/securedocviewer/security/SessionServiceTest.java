package com.example.securedocviewer.security;

import com.example.securedocviewer.config.ViewerProperties;
import com.example.securedocviewer.exception.SessionExpiredException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SessionServiceTest {

    private SessionService serviceWithTtl(long ttlSeconds) {
        ViewerProperties properties = new ViewerProperties();
        properties.setSessionTtlSeconds(ttlSeconds);
        return new SessionService(properties);
    }

    @Test
    void loginProducesAValidSessionBoundToTheUsername() {
        SessionService service = serviceWithTtl(300);
        String sessionId = service.login("alice");

        assertEquals("alice", service.requireValidSession(sessionId));
        assertTrue(service.isValid(sessionId));
    }

    @Test
    void logoutImmediatelyInvalidatesTheSession() {
        SessionService service = serviceWithTtl(300);
        String sessionId = service.login("alice");

        service.logout(sessionId);

        assertFalse(service.isValid(sessionId));
        assertThrows(SessionExpiredException.class, () -> service.requireValidSession(sessionId));
    }

    @Test
    void expiredSessionIsRejected() {
        SessionService service = serviceWithTtl(-1); // expires the instant it is created
        String sessionId = service.login("alice");

        assertThrows(SessionExpiredException.class, () -> service.requireValidSession(sessionId));
    }

    @Test
    void unknownSessionIsRejected() {
        SessionService service = serviceWithTtl(300);
        assertThrows(SessionExpiredException.class, () -> service.requireValidSession("never-issued"));
    }
}
