package com.example.securedocviewer.document;

import com.example.securedocviewer.account.Role;
import com.example.securedocviewer.account.UserAccountService;
import com.example.securedocviewer.exception.UsernameTakenException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Per-document access, lifecycle and audit, through the real filter chain and database. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DocumentAccessIntegrationTest {

    private static final String PASSWORD = "correct-horse-battery";
    private static final Path STORAGE = Path.of("target", "test-storage");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserAccountService accounts;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void privateDocumentsAreInvisibleToOthers() throws Exception {
        MockHttpSession owner = signIn("owner-a", Role.PUBLISHER);
        MockHttpSession stranger = signIn("stranger-a", Role.READER);
        String id = upload(owner, "Owner A private", "PRIVATE");

        assertFalse(listedTitles(stranger).contains("Owner A private"));
        mvc.perform(get("/api/documents/" + id).session(stranger)).andExpect(status().isNotFound());
        mvc.perform(get("/api/documents/" + id + "/pages/0/tile-urls").session(stranger))
                .andExpect(status().isNotFound());
        // Management attempts on an invisible document also say "not found", not "forbidden".
        mvc.perform(delete("/api/documents/" + id).session(stranger).with(csrf())).andExpect(status().isNotFound());

        assertTrue(listedTitles(owner).contains("Owner A private"));
    }

    @Test
    void everyoneVisibilityShowsItToAllButOnlyTheOwnerCanManage() throws Exception {
        MockHttpSession owner = signIn("owner-b", Role.PUBLISHER);
        MockHttpSession reader = signIn("reader-b", Role.READER);
        String id = upload(owner, "Owner B public", "EVERYONE");

        mvc.perform(get("/api/documents/" + id).session(reader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canManage").value(false))
                .andExpect(jsonPath("$.sharedWith").doesNotExist());
        mvc.perform(patch("/api/documents/" + id).session(reader).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"hijacked\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/documents/" + id).session(reader).with(csrf())).andExpect(status().isForbidden());
    }

    @Test
    void sharingGrantsAccessAndUnsharingRevokesEvenIssuedTileUrls() throws Exception {
        MockHttpSession owner = signIn("owner-c", Role.PUBLISHER);
        MockHttpSession friend = signIn("friend-c", Role.READER);
        String id = upload(owner, "Owner C shared", "PRIVATE");

        mvc.perform(put("/api/documents/" + id + "/shares/Friend-C").session(owner).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("friend-c"));
        assertTrue(listedTitles(friend).contains("Owner C shared"));

        String tileUrl = firstTileUrl(id, friend);
        mvc.perform(get(tileUrl).session(friend)).andExpect(status().isOk());

        mvc.perform(delete("/api/documents/" + id + "/shares/friend-c").session(owner).with(csrf()))
                .andExpect(status().isOk());
        // The URL was issued while access was granted, but access is re-checked per tile.
        mvc.perform(get(tileUrl).session(friend)).andExpect(status().isNotFound());
        assertFalse(listedTitles(friend).contains("Owner C shared"));
    }

    @Test
    void sharingWithUnknownUsersOrTheOwnerIsRejected() throws Exception {
        MockHttpSession owner = signIn("owner-d", Role.PUBLISHER);
        String id = upload(owner, "Owner D", "PRIVATE");

        mvc.perform(put("/api/documents/" + id + "/shares/nobody-here").session(owner).with(csrf()))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/documents/" + id + "/shares/owner-d").session(owner).with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminsCanSeeAndManageEverything() throws Exception {
        MockHttpSession owner = signIn("owner-e", Role.PUBLISHER);
        MockHttpSession admin = signInExisting("admin", "bootstrap-admin-password");
        String id = upload(owner, "Owner E private", "PRIVATE");

        mvc.perform(get("/api/documents/" + id).session(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canManage").value(true))
                .andExpect(jsonPath("$.owner").value("owner-e"));
        mvc.perform(patch("/api/documents/" + id).session(admin).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"Renamed by admin\"}"))
                .andExpect(jsonPath("$.title").value("Renamed by admin"));
    }

    @Test
    void ownerCanRenameChangeVisibilityReplaceAndDelete() throws Exception {
        MockHttpSession owner = signIn("owner-f", Role.PUBLISHER);
        MockHttpSession reader = signIn("reader-f", Role.READER);
        String id = upload(owner, "Owner F", "PRIVATE");

        mvc.perform(patch("/api/documents/" + id).session(owner).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"  Owner F v2  \",\"visibility\":\"EVERYONE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Owner F v2"))
                .andExpect(jsonPath("$.visibility").value("EVERYONE"));
        assertTrue(listedTitles(reader).contains("Owner F v2"));

        mvc.perform(multipart(HttpMethod.PUT, "/api/documents/" + id + "/file")
                        .file(pdfPart(3)).session(owner).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentId").value(id))
                .andExpect(jsonPath("$.pageCount").value(3));
        assertTrue(Files.isDirectory(STORAGE.resolve(id).resolve("v2").resolve("page-2")), "replacement tiles not in place");
        assertFalse(Files.exists(STORAGE.resolve(id).resolve("v1")), "superseded render not removed");

        mvc.perform(delete("/api/documents/" + id).session(owner).with(csrf())).andExpect(status().isNoContent());
        mvc.perform(get("/api/documents/" + id).session(owner)).andExpect(status().isNotFound());
        assertFalse(Files.exists(STORAGE.resolve(id)), "tiles left on disk after delete");
    }

    @Test
    void uploadDefaultsTitleToFileNameAndVisibilityToPrivate() throws Exception {
        MockHttpSession owner = signIn("owner-g", Role.PUBLISHER);
        MvcResult result = mvc.perform(multipart("/api/documents").file(pdfPart(1)).session(owner).with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode created = json(result);
        assertEquals("quarterly-report", created.get("title").asString());
        assertEquals("PRIVATE", created.get("visibility").asString());
        assertEquals("owner-g", created.get("owner").asString());
    }

    @Test
    void aCorruptUploadIsA400AndLeavesNothingBehind() throws Exception {
        MockHttpSession owner = signIn("owner-h", Role.PUBLISHER);
        List<Path> before = storageEntries();

        mvc.perform(multipart("/api/documents")
                        .file(new MockMultipartFile("file", "fake.pdf", "application/pdf", "not a pdf".getBytes()))
                        .param("title", "Broken").session(owner).with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("The file is not a readable PDF."));

        assertEquals(before, storageEntries(), "a failed upload changed the storage directory");
        assertFalse(listedTitles(owner).contains("Broken"));
    }

    @Test
    void auditRecordsDocumentEventsWithTitlesAndSupportsFiltersAndCsv() throws Exception {
        MockHttpSession owner = signIn("owner-i", Role.PUBLISHER);
        MockHttpSession snooper = signIn("snooper-i", Role.READER);
        MockHttpSession admin = signInExisting("admin", "bootstrap-admin-password");
        String id = upload(owner, "=Audit me", "PRIVATE");
        mvc.perform(get("/api/documents/" + id).session(snooper)).andExpect(status().isNotFound());

        JsonNode uploads = json(mvc.perform(get("/api/admin/audit").session(admin)
                        .param("type", "DOCUMENT_UPLOADED").param("documentId", id))
                .andExpect(status().isOk()).andReturn());
        assertEquals(1, uploads.get("total").asLong());
        assertEquals("=Audit me", uploads.at("/items/0/documentTitle").asString());
        assertEquals("owner-i", uploads.at("/items/0/username").asString());

        JsonNode denied = json(mvc.perform(get("/api/admin/audit").session(admin)
                        .param("type", "ACCESS_DENIED").param("username", "snooper-i"))
                .andReturn());
        assertEquals(1, denied.get("total").asLong());

        JsonNode signIns = json(mvc.perform(get("/api/admin/audit").session(admin)
                        .param("type", "SIGN_IN").param("username", "owner-i"))
                .andReturn());
        assertTrue(signIns.get("total").asLong() >= 1);

        String csv = mvc.perform(get("/api/admin/audit/export").session(admin).param("documentId", id))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"audit-log.csv\""))
                .andReturn().getResponse().getContentAsString();
        assertTrue(csv.startsWith("time_utc,event,"));
        assertTrue(csv.contains("\"'=Audit me\""), "formula-looking title was not neutralised in CSV: " + csv);

        mvc.perform(get("/api/admin/audit").session(owner)).andExpect(status().isForbidden());
    }

    @Test
    void aWatermarkTraceCodeFindsTheExactSessionInTheAuditLog() throws Exception {
        MockHttpSession owner = signIn("owner-j", Role.PUBLISHER);
        MockHttpSession admin = signInExisting("admin", "bootstrap-admin-password");
        upload(owner, "Traceable", "PRIVATE");

        JsonNode upload = json(mvc.perform(get("/api/admin/audit").session(admin)
                .param("type", "DOCUMENT_UPLOADED").param("username", "owner-j")).andReturn());
        String handle = upload.at("/items/0/sessionHandle").asString();
        assertTrue(handle.matches("[0-9A-HJKMNP-TV-Z]{16}"), "handle is not unambiguous Crockford Base32: " + handle);

        // What a person would type after reading the watermark: first six characters, any case.
        JsonNode found = json(mvc.perform(get("/api/admin/audit").session(admin)
                .param("trace", handle.substring(0, 6).toLowerCase())).andReturn());
        assertTrue(found.get("total").asLong() >= 1);
        found.get("items").forEach(e -> assertEquals("owner-j", e.get("username").asString()));
    }

    @Test
    void aPublisherDemotedToReaderCanStillReadButNoLongerManageTheirDocuments() throws Exception {
        MockHttpSession owner = signIn("owner-k", Role.PUBLISHER);
        String id = upload(owner, "Owner K private", "PRIVATE");
        accounts.update("admin", "owner-k", Role.READER, null);
        MockHttpSession demoted = signInExisting("owner-k", PASSWORD);

        mvc.perform(get("/api/documents/" + id).session(demoted))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canManage").value(false));
        mvc.perform(patch("/api/documents/" + id).session(demoted).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"visibility\":\"EVERYONE\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/documents/" + id + "/shares/reader-b").session(demoted).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void viewingAPageRecordsOneAuditEventNotOnePerTile() throws Exception {
        MockHttpSession owner = signIn("owner-l", Role.PUBLISHER);
        MockHttpSession admin = signInExisting("admin", "bootstrap-admin-password");
        String id = upload(owner, "Owner L", "PRIVATE");

        JsonNode grid = json(mvc.perform(get("/api/documents/" + id + "/pages/0/tile-urls").session(owner)).andReturn());
        int tiles = 0;
        for (JsonNode row : grid.get("tileUrls")) {
            for (JsonNode url : row) {
                mvc.perform(get(url.asString()).session(owner)).andExpect(status().isOk());
                tiles++;
            }
        }
        assertTrue(tiles > 1, "test needs a multi-tile page");

        JsonNode views = json(mvc.perform(get("/api/admin/audit").session(admin)
                .param("type", "PAGE_VIEWED").param("documentId", id)).andReturn());
        assertEquals(1, views.get("total").asLong(), "expected one PAGE_VIEWED for " + tiles + " tiles");
        assertEquals(0, views.at("/items/0/page").asInt());
    }

    @Test
    void probingMadeUpDocumentIdsCannotFloodTheAuditLog() throws Exception {
        MockHttpSession prober = signIn("prober-m", Role.READER);
        MockHttpSession admin = signInExisting("admin", "bootstrap-admin-password");
        for (int i = 0; i < 25; i++) {
            mvc.perform(get("/api/documents/00000000-0000-0000-0000-0000000000" + String.format("%02d", i)).session(prober))
                    .andExpect(status().isNotFound());
        }
        JsonNode denied = json(mvc.perform(get("/api/admin/audit").session(admin)
                .param("type", "ACCESS_DENIED").param("username", "prober-m")).andReturn());
        long recorded = denied.get("total").asLong();
        assertTrue(recorded >= 1 && recorded <= 2, "25 probes should leave 1-2 audit rows, got " + recorded);
    }

    @Test
    void userDirectoryIsForPublishersOnlyAndReturnsNamesOnly() throws Exception {
        MockHttpSession publisher = signIn("dir-publisher", Role.PUBLISHER);
        MockHttpSession reader = signIn("dir-reader", Role.READER);

        mvc.perform(get("/api/users").param("q", "dir-").session(publisher))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("dir-reader"));
        mvc.perform(get("/api/users").param("q", "dir-").session(reader)).andExpect(status().isForbidden());
    }

    private MockHttpSession signIn(String username, Role role) throws Exception {
        try {
            accounts.create(username, PASSWORD, role, false);
        } catch (UsernameTakenException alreadyCreated) {
            // The context and its in-memory database are shared across tests.
        }
        return signInExisting(username, PASSWORD);
    }

    private MockHttpSession signInExisting(String username, String password) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    private String upload(MockHttpSession session, String title, String visibility) throws Exception {
        MvcResult result = mvc.perform(multipart("/api/documents").file(pdfPart(1))
                        .param("title", title).param("visibility", visibility)
                        .session(session).with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        return json(result).get("documentId").asString();
    }

    private List<String> listedTitles(MockHttpSession session) throws Exception {
        List<String> titles = new ArrayList<>();
        json(mvc.perform(get("/api/documents").session(session)).andExpect(status().isOk()).andReturn())
                .forEach(doc -> titles.add(doc.get("title").asString()));
        return titles;
    }

    private String firstTileUrl(String documentId, MockHttpSession session) throws Exception {
        return json(mvc.perform(get("/api/documents/" + documentId + "/pages/0/tile-urls").session(session))
                .andExpect(status().isOk()).andReturn()).at("/tileUrls/0/0").asString();
    }

    private JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private static List<Path> storageEntries() throws IOException {
        List<Path> entries = new ArrayList<>();
        for (Path dir : List.of(STORAGE, STORAGE.resolve(".staging"))) {
            if (Files.isDirectory(dir)) {
                try (Stream<Path> children = Files.list(dir)) {
                    children.sorted().forEach(entries::add);
                }
            }
        }
        return entries;
    }

    private static MockMultipartFile pdfPart(int pages) throws IOException {
        try (PDDocument document = new PDDocument()) {
            for (int i = 0; i < pages; i++) {
                document.addPage(new PDPage(PDRectangle.A6));
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return new MockMultipartFile("file", "quarterly-report.pdf", "application/pdf", out.toByteArray());
        }
    }
}
