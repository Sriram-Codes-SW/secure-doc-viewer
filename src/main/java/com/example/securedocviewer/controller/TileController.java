package com.example.securedocviewer.controller;

import com.example.securedocviewer.model.AuditEntry;
import com.example.securedocviewer.model.SignedTilePayload;
import com.example.securedocviewer.security.SessionService;
import com.example.securedocviewer.security.TileRateLimiter;
import com.example.securedocviewer.service.AuditLogService;
import com.example.securedocviewer.service.SignedUrlService;
import com.example.securedocviewer.service.TileGenerationService;
import com.example.securedocviewer.service.WatermarkService;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;

/**
 * The only endpoint that ever returns pixel data. Every request must carry
 * a token that (a) has a valid HMAC signature, (b) has not passed its own
 * expiry, and (c) is bound to a session that is still active, and (d) has
 * not exceeded that session's tile rate limit. Passing all four still only
 * gets you ONE tile of ONE page, watermarked with the requester's identity
 * baked in server-side.
 */
@RestController
public class TileController {

    private final SignedUrlService signedUrlService;
    private final SessionService sessionService;
    private final TileRateLimiter tileRateLimiter;
    private final TileGenerationService tileGenerationService;
    private final WatermarkService watermarkService;
    private final AuditLogService auditLogService;

    public TileController(SignedUrlService signedUrlService,
                           SessionService sessionService,
                           TileRateLimiter tileRateLimiter,
                           TileGenerationService tileGenerationService,
                           WatermarkService watermarkService,
                           AuditLogService auditLogService) {
        this.signedUrlService = signedUrlService;
        this.sessionService = sessionService;
        this.tileRateLimiter = tileRateLimiter;
        this.tileGenerationService = tileGenerationService;
        this.watermarkService = watermarkService;
        this.auditLogService = auditLogService;
    }

    @GetMapping(value = "/api/tiles", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getTile(@RequestParam String token) throws IOException {
        SignedTilePayload payload = signedUrlService.verifyAndDecode(token);

        // Second, independent check: the token's own expiry can still be in
        // the future while the session it was issued under has since been
        // logged out or timed out.
        String username = sessionService.requireValidSession(payload.sessionId());

        // Third, independent check: a signature- and session-valid request
        // can still be part of a burst trying to redeem every tile of every
        // page. Enforced after auth so unauthenticated requests can't burn
        // a legitimate session's allowance, and before the disk read/render
        // so a throttled request doesn't pay that cost.
        tileRateLimiter.recordAndEnforce(payload.sessionId());

        BufferedImage rawTile = tileGenerationService.loadRawTile(
                payload.documentId(), payload.page(), payload.row(), payload.col());

        BufferedImage watermarked = watermarkService.applyWatermark(rawTile, username);

        auditLogService.record(new AuditEntry(
                payload.sessionId(), username, payload.documentId(),
                payload.page(), payload.row(), payload.col(),
                Instant.now().getEpochSecond()));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(watermarked, "png", out);

        return ResponseEntity.ok()
                // Deliberately not cacheable beyond a moment — a shared cache
                // holding onto a watermarked-for-someone-else tile would leak it.
                .cacheControl(CacheControl.noStore())
                .body(out.toByteArray());
    }
}
