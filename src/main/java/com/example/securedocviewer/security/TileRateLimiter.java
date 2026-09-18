package com.example.securedocviewer.security;

import com.example.securedocviewer.config.ViewerProperties;
import com.example.securedocviewer.exception.RateLimitExceededException;
import com.example.securedocviewer.model.RateLimitStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caps how many tiles a single session can redeem within a rolling time
 * window. A signed, unexpired, session-bound token still only proves the
 * request is legitimate one tile at a time — nothing about the token
 * mechanism stops a script from redeeming every tile of every page in a
 * few seconds. This is what turns that into a slow, boundable operation
 * instead of an instant one, so bulk harvesting either takes long enough
 * to notice or is capped outright by {@code tile-rate-limit-per-window}.
 */
@Service
public class TileRateLimiter {

    private static final class Window {
        final Deque<Instant> timestamps = new ArrayDeque<>();
    }

    private final Map<String, Window> windowsBySession = new ConcurrentHashMap<>();
    private final ViewerProperties properties;

    public TileRateLimiter(ViewerProperties properties) {
        this.properties = properties;
    }

    /**
     * Records one tile request for the session and throws if that pushes
     * the session over its allowance for the current rolling window.
     */
    public void recordAndEnforce(String sessionId) {
        Window window = windowsBySession.computeIfAbsent(sessionId, id -> new Window());
        Instant now = Instant.now();
        Instant cutoff = now.minusSeconds(properties.getTileRateLimitWindowSeconds());

        synchronized (window) {
            while (!window.timestamps.isEmpty() && window.timestamps.peekFirst().isBefore(cutoff)) {
                window.timestamps.pollFirst();
            }
            if (window.timestamps.size() >= properties.getTileRateLimitPerWindow()) {
                // The next slot frees up when the oldest counted request
                // slides out of the window; tell the client exactly when.
                long retryAfterSeconds = Math.max(1, (long) Math.ceil(
                        Duration.between(cutoff, window.timestamps.peekFirst()).toMillis() / 1000.0));
                throw new RateLimitExceededException(
                        "Session " + sessionId + " exceeded " + properties.getTileRateLimitPerWindow()
                                + " tile requests per " + properties.getTileRateLimitWindowSeconds() + "s window",
                        retryAfterSeconds);
            }
            window.timestamps.addLast(now);
        }
    }

    /** Drops tracking for a session, e.g. on logout, so memory doesn't grow forever. */
    public void forget(String sessionId) {
        windowsBySession.remove(sessionId);
    }

    /**
     * Read-only snapshot of a session's current usage, for the admin module.
     * Prunes the same way {@link #recordAndEnforce} does but never records a
     * new request or throws, so merely checking status can't itself count
     * against the session's budget.
     */
    public RateLimitStatus getUsage(String sessionId) {
        Window window = windowsBySession.get(sessionId);
        int used;
        if (window == null) {
            used = 0;
        } else {
            Instant cutoff = Instant.now().minusSeconds(properties.getTileRateLimitWindowSeconds());
            synchronized (window) {
                while (!window.timestamps.isEmpty() && window.timestamps.peekFirst().isBefore(cutoff)) {
                    window.timestamps.pollFirst();
                }
                used = window.timestamps.size();
            }
        }
        return new RateLimitStatus(
                sessionId,
                used,
                properties.getTileRateLimitPerWindow(),
                properties.getTileRateLimitWindowSeconds()
        );
    }
}
