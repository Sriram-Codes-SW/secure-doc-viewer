package com.example.securedocviewer.exception;

/** Temporarily at capacity (e.g. too many PDFs rendering); mapped to 503 with Retry-After. */
public class ServiceBusyException extends RuntimeException {

    private final long retryAfterSeconds;

    public ServiceBusyException(String message, long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
