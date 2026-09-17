package com.example.securedocviewer.exception;

/** Thrown when a tile access token fails signature verification, is malformed, or has expired. */
public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
