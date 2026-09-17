package com.example.securedocviewer.exception;

/** Thrown when a requested document, page, or tile does not exist on disk. */
public class DocumentNotFoundException extends RuntimeException {
    public DocumentNotFoundException(String message) {
        super(message);
    }
}
