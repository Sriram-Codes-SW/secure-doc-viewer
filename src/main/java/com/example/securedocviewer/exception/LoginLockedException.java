package com.example.securedocviewer.exception;

/** Too many failed sign-ins; mapped to 429 with Retry-After. */
public class LoginLockedException extends RuntimeException {

    private final long retryAfterSeconds;

    public LoginLockedException(long retryAfterSeconds) {
        super("Too many failed sign-in attempts. Try again later.");
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
