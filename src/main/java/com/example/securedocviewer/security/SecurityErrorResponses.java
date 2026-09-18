package com.example.securedocviewer.security;

import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/** Same {"error": "..."} JSON shape as GlobalExceptionHandler, for failures raised inside the filter chain. */
@Component
public class SecurityErrorResponses
        implements AuthenticationEntryPoint, AccessDeniedHandler, SessionInformationExpiredStrategy {

    private final ObjectMapper objectMapper;

    public SecurityErrorResponses(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        write(response, HttpStatus.UNAUTHORIZED, "Sign-in required.");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        String message = accessDeniedException instanceof CsrfException
                ? "Missing or invalid CSRF token. Reload the page and try again."
                : "You don't have permission to do that.";
        write(response, HttpStatus.FORBIDDEN, message);
    }

    /** A session revoked by an admin (or otherwise expired in the registry). */
    @Override
    public void onExpiredSessionDetected(SessionInformationExpiredEvent event) throws IOException {
        write(event.getResponse(), HttpStatus.UNAUTHORIZED, "Your session has ended. Please sign in again.");
    }

    private void write(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), Map.of("error", message));
    }
}
