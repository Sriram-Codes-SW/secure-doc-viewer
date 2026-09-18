package com.example.securedocviewer.exception;

/**
 * The user may see the resource but not change it; mapped to 403. Resources
 * the user may not see at all are reported as 404 instead, so their
 * existence isn't revealed.
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
