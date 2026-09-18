package com.example.securedocviewer.service;

import com.example.securedocviewer.model.AuditEntry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuditLogServiceTest {

    private AuditEntry entryAt(long epochSeconds) {
        return new AuditEntry("session-1", "alice", "doc-1", 0, 0, 0, epochSeconds);
    }

    @Test
    void recentReturnsNewestFirst() {
        AuditLogService service = new AuditLogService();

        service.record(entryAt(1));
        service.record(entryAt(2));
        service.record(entryAt(3));

        var recent = service.recent(10);

        assertEquals(3, recent.size());
        assertEquals(3, recent.get(0).timestampEpochSeconds());
        assertEquals(2, recent.get(1).timestampEpochSeconds());
        assertEquals(1, recent.get(2).timestampEpochSeconds());
    }

    @Test
    void recentHonorsTheRequestedLimit() {
        AuditLogService service = new AuditLogService();

        for (int i = 0; i < 10; i++) {
            service.record(entryAt(i));
        }

        assertEquals(3, service.recent(3).size());
    }

    @Test
    void oldestEntriesAreDroppedOnceCapacityIsExceeded() {
        AuditLogService service = new AuditLogService();

        for (int i = 0; i < 600; i++) {
            service.record(entryAt(i));
        }

        var recent = service.recent(1000);

        assertEquals(500, recent.size());
        assertEquals(599, recent.get(0).timestampEpochSeconds());
        assertEquals(100, recent.get(recent.size() - 1).timestampEpochSeconds());
    }
}
