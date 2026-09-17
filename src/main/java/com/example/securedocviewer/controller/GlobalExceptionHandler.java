package com.example.securedocviewer.controller;

import com.example.securedocviewer.exception.DocumentNotFoundException;
import com.example.securedocviewer.exception.InvalidTokenException;
import com.example.securedocviewer.exception.SessionExpiredException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Map<String, String>> handleInvalidToken(InvalidTokenException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(SessionExpiredException.class)
    public ResponseEntity<Map<String, String>> handleSessionExpired(SessionExpiredException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(DocumentNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(DocumentNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }
}
