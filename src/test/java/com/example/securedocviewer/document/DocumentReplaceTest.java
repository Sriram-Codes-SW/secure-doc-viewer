package com.example.securedocviewer.document;

import com.example.securedocviewer.account.Role;
import com.example.securedocviewer.account.UserAccountService;
import com.example.securedocviewer.audit.AuditEvent.Actor;
import com.example.securedocviewer.exception.ForbiddenException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** PDF replacement edge cases the controller-level tests can't reach. */
@SpringBootTest
@ActiveProfiles("test")
class DocumentReplaceTest {

    private static final Path STORAGE = Path.of("./target/test-storage");

    @Autowired
    private DocumentService documents;

    @Autowired
    private UserAccountService accounts;

    private String uploadAs(String username) throws IOException {
        accounts.create(username, "correct-horse-battery", Role.PUBLISHER, false);
        Viewer owner = new Viewer(username, false, true);
        return documents.upload("Replace me", "r.pdf", pdf(1), Visibility.PRIVATE, owner, actor(username)).documentId();
    }

    @Test
    void aLeftoverDirectoryFromAFailedReplaceDoesNotBlockTheNextOne() throws IOException {
        String id = uploadAs("rp-debris");
        // What a replace leaves behind if the database commit fails after the tiles were moved.
        Path debris = Files.createDirectories(STORAGE.resolve(id).resolve("v2").resolve("page-0"));
        Files.writeString(debris.resolve("tile-0_0.png"), "stale");

        DocumentDetail replaced = documents.replaceFile(id, pdf(2), new Viewer("rp-debris", false, true), actor("rp-debris"));

        assertEquals(2, replaced.pageCount());
        assertTrue(Files.isDirectory(STORAGE.resolve(id).resolve("v2").resolve("page-1")), "new render not in place");
        assertNotEquals("stale", Files.readString(debris.resolve("tile-0_0.png"), java.nio.charset.StandardCharsets.ISO_8859_1),
                "debris survived");
    }

    @Test
    void anOwnerDemotedWhileTheirReplacementRendersCannotCommitIt() throws IOException {
        String id = uploadAs("rp-demoted");
        // The session still says PUBLISHER, but the database no longer does by the time the row is locked.
        accounts.update("admin", "rp-demoted", Role.READER, null);

        assertThrows(ForbiddenException.class, () ->
                documents.replaceFile(id, pdf(2), new Viewer("rp-demoted", false, true), actor("rp-demoted")));
        assertFalse(Files.exists(STORAGE.resolve(id).resolve("v2")), "the rejected render must not be committed");
    }

    @Autowired
    private com.example.securedocviewer.config.ViewerProperties properties;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbc;

    @Test
    void aReplacementThatTimesOutLeavesTheDocumentAsItWas() throws Exception {
        String id = uploadAs("rp-timeout");
        Viewer owner = new Viewer("rp-timeout", false, true);
        java.time.Duration normal = properties.getRenderTimeout();
        properties.setRenderTimeout(java.time.Duration.ofMillis(1));
        try {
            assertThrows(com.example.securedocviewer.exception.BadRequestException.class,
                    () -> documents.replaceFile(id, pdf(40), owner, actor("rp-timeout")));
        } finally {
            properties.setRenderTimeout(normal);
        }
        assertEquals(1, jdbc.queryForObject("select tile_version from document where id = ?", Integer.class, id));
        assertTrue(Files.isDirectory(STORAGE.resolve(id).resolve("v1").resolve("page-0")), "current tiles intact");
        assertFalse(Files.exists(STORAGE.resolve(id).resolve("v2")), "nothing committed");
        // The abandoned render stops and frees its slot: the next replace goes through.
        assertEquals(2, documents.replaceFile(id, pdf(2), owner, actor("rp-timeout")).pageCount());
    }

    @Test
    void aPublisherDemotedWhileTheirUploadRendersCannotCreateTheDocument() throws IOException {
        accounts.create("rp-upload-demoted", "correct-horse-battery", Role.READER, false);
        long before = jdbc.queryForObject("select count(*) from document", Long.class);
        // The session still says PUBLISHER; the database already says READER.
        assertThrows(ForbiddenException.class, () -> documents.upload("Late", "late.pdf", pdf(1),
                Visibility.PRIVATE, new Viewer("rp-upload-demoted", false, true), actor("rp-upload-demoted")));
        assertEquals(before, jdbc.queryForObject("select count(*) from document", Long.class));
    }

    @Test
    void aDisabledUserCannotBeGivenAccess() throws IOException {
        String id = uploadAs("rp-sharer");
        accounts.create("rp-disabled", "correct-horse-battery", Role.READER, false);
        accounts.update("admin", "rp-disabled", null, false);
        assertThrows(com.example.securedocviewer.exception.BadRequestException.class, () ->
                documents.share(id, "rp-disabled", new Viewer("rp-sharer", false, true), actor("rp-sharer")));
    }

    private static Actor actor(String username) {
        return new Actor(username, null, "127.0.0.1");
    }

    private static ByteArrayInputStream pdf(int pages) throws IOException {
        try (PDDocument document = new PDDocument()) {
            for (int i = 0; i < pages; i++) {
                document.addPage(new PDPage(PDRectangle.A6));
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}
