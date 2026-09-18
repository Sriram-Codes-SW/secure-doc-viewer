package com.example.securedocviewer.controller;

import com.example.securedocviewer.account.UserAccountService;
import com.example.securedocviewer.model.AuditEntry;
import com.example.securedocviewer.model.RateLimitStatus;
import com.example.securedocviewer.model.SessionSummary;
import com.example.securedocviewer.security.SessionAdministration;
import com.example.securedocviewer.security.TileRateLimiter;
import com.example.securedocviewer.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Visibility into what the security layer is enforcing: who is signed in,
 * how close each user is to the tile rate limit, and which tiles were
 * redeemed by whom. ADMIN only (see SecurityConfig). Sessions are exposed by
 * opaque handle for revocation — never by id, which is a credential.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final SessionAdministration sessions;
    private final TileRateLimiter tileRateLimiter;
    private final AuditLogService auditLogService;

    public AdminController(SessionAdministration sessions,
                           TileRateLimiter tileRateLimiter,
                           AuditLogService auditLogService) {
        this.sessions = sessions;
        this.tileRateLimiter = tileRateLimiter;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/sessions")
    public List<SessionSummary> sessions(HttpServletRequest request) {
        return sessions.list(request.getSession().getId());
    }

    @DeleteMapping("/sessions/{handle}")
    public ResponseEntity<Void> revokeSession(@PathVariable String handle) {
        sessions.revoke(handle);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/rate-limit/{username}")
    public RateLimitStatus rateLimit(@PathVariable String username) {
        return tileRateLimiter.getUsage(UserAccountService.normalizeUsername(username));
    }

    @GetMapping("/audit")
    public List<AuditEntry> audit(@RequestParam(defaultValue = "50") @Min(1) @Max(500) int limit) {
        return auditLogService.recent(limit);
    }
}
