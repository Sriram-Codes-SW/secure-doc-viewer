package com.example.securedocviewer;

import com.example.securedocviewer.account.Role;
import com.example.securedocviewer.account.UserAccountService;
import com.example.securedocviewer.audit.AuditEvent.Actor;
import com.example.securedocviewer.audit.AuditEvent.Subject;
import com.example.securedocviewer.audit.AuditEventType;
import com.example.securedocviewer.audit.AuditLogService;
import com.example.securedocviewer.document.DocumentDetail;
import com.example.securedocviewer.document.DocumentService;
import com.example.securedocviewer.document.Viewer;
import com.example.securedocviewer.document.Visibility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The parts H2 can't vouch for, run against the same MySQL version as
 * production: the Flyway migrations, the row lock that serialises PDF
 * replacement, and UTC storage of timestamps. Skipped where Docker isn't
 * available; CI always has it.
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class MySqlIntegrationTest {

    private static final Path STORAGE = Path.of("./target/test-storage");

    /**
     * The server runs in a non-UTC zone and the JVM in another, like a dev
     * machine in India talking to a database set to local time: stored
     * timestamps must still come out as UTC.
     */
    @Container
    @ServiceConnection
    static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4")
            .withCommand("--default-time-zone=-03:00")
            .withUrlParam("connectionTimeZone", "UTC")
            .withUrlParam("forceConnectionTimeZoneToSession", "true");

    private static TimeZone originalZone;

    @BeforeAll
    static void runTheJvmOutsideUtc() {
        originalZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
    }

    @AfterAll
    static void restoreZone() {
        TimeZone.setDefault(originalZone);
    }

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private UserAccountService accounts;

    @Autowired
    private DocumentService documents;

    @Autowired
    private AuditLogService audit;

    @Test
    void everyMigrationAppliesToMySql() {
        List<String> applied = jdbc.queryForList(
                "select version from flyway_schema_history where success = 1 and version is not null order by installed_rank",
                String.class);
        assertEquals(List.of("1", "2", "3"), applied);
        assertEquals(1, jdbc.queryForObject("""
                select count(*) from information_schema.columns
                where table_schema = database() and table_name = 'document' and column_name = 'tile_version'""",
                Integer.class));
    }

    @Test
    void timestampsAreStoredInUtcWhateverTheServerAndJvmZones() {
        assertEquals("+00:00", jdbc.queryForObject("select @@session.time_zone", String.class).replace("UTC", "+00:00"));

        audit.record(AuditEventType.SIGN_IN, new Actor("tz-probe", null, "127.0.0.1"), Subject.none());
        String stored = jdbc.queryForObject("""
                select date_format(occurred_at, '%Y-%m-%dT%H:%i:%s') from audit_event
                where username = 'tz-probe' order by id desc limit 1""", String.class);
        Duration drift = Duration.between(LocalDateTime.parse(stored), LocalDateTime.now(ZoneOffset.UTC)).abs();
        assertTrue(drift.compareTo(Duration.ofMinutes(1)) < 0, "stored " + stored + " is not UTC");
    }

    @Test
    void concurrentReplacementsAreSerialisedByTheRowLock() throws Exception {
        accounts.create("tc-owner", "correct-horse-battery", Role.PUBLISHER, false);
        Viewer owner = new Viewer("tc-owner", false, true);
        Actor actor = new Actor("tc-owner", null, "127.0.0.1");
        String id = documents.upload("Locked", "locked.pdf", pdf(1), Visibility.PRIVATE, owner, actor).documentId();

        Callable<DocumentDetail> replace = () -> documents.replaceFile(id, pdf(2), owner, actor);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            List<Future<DocumentDetail>> results = pool.invokeAll(List.of(replace, replace));
            for (Future<DocumentDetail> result : results) {
                assertEquals(2, result.get().pageCount());
            }
        } finally {
            pool.shutdown();
        }

        // Each replace got its own version (no lost update, no clash on v2) and
        // only the newest one is left on disk.
        assertEquals(3, jdbc.queryForObject("select tile_version from document where id = ?", Integer.class, id));
        assertTrue(Files.isDirectory(STORAGE.resolve(id).resolve("v3").resolve("page-1")));
        assertFalse(Files.exists(STORAGE.resolve(id).resolve("v1")));
        assertFalse(Files.exists(STORAGE.resolve(id).resolve("v2")));
    }

    private static ByteArrayInputStream pdf(int pages) throws IOException {
        try (PDDocument document = new PDDocument()) {
            for (int i = 0; i < pages; i++) {
                document.addPage(new PDPage(PDRectangle.A6));
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}
