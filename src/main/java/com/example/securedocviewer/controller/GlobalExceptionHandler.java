package com.example.securedocviewer.controller;

import com.example.securedocviewer.exception.DocumentNotFoundException;
import com.example.securedocviewer.exception.InvalidTokenException;
import com.example.securedocviewer.exception.RateLimitExceededException;
import com.example.securedocviewer.exception.SessionExpiredException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Every error body is JSON with an explicitly set Content-Type. Without it,
 * Spring negotiates the error body against the request's Accept header, and
 * the tile endpoint's callers may accept only images — the JSON body is then
 * "not acceptable", the handler itself fails, and a clean 401/429 turns
 * into a 500.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Map<String, String>> handleInvalidToken(InvalidTokenException e) {
        return error(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(SessionExpiredException.class)
    public ResponseEntity<Map<String, String>> handleSessionExpired(SessionExpiredException e) {
        return error(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(DocumentNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(DocumentNotFoundException e) {
        return error(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<Map<String, String>> handleRateLimitExceeded(RateLimitExceededException e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(e.getRetryAfterSeconds()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("error", e.getMessage()));
    }

    private static ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("error", message));
    }
}
