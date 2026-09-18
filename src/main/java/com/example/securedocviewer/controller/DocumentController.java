package com.example.securedocviewer.controller;

import com.example.securedocviewer.model.DocumentManifest;
import com.example.securedocviewer.service.DocumentRegistry;
import com.example.securedocviewer.service.TileGenerationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/** Every endpoint here requires a signed-in user; see SecurityConfig. */
@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final TileGenerationService tileGenerationService;
    private final DocumentRegistry documentRegistry;

    public DocumentController(TileGenerationService tileGenerationService,
                               DocumentRegistry documentRegistry) {
        this.tileGenerationService = tileGenerationService;
        this.documentRegistry = documentRegistry;
    }

    /**
     * Uploads a PDF, rasterizes and tiles every page, and registers the
     * resulting manifest. Restricted to PUBLISHER and ADMIN in SecurityConfig.
     */
    @PostMapping
    public ResponseEntity<DocumentManifest> upload(
            @RequestParam("title") String title,
            @RequestParam("file") MultipartFile file) throws IOException {

        DocumentManifest manifest = tileGenerationService.ingest(title, file.getBytes());
        documentRegistry.save(manifest);
        return ResponseEntity.ok(manifest);
    }

    /** Backs the document library view — every document uploaded so far. */
    @GetMapping
    public ResponseEntity<List<DocumentManifest>> listDocuments() {
        return ResponseEntity.ok(documentRegistry.listAll());
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<DocumentManifest> getManifest(@PathVariable String documentId) {
        return ResponseEntity.ok(documentRegistry.require(documentId));
    }
}
