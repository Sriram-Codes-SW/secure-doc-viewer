package com.example.securedocviewer.exception;

/** A request that is well-formed HTTP but breaks a business rule; mapped to 400. */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
