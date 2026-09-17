package com.example.securedocviewer.controller;

import com.example.securedocviewer.security.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Stands in for real authentication. In this demo, "logging in" just mints
 * a session id for whatever username you give it — the interesting part of
 * the module is what happens after login (signed, session-bound tile URLs),
 * not how login itself works.
 */
@RestController
@RequestMapping("/api/session")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestParam String username) {
        String sessionId = sessionService.login(username);
        return ResponseEntity.ok(Map.of("sessionId", sessionId, "username", username));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("X-Session-Id") String sessionId) {
        sessionService.logout(sessionId);
        return ResponseEntity.noContent().build();
    }
}
