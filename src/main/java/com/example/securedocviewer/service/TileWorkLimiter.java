package com.example.securedocviewer.service;

import com.example.securedocviewer.config.ViewerProperties;
import com.example.securedocviewer.exception.ServiceBusyException;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Caps how many tiles are watermarked and encoded at once across all users.
 * The per-user rate limit bounds each reader; this bounds the server when many
 * readers (or accounts) pull tiles together. A request that can't get a slot
 * within a moment gets 503 + Retry-After, which the viewer retries like a 429.
 */
@Component
public class TileWorkLimiter {

    static final long WAIT_MILLIS = 2_000;

    private final Semaphore slots;
    private final ViewerMetrics metrics;

    public TileWorkLimiter(ViewerProperties properties, ViewerMetrics metrics) {
        int configured = properties.getMaxConcurrentTileRenders();
        this.slots = new Semaphore(configured > 0 ? configured : 2 * Runtime.getRuntime().availableProcessors(), true);
        this.metrics = metrics;
    }

    int availableSlots() {
        return slots.availablePermits();
    }

    public <T> T run(Callable<T> work) throws Exception {
        if (!slots.tryAcquire(WAIT_MILLIS, TimeUnit.MILLISECONDS)) {
            metrics.tileBusy();
            throw new ServiceBusyException("The server is busy. Retrying shortly.", 1);
        }
        try {
            return work.call();
        } finally {
            slots.release();
        }
    }
}
