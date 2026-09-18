package com.example.securedocviewer.controller;

import jakarta.validation.constraints.Size;
import com.example.securedocviewer.account.UserAccountService;
import com.example.securedocviewer.audit.AuditEvent;
import com.example.securedocviewer.audit.AuditEvent.Subject;
import com.example.securedocviewer.audit.AuditEventType;
import com.example.securedocviewer.audit.AuditLogService;
import com.example.securedocviewer.audit.RequestActors;
import com.example.securedocviewer.model.RateLimitStatus;
import com.example.securedocviewer.model.SessionSummary;
import com.example.securedocviewer.security.SessionAdministration;
import com.example.securedocviewer.security.TileRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Visibility into what the security layer is enforcing: who is signed in,
 * how close each user is to the tile rate limit, and the audit trail. ADMIN
 * only (see SecurityConfig). Sessions are exposed by opaque handle for
 * revocation — never by id, which is a credential.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    static final int MAX_EXPORT_ROWS = 50_000;

    private final SessionAdministration sessions;
    private final TileRateLimiter tileRateLimiter;
    private final AuditLogService auditLogService;
    private final RequestActors actors;

    public AdminController(SessionAdministration sessions,
                           TileRateLimiter tileRateLimiter,
                           AuditLogService auditLogService,
                           RequestActors actors) {
        this.sessions = sessions;
        this.tileRateLimiter = tileRateLimiter;
        this.auditLogService = auditLogService;
        this.actors = actors;
    }

    @GetMapping("/sessions")
    public List<SessionSummary> sessions(HttpServletRequest request) {
        return sessions.list(request.getSession().getId());
    }

    @DeleteMapping("/sessions/{handle}")
    public ResponseEntity<Void> revokeSession(@PathVariable String handle, Authentication authentication,
                                              HttpServletRequest request) {
        String revokedUser = sessions.revoke(handle);
        auditLogService.record(AuditEventType.SESSION_REVOKED, actors.of(request, authentication),
                Subject.detail("session of " + revokedUser));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/rate-limit/{username}")
    public RateLimitStatus rateLimit(@PathVariable String username) {
        return tileRateLimiter.getUsage(UserAccountService.normalizeUsername(username));
    }

    @GetMapping("/audit")
    public AuditLogService.Page audit(
            @RequestParam(required = false) AuditEventType type,
            @RequestParam(required = false) @Size(max = 64) String username,
            @RequestParam(required = false) @Size(max = 64) String documentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) @Size(max = 64) String trace,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(500) int size) {
        return auditLogService.search(new AuditLogService.Query(type, username, documentId, from, to, trace), page, size);
    }

    /** The same filters as {@link #audit}, as CSV (newest first, capped at 50,000 rows). */
    @GetMapping(value = "/audit/export", produces = "text/csv")
    public ResponseEntity<String> exportAudit(
            @RequestParam(required = false) AuditEventType type,
            @RequestParam(required = false) @Size(max = 64) String username,
            @RequestParam(required = false) @Size(max = 64) String documentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) @Size(max = 64) String trace) {
        List<AuditEvent> events = auditLogService.export(
                new AuditLogService.Query(type, username, documentId, from, to, trace), MAX_EXPORT_ROWS);
        String header = "time_utc,event,username,client_ip,session,document_id,document_title,page,tile_row,tile_col,detail";
        String rows = events.stream().map(AdminController::csvRow).collect(Collectors.joining("\n"));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"audit-log.csv\"")
                .contentType(new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8))
                .body(header + "\n" + rows + (rows.isEmpty() ? "" : "\n"));
    }

    private static String csvRow(AuditEvent e) {
        return String.join(",",
                Instant.ofEpochMilli(e.occurredAtEpochMillis()).toString(),
                e.type().name(),
                csv(e.username()), csv(e.clientIp()), csv(e.sessionHandle()),
                csv(e.documentId()), csv(e.documentTitle()),
                e.page() == null ? "" : String.valueOf(e.page() + 1),
                e.tileRow() == null ? "" : e.tileRow().toString(),
                e.tileCol() == null ? "" : e.tileCol().toString(),
                csv(e.detail()));
    }

    /**
     * Quotes a field and neutralises spreadsheet formulas: a title such as
     * "=HYPERLINK(...)" would otherwise execute when the export is opened in Excel.
     */
    static String csv(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        String safe = "=+-@\t\r".indexOf(value.charAt(0)) >= 0 ? "'" + value : value;
        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }
}
