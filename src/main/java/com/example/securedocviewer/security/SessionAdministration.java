package com.example.securedocviewer.security;

import com.example.securedocviewer.exception.ResourceNotFoundException;
import com.example.securedocviewer.model.SessionSummary;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Admin-facing view of live sessions. Sessions are identified only by
 * {@link SessionKeys#adminHandle}, never by id. Expiring a session in the
 * registry makes Spring Security reject its very next request, which also
 * kills any tile URLs issued to it.
 */
@Service
public class SessionAdministration {

    private final SessionRegistry registry;
    private final SessionKeys sessionKeys;

    public SessionAdministration(SessionRegistry registry, SessionKeys sessionKeys) {
        this.registry = registry;
        this.sessionKeys = sessionKeys;
    }

    public List<SessionSummary> list(String currentSessionId) {
        List<SessionSummary> summaries = new ArrayList<>();
        for (Object principal : registry.getAllPrincipals()) {
            if (!(principal instanceof UserDetails user)) {
                continue;
            }
            String role = user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(a -> a.startsWith("ROLE_"))
                    .map(a -> a.substring("ROLE_".length()))
                    .findFirst()
                    .orElse("");
            for (SessionInformation session : registry.getAllSessions(principal, false)) {
                summaries.add(new SessionSummary(
                        sessionKeys.adminHandle(session.getSessionId()),
                        user.getUsername(),
                        role,
                        session.getLastRequest().toInstant().getEpochSecond(),
                        session.getSessionId().equals(currentSessionId)));
            }
        }
        summaries.sort(Comparator.comparingLong(SessionSummary::lastActiveEpochSeconds).reversed());
        return summaries;
    }

    /** Expires the session with this handle and returns whose it was. */
    public String revoke(String handle) {
        for (Object principal : registry.getAllPrincipals()) {
            for (SessionInformation session : registry.getAllSessions(principal, false)) {
                if (sessionKeys.adminHandle(session.getSessionId()).equals(handle)) {
                    session.expireNow();
                    return principal instanceof UserDetails user ? user.getUsername() : String.valueOf(principal);
                }
            }
        }
        throw new ResourceNotFoundException("No active session with that handle.");
    }

    /**
     * Ends every session a user has, e.g. after their role changes, they're
     * disabled, or their password is reset — so the change takes effect now,
     * not when their session happens to time out.
     */
    public void revokeAllFor(String username, String exceptSessionId) {
        for (Object principal : registry.getAllPrincipals()) {
            if (principal instanceof UserDetails user && user.getUsername().equals(username)) {
                for (SessionInformation session : registry.getAllSessions(principal, false)) {
                    if (!session.getSessionId().equals(exceptSessionId)) {
                        session.expireNow();
                    }
                }
            }
        }
    }
}
