package com.example.securedocviewer.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Allows the Angular dev server (a separate origin) to call this API, to
 * send/read the {@code X-Session-Id} header, and to read {@code Retry-After}
 * on throttled tile responses (the viewer uses it to schedule its retry).
 * Scoped to {@code /api/**} only, and to the known local dev origin — a real deployment would drive the
 * allowed origin list from configuration instead of hardcoding it.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:4200")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("X-Session-Id", "Retry-After");
    }
}
