package com.example.securedocviewer.model;

/**
 * One live sign-in as the admin UI sees it. {@code handle} is an opaque,
 * revocation-only reference derived from the session id; the id itself is
 * never exposed.
 */
public record SessionSummary(
        String handle,
        String username,
        String role,
        long lastActiveEpochSeconds,
        boolean current,
        String clientIp,
        String device,
        Long startedAtEpochSeconds
) {
}
