package com.example.securedocviewer.controller;

import com.example.securedocviewer.model.DocumentManifest;
import com.example.securedocviewer.security.SessionService;
import com.example.securedocviewer.service.DocumentRegistry;
import com.example.securedocviewer.service.TileGenerationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final TileGenerationService tileGenerationService;
    private final DocumentRegistry documentRegistry;
    private final SessionService sessionService;

    public DocumentController(TileGenerationService tileGenerationService,
                               DocumentRegistry documentRegistry,
                               SessionService sessionService) {
        this.tileGenerationService = tileGenerationService;
        this.documentRegistry = documentRegistry;
        this.sessionService = sessionService;
    }

    /**
     * Uploads a PDF, rasterizes and tiles every page, and registers the
     * resulting manifest. Requires a valid session purely to demonstrate
     * that ingestion is gated the same way viewing is — a real app would
     * also check the uploader's role here.
     */
    @PostMapping
    public ResponseEntity<DocumentManifest> upload(
            @RequestHeader("X-Session-Id") String sessionId,
            @RequestParam("title") String title,
            @RequestParam("file") MultipartFile file) throws IOException {

        sessionService.requireValidSession(sessionId);

        DocumentManifest manifest = tileGenerationService.ingest(title, file.getBytes());
        documentRegistry.save(manifest);
        return ResponseEntity.ok(manifest);
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<DocumentManifest> getManifest(
            @RequestHeader("X-Session-Id") String sessionId,
            @PathVariable String documentId) {

        sessionService.requireValidSession(sessionId);
        return ResponseEntity.ok(documentRegistry.require(documentId));
    }
}
