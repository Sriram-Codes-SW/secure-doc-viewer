package com.example.securedocviewer.audit;

import com.example.securedocviewer.audit.AuditEvent.Actor;
import com.example.securedocviewer.audit.AuditEvent.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Append-only audit trail in the database. Plain JDBC rather than JPA: rows
 * are written on every tile request, never updated, and read back only
 * through filtered, paged queries.
 *
 * <p>Writes run in their own transaction: the events that matter most
 * (access denied, a failed operation) are recorded just before the caller
 * throws and rolls its own transaction back, and must not be rolled back
 * with it.
 */
@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    /** Filters for {@link #search}; any field may be null. */
    /** {@code traceCode} matches the start of the session handle, as printed in the watermark. */
    public record Query(AuditEventType type, String username, String documentId, Instant from, Instant to,
                        String traceCode) {

        public Query(AuditEventType type, String username, String documentId, Instant from, Instant to) {
            this(type, username, documentId, from, to, null);
        }
    }

    public record Page(List<AuditEvent> items, long total, int page, int size) {
    }

    private static final String COLUMNS = """
            id, occurred_at, event_type, username, session_handle, client_ip,
            document_id, document_title, page_index, tile_row, tile_col, detail""";

    private final JdbcTemplate jdbc;
    private final int retentionDays;
    /** Per throttle key: when an event was last written and how many were suppressed since. */
    private static final class Throttled {
        Instant lastWritten;
        int suppressed;
    }

    private final Map<String, Throttled> throttled = new ConcurrentHashMap<>();

    public AuditLogService(JdbcTemplate jdbc,
                           @Value("${secure-doc-viewer.audit-retention-days:180}") int retentionDays) {
        this.jdbc = jdbc;
        this.retentionDays = retentionDays;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(AuditEventType type, Actor actor, Subject subject) {
        jdbc.update("""
                        insert into audit_event (occurred_at, event_type, username, session_handle, client_ip,
                            document_id, document_title, page_index, tile_row, tile_col, detail)
                        values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
                Timestamp.from(Instant.now()), type.name(), actor.username(), actor.sessionHandle(),
                actor.clientIp(), subject.documentId(), truncate(subject.documentTitle(), 200),
                subject.page(), subject.tileRow(), subject.tileCol(), truncate(subject.detail(), 255));
    }

    /**
     * Records the event only if the same key hasn't been recorded within
     * {@code interval}. Used for events that can fire on every request (e.g.
     * throttled tile fetches) and would otherwise flood the log.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordAtMostEvery(Duration interval, String key, AuditEventType type, Actor actor, Subject subject) {
        Instant now = Instant.now();
        Throttled state = throttled.computeIfAbsent(key, k -> new Throttled());
        int suppressedBefore;
        synchronized (state) {
            if (state.lastWritten != null && state.lastWritten.plus(interval).isAfter(now)) {
                state.suppressed++;
                return;
            }
            state.lastWritten = now;
            suppressedBefore = state.suppressed;
            state.suppressed = 0;
        }
        // Say how many similar events were dropped since the last one, so volume isn't hidden.
        Subject withCount = suppressedBefore == 0 ? subject : new Subject(subject.documentId(), subject.documentTitle(),
                subject.page(), subject.tileRow(), subject.tileCol(),
                (subject.detail() == null ? "" : subject.detail() + " ") + "(+" + suppressedBefore + " similar suppressed)");
        record(type, actor, withCount);
    }

    /** Forget throttle keys idle for an hour, so the map stays small. */
    @Scheduled(fixedDelay = 3_600_000)
    public void sweepThrottled() {
        Instant cutoff = Instant.now().minus(Duration.ofHours(1));
        throttled.entrySet().removeIf(e -> {
            synchronized (e.getValue()) {
                return e.getValue().lastWritten == null || e.getValue().lastWritten.isBefore(cutoff);
            }
        });
    }

    public Page search(Query query, int page, int size) {
        List<Object> args = new ArrayList<>();
        String where = whereClause(query, args);
        long total = jdbc.queryForObject("select count(*) from audit_event" + where, Long.class, args.toArray());
        args.add(size);
        args.add((long) page * size);
        List<AuditEvent> items = jdbc.query(
                "select " + COLUMNS + " from audit_event" + where + " order by occurred_at desc, id desc limit ? offset ?",
                ROW_MAPPER, args.toArray());
        return new Page(items, total, page, size);
    }

    /** Newest first, capped at {@code max} rows; for CSV export. */
    public List<AuditEvent> export(Query query, int max) {
        List<Object> args = new ArrayList<>();
        String where = whereClause(query, args);
        args.add(max);
        return jdbc.query("select " + COLUMNS + " from audit_event" + where + " order by occurred_at desc, id desc limit ?",
                ROW_MAPPER, args.toArray());
    }

    /** Deletes events older than the retention period, once a day. */
    @Scheduled(cron = "${secure-doc-viewer.audit-retention-cron:0 30 3 * * *}")
    public void purgeExpired() {
        Instant cutoff = Instant.now().minus(Duration.ofDays(retentionDays));
        int deleted = jdbc.update("delete from audit_event where occurred_at < ?", Timestamp.from(cutoff));
        if (deleted > 0) {
            log.info("Purged {} audit events older than {} days", deleted, retentionDays);
        }
    }

    private static String whereClause(Query query, List<Object> args) {
        List<String> conditions = new ArrayList<>();
        if (query.type() != null) {
            conditions.add("event_type = ?");
            args.add(query.type().name());
        }
        if (query.username() != null && !query.username().isBlank()) {
            conditions.add("username = ?");
            args.add(query.username().trim().toLowerCase());
        }
        if (query.documentId() != null && !query.documentId().isBlank()) {
            conditions.add("document_id = ?");
            args.add(query.documentId().trim());
        }
        if (query.traceCode() != null && !query.traceCode().isBlank()) {
            // Crockford Base32 is case-insensitive; keep only valid characters so
            // the value can't smuggle LIKE wildcards into the pattern.
            String code = query.traceCode().trim().toUpperCase().replaceAll("[^0-9A-Z]", "");
            conditions.add("session_handle like ?");
            args.add(code + "%");
        }
        if (query.from() != null) {
            conditions.add("occurred_at >= ?");
            args.add(Timestamp.from(query.from()));
        }
        if (query.to() != null) {
            conditions.add("occurred_at < ?");
            args.add(Timestamp.from(query.to()));
        }
        return conditions.isEmpty() ? "" : " where " + String.join(" and ", conditions);
    }

    private static String truncate(String value, int max) {
        return value == null || value.length() <= max ? value : value.substring(0, max);
    }

    private static final RowMapper<AuditEvent> ROW_MAPPER = (ResultSet rs, int rowNum) -> new AuditEvent(
            rs.getLong("id"),
            rs.getTimestamp("occurred_at").toInstant().toEpochMilli(),
            AuditEventType.valueOf(rs.getString("event_type")),
            rs.getString("username"),
            rs.getString("session_handle"),
            rs.getString("client_ip"),
            rs.getString("document_id"),
            rs.getString("document_title"),
            nullableInt(rs, "page_index"),
            nullableInt(rs, "tile_row"),
            nullableInt(rs, "tile_col"),
            rs.getString("detail"));

    private static Integer nullableInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }
}
