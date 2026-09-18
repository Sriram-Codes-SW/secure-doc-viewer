package com.example.securedocviewer.controller;

import com.example.securedocviewer.audit.RequestActors;
import com.example.securedocviewer.document.DocumentDetail;
import com.example.securedocviewer.document.DocumentService;
import com.example.securedocviewer.document.DocumentSummary;
import com.example.securedocviewer.document.Viewer;
import com.example.securedocviewer.document.Visibility;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * The document library. Who may see or change what is decided in
 * {@link DocumentService}; this layer only adapts HTTP. Uploading is further
 * restricted to PUBLISHER and ADMIN in SecurityConfig.
 */
@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    public record UpdateDocumentRequest(String title, Visibility visibility) {
    }

    private final DocumentService documents;
    private final RequestActors actors;

    public DocumentController(DocumentService documents, RequestActors actors) {
        this.documents = documents;
        this.actors = actors;
    }

    /** Only the documents the caller is allowed to open. */
    @GetMapping
    public List<DocumentSummary> list(Authentication authentication) {
        return documents.list(Viewer.of(authentication));
    }

    @GetMapping("/{documentId}")
    public DocumentDetail get(@PathVariable String documentId, Authentication authentication,
                              HttpServletRequest request) {
        return documents.get(documentId, Viewer.of(authentication), actors.of(request, authentication));
    }

    /**
     * Uploads a PDF, rasterizes and tiles every page, and records the caller
     * as owner. A missing title defaults to the file name; visibility
     * defaults to PRIVATE.
     */
    @PostMapping
    public DocumentDetail upload(@RequestParam(value = "title", required = false) String title,
                                 @RequestParam(value = "visibility", required = false) Visibility visibility,
                                 @RequestParam("file") MultipartFile file,
                                 Authentication authentication,
                                 HttpServletRequest request) throws IOException {
        try (var in = file.getInputStream()) {
            return documents.upload(title, file.getOriginalFilename(), in, visibility,
                    Viewer.of(authentication), actors.of(request, authentication));
        }
    }

    @PatchMapping("/{documentId}")
    public DocumentDetail update(@PathVariable String documentId, @RequestBody UpdateDocumentRequest body,
                                 Authentication authentication, HttpServletRequest request) {
        return documents.update(documentId, body.title(), body.visibility(),
                Viewer.of(authentication), actors.of(request, authentication));
    }

    /** Replaces the PDF; the document keeps its id, title, visibility and shares. */
    @PutMapping("/{documentId}/file")
    public DocumentDetail replaceFile(@PathVariable String documentId, @RequestParam("file") MultipartFile file,
                                      Authentication authentication, HttpServletRequest request) throws IOException {
        try (var in = file.getInputStream()) {
            return documents.replaceFile(documentId, in, Viewer.of(authentication), actors.of(request, authentication));
        }
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> delete(@PathVariable String documentId, Authentication authentication,
                                       HttpServletRequest request) {
        documents.delete(documentId, Viewer.of(authentication), actors.of(request, authentication));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{documentId}/shares")
    public List<String> shares(@PathVariable String documentId, Authentication authentication,
                               HttpServletRequest request) {
        return documents.shares(documentId, Viewer.of(authentication), actors.of(request, authentication));
    }

    @PutMapping("/{documentId}/shares/{username}")
    public List<String> share(@PathVariable String documentId, @PathVariable String username,
                              Authentication authentication, HttpServletRequest request) {
        return documents.share(documentId, username, Viewer.of(authentication), actors.of(request, authentication));
    }

    @DeleteMapping("/{documentId}/shares/{username}")
    public List<String> unshare(@PathVariable String documentId, @PathVariable String username,
                                Authentication authentication, HttpServletRequest request) {
        return documents.unshare(documentId, username, Viewer.of(authentication), actors.of(request, authentication));
    }
}
