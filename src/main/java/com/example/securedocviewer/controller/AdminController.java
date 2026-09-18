package com.example.securedocviewer.controller;

import com.example.securedocviewer.model.AuditEntry;
import com.example.securedocviewer.model.RateLimitStatus;
import com.example.securedocviewer.model.SessionSummary;
import com.example.securedocviewer.security.SessionService;
import com.example.securedocviewer.security.TileRateLimiter;
import com.example.securedocviewer.service.AuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Read-only visibility into the state the other services already track:
 * who's currently logged in, how close each session is to being
 * rate-limited, and which tiles have actually been redeemed and by whom.
 * This module doesn't add new security controls of its own — it surfaces
 * the ones the rest of the backend already enforces, which is exactly what
 * makes the watermark and rate limiter useful in practice (an admin can
 * actually see the audit trail they produce). Gated behind a valid session
 * only, same as everything else here; a real deployment would additionally
 * require an admin role.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final SessionService sessionService;
    private final TileRateLimiter tileRateLimiter;
    private final AuditLogService auditLogService;

    public AdminController(SessionService sessionService,
                            TileRateLimiter tileRateLimiter,
                            AuditLogService auditLogService) {
        this.sessionService = sessionService;
        this.tileRateLimiter = tileRateLimiter;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<SessionSummary>> sessions(@RequestHeader("X-Session-Id") String sessionId) {
        sessionService.requireValidSession(sessionId);
        return ResponseEntity.ok(sessionService.listActiveSessions());
    }

    @GetMapping("/rate-limit/{targetSessionId}")
    public ResponseEntity<RateLimitStatus> rateLimit(
            @RequestHeader("X-Session-Id") String sessionId,
            @PathVariable String targetSessionId) {

        sessionService.requireValidSession(sessionId);
        return ResponseEntity.ok(tileRateLimiter.getUsage(targetSessionId));
    }

    @GetMapping("/audit")
    public ResponseEntity<List<AuditEntry>> audit(
            @RequestHeader("X-Session-Id") String sessionId,
            @RequestParam(defaultValue = "50") int limit) {

        sessionService.requireValidSession(sessionId);
        return ResponseEntity.ok(auditLogService.recent(limit));
    }
}
