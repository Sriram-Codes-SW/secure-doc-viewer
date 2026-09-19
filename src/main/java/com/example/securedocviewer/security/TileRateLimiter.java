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
 * Caps how many tiles a single user can redeem within a rolling time
 * window. Keyed by username rather than session, so signing in again (or in
 * several tabs/browsers at once) doesn't hand out a fresh allowance. A
 * signed, unexpired, session-bound token still only proves the
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

    private final Map<String, Window> windowsByUser = new ConcurrentHashMap<>();
    private final ViewerProperties properties;

    public TileRateLimiter(ViewerProperties properties) {
        this.properties = properties;
    }

    /**
     * Records one tile request for the user and throws if that pushes
     * them over their allowance for the current rolling window.
     */
    /** @return the counted request, for {@link #refund} if it ends up not being served */
    public Instant recordAndEnforce(String username) {
        Window window = windowsByUser.computeIfAbsent(username, id -> new Window());
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
                        "Tile rate limit reached (" + properties.getTileRateLimitPerWindow()
                                + " per " + properties.getTileRateLimitWindowSeconds() + "s).",
                        retryAfterSeconds);
            }
            window.timestamps.addLast(now);
        }
        return now;
    }

    /** Hands back a counted request that was refused for reasons that aren't the reader's (server busy). */
    public void refund(String username, Instant counted) {
        Window window = windowsByUser.get(username);
        if (window != null) {
            synchronized (window) {
                window.timestamps.removeLastOccurrence(counted);
            }
        }
    }

    /** Drops windows with no requests left in them, so the map can't grow without bound. */
    @org.springframework.scheduling.annotation.Scheduled(fixedDelay = 300_000)
    public void sweep() {
        Instant cutoff = Instant.now().minusSeconds(properties.getTileRateLimitWindowSeconds());
        windowsByUser.entrySet().removeIf(entry -> {
            synchronized (entry.getValue()) {
                Deque<Instant> timestamps = entry.getValue().timestamps;
                while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(cutoff)) {
                    timestamps.pollFirst();
                }
                return timestamps.isEmpty();
            }
        });
    }

    /** Drops tracking for a user, so memory doesn't grow forever. */
    public void forget(String username) {
        windowsByUser.remove(username);
    }

    /**
     * Read-only snapshot of a user's current usage, for the admin module.
     * Prunes the same way {@link #recordAndEnforce} does but never records a
     * new request or throws, so merely checking status can't itself count
     * against the user's budget.
     */
    public RateLimitStatus getUsage(String username) {
        Window window = windowsByUser.get(username);
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
                username,
                used,
                properties.getTileRateLimitPerWindow(),
                properties.getTileRateLimitWindowSeconds()
        );
    }
}
