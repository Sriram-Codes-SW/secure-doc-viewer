<!-- chapter: 14 | part: II | owner: writer-backend | tag: book-m2-documents | status: draft -->
# Chapter 14: Storing data with JPA and Flyway

The Secure Document Viewer keeps accounts, documents, shares and an audit trail in MySQL. This chapter shows how Java objects map to tables, how the schema is created and changed safely, how transactions keep changes all-or-nothing, and how timed cleanup jobs run.

## Learning objectives

By the end of this chapter, you will be able to:

- Explain the mismatch between objects and tables and how an ORM bridges it.
- Read an entity class and a Spring Data repository interface.
- Explain what a transaction is and compare `@Transactional` with `TransactionTemplate`.
- Read a Flyway migration and explain why the schema is versioned SQL files.
- Explain a row lock and when `REQUIRES_NEW` is needed.
- Describe how `@Scheduled` runs the project's cleanup sweeps.

## Prerequisites

- Chapter 4: classes, objects, records
- Chapter 9: SQL and MySQL (tables, rows, keys, foreign keys)
- Chapter 11: Spring Boot foundations (beans, configuration)

## Beginner tier: Objects on one side, tables on the other

### 14.1 Objects and tables: the mismatch

Java code works with objects that hold other objects: a `Document` has an owner, a list of pages and a set of users it's shared with. A database holds flat tables of rows, linked by numbers called foreign keys. Translating between the two by hand means writing SQL for every save and load, and then copying columns into fields.

An **ORM** (object-relational mapper) does the translation from declarations you write once. The standard Java specification is **JPA** (Jakarta Persistence); **Hibernate** is the implementation the project uses (Hibernate 7 with Spring Boot 4). **Spring Data JPA** sits on top and writes the common queries for you.

**Where the analogy breaks down.** An ORM feels like a translator who makes the database disappear. It doesn't: every property you map costs a column, and a careless mapping can run hundreds of queries. You still need to know SQL (Chapter 9) to know what is happening.

### 14.2 Entities and repositories

An **entity** is a class mapped to a table. Listing 14.1 is the account entity.

**Listing 14.1 — `AppUser.java` (`book-m6-final`, simplified: getters and setters after the constructors, and two columns, are omitted)**

*`src/main/java/com/example/securedocviewer/account/AppUser.java`*

```java
@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // ... must_change_password and last_sign_in_at columns omitted ...

    protected AppUser() {
    }

    public AppUser(String username, String passwordHash, Role role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = Instant.now();
    }
```

`@Entity` and `@Table` say "this class is the `app_user` table". `@Id` marks the primary key and `IDENTITY` lets MySQL generate it (`AUTO_INCREMENT`). `@Column` maps a field to a column and repeats constraints such as `nullable = false`. `@Enumerated(EnumType.STRING)` stores the role as the text `READER`, `PUBLISHER` or `ADMIN` rather than a number, so reordering the enum can never silently change what a stored value means. The protected no-argument constructor is there for Hibernate; your code uses the public one.

A **repository** is an interface through which you load and save entities. You write no implementation. Listing 14.2 is the account repository.

**Listing 14.2 — `AppUserRepository.java` (`book-m6-final`)**

*`src/main/java/com/example/securedocviewer/account/AppUserRepository.java`*

```java
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);

    boolean existsByUsername(String username);

    List<AppUser> findAllByOrderByUsernameAsc();

    List<AppUser> findTop20ByEnabledTrueAndUsernameStartingWithOrderByUsernameAsc(String prefix);
}
```

Extending `JpaRepository<AppUser, Long>` gives you `save`, `findById`, `count`, `delete` and more. Each extra method is a **query method**: Spring reads its name and writes the SQL. `findByUsername` becomes `select ... where username = ?`. `Optional` (Chapter 5) forces the caller to handle "no such user". Spring creates the implementation as a bean, so you inject the interface like any other dependency (Chapter 11).

## Intermediate tier: Changing data safely

### 14.3 Query methods and transactions

A **transaction** groups several database changes so that either all succeed or none do: it either **commits** (makes them all permanent) or **rolls back** (undoes everything it did). Without one, a crash halfway through "create the document row, then its page rows" would leave half a document. `UserAccountService` uses the simplest form: an annotation.

```java
@Transactional
public UserSummary create(String rawUsername, String password, Role role, boolean mustChangePassword) {
```

(`book-m6-final`, `UserAccountService.java`, signature only.) Spring wraps the method: it begins a transaction, runs the body, commits if it returns, and rolls back if it throws. Read-only methods use `@Transactional(readOnly = true)`. Inside the transaction, an entity you load is tracked, so `user.setEnabled(enabled)` in `update` is saved at commit without calling `save`.

`DocumentService` doesn't use the annotation. It holds a `TransactionTemplate` and wraps only the parts that need it:

```java
this.tx = new TransactionTemplate(transactionManager);
// ...
return tx.execute(status -> detail(requireViewable(documentId, viewer, actor), viewer));
```

(`book-m6-final`, `DocumentService.java`, excerpts.) The reason is in the class comment: "Rendering a PDF is slow, so it runs outside any database transaction: render to staging, then commit tiles and metadata." A transaction holds a database connection; holding one while a PDF renders for seconds would starve other requests. The template lets the service open the transaction only around the fast database work.

### 14.4 Flyway migrations (`V1`, `V2`, `V3`)

Someone has to create the tables. If Hibernate did it automatically, the schema would depend on whichever code last ran, and production changes would be guesses. Instead the project sets `spring.jpa.hibernate.ddl-auto: none` ("Flyway owns the schema; Hibernate never alters it", in `application.yml`) and uses **Flyway**, which applies numbered SQL files in order and records what it applied in a table, so each runs exactly once on each database.

**Listing 14.3 — `V1__create_app_user.sql` (`book-m2-documents`, identical at `book-m6-final`)**

*`src/main/resources/db/migration/V1__create_app_user.sql`*

```sql
-- Accounts that can sign in. Usernames are stored lower-cased by the
-- application, so the unique constraint is effectively case-insensitive.
CREATE TABLE app_user (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    username      VARCHAR(64)  NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_app_user_username UNIQUE (username)
);
```

Compare it with Listing 14.1: each column matches a field. The project has three migrations at the final tag. `V1` creates `app_user`. `V2` creates `document`, `document_page`, `document_share` and `audit_event`, with indexes. `V3` adds columns (`tile_version`, `must_change_password`, `last_sign_in_at`) and the `account_known_ip` table. **Never edit a migration that has run**: Flyway detects the change and refuses to start. To change the schema, add `V4`.

### 14.5 Locking rows, optimistic and pessimistic

Two people replacing the same PDF at the same moment could overwrite each other. A **row lock** makes the second wait. `DocumentRepository` has:

```java
/** Row lock for replace/delete, so concurrent changes to one document are serialised. */
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("select d from Document d where d.id = :id")
Optional<Document> findByIdForUpdate(@Param("id") String id);
```

(`book-m6-final`, `DocumentRepository.java`, excerpt.) A **pessimistic** lock assumes conflicts are likely and blocks up front (`select ... for update`). An **optimistic** approach lets both proceed and detects the conflict at save time. The project chose pessimistic for replace and delete, where a conflict would corrupt files on disk, and the rows are few. `@Query` uses JPQL, a query language over entities rather than tables.

## Advanced tier: Separate transactions, cleanup and real databases

### 14.6 Separate transactions: `REQUIRES_NEW`

An audit trail must record failures, but a failure rolls back the caller's transaction, and with it any audit row written inside it. `AuditLogService` therefore writes in its own transaction:

```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void record(AuditEventType type, Actor actor, Subject subject) {
    insert(Instant.now(), type, actor, subject);
}
```

(`book-m6-final`, `AuditLogService.java`.) `REQUIRES_NEW` suspends the caller's transaction, opens another and commits it independently. The class comment gives the reason: the most important events (access denied, a failed operation) "are recorded just before the caller throws and rolls its own transaction back, and must not be rolled back with it." `KnownDevices.remember` uses the same setting. Two cautions: the annotation only works when another bean calls the method (the wrapper sits between beans), which is why the class also builds a `TransactionTemplate` with `PROPAGATION_REQUIRES_NEW` for its internal calls; and the audit code uses plain `JdbcTemplate` SQL rather than JPA, because rows are only inserted and searched, never updated.

### 14.7 Timed sweeps with `@Scheduled`

Some data must be cleaned up on a timer. Chapter 11 mentioned `@EnableScheduling`; with it on, a method marked `@Scheduled` runs by itself. The project has five:

**Table 14.1 — Scheduled sweeps (`book-m6-final`)**

| Method | Schedule | Purpose |
|---|---|---|
| `AuditLogService.purgeExpired` | cron `0 30 3 * * *` (daily, 03:30) | Delete audit events older than the retention period |
| `AuditLogService.sweepThrottled` | fixed delay 1 hour | Forget idle throttle keys, first writing a summary of suppressed events |
| `KnownDevices.purgeExpired` | cron `0 45 3 * * *` (daily, 03:45) | Delete known-address rows older than 30 days |
| `LoginThrottle.sweep` | fixed delay 5 minutes | Drop sign-in counters whose failures have aged out |
| `StorageJanitor.sweep` | 2 minute initial delay, then every 6 hours | Remove tile directories nothing points to |

A **cron** expression lists second, minute, hour, day, month and weekday. A **fixed delay** waits that long after the last run finishes. The cron values are themselves configurable, as in `@Scheduled(cron = "${secure-doc-viewer.audit-retention-cron:0 30 3 * * *}")`, using the placeholder syntax from Chapter 11. The `StorageJanitor` is deliberately conservative: it touches only document-id-shaped directories and only ones older than an hour, "once they are old enough that no upload can still be in flight" (its class comment). Sweeps should be safe to run at any time, safe to repeat, and log rather than crash when one item fails.

### 14.8 Testing with H2 vs. real MySQL: why both

Most tests run against H2, an in-memory database started in MySQL compatibility mode (`jdbc:h2:mem:securedocs;MODE=MySQL;DATABASE_TO_LOWER=TRUE`), so the same Flyway files run with no server. But H2 isn't MySQL. `MySqlIntegrationTest` runs the migrations, the row lock and the timestamp behavior against a real `mysql:8.4` container, and is skipped when Docker isn't available (Chapter 18).

### 14.9 Time zones and UTC storage

A `DATETIME` column has no time zone. If a laptop in one zone and a container in another read the same value, they disagree about the moment. The project stores UTC: the JDBC URL sets `connectionTimeZone=UTC&forceConnectionTimeZoneToSession=true`, and `hibernate.jdbc.time_zone: UTC` is set in `application.yml`. `MySqlIntegrationTest` proves it by running the JVM in `Asia/Kolkata` against a server set to `-03:00` and checking that stored timestamps are still UTC.

## In this project

**Table 14.2 — Where Chapter 14's ideas live**

| Idea | File |
|---|---|
| Entities | `account/AppUser.java`, `document/Document.java`, `document/DocumentPage.java` |
| Repositories | `account/AppUserRepository.java`, `document/DocumentRepository.java` |
| Transactions | `account/UserAccountService.java`, `document/DocumentService.java`, `audit/AuditLogService.java` |
| Migrations | `src/main/resources/db/migration/V1`, `V2`, `V3` |
| Sweeps | `AuditLogService`, `KnownDevices`, `LoginThrottle`, `service/StorageJanitor.java` |

## Try it

### Exercise 14.1 ★ Store roles as text

Which annotation stores the role as text, and why is that safer than a number?

### Exercise 14.2 ★ Indexes on the audit table

Find the migration that creates `audit_event`. Which indexes does it define?

### Exercise 14.3 ★★ A query with no SQL

`findByUsername` has no SQL. Where does the query come from?

### Exercise 14.4 ★★ TransactionTemplate versus @Transactional

Explain why `DocumentService` uses `TransactionTemplate` rather than `@Transactional`.

### Exercise 14.5 ★★★ Editing a migration that already ran

An admin edits a migration that already ran. Predict what happens at the next startup and how to make the change correctly.

## Summary

- JPA and Hibernate map objects to tables; Spring Data writes common queries from method names.
- Flyway applies versioned SQL once each; Hibernate never alters the schema.
- Transactions make changes all-or-nothing; `TransactionTemplate` keeps slow work outside them.
- `PESSIMISTIC_WRITE` serializes concurrent changes to one row; `REQUIRES_NEW` commits audit rows even when the caller rolls back.
- `@Scheduled` runs cleanup sweeps that are safe to repeat.
- Test against H2 for speed and real MySQL for truth; store time in UTC.

## Further reading

- *Spring Data JPA Reference Documentation*, "Query Methods." https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html
- *Spring Framework Reference Documentation*, "Transaction Management." https://docs.spring.io/spring-framework/reference/data-access/transaction.html
- *Spring Framework Reference Documentation*, "Task Execution and Scheduling." https://docs.spring.io/spring-framework/reference/integration/scheduling.html
- *Flyway Documentation*, "Migrations." https://documentation.red-gate.com/flyway
