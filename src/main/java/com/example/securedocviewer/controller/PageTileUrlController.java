package com.example.securedocviewer.controller;

import com.example.securedocviewer.audit.RequestActors;
import com.example.securedocviewer.document.DocumentService;
import com.example.securedocviewer.document.Viewer;
import com.example.securedocviewer.model.PageInfo;
import com.example.securedocviewer.model.TileUrlGrid;
import com.example.securedocviewer.security.SessionKeys;
import com.example.securedocviewer.service.SignedUrlService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Hands out a fresh grid of signed tile URLs for one page of one document,
 * scoped to the caller's session, and only if the caller may view the
 * document (otherwise 404). Nothing here returns a document- or
 * page-level "download" link — only individually signed, individually
 * expiring links to single tiles, which is exactly the pattern discussed
 * for why "view source" on a real flipbook reader doesn't get you a PDF.
 */
@RestController
@RequestMapping("/api/documents/{documentId}/pages/{page}")
public class PageTileUrlController {

    private final DocumentService documents;
    private final SignedUrlService signedUrlService;
    private final SessionKeys sessionKeys;
    private final RequestActors actors;

    public PageTileUrlController(DocumentService documents,
                                  SignedUrlService signedUrlService,
                                  SessionKeys sessionKeys,
                                  RequestActors actors) {
        this.documents = documents;
        this.signedUrlService = signedUrlService;
        this.sessionKeys = sessionKeys;
        this.actors = actors;
    }

    @GetMapping("/tile-urls")
    public ResponseEntity<TileUrlGrid> tileUrls(
            @PathVariable String documentId,
            @PathVariable int page,
            Authentication authentication,
            HttpServletRequest request) {

        DocumentService.IssuablePage issuable = documents.requirePage(documentId, page,
                Viewer.of(authentication), actors.of(request, authentication));
        PageInfo pageInfo = issuable.info();

        // Tokens carry a keyed binding to this session, never its id, so a
        // tile URL can be logged or leaked without leaking the credential.
        String sessionBinding = sessionKeys.tileBinding(request.getSession().getId());

        String[][] urls = new String[pageInfo.rows()][pageInfo.cols()];
        for (int row = 0; row < pageInfo.rows(); row++) {
            for (int col = 0; col < pageInfo.cols(); col++) {
                String token = signedUrlService.issueToken(documentId, page, row, col, issuable.tileVersion(), sessionBinding);
                urls[row][col] = "/api/tiles?token=" + token;
            }
        }

        return ResponseEntity.ok(new TileUrlGrid(page, pageInfo.rows(), pageInfo.cols(), pageInfo.tileSize(), issuable.tileVersion(), urls));
    }
}
