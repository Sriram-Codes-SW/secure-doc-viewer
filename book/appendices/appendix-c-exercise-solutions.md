# Appendix C: Exercise solutions

Try each exercise before you look. Solutions are grouped by chapter. Compiled from the per-chapter solution files.

## Chapter 1 solutions


### Exercise 1.1 ★ Count the tiles

Columns: 2,000 / 512 = 3.9, rounded up to 4. Rows: 3,000 / 512 = 5.86, rounded up to 6. Total: 4 x 6 = 24 tiles. The tiles in the last column (2,000 - 3 x 512 = 464 px wide) and the last row (3,000 - 5 x 512 = 440 px high) are cropped shorter; the rest are 512 x 512.

### Exercise 1.2 ★ Client or server?

Hiding the download button: the client can do it, but it protects nothing. Checking token expiry: only the server, because the client could otherwise change the clock or the token. Blocking an unauthorized user: only the server.

### Exercise 1.3 ★★ Read the limits

One good answer: "A valid session can request every tile" is accepted because readers need normal reading speed (about 15 pages a minute, derived as 180 tiles per minute divided by about 12 tiles per page); the watermark makes the result traceable instead. "No text layer" is accepted because a text layer would be the copyable text the design avoids.

### Exercise 1.4 ★ Authentication or authorization?

Checking your password: authentication (it establishes who you are). Refusing a document that was never shared with you: authorization (it decides what you may do). Ending your session after 30 idle minutes: neither is a perfect fit; it is session management, which protects authentication by making a forgotten sign-in expire. Accept an answer of "authentication" with the reasoning that the session stops proving who you are.

### Exercise 1.5 ★★ Trace a page view

After sign-in and opening the document, the browser makes: one request for the page's tile addresses (`/api/documents/{documentId}/pages/2/tile-urls`), then one request per tile, `/api/tiles?token=<signed-token>`, 12 of them for a 3 by 4 grid. That is 13 requests. With a smaller tile size the grid has more tiles, so more requests: for example 256-pixel tiles on a 1,275 by 1,650 page make 5 columns by 7 rows, 35 tiles, and 36 requests.

### Exercise 1.6 ★★★ Argue for a different trade-off

One good answer. For the skimming customer: 1,024-pixel tiles give 2 by 2, four tiles per page, so at 180 requests a minute a reader can move through about 45 pages a minute, and a script needs about 2,000 tiles / 180 per minute, roughly 11 minutes, for 500 pages. The cost: a harvest becomes much faster, so the watermark is doing more of the work. For the sensitive customer: 256-pixel tiles give 35 tiles per page, and with 120 requests a minute reading drops to about 3 pages a minute while a harvest of 500 pages takes about 2.4 hours (17,500 tiles / 120 per minute). The cost: readers hit the limit and see blank pages while tiles load, which is exactly what an early review found. Note that a document keeps the tile size it was rendered with, so changing the setting affects only documents uploaded afterward.

## Chapter 2 solutions


### Exercise 2.1 ★ Explore the repository

`cd src/main/resources`, then `cat application.yml`. The file is three levels below the project folder (`src`, `main`, `resources`).

### Exercise 2.2 ★ Set and use a variable

`export SCRATCH_SIZE=512` then `echo $SCRATCH_SIZE` prints 512. In a new window the print is empty, because the variable existed only in the first shell process.

### Exercise 2.3 ★★ Why is `.env` ignored?

The `.gitignore` line is `.env` under the "Local secrets" comment. Without it, `git add .` could commit the database password and signing key. Git history is permanent and shared, so the secrets would be exposed and would have to be replaced.

### Exercise 2.4 ★ Redirect and count

```bash
echo "tile one" > list.txt
echo "tile two" >> list.txt
echo "page three" >> list.txt
wc -l list.txt
grep "tile" list.txt
```

`wc -l` prints 3 (with the filename). `grep "tile"` prints the first two lines only. Using `>` for the first line and `>>` for the others matters: a second `>` would replace the file.

### Exercise 2.5 ★★ Who owns port 8080?

On macOS or Linux, `lsof -i :8080` shows a line for the Python process with its PID; on Windows (Git Bash or PowerShell), `netstat -ano | findstr :8080` shows the `LISTENING` line whose last column is the PID; in PowerShell, `Get-NetTCPConnection -LocalPort 8080 | Select-Object OwningProcess` shows the PID, and `Get-Process -Id <pid>` names the program. Press <kbd>Ctrl</kbd>+<kbd>C</kbd> in the window running the server to stop it, and rerun the command: it prints nothing.

### Exercise 2.6 ★★★ Read the environment like the app does

With `DB_PORT=3307` in `.env`, the placeholder `${DB_PORT:3306}` resolves to 3307, so the URL becomes `jdbc:mysql://localhost:3307/securedocs?connectionTimeZone=UTC&forceConnectionTimeZoneToSession=true` (assuming the other variables are unset or default). If you also `export DB_PORT=3308`, the app connects to port 3308: the comment in `application.yml` says real environment variables win over the `.env` file. The reason is that the environment is the more specific, more deliberate source: whoever started this process chose it, whereas the file holds a machine's everyday defaults.

## Chapter 3 solutions


### Exercise 3.1 ★ Hello, tiles

```java
public class Tiles {
    public static void main(String[] args) {
        int tileSize = 512;
        int cols = (1275 + tileSize - 1) / tileSize;
        int rows = (1650 + tileSize - 1) / tileSize;
        System.out.println(cols + " columns, " + rows + " rows");
    }
}
```

Output: `3 columns, 4 rows`.

### Exercise 3.2 ★ Break it on purpose

A missing semicolon gives a compiler error (`';' expected`) naming the line. Renaming `main` to `mian` compiles, but `java` fails at launch with an error that the class has no `main` method, because the JVM looks for `main` by name.

### Exercise 3.3 ★★ Print the grid

Put a `for (int col = 0; col < cols; col++)` loop inside a `for (int row = 0; row < rows; row++)` loop and print `row + "," + col`. Expect 3 x 4 = 12 lines.

### Exercise 3.4 ★ Characters or bytes

For `"café"` the expected numbers are 4 characters and 5 bytes, because `é` takes two bytes in UTF-8. Every extra accented letter adds one byte. If your prediction was wrong, recount which letters are outside plain English.

### Exercise 3.5 ★★ Write a rule

```java
public class Titles {
    static boolean validTitle(String title) {
        return title != null && !title.isBlank() && title.length() <= 200;
    }

    public static void main(String[] args) {
        System.out.println(validTitle(""));                    // false
        System.out.println(validTitle("   "));                 // false
        System.out.println(validTitle("Quarterly report"));    // true
        System.out.println(validTitle("x".repeat(201)));       // false
    }
}
```

`"x".repeat(201)` builds a 201-character string. The `null` check comes first so that `isBlank()` is never called on `null`; `&&` stops at the first false part.

### Exercise 3.6 ★★★ Test your own method

```java
public class Check {
    static int tilesNeeded(int lengthPx, int tileSize) {
        if (lengthPx <= 0 || tileSize <= 0) {
            throw new IllegalArgumentException("lengthPx and tileSize must both be positive");
        }
        return (lengthPx + tileSize - 1) / tileSize;
    }

    static void expect(int expected, int actual) {
        System.out.println(expected == actual ? "PASS" : "FAIL");
    }

    public static void main(String[] args) {
        expect(1, tilesNeeded(1, 256));
        expect(1, tilesNeeded(256, 256));
        expect(2, tilesNeeded(257, 256));
        try {
            tilesNeeded(0, 256);
            System.out.println("FAIL");
        } catch (IllegalArgumentException e) {
            System.out.println("PASS");
        }
    }
}
```

It prints four `PASS` lines. Choosing the boundaries (1, 256, 257) is the point: rounding errors live at the edges. Chapter 18 shows the same checks written with the real testing library.

## Chapter 4 solutions


### Exercise 4.1 ★ Class or record?

A tile's row and column: a record, because it is plain data that never changes. An account whose password can change: a class, because it has changing state and hides its fields. A search result: a record, because it is a fixed bundle of values.

### Exercise 4.2 ★ Write a record

```java
public class TileRefDemo {
    record TileRef(int row, int col) { }

    public static void main(String[] args) {
        System.out.println(new TileRef(2, 1));
    }
}
```

`toString` prints `TileRef[row=2, col=1]`.

### Exercise 4.3 ★★ Add an enum

One good answer: `enum TileStatus { QUEUED, READY, FAILED }`, then `TileStatus s = TileStatus.READY; if (s == TileStatus.READY) { ... }`. Enum values are compared with `==` because each value exists once.

### Exercise 4.4 ★★ A class that keeps a rule

```java
public class Counter {
    private int count;
    private final int max;

    public Counter(int max) {
        this.max = max;
    }

    public void increment() {
        if (count < max) {
            count++;
        }
    }

    public int getCount() {
        return count;
    }

    public static void main(String[] args) {
        Counter c = new Counter(2);
        c.increment();
        c.increment();
        c.increment();
        System.out.println(c.getCount());   // 2
    }
}
```

Because `count` is `private` and only `increment` changes it, no caller can push it past `max`. If `count` were a public field, `c.count = 99` would break the rule, just as a public `updatedAt` would break `Document`'s.

### Exercise 4.5 ★★ Swap a part

```java
public class Swap {
    interface TileStore { byte[] load(String name); }
    static class DiskTileStore implements TileStore {
        public byte[] load(String name) { return new byte[0]; }
    }
    static class FakeTileStore implements TileStore {
        public byte[] load(String name) { return new byte[] {1, 2, 3}; }
    }

    static void describe(TileStore store) {
        System.out.println(store.load("tile-0_0.png").length);
    }

    public static void main(String[] args) {
        describe(new DiskTileStore());   // 0
        describe(new FakeTileStore());   // 3
    }
}
```

Nothing in `describe` has to change for a third store: it depends only on the `TileStore` interface, so any new class that `implements TileStore` works.

### Exercise 4.6 ★★★ Model a request

One good answer follows the project's own design: `enum EventType { SIGN_IN, PAGE_VIEWED, ACCESS_DENIED }`; `record Actor(String username, String clientIp)` with `static Actor anonymous(String ip)`; `record Subject(String documentId, Integer page)` with `static Subject none()`; and `record Event(EventType type, Actor actor, Subject subject)`. Records fit because each is plain data that must not change after being recorded, which is exactly what an audit trail promises; the enum fits because the kinds of event are a fixed list the compiler can check. Fields that may be `null`: the actor's `username` (an anonymous request has none) and every field of `Subject`, since a sign-in is about no document. Use `Integer` rather than `int` for `page`, so that "no page" can be `null`, as `AuditEvent` does.

## Chapter 5 solutions


### Exercise 5.1 ★ Pick the collection

Pages in order: a list. Usernames allowed to open a document: a set (no duplicates). Each username with its role: a map from username to role.

### Exercise 5.2 ★ Stream it

```java
List.of(1275, 1650, 512, 300).stream()
        .filter(n -> n > 512)
        .map(n -> (n + 512 - 1) / 512)
        .forEach(System.out::println);
```

It prints 3 and 4.

### Exercise 5.3 ★★ Throw and catch

Throw with `throw new IllegalArgumentException("...")` inside `if (n <= 0)`, and wrap the call in `try { ... } catch (IllegalArgumentException e) { System.out.println(e.getMessage()); }`. For the custom type, write `class PageOutOfRange extends RuntimeException { PageOutOfRange(String m) { super(m); } }` and catch `PageOutOfRange` instead.

### Exercise 5.4 ★★ Look up a user

```java
import java.util.Map;
import java.util.Optional;

public class Lookup {
    static Optional<String> findRole(Map<String, String> roles, String username) {
        return Optional.ofNullable(roles.get(username));
    }

    public static void main(String[] args) {
        Map<String, String> roles = Map.of("pub.one", "PUBLISHER");
        System.out.println(findRole(roles, "pub.one").orElse("none"));
        try {
            findRole(roles, "nobody")
                    .orElseThrow(() -> new IllegalStateException("No such user: nobody"));
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        }
    }
}
```

It prints `PUBLISHER`, then `No such user: nobody`.

### Exercise 5.5 ★★★ Build a small throttle

One good answer:

```java
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class MiniThrottle {
    private final Map<String, Deque<Instant>> calls = new HashMap<>();

    boolean allow(String user, Instant now) {
        Deque<Instant> q = calls.computeIfAbsent(user, k -> new ArrayDeque<>());
        Instant cutoff = now.minus(Duration.ofSeconds(60));
        while (!q.isEmpty() && q.peekFirst().isBefore(cutoff)) {
            q.pollFirst();
        }
        if (q.size() >= 3) {
            return false;
        }
        q.addLast(now);
        return true;
    }
}
```

Test with four calls in a row (the fourth returns `false`), then a call 61 seconds later (returns `true`, because the earlier ones have aged out). With many threads, the whole body of `allow` must run as one step: the `computeIfAbsent` and the prune/size-check/`addLast` sequence would need a `ConcurrentHashMap` and a `synchronized (q)` block around the last three steps, or the check and the update could interleave, which is the same gap as in the nine-simultaneous-passwords incident.

## Chapter 6 solutions


### Exercise 6.1 ★ Read the coordinates

`groupId` `com.example`, `artifactId` `secure-doc-viewer`, version `0.1.0`. It compiles for Java 25 (`<java.version>25</java.version>`).

### Exercise 6.2 ★ Build it

The JAR is `target/secure-doc-viewer.jar`, named by `<finalName>` in the build section of `pom.xml`. `target/classes` holds the compiled `.class` files, in folders that mirror the packages (`com/example/securedocviewer/...`), plus copies of the files from `src/main/resources` such as `application.yml`.

### Exercise 6.3 ★★ Why pin Tomcat?

The project overrides the web server (Tomcat) version to 11.0.26 because the version Spring Boot 4.1.1 manages (11.0.24) had three critical security advisories. The override should be removed once Spring Boot manages Tomcat 11.0.25 or later, so the parent's tested versions apply again.

### Exercise 6.4 ★★ Follow the tree

Your exact output depends on the resolved versions, so the names below are examples of what to look for: PDFBox brings in its own supporting libraries (for instance a logging library and font or image helpers), which appear indented beneath `org.apache.pdfbox:pdfbox`. You did not list them because they are transitive dependencies: PDFBox's own POM declares them, and Maven downloads everything a dependency needs.

### Exercise 6.5 ★★★ Compare two milestones

One good answer, from `git show book-m2-documents:pom.xml` compared with `book-m0-mvp`: `spring-boot-starter-security` (accounts, from milestone 1), `spring-boot-starter-validation` (input checks), `spring-boot-starter-data-jpa` (storing objects in the database), `flyway-core` and `flyway-mysql` (schema migrations), `mysql-connector-j` (the MySQL driver), `spring-security-test` and `h2` (test tools). Scopes other than the default: `mysql-connector-j` is `runtime`; `spring-boot-starter-test`, `spring-security-test` and `h2` are `test`. The guess for each feature is the point of the exercise; check it against Chapters 14 to 16.

## Chapter 7 solutions


### Exercise 7.1 ★ Read the first commit

The oldest commit is `b6aef4e`, "Add secure document viewer: tiled rendering with signed URLs and per-viewer watermarking". It changed 31 files, all additions; most are under `src/`, chiefly `src/main/java/com/example/securedocviewer/`.

### Exercise 7.2 ★ Time travel without moving

`git show book-m0-mvp:pom.xml` and `git show book-m6-final:pom.xml`. At `book-m0-mvp` the property is `<java.version>21</java.version>`; at `book-m6-final` it is `25`. The Spring Boot parent also changes from 3.3.4 to 4.1.1.

### Exercise 7.3 ★★ Your own branch

`git switch -c exercise-7-3 book-m2-documents`, then `echo "hello" > notes.txt`, `git add notes.txt`, `git commit -m "Add notes"`. `git switch main` removes the file from the folder; `git switch exercise-7-3` brings it back, because the commit lives on that branch.

### Exercise 7.4 ★ Two commits in a scratch repository

```bash
mkdir scratch && cd scratch
git init
echo "one" > a.txt
git add a.txt
git commit -m "Add a.txt"
echo "two" >> a.txt
git diff
git add a.txt
git commit -m "Append a line"
git log --oneline
```

`git diff` shows `+two`; `git log --oneline` lists two commits.

### Exercise 7.5 ★★ Find when a file appeared

`git log --diff-filter=A --oneline -- Dockerfile` gives `2d10e07`, "Phase 5: Spring Boot 4 / Java 25, Docker stack, CI, and e2e tests", which is part of milestone 5 (`book-m5-platform`). `git log --diff-filter=A --oneline -- src/main/resources/db/migration/V2__documents_shares_audit.sql` gives `ba00693`, "Phase 2: document ownership, sharing, persistence, and audit", part of milestone 2 (`book-m2-documents`).

### Exercise 7.6 ★★★ Cause and resolve a conflict

One good answer: create `notes.txt` with a line, commit it on `main`, branch `a` and `b` from there, change that line differently on each, and commit. Merge `a` (fast-forward), then `git merge b`, which stops with a conflict. The file then contains `<<<<<<< HEAD` (start of the version from the branch you are on), `=======` (the divider) and `>>>>>>> b` (end of the incoming version from the other branch). Keep the correct text, delete all three marker lines, `git add notes.txt`, and `git commit`.

## Chapter 8 solutions


### Exercise 8.1 ★ Match the code

A document you may not see: `404`. A 60 MB upload: `413 Content Too Large` (limit 50 MB). Too many tile requests: `429 Too Many Requests` with a `Retry-After` header.

### Exercise 8.2 ★ Read a URL

Scheme `https`, host `docs.example.com`, path `/api/tiles`, query string `token=abc`.

### Exercise 8.3 ★★ Watch the cookie

The cookie `SDV_SESSION` shows `HttpOnly` and `SameSite=Strict`. `Secure` is set only if `SESSION_COOKIE_SECURE=true`, so on plain local HTTP it is off, matching the default in Listing 8.3.

### Exercise 8.4 ★ Write a request by hand

```text
GET /api/documents HTTP/1.1
Host: localhost:8080
Accept: application/json
```

With no session cookie the app answers `401` (with a JSON body containing an `error` field), because the request is not authenticated.

### Exercise 8.5 ★★ Two cookies

You should see `SDV_SESSION` (marked HttpOnly, so JavaScript cannot read it) and a CSRF cookie (`XSRF-TOKEN`, not HttpOnly, so the frontend can read it). The design needs both because they do different jobs: the session cookie proves who you are and must stay hidden so a script cannot steal it; the CSRF token proves that a change request came from the app's own page, which is why the page has to read it and send it back in a header. The CSRF token is useless without the session cookie, so being readable does no harm.

### Exercise 8.6 ★★★ Explain the header

One good answer, for `Cache-Control: no-store` on tiles: it stops browsers and shared caches from keeping a tile. Without it, a cache that many people share could store a tile watermarked for one viewer and serve it to another, which would both leak content and put the wrong name on the watermark. The cost is that tiles are re-fetched instead of reused, so each view takes a little more time and server work. For `Referrer-Policy: no-referrer`: it stops the browser from telling the next site which page the visitor came from; without it, a link followed from a page whose URL contains a signed token could leak the token; the cost is that other sites lose the referral information they might otherwise use for analytics.

## Chapter 9 solutions


### Exercise 9.1 ★ Read a table

The column is `owner_id`, type `BIGINT`, declared `NOT NULL` and a foreign key to `app_user (id)`. It can't be empty because every document must have an owner: the owner and admins manage it, and the access rules depend on it.

### Exercise 9.2 ★ Write a query

```sql
SELECT title FROM document WHERE visibility = 'PRIVATE' ORDER BY created_at DESC;
```

### Exercise 9.3 ★★ Write a migration

```sql
ALTER TABLE document ADD COLUMN description VARCHAR(500) NULL;
```

Existing rows have no description, so the new column must allow `NULL` (or have a default); otherwise the migration would fail on a database that already contains documents. Never edit `V2`; add `V4`.

### Exercise 9.4 ★★ Count and group

```sql
SELECT role, COUNT(*) AS accounts FROM app_user GROUP BY role;

SELECT enabled, COUNT(*) AS accounts FROM app_user GROUP BY enabled;
```

The first returns one row per role with its count; the second returns up to two rows, one for `enabled` true (shown as 1) and one for false (0).

### Exercise 9.5 ★★ Join three tables

```sql
SELECT d.title, u.username AS shared_with
FROM document d
JOIN document_share s ON s.document_id = d.id
JOIN app_user u ON u.id = s.user_id
ORDER BY d.title, u.username;
```

To include documents shared with nobody, use `LEFT JOIN` for both joins (`FROM document d LEFT JOIN document_share s ... LEFT JOIN app_user u ...`); those documents then appear with `NULL` in `shared_with`.

### Exercise 9.6 ★★★ Design a table

One good answer:

```sql
CREATE TABLE document_comment (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    document_id VARCHAR(36)   NOT NULL,
    author_id   BIGINT        NOT NULL,
    body        VARCHAR(2000) NOT NULL,
    created_at  DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_comment_document FOREIGN KEY (document_id) REFERENCES document (id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_author FOREIGN KEY (author_id) REFERENCES app_user (id)
);

CREATE INDEX ix_comment_document_time ON document_comment (document_id, created_at);
```

Key decisions: comments on a deleted document have no meaning, so `ON DELETE CASCADE` on `document_id` (as `document_page` and `document_share` do). Users are disabled, never deleted, so the author foreign key needs no cascade, and the default refusal to delete a user with comments is consistent with that policy. If comments had to survive as evidence, the audit table's approach applies instead: copy the author's username and the document title into the row and drop the foreign keys (denormalize), at the cost of copies that can go stale. The index serves "comments of this document in time order". The type sizes and the 2,000-character limit are choices you should be able to defend.

## Chapter 10 solutions


### Exercise 10.1 ★ Image or container?

`mysql:8.4` is an image. `securedocs-mysql` is a container (its `container_name`). `mysql-data` is a volume.

### Exercise 10.2 ★ Start the database

`docker compose ps` shows the `mysql` service as `healthy` after the health check passes. The data is in the named volume `mysql-data`, which `docker compose down` doesn't remove, so a new container started from the same file reattaches it. (`down -v` would delete it.)

### Exercise 10.3 ★★ Read the ports

`"3306:3306"` would publish the database on every network interface of your computer, so other machines on the network could try to connect. The `127.0.0.1` prefix limits it to your own computer, in line with the file's comment: "the database is never exposed to the network."

### Exercise 10.4 ★★ Compare two health checks

Differences: (1) `test` uses `CMD` with a list of arguments in Listing 10.1 but `CMD-SHELL` with one string in Listing 10.2, because the final version needs a shell to set an environment variable for the command; (2) Listing 10.1 passes the password as an argument, `-p${DB_ROOT_PASSWORD}`, while Listing 10.2 sets `MYSQL_PWD` and refers to `$$MYSQL_ROOT_PASSWORD`, expanded inside the container; (3) Listing 10.2 adds `--silent`. The final version is safer because the password no longer appears in the command that `docker inspect` stores, or in the process's argument list, where other users of the machine could read it.

### Exercise 10.5 ★★ Trace a dependency

The lines are `depends_on:`, `mysql:` and `condition: service_healthy` under the `app` service. Without them, on a slow computer Compose would start the backend at the same time as MySQL; the backend would try to connect before MySQL accepts connections, fail at startup (its database migrations need the connection), and, with `restart: unless-stopped`, keep restarting until MySQL happened to be ready.

### Exercise 10.6 ★★★ Reorder the Dockerfile

Before the move: changing one Java file changes the layer created by `COPY src src`, so only that layer and those after it (the `package` build) run again; the layer that downloaded dependencies is reused from the cache, because `pom.xml` did not change. After the move: `COPY src src` now comes before the download step, so a code change invalidates the cache from that point on, and the dependency download runs again every time. The original order is faster: downloads dominate the build time, and they should repeat only when `pom.xml` changes.

## Chapter 11 solutions

# Solutions: Chapter 11

### Exercise 11.1 ★ Find the cookie settings

The cookie name is `SDV_SESSION`, under `server.servlet.session.cookie.name`. The line `http-only: true` makes it unreadable to JavaScript. The placeholder `${SESSION_COOKIE_SECURE:false}` on the `secure` line controls whether it is sent only over HTTPS: the environment variable `SESSION_COOKIE_SECURE` if set, otherwise `false`.

### Exercise 11.2 ★ List the dependencies

`DocumentController` receives `DocumentService documents` and `RequestActors actors`. `DocumentService` receives `DocumentRepository`, `AppUserRepository`, `TileGenerationService`, `AuditLogService` and `PlatformTransactionManager`. Going one level further, `RequestActors` needs `SessionKeys`, which needs `ViewerProperties`, and `TileGenerationService` needs `ViewerProperties` and `ViewerMetrics`. A diagram has `DocumentController` at the top with arrows down to those two, and so on, as in Figure 11.1. Notice that `ViewerProperties` is at the bottom of several branches: one shared bean serves them all.

### Exercise 11.3 ★★ Find the `@Value` settings

`BootstrapAdmin`'s constructor has `@Value("${secure-doc-viewer.bootstrap-admin.username:admin}")`, so the username defaults to `admin`, and `@Value("${secure-doc-viewer.bootstrap-admin.password:}")`, so the password defaults to empty, which the class treats as "generate a random one". In `application.yml`, the block under `secure-doc-viewer.bootstrap-admin` sets them from the environment variables `BOOTSTRAP_ADMIN_USERNAME` (default `admin`) and `BOOTSTRAP_ADMIN_PASSWORD` (default empty).

### Exercise 11.4 ★★ Who wins?

The port is 9090. Spring Boot's relaxed binding maps the environment variable `SERVER_PORT` onto the property `server.port` (upper case and underscores are accepted for dotted, dashed names), and an environment variable overrides the value in `application.yml`. To check on your own copy, start the program with the variable set and read the port the framework reports in its startup output, or request `http://localhost:9090/actuator/health`. Remember to clear the variable afterward, or a later "I changed the YAML and nothing happened" will be this exercise coming back.

### Exercise 11.5 ★★★ Explain the `@Autowired` in `KnownDevices`

A worked outline. Spring needs to know which constructor to call. When a class has exactly one constructor, Spring uses it. `KnownDevices` has two: a public one taking a `JdbcTemplate` and `ViewerProperties` (which fills in the real system clock) and a second one, visible only in its own package, that also takes a `Clock`. The tests use the second one to pass a fixed clock and move time by hand (Chapter 18). If you removed `@Autowired` from the public constructor, Spring would find two constructors and no marker, and, unless there is a no-argument constructor to fall back on, startup would fail with an error saying it could not find a default constructor. The second constructor exists for testability: it lets the class depend on a `Clock` supplied from outside without complicating the production wiring.

### Exercise 11.6 ★★★ Break the startup on purpose

A worked outline, since the exact text depends on your versions.

- **Unset the signing secret.** Failure at step 3 of Section 11.2 (creating objects): binding `ViewerProperties` fails validation, and the error mentions `SIGNING_SECRET must be set`. This is the easiest to act on, because the message was written by the project and names the variable.
- **`server.port` set to text.** Failure in step 1 or when the web server is created: Spring can't convert the value to a number, and the error names the property and the value. Also easy: the property name is in the message.
- **Delete `@Service` from a class.** Failure at step 3: `DocumentController` (or another class) asks for a bean of that type and none exists, so startup stops with an error that names the type and the class that needed it. This one is easy to act on too, if you read the "Caused by" chain from the bottom.

What makes a message easy to act on: it names the thing that is wrong (the property, the type) and, where the project wrote it, says what to do. Restore each change before the next experiment.

## Chapter 12 solutions

# Solutions: Chapter 12

### Exercise 12.1 ★ Match a request to a method

`@GetMapping("/{documentId}")` on the method, combined with the class-level `@RequestMapping("/api/documents")`, gives the full path `/api/documents/{documentId}` for `GET`. Spring fills the `documentId` parameter from the matching part of the URL because of `@PathVariable`.

### Exercise 12.2 ★ Why `ResponseEntity`?

In `DocumentController`, `delete` returns `ResponseEntity<Void>` built with `ResponseEntity.noContent().build()`: the answer is status `204` with *no body*, which a plain return value can't express (returning `void` would give `200`). The other methods return plain objects (`DocumentDetail`, `List<DocumentSummary>`, `List<String>`) because `200 OK` with a JSON body is exactly right, so they don't need `ResponseEntity`. Elsewhere, `TileController.getTile` and `PageTileUrlController.tileUrls` return `ResponseEntity` because they set headers (`Cache-Control: no-store`) or want explicit control of the response.

### Exercise 12.3 ★★ Read the JSON of your own copy

Every key of an element in the array matches a component of `DocumentSummary`: `documentId`, `title`, `pageCount`, `owner`, `visibility`, `createdAtEpochSeconds`, `updatedAtEpochSeconds`, `canManage` and `sharedWithCount`. For a reader (or anyone who can't manage the document), `canManage` is `false` and `sharedWithCount` is `null`, because the count is only filled in for users who can manage the document. That way a reader can't learn how widely a document is shared.

### Exercise 12.4 ★★ Rename a component

On the server: the record `DocumentSummary` (the component name), the code that builds it (`DocumentService.summary`, which passes `d.getPageCount()` to the constructor by position, so the compiler doesn't force a change there), and any test that reads the field, for example a `jsonPath("$[0].pageCount")` assertion. On the client: the TypeScript interface that models a document summary in the Angular app, and every template or component that reads `pageCount`. The JSON key is a contract: the server and the browser app are separate programs, built separately, and the only thing that connects them is the name in the JSON. A rename that compiles on the server can silently break the client, where the missing key appears as `undefined`. That's why such changes are made deliberately, in both places, and covered by an end-to-end test (Chapter 24).

### Exercise 12.5 ★★★ Design an endpoint

One good answer.

- **HTTP:** `GET /api/documents?limit=10&sort=updated`, or a dedicated `GET /api/documents/recent`. Prefer extending the existing collection with a query parameter when the result is the same kind of thing (a list of `DocumentSummary`), so clients reuse the same model. Validate `limit` with `@Min(1) @Max(50)`, as `AdminController.audit` does for its paging, so a caller can't ask for a million rows.
- **Response:** `List<DocumentSummary>`, newest first, using the same `updatedAtEpochSeconds` field the summary already has.
- **Status codes:** `200` on success, `400` for a bad `limit` (through the shared handler), `401` when not signed in.
- **Where the rule lives:** the controller method only reads the parameter and calls `DocumentService`. Which documents a user may see is already decided in `DocumentService.list` (using `DocumentRepository.findVisibleTo` or `findAllWithOwner`), so the new service method must reuse it and then sort and limit; it must not build its own visibility rule.
- **Tests:** a unit test of the sort-and-limit logic; a `MockMvc` test that a reader sees only documents visible to them, that `limit=0` returns `400`, and that the anonymous call returns `401` (Chapter 18).

### Exercise 12.6 ★★★ Follow the tile chain

The requests you should see for a page, in order: the document detail (`GET /api/documents/{id}`), the tile-URL grid for the page (`GET /api/documents/{id}/pages/{n}/tile-urls`), then one `GET /api/tiles?token=...` per tile, several in parallel. Decoding the part of the token before the dot with a base64url decoder gives a pipe-separated string with seven fields: the document id, page, row, column, tile version, a session binding and an expiry time in epoch seconds (the canonical string of `SignedTilePayload`). You would not want the *session binding* logged if it were the session id, but it is not the session id: it is a keyed value derived from it, so the design makes the token safe to leak in a log or a shared screenshot for its short lifetime. What a leaked token still allows is fetching that one tile, from the session it was issued to, until it expires; the signature (the part after the dot) stops anyone changing any of the fields.

## Chapter 13 solutions

# Solutions: Chapter 13

### Exercise 13.1 ★ Which rule stops a long username?

`@Size(max = 64)` on the `username` component of `LoginRequest`, checked because the controller parameter is marked `@Valid`. Spring throws `MethodArgumentNotValidException` before `login` runs, and `GlobalExceptionHandler.handleInvalidBody` answers `400` with `{"error": "username: size must be between 0 and 64"}` (the standard message of `@Size` states its bounds; the exact wording comes from Hibernate Validator). The same rule stops a 10 MB username: it is refused before any sign-in code runs.

### Exercise 13.2 ★ The reference code

`{"error": "Something went wrong on our side. Reference: <8 characters>."}` with status `500`. The sentence and the reference are safe to show. The exception, with its message and stack trace, is written to the server log by `log.error("Unhandled error, reference {}", reference, e)`, next to the same reference, so an operator can find it from what a user reports.

### Exercise 13.3 ★★ Add an exception and a mapping

```java
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
```

In `GlobalExceptionHandler`:

```java
@ExceptionHandler(ConflictException.class)
public ResponseEntity<Map<String, String>> handleConflict(ConflictException e) {
    return error(HttpStatus.CONFLICT, e.getMessage());
}
```

For the test, copy the shape of `ErrorContractTest`: a `@TestConfiguration @RestController` with a `@GetMapping("/api/test/conflict")` method that throws `new ConflictException("That title is taken.")`, `@Import` it into a `@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")` class, sign in (the path is under `/api/`, so it needs authentication), and assert `status().isConflict()` and `jsonPath("$.error").value("That title is taken.")`. The message text comes from whoever throws the exception, which is why exception messages that reach the client must be written for users.

### Exercise 13.4 ★★ Start without the secret

Unset `SIGNING_SECRET` in your shell, and remove or rename the `.env` file if it defines it (the project imports `.env` with `spring.config.import`, so a value there would satisfy the setting). The application refuses to start, and the error output mentions the property and your message `SIGNING_SECRET must be set`; the annotation is `@NotBlank(message = "SIGNING_SECRET must be set")` on the `signingSecret` field. With a 10-character value, the `@Size(min = 32, ...)` rule fires instead and the message is `SIGNING_SECRET must be at least 32 characters`. Restore your real value afterward.

### Exercise 13.5 ★★★ Design the limits

One good answer, in order: (1) role or sign-in required, `401` or `403`, before the body is read, protecting time and bandwidth; (2) maximum request size 2 MB (`413`), protecting memory and disk; (3) stream to a temporary file instead of holding it in memory; (4) check the file's leading bytes against the expected image format (PNG starts with a fixed 8-byte signature, JPEG with `FF D8`), `400`, because the client-supplied name and content type can't be trusted; (5) decode with a maximum pixel area (width times height), `400`, protecting memory against a small file that expands hugely; (6) re-encode into a standard format and discard the original, so anything hidden in the file is dropped; (7) limit the rate per user, `429`. The PDF-specific limits with no equivalent are the page count and the render-slot and timeout layers, because decoding one small image is cheap and bounded; the size, signature and pixel-area checks are still needed.

### Exercise 13.6 ★★★ Why three places?

The three are the server's multipart limit (what actually stops the upload), the number in the error message (what the user is told) and the frontend's pre-check (what avoids a wasted upload). If the frontend allows 100 MB but the server stops at 50, the user selects a 70 MB file, waits for a long upload, and is then refused with a `413` at the end. If the message says 50 but the setting is different, the user is told something false. To keep them from drifting: make one the source of truth, for example expose the limit through an endpoint or a build-time setting the frontend reads instead of copying it, or at minimum add a test that reads the server setting and asserts the constant and the frontend value match. The project's answer is a comment in each place saying to keep them in step, which is the weakest of the three, but cheap.

## Chapter 14 solutions

# Solutions: Chapter 14

### Exercise 14.1 ★ Why store the enum as text?

`@Enumerated(EnumType.STRING)` on the `role` field of `AppUser`. It stores the enum constant's *name* (`READER`, `PUBLISHER`, `ADMIN`). The default, `EnumType.ORDINAL`, stores the constant's *position*: 0, 1, 2. If you inserted a new role between `READER` and `PUBLISHER`, every stored `1` (formerly `PUBLISHER`) would now mean the new role, and every stored `2` would mean `PUBLISHER`, so existing accounts would silently change role, with no error. Text is also readable when you look at the table.

### Exercise 14.2 ★ Find the audit indexes

`V2__documents_shares_audit.sql` creates `audit_event` and four indexes: `ix_audit_event_time` on `occurred_at` (newest-first listing and date ranges), `ix_audit_event_user_time` on `(username, occurred_at)` (filter by user), `ix_audit_event_document_time` on `(document_id, occurred_at)` (filter by document) and `ix_audit_event_type_time` on `(event_type, occurred_at)` (filter by event type). Each pairs a filter column with the time, so a filtered list that is sorted by time can be read straight from the index.

### Exercise 14.3 ★★ Read a query method

`existsByUsername(String username)` becomes roughly `select ... from app_user where username = ? limit 1`, and the result is `true` if any row comes back. `findAllByOrderByUsernameAsc()` becomes `select ... from app_user order by username asc`, with no `where` because there is nothing between `By` and `OrderBy`. Another example: `countByOwner_Username` in `DocumentRepository` is `count` (the operation), `By` (conditions follow), `Owner_Username` (the `username` field of the `owner` relationship), that is `select count(*) from document d join app_user u on d.owner_id = u.id where u.username = ?`.

### Exercise 14.4 ★★ Annotation or template?

`UserAccountService` does quick database work in every method, so wrapping each whole method in a transaction with `@Transactional` is simple and correct. `DocumentService.upload` and `replaceFile` do a slow PDF render and file moves between the database steps. With `@Transactional` on the whole method, the transaction, and the database connection it holds, would stay open for the entire render, so a few simultaneous uploads could exhaust the connection pool. `TransactionTemplate` lets the service choose the transactional part precisely: render outside, then `tx.execute(...)` around the short database work. If you swapped them, the account service would work but with more code, and the document service would hold connections during rendering.

### Exercise 14.5 ★★★ Add a migration on a scratch copy

A worked outline. `V4__document_notes.sql` containing `ALTER TABLE document ADD COLUMN notes VARCHAR(500) NULL;`. On the first start, Flyway compares the migrations on disk with its history table, sees `V4` is new, applies it and records it with a checksum. After you edit `V4` and start again, Flyway recomputes the file's checksum, finds it differs from the recorded one, and refuses to start with a message that mentions a checksum mismatch for the applied migration. It is protecting every database that already ran the old version, which would otherwise disagree with this one. To make the change you wanted, restore `V4` to its original text (or, on a scratch database only, drop the database and start over), and put the new change in `V5`. On a real database you never repair by editing history.

### Exercise 14.6 ★★★ Predict the race

Without the lock, both requests render, then both read `tile_version = 1`, both compute the next version as 2, and both try to move their tiles into `v2`: they collide or mix files, and one update overwrites the other, so the document could end up pointing at tiles that are a mixture. With the row lock, `findByIdForUpdate` makes the second transaction wait at `select ... for update` until the first commits. The first sees version 1 and produces `v2`; the second then reads version 2 and produces `v3`. On disk, after the first replace removes `v1` and the second removes `v2`, only `v3` remains, and the row's `tile_version` is 3. `MySqlIntegrationTest.concurrentReplacementsAreSerialisedByTheRowLock` asserts exactly this. It needs a real MySQL because the test is about the database's own row-lock behavior, which the in-memory H2 imitation is not a trustworthy stand-in for (the class comment names "the row lock that serialises PDF replacement" among the things H2 can't vouch for).

## Chapter 15 solutions

# Solutions: Chapter 15

### Exercise 15.1 ★ Find the cookie settings

`http-only: true` under `server.servlet.session.cookie` in `application.yml` makes the session cookie unreadable to JavaScript. It is configuration, not an annotation. The idle timeout is `server.servlet.session.timeout`, set to `30m`: after 30 minutes without a request the session ends.

### Exercise 15.2 ★ What does the database store?

The column is `password_hash VARCHAR(100) NOT NULL` in `app_user`. It holds the delegating encoder's output: a label such as `{bcrypt}` (8 characters) followed by a BCrypt hash, which is 60 characters. That is 68 characters in total, so 100 leaves room for a longer label or a future algorithm's output. It never holds the password itself.

### Exercise 15.3 ★★ Count bytes, not characters

A minimal program, with the method copied from Listing 15.3 so that it needs nothing from the project:

```java
import java.nio.charset.StandardCharsets;

public class Bytes {
    static final int MAX_PASSWORD_BYTES = 72;

    static boolean fitsBcrypt(String password) {
        return password == null || password.getBytes(StandardCharsets.UTF_8).length <= MAX_PASSWORD_BYTES;
    }

    public static void main(String[] args) {
        System.out.println(fitsBcrypt("a".repeat(72)));
        System.out.println(fitsBcrypt("a".repeat(73)));
        System.out.println(fitsBcrypt("é".repeat(30)));
        System.out.println(fitsBcrypt("😀".repeat(20)));
    }
}
```

Save it as `Bytes.java`; you can run it with `java Bytes.java` (Chapter 3).

Results: `true`, `false`, `true`, `false`. Seventy-two letters are 72 bytes (one byte each in UTF-8) and fit; 73 are one byte over. Thirty copies of `é` are 60 bytes (two bytes each) and fit even though they look like fewer characters than they are bytes. Twenty emoji are 80 bytes (four bytes each) and don't fit, although 20 characters seems short. If you use a different emoji, check that it's a single four-byte character; some emoji are sequences of several code points and take more. The lesson is that the limit is in bytes.

### Exercise 15.4 ★★ Trace a failed sign-in

For an existing user with a wrong password: `login` normalizes the username; `knownDevices.isRecognised`; `loginThrottle.reserve` (counts the attempt); `UserAccountService.fitsBcrypt`; `authenticationManager.authenticate`, which calls `DatabaseUserDetailsService.loadUserByUsername`, then the encoder's `matches`, which returns false, so an `AuthenticationException` (a `BadCredentialsException`) is thrown; the `catch` block records a failure metric and a `SIGN_IN_FAILED` audit event, and throws a new `BadCredentialsException("Invalid username or password.")`. `GlobalExceptionHandler.handleBadCredentials` turns it into `401` with `{"error": "Invalid username or password."}`.

For a user that doesn't exist: the path is the same up to `loadUserByUsername`, which throws `UsernameNotFoundException`. Spring Security's provider treats this as an authentication failure (by default it hides the "not found" detail by reporting bad credentials), so the same `catch` block runs. The two paths meet at the `catch (AuthenticationException e)`, and everything after it, including the response, is identical.

### Exercise 15.5 ★★★ Sessions or tokens?

A worked outline.

- **Current design.** `UserAdminController.resetPassword` and `AuthController.changePassword` call `sessions.revokeAllFor(...)`, which expires every session of that user in the `SessionRegistry`. Spring Security then rejects each of those sessions on its very next request. Because the server owns the record, "immediately" is easy.
- **Token-only design.** A signed token stays valid until it expires and needs no server lookup, so nothing tells the server the token should now be refused. To add "sign out everywhere" you would need server-side state anyway: a list of revoked tokens, or a per-user "tokens issued before this time are invalid" value checked on every request. That is most of the server-side state a session already gives you.
- **When tokens win.** When many independent servers, or a third party, must verify identity without contacting a central store, or when the client isn't a browser and can't keep cookies. Switching would give up instant revocation, the `httpOnly` protection from scripts, and the simple admin session list. It would also make it necessary to protect the token in the browser, which scripts can read if it's kept in storage.

### Exercise 15.6 ★★★ Design a safe response

This is a design outline, not code from the project; the project has no email feature. One good answer: respond with the *same* status and message whether or not the address is registered, for example `202 Accepted` with "If that address has an account, we've sent a link." Do the work of sending an email in the background, after the response has been returned, so that the time to answer doesn't depend on whether an account exists (looking up and mailing take longer than not finding anyone). Rate-limit the endpoint per address and per client, as `LoginThrottle` does for sign-in, so it can't be used to send a flood of email or to probe addresses at speed. Key decisions: identical bodies and status codes, comparable timing, and a throttle that doesn't reveal by its own behavior which addresses are known.

## Chapter 16 solutions

# Solutions: Chapter 16

### Exercise 16.1 ★ What does `denyAll()` do?

`GET /api/does-not-exist` from a signed-in reader matches the rule `/api/**` with `authenticated()`. The reader is authenticated, so security lets the request through; no controller has that path, so Spring raises a "no resource" error, and `GlobalExceptionHandler` turns it into a `404` with `{"error": "Not found."}` (`ErrorContractTest` checks this). `GET /somewhere-else` matches no earlier rule, so it reaches `anyRequest().denyAll()`. For an anonymous caller that produces the `401` "Sign-in required." (the authentication entry point); for a signed-in caller it produces `403`. Either way the request never reaches application code.

### Exercise 16.2 ★ Two cookies, two rules

The app's JavaScript has to copy the CSRF token into the `X-XSRF-TOKEN` header, so it must be able to read the `XSRF-TOKEN` cookie. The session cookie is only ever sent back by the browser and is never needed by scripts, so it can be `httpOnly` and out of their reach, which protects the real credential if a script gets injected. The difference is safe because the CSRF token alone can't do anything: a forged request also needs the session cookie, which a foreign page can make the browser send but can't read, and it needs the header, which a foreign page can't write.

### Exercise 16.3 ★★ Read the headers on your own copy

Run, against your own copy:

```bash
curl -i http://localhost:8080/api/auth/me
```

You should find `Content-Security-Policy`, `Referrer-Policy: no-referrer`, `Permissions-Policy`, `X-Content-Type-Options: nosniff` and `X-Frame-Options: DENY`, matching the rows of Table 16.1 (`SecurityHeadersTest` asserts the same list). Which would you notice last is a judgment: most people would say `Permissions-Policy`, because the API never uses those browser features anyway, so nothing visibly breaks when it's missing; the headers whose absence has real consequences are the CSP and `Referrer-Policy`, because the signed tile URLs would leak through the latter.

### Exercise 16.4 ★★ 404 or 403?

- (a) `404`. `DocumentService` reports a document the caller can't view as not found, so its existence isn't revealed.
- (b) `403`. The reader can see the document, so hiding it would be pointless, but sharing needs ownership (as a publisher) or the admin role, and `requireManageable` throws `ForbiddenException`.
- (c) `403`. `SecurityConfig` requires the `ADMIN` role for `/api/admin/**`, and the access-denied handler writes the JSON.
- (d) `401`. Nobody is signed in, so the authentication entry point answers `Sign-in required.`

(a) and (b) are decided in the service; (c) and (d) are decided by the rules in `SecurityConfig`, before a controller runs.

### Exercise 16.5 ★★★ Break the throttle in a scratch copy

A worked outline. With check-then-act, all twelve threads pass `checkAllowed` at nearly the same instant, because no failure has been recorded yet, and each then spends about a tenth of a second in BCrypt before recording its failure. Far more than five passwords are actually verified, so the count of `401` responses is well above `MAX_FAILURES_PER_ACCOUNT` and the assertion `assertEquals(MAX_FAILURES_PER_ACCOUNT, guessed, ...)` fails. The exact number varies by run, which is itself the sign of a race. The original passes because `reserve` is `synchronized` and counts before the password is verified, so the sixth simultaneous caller finds the limit already reached and gets `429`. Put the code back and confirm that the test passes every time.

### Exercise 16.6 ★★★ Design a lockout that can't be abused

One good answer.

- **Count atomically.** Reserve an attempt before checking the PIN, as `LoginThrottle.reserve` does, and give it back on success. Do the check and the count under one lock or one atomic database update (for example, an `UPDATE ... SET attempts = attempts + 1 WHERE attempts < 3` and look at the number of rows changed).
- **Scope the count.** Count per account *and* per address, so a single address can't burn everyone's attempts and one account's attempts from one address can't be reset by other addresses.
- **Protect the owner.** Don't let unknown addresses lock the owner out of a device they've used successfully: keep a short list of recognised devices (stored as keyed hashes, not raw addresses), as `KnownDevices` does.
- **Recovery.** A locked-out legitimate owner needs a path that an attacker can't trigger for them: an administrator unlock, or a reset link sent to a verified channel, plus an audit event recording the lock and unlock. Accept and document the trade-off, as the project does for a new device during a lockout.
- **Store.** In memory is fine for a single instance and a short window (a restart clears it); use the database if attempts must survive restarts or be shared between instances.

## Chapter 17 solutions

# Solutions: Chapter 17

### Exercise 17.1 ★ Count the tiles

Width: 8.5 × 150 = 1,275 pixels. Height: 11 × 150 = 1,650 pixels. Columns: `tileCount(1275, 512)` is `(1275 + 511) / 512 = 1786 / 512 = 3` (integer division drops the remainder), so 3 columns, and the last column is 1,275 − 1,024 = 251 pixels wide. Rows: `tileCount(1650, 512)` is `(1650 + 511) / 512 = 2161 / 512 = 4`, so 4 rows, and the last row is 1,650 − 1,536 = 114 pixels tall. That is 12 tiles, matching the "~12 tiles" in the comment in `application.yml`.

### Exercise 17.2 ★ Why crop, not pad?

Because reassembling the tiles at `(col * tileSize, row * tileSize)` must reproduce the page exactly, and the page itself has no pixels beyond its right and bottom edges. A padded tile would carry a strip of white that doesn't belong to the page: the viewer would show a light band beyond the page's edge, most visible against a dark background; each tile would also claim a size that doesn't match `PageInfo`'s page width and height; and the padding would be extra pixels to store, send and watermark. Cropping keeps the data exactly as large as the page.

### Exercise 17.3 ★★ Tamper with a token

Changing a character of the signature (after the dot) makes the recomputed HMAC differ from the supplied one, so `verifyAndDecode` throws `InvalidTokenException("Signature mismatch ...")`, which `GlobalExceptionHandler` turns into `401` with that message in the JSON. Changing a character before the dot changes the payload, so the HMAC recomputed from the altered payload no longer matches the original signature, and the result is the same `401`. (If your change makes the payload invalid base64url, you get `Malformed token payload` instead, also `401`.) The signature covers the whole payload, so any edit to either half is detected; the only way to get a valid token is to ask the server to issue one.

### Exercise 17.4 ★★ Why `finally`?

`finally` runs whether the work returns normally or throws. If the permit were released only after a normal return, every exception thrown inside the work (a corrupt image, a failure while writing the PNG) would keep its permit forever. The permit count is fixed (twice the CPU count, by default), so after that many failures the semaphore would be empty and every tile request would wait 2 seconds and then get `503`. The server would look busy while doing nothing, and only a restart would fix it.

### Exercise 17.5 ★★★ Walk the leaked link

A worked outline. The independent checks that still refuse the friend's request:

- **Expiry:** after 120 seconds the token is refused (`Token expired ...`, `401`), whatever else is true.
- **A live signed-in session:** if the friend isn't signed in, Spring Security answers `401` before the controller runs.
- **Session binding:** the token contains a value derived from the *original* session's id. Even if the friend signs in as the *same user*, their session has a different id, so `tileBindingMatches` fails and `TileController` throws `InvalidTokenException("This tile link was issued to a different session.")`. This is the single check that refuses the link even when the friend is the same person on another device.
- **Fresh access check:** if the document has been unshared or deleted since, the access check answers `404`.

The rate limit doesn't refuse it on its own (one request is under the limit), and the watermark doesn't refuse anything; it makes a leaked tile traceable to the session that requested it.

### Exercise 17.6 ★★★ Design the failure paths

One good answer.

1. Validate the uploaded file and read it into a temporary file (no shared state changed).
2. Produce the thumbnail into a **staging** folder or temporary file (invisible to readers). If this fails, delete the staging file; nothing else has changed.
3. **Atomically rename** the staged file to its final name in the storage folder, with a unique name (for example including a version or random id) so it can't overwrite the current thumbnail. If the rename fails, delete the staging file.
4. Update the database column to point at the new file name in one short transaction. If it fails, delete the newly renamed file (nothing points at it).
5. After the transaction commits, delete the *old* thumbnail file. If that fails, log it and leave it for a janitor: the row no longer points at it, so it is unreachable garbage, not an error.

The key decisions: write in a place nobody looks and make it visible in one atomic step; change the database only after the file exists; delete the old file only after the database points at the new one; and give every failure path a cleanup, with a scheduled sweep as the last resort, as `StorageJanitor` does for tiles.

## Chapter 18 solutions

# Solutions: Chapter 18

### Exercise 18.1 ★ Run the tests and read the summary

`./mvnw test` runs every test class under `src/test/java`. Near the end of the output, Maven prints a line of the form `Tests run: N, Failures: 0, Errors: 0, Skipped: S`. If Docker isn't running, `MySqlIntegrationTest` is skipped: the class is annotated `@Testcontainers(disabledWithoutDocker = true)`, so JUnit reports its tests as skipped instead of failed. With Docker running, nothing is skipped and the class starts a `mysql:8.4` container.

### Exercise 18.2 ★ Add a boundary case

```java
@Test
void aPageExactlyOneTileWideNeedsOneTileAndOnePixelMoreNeedsTwo() {
    assertEquals(1, TileGrid.tileCount(512, 512));
    assertEquals(2, TileGrid.tileCount(513, 512));
}
```

Prediction: `(512 + 511) / 512` is `1023 / 512`, which is 1 with integer division; `(513 + 511) / 512` is `1024 / 512`, which is 2. Both values sit on either side of the point where the answer changes, which is why they are the right cases.

### Exercise 18.3 ★★ Write an unauthenticated test

The anonymous test:

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminAuditAccessTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void anonymousCallersAreToldToSignIn() throws Exception {
        mvc.perform(get("/api/admin/audit"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Sign-in required."));
    }
}
```

For the reader case, create a reader with `UserAccountService.create(...)`, sign in as in `SecurityIntegrationTest.login`, pass the session with `.session(reader)`, and expect `status().isForbidden()`. The `401` comes from the authentication entry point (nobody is signed in), and the `403` comes from the access-denied handler (someone is signed in but lacks the `ADMIN` role); both are written by `SecurityErrorResponses` in the same JSON shape, but the framework calls them at different points in the filter chain.

### Exercise 18.4 ★★ Break a rule and watch the test

**Experiment 1 (5 to 6).** Nothing fails. The tests in `LoginThrottleTest` and `SecurityIntegrationTest` loop `LoginThrottle.MAX_FAILURES_PER_ACCOUNT` times rather than writing the literal 5, so they follow the constant to its new value. The same is true of the burst test, which asserts that exactly `MAX_FAILURES_PER_ACCOUNT` guesses were checked. Your prediction may have been "some tests fail"; the surprise is the lesson. Those tests check that the throttle *enforces whatever limit is configured*, not that the limit is 5.

**Experiment 2 (ignore the account-and-address rule).** Tests such as `locksOneAddressOutOfAnAccountAfterFiveFailures` and `repeatedFailuresLockTheAccountWithRetryAfter` fail, because they expect a `LoginLockedException` (or a `429`) after the limit and no longer get one. Read each message: JUnit reports which assertion failed and what was expected.

What the two experiments show: the tests are tied to the *behavior* (locking happens at the limit), not to the number. That is usually what you want. The cost is that nothing in the tests pins the value 5 itself; if the product decision "five tries" matters, add one test that says so.

### Exercise 18.5 ★★★ Is the burst test strong enough?

A worked outline, since results depend on your machine.

1. **Without `start.await()`**, each task begins as soon as its thread is scheduled. On a quick machine the twelve requests may run almost one after another. Against the correct, atomic `reserve`, the test still passes, because the throttle is correct. That is fine, but it shows the test stopped forcing an overlap.
2. **With a non-atomic throttle** (check first, count afterward) **and the latch in place**, the twelve requests all pass the check before any is counted, so far more than 5 passwords are actually verified, and the assertion on the count of `401` responses fails. That is the test doing its job.
3. **With a non-atomic throttle and no latch**, the test may pass on some runs, because the overlap that exposes the bug is not guaranteed. That is a test that cannot be trusted.

A paragraph that earns full credit says: a concurrency test is trustworthy when (a) it forces the overlap rather than hoping for it, (b) it asserts exact outcomes that the protection guarantees, and (c) you have seen it fail against the bug it is meant to catch.

### Exercise 18.6 ★★★ Design a test for a promise

One good answer, for "a document unshared from a user stops serving new tiles to them":

- **Name:** `unsharingADocumentStopsNewTileRequestsFromThatUser`.
- **Level:** an application test with `MockMvc` (Section 18.6), because the promise spans the sharing endpoints, the signed-URL issuing endpoint, the tile controller and the access check; a unit test of one class could not prove it.
- **Arrange:** create a publisher, a reader and a PDF; the publisher uploads it and shares it with the reader; the reader signs in and requests a tile URL and one tile, which succeeds.
- **Act:** the publisher unshares the document (`DELETE /api/documents/{id}/shares/{username}`), then the reader requests the same tile again with the still-valid token.
- **Assert:** the response is `404` with the JSON error shape, not `200`. This is the "re-check on every tile" behavior described in the `TileController` class comment.

The key decisions: use the real filter chain so sessions and roles are real, use a token that has not expired so that only the access check can be the reason for refusal, and give each user a unique name, as in Listing 18.7.

## Chapter 19 solutions

# Chapter 19 solutions

### Exercise 19.1 ★ The nullable property

In `DocumentDetail`, `sharedWith: string[] | null` is `null` for anyone who can't manage the document (the comment: "Only present when canManage is true"). It is the only nullable property in that interface. (`DocumentSummary` has the matching `sharedWithCount: number | null`.)

### Exercise 19.2 ★ A Direction type

Example solution:

   ```typescript
   type Direction = 'next' | 'previous';

   function step(current: number, d: Direction): number {
     return d === 'next' ? current + 1 : current - 1;
   }
   ```

   `step(3, 'back')` is a compile error.

### Exercise 19.3 ★★ Extend IdleState

In `app.ts`, `checkIdle` stores the state and only tests for `'expired'`, and `app.html` tests for `'warning'`, so neither breaks by itself; the new alternative simply falls through as "not a warning". The compiler complains only where code exhaustively depends on the list, and there is none here. The lesson: unions catch wrong values, not missing handling, unless you write a `switch` with a `never` check. Verify by running `npx tsc --noEmit -p tsconfig.app.json` in a scratch copy.

### Exercise 19.4 ★★ A fetchOrNull helper

Example solution:

   ```typescript
   async function fetchOrNull(url: string, signal: AbortSignal): Promise<Response | null> {
     try {
       return await fetch(url, { signal, cache: 'no-store' });
     } catch {
       return null;
     }
   }
   ```

   An aborted request also gives `null`, so the caller must check `signal.aborted` to tell the two apart, as the viewer does.

### Exercise 19.5 ★★★ A generic firstWhere

Example solution:

   ```typescript
   function firstWhere<T>(items: T[], test: (item: T) => boolean): T | null {
     for (const item of items) {
       if (test(item)) {
         return item;
       }
     }
     return null;
   }

   const firstLoaded = firstWhere(tiles, (t) => t.status === 'loaded');
   ```

### Exercise 19.6 ★★ Tile arithmetic

Columns: 600 ÷ 256 rounds up to 3. Rows: 800 ÷ 256 rounds up to 4. So there are 3 × 4 = 12 tiles. The `left` values are 0, 256 and 512; the `top` values are 0, 256, 512 and 768. Widths are 256, 256 and `min(256, 600 − 512)` = 88; heights are 256, 256, 256 and `min(256, 800 − 768)` = 32. The bottom-right tile (row 3, column 2 when counting from 0) is at left 512, top 768, and is 88 pixels wide and 32 pixels tall. Its key is `"3-2"`.

## Chapter 20 solutions

# Chapter 20 solutions

### Exercise 20.1 ★ Which script needs a stack

`npm run e2e` (`playwright test`). Per `playwright.config.ts`, the end-to-end tests run against a full running stack served at `http://localhost:8081` and need an admin account supplied through environment variables.

### Exercise 20.2 ★ What ^22.1.8 accepts

`^22.1.8` accepts 22.1.8 and any newer 22.x.y, so 22.9.0 is accepted. 23.0.0 is not (a new major).

### Exercise 20.3 ★★ Why secure false is fine in development

`secure: false` turns off certificate verification for the proxy's connection to the target. In development the target is `http://localhost:8080` on your own machine, so nothing travels over a network. A production proxy would forward across a real network, where skipping verification would allow someone to impersonate the backend. (Note that production here uses nginx over a private container network, not this dev proxy.)

### Exercise 20.4 ★★ Install and build

A `dist/` folder appears, containing `dist/frontend/browser/` with `index.html` and JavaScript and CSS files whose names include a content hash, for example `main-XXXXXXXX.js` (the exact names vary). The Dockerfile copies that `browser` folder into nginx.

### Exercise 20.5 ★★★ Delete the lock file

Without a lock file, `npm install` may pick the newest 6.0.x available (for example a later patch than the one you tested with) and writes a new lock file. `npm ci` refuses to run because its whole purpose is to install exactly what a lock file records; with none, there is nothing to reproduce.

### Exercise 20.6 ★★ Reorder the Dockerfile

Docker reuses a cached layer only if everything above it is unchanged. With `COPY . .` before `npm ci`, any edit to any source file changes that layer, so the `npm ci` layer below it is invalidated and every rebuild re-downloads all dependencies. In the original order, `npm ci` is cached until `package.json` or `package-lock.json` changes, so a one-file edit re-runs only the final `ng build`.

## Chapter 21 solutions

# Chapter 21 solutions

### Exercise 21.1 ★ The Admin link condition

`@if (sessionService.isAdmin()) { ... }`. It reads `sessionService.isAdmin`, a computed signal in `SessionService` that is true when the signed-in user's role is `ADMIN`.

### Exercise 21.2 ★ Change the accent color

Every screen whose CSS uses `var(--accent)`: buttons, the active navigation underline, the Manage link in the viewer header, and the idle banner tint. Only the tokens changed, so no component file needs editing. (Remember the dark theme redefines `--accent` in its own block.)

### Exercise 21.3 ★★ Toggle a signal

Example:

   ```typescript
   readonly showHint = signal(false);
   ```

   ```html
   <button (click)="showHint.update((v) => !v)">Toggle hint</button>
   @if (showHint()) {
     <p>This is the hint.</p>
   }
   ```

### Exercise 21.4 ★★ Check color contrast

Compute with any WCAG contrast checker. Light: `#5d6470` on `#ffffff` and on `#f6f7f9`; dark: `#9aa0aa` on `#191b1f` and on `#101114`. Expected, rounded: about 6.0 : 1 (light on `--surface`), 5.6 : 1 (light on `--bg`), 6.6 : 1 (dark on `--surface`) and 7.2 : 1 (dark on `--bg`), all above the 4.5 : 1 the comment in `styles.css` promises. Small differences in the last digit between checkers are normal.

### Exercise 21.5 ★★★ Why track by id

Tracking by position would treat "the third card" as the same item before and after filtering, so Angular would reuse and rewrite existing cards' contents. Tracking by `documentId` gives every card a stable identity, so Angular keeps the cards that remain, removes the ones that disappear, and doesn't rebuild the rest.

### Exercise 21.6 ★★ Extend the counter

Example:

```typescript
readonly label = computed(() =>
  this.count() === 1 ? 'Clicked once' : `Clicked ${this.count()} times`);

reset(): void {
  this.count.set(0);
}
```

```html
<button (click)="add()">{{ label() }}</button>
<button (click)="reset()">Reset</button>
```

Add `label` and `reset` to the class in Example 21.1 and replace the button in the template. `label` is recalculated only when `count` changes, and a count of zero reads "Clicked 0 times".

## Chapter 22 solutions

# Chapter 22 solutions

### Exercise 22.1 ★ The HTTP methods

`GET` (list, get, findUsers, getTileUrls), `POST` (upload), `PATCH` (update), `PUT` (replaceFile, transferOwnership, share), and `DELETE` (delete, unshare). The file is sent by `upload` (`POST`, multipart `FormData`) and by `replaceFile` (`PUT`).

### Exercise 22.2 ★ The interceptor and a 401

Nothing special: `/api/auth/login` is in `AUTH_PROBES`, so the 401 is passed to the login form's own error handler, which shows "Incorrect username or password." Redirecting to `/login` would be pointless (the reader is already there) and would wipe what they typed.

### Exercise 22.3 ★★ Find the CSRF header

Yes: the header `X-XSRF-TOKEN` carries the same value as the `XSRF-TOKEN` cookie. A page on another origin can't read that cookie, so it can't produce the header.

### Exercise 22.4 ★★ The fallback retry time

It applies only when a 429 or 503 arrives without a usable `Retry-After` header: the viewer then waits that many seconds (5 by default) before asking for fresh tile URLs.

### Exercise 22.5 ★★★ A reload without the version check

If the reload returned the same version that was reported gone (for example because the tiles were unavailable for another reason), the viewer would reload, refetch the tiles, receive 410 again, and repeat indefinitely, hammering the server and spending the rate-limit budget. The check stops after one reload and shows an error instead.

### Exercise 22.6 ★★ Why `switchMap`

The reader types "al" and a request for "al" starts, and the server is slow to answer. The reader types "alice" and a request for "alice" starts, and the server answers it quickly. With `mergeMap`, both requests stay alive; when the slow answer for "al" finally arrives, it is emitted after the answer for "alice", replacing the correct suggestions with the ones for "al". `switchMap` cancels the "al" request as soon as "alice" starts, so a stale answer can never arrive.

## Chapter 23 solutions

# Chapter 23 solutions

### Exercise 23.1 ★ Admin-only routes

`admin` uses `roleGuard('ADMIN')`. `documents/upload` uses `roleGuard('PUBLISHER', 'ADMIN')`. Signed-in only (`authGuard`): `documents`, `documents/:documentId/manage`, `viewer/:documentId`, and `account`. `login` has no guard.

### Exercise 23.2 ★ The sign-out redirect

`/login?returnUrl=%2Fdocuments` (the guard passes the attempted URL, percent-encoded, in `returnUrl`). `auth.guard.spec.ts` checks the same behavior with `/viewer/abc?page=3`.

### Exercise 23.3 ★★ Guards are not security

The guard only decides which screen the browser shows. The document's tiles come from the API, which checks on every request that the signed-in user may see the document (the viewer shows "no longer available" on a 404). Removing the guard would let a signed-out visitor see the viewer's empty shell, but the API would still refuse them.

### Exercise 23.4 ★★ Add a lazy route

Example:

   ```typescript
   {
     path: 'about',
     loadComponent: () => import('./features/about/about.component').then((m) => m.AboutComponent),
   },
   ```

   placed before the catch-all `**` route (routes match in order).

### Exercise 23.5 ★★★ Typing a page number

Typing "1" or "-" in the page box would also trigger the key actions: "-" would zoom out, and arrow keys, meant to move the text cursor, would turn pages. Worse, `preventDefault()` would block the character from being typed at all.

### Exercise 23.6 ★★ Run `safeReturnUrl`

- `/documents/abc/manage` starts with one slash and not two, so it is returned unchanged.
- `///x` starts with `//`, so the result is `/documents`.
- `javascript:alert(1)` doesn't start with `/`, so the result is `/documents`.
- The empty string is falsy (`returnUrl &&` fails), so the result is `/documents`.

## Chapter 24 solutions

# Chapter 24 solutions

### Exercise 24.1 ★ Add a timeout test

Add inside the `describe` block:

   ```typescript
   it('is active when only a sixth of a long timeout has passed', () => {
     expect(idleState(t0 + 100_000, t0, 600)).toEqual({ kind: 'active' });
   });
   ```

   With 600 seconds of timeout, 500 seconds are left; the warning window is the smaller of 300 seconds and half the timeout (300), so 500 is outside it. Run with `npm test` in `frontend/`.

### Exercise 24.2 ★ What expectOne catches

`expectOne` fails the test, reporting that more than one matching request was found. It asserts *exactly* one, so it also catches accidental duplicate requests.

### Exercise 24.3 ★★ Test SessionService

Example:

   ```typescript
   it('is signed in after login', () => {
     const session = TestBed.inject(SessionService);
     session.login('someone', 'pw').subscribe();
     TestBed.inject(HttpTestingController).expectOne('/api/auth/login')
       .flush({ username: 'someone', role: 'READER', sessionTimeoutSeconds: 1800, mustChangePassword: false });
     expect(session.isLoggedIn()).toBe(true);
   });
   ```

   Use the same `TestBed.configureTestingModule` providers as in Listing 24.2.

### Exercise 24.4 ★★ Two themes

The two themes use different color values (Listing 21.8), so text that has enough contrast in one palette may not in the other. Checking only the light theme would leave the dark palette unverified.

### Exercise 24.5 ★★★ Why only end-to-end catches it

The bug was in how nginx built the `X-Forwarded-For` header before passing the request to the backend, and the backend's decision to trust it. A Vitest spec runs neither. Only a test that sends real requests through the whole stack can see the header being overwritten (or not). A unit test could still add a check on the frontend side that no code sets that header itself, but that is not where the vulnerability was.

### Exercise 24.6 ★★ Prove it can fail

In `idle.ts`, change `IDLE_WARNING_SECONDS = 5 * 60` to `5 * 61`. The warning window becomes 305 seconds, so at 1,499 seconds after activity (301 seconds left) the function returns a warning instead of `active`. The test's output shows the expected value `{ kind: 'active' }` next to the received `{ kind: 'warning', secondsLeft: 301 }`. (The test "starts warning exactly when five minutes remain" would still pass, which is why the two tests are needed together to pin down both sides of the boundary.) Put the constant back afterward.

## Chapter 25 solutions

# Solutions for Chapter 25

### Exercise 25.1 ★ Count the tiles

`tileCount(1240, 256)` is (1240 + 255) / 256 = 5 columns using integer division. Four full columns cover 1,024 pixels, so the last column is 1240 - 1024 = 216 pixels wide. `tileCount(1754, 256)` is (1754 + 255) / 256 = 7 rows; six full rows cover 1,536 pixels, so the last row is 218 pixels tall.

### Exercise 25.2 ★ Letter page

At 150 DPI the page is about 1,275 by 1,650 pixels. Columns: (1275 + 127) / 128 = 10 (nine full columns cover 1,152 pixels; the last is 123 pixels wide). Rows: (1650 + 127) / 128 = 13 (twelve full rows cover 1,536; the last is 114 tall). 10 × 13 = 130 tiles, about four times as many as with 256-pixel tiles, which is why tile size affects request counts and rate limits.

### Exercise 25.3 ★★ Tamper with a token

The client receives HTTP 401 with a JSON body `{"error": "..."}`. At `book-m0-mvp`, `GlobalExceptionHandler` maps `InvalidTokenException` to 401. A token whose payload was altered fails the signature comparison ("Signature mismatch"). The signature is checked first so that data nobody signed never reaches the parsing code, and the comparison is constant-time so response timing reveals nothing about how much matched.

### Exercise 25.4 ★★ Build a canonical string

The canonical string is `d9|1|0|4|s7|5000` (document, page, row, column, session, expiry, separated by `|`). If the column is changed to 5 and the old signature kept, the payload part of the token now decodes to `d9|1|0|5|s7|5000`. `verifyAndDecode` recomputes the HMAC of that canonical string, gets a value different from the supplied signature, and `constantTimeEquals` fails, so it throws `InvalidTokenException("Signature mismatch — token was tampered with or forged")`. The expiry check is never reached.

### Exercise 25.5 ★★★ Two independent checks

One good answer: the token proves that the server issued it for one tile until a fixed time, but not that the person is still allowed to read. If the user signs out or the session times out, tokens issued under that session are still unexpired, so `TileController` also calls `requireValidSession`. The two failures (bad token, dead session) stay distinguishable. Later milestones add a third question, whether the user may still see the document at all (Chapter 27).

### Exercise 25.6 ★★★ Design a limit

One good answer. Count after the token and session checks (so unauthenticated or forged requests can't consume a real reader's allowance) and before the disk read and watermark (so a refused request doesn't pay their cost). Use a sliding window of recent request times per key. When the count is over the limit, refuse with HTTP 429 (Too Many Requests) and a `Retry-After` header saying when to try again. Keying by session id is weak because a user can sign in again and get a fresh session with a fresh allowance; the project found this (finding `TM-3`) and moved to a per-user key in milestone 1 (Chapter 26).

## Chapter 26 solutions

# Solutions for Chapter 26

### Exercise 26.1 ★ Who may upload?

PUBLISHER and ADMIN (`hasAnyRole("PUBLISHER", "ADMIN")`). A signed-in READER gets HTTP 403 with a JSON body in the `{"error": "..."}` shape: `SecurityErrorResponses` writes FORBIDDEN through the access-denied handler. An anonymous caller gets 401 instead.

### Exercise 26.2 ★ Why deny by default

Rules match top to bottom, and the last one catches everything nobody thought of. If it were `permitAll()`, every route added later and not listed would be public until someone noticed. With `denyAll()`, a forgotten route is closed, and you find out in testing rather than in an incident.

### Exercise 26.3 ★★ Normalize and validate

- `Alice` becomes `alice`: accepted.
- ` bob ` becomes `bob` (trimmed and lower-cased): accepted, because it is 3 characters.
- `a` stays `a`: rejected, fewer than 3 characters.
- `Carol!` becomes `carol!`: rejected, because `!` is not in `[a-z0-9._-]`.
- `dave.smith` stays `dave.smith`: accepted.
(All of them also need an acceptable password of 12 to 128 characters and a role, and must not already exist.)

### Exercise 26.4 ★★ Sliding window

Yes, it is refused. The window is 60 seconds and all 120 earlier requests happened within the last 20 seconds, so the count is 120, which reaches the limit. The oldest recorded request was at about second 0 and leaves the window at second 60; now is second 20, so `Retry-After` is about 40 seconds (the code computes the time between the window's cutoff and the oldest timestamp, rounds up to whole seconds and uses at least 1).

### Exercise 26.5 ★★★ Bind a token

The signature check passes: the token really was issued by the server, and it hasn't expired. The next check compares the token's session binding, `HMAC("tile-binding:" + Alice's session id)`, with the binding recomputed from *Bob's* own session id (`SessionKeys.tileBindingMatches`, a constant-time comparison). They differ, so the request is refused as issued to a different session. Signing alone proves only that the server issued the token; without the binding, anyone holding the URL could use it until it expires.

### Exercise 26.6 ★★★ Inspect the cookies

One good answer: the session cookie (`SDV_SESSION`) is httpOnly, so JavaScript can't read it, while the CSRF cookie (`XSRF-TOKEN`) is created with `withHttpOnlyFalse()` so Angular's `HttpClient` can copy its value into the `X-XSRF-TOKEN` request header. That is the double-submit pattern: another site can make your browser send the cookies but can't read the CSRF value to put it in the header. Both cookies are `SameSite=Strict`.

## Chapter 27 solutions

# Solutions for Chapter 27

### Exercise 27.1 ★ Why 404

A 403 would confirm that a document with that id exists. With 404, an outsider can't tell a hidden document from one that was never there, and the response is the only thing an outsider sees, so it is the whole disclosure (Section 27.2).

### Exercise 27.2 ★ Three ways to see a document

(1) `d.visibility = :everyone`, the document is open to everyone signed in; (2) `d.owner.username = :username`, the caller is the owner; (3) `:username in (select s.username from d.sharedWith s)`, the caller is among the users it is shared with. Admins skip this query and use `findTitle` in `DocumentService.titleIfViewable`.

### Exercise 27.3 ★★ Read the rows

- `pub.one` (owner): passes; rename allowed.
- `reader.one` (shared with): passes; rename returns 403 (`ForbiddenException`, and an `ACCESS_DENIED` event with detail `manage`).
- `outsider.one`: fails the check (not everyone, not owner, not in the shares); rename returns 404, because `requireViewable` refuses first (and records `ACCESS_DENIED` with detail `view`).
- Admin: passes through `findTitle` (no visibility test); rename allowed.

### Exercise 27.4 ★★ Why REQUIRES_NEW

By default Spring runs the request's database work in one transaction. In `requireViewable` the code writes the `ACCESS_DENIED` row and then throws `DocumentNotFoundException`. The exception makes Spring roll back the transaction, and the audit row, being part of it, is rolled back too, so the denial is never stored. With `REQUIRES_NEW` the insert runs in a separate transaction that commits when `record` returns, before the exception is thrown, so the caller's rollback can't touch it. The ordering matters: `audit.record(...)` is called before the `throw`.

### Exercise 27.5 ★★ CSV injection

`csv("=1+1")` returns `"'=1+1"` (with the surrounding double quotes): the first character `=` triggers the single-quote prefix, so the spreadsheet shows text instead of computing 2. `csv("Report, final")` returns `"Report, final"`: the value is wrapped in quotes, so the comma is part of the field and doesn't split the row. (`csv("")` and `csv(null)` return an empty string.)

### Exercise 27.6 ★★★ Unshare while reading

One good answer: the next tile request returns 404. The URL is still validly signed and unexpired, but `findTitleIfVisible` finds no share, so the access gate refuses. The reader's already-loaded tiles remain on screen because they are images in the browser. To make the page disappear too, the viewer would need to notice the 404 and discard the tiles and show a message; the design in Chapter 30 adds exactly an "access lost" screen for this. The server can't take back pixels it has already sent, which is why the watermark, the short URL lifetime and the rate limit matter as well.

## Chapter 28 solutions

# Solutions for Chapter 28

### Exercise 28.1 ★ A 60 MB upload

HTTP 413 with a JSON body `{"error": "The file is too large (limit 50 MB)."}`. The multipart limit (`max-file-size: 50MB`) makes the framework throw `MaxUploadSizeExceededException`, and `GlobalExceptionHandler.handleUploadTooLarge` turns it into a 413. (A browser using the project's frontend would have refused the file earlier, with a message.)

### Exercise 28.2 ★ Reference, not message

The exception message can contain SQL, file paths, class names or other internals that help an attacker and confuse users. The catch-all logs the full exception under an eight-character reference and returns only the reference. A user reports the reference and an operator finds the exact failure in the log, without the client ever seeing the internals.

### Exercise 28.3 ★★ Is this page allowed?

Scale = 150 / 72 ≈ 2.0833. Width ≈ 1,000 × 2.0833 ≈ 2,083 pixels; height ≈ 1,500 × 2.0833 ≈ 3,125 pixels. The area is about 6.5 million pixels, which rounds to 7 million. That is well below 40,000,000, so the page passes (assuming the document is also at most 500 pages and under 50 MB).

### Exercise 28.4 ★★ Order of checks

The signature check is cheap: it reads at most 1,024 bytes and needs no parser, so obviously wrong files are refused before any parsing cost or exposure to parser bugs. The page count and page sizes need the document to be opened and parsed. The pixel check comes before rendering because rendering is what allocates the image: the arithmetic on the declared size must run first, or a hostile page size would exhaust memory before the check could refuse it.

### Exercise 28.5 ★★ Read the headers

You should see `Content-Security-Policy` (containing `default-src 'none'` and `frame-ancestors 'none'`), `Referrer-Policy: no-referrer`, `Permissions-Policy`, `X-Content-Type-Options: nosniff` and `X-Frame-Options: DENY`. `SecurityHeadersTest.apiResponsesCarryHardeningHeaders` asserts them.

### Exercise 28.6 ★★★ Health and env

One good answer. With MySQL running, health returns 200 with `{"status":"UP"}` and no details. With MySQL stopped, it returns 503 with status `DOWN`. `/actuator/env` stays closed because only `health` is exposed (`management.endpoints.web.exposure.include: health`), the security rules permit only the health paths, and every other route falls under `anyRequest().denyAll()`, so an anonymous caller gets 401 (403 or 404 would also satisfy `SecurityHeadersTest`).

## Chapter 29 solutions

# Solutions for Chapter 29

### Exercise 29.1 ★ Idle state

With 10 minutes (600 s) left of a 1,800 s timeout, `idleState` returns `{ kind: 'active' }`, because the warning window is
`min(300, 1800 / 2) = 300` seconds and 600 is more than 300. With 4 minutes (240 s) left it returns
`{ kind: 'warning', secondsLeft: 240 }`.

### Exercise 29.2 ★ Keys while typing

Arrow keys move the text cursor inside an input. If the viewer also turned pages, you couldn't edit the number you were typing. `onKeydown` returns early when the event target is an `INPUT`, `TEXTAREA` or `SELECT`, when it is editable content, and when Ctrl, Cmd or Alt is held.

### Exercise 29.3 ★★ Which page opens?

Page 7 (the stored zero-based 6 is page 7 when counting from 1). `?page=25` is invalid for a 20-page document, so `initialPage` ignores it and falls back to storage. If the address has no `page` and storage is empty, `Number(null)` is 0 for the first read, which fails the `>= 1` test. The stored read of `null` also gives 0, which is a valid index, so page 1 opens.

### Exercise 29.4 ★★ Spacing arithmetic

`gap = round(20 * 2.0) = 40`. `stepX = blockWidth + gap = 120 + 40 = 160`. With two lines, `stepY = lineHeight * 2 + gap / 2 = 40 + 20 = 60`.

### Exercise 29.5 ★★ Clamped spacing

The constructor computes `Math.max(0.5, Math.min(6.0, spacing))`. For 20, `Math.min(6.0, 20)` is 6.0 and `Math.max(0.5, 6.0)` is 6.0. A bad setting can't make the mark absurdly sparse, and 0.5 is the floor at the dense end.

### Exercise 29.6 ★★★ Design a trace lookup

One good answer. `trim()` removes the outer spaces, giving `abc 123`; `toUpperCase()` gives `ABC 123`; `replaceAll("[^0-9A-Z]", "")` removes the inner space, giving `ABC123`. The query is `session_handle like ?` with the argument `ABC123%`, so it matches every audit row whose session handle starts with those six characters, that is, that session's events. Without the normalization, a `%` or `_` typed or pasted into the box would act as a wildcard in the `LIKE` pattern (`%` would match every row), turning a precise lookup into a broad scan; and uppercase and stray spaces would cause false misses. The parameter placeholder `?` already prevents SQL injection, so this is a separate protection.

## Chapter 30 solutions

# Solutions for Chapter 30

### Exercise 30.1 ★ Forwarded header

Any client can send a header with any value. If the backend believed `X-Forwarded-For` from every sender, a client could choose the address that the sign-in throttle and the audit log see, resetting its own lockout at will. Trust belongs to a network position: nginx overwrites the header with the real peer address, and the API accepts forwarded headers only from nginx's fixed address (`TRUSTED_PROXY_REGEX`).

### Exercise 30.2 ★ Layers of an image

Docker caches each layer and reuses it when its inputs are unchanged. Dependencies change rarely (only when `pom.xml` changes) and the source changes on almost every build. Copying `pom.xml` first and downloading dependencies in their own layer means a source-only change rebuilds from the `COPY src` step, skipping the slow download.

### Exercise 30.3 ★★ Lockout abuse

With an account-wide count that applied to every address, an attacker could fail sign-in for a victim's account from several addresses until the total reached 20, and then the victim's own attempts (from their usual computer) were refused: a denial of service. The final rule applies the account-wide count only to unrecognised devices; an address that signed in successfully in the last 30 days is recognised and isn't blocked by it. The cost: a correct password from a new device is refused (429) during an account-wide lockout until an admin unlocks the account.

### Exercise 30.4 ★★ Order of gates

Cheap checks come before expensive ones, and unauthenticated requests must not be able to spend a legitimate user's allowance. The rate limit runs after the signature and session-binding checks (so only the real session's owner is counted) and before the disk read and render, so a throttled request doesn't pay their cost. The access check comes before the disk read because there is no reason to load a tile the user may not see, and because it is the check that turns an unshared or deleted document into a 404. (It also comes after the rate limit in the code, which means even requests for documents the user can't see are counted against the allowance.)

### Exercise 30.5 ★★★ Version inside the token

Only the signed fields of a token are tamper-proof. A version passed as a separate query parameter could be edited by the client to ask for an older or newer render without invalidating the signature, so a stale URL could still be redeemed. Signing the version means the token names exactly the render it was issued for; the server compares it with the document's current version and answers 410 when they differ.

### Exercise 30.6 ★★★ Design a limit

One good answer, following `TileWorkLimiter`: create a component with `private final Semaphore slots = new Semaphore(2, true)` and a method `run(Callable<T> work)` that calls `slots.tryAcquire(...)` with a short wait; on failure throw a "busy" exception that the error handler turns into HTTP 503 with a `Retry-After` header (a message such as "Too many exports are running. Try again in a few seconds."); on success run the work inside `try`/`finally` and release the permit in the `finally`. Add a metric counter for refusals, and make the permit count configuration.

## Chapter 31 solutions

# Solutions for Chapter 31

### Exercise 31.1 ★ Read the range

- `^5.0.1` allows 5.0.1 up to but not including 6.0.0. Version 5.4.0: yes. Version 6.0.5: no.
- `~6.0.2` allows 6.0.2 up to but not including 6.1.0. Version 5.4.0: no. Version 6.0.5: yes.
- `5.0.1` (no prefix) means exactly that version. Neither 5.4.0 nor 6.0.5 is allowed.

### Exercise 31.2 ★ Why the polling loop does not slow a passing test

The loop checks the condition first and sleeps only while the condition is false. In the usual order of
events the slot is already free on the first check, or after one 10 ms sleep. The 5-second deadline
matters only when something is actually wrong.

### Exercise 31.3 ★★ Peer range

First find which package declares the range (here `@angular/build` declares TypeScript `>=6.0 <6.1`) and
whether the update falls outside it. Two acceptable outcomes: close or defer the update until the
framework upgrade that widens the range, or upgrade the framework and the dependency together in one
planned change. Forcing the install past the range is not acceptable.

### Exercise 31.4 ★★ Spot the race

Two requests can both run the first line before either reaches the third, because the password check
takes about 100 ms. Both see fewer than 5 failures and both proceed, so a burst of parallel guesses
exceeds the limit. To fix it, make the check and the count one atomic step before the slow work: reserve an
attempt (increment) under a lock or with an atomic operation, check the password, and give the
attempt back only when the password is correct. `LoginThrottle.reserve` does this with `synchronized`.

### Exercise 31.5 ★★ Characters and bytes

30 characters at 4 bytes each is 120 bytes. The password passes the 12 to 128 character rule but
fails `fitsBcrypt`, because 120 is more than `MAX_PASSWORD_BYTES` (72). The user sees a 400 error with
the message "Password is too long: at most 72 bytes (fewer characters if it uses accents, non-Latin
letters or emoji)."

### Exercise 31.6 ★★★ Draft a policy

One good answer, modeled on the project's MySQL rule:

```yaml
ignore:
  # Stay on <library> major N: moving to N+1 is a planned upgrade with a migration test.
  - dependency-name: <library>
    update-types: ['version-update:semver-major']
```

On the day the current major stops being supported: read the migration guide, upgrade in its own
pull request on a branch, run the full suite including any integration tests that touch stored data,
take a backup first, remove or update the ignore rule with the reason changed, and record the
decision in the pull request description.

## Chapter 32 solutions


### Exercise 32.1 ★ Find the control

- PDF: `TileGenerationService` (deletes the source after ingest) and `TileController` (the only pixel endpoint).
- Tile: `SignedUrlService` (HMAC and expiry) and `SessionKeys` (session binding).
- Document: the `document/` package (`DocumentService` checks owner, visibility, and shares).
- Account: `LoginThrottle` (counters) and `SecurityConfig` (registers `PasswordChangeRequiredFilter`).
- Session: `SecurityConfig` (CSRF repository, cookie settings, session management).
- Availability: `TileGenerationService` (limits, render pool, timeout) and `TileRateLimiter`.
- Audit trail: `TileController` (`PAGE_VIEW_AUDIT_INTERVAL`, 10 minutes) and the `audit/` package.

Run `git grep -n ClassName book-m5-platform` to confirm each path.

### Exercise 32.2 ★★ Predict the denial

With `denyAll()` first, every request matches it before any other rule, so every request is refused, including `POST /api/auth/login` and `GET /actuator/health`. The health check failing is the surprise: the compose health check for the app container would report it unhealthy, so `web` (which depends on a healthy `app`) would not start. Most security integration tests fail, because they expect sign-in to work.

### Exercise 32.3 ★★ Threat-model a new feature

A model answer. Asset: the audit history (who viewed what, and when). Actors: administrators (allowed), other roles (not), an attacker with a stolen reader session. Entry points: the export endpoint and its query parameters. Controls: restrict the endpoint to `ADMIN` in `SecurityConfig` (as `/api/admin/**` already is); neutralize spreadsheet formulas in exported cells (the existing CSV export does this); cap the number of rows or the date range per export so one request cannot exhaust memory. Any two of these earn full credit.

### Exercise 32.4 ★★★ Attack the lockout

The first design counted failures per account across all addresses. Anyone could fail 20 times against a victim's username and lock the real owner out (a denial of service against the victim).

The recognised-device design applies that account-wide counter only to attempts from unrecognised devices. The attacker's gain: while an account is under a distributed attack, its owner cannot sign in from a new device (new laptop, hotel network) until the window passes or an administrator unlocks it. An attacker who knows this can time an attack to coincide with a victim's travel.

A mitigation not in the app: a second proof for unrecognised devices, such as an emailed one-time code or MFA, so that a new device can prove itself without waiting for an administrator.

### Exercise 32.5 ★★ Reorder the checks

The user would still be refused, but the server would already have done the expensive work: reading the tile from disk, drawing the watermark, and encoding a PNG. A signed-in attacker over their limit could keep sending requests and force that work on every one, spending the server's CPU while receiving nothing useful. That turns the rate limit from a protection for the server into a cosmetic one, and it would also make the `503` busy-cap fire for everyone else. The third comment in the listing warns against exactly this: the limit is enforced "before the disk read/render so a throttled request doesn't pay that cost". The same comment explains why it runs after authentication: "so unauthenticated requests can't burn a legitimate user's allowance".

### Exercise 32.6 ★★★ Review a new endpoint

A model answer. (1) Asset and users: the page-1 thumbnail of a document; the same people who may view the document (owner, shared users, everyone if visibility is EVERYONE, admins). (2) Forgeable inputs: the document id in the path, the session cookie; every other value must be derived from server state, so nothing else should be accepted from the caller. (3) Checks: authentication (`/api/**` is `authenticated()`); the same document-access check used for the list and the tile requests, answering `404` for "not found" and "not yours"; a rate limit, because a thumbnail is still a page image; and the same watermark, because a thumbnail is still content (or the endpoint shouldn't exist). The check must run on every request, not only when a link is issued. (4) Concurrency: many thumbnails at once cost CPU, so route them through the existing tile work limiter or a similar cap, and refund allowance on `503`. (5) Failure: identical responses for missing and not-shared documents; generic errors; no file paths. Tests: an integration test that a reader without a share gets `404` for a real document id, identical to the answer for a random id; and an end-to-end test through nginx in which an outsider is told the document doesn't exist, as in the project's Playwright test.

## Chapter 33 solutions


### Exercise 33.1 ★ Read the ports

`mysql`: `127.0.0.1:3306` (configurable with `DB_PORT`). `web`: `127.0.0.1:8081` (`WEB_PORT`), mapped to nginx's 8080 inside the network. `tls`: `127.0.0.1:8443` (`TLS_PORT`), mapped to Caddy's 443. `app` publishes nothing; it only `expose`s 8080 to the compose network. None is `0.0.0.0` because each mapping starts with `127.0.0.1:`, so only the host machine itself can connect. Publishing on all interfaces is a deliberate go-live step: you edit the `tls` service's `ports` entry.

### Exercise 33.2 ★★ Predict the spoof

The app sees the real connection address, never `203.0.113.9`. In the `/api/` location nginx runs `proxy_set_header X-Forwarded-For $remote_addr;`, which overwrites whatever the client sent with the address of the TCP peer. Also, `set_real_ip_from 172.28.0.11` trusts a forwarded address only from Caddy, so a client connecting directly to nginx is not believed. In a local setup the address the app sees is the Docker gateway's address.

### Exercise 33.3 ★★★ Change the address

`TRUSTED_PROXY_REGEX` still names `172\.28\.0\.10`, so the app no longer trusts the forwarded header from nginx at its new address; it judges every request by its own peer address, which is now nginx's. Symptom: every user appears to come from the same address, so per-address throttling and the audit log's addresses all show nginx, and one person's failed sign-ins count against everyone (the 20-per-address rule then locks out all users sooner). Nothing crashes, which is why the failure is silent. Also, the new address must lie inside the `172.28.0.0/24` subnet declared in the compose file. The fix is to change the compose `ipv4_address` and `TRUSTED_PROXY_REGEX` together (and `set_real_ip_from` too, if the change was to Caddy's address).

### Exercise 33.4 ★★ Read the Dockerfile

Docker caches each instruction as a layer and reuses a layer only if the instruction and everything before it are unchanged. Copying `pom.xml` and running `dependency:go-offline` first means the dependency download layer is reused until `pom.xml` changes. If you copied `src` first, any code edit would invalidate the cache from that point on and every build would download all dependencies again, which is much slower.

### Exercise 33.5 ★★ Diagnose

A reasonable order: (1) Are you actually using HTTPS end to end? A `Secure` cookie isn't stored or sent over plain HTTP; open the site by its `https://` address and check the browser's developer tools (Application, Cookies) for the `SDV_SESSION` cookie. (2) Does the app see the request as HTTPS? Check that `FORWARD_HEADERS_STRATEGY` and `TRUSTED_PROXY_REGEX` are set so that `X-Forwarded-Proto` from the proxy is believed. (3) Is something dropping the cookie: a proxy stripping `Set-Cookie`, a different host name between requests, or a session lifetime or idle timeout ending the session (`session-max-lifetime`, 30-minute idle timeout)? Also check the health of the app (a restart signs everyone out, Chapter 34). Any ordered list that starts with the cheapest, most likely checks earns credit.

### Exercise 33.6 ★★★ Plan the go-live

A model answer. `.env`: strong unique `DB_PASSWORD`, `DB_ROOT_PASSWORD`, `SIGNING_SECRET` (32+ characters), `SITE_ADDRESS=docs.example.com`, `TLS_MODE=<an email address>`, `SESSION_COOKIE_SECURE=true`, `METRICS_ALLOWED_ADDRESSES` set to the monitoring server, and the bootstrap admin password either unset (generated, read from the log once) or set and then cleared. Compose: publish 80 and 443 on the `tls` service. DNS: an A (and AAAA if used) record pointing the name at the server. First three tests: (1) `https://docs.example.com` loads with a valid certificate and the response has `Strict-Transport-Security`; (2) sign in as the admin, change the password, create a reader and confirm they can open a shared document; (3) from another machine, confirm that ports 3306 and 8080 are unreachable and that `/actuator/prometheus` is refused. Then schedule the first backup and restore drill (Chapter 34).

## Chapter 34 solutions


### Exercise 34.1 ★ Name the state

- **Login sessions:** no. They live in the app's memory; after a restore, users sign in again.
- **Tiles:** yes (`app-storage` volume). They are the only copy of the content.
- **Audit log:** yes. It is in MySQL, so the dump covers it.
- **`.env` file:** yes, kept with the backup and protected like it. `SIGNING_SECRET` keys the recognised-device hashes, and the database passwords are needed to start MySQL against the old data.
- **Source PDFs:** nothing to back up; they are deleted after ingest, so the tiles hold the content.

### Exercise 34.2 ★ Read a command

`docker compose exec -T mysql` runs a command in the running `mysql` container; `-T` disables the pseudo-terminal so redirecting the output to a file doesn't mix in terminal control characters. `sh -c '...'` starts a shell inside the container. The single quotes stop the host shell from expanding `$MYSQL_ROOT_PASSWORD` and `$MYSQL_DATABASE`; the container's shell expands them from the container's own environment, so the password doesn't appear in your terminal or history. `exec mysqldump` replaces the shell with the dump program. `--single-transaction` reads a consistent snapshot of the InnoDB tables without long locks; `--routines` includes stored routines; `-u root -p"..."` gives the user and password; the last word is the database name. `> securedocs.sql` writes the dump to a file on the host.

### Exercise 34.3 ★★ Break the match

At 10:00 the dump records document D at version 1. At 10:01 the replace renders version 2, switches D to it, and deletes version 1's tiles. At 10:02 the archive holds only version 2. After a restore, the database says D uses version 1, but `D/v1/...` isn't on disk, so every tile request for D finds nothing and the reader sees blank pages or errors. The janitor's safety net notices that D's current version is missing, logs a warning, and refuses to prune D's other versions (here, version 2), so an operator can still recover the content, for example by pointing the document at version 2. It does not repair anything by itself. The runbook prevents the whole situation by stopping the app around both captures.

### Exercise 34.4 ★★ Read the janitor

(a) `notes` doesn't match the document-id pattern, so the janitor skips it and never deletes it. (b) A document-id-shaped folder with no matching document is an orphan, but it is only ten minutes old, which is inside `MIN_AGE` (one hour), so it is left alone. It may be a render in progress. (c) The same folder at two days old is an orphan and old enough, so `tryDelete` removes it (and counts it in the log line). If the delete fails because a file is locked, it logs a warning and retries at the next sweep, six hours later.

### Exercise 34.5 ★★★ Design the drill

A good answer names: a scratch environment (a separate compose project with its own folder name, ports, and volumes, never production); the steps (start MySQL, load the dump, unpack the tiles, start the app); proof (Flyway validates, a known account signs in, several documents open including one that was replaced, every tile loads, the newest audit event has the expected date); what is timed (start of restore to first watermarked tile, giving the real recovery time); the frequency (before go-live, then at a stated interval such as quarterly, and after any change to the backup commands or the storage layout); and cleanup (delete the scratch containers, volumes, and backup files, because they contain password hashes and document content).

### Exercise 34.6 ★★★ Plan a change

Two changes, in the spirit of the chapter: (1) the tile store needs a way to take a point-in-time snapshot (storage-level snapshots, or object storage with versioned keys) so that the archive doesn't depend on the files being still while it is read; (2) the database and the tile store need a common reference point, for example a recorded database position or snapshot time that says which tile versions belong to it, so a restore can be checked or made consistent afterward. Chapter 37's alternatives point the same way: object storage such as S3 (section 37.7) for versioned tiles, and a managed database (section 37.9) with point-in-time recovery. Even then, the "current tile version" pointer in the database still has to match the tiles, so consistency needs design, not just a different product.

## Chapter 35 solutions


### Exercise 35.1 ★ Health, metric, or audit?

"Is the database reachable?" is a health check (`/actuator/health` answers `503` when it is down). "How many tiles were served today?" is a metric (`sdv_tiles_served_total`, looked at as an increase over a day). "Who opened document X?" is the audit log (`PAGE_VIEWED` events, filtered by document). "Are uploads being refused?" is a metric (`sdv_render_rejected_total`), with the audit log available afterward if you need to know which publisher was affected.

### Exercise 35.2 ★ Read the page

Average render time is `sdv_render_seconds_sum / sdv_render_seconds_count` = 118.6 / 31, which is about 3.8 seconds per PDF. The page shows 9 failed sign-ins (`sdv_sign_in_total{outcome="failure"}`), out of 214 + 9 + 0 = 223 attempts.

### Exercise 35.3 ★★ Read the rule

`fromAddresses(cidrs)` builds one address matcher per configured range and allows the request only if the request's address matches at least one. It judges by the connection address because `X-Forwarded-For` can be forged (Chapter 32); a rule that believed the header would let any caller claim to be the Prometheus server. If the setting stays at its default (loopback only) while Prometheus runs in another container, Prometheus's requests come from that container's network address, which isn't on the list, so every scrape is refused and Prometheus shows the target as down. You would set `METRICS_ALLOWED_ADDRESSES` to Prometheus's address (or its subnet).

### Exercise 35.4 ★★ Which metric?

`sdv_render_rejected_total` counts uploads refused with `503` because every render slot stayed busy. The governing settings are `max-concurrent-renders` (default 2) and `render-queue-timeout-seconds` (default 30): uploads wait up to that long for a slot before being refused. To reduce it, raise `max-concurrent-renders` (mindful of memory; the app container has a memory limit) or give more time, and check `sdv_render_seconds` and `sdv_render_timed_out_total` to see whether a few very slow PDFs are holding the slots.

### Exercise 35.5 ★★★ Design an alert

A model answer. Condition: `increase(sdv_sign_in_total{outcome="locked"}[15m]) > 5`, holding for 5 minutes (the throttle window is 15 minutes). Meaning: a throttle rule is firing repeatedly, so either guessing is happening or real users are locked out. First three steps: (1) open the admin audit log and filter for the lockout event, reading the `rule=` value (account and IP, IP, or account-wide) and the addresses; (2) if one address dominates, treat it as guessing and consider blocking it at the network edge; if the account-wide rule fired, tell the account's owner that a new device won't be accepted until the window passes or an administrator unlocks the account, and decide whether to press Unlock; (3) check whether `sdv_sign_in_total{outcome="success"}` is normal, which suggests the attack is failing. Any threshold with a stated window, a hold time, and a concrete first step earns credit.

### Exercise 35.6 ★★★ Explain the zero

A Prometheus series exists only after the app first exports it. If the counter for `outcome="locked"` were created lazily (only at the first lockout), then before that first lockout the series wouldn't exist. An alert rule such as `increase(sdv_sign_in_total{outcome="locked"}[15m]) > 5` compares against a series that isn't there, so it evaluates to nothing rather than to 0 or to true; and the very first lockout would appear as a new series with no earlier point to compute an increase from, so the rise from 0 to 1 can be missed. Registering every outcome up front exports each as 0 from the start, so the series is continuous and the rule always has a value to compare.

## Chapter 36 solutions


### Exercise 36.1 ★ Which job?

A failing unit test: Backend tests (or Frontend tests and build for a TypeScript test). A vulnerable Maven library: the OSV job. An outdated operating system package in the nginx image: the Trivy step in the end-to-end job, which scans the built `secure-doc-viewer-web` image. A broken sign-in screen: the end-to-end job, where Playwright drives a real browser against the built stack.

### Exercise 36.2 ★ Read the pin

The workflow uses the long hash (`3d3c42e5...`), the commit SHA. The `# v7.0.1` comment is only for humans, so they can see which release the hash corresponds to. It matters because a tag such as `v7` can be moved to different code by whoever controls the action's repository, while a commit SHA identifies one fixed commit; the pinned workflow runs the code that was reviewed.

### Exercise 36.3 ★★ Why pin?

If the tag were moved to malicious code, the next run would execute it inside your workflow, where it could read the checked-out source and any secrets that job has, or tamper with build output. The setting `permissions: contents: read` near the top of the workflow limits the damage: the workflow's token can read the repository but can't push to it. Pinning by SHA prevents the swap in the first place.

### Exercise 36.4 ★★ Read the flags

`--severity HIGH,CRITICAL` reports only serious findings; it hides medium and low ones, which may still matter in combination or in your context. `--ignore-unfixed` skips findings for which no fixed version exists; it hides serious flaws that you can't fix yet, so someone has to watch the advisories. `--exit-code 1` makes Trivy exit with an error when it reports any finding (after the other two flags have filtered), which is what turns the step red; without it, Trivy would print findings and the job would still pass, hiding them from anyone who doesn't read the log.

### Exercise 36.5 ★★★ Handle a finding

A model answer. (1) Read the advisory: the library, the vulnerable range, and the fixed version. (2) Confirm the fixed version is a patch release that should be compatible. (3) Override the managed version in `pom.xml`, in the same way the project overrode Tomcat: add a property such as `<library.version>1.2.4</library.version>` (the exact property name comes from the framework's dependency management for that library). (4) Write a comment above it: name the advisory identifiers, say which framework version ships the older library, and say when to remove the override ("Drop this once the framework manages 1.2.4 or newer"). (5) Run `./mvnw -B verify`, then let CI run the OSV job and the end-to-end job. (6) Add a calendar reminder or an issue to remove the override when the framework catches up. Any answer with an override, a comment naming the advisories and the removal condition, and a full test run earns credit.

### Exercise 36.6 ★★★ Write a rule

The exercise is hypothetical. The `typescript` rule ignores minor and major updates, so Dependabot would not propose the newer minor version at all; the project's Angular 22 accepts only `>=6.0 <6.1`. When a person upgrades Angular, they would upgrade Angular and TypeScript together by hand, following the supported range in the new Angular release's notes, and then adjust the ignore rule to match, for example allowing patches of the new minor. The rule exists so that TypeScript moves in step with Angular, never ahead of it.

## Chapter 37 solutions


### Exercise 37.1 ★ Where is the choice?

Examples: 37.1 tiles, no PDF: README "Why this design" and `TileGenerationService`. 37.2 watermark timing: README "Watermarking happens on the way out" and `WatermarkService`. 37.4 URL signing: `SignedUrlService`. 37.5 sessions: README Limitations. 37.7 tile storage: `storage-root` in the README configuration table and `StorageJanitor`. 37.11 one instance: README "Go-live checklist".

### Exercise 37.2 ★★ Why S3 forces a rethink

If tiles move to S3 behind a CDN with signed URLs, the CDN serves tile bytes and the app no longer sees each request. Consequences:

- **Watermarking (37.2):** stamping happens in the app at serve time. At the edge, either you stamp somewhere else (an image service or edge function) or you serve unstamped tiles, which loses attribution.
- **Session checks (37.4):** the app checks the session on every tile request. A CDN signed URL is checked by the CDN, which knows nothing about your session, so logout and unsharing no longer cut off outstanding URLs until they expire. You would shorten lifetimes or add an edge authorizer.
- **Rate limiting (37.8):** per-user counters in the app no longer see tile traffic.

### Exercise 37.3 ★★ Order of change

One defensible order: (1) shared sessions and counters in Redis (37.5), (2) tile storage on S3 (37.7), (3) managed database (37.9), (4) more instances behind a load balancer (37.11), (5) CDN signed URLs (37.4). Reasoning: a second instance is unsafe until sessions, counters, and tiles are all shared, so those come first; the CDN change comes last because it changes watermarking and session checks. Any order that puts 37.11 after 37.5 and 37.7 and justifies it is acceptable.

### Exercise 37.4 ★★★ A switch trigger as an alert

Example for 37.5 to 37.11 (needing a second instance): alert when average CPU of the app container stays above 80% for 15 minutes while `sdv_tiles_rate_limited_total` is flat (load is real reader traffic, not throttled harvesting). For 37.8: alert when the rate of `sdv_tiles_rate_limited_total` per hour exceeds an agreed share of `sdv_tiles_served_total`, meaning readers are hurt by the limit. The answer must name a metric from Chapter 35 and a threshold with a time window.

### Exercise 37.5 ★★ Cookie or token?

With tokens: the client (mobile app) stores the token itself and sends it in a header on each request, so there is no cookie and therefore no automatic sending; the CSRF attack that cookies invite doesn't apply in the same way, though storing the token safely on the device becomes the app's job. Revocation gets harder: a signed token is valid until it expires unless the server keeps a list of revoked tokens, so "end this user's sessions now" (sign-out everywhere, a role change, an admin revoke) needs extra work or short token lifetimes plus refresh tokens. What the project would build or give up: a sign-in endpoint that issues tokens, verification on every request (Spring Security supports it), a decision about where to store and how to rotate refresh tokens, and a rethink of the tile binding, which currently ties every tile URL to the server session (Chapter 32) and would need to bind to the token instead. It would also give up the immediate server-side revocation that the admin's Sessions page relies on. The browser app could keep its cookie; the two mechanisms could coexist on different endpoints.

### Exercise 37.6 ★★★ Critique the plan

Step 3 (tiles to shared storage) hides the most work: it touches URL signing, watermarking, the janitor, backups, and the atomic replace (which depends on a row lock plus a file layout under a version folder, so an object store needs an equivalent of "switch the document to version n" that readers never see half-done). A defensible reordering to reach two instances earlier: run two instances with a *shared network file system* mounted at the storage root instead of object storage, and steps 1, 2, 4, 5, and 6 (shared sessions, counters, one job runner, the same secret, a balancer). That avoids rewriting tile serving at first, at the price of a shared file system's own limits and of postponing the CDN. Another valid answer: run three instances only for the read path, with one designated instance handling uploads and replacements, so the replace logic needn't change. Any answer must name the extra work and keep sessions and counters shared before the balancer goes in front.

## Chapter 38 solutions

# Solutions: Chapter 38

### Exercise 38.1 ★ Name the pattern

- (a) A **factory method** (and the class it builds is a **value object**): `Viewer.of(authentication)` in `document/Viewer.java`.
- (b) A **template method with a callback**: `TransactionTemplate.execute(...)` runs the fixed steps (begin, commit or roll back) and calls your lambda for the variable step; used in `document/DocumentService.java`.
- (c) A **composite** of **strategies**: it groups two `SessionAuthenticationStrategy` objects and is itself used as one; in `security/SecurityConfig.java`.
- (d) An **observer** (publish-subscribe): the method is called when the framework publishes a session event; in `security/SessionMetadata.java`.

### Exercise 38.2 ★ Where does the chain stop?

The line `chain.doFilter(request, response);` at the end of `doFilterInternal` continues the chain, and it is reached only when the `if` condition is false. When the session carries the pending-change flag and the path is under `/api/` but not `/api/auth/`, the `if` body runs: it sets status `403`, writes the JSON error with `passwordChangeRequired: true`, and executes `return;`. The chain is never called, so the request stops in this filter and no controller sees it.

### Exercise 38.3 ★★ Find the strategies

The beans in `SecurityConfig` whose types are Spring Security interfaces, with a possible second implementation for each:

- `PasswordEncoder`: the delegating encoder could add a stronger algorithm's label (for example, a newer hashing function); no reason now, but the design allows it without a migration.
- `AuthenticationManager` (a `ProviderManager` around a `DaoAuthenticationProvider`): a second provider could authenticate against an identity provider, which Section 37.6 discusses; not needed today.
- `CsrfTokenRepository`: a session-based repository would keep the token on the server instead of in a cookie, but the single-page app's double-submit design needs the cookie.
- `SecurityContextRepository`: the current one stores the security context in the HTTP session; a token-based one would make authentication stateless, the design Section 37.12 weighs and rejects.
- `SessionRegistry`: a shared registry (for example, one backed by a shared store) would be needed to run several instances (Section 37.5).
- `SessionAuthenticationStrategy`: the composite already combines two; another could be added, for example to limit sessions per user.

`HttpSessionEventPublisher` is also a bean but is not a strategy: it is a publisher of events (an observer's other half).

### Exercise 38.4 ★★ Trace the state machine

In `TileGenerationService.render`: the state is created as `RUNNING`; the render job tries `compareAndSet(RUNNING, DONE)` when it finishes, and if that fails (the request had already abandoned it) it discards its staging folder and throws; on a `TimeoutException` the request tries `compareAndSet(RUNNING, ABANDONED)`, and if that fails it uses the finished result; on an `InterruptedException` it does the same `compareAndSet(RUNNING, ABANDONED)`; and the wrapper that frees the render slot reads `state.get() == RenderState.ABANDONED` to decrement the count of abandoned renders still stopping (a read, not a move). Figure 38.1 shows all three moves: start in `RUNNING`, then `DONE` or `ABANDONED`, both final. No move is missing: nothing ever leaves `DONE` or `ABANDONED`.

### Exercise 38.5 ★★★ Apply the fifth question

A worked outline; any three good answers will do. Examples. *Builder:* a builder for `TileAccess` (three fields), where the record's constructor is already clear and checked by the compiler. *Strategy:* a `TitleValidator` interface with one implementation for `validTitle`, which is a few lines in `DocumentService` and has no second candidate. *Observer:* publishing an event whenever a document is renamed so that a class can update the `updatedAt` time, when the entity's setter already calls `touch()` in one line. *Bulkhead:* a separate limiter for the audit search endpoint, which is used only by administrators and is bounded by its `size` limit of 500. In each case the simpler design is the direct one: a constructor, a private method, a call inside the setter, an existing validation. The test for over-engineering is to say what goes wrong without the pattern; if the answer is "nothing", leave it out.

### Exercise 38.6 ★★★ A decision in pattern words

One good answer, using Section 37.5 (in-memory sessions and counters versus a shared store). *Pattern in use:* a **sliding window log** rate limiter and a **reserve and compensate** rule in `TileRateLimiter` and `LoginThrottle`, plus a `SessionRegistry` that keeps sessions in memory; all are ordinary objects in one process. *Cost as the chapter states it:* per-instance state; the limiter's memory grows with the limit and needs a sweep; and a restart clears counters and sessions. *What would change:* the counters and the session registry would move behind an interface that a shared store implements (a **strategy**), the reserve step would have to be atomic in the store (for example, an atomic increment or a script), and the sweeps would be replaced by expiry in the store. *Team of five:* keep the in-memory version until the need for a second instance is real, because the shared store is another service to run, secure and back up, which a small team pays for every day; but write the counters behind a small interface now if a move is likely, since that costs a few lines and makes the later change local. Either answer earns full credit if it names the problem, the pattern, the cost and the trigger for changing.

## Chapter 39 solutions


### Exercise 39.1 ★ Name the pattern

(a) The reverse proxy or gateway at the trust boundary: nginx decides what the app is told about the caller. (b) The single-page-app fallback of client-server with a REST API: any path that isn't a real file serves `index.html`, so the client-side router handles it. (c) Secure by default, part of defense in depth: whatever no rule allows is refused. (d) Immutable versions with an atomic switch (copy-on-write): build the new version beside the old one, then move one pointer.

### Exercise 39.2 ★★ Check the layers

A model answer using two controllers at `book-m6-final`. `DocumentController` depends on `DocumentService` and `RequestActors` only, so it follows the rule: it talks to the service layer, and the service talks to the repository. `UserDirectoryController` depends on `AppUserRepository` directly, so it skips the service layer: a controller reaches straight into data access. Dependencies still point down (no class outside the `controller` package imports a controller), so the rule "never point up" holds even where the rule "one layer at a time" is relaxed. A third example: `TileController` has eleven collaborators, so it also carries coordination logic that a strict layering would put in a service. Any two controllers, with their constructor fields read from the code, earn credit.

### Exercise 39.3 ★★ Cost first

Check your answer against the "What it costs" and "When not to use it" lines of the pattern you chose. For example, for the pipeline: cost is that the whole behavior is hidden in the order of the stages, so you must read the entire chain; not to use it when there are two fixed steps that always run in the same order.

### Exercise 39.4 ★★ The twelve factors

Three pieces of in-memory or local state: (1) sessions, held by the app in memory (37.5); (2) the rate-limit counters, `TileRateLimiter` and the sign-in throttle `LoginThrottle` (37.5, 37.8); (3) the tiles on the local disk volume (37.7). A fourth is the audit throttle, which limits how often events are written and lives in a map in `AuditLogService`. All of them are correct with one instance and wrong with several; that is the connection to decision 37.11 and the plan in 37.17.

### Exercise 39.5 ★★★ Apply the framework

A model answer. Problem and constraints: users want a list of pages they have viewed; the audit log already records `PAGE_VIEWED` once per session, document, and page every 10 minutes; retention is 180 days; privacy matters because the log names users. Options: (a) query the existing audit log filtered by the user (event log pattern, cheapest, but a page opened again within 10 minutes isn't recorded, and the log is purged after 180 days); (b) a new table of reading progress written on each page view (an ordinary table, more accurate, more writes, more personal data to protect and back up); (c) keep it in the browser only, as the app already does for "resume last page" (client-side state, no server cost, lost when the browser data is cleared, and not shared across devices). Costs: as stated in each. Decision record: choose (a) if approximate history is enough and expose only the signed-in user's own events; trigger to revisit: users need exact history or history beyond 180 days, or the count of events makes queries slow. Any answer that gives three options with patterns and costs, and a trigger, earns credit.

### Exercise 39.6 ★★★ Argue against a pattern

Against: rendering is CPU-heavy but happens inside the request today, bounded by the render pool (two at once) and timeout; splitting it adds a network call, a second deployable, shared storage, and new failure modes (what does the upload return if the render service is down?). The project's records show no evaluation, and one team runs one deployable. For: rendering is the resource hog, so a separate service could be scaled and limited independently and a hostile PDF could not starve sign-ins. What would have to be true: rendering load must actually slow other requests despite the bulkhead; the team or scale must justify the operational cost. What to measure first: `sdv_render_seconds`, `sdv_render_rejected_total`, and `sdv_render_abandoned_running` (Chapter 35), plus CPU and response times of unrelated endpoints during renders. If those are healthy, the split is over-engineering. A defensible middle course, noted in section 39.6: keep the monolith and improve the modules' boundaries first.

