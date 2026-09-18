package com.example.securedocviewer.service;

import com.example.securedocviewer.exception.DocumentNotFoundException;
import com.example.securedocviewer.model.DocumentManifest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory index of documents that have been tiled, keyed by documentId.
 * A real deployment would persist this (Postgres row per document, or a
 * manifest.json alongside the tiles in object storage) so it survives a
 * restart; kept in-memory here to keep the demo dependency-free.
 */
@Service
public class DocumentRegistry {

    private final Map<String, DocumentManifest> manifests = new ConcurrentHashMap<>();

    public void save(DocumentManifest manifest) {
        manifests.put(manifest.documentId(), manifest);
    }

    public DocumentManifest require(String documentId) {
        DocumentManifest manifest = manifests.get(documentId);
        if (manifest == null) {
            throw new DocumentNotFoundException("No such document: " + documentId);
        }
        return manifest;
    }

    /** Powers the document library view — every manifest ingested so far. */
    public List<DocumentManifest> listAll() {
        return List.copyOf(manifests.values());
    }
}
