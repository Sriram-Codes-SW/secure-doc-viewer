package com.example.securedocviewer.controller;

import com.example.securedocviewer.model.SignedTilePayload;
import com.example.securedocviewer.exception.InvalidTokenException;
import com.example.securedocviewer.security.SessionKeys;
import com.example.securedocviewer.security.TileRateLimiter;
import com.example.securedocviewer.audit.AuditEvent.Actor;
import com.example.securedocviewer.audit.AuditEvent.Subject;
import com.example.securedocviewer.audit.AuditEventType;
import com.example.securedocviewer.audit.AuditLogService;
import com.example.securedocviewer.audit.RequestActors;
import com.example.securedocviewer.config.ViewerProperties;
import com.example.securedocviewer.document.DocumentService;
import com.example.securedocviewer.document.Viewer;
import com.example.securedocviewer.exception.DocumentNotFoundException;
import com.example.securedocviewer.exception.RateLimitExceededException;
import com.example.securedocviewer.service.SignedUrlService;
import com.example.securedocviewer.service.TileGenerationService;
import com.example.securedocviewer.service.WatermarkService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;

/**
 * The only endpoint that ever returns pixel data. Every request must carry
 * a token that (a) has a valid HMAC signature, (b) has not passed its own
 * expiry, and (c) was issued to the very session making the request, which
 * must still be signed in, and (d) the user must not have exceeded their
 * tile rate limit, and (e) the user must still be allowed to view the
 * document — re-checked here, so unsharing or deleting a document also cuts
 * off tile URLs that were already issued. Passing all five still only
 * gets you ONE tile of ONE page, watermarked with the requester's identity
 * baked in server-side.
 */
@RestController
public class TileController {

    static final Duration PAGE_VIEW_AUDIT_INTERVAL = Duration.ofMinutes(10);

    private final SignedUrlService signedUrlService;
    private final SessionKeys sessionKeys;
    private final TileRateLimiter tileRateLimiter;
    private final TileGenerationService tileGenerationService;
    private final WatermarkService watermarkService;
    private final AuditLogService auditLogService;
    private final DocumentService documents;
    private final RequestActors actors;
    private final ViewerProperties properties;

    public TileController(SignedUrlService signedUrlService,
                           SessionKeys sessionKeys,
                           TileRateLimiter tileRateLimiter,
                           TileGenerationService tileGenerationService,
                           WatermarkService watermarkService,
                           AuditLogService auditLogService,
                           DocumentService documents,
                           RequestActors actors,
                           ViewerProperties properties) {
        this.signedUrlService = signedUrlService;
        this.sessionKeys = sessionKeys;
        this.tileRateLimiter = tileRateLimiter;
        this.tileGenerationService = tileGenerationService;
        this.watermarkService = watermarkService;
        this.auditLogService = auditLogService;
        this.documents = documents;
        this.actors = actors;
        this.properties = properties;
    }

    @GetMapping(value = "/api/tiles", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getTile(@RequestParam String token,
                                          Authentication authentication,
                                          HttpServletRequest request) throws IOException {
        SignedTilePayload payload = signedUrlService.verifyAndDecode(token);

        // Second, independent check: the request must come from the very
        // session the token was issued to. Spring Security has already
        // rejected the request if that session was logged out, timed out or
        // revoked, so a still-unexpired token dies with its session — and a
        // URL copied into another browser or account fails here.
        HttpSession session = request.getSession(false);
        if (session == null || !sessionKeys.tileBindingMatches(session.getId(), payload.sessionBinding())) {
            throw new InvalidTokenException("This tile link was issued to a different session.");
        }
        String username = authentication.getName();
        Actor actor = actors.of(request, authentication);

        // Third, independent check: a signature- and session-valid request
        // can still be part of a burst trying to redeem every tile of every
        // page. Enforced after auth so unauthenticated requests can't burn
        // a legitimate user's allowance, and before the disk read/render
        // so a throttled request doesn't pay that cost.
        try {
            tileRateLimiter.recordAndEnforce(username);
        } catch (RateLimitExceededException e) {
            auditLogService.recordAtMostEvery(Duration.ofSeconds(properties.getTileRateLimitWindowSeconds()),
                    "rate-limited:" + username, AuditEventType.RATE_LIMITED, actor,
                    Subject.document(payload.documentId(), null));
            throw e;
        }

        // Fourth: the document may have been unshared or deleted since the URL was issued.
        String title = documents.titleIfViewable(payload.documentId(), Viewer.of(authentication))
                .orElseThrow(() -> {
                    auditLogService.recordAtMostEvery(Duration.ofSeconds(5), "denied:" + username,
                            AuditEventType.ACCESS_DENIED, actor, Subject.document(payload.documentId(), null, "tile"));
                    return new DocumentNotFoundException("Document not found.");
                });

        BufferedImage rawTile = tileGenerationService.loadRawTile(
                payload.documentId(), payload.page(), payload.row(), payload.col());

        // First 6 characters of the session's admin handle: enough to single out one sign-in
        // in the audit log's session column, too short to be of any other use.
        String traceCode = sessionKeys.adminHandle(session.getId()).substring(0, 6);
        BufferedImage watermarked = watermarkService.applyWatermark(rawTile, username, traceCode);

        // One event per page view rather than per tile: a page is ~35 tiles, and
        // per-tile rows buried everything else in the audit log.
        auditLogService.recordAtMostEvery(PAGE_VIEW_AUDIT_INTERVAL,
                "page:" + actor.sessionHandle() + "|" + payload.documentId() + "|" + payload.page(),
                AuditEventType.PAGE_VIEWED, actor,
                new Subject(payload.documentId(), title, payload.page(), null, null, null));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(watermarked, "png", out);

        return ResponseEntity.ok()
                // Deliberately not cacheable beyond a moment — a shared cache
                // holding onto a watermarked-for-someone-else tile would leak it.
                .cacheControl(CacheControl.noStore())
                .body(out.toByteArray());
    }
}
