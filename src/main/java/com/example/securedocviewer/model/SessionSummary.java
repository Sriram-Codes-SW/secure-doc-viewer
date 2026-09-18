package com.example.securedocviewer.model;

/**
 * A read-only view of an active session for the admin module — deliberately
 * excludes anything an admin panel doesn't need to render a session list.
 */
public record SessionSummary(
        String sessionId,
        String username,
        long expiresAtEpochSeconds
) {
}
