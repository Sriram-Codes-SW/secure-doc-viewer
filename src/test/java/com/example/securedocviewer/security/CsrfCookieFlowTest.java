package com.example.securedocviewer.security;

import com.example.securedocviewer.account.Role;
import com.example.securedocviewer.account.UserAccountService;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Drives the CSRF cookie exactly as a browser would, with no test CSRF
 * helper. Kept in its own class with a fresh context because
 * spring-security-test's csrf() post-processor permanently swaps the CSRF
 * filter's repository in whatever context it runs in, after which no real
 * XSRF-TOKEN cookie is ever written.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class CsrfCookieFlowTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserAccountService accounts;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void signInResponseCarriesAFreshCsrfTokenUsableImmediately() throws Exception {
        // Regression: Spring's CsrfAuthenticationStrategy deleted the cookie
        // at sign-in without issuing a new one, so the first write made
        // straight after signing in got a 403.
        accounts.create("csrf-user", "correct-horse-battery", Role.READER);

        MvcResult anonymous = mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized()).andReturn();
        Cookie initialToken = anonymous.getResponse().getCookie("XSRF-TOKEN");
        assertNotNull(initialToken, "first visit did not set XSRF-TOKEN");

        MvcResult signIn = mvc.perform(post("/api/auth/login")
                        .session((MockHttpSession) anonymous.getRequest().getSession(true))
                        .cookie(initialToken)
                        .header("X-XSRF-TOKEN", initialToken.getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", "csrf-user", "password", "correct-horse-battery"))))
                .andExpect(status().isOk())
                .andReturn();
        Cookie rotated = signIn.getResponse().getCookie("XSRF-TOKEN");
        assertNotNull(rotated, "sign-in response did not set XSRF-TOKEN");
        assertFalse(rotated.getValue().isEmpty(), "sign-in response cleared XSRF-TOKEN without replacing it");
        assertNotEquals(initialToken.getValue(), rotated.getValue(), "CSRF token was not rotated at sign-in");

        // The old token must no longer work...
        MockHttpSession session = (MockHttpSession) signIn.getRequest().getSession(false);
        mvc.perform(post("/api/auth/logout").session(session)
                        .cookie(rotated).header("X-XSRF-TOKEN", initialToken.getValue()))
                .andExpect(status().isForbidden());
        // ...and the new one must work straight away.
        mvc.perform(post("/api/auth/logout").session(session)
                        .cookie(rotated).header("X-XSRF-TOKEN", rotated.getValue()))
                .andExpect(status().isNoContent());
    }
}
