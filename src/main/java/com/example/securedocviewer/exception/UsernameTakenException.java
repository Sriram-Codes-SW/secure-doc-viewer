package com.example.securedocviewer.exception;

/** Mapped to 409. */
public class UsernameTakenException extends RuntimeException {
    public UsernameTakenException(String username) {
        super("Username already exists: " + username);
    }
}
