package com.example.securedocviewer.service;

import com.example.securedocviewer.config.ViewerProperties;
import com.example.securedocviewer.document.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Removes tiles nothing points to: directories left by a crash between
 * rendering and saving, by a failed tile delete, or from before documents
 * were persisted. Deliberately conservative — it only touches
 * document-id-shaped directories directly under the storage root (plus
 * abandoned staging renders), and only once they are old enough that no
 * upload can still be in flight.
 */
@Component
public class StorageJanitor {

    private static final Logger log = LoggerFactory.getLogger(StorageJanitor.class);
    private static final Pattern DOCUMENT_ID = Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    static final Duration MIN_AGE = Duration.ofHours(1);

    private final ViewerProperties properties;
    private final DocumentRepository documents;

    public StorageJanitor(ViewerProperties properties, DocumentRepository documents) {
        this.properties = properties;
        this.documents = documents;
    }

    @Scheduled(initialDelayString = "PT2M", fixedDelayString = "PT6H")
    public void sweep() {
        try {
            int removed = removeOrphans(Instant.now().minus(MIN_AGE));
            if (removed > 0) {
                log.info("Storage janitor removed {} orphaned tile director{}", removed, removed == 1 ? "y" : "ies");
            }
        } catch (IOException e) {
            log.warn("Storage janitor could not list the storage directory", e);
        }
    }

    /** Deletes orphaned directories last modified before {@code olderThan}; returns how many. */
    int removeOrphans(Instant olderThan) throws IOException {
        Path root = Path.of(properties.getStorageRoot());
        if (!Files.isDirectory(root)) {
            return 0;
        }
        Set<String> known = new HashSet<>(documents.findAllIds());
        int removed = 0;
        for (Path dir : directories(root)) {
            String name = dir.getFileName().toString();
            if (DOCUMENT_ID.matcher(name).matches() && !known.contains(name) && olderThan(dir, olderThan)) {
                removed += tryDelete(dir);
            }
        }
        Path staging = root.resolve(TileGenerationService.STAGING_DIR);
        if (Files.isDirectory(staging)) {
            for (Path dir : directories(staging)) {
                if (olderThan(dir, olderThan)) {
                    removed += tryDelete(dir);
                }
            }
        }
        return removed;
    }

    /** One locked directory must not stop the sweep; it is simply retried next time. */
    private static int tryDelete(Path dir) {
        try {
            return FileOperations.deleteDirectory(dir) ? 1 : 0;
        } catch (IOException e) {
            log.warn("Storage janitor could not delete {} ({}); will retry on the next sweep", dir, e.getMessage());
            return 0;
        }
    }

    private static List<Path> directories(Path parent) throws IOException {
        try (Stream<Path> children = Files.list(parent)) {
            return children.filter(Files::isDirectory).toList();
        }
    }

    private static boolean olderThan(Path dir, Instant cutoff) throws IOException {
        return Files.getLastModifiedTime(dir).toInstant().isBefore(cutoff);
    }
}
