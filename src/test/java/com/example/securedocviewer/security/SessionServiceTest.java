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

    @Test
    void listActiveSessionsIncludesOnlyUnexpiredSessions() {
        SessionService service = serviceWithTtl(300);
        String aliceSession = service.login("alice");
        String bobSession = service.login("bob");
        service.logout(bobSession);

        var summaries = service.listActiveSessions();

        assertEquals(1, summaries.size());
        assertEquals(aliceSession, summaries.get(0).sessionId());
        assertEquals("alice", summaries.get(0).username());
    }

    @Test
    void listActiveSessionsExcludesExpiredSessions() {
        SessionService service = serviceWithTtl(-1);
        service.login("alice");

        assertTrue(service.listActiveSessions().isEmpty());
    }
}
