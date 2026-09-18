package com.example.securedocviewer.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

/**
 * Operational counters scraped from {@code /actuator/prometheus}: what an
 * on-call person needs to tell "a scraper is hammering tiles" or "sign-ins
 * are being brute-forced" from "rendering has become slow". Counts only;
 * who did what lives in the audit log.
 */
@Component
public class ViewerMetrics {

    /** Outcome tag of {@code sdv.sign_in}. */
    public enum SignInOutcome { SUCCESS, FAILURE, LOCKED }

    private final MeterRegistry registry;
    private final Counter tilesServed;
    private final Counter tilesRateLimited;
    private final Counter rendersRejected;
    private final Timer renderTime;

    public ViewerMetrics(MeterRegistry registry) {
        this.registry = registry;
        this.tilesServed = Counter.builder("sdv.tiles.served")
                .description("Watermarked tiles returned").register(registry);
        this.tilesRateLimited = Counter.builder("sdv.tiles.rate_limited")
                .description("Tile requests refused by the per-user rate limit").register(registry);
        this.rendersRejected = Counter.builder("sdv.render.rejected")
                .description("Uploads refused with 503 because every render slot stayed busy").register(registry);
        this.renderTime = Timer.builder("sdv.render")
                .description("Time to render a whole PDF into tiles").register(registry);
        // Registered up front so every outcome is exported as 0 before it first happens.
        for (SignInOutcome outcome : SignInOutcome.values()) {
            signInCounter(outcome);
        }
    }

    public void tileServed() {
        tilesServed.increment();
    }

    public void tileRateLimited() {
        tilesRateLimited.increment();
    }

    public void renderRejected() {
        rendersRejected.increment();
    }

    public void signIn(SignInOutcome outcome) {
        signInCounter(outcome).increment();
    }

    /** Starts timing a render; pass the result to {@link #renderFinished}. */
    public Timer.Sample renderStarted() {
        return Timer.start(registry);
    }

    public void renderFinished(Timer.Sample sample) {
        sample.stop(renderTime);
    }

    private Counter signInCounter(SignInOutcome outcome) {
        return Counter.builder("sdv.sign_in")
                .description("Sign-in attempts by outcome")
                .tag("outcome", outcome.name().toLowerCase())
                .register(registry);
    }
}
