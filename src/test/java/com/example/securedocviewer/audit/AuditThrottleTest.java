package com.example.securedocviewer.audit;

import com.example.securedocviewer.audit.AuditEvent.Actor;
import com.example.securedocviewer.audit.AuditEvent.Subject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** The per-key audit throttle under parallel callers and a concurrent sweep. */
@SpringBootTest
@ActiveProfiles("test")
class AuditThrottleTest {

    @Autowired
    private AuditLogService audit;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void parallelCallersWriteOneEventPerInterval() throws Exception {
        int callers = 32;
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(callers);
        for (int i = 0; i < callers; i++) {
            pool.submit(() -> {
                start.await();
                audit.recordAtMostEvery(Duration.ofMinutes(10), "race-key", AuditEventType.ACCESS_DENIED,
                        new Actor("throttle-probe", null, "127.0.0.1"), Subject.none());
                if (Thread.currentThread().getName().endsWith("-1")) {
                    audit.sweepThrottled(); // a sweep in the middle must not reopen the interval
                }
                return null;
            });
        }
        start.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(30, TimeUnit.SECONDS));

        assertEquals(1, jdbc.queryForObject(
                "select count(*) from audit_event where username = 'throttle-probe'", Integer.class));
    }
}
