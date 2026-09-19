# Appendix C: Exercise solutions

Try each exercise before you look. Solutions are grouped by chapter. Compiled from the per-chapter solution files; chapters without a section have no solutions written yet.

## Chapter 1 solutions


### Exercise 1.1 ★ Count the tiles

Columns: 2,000 / 512 = 3.9, rounded up to 4. Rows: 3,000 / 512 = 5.86, rounded up to 6. Total: 4 x 6 = 24 tiles. The tiles in the last column (2,000 - 3 x 512 = 464 px wide) and the last row (3,000 - 5 x 512 = 440 px high) are cropped shorter; the rest are 512 x 512.

### Exercise 1.2 ★ Client or server?

Hiding the download button: the client can do it, but it protects nothing. Checking token expiry: only the server, because the client could otherwise change the clock or the token. Blocking an unauthorized user: only the server.

### Exercise 1.3 ★★ Read the limits

One good answer: "A valid session can request every tile" is accepted because readers need normal reading speed (about 15 pages a minute, derived as 180 tiles per minute divided by about 12 tiles per page); the watermark makes the result traceable instead. "No text layer" is accepted because a text layer would be the copyable text the design avoids.

## Chapter 2 solutions


### Exercise 2.1 ★ Explore the repository

`cd src/main/resources`, then `cat application.yml`. The file is three levels below the project folder (`src`, `main`, `resources`).

### Exercise 2.2 ★ Set and use a variable

`export TILE_SIZE=512` then `echo $TILE_SIZE` prints 512. In a new window the print is empty, because the variable existed only in the first shell process.

### Exercise 2.3 ★★ Why is `.env` ignored?

The `.gitignore` line is `.env` under the "Local secrets" comment. Without it, `git add .` could commit the database password and signing key. Git history is permanent and shared, so the secrets would be exposed and would have to be replaced.

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

## Chapter 6 solutions


### Exercise 6.1 ★ Read the coordinates

`groupId` `com.example`, `artifactId` `secure-doc-viewer`, version `0.1.0`. It compiles for Java 25 (`<java.version>25</java.version>`).

### Exercise 6.2 ★ Build it

The JAR is `target/secure-doc-viewer.jar`, named by `<finalName>` in the build section of `pom.xml`.

### Exercise 6.3 ★★ Why pin Tomcat?

The project overrides the web server (Tomcat) version to 11.0.26 because the version Spring Boot 4.1.1 manages (11.0.24) had three critical security advisories. The override should be removed once Spring Boot manages Tomcat 11.0.25 or later, so the parent's tested versions apply again.

## Chapter 7 solutions


### Exercise 7.1 ★ Read the first commit

The oldest commit is `b6aef4e`, "Add secure document viewer: tiled rendering with signed URLs and per-viewer watermarking". Most of its files are under `src/`, chiefly `src/main/java/com/example/securedocviewer/`.

### Exercise 7.2 ★ Time travel without moving

`git show book-m0-mvp:pom.xml` and `git show book-m6-final:pom.xml`. At `book-m0-mvp` the property is `<java.version>21</java.version>`; at `book-m6-final` it is `25`. The Spring Boot parent also changes from 3.3.4 to 4.1.1.

### Exercise 7.3 ★★ Your own branch

`git switch -c exercise-7-3 book-m2-documents`, then `echo "hello" > notes.txt`, `git add notes.txt`, `git commit -m "Add notes"`. `git switch main` removes the file from the folder; `git switch exercise-7-3` brings it back, because the commit lives on that branch.

## Chapter 8 solutions


### Exercise 8.1 ★ Match the code

A document you may not see: `404`. A 60 MB upload: `413 Content Too Large` (limit 50 MB). Too many tile requests: `429 Too Many Requests` with a `Retry-After` header.

### Exercise 8.2 ★ Read a URL

Scheme `https`, host `docs.example.com`, path `/api/tiles`, query string `token=abc`.

### Exercise 8.3 ★★ Watch the cookie

The cookie `SDV_SESSION` shows `HttpOnly` and `SameSite=Strict`. `Secure` is set only if `SESSION_COOKIE_SECURE=true`, so on plain local HTTP it is off, matching the default in Listing 8.2.

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

## Chapter 10 solutions


### Exercise 10.1 ★ Image or container?

`mysql:8.4` is an image. `securedocs-mysql` is a container (its `container_name`). `mysql-data` is a volume.

### Exercise 10.2 ★ Start the database

`docker compose ps` shows the `mysql` service as `healthy` after the health check passes. The data is in the named volume `mysql-data`, which `docker compose down` doesn't remove, so a new container started from the same file reattaches it. (`down -v` would delete it.)

### Exercise 10.3 ★★ Read the ports

`"3306:3306"` would publish the database on every network interface of your computer, so other machines on the network could try to connect. The `127.0.0.1` prefix limits it to your own computer, in line with the file's comment: "the database is never exposed to the network."

## Chapter 19 solutions

<!-- chapter: 19 | part: III | owner: writer-frontend | solutions -->
# Chapter 19 solutions

1. In `DocumentDetail`, `sharedWith: string[] | null` is `null` for anyone who can't manage the document (the comment: "Only present when canManage is true"). It is the only nullable property in that interface. (`DocumentSummary` has the matching `sharedWithCount: number | null`.)

2. Example solution:

   ```typescript
   type Direction = 'next' | 'previous';

   function step(current: number, d: Direction): number {
     return d === 'next' ? current + 1 : current - 1;
   }
   ```

   `step(3, 'back')` is a compile error.

3. In `app.ts`, `checkIdle` stores the state and only tests for `'expired'`, and `app.html` tests for `'warning'`, so neither breaks by itself; the new alternative simply falls through as "not a warning". The compiler complains only where code exhaustively depends on the list, and there is none here. The lesson: unions catch wrong values, not missing handling, unless you write a `switch` with a `never` check. Verify by running `npx tsc --noEmit -p tsconfig.app.json` in a scratch copy.

4. Example solution:

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

5. Example solution:

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

1. `npm run e2e` (`playwright test`). Per `playwright.config.ts`, the end-to-end tests run against a full running stack served at `http://localhost:8081` and need an admin account supplied through environment variables.

2. `^22.1.8` accepts 22.1.8 and any newer 22.x.y, so 22.9.0 is accepted. 23.0.0 is not (a new major).

3. `secure: false` turns off certificate verification for the proxy's connection to the target. In development the target is `http://localhost:8080` on your own machine, so nothing travels over a network. A production proxy would forward across a real network, where skipping verification would allow someone to impersonate the backend. (Note that production here uses nginx over a private container network, not this dev proxy.)

4. A `dist/` folder appears, containing `dist/frontend/browser/` with `index.html` and JavaScript and CSS files whose names include a content hash, for example `main-XXXXXXXX.js` (the exact names vary). The Dockerfile copies that `browser` folder into nginx.

5. Without a lock file, `npm install` may pick the newest 6.0.x available (for example a later patch than the one you tested with) and writes a new lock file. `npm ci` refuses to run because its whole purpose is to install exactly what a lock file records; with none, there is nothing to reproduce.

## Chapter 21 solutions

<!-- chapter: 21 | part: III | owner: writer-frontend | solutions -->
# Chapter 21 solutions

1. `@if (sessionService.isAdmin()) { ... }`. It reads `sessionService.isAdmin`, a computed signal in `SessionService` that is true when the signed-in user's role is `ADMIN`.

2. Every screen whose CSS uses `var(--accent)`: buttons, the active navigation underline, the Manage link in the viewer header, and the idle banner tint. Only the tokens changed, so no component file needs editing. (Remember the dark theme redefines `--accent` in its own block.)

3. Example:

   ```typescript
   readonly showHint = signal(false);
   ```

   ```html
   <button (click)="showHint.update((v) => !v)">Toggle hint</button>
   @if (showHint()) {
     <p>This is the hint.</p>
   }
   ```

4. Compute with any WCAG contrast checker. Light: `#5d6470` on `#ffffff` and on `#f6f7f9`; dark: `#9aa0aa` on `#191b1f` and on `#101114`. The comment in `styles.css` states the project's aim of at least 4.5:1 on `--bg` and `--surface`; check each pair yourself and record the ratios.

5. Tracking by position would treat "the third card" as the same item before and after filtering, so Angular would reuse and rewrite existing cards' contents. Tracking by `documentId` gives every card a stable identity, so Angular keeps the cards that remain, removes the ones that disappear, and doesn't rebuild the rest.

## Chapter 22 solutions

<!-- chapter: 22 | part: III | owner: writer-frontend | solutions -->
# Chapter 22 solutions

1. `GET` (list, get, findUsers, getTileUrls), `POST` (upload), `PATCH` (update), `PUT` (replaceFile, transferOwnership, share), and `DELETE` (delete, unshare). The file is sent by `upload` (`POST`, multipart `FormData`) and by `replaceFile` (`PUT`).

2. Nothing special: `/api/auth/login` is in `AUTH_PROBES`, so the 401 is passed to the login form's own error handler, which shows "Incorrect username or password." Redirecting to `/login` would be pointless (the reader is already there) and would wipe what they typed.

3. Yes: the header `X-XSRF-TOKEN` carries the same value as the `XSRF-TOKEN` cookie. A page on another origin can't read that cookie, so it can't produce the header.

4. It applies only when a 429 or 503 arrives without a usable `Retry-After` header: the viewer then waits that many seconds (5 by default) before asking for fresh tile URLs.

5. If the reload returned the same version that was reported gone (for example because the tiles were unavailable for another reason), the viewer would reload, refetch the tiles, receive 410 again, and repeat indefinitely, hammering the server and spending the rate-limit budget. The check stops after one reload and shows an error instead.

## Chapter 23 solutions

<!-- chapter: 23 | part: III | owner: writer-frontend | solutions -->
# Chapter 23 solutions

1. `admin` uses `roleGuard('ADMIN')`. `documents/upload` uses `roleGuard('PUBLISHER', 'ADMIN')`. Signed-in only (`authGuard`): `documents`, `documents/:documentId/manage`, `viewer/:documentId`, and `account`. `login` has no guard.

2. `/login?returnUrl=%2Fdocuments` (the guard passes the attempted URL, percent-encoded, in `returnUrl`). `auth.guard.spec.ts` checks the same behavior with `/viewer/abc?page=3`.

3. The guard only decides which screen the browser shows. The document's tiles come from the API, which checks on every request that the signed-in user may see the document (the viewer shows "no longer available" on a 404). Removing the guard would let a signed-out visitor see the viewer's empty shell, but the API would still refuse them.

4. Example:

   ```typescript
   {
     path: 'about',
     loadComponent: () => import('./features/about/about.component').then((m) => m.AboutComponent),
   },
   ```

   placed before the catch-all `**` route (routes match in order).

5. Typing "1" or "-" in the page box would also trigger the key actions: "-" would zoom out, and arrow keys, meant to move the text cursor, would turn pages. Worse, `preventDefault()` would block the character from being typed at all.

## Chapter 24 solutions

<!-- chapter: 24 | part: III | owner: writer-frontend | solutions -->
# Chapter 24 solutions

1. Add inside the `describe` block:

   ```typescript
   it('is active when only a sixth of a long timeout has passed', () => {
     expect(idleState(t0 + 100_000, t0, 600)).toEqual({ kind: 'active' });
   });
   ```

   With 600 seconds of timeout, 500 seconds are left; the warning window is the smaller of 300 seconds and half the timeout (300), so 500 is outside it. Run with `npm test` in `frontend/`.

2. `expectOne` fails the test, reporting that more than one matching request was found. It asserts *exactly* one, so it also catches accidental duplicate requests.

3. Example:

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

4. The two themes use different color values (Listing 21.5), so text that has enough contrast in one palette may not in the other. Checking only the light theme would leave the dark palette unverified.

5. The bug was in how nginx built the `X-Forwarded-For` header before passing the request to the backend, and the backend's decision to trust it. A Vitest spec runs neither. Only a test that sends real requests through the whole stack can see the header being overwritten (or not). A unit test could still add a check on the frontend side that no code sets that header itself, but that is not where the vulnerability was.

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


### Exercise 35.1 ★ Health or metric?

"Is the database reachable?" is a health check (`/actuator/health` returns `503` when it is down). "How many tiles were served today?" is a metric (`sdv_tiles_served_total`, looked at as a rate over a day). "Who opened document X?" is the audit log (`PAGE_VIEWED` events, filtered by document).

### Exercise 35.2 ★★ Read the rule

`fromAddresses(cidrs)` builds one address matcher per configured range and allows the request only if the request's address matches at least one. Judging by the connection address (the TCP peer) matters because `X-Forwarded-For` can be forged (Chapter 32); if the rule believed the header, anyone could claim to be the Prometheus server. In the compose stack nginx never proxies `/actuator/prometheus`, so it is also unreachable from outside.

### Exercise 35.3 ★★★ Design an alert

An example: fire when `increase(sdv_sign_in_total{outcome="locked"}[15m]) > 5` (the throttle window is 15 minutes). First response: open the admin audit log, filter by the lockout event type, read the `rule=` value (account+IP, IP, or account-wide) and the addresses. If one IP: likely guessing, consider blocking it at the network edge. If account-wide: expect the owner may be locked out from a new device, and decide whether to press Unlock after contacting them. Any threshold with a stated time window and a concrete first step earns credit.

## Chapter 36 solutions


### Exercise 36.1 ★ Which job?

A failing unit test: Backend tests (or Frontend tests for TypeScript). A vulnerable Maven library: the OSV job. An outdated OS package in the nginx image: the Trivy step in the end-to-end job, which scans the built `secure-doc-viewer-web` image. A broken sign-in screen: the end-to-end job (Playwright drives a real browser against the built stack).

### Exercise 36.2 ★★ Why pin?

Git tags can be moved by whoever controls the action's repository. If `actions/checkout@v7` were moved to malicious code, your next CI run would execute it with your workflow's permissions and could read secrets or tamper with build output. A full commit SHA identifies one immutable commit, so the code that runs is the code you reviewed. The project also restricts the workflow with `permissions: contents: read` to limit the damage of any single compromised step.

### Exercise 36.3 ★★★ Write a rule

Today the `typescript` rule ignores minor and major updates, so Dependabot would not propose 6.1 at all (Angular 22 accepts only `>=6.0 <6.1`). At the Angular 23 upgrade a person would upgrade Angular first (or together with TypeScript) by hand, follow Angular's supported version range, and then update the ignore rule so it matches the new range, for example allowing patches of the new minor. The rule exists so that TypeScript moves in step with Angular, never ahead of it.

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

