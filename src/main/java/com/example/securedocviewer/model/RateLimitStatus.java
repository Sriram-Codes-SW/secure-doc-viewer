package com.example.securedocviewer.model;

/**
 * A point-in-time read of a session's tile-request budget, for display in
 * the admin module. {@code used} already reflects the same window-pruning
 * {@link com.example.securedocviewer.security.TileRateLimiter} applies
 * before enforcing a limit, so it never overstates how close a session is
 * to being throttled.
 */
public record RateLimitStatus(
        String sessionId,
        int used,
        int limit,
        long windowSeconds
) {
}
