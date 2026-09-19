package com.example.securedocviewer.service;

import com.example.securedocviewer.config.ViewerProperties;
import com.example.securedocviewer.exception.ServiceBusyException;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

class TileWorkLimiterTest {

    @Test
    void refusesWithServiceBusyWhenEverySlotStaysTaken() throws Exception {
        ViewerProperties properties = new ViewerProperties();
        properties.setMaxConcurrentTileRenders(1);
        TileWorkLimiter limiter = new TileWorkLimiter(properties, new ViewerMetrics(new SimpleMeterRegistry()));

        CountDownLatch holding = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            Future<String> first = pool.submit(() -> limiter.run(() -> {
                holding.countDown();
                release.await();
                return "first";
            }));
            holding.await();

            ServiceBusyException busy = assertThrows(ServiceBusyException.class, () -> limiter.run(() -> "second"));
            assertEquals(1, busy.getRetryAfterSeconds());

            release.countDown();
            assertEquals("first", first.get());
            assertEquals("third", limiter.run(() -> "third"), "the slot is free again");
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void workThatFailsStillFreesItsSlot() throws Exception {
        ViewerProperties properties = new ViewerProperties();
        properties.setMaxConcurrentTileRenders(1);
        TileWorkLimiter limiter = new TileWorkLimiter(properties, new ViewerMetrics(new SimpleMeterRegistry()));

        assertThrows(java.io.IOException.class, () -> limiter.run(() -> {
            throw new java.io.IOException("unreadable tile");
        }));
        assertEquals(1, limiter.availableSlots());
        assertEquals("next", limiter.run(() -> "next"));
    }
}
