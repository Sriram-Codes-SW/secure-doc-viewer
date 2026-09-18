package com.example.securedocviewer.exception;

/** The current password given when changing it was wrong: counted like a failed sign-in. */
public class WrongPasswordException extends BadRequestException {

    public WrongPasswordException() {
        super("Current password is incorrect.");
    }
}
