package com.example.securedocviewer.controller;

import com.example.securedocviewer.exception.DocumentNotFoundException;
import com.example.securedocviewer.model.DocumentManifest;
import com.example.securedocviewer.model.PageInfo;
import com.example.securedocviewer.model.TileUrlGrid;
import com.example.securedocviewer.security.SessionService;
import com.example.securedocviewer.service.DocumentRegistry;
import com.example.securedocviewer.service.SignedUrlService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Hands out a fresh grid of signed tile URLs for one page of one document,
 * scoped to the caller's session. Nothing here returns a document- or
 * page-level "download" link — only individually signed, individually
 * expiring links to single tiles, which is exactly the pattern discussed
 * for why "view source" on a real flipbook reader doesn't get you a PDF.
 */
@RestController
@RequestMapping("/api/documents/{documentId}/pages/{page}")
public class PageTileUrlController {

    private final DocumentRegistry documentRegistry;
    private final SignedUrlService signedUrlService;
    private final SessionService sessionService;

    public PageTileUrlController(DocumentRegistry documentRegistry,
                                  SignedUrlService signedUrlService,
                                  SessionService sessionService) {
        this.documentRegistry = documentRegistry;
        this.signedUrlService = signedUrlService;
        this.sessionService = sessionService;
    }

    @GetMapping("/tile-urls")
    public ResponseEntity<TileUrlGrid> tileUrls(
            @RequestHeader("X-Session-Id") String sessionId,
            @PathVariable String documentId,
            @PathVariable int page) {

        sessionService.requireValidSession(sessionId);

        DocumentManifest manifest = documentRegistry.require(documentId);
        PageInfo pageInfo = manifest.pages().stream()
                .filter(p -> p.page() == page)
                .findFirst()
                .orElseThrow(() -> new DocumentNotFoundException(
                        "No such page: document=" + documentId + " page=" + page));

        String[][] urls = new String[pageInfo.rows()][pageInfo.cols()];
        for (int row = 0; row < pageInfo.rows(); row++) {
            for (int col = 0; col < pageInfo.cols(); col++) {
                String token = signedUrlService.issueToken(documentId, page, row, col, sessionId);
                urls[row][col] = "/api/tiles?token=" + token;
            }
        }

        return ResponseEntity.ok(new TileUrlGrid(page, pageInfo.rows(), pageInfo.cols(), pageInfo.tileSize(), urls));
    }
}
