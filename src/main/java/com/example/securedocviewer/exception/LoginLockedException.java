package com.example.securedocviewer.exception;

/** Too many failed sign-ins; mapped to 429 with Retry-After. */
public class LoginLockedException extends RuntimeException {

    private final long retryAfterSeconds;
    private final String rule;

    public LoginLockedException(long retryAfterSeconds, String rule) {
        super("Too many failed sign-in attempts. Try again later.");
        this.retryAfterSeconds = retryAfterSeconds;
        this.rule = rule;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }

    /** Which throttle rule refused the attempt (for the audit log; never shown to the client). */
    public String getRule() {
        return rule;
    }
}
