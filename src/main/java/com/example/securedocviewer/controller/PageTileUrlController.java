package com.example.securedocviewer.controller;

import com.example.securedocviewer.exception.DocumentNotFoundException;
import com.example.securedocviewer.model.DocumentManifest;
import com.example.securedocviewer.model.PageInfo;
import com.example.securedocviewer.model.TileUrlGrid;
import com.example.securedocviewer.security.SessionKeys;
import jakarta.servlet.http.HttpServletRequest;
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
    private final SessionKeys sessionKeys;

    public PageTileUrlController(DocumentRegistry documentRegistry,
                                  SignedUrlService signedUrlService,
                                  SessionKeys sessionKeys) {
        this.documentRegistry = documentRegistry;
        this.signedUrlService = signedUrlService;
        this.sessionKeys = sessionKeys;
    }

    @GetMapping("/tile-urls")
    public ResponseEntity<TileUrlGrid> tileUrls(
            @PathVariable String documentId,
            @PathVariable int page,
            HttpServletRequest request) {

        // Tokens carry a keyed binding to this session, never its id, so a
        // tile URL can be logged or leaked without leaking the credential.
        String sessionBinding = sessionKeys.tileBinding(request.getSession().getId());

        DocumentManifest manifest = documentRegistry.require(documentId);
        PageInfo pageInfo = manifest.pages().stream()
                .filter(p -> p.page() == page)
                .findFirst()
                .orElseThrow(() -> new DocumentNotFoundException(
                        "No such page: document=" + documentId + " page=" + page));

        String[][] urls = new String[pageInfo.rows()][pageInfo.cols()];
        for (int row = 0; row < pageInfo.rows(); row++) {
            for (int col = 0; col < pageInfo.cols(); col++) {
                String token = signedUrlService.issueToken(documentId, page, row, col, sessionBinding);
                urls[row][col] = "/api/tiles?token=" + token;
            }
        }

        return ResponseEntity.ok(new TileUrlGrid(page, pageInfo.rows(), pageInfo.cols(), pageInfo.tileSize(), urls));
    }
}
