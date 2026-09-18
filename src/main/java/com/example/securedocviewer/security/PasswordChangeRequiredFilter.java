package com.example.securedocviewer.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * While a session is flagged as needing a password change (the account's
 * password was set by an admin), everything under /api except the auth
 * endpoints is refused with 403. Enforced here, not just in the UI.
 */
public class PasswordChangeRequiredFilter extends OncePerRequestFilter {

    public static final String SESSION_ATTRIBUTE = "sdv.mustChangePassword";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (session != null && Boolean.TRUE.equals(session.getAttribute(SESSION_ATTRIBUTE))
                && path.startsWith("/api/") && !path.startsWith("/api/auth/")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"You must change your password before continuing.\",\"passwordChangeRequired\":true}");
            return;
        }
        chain.doFilter(request, response);
    }
}
