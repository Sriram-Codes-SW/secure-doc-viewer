# Appendix C: Exercise solutions

Try each exercise before you look. Solutions are grouped by chapter. Compiled from the per-chapter solution files.

## Chapter 1: The big picture

### Exercise 1.1 ★ Count the tiles

Columns: 2,000 / 512 = 3.9, rounded up to 4. Rows: 3,000 / 512 = 5.86, rounded up to 6. Total: 4 x 6 = 24 tiles. The tiles in the last column (2,000 - 3 x 512 = 464 px wide) and the last row (3,000 - 5 x 512 = 440 px high) are cropped shorter; the rest are 512 x 512.

### Exercise 1.2 ★ Client or server?

Hiding the download button: the client can do it, but it protects nothing. Checking token expiry: only the server, because the client could otherwise change the clock or the token. Blocking an unauthorized user: only the server.

### Exercise 1.3 ★★ Read the limits

One good answer: "A valid session can request every tile" is accepted because readers need normal reading speed (about 15 pages a minute, derived as 180 tiles per minute divided by about 12 tiles per page); the watermark makes the result traceable instead. "No text layer" is accepted because a text layer would be the copyable text the design avoids.

## Chapter 2: The command line and your files

### Exercise 2.1 ★ Explore the repository

`cd src/main/resources`, then `cat application.yml`. The file is three levels below the project folder (`src`, `main`, `resources`).

### Exercise 2.2 ★ Set and use a variable

`export TILE_SIZE=512` then `echo $TILE_SIZE` prints 512. In a new window the print is empty, because the variable existed only in the first shell process.

### Exercise 2.3 ★★ Why is `.env` ignored?

The `.gitignore` line is `.env` under the "Local secrets" comment. Without it, `git add .` could commit the database password and signing key. Git history is permanent and shared, so the secrets would be exposed and would have to be replaced.

## Chapter 3: Your first Java program

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

## Chapter 4: Classes, objects, records and interfaces

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

## Chapter 5: Collections, generics, lambdas and exceptions

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

## Chapter 6: Maven and the shape of a project

### Exercise 6.1 ★ Read the coordinates

`groupId` `com.example`, `artifactId` `secure-doc-viewer`, version `0.1.0`. It compiles for Java 25 (`<java.version>25</java.version>`).

### Exercise 6.2 ★ Build it

The JAR is `target/secure-doc-viewer.jar`, named by `<finalName>` in the build section of `pom.xml`.

### Exercise 6.3 ★★ Why pin Tomcat?

The project overrides the web server (Tomcat) version to 11.0.26 because the version Spring Boot 4.1.1 manages (11.0.24) had three critical security advisories. The override should be removed once Spring Boot manages Tomcat 11.0.25 or later, so the parent's tested versions apply again.

## Chapter 7: Git and GitHub

### Exercise 7.1 ★ Read the first commit

The oldest commit is `b6aef4e`, "Add secure document viewer: tiled rendering with signed URLs and per-viewer watermarking". Most of its files are under `src/`, chiefly `src/main/java/com/example/securedocviewer/`.

### Exercise 7.2 ★ Time travel without moving

`git show book-m0-mvp:pom.xml` and `git show book-m6-final:pom.xml`. At `book-m0-mvp` the property is `<java.version>21</java.version>`; at `book-m6-final` it is `25`. The Spring Boot parent also changes from 3.3.4 to 4.1.1.

### Exercise 7.3 ★★ Your own branch

`git switch -c exercise-7-3 book-m2-documents`, then `echo "hello" > notes.txt`, `git add notes.txt`, `git commit -m "Add notes"`. `git switch main` removes the file from the folder; `git switch exercise-7-3` brings it back, because the commit lives on that branch.

## Chapter 8: How the web works

### Exercise 8.1 ★ Match the code

A document you may not see: `404`. A 60 MB upload: `413 Content Too Large` (limit 50 MB). Too many tile requests: `429 Too Many Requests` with a `Retry-After` header.

### Exercise 8.2 ★ Read a URL

Scheme `https`, host `docs.example.com`, path `/api/tiles`, query string `token=abc`.

### Exercise 8.3 ★★ Watch the cookie

The cookie `SDV_SESSION` shows `HttpOnly` and `SameSite=Strict`. `Secure` is set only if `SESSION_COOKIE_SECURE=true`, so on plain local HTTP it is off, matching the default in Listing 8.2.

## Chapter 9: SQL and MySQL

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

## Chapter 10: Containers and Docker

### Exercise 10.1 ★ Image or container?

`mysql:8.4` is an image. `securedocs-mysql` is a container (its `container_name`). `mysql-data` is a volume.

### Exercise 10.2 ★ Start the database

`docker compose ps` shows the `mysql` service as `healthy` after the health check passes. The data is in the named volume `mysql-data`, which `docker compose down` doesn't remove, so a new container started from the same file reattaches it. (`down -v` would delete it.)

### Exercise 10.3 ★★ Read the ports

`"3306:3306"` would publish the database on every network interface of your computer, so other machines on the network could try to connect. The `127.0.0.1` prefix limits it to your own computer, in line with the file's comment: "the database is never exposed to the network."

## Chapter 11: Spring Boot foundations

### Exercise 11.1 ★ Find the session cookie

The cookie name is `SDV_SESSION`, under `server.servlet.session.cookie.name`. The line `http-only: true` makes it unreadable to JavaScript.

### Exercise 11.2 ★ List a constructor's dependencies

`DocumentService documents` and `RequestActors actors`.

### Exercise 11.3 ★★ Settings read with @Value

`BootstrapAdmin` reads `${secure-doc-viewer.bootstrap-admin.username:admin}` (default `admin`) and `${secure-doc-viewer.bootstrap-admin.password:}` (default empty, which makes the class generate a random password).

### Exercise 11.4 ★★★ Why one constructor needs @Autowired

Spring needs to know which constructor to call when a class has more than one. `KnownDevices` has a public one for Spring and a second, visible only inside its package, that tests use to pass a fixed clock. `@Autowired` on the public one resolves the choice. A class with a single constructor needs no label, because Spring uses it.

## Chapter 12: REST controllers and JSON

### Exercise 12.1 ★ Map a URL to a method

`@GetMapping("/{documentId}")`, combined with the class-level `@RequestMapping("/api/documents")`. `@PathVariable` copies the matching part of the URL into `documentId`.

### Exercise 12.2 ★ Why ResponseEntity

`delete` returns `ResponseEntity.noContent().build()` because it must send status `204` with no body. Other methods return plain objects and get `200`.

### Exercise 12.3 ★★ Renaming a JSON key

The JSON key would change from `pageCount` to `pages`, so any client code reading `pageCount` (the Angular app's model of a document) would get `undefined` until it is updated. The record component names are the contract.

### Exercise 12.4 ★★★ Why tiles skip Jackson

`byte[]` is already the final format, so Spring writes the bytes as they are and Jackson, which converts objects to JSON, isn't involved. Without `Cache-Control: no-store`, a shared cache such as a proxy could keep a tile that has one viewer's name drawn into it and serve it to someone else.

## Chapter 13: Validation, configuration properties and errors

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

## Chapter 14: Storing data with JPA and Flyway

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

## Chapter 15: Spring Security I: who are you?

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

## Chapter 16: Spring Security II: defenses

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

## Chapter 17: Files, images, PDFs and signatures

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

## Chapter 18: Testing the backend

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

## Chapter 19: TypeScript

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

## Chapter 20: Node, npm and the Angular toolchain

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

## Chapter 21: Angular components and templates

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

## Chapter 22: Talking to the backend

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

## Chapter 23: Routing, guards and forms

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

## Chapter 24: Testing the frontend

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

## Chapter 25: Milestone 0: The tiled viewer

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

## Chapter 26: Milestone 1: Accounts, roles and sessions

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

## Chapter 27: Milestone 2: Documents, ownership and audit

### Exercise 27.1 ★ Why 404

Because a 403 would confirm the document exists. With 404 an outsider can't tell a hidden document from one that was never there. The response is the only thing they ever see, so it is the whole disclosure (Section 27.2).

### Exercise 27.2 ★★ Three ways to see a document

(1) `d.visibility = :everyone`, (2) `d.owner.username = :username`, or (3) the username is among the users in `d.sharedWith`. Admins skip the query's visibility test in `DocumentService`.

### Exercise 27.3 ★★★ Unshare while reading

One good answer: the tile endpoint re-checks access on every request, so the request is answered 404 even though the signed URL hasn't expired. Signing proves the server issued the URL; only the access check knows the document is no longer shared. The pull request for this milestone records a test for exactly this case.

## Chapter 28: Milestone 3: Upload and API hardening

### Exercise 28.1 ★ A 60 MB upload

413, with a JSON body in the `{"error": "..."}` shape saying the file is too large (limit 50 MB), from `GlobalExceptionHandler.handleUploadTooLarge`.

### Exercise 28.2 ★★ Reference, not message

The message could hold SQL, file paths or other internals. The reference lets an operator find the full exception, which is logged under the same reference, without exposing it to the client.

### Exercise 28.3 ★★★ Health and env

One good answer: health reports UP, then DOWN with HTTP 503 when the database is unreachable; it reports status only. Only the health paths are `permitAll`; `anyRequest().denyAll()` and the `/api/**` rules leave every other Actuator endpoint closed, so `/actuator/env` returns 401 for an anonymous caller (as the pull request's live check records).

## Chapter 29: Milestone 4: The reading experience

### Exercise 29.1 ★ Idle state

`{ kind: 'active' }`. The timeout is 1,800 seconds and 600 remain. The warning window is min(300, 1800 / 2) = 300 seconds, and 600 is more than 300.

### Exercise 29.2 ★★ Keys while typing

Arrow keys move the text cursor in a field. If the viewer also turned pages, typing would be impossible. The shortcuts are ignored while typing and with Ctrl, Cmd or Alt held.

### Exercise 29.3 ★★★ Clamped spacing

The constructor uses `Math.min(6.0, spacing)`, so 20 becomes 6.0. A bad setting can't make the mark absurdly sparse or dense; opacity is clamped to 0.05 to 0.6 the same way.

## Chapter 30: Milestone 5: The platform and the review rounds

### Exercise 30.1 ★ Forwarded header

Any client can send the header. If the backend believed every sender, a client could pretend to be any address and reset per-address lockouts. Trust belongs to a network position (nginx's fixed address), not to the header.

### Exercise 30.2 ★★ Lockout abuse

Failing from several addresses tripped the account-wide count and blocked the real owner. The final rule applies the account-wide limit only to unrecognised devices; a device that signed in successfully within 30 days is recognised and keeps working. The cost: a correct password from a new device is refused until an admin unlocks the account.

### Exercise 30.3 ★★★ Version inside the token

Only signed fields are tamper-proof. A separate parameter could be edited to ask for an old or new render without invalidating the token. Signing it means a stale URL is refused (410) and can't be repurposed.

## Chapter 31: Milestone 6: The final state and keeping it healthy

### Exercise 31.1 ★ Polling loop

It checks every 10 ms and stops as soon as the condition is true; the 5-second deadline only matters when something is wrong.

### Exercise 31.2 ★★ Peer range

The package that declares the range (here `@angular/build` requires TypeScript `>=6.0 <6.1`) and whether the update conflicts with it. Then decide whether the update should wait for the framework upgrade.

### Exercise 31.3 ★★★ Ignore rule

One good answer, modeled on the project's MySQL rule: `- dependency-name: <library>` with `update-types: ['version-update:semver-major']` under the `ignore:` key of that ecosystem.

## Chapter 32: Security review and threat modeling

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

## Chapter 33: Deployment and TLS

### Exercise 33.1 ★ Read the ports

`mysql`: `127.0.0.1:3306` (configurable with `DB_PORT`). `web`: `127.0.0.1:8081` (`WEB_PORT`), mapped to nginx's 8080 inside the network. `tls`: `127.0.0.1:8443` (`TLS_PORT`), mapped to Caddy's 443. `app` publishes nothing; it only `expose`s 8080 to the compose network. None is `0.0.0.0` because each mapping starts with `127.0.0.1:`, so only the host machine itself can connect. Publishing on all interfaces is a deliberate go-live step (ports 80 and 443).

### Exercise 33.2 ★★ Predict the spoof

The app sees nginx's view of the TCP peer, not the client's header. In the `/api/` location nginx runs `proxy_set_header X-Forwarded-For $remote_addr;`, which overwrites the header. Also, `set_real_ip_from` trusts a forwarded address only from `172.28.0.11` (Caddy), so a client connecting directly is not believed. The app receives the client's real connection address (the Docker gateway address in a local setup), never `203.0.113.9`.

### Exercise 33.3 ★★★ Change the address

`TRUSTED_PROXY_REGEX` still names `172\.28\.0\.10`, so the app no longer trusts nginx's forwarded header: it judges every request by its own peer address, which is now nginx's. Symptom: every user appears to come from the same address, so per-IP throttling and the audit log's addresses all show nginx, and one user's failed sign-ins count against everyone (the 20-per-IP rule locks out all users sooner). Also, Caddy's fixed address `172.28.0.11` in nginx's `set_real_ip_from` is unaffected, but `ipv4_address` for `web` must still sit in the `172.28.0.0/24` subnet. The fix is to change the compose address and `TRUSTED_PROXY_REGEX` together.

## Chapter 34: Backups, restores and operations

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

## Chapter 35: Health, metrics and alerting

### Exercise 35.1 ★ Health or metric?

"Is the database reachable?" is a health check (`/actuator/health` returns `503` when it is down). "How many tiles were served today?" is a metric (`sdv_tiles_served_total`, looked at as a rate over a day). "Who opened document X?" is the audit log (`PAGE_VIEWED` events, filtered by document).

### Exercise 35.2 ★★ Read the rule

`fromAddresses(cidrs)` builds one address matcher per configured range and allows the request only if the request's address matches at least one. Judging by the connection address (the TCP peer) matters because `X-Forwarded-For` can be forged (Chapter 32); if the rule believed the header, anyone could claim to be the Prometheus server. In the compose stack nginx never proxies `/actuator/prometheus`, so it is also unreachable from outside.

### Exercise 35.3 ★★★ Design an alert

An example: fire when `increase(sdv_sign_in_total{outcome="locked"}[15m]) > 5` (the throttle window is 15 minutes). First response: open the admin audit log, filter by the lockout event type, read the `rule=` value (account+IP, IP, or account-wide) and the addresses. If one IP: likely guessing, consider blocking it at the network edge. If account-wide: expect the owner may be locked out from a new device, and decide whether to press Unlock after contacting them. Any threshold with a stated time window and a concrete first step earns credit.

## Chapter 36: Supply chain and CI

### Exercise 36.1 ★ Which job?

A failing unit test: Backend tests (or Frontend tests for TypeScript). A vulnerable Maven library: the OSV job. An outdated OS package in the nginx image: the Trivy step in the end-to-end job, which scans the built `secure-doc-viewer-web` image. A broken sign-in screen: the end-to-end job (Playwright drives a real browser against the built stack).

### Exercise 36.2 ★★ Why pin?

Git tags can be moved by whoever controls the action's repository. If `actions/checkout@v7` were moved to malicious code, your next CI run would execute it with your workflow's permissions and could read secrets or tamper with build output. A full commit SHA identifies one immutable commit, so the code that runs is the code you reviewed. The project also restricts the workflow with `permissions: contents: read` to limit the damage of any single compromised step.

### Exercise 36.3 ★★★ Write a rule

The exercise is hypothetical. The `typescript` rule ignores minor and major updates, so Dependabot would not propose the newer minor version at all; the project's Angular 22 accepts only `>=6.0 <6.1`. When a person upgrades Angular, they would upgrade Angular and TypeScript together by hand, following the supported range in the new Angular release's notes, and then adjust the ignore rule to match, for example allowing patches of the new minor. The rule exists so that TypeScript moves in step with Angular, never ahead of it.

## Chapter 37: The engineering trade-offs

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
