package com.example.securedocviewer.security;

import com.example.securedocviewer.config.ViewerProperties;
import com.example.securedocviewer.exception.SessionExpiredException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Minimal in-memory session store standing in for a real auth system
 * (Spring Security + a proper identity provider, in a non-demo build).
 * The point this module demonstrates is that a signed tile URL is bound to
 * a session id, not just to a document — so revoking the session (logout,
 * or subscription lapsing) kills every outstanding URL issued under it,
 * independent of whether the URL's own HMAC expiry has passed yet.
 */
@Service
public class SessionService {

    private record Session(String username, Instant expiresAt) {
    }

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final ViewerProperties properties;

    public SessionService(ViewerProperties properties) {
        this.properties = properties;
    }

    public String login(String username) {
        String sessionId = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plusSeconds(properties.getSessionTtlSeconds());
        sessions.put(sessionId, new Session(username, expiresAt));
        return sessionId;
    }

    public void logout(String sessionId) {
        sessions.remove(sessionId);
    }

    /**
     * @return the username bound to this session
     * @throws SessionExpiredException if the session id is unknown or has expired
     */
    public String requireValidSession(String sessionId) {
        Session session = sessions.get(sessionId);
        if (session == null) {
            throw new SessionExpiredException("Session not found or already logged out: " + sessionId);
        }
        if (Instant.now().isAfter(session.expiresAt())) {
            sessions.remove(sessionId);
            throw new SessionExpiredException("Session expired: " + sessionId);
        }
        return session.username();
    }

    public boolean isValid(String sessionId) {
        try {
            requireValidSession(sessionId);
            return true;
        } catch (SessionExpiredException e) {
            return false;
        }
    }
}
