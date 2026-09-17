package com.example.securedocviewer.controller;

import com.example.securedocviewer.model.SignedTilePayload;
import com.example.securedocviewer.security.SessionService;
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

/**
 * The only endpoint that ever returns pixel data. Every request must carry
 * a token that (a) has a valid HMAC signature, (b) has not passed its own
 * expiry, and (c) is bound to a session that is still active. Passing all
 * three still only gets you ONE tile of ONE page, watermarked with the
 * requester's identity baked in server-side.
 */
@RestController
public class TileController {

    private final SignedUrlService signedUrlService;
    private final SessionService sessionService;
    private final TileGenerationService tileGenerationService;
    private final WatermarkService watermarkService;

    public TileController(SignedUrlService signedUrlService,
                           SessionService sessionService,
                           TileGenerationService tileGenerationService,
                           WatermarkService watermarkService) {
        this.signedUrlService = signedUrlService;
        this.sessionService = sessionService;
        this.tileGenerationService = tileGenerationService;
        this.watermarkService = watermarkService;
    }

    @GetMapping(value = "/api/tiles", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getTile(@RequestParam String token) throws IOException {
        SignedTilePayload payload = signedUrlService.verifyAndDecode(token);

        // Second, independent check: the token's own expiry can still be in
        // the future while the session it was issued under has since been
        // logged out or timed out.
        String username = sessionService.requireValidSession(payload.sessionId());

        BufferedImage rawTile = tileGenerationService.loadRawTile(
                payload.documentId(), payload.page(), payload.row(), payload.col());

        BufferedImage watermarked = watermarkService.applyWatermark(rawTile, username);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(watermarked, "png", out);

        return ResponseEntity.ok()
                // Deliberately not cacheable beyond a moment — a shared cache
                // holding onto a watermarked-for-someone-else tile would leak it.
                .cacheControl(CacheControl.noStore())
                .body(out.toByteArray());
    }
}
