package com.example.securedocviewer.document;

import com.example.securedocviewer.account.Role;
import com.example.securedocviewer.account.UserAccountService;
import com.example.securedocviewer.exception.UsernameTakenException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Readers pulling tiles while the owner replaces or deletes the document:
 * every tile answer must be a clean one (the tile, "replaced" or "gone"),
 * never a server error, and a replaced document must serve its new render.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ConcurrentServingIntegrationTest {

    private static final String PASSWORD = "correct-horse-battery";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserAccountService accounts;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void tilesStayConsistentWhileTheDocumentIsReplacedAndThenDeleted() throws Exception {
        MockHttpSession owner = signIn("cs-owner", Role.PUBLISHER);
        MockHttpSession reader = signIn("cs-reader", Role.READER);
        String id = json(mvc.perform(multipart("/api/documents").file(pdf(2)).param("title", "Busy")
                        .param("visibility", "EVERYONE").session(owner).with(csrf()))
                .andExpect(status().isOk()).andReturn()).get("documentId").asString();

        JsonNode grid = json(mvc.perform(get("/api/documents/" + id + "/pages/0/tile-urls").session(reader))
                .andExpect(status().isOk()).andReturn());
        assertEquals(1, grid.get("tileVersion").asInt());
        String tileUrl = grid.at("/tileUrls/0/0").asString();

        // Four readers' worth of requests (well under the per-user rate limit) during a replace.
        ConcurrentLinkedQueue<Integer> duringReplace = hammer(reader, tileUrl, () -> {
            mvc.perform(multipart(HttpMethod.PUT, "/api/documents/" + id + "/file").file(pdf(3))
                    .session(owner).with(csrf())).andExpect(status().isOk());
            return null;
        });
        assertTrue(Set.of(200, 410).containsAll(duringReplace), "during replace: " + duringReplace);
        assertTrue(duringReplace.contains(410), "old URLs must stop working once replaced");

        JsonNode fresh = json(mvc.perform(get("/api/documents/" + id + "/pages/0/tile-urls").session(reader))
                .andExpect(status().isOk()).andReturn());
        assertEquals(2, fresh.get("tileVersion").asInt(), "grid names the new render");
        String freshUrl = fresh.at("/tileUrls/0/0").asString();
        mvc.perform(get(freshUrl).session(reader)).andExpect(status().isOk());

        ConcurrentLinkedQueue<Integer> duringDelete = hammer(reader, freshUrl, () -> {
            mvc.perform(delete("/api/documents/" + id).session(owner).with(csrf())).andExpect(status().isNoContent());
            return null;
        });
        assertTrue(Set.of(200, 404, 410).containsAll(duringDelete), "during delete: " + duringDelete);
        mvc.perform(get(freshUrl).session(reader)).andExpect(status().isNotFound());
    }

    /** Fetches the tile from 4 threads, 10 times each, while {@code change} runs. */
    private ConcurrentLinkedQueue<Integer> hammer(MockHttpSession session, String url,
                                                  java.util.concurrent.Callable<Void> change) throws Exception {
        ConcurrentLinkedQueue<Integer> statuses = new ConcurrentLinkedQueue<>();
        AtomicBoolean started = new AtomicBoolean();
        ExecutorService pool = Executors.newFixedThreadPool(4);
        try {
            List<Future<?>> readers = new ArrayList<>();
            for (int t = 0; t < 4; t++) {
                readers.add(pool.submit(() -> {
                    for (int i = 0; i < 10; i++) {
                        started.set(true);
                        statuses.add(mvc.perform(get(url).session(session)).andReturn().getResponse().getStatus());
                    }
                    return null;
                }));
            }
            while (!started.get()) {
                Thread.onSpinWait();
            }
            change.call();
            for (Future<?> r : readers) {
                r.get();
            }
            // A few more after the change completed, so the "after" state is always observed.
            for (int i = 0; i < 3; i++) {
                statuses.add(mvc.perform(get(url).session(session)).andReturn().getResponse().getStatus());
            }
        } finally {
            pool.shutdown();
        }
        return statuses;
    }

    private MockHttpSession signIn(String username, Role role) throws Exception {
        try {
            accounts.create(username, PASSWORD, role, false);
        } catch (UsernameTakenException alreadyCreated) {
            // shared context
        }
        MvcResult result = mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", username, "password", PASSWORD))))
                .andExpect(status().isOk()).andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    private JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private static MockMultipartFile pdf(int pages) throws IOException {
        try (PDDocument document = new PDDocument()) {
            for (int i = 0; i < pages; i++) {
                document.addPage(new PDPage(PDRectangle.A6));
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return new MockMultipartFile("file", "busy.pdf", "application/pdf", out.toByteArray());
        }
    }
}
