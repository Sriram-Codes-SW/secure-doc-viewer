package com.example.securedocviewer.exception;

/**
 * Thrown when a token's HMAC and expiry are still valid but the session it
 * is bound to has since expired or been logged out. This is the second,
 * independent layer of revocation: killing a session invalidates every
 * outstanding signed URL issued under it, even ones that haven't hit their
 * own expiry yet.
 */
public class SessionExpiredException extends RuntimeException {
    public SessionExpiredException(String message) {
        super(message);
    }
}
