# Appendix C: Exercise solutions

Try each exercise before you look. Solutions are grouped by chapter. Compiled from the per-chapter solution files; chapters without a section have no solutions written yet.

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

`export TILE_SIZE=512` then `echo $TILE_SIZE` prints 512. In a new window the print is empty, because the variable existed only in the first shell process.

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

On macOS or Linux, `lsof -i :8080` shows a line for the Python process with its PID; on Windows, `Get-NetTCPConnection -LocalPort 8080 | Select-Object OwningProcess` shows the PID, and `Get-Process -Id <pid>` names the program. Press <kbd>Ctrl</kbd>+<kbd>C</kbd> in the window running the server to stop it, and rerun the command: it prints nothing.

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

<!-- chapter: 11 | part: II | owner: writer-backend | tag: book-m6-final | status: draft -->
# Solutions: Chapter 11

### Exercise 11.1 ★ Find the session cookie

The cookie name is `SDV_SESSION`, under `server.servlet.session.cookie.name`. The line `http-only: true` makes it unreadable to JavaScript.

### Exercise 11.2 ★ List a constructor's dependencies

`DocumentService documents` and `RequestActors actors`.

### Exercise 11.3 ★★ Settings read with @Value

`BootstrapAdmin` reads `${secure-doc-viewer.bootstrap-admin.username:admin}` (default `admin`) and `${secure-doc-viewer.bootstrap-admin.password:}` (default empty, which makes the class generate a random password).

### Exercise 11.4 ★★★ Why one constructor needs @Autowired

Spring needs to know which constructor to call when a class has more than one. `KnownDevices` has a public one for Spring and a second, visible only inside its package, that tests use to pass a fixed clock. `@Autowired` on the public one resolves the choice. A class with a single constructor needs no label, because Spring uses it.

## Chapter 12 solutions

<!-- chapter: 12 | part: II | owner: writer-backend | tag: book-m6-final | status: draft -->
# Solutions: Chapter 12

### Exercise 12.1 ★ Map a URL to a method

`@GetMapping("/{documentId}")`, combined with the class-level `@RequestMapping("/api/documents")`. `@PathVariable` copies the matching part of the URL into `documentId`.

### Exercise 12.2 ★ Why ResponseEntity

`delete` returns `ResponseEntity.noContent().build()` because it must send status `204` with no body. Other methods return plain objects and get `200`.

### Exercise 12.3 ★★ Renaming a JSON key

The JSON key would change from `pageCount` to `pages`, so any client code reading `pageCount` (the Angular app's model of a document) would get `undefined` until it is updated. The record component names are the contract.

### Exercise 12.4 ★★★ Why tiles skip Jackson

`byte[]` is already the final format, so Spring writes the bytes as they are and Jackson, which converts objects to JSON, isn't involved. Without `Cache-Control: no-store`, a shared cache such as a proxy could keep a tile that has one viewer's name drawn into it and serve it to someone else.

## Chapter 13 solutions

<!-- chapter: 13 | part: II | owner: writer-backend | tag: book-m3-hardening | status: draft -->
# Solutions: Chapter 13

### Exercise 13.1 ★ Stop an oversized username

`@Size(max = 64)` on `username` in `LoginRequest`, applied because the controller parameter is marked `@Valid`. A failure becomes a `400`.

### Exercise 13.2 ★ The unexpected-error response

`{"error": "Something went wrong on our side. Reference: <8 characters>."}` with status `500`. The message and the reference are safe to show; the exception details stay in the server log next to the same reference.

### Exercise 13.3 ★★ Add a 409 exception

Add `public class ConflictException extends RuntimeException` with a constructor taking a message, then in `GlobalExceptionHandler` add `@ExceptionHandler(ConflictException.class)` returning `error(HttpStatus.CONFLICT, e.getMessage())`. The message text comes from whoever throws the exception.

### Exercise 13.4 ★★ Start without a secret

Unset `SIGNING_SECRET` and start the app from the project folder. If a `.env` file supplies the value, remove it there too, because the project imports `.env` as a fallback. Startup stops with the message `SIGNING_SECRET must be set`, from `@NotBlank` on the `signingSecret` field of `ViewerProperties`.

### Exercise 13.5 ★★★ Why the upload limit appears three times

The server limit is `spring.servlet.multipart.max-file-size`, the handler message uses `MAX_UPLOAD_MB`, and the frontend checks the size before sending. If the frontend allowed 100 MB while the server allowed 50 MB, users would wait through a long upload only to be refused with `413`; the three should agree so the refusal comes early.

## Chapter 14 solutions

<!-- chapter: 14 | part: II | owner: writer-backend | tag: book-m2-documents | status: draft -->
# Solutions: Chapter 14

### Exercise 14.1 ★ Store roles as text

`@Enumerated(EnumType.STRING)`. Storing the name (`READER`) means reordering or inserting enum constants can never change what an existing row means; a stored number would silently shift.

### Exercise 14.2 ★ Indexes on the audit table

`V2__documents_shares_audit.sql` creates `audit_event` and four indexes: `ix_audit_event_time` (occurred_at), `ix_audit_event_user_time` (username, occurred_at), `ix_audit_event_document_time` (document_id, occurred_at) and `ix_audit_event_type_time` (event_type, occurred_at).

### Exercise 14.3 ★★ A query with no SQL

From the method name. Spring Data reads `findByUsername` and generates `select ... where username = ?` when it builds the repository bean at startup.

### Exercise 14.4 ★★ TransactionTemplate versus @Transactional

`DocumentService` renders PDFs, which is slow. A transaction holds a database connection, so it must wrap only the fast database work; `TransactionTemplate` lets the service choose exactly which code runs inside the transaction, where an annotation would wrap the whole method.

### Exercise 14.5 ★★★ Editing a migration that already ran

Flyway stores a checksum of each applied migration. It detects the changed file and refuses to start. The correct change is a new migration, for example `V4__...sql`, containing the `ALTER` statements.

## Chapter 15 solutions

<!-- chapter: 15 | part: II | owner: writer-backend | tag: book-m1-accounts | status: draft -->
# Solutions: Chapter 15

### Exercise 15.1 ★ The unreadable session cookie

The setting `http-only: true` (it is configuration, not an annotation) under `server.servlet.session.cookie` in `application.yml`.

### Exercise 15.2 ★ What the database stores

A salted BCrypt hash, prefixed with the algorithm name such as `{bcrypt}`, in the `password_hash` column.

### Exercise 15.3 ★★ Thirty emoji versus 72 bytes

No. Each emoji takes 4 bytes in UTF-8, so 30 emoji are 120 bytes, above the 72-byte limit; `fitsBcrypt` counts bytes, not characters.

### Exercise 15.4 ★★ The role string Spring builds

`ROLE_PUBLISHER`. Spring adds the `ROLE_` prefix.

### Exercise 15.5 ★★★ Cookie or browser token

A session cookie lets the server end a session instantly and keeps the id away from JavaScript when `httpOnly`; but the server must store sessions and browsers send the cookie automatically, which is why CSRF protection is needed. A token in the browser needs no server-side storage, but it can't be revoked easily before it expires and JavaScript can read it. The project chose the session cookie for instant revocation and to keep the id out of scripts' reach.

## Chapter 16 solutions

<!-- chapter: 16 | part: II | owner: writer-backend | tag: book-m6-final | status: draft -->
# Solutions: Chapter 16

### Exercise 16.1 ★ The deny-all default

Under `/api/**` it falls under the `authenticated()` rule, so a signed-in user reaches it. Outside `/api` and the listed paths, `anyRequest().denyAll()` refuses it. Either way, an endpoint with a role requirement gets none until you add a rule.

### Exercise 16.2 ★ Why the CSRF cookie is readable

The app's JavaScript must copy the CSRF token into a header, so it has to read the cookie. The session cookie is only ever sent back by the browser, so scripts have no need to read it, and hiding it protects it from theft.

### Exercise 16.3 ★★ Count before checking

So parallel attempts can't all pass the check before any is counted. Counting first, and handing the count back on success, bounds the number of guesses to the limit.

### Exercise 16.4 ★★ Someone else's document

`404`. A `403` would confirm the id exists; `404` reveals nothing.

### Exercise 16.5 ★★★ Trusting forwarded headers

With `native` on a directly reachable server, any client could send a forged `X-Forwarded-For` header and the server would believe it, so an attacker could pick a new fake address for every attempt and avoid the per-address throttle.

## Chapter 17 solutions

<!-- chapter: 17 | part: II | owner: writer-backend | tag: book-m6-final | status: draft -->
# Solutions: Chapter 17

### Exercise 17.1 ★ Count the tiles

`tileCount(1000, 512)` is `(1000 + 511) / 512 = 1511 / 512 = 2` with integer division.

### Exercise 17.2 ★ Cropping edge tiles

So that reassembling every tile at `(col * tileSize, row * tileSize)` reproduces the page exactly, with no padding and no seams.

### Exercise 17.3 ★★ Tamper with a token

`SignedUrlService.verifyAndDecode` recomputes the HMAC, finds a mismatch and throws `InvalidTokenException("Signature mismatch ...")`, which `GlobalExceptionHandler` maps to `401`.

### Exercise 17.4 ★★ Release in a finally block

So the permit is returned even if the work throws; otherwise permits would leak and the server would eventually refuse every request.

### Exercise 17.5 ★★★ Two checks on a copied link

Any two of: the token is bound to the session it was issued to, so a different browser's session doesn't match (`InvalidTokenException`); the friend has no signed-in session at all (`401`); the per-user rate limit; the current access check on the document.

## Chapter 18 solutions

<!-- chapter: 18 | part: II | owner: writer-backend | tag: book-m6-final | status: draft -->
# Solutions: Chapter 18

### Exercise 18.1 ★ Run the backend tests

`mvn test` runs every test class under `src/test/java`. `MySqlIntegrationTest` is skipped when Docker isn't running, because of `@Testcontainers(disabledWithoutDocker = true)`.

### Exercise 18.2 ★ Extend a tile-count test

`assertEquals(1, TileGrid.tileCount(512, 512));` since `(512 + 511) / 512 = 1`.

### Exercise 18.3 ★★ Test the sign-in requirement

`mvc.perform(get("/api/documents")).andExpect(status().isUnauthorized());` in a `@SpringBootTest` class with `@AutoConfigureMockMvc` and `@ActiveProfiles("test")`. `apiRequiresSignIn` also checks the JSON error text.

### Exercise 18.4 ★★ A fresh context for CSRF

The `csrf()` test helper permanently replaces the CSRF filter's repository in the context where it runs, so real `XSRF-TOKEN` cookies would never be written. A fresh context (`@DirtiesContext`) keeps that class unaffected by other tests.

### Exercise 18.5 ★★★ Removing the start latch

Without the latch, threads start as they are created and may finish one by one, so the requests might not overlap and the code could pass without proving anything about parallel attempts. The latch makes them collide.

## Chapter 19 solutions

<!-- chapter: 19 | part: III | owner: writer-frontend | solutions -->
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

## Chapter 20 solutions

<!-- chapter: 20 | part: III | owner: writer-frontend | solutions -->
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

<!-- chapter: 21 | part: III | owner: writer-frontend | solutions -->
# Chapter 21 solutions

### Exercise 21.1 ★ The Admin link condition

`@if (sessionService.isAdmin()) { ... }`. It reads `sessionService.isAdmin`, a computed signal in `SessionService` that is true when the signed-in user's role is `ADMIN`.

### Exercise 21.2 ★ Change the accent colour

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

### Exercise 21.4 ★★ Check colour contrast

Compute with any WCAG contrast checker. Light: `#5d6470` on `#ffffff` and on `#f6f7f9`; dark: `#9aa0aa` on `#191b1f` and on `#101114`. The comment in `styles.css` states the project's aim of at least 4.5:1 on `--bg` and `--surface`; check each pair yourself and record the ratios.

### Exercise 21.5 ★★★ Why track by id

Tracking by position would treat "the third card" as the same item before and after filtering, so Angular would reuse and rewrite existing cards' contents. Tracking by `documentId` gives every card a stable identity, so Angular keeps the cards that remain, removes the ones that disappear, and doesn't rebuild the rest.

## Chapter 22 solutions

<!-- chapter: 22 | part: III | owner: writer-frontend | solutions -->
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

## Chapter 23 solutions

<!-- chapter: 23 | part: III | owner: writer-frontend | solutions -->
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

## Chapter 24 solutions

<!-- chapter: 24 | part: III | owner: writer-frontend | solutions -->
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

The two themes use different color values (Listing 21.5), so text that has enough contrast in one palette may not in the other. Checking only the light theme would leave the dark palette unverified.

### Exercise 24.5 ★★★ Why only end-to-end catches it

The bug was in how nginx built the `X-Forwarded-For` header before passing the request to the backend, and the backend's decision to trust it. A Vitest spec runs neither. Only a test that sends real requests through the whole stack can see the header being overwritten (or not). A unit test could still add a check on the frontend side that no code sets that header itself, but that is not where the vulnerability was.

## Chapter 25 solutions

<!-- chapter: 25 | part: IV | owner: writer-app | tag: book-m0-mvp | status: draft -->
# Solutions for Chapter 25

### Exercise 25.1 ★ Count the tiles

`tileCount(1240, 256)` is (1240 + 255) / 256 = 5 columns using integer division. Four full columns cover
1,024 pixels, so the last column is 1240 - 1024 = 216 pixels wide. `tileCount(1754, 256)` is
(1754 + 255) / 256 = 7 rows; six full rows cover 1,536 pixels, so the last row is 218 pixels tall.

### Exercise 25.2 ★★ Tamper with a token

The client receives HTTP 401 with a JSON body `{"error": "..."}`. At `book-m0-mvp`,
`GlobalExceptionHandler` maps `InvalidTokenException` to 401. A token whose payload was altered fails
the signature comparison ("Signature mismatch"). The signature is checked first so that data nobody
signed never reaches the parsing code, and the comparison is constant-time so response timing
reveals nothing about how much matched.

### Exercise 25.3 ★★★ Two independent checks

One good answer: the token proves that the server issued it for one tile until a fixed time, but not that the
person is still allowed to read. If the user signs out or the session times out, tokens issued under
that session are still unexpired, so `TileController` also calls `requireValidSession`. The two
failures (bad token, dead session) stay distinguishable. Later milestones add a third question, whether the
user may still see the document at all (Chapter 27).

## Chapter 26 solutions

<!-- chapter: 26 | part: IV | owner: writer-app | tag: book-m1-accounts | status: draft -->
# Solutions for Chapter 26

### Exercise 26.1 ★ Who may upload?

PUBLISHER and ADMIN (`hasAnyRole("PUBLISHER", "ADMIN")`). A signed-in READER gets HTTP 403 with a JSON
body in the `{"error": "..."}` shape: `SecurityErrorResponses` writes FORBIDDEN through the access-denied
handler. An anonymous caller gets 401 instead.

### Exercise 26.2 ★★ Why deny by default

Rules match top to bottom, and the last one catches everything nobody thought of. If it were
`permitAll()`, every route added later and not listed would be public until someone noticed. With
`denyAll()`, a forgotten route is closed, and you find out in testing rather than in an incident.

### Exercise 26.3 ★★★ Inspect the cookies

One good answer: the session cookie is httpOnly, so JavaScript can't read it, while the CSRF cookie
(`XSRF-TOKEN`) is created with `withHttpOnlyFalse()` so Angular's `HttpClient` can copy its value into a
request header. That is the double-submit pattern: another site can make your browser send the cookies
but can't read the CSRF value to put it in the header. Both cookies are `SameSite=Strict`
(`SecurityConfig.csrfTokenRepository`).

## Chapter 27 solutions

<!-- chapter: 27 | part: IV | owner: writer-app | tag: see chapter | status: draft -->
# Solutions for Chapter 27

### Exercise 27.1 ★ Why 404

Because a 403 would confirm the document exists. With 404 an outsider can't tell a hidden document from one that was never there. The response is the only thing they ever see, so it is the whole disclosure (Section 27.2).

### Exercise 27.2 ★★ Three ways to see a document

(1) `d.visibility = :everyone`, (2) `d.owner.username = :username`, or (3) the username is among the users in `d.sharedWith`. Admins skip the query's visibility test in `DocumentService`.

### Exercise 27.3 ★★★ Unshare while reading

One good answer: the tile endpoint re-checks access on every request, so the request is answered 404 even though the signed URL hasn't expired. Signing proves the server issued the URL; only the access check knows the document is no longer shared. The pull request for this milestone records a test for exactly this case.


## Chapter 28 solutions

<!-- chapter: 28 | part: IV | owner: writer-app | tag: see chapter | status: draft -->
# Solutions for Chapter 28

### Exercise 28.1 ★ A 60 MB upload

413, with a JSON body in the `{"error": "..."}` shape saying the file is too large (limit 50 MB), from `GlobalExceptionHandler.handleUploadTooLarge`.

### Exercise 28.2 ★★ Reference, not message

The message could hold SQL, file paths or other internals. The reference lets an operator find the full exception, which is logged under the same reference, without exposing it to the client.

### Exercise 28.3 ★★★ Health and env

One good answer: health reports UP, then DOWN with HTTP 503 when the database is unreachable; it reports status only. Only the health paths are `permitAll`; `anyRequest().denyAll()` and the `/api/**` rules leave every other Actuator endpoint closed, so `/actuator/env` returns 401 for an anonymous caller (as the pull request's live check records).


## Chapter 29 solutions

<!-- chapter: 29 | part: IV | owner: writer-app | tag: see chapter | status: draft -->
# Solutions for Chapter 29

### Exercise 29.1 ★ Idle state

`{ kind: 'active' }`. The timeout is 1,800 seconds and 600 remain. The warning window is min(300, 1800 / 2) = 300 seconds, and 600 is more than 300.

### Exercise 29.2 ★★ Keys while typing

Arrow keys move the text cursor in a field. If the viewer also turned pages, typing would be impossible. The shortcuts are ignored while typing and with Ctrl, Cmd or Alt held.

### Exercise 29.3 ★★★ Clamped spacing

The constructor uses `Math.min(6.0, spacing)`, so 20 becomes 6.0. A bad setting can't make the mark absurdly sparse or dense; opacity is clamped to 0.05 to 0.6 the same way.


## Chapter 30 solutions

<!-- chapter: 30 | part: IV | owner: writer-app | tag: see chapter | status: draft -->
# Solutions for Chapter 30

### Exercise 30.1 ★ Forwarded header

Any client can send the header. If the backend believed every sender, a client could pretend to be any address and reset per-address lockouts. Trust belongs to a network position (nginx's fixed address), not to the header.

### Exercise 30.2 ★★ Lockout abuse

Failing from several addresses tripped the account-wide count and blocked the real owner. The final rule applies the account-wide limit only to unrecognised devices; a device that signed in successfully within 30 days is recognised and keeps working. The cost: a correct password from a new device is refused until an admin unlocks the account.

### Exercise 30.3 ★★★ Version inside the token

Only signed fields are tamper-proof. A separate parameter could be edited to ask for an old or new render without invalidating the token. Signing it means a stale URL is refused (410) and can't be repurposed.


## Chapter 31 solutions

<!-- chapter: 31 | part: IV | owner: writer-app | tag: book-m6-final | status: expanded -->
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

## Chapter 33 solutions


### Exercise 33.1 ★ Read the ports

`mysql`: `127.0.0.1:3306` (configurable with `DB_PORT`). `web`: `127.0.0.1:8081` (`WEB_PORT`), mapped to nginx's 8080 inside the network. `tls`: `127.0.0.1:8443` (`TLS_PORT`), mapped to Caddy's 443. `app` publishes nothing; it only `expose`s 8080 to the compose network. None is `0.0.0.0` because each mapping starts with `127.0.0.1:`, so only the host machine itself can connect. Publishing on all interfaces is a deliberate go-live step (ports 80 and 443).

### Exercise 33.2 ★★ Predict the spoof

The app sees nginx's view of the TCP peer, not the client's header. In the `/api/` location nginx runs `proxy_set_header X-Forwarded-For $remote_addr;`, which overwrites the header. Also, `set_real_ip_from` trusts a forwarded address only from `172.28.0.11` (Caddy), so a client connecting directly is not believed. The app receives the client's real connection address (the Docker gateway address in a local setup), never `203.0.113.9`.

### Exercise 33.3 ★★★ Change the address

`TRUSTED_PROXY_REGEX` still names `172\.28\.0\.10`, so the app no longer trusts nginx's forwarded header: it judges every request by its own peer address, which is now nginx's. Symptom: every user appears to come from the same address, so per-IP throttling and the audit log's addresses all show nginx, and one user's failed sign-ins count against everyone (the 20-per-IP rule locks out all users sooner). Also, Caddy's fixed address `172.28.0.11` in nginx's `set_real_ip_from` is unaffected, but `ipv4_address` for `web` must still sit in the `172.28.0.0/24` subnet. The fix is to change the compose address and `TRUSTED_PROXY_REGEX` together.

## Chapter 34 solutions


### Exercise 34.1 ★ Name the state

- Login sessions: no. They live in memory and users simply sign in again.
- Tiles: yes (`app-storage` volume).
- Audit log: yes (in MySQL).
- `.env` file: yes, kept securely with the backup; `SIGNING_SECRET` keys the recognised-device hashes.
- Source PDFs: they don't exist after ingest, so there is nothing to back up; the tiles hold the content.

### Exercise 34.2 ★★ Break the match

10:00 the dump records document D at version 1. 10:01 the replace renders version 2 and, on commit, deletes version 1's tiles. 10:02 the archive contains only version 2. After a restore the database says D uses version 1, but `D/v1/...` isn't on disk. Each tile request finds nothing, so readers see blank pages or errors for D. The janitor's safety net (it won't prune other versions while the current one is missing) keeps the situation from getting worse, but the content is gone. The runbook prevents this by stopping the app between the two captures.

### Exercise 34.3 ★★★ Design the drill

A good answer names: a scratch environment (separate compose project, ports, and volumes, never production); the steps (start MySQL, load the dump, unpack the tiles, start the app); proof (sign in as a known account, open several documents including one that was replaced, confirm every tile loads, check the audit log's newest event date); an owner; and a schedule (for example before go-live and then quarterly, and after any change to the backup commands).

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

A failing unit test: Backend tests (or Frontend tests for TypeScript). A vulnerable Maven library: the OSV job. An outdated OS package in the nginx image: the Trivy step in the end-to-end job, which scans the built `secure-doc-viewer-web` image. A broken sign-in screen: the end-to-end job (Playwright drives a real browser against the built stack).

### Exercise 36.2 ★★ Why pin?

Git tags can be moved by whoever controls the action's repository. If `actions/checkout@v7` were moved to malicious code, your next CI run would execute it with your workflow's permissions and could read secrets or tamper with build output. A full commit SHA identifies one immutable commit, so the code that runs is the code you reviewed. The project also restricts the workflow with `permissions: contents: read` to limit the damage of any single compromised step.

### Exercise 36.3 ★★★ Write a rule

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

