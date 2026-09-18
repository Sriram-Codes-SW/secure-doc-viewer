package com.example.securedocviewer.audit;

import com.example.securedocviewer.audit.AuditEvent.Actor;
import com.example.securedocviewer.security.SessionKeys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/** Builds the audit {@link Actor} for the current request: user, session handle, client IP. */
@Component
public class RequestActors {

    private final SessionKeys sessionKeys;

    public RequestActors(SessionKeys sessionKeys) {
        this.sessionKeys = sessionKeys;
    }

    public Actor of(HttpServletRequest request, Authentication authentication) {
        return of(request, authentication == null ? null : authentication.getName());
    }

    public Actor of(HttpServletRequest request, String username) {
        HttpSession session = request.getSession(false);
        String handle = session == null ? null : sessionKeys.adminHandle(session.getId());
        return new Actor(username, handle, request.getRemoteAddr());
    }
}
