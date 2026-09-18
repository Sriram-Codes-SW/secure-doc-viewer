package com.example.securedocviewer.security;

import com.example.securedocviewer.account.Role;
import com.example.securedocviewer.account.UserAccountService;
import com.example.securedocviewer.exception.UsernameTakenException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end checks of the access rules through the real filter chain, one
 * test per property the security review called out.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    private static final String PASSWORD = "correct-horse-battery";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserAccountService accounts;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void apiRequiresSignIn() throws Exception {
        mvc.perform(get("/api/documents"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Sign-in required."));
    }

    @Test
    void wrongPasswordAndUnknownUserGetTheSameAnswer() throws Exception {
        user("wrongpw-user", Role.READER);
        String wrongPassword = loginRequest("wrongpw-user", "not-the-password");
        String unknownUser = loginRequest("no-such-user", "not-the-password");

        String a = mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(wrongPassword))
                .andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();
        String b = mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(unknownUser))
                .andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();
        assertEquals(a, b);
    }

    @Test
    void repeatedFailuresLockTheAccountWithRetryAfter() throws Exception {
        user("lockout-user", Role.READER);
        for (int i = 0; i < LoginThrottle.MAX_FAILURES_PER_ACCOUNT; i++) {
            mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                            .content(loginRequest("lockout-user", "wrong-password-" + i)))
                    .andExpect(status().isUnauthorized());
        }
        // Even the right password is refused while locked.
        mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest("lockout-user", PASSWORD)))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
    }

    @Test
    void disabledAccountCannotSignIn() throws Exception {
        user("disabled-user", Role.READER);
        accounts.update("admin", "disabled-user", null, false);
        mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest("disabled-user", PASSWORD)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void signInNeverExposesTheSessionId() throws Exception {
        user("cookie-user", Role.READER);
        MvcResult result = mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest("Cookie-User", PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("cookie-user"))
                .andExpect(jsonPath("$.role").value("READER"))
                .andExpect(jsonPath("$.sessionTimeoutSeconds").value(1800))
                .andReturn();
        String sessionId = result.getRequest().getSession(false).getId();
        assertFalse(result.getResponse().getContentAsString().contains(sessionId));
    }

    @Test
    void stateChangingRequestsNeedACsrfToken() throws Exception {
        MockHttpSession admin = login("admin", "bootstrap-admin-password");
        mvc.perform(post("/api/auth/logout").session(admin))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Missing or invalid CSRF token. Reload the page and try again."));
    }

    @Test
    void readersCannotReachAdminOrUpload() throws Exception {
        user("plain-reader", Role.READER);
        MockHttpSession reader = login("plain-reader", PASSWORD);

        mvc.perform(get("/api/admin/sessions").session(reader)).andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/audit").session(reader)).andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/users").session(reader)).andExpect(status().isForbidden());
        mvc.perform(multipart("/api/documents").file(pdfPart()).param("title", "x").session(reader).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminSessionListUsesHandlesNotSessionIds() throws Exception {
        user("listed-reader", Role.READER);
        MockHttpSession reader = login("listed-reader", PASSWORD);
        MockHttpSession admin = login("admin", "bootstrap-admin-password");

        JsonNode sessions = json(mvc.perform(get("/api/admin/sessions").session(admin))
                .andExpect(status().isOk())
                .andReturn());

        boolean readerListed = false;
        for (JsonNode session : sessions) {
            readerListed |= session.get("username").asText().equals("listed-reader");
            assertFalse(session.has("sessionId"), "response has a sessionId field");
            String handle = session.get("handle").asText();
            assertNotEquals(reader.getId(), handle, "reader's session id exposed as handle");
            assertNotEquals(admin.getId(), handle, "admin's session id exposed as handle");
        }
        assertTrue(readerListed);
    }

    @Test
    void revokedSessionIsRejectedOnItsNextRequest() throws Exception {
        user("revoked-reader", Role.READER);
        MockHttpSession reader = login("revoked-reader", PASSWORD);
        MockHttpSession admin = login("admin", "bootstrap-admin-password");
        mvc.perform(get("/api/auth/me").session(reader)).andExpect(status().isOk());

        String handle = handleOf("revoked-reader", admin);
        mvc.perform(delete("/api/admin/sessions/" + handle).session(admin).with(csrf()))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/auth/me").session(reader)).andExpect(status().isUnauthorized());
    }

    @Test
    void tileLinksOnlyWorkForTheSessionTheyWereIssuedTo() throws Exception {
        user("tile-owner", Role.READER);
        user("tile-thief", Role.READER);
        MockHttpSession admin = login("admin", "bootstrap-admin-password");
        String documentId = upload(admin);

        MockHttpSession owner = login("tile-owner", PASSWORD);
        MockHttpSession thief = login("tile-thief", PASSWORD);
        JsonNode grid = json(mvc.perform(get("/api/documents/" + documentId + "/pages/0/tile-urls").session(owner))
                .andExpect(status().isOk()).andReturn());
        String tileUrl = grid.at("/tileUrls/0/0").asText();
        assertFalse(tileUrl.contains(owner.getId()), "tile URL leaks the session id");

        mvc.perform(get(tileUrl).session(owner)).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG));
        mvc.perform(get(tileUrl).session(thief)).andExpect(status().isUnauthorized());
        mvc.perform(get(tileUrl)).andExpect(status().isUnauthorized());
    }

    @Test
    void auditLimitIsValidated() throws Exception {
        MockHttpSession admin = login("admin", "bootstrap-admin-password");
        mvc.perform(get("/api/admin/audit").param("size", "0").session(admin)).andExpect(status().isBadRequest());
        mvc.perform(get("/api/admin/audit").param("size", "501").session(admin)).andExpect(status().isBadRequest());
        mvc.perform(get("/api/admin/audit").param("page", "-1").session(admin)).andExpect(status().isBadRequest());
        mvc.perform(get("/api/admin/audit").param("type", "NOT_A_TYPE").session(admin))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/admin/audit").param("size", "10").session(admin)).andExpect(status().isOk());
    }

    @Test
    void adminCanCreateUsersButNotDemoteThemselves() throws Exception {
        MockHttpSession admin = login("admin", "bootstrap-admin-password");
        String create = objectMapper.writeValueAsString(Map.of(
                "username", "New.Publisher", "password", PASSWORD, "role", "PUBLISHER"));

        mvc.perform(post("/api/admin/users").session(admin).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(create))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("new.publisher"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
        mvc.perform(post("/api/admin/users").session(admin).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(create))
                .andExpect(status().isConflict());

        mvc.perform(patch("/api/admin/users/admin").session(admin).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"READER\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void roleChangeEndsTheUsersExistingSessions() throws Exception {
        user("promoted-user", Role.READER);
        MockHttpSession promoted = login("promoted-user", PASSWORD);
        MockHttpSession admin = login("admin", "bootstrap-admin-password");

        mvc.perform(patch("/api/admin/users/promoted-user").session(admin).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"PUBLISHER\"}"))
                .andExpect(status().isOk());

        mvc.perform(get("/api/auth/me").session(promoted)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/me").session(login("promoted-user", PASSWORD)))
                .andExpect(jsonPath("$.role").value("PUBLISHER"));
    }

    private void user(String username, Role role) {
        try {
            accounts.create(username, PASSWORD, role);
        } catch (UsernameTakenException alreadyCreated) {
            // Context (and its in-memory database) is shared across tests.
        }
    }

    private MockHttpSession login(String username, String password) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(loginRequest(username, password)))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    private String handleOf(String username, MockHttpSession admin) throws Exception {
        for (JsonNode session : json(mvc.perform(get("/api/admin/sessions").session(admin)).andReturn())) {
            if (session.get("username").asText().equals(username)) {
                return session.get("handle").asText();
            }
        }
        throw new AssertionError("no session listed for " + username);
    }

    private String upload(MockHttpSession publisher) throws Exception {
        MvcResult result = mvc.perform(multipart("/api/documents").file(pdfPart())
                        .param("title", "Integration test doc").param("visibility", "EVERYONE")
                        .session(publisher).with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        return json(result).get("documentId").asText();
    }

    private String loginRequest(String username, String password) throws Exception {
        return objectMapper.writeValueAsString(Map.of("username", username, "password", password));
    }

    private JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private static MockMultipartFile pdfPart() throws IOException {
        try (PDDocument document = new PDDocument()) {
            document.addPage(new PDPage(PDRectangle.LETTER));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return new MockMultipartFile("file", "test.pdf", "application/pdf", out.toByteArray());
        }
    }
}
