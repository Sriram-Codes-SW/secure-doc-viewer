package com.example.securedocviewer.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

/**
 * Ends a session a fixed time after sign-in, however active it is: the idle
 * timeout alone would let a stolen or forgotten session live forever as long
 * as something keeps using it. The request then continues unauthenticated, so
 * the normal 401 is returned and the app sends the user to sign in again.
 */
public class SessionLifetimeFilter extends OncePerRequestFilter {

    /** Set at sign-in by AuthController. */
    public static final String SIGNED_IN_AT = "sdv.signedInAt";

    private final Duration maxLifetime;

    public SessionLifetimeFilter(Duration maxLifetime) {
        this.maxLifetime = maxLifetime;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(SIGNED_IN_AT) instanceof Instant signedInAt
                && Instant.now().isAfter(signedInAt.plus(maxLifetime))) {
            session.invalidate();
            SecurityContextHolder.clearContext();
        }
        chain.doFilter(request, response);
    }
}
