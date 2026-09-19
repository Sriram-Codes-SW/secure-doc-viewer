package com.example.securedocviewer.security;

import com.example.securedocviewer.config.ViewerProperties;
import com.example.securedocviewer.exception.RateLimitExceededException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TileRateLimiterTest {

    private TileRateLimiter limiterWith(int perWindow, long windowSeconds) {
        ViewerProperties properties = new ViewerProperties();
        properties.setTileRateLimitPerWindow(perWindow);
        properties.setTileRateLimitWindowSeconds(windowSeconds);
        return new TileRateLimiter(properties);
    }

    @Test
    void allowsRequestsUpToTheLimit() {
        TileRateLimiter limiter = limiterWith(3, 60);

        assertDoesNotThrow(() -> {
            limiter.recordAndEnforce("session-1");
            limiter.recordAndEnforce("session-1");
            limiter.recordAndEnforce("session-1");
        });
    }

    @Test
    void rejectsTheRequestThatCrossesTheLimit() {
        TileRateLimiter limiter = limiterWith(2, 60);

        limiter.recordAndEnforce("session-1");
        limiter.recordAndEnforce("session-1");

        assertThrows(RateLimitExceededException.class, () -> limiter.recordAndEnforce("session-1"));
    }

    @Test
    void rejectionCarriesRetryAfterWithinTheWindow() {
        TileRateLimiter limiter = limiterWith(1, 60);

        limiter.recordAndEnforce("session-1");
        RateLimitExceededException e =
                assertThrows(RateLimitExceededException.class, () -> limiter.recordAndEnforce("session-1"));

        // The only counted request was just made, so its slot frees up
        // roughly a full window from now — never 0, never past the window.
        assertTrue(e.getRetryAfterSeconds() >= 1 && e.getRetryAfterSeconds() <= 60,
                "retryAfter out of range: " + e.getRetryAfterSeconds());
    }

    @Test
    void rejectedRequestsDoNotConsumeBudget() {
        TileRateLimiter limiter = limiterWith(1, 60);

        limiter.recordAndEnforce("session-1");
        assertThrows(RateLimitExceededException.class, () -> limiter.recordAndEnforce("session-1"));
        assertThrows(RateLimitExceededException.class, () -> limiter.recordAndEnforce("session-1"));

        assertEquals(1, limiter.getUsage("session-1").used());
    }

    @Test
    void sessionsAreTrackedIndependently() {
        TileRateLimiter limiter = limiterWith(1, 60);

        limiter.recordAndEnforce("session-1");

        assertDoesNotThrow(() -> limiter.recordAndEnforce("session-2"));
        assertThrows(RateLimitExceededException.class, () -> limiter.recordAndEnforce("session-1"));
    }

    @Test
    void anExpiredWindowLetsTheLimitResetImmediately() throws InterruptedException {
        TileRateLimiter limiter = limiterWith(1, -1); // window is already in the past the instant it's recorded

        limiter.recordAndEnforce("session-1");

        assertDoesNotThrow(() -> limiter.recordAndEnforce("session-1"));
    }

    @Test
    void forgetDropsTheWindowSoLimitResetsForThatSession() {
        TileRateLimiter limiter = limiterWith(1, 60);

        limiter.recordAndEnforce("session-1");
        limiter.forget("session-1");

        assertDoesNotThrow(() -> limiter.recordAndEnforce("session-1"));
    }

    @Test
    void getUsageReflectsRecordedRequestsWithoutCountingItself() {
        TileRateLimiter limiter = limiterWith(5, 60);

        limiter.recordAndEnforce("session-1");
        limiter.recordAndEnforce("session-1");

        var status = limiter.getUsage("session-1");

        assertEquals(2, status.used());
        assertEquals(5, status.limit());
        // Checking usage twice must not itself consume budget.
        assertEquals(2, limiter.getUsage("session-1").used());
    }

    @Test
    void getUsageForAnUntrackedSessionIsZero() {
        TileRateLimiter limiter = limiterWith(5, 60);

        var status = limiter.getUsage("never-seen");

        assertEquals(0, status.used());
        assertEquals(5, status.limit());
    }

    @Test
    void getUsagePrunesExpiredEntriesBeforeReporting() {
        TileRateLimiter limiter = limiterWith(5, -1); // window is already in the past

        limiter.recordAndEnforce("session-1");

        assertEquals(0, limiter.getUsage("session-1").used());
    }

    @Test
    void aRefundedRequestDoesNotCountAgainstTheReader() {
        ViewerProperties properties = new ViewerProperties();
        properties.setTileRateLimitPerWindow(2);
        TileRateLimiter limiter = new TileRateLimiter(properties);
        limiter.recordAndEnforce("rita");
        java.time.Instant busy = limiter.recordAndEnforce("rita");
        limiter.refund("rita", busy); // the server was busy: not the reader's fault
        assertDoesNotThrow(() -> limiter.recordAndEnforce("rita"));
        assertThrows(com.example.securedocviewer.exception.RateLimitExceededException.class,
                () -> limiter.recordAndEnforce("rita"));
    }
}
