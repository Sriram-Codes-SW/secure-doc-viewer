package com.example.securedocviewer.service;

import com.example.securedocviewer.config.ViewerProperties;
import com.example.securedocviewer.document.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StorageJanitorTest {

    private static final String KNOWN = "11111111-1111-1111-1111-111111111111";
    private static final String ORPHAN = "22222222-2222-2222-2222-222222222222";
    private static final String FRESH_ORPHAN = "33333333-3333-3333-3333-333333333333";

    @Test
    void removesOnlyOldOrphanedDocumentDirectoriesAndAbandonedStaging(@TempDir Path root) throws IOException {
        Instant old = Instant.now().minus(StorageJanitor.MIN_AGE).minusSeconds(60);
        Path known = dir(root.resolve(KNOWN), old);
        Path orphan = dir(root.resolve(ORPHAN), old);
        Path freshOrphan = dir(root.resolve(FRESH_ORPHAN), Instant.now());
        Path notADocument = dir(root.resolve("backups"), old);
        Path oldStaging = dir(root.resolve(TileGenerationService.STAGING_DIR).resolve("abandoned"), old);
        Path activeStaging = dir(root.resolve(TileGenerationService.STAGING_DIR).resolve("in-progress"), Instant.now());
        Path currentVersion = dir(root.resolve(KNOWN).resolve("v2"), old);
        Path supersededVersion = dir(root.resolve(KNOWN).resolve("v1"), old);
        Path legacyPage = dir(root.resolve(KNOWN).resolve("page-0"), old);

        ViewerProperties properties = new ViewerProperties();
        properties.setStorageRoot(root.toString());
        DocumentRepository documents = mock(DocumentRepository.class);
        when(documents.findAllTileVersions()).thenReturn(List.<Object[]>of(new Object[] {KNOWN, 2}));

        int removed = new StorageJanitor(properties, documents).removeOrphans(Instant.now().minus(StorageJanitor.MIN_AGE));

        assertEquals(4, removed);
        assertTrue(Files.exists(currentVersion), "the committed version must never be removed");
        assertFalse(Files.exists(supersededVersion), "a superseded version should be removed");
        assertFalse(Files.exists(legacyPage), "legacy unversioned pages of a versioned document should be removed");
        assertFalse(Files.exists(orphan), "old orphan should be removed");
        assertFalse(Files.exists(oldStaging), "abandoned staging render should be removed");
        assertTrue(Files.exists(known), "a document's tiles must never be removed");
        assertTrue(Files.exists(freshOrphan), "a very recent directory may be an upload in flight");
        assertTrue(Files.exists(notADocument), "directories that aren't document ids are left alone");
        assertTrue(Files.exists(activeStaging), "a render in progress must not be removed");
    }

    private static Path dir(Path path, Instant modified) throws IOException {
        Files.createDirectories(path);
        Files.writeString(path.resolve("tile.png"), "x");
        Files.setLastModifiedTime(path, FileTime.from(modified));
        return path;
    }
}
