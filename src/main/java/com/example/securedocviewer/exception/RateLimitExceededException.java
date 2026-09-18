package com.example.securedocviewer.exception;

/**
 * Thrown when a session requests more tiles than its rate limit allows
 * within the configured window. A valid signature, unexpired token, and
 * live session together still only grant access one tile at a time —
 * this is the throttle that keeps a script from redeeming every tile of
 * every page of a document in a single burst.
 */
public class RateLimitExceededException extends RuntimeException {

    private final long retryAfterSeconds;

    public RateLimitExceededException(String message, long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    /** Seconds until this session can redeem another tile; sent as {@code Retry-After}. */
    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
