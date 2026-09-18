package com.example.securedocviewer.model;

/**
 * One record of "this session redeemed this exact tile at this instant" —
 * the server-side counterpart to the pixel watermark. The watermark
 * survives a screenshot of the tile itself; this log is what lets an admin
 * answer "who viewed document X" without needing to recover a leaked image
 * at all.
 */
public record AuditEntry(
        String sessionId,
        String username,
        String documentId,
        int page,
        int row,
        int col,
        long timestampEpochSeconds
) {
}
