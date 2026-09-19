package com.example.securedocviewer.controller;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Every failure is {"error": "..."} JSON with the right status and no internals. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(ErrorContractTest.FailingController.class)
class ErrorContractTest {

    @TestConfiguration
    @RestController
    static class FailingController {
        @GetMapping("/api/test/boom")
        String boom() {
            throw new IllegalStateException("SELECT * FROM secret_table WHERE path='C:/internal'");
        }
    }

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void unknownApiRouteIsAJson404() throws Exception {
        mvc.perform(get("/api/no-such-endpoint").session(admin()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not found."));
    }

    @Test
    void wrongMethodIsAJson405() throws Exception {
        mvc.perform(delete("/api/auth/me").session(admin()).with(csrf()))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void missingParameterOrFileIsAJson400() throws Exception {
        mvc.perform(get("/api/tiles").session(admin()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Missing required 'token'."));
        mvc.perform(multipart("/api/documents").session(admin()).with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Missing required 'file'."));
    }

    @Test
    void unexpectedErrorsAreGeneric500sWithoutInternals() throws Exception {
        String body = mvc.perform(get("/api/test/boom").session(admin()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.startsWith("Something went wrong on our side. Reference: ")))
                .andReturn().getResponse().getContentAsString();
        assertFalse(body.contains("secret_table") || body.contains("C:/internal") || body.contains("IllegalState"), body);
    }

    private MockHttpSession admin() throws Exception {
        return (MockHttpSession) mvc.perform(post("/api/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", "admin", "password", "bootstrap-admin-password"))))
                .andExpect(status().isOk())
                .andReturn().getRequest().getSession(false);
    }
}
