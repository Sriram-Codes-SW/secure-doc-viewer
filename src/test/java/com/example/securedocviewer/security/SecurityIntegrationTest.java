package com.example.securedocviewer.security;

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
            readerListed |= session.get("username").asString().equals("listed-reader");
            assertFalse(session.has("sessionId"), "response has a sessionId field");
            String handle = session.get("handle").asString();
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
        String tileUrl = grid.at("/tileUrls/0/0").asString();
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
    void anAdminSetPasswordMustBeChangedBeforeAnythingElseWorks() throws Exception {
        MockHttpSession admin = login("admin", "bootstrap-admin-password");
        mvc.perform(post("/api/admin/users").session(admin).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", "fresh-user",
                                "password", "temporary-password-1", "role", "READER"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mustChangePassword").value(true));

        MvcResult signIn = mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest("fresh-user", "temporary-password-1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mustChangePassword").value(true))
                .andReturn();
        MockHttpSession fresh = (MockHttpSession) signIn.getRequest().getSession(false);

        mvc.perform(get("/api/documents").session(fresh))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.passwordChangeRequired").value(true));
        mvc.perform(get("/api/auth/me").session(fresh)).andExpect(status().isOk());

        mvc.perform(post("/api/auth/password").session(fresh).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("currentPassword", "temporary-password-1",
                                "newPassword", "my-own-password-123"))))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/documents").session(fresh)).andExpect(status().isOk());
        mvc.perform(get("/api/auth/me").session(fresh)).andExpect(jsonPath("$.mustChangePassword").value(false));
    }

    @Test
    void adminCanUnlockAnAccountButReadersCannot() throws Exception {
        user("locked-user", Role.READER);
        for (int i = 0; i < LoginThrottle.MAX_FAILURES_PER_ACCOUNT; i++) {
            mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                    .content(loginRequest("locked-user", "wrong-" + i))).andExpect(status().isUnauthorized());
        }
        mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest("locked-user", PASSWORD))).andExpect(status().isTooManyRequests());

        user("plain-reader-2", Role.READER);
        MockHttpSession reader = login("plain-reader-2", PASSWORD);
        mvc.perform(post("/api/admin/users/locked-user/unlock").session(reader).with(csrf()))
                .andExpect(status().isForbidden());
        MockHttpSession admin = login("admin", "bootstrap-admin-password");
        mvc.perform(post("/api/admin/users/locked-user/unlock").session(admin))
                .andExpect(status().isForbidden()); // no CSRF token
        mvc.perform(post("/api/admin/users/no-such-user/unlock").session(admin).with(csrf()))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/admin/users/Locked-User/unlock").session(admin).with(csrf()))
                .andExpect(status().isNoContent());

        mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest("locked-user", PASSWORD))).andExpect(status().isOk());
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

    @Test
    void aBurstOfParallelWrongPasswordsGetsNoMoreThanTheLimit() throws Exception {
        user("burst-user", Role.READER);
        int attempts = 12;
        java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(attempts);
        java.util.List<java.util.concurrent.Future<Integer>> statuses = new java.util.ArrayList<>();
        java.util.concurrent.CountDownLatch start = new java.util.concurrent.CountDownLatch(1);
        for (int i = 0; i < attempts; i++) {
            String guess = "wrong-guess-" + i;
            statuses.add(pool.submit(() -> {
                start.await();
                return mvc.perform(post("/api/auth/login").with(csrf()).with(from("198.51.100.61"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest("burst-user", guess))).andReturn().getResponse().getStatus();
            }));
        }
        start.countDown();
        int guessed = 0;
        for (java.util.concurrent.Future<Integer> status : statuses) {
            int code = status.get();
            assertTrue(code == 401 || code == 429, "unexpected " + code);
            guessed += code == 401 ? 1 : 0;
        }
        pool.shutdown();
        assertEquals(LoginThrottle.MAX_FAILURES_PER_ACCOUNT, guessed, "passwords actually checked");
    }

    @Test
    void passwordsLongerThanBcryptAcceptsAreRefusedCleanly() throws Exception {
        MockHttpSession admin = login("admin", "bootstrap-admin-password");
        for (String tooLong : new String[] {"x".repeat(100), "\uD83D\uDD12".repeat(30)}) {
            mvc.perform(post("/api/admin/users").session(admin).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    Map.of("username", "long-pw-user", "password", tooLong, "role", "READER"))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("72 bytes")));
        }
        // Signing in with one is just a failed sign-in, never a 500.
        mvc.perform(post("/api/auth/login").with(csrf()).with(from("198.51.100.62"))
                        .contentType(MediaType.APPLICATION_JSON).content(loginRequest("admin", "y".repeat(100))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void guessingTheCurrentPasswordFromASessionIsThrottled() throws Exception {
        user("pwguess-user", Role.READER);
        MockHttpSession session = login("pwguess-user", PASSWORD);
        for (int i = 0; i < LoginThrottle.MAX_FAILURES_PER_ACCOUNT; i++) {
            mvc.perform(post("/api/auth/password").session(session).with(csrf()).with(from("198.51.100.63"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "currentPassword", "not-it-" + i, "newPassword", "a-brand-new-password"))))
                    .andExpect(status().isBadRequest());
        }
        mvc.perform(post("/api/auth/password").session(session).with(csrf()).with(from("198.51.100.63"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "currentPassword", PASSWORD, "newPassword", "a-brand-new-password"))))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void readersAreRefusedAReplacementUploadBeforeItIsRead() throws Exception {
        user("replace-reader", Role.READER);
        MockHttpSession reader = login("replace-reader", PASSWORD);
        mvc.perform(multipart(org.springframework.http.HttpMethod.PUT, "/api/documents/any-id/file")
                        .file(pdfPart()).session(reader).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void aSessionEndsAFixedTimeAfterSignInHoweverActive() throws Exception {
        user("lifetime-user", Role.READER);
        MockHttpSession session = login("lifetime-user", PASSWORD);
        mvc.perform(get("/api/auth/me").session(session)).andExpect(status().isOk());
        session.setAttribute(SessionLifetimeFilter.SIGNED_IN_AT, java.time.Instant.now().minus(java.time.Duration.ofHours(13)));
        mvc.perform(get("/api/auth/me").session(session)).andExpect(status().isUnauthorized());
    }

    /** Tests that cause failures use their own address, so they don't use up 127.0.0.1's allowance. */
    private static org.springframework.test.web.servlet.request.RequestPostProcessor from(String ip) {
        return request -> {
            request.setRemoteAddr(ip);
            return request;
        };
    }

    private void user(String username, Role role) {
        try {
            accounts.create(username, PASSWORD, role, false);
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
            if (session.get("username").asString().equals(username)) {
                return session.get("handle").asString();
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
        return json(result).get("documentId").asString();
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
