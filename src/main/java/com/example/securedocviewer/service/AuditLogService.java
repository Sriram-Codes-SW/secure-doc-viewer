package com.example.securedocviewer.service;

import com.example.securedocviewer.model.AuditEntry;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * A bounded, in-memory log of every tile a session has redeemed. Kept as a
 * fixed-capacity ring buffer rather than an unbounded list so a long-running
 * demo instance can't leak memory — a real deployment would ship this to a
 * proper log/audit store instead of holding it in the JVM.
 */
@Service
public class AuditLogService {

    private static final int MAX_ENTRIES = 500;

    private final Deque<AuditEntry> entries = new ArrayDeque<>();

    public synchronized void record(AuditEntry entry) {
        entries.addFirst(entry);
        while (entries.size() > MAX_ENTRIES) {
            entries.removeLast();
        }
    }

    /** Most recent entries first, newest-to-oldest, capped at {@code limit}. */
    public synchronized List<AuditEntry> recent(int limit) {
        List<AuditEntry> result = new ArrayList<>(Math.min(limit, entries.size()));
        int i = 0;
        for (AuditEntry entry : entries) {
            if (i++ >= limit) {
                break;
            }
            result.add(entry);
        }
        return result;
    }
}
