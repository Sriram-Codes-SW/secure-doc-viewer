package com.example.securedocviewer.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Prometheus metrics are readable from the allowed scraper addresses only. */
@SpringBootTest(properties = {
        // Spring Boot turns metrics export off in tests unless asked.
        "management.defaults.metrics.export.enabled=true",
        "management.prometheus.metrics.export.enabled=true"})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MetricsEndpointTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void anAllowedAddressCanScrapeTheViewerCounters() throws Exception {
        // MockMvc requests come from 127.0.0.1, which is allowed by default.
        mvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("sdv_tiles_served_total")))
                .andExpect(content().string(containsString("sdv_tiles_rate_limited_total")))
                .andExpect(content().string(containsString("sdv_sign_in_total{outcome=\"locked\"}")))
                .andExpect(content().string(containsString("sdv_render_seconds")));
    }

    @Test
    void anyOtherAddressIsRefused() throws Exception {
        mvc.perform(get("/actuator/prometheus").with(request -> {
                    request.setRemoteAddr("203.0.113.7");
                    return request;
                }))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(not(containsString("sdv_"))));
    }
}
