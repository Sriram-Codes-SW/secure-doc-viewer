# Secure Document Viewer

> **Status: archived, read-only snapshot.** This repository was published as a finished reference and is not
> maintained. It does not accept issues or pull requests: fork it to continue. The companion book, "Building a
> Secure Document Viewer", is in [`book/`](book/) (see [About this edition](book/front-matter/00-about-this-edition.md)).

![Building a Secure Document Viewer: a free textbook with 41 chapters, 242 exercises and 81 diagrams, by Claude (Anthropic)](book/build/preview-1200x630.png)

A small Spring Boot service that serves PDF documents to authenticated viewers **without ever
handing out the PDF**. Pages are rasterized server-side, sliced into image tiles, and delivered
one tile at a time through short-lived HMAC-signed URLs that are bound to a login session and
watermarked per viewer at request time. The browser reassembles the tiles into a page by placing
each one at its position, as a positioned element with the tile as its background image.

This is the architecture commercial e-magazine and flipbook readers use. The project exists to
implement that pattern end to end and to be explicit about what it does and does not achieve.

---

## The companion book

The `book/` folder holds a beginner-to-production textbook that builds this application step by step: 41 chapters,
242 exercises with solutions, and 81 diagrams. The PDF, EPUB and HTML editions are attached to the [Release](https://github.com/Sriram-Codes-SW/secure-doc-viewer/releases/tag/book-v1.0),
and the web edition can be read online at <https://sriram-codes-sw.github.io/secure-doc-viewer/>. To build the editions yourself,
run `python book/build/build.py` (Docker is required; see `book/build/build.py`).

- **History.** The seven milestone tags (`book-m0-mvp` to `book-m6-final`) are the code the book's listings come from.
  The pull requests (#1 to #14) were recreated in this repository after personal data was removed from the commit
  history, so the dates GitHub shows on them are the recreation dates; the commits keep their original dates.
- **Review records.** `book/_team/` keeps the records of how the book was reviewed and checked (see its README).
- **Honest limits.** The book, and the [Limitations](#limitations) section below, say what the application does not
  defend against. It is a reference project, not production software, and it comes with no warranty.

## Why this design

A naive "protected" viewer hides the download button in CSS and blocks right-click in JavaScript.
Both live entirely in the browser, so both are undone in about ten seconds with DevTools. The
protection here is structural instead — it lives on the server, where the client cannot reach it:

| Concern | How it is handled |
|---|---|
| "Save the PDF" | The PDF stops existing as a servable file after ingest. Only disconnected PNG tiles remain on disk. There is no endpoint that returns a whole page, let alone a whole document. |
| "Copy the tile URL" | Every tile URL carries an HMAC-SHA256 signature over document + page + row + col + render version + session binding + expiry. After a PDF is replaced, URLs for the old render answer `410` (so a page is never a mix of old and new tiles) and the viewer reloads the new one with an "updated" notice. Change any field and the signature fails. |
| "Reuse the URL later" | Tokens expire (default 120s). Expiry is inside the signed payload, so it cannot be edited. |
| "Share the URL with someone else" | Tokens carry a keyed hash of the issuing session (never the session id itself), and a tile request must also present that session's httpOnly cookie. A URL pasted into another browser or account gets 401. |
| "Keep using URLs after logout" | The session is checked on every tile request, independently of token expiry, so signing out — or an admin revoking the session — kills every outstanding URL immediately. |
| "Open a document that wasn't shared with me" | Every document has an owner and a visibility: PRIVATE (owner + users it is shared with) or EVERYONE. Anyone else gets `404` — not `403` — so they can't even tell it exists. The check runs on the list, the manifest, when tile URLs are issued, and again on every single tile request, so unsharing or deleting a document cuts off pages that are already open. |
| "Just sign in as someone else" | Real accounts: username + BCrypt password, created by an admin (no self-signup). Failed sign-ins are throttled per account, per client IP and account-wide from unrecognised devices (`429` + `Retry-After`; see [Sign-in lockout](#sign-in-lockout)). Accounts created or reset by an admin must choose their own password before doing anything else. Roles — READER, PUBLISHER, ADMIN — are enforced server-side on every endpoint. |
| "Script every tile of every page in one burst" | `/api/tiles` is rate-limited per user (default 180 tile requests per 60s window; with 512 px tiles a letter page is ~12 tiles, so about 15 pages a minute). A valid signature, unexpired token, and live session still only get throttled access — bulk harvesting becomes slow and boundable instead of instant. Throttled requests get `429` with a `Retry-After` header, and the viewer shows a countdown and loads the rest of the page when the window allows. |
| "Screenshot it anyway" | Not prevented — see [Limitations](#limitations). Every served tile is watermarked with the requesting viewer's identity and a UTC timestamp, so a leaked capture is attributable. The mark (viewer on one line; UTC timestamp and a six-character trace code on the next — type the code into the audit log's trace filter to find the exact sign-in) is repeated in a non-overlapping pattern across each tile rather than stamped once in the centre, so every tile carries it and a full tile holds at least one complete, readable copy. |

### The client also blocks right-click — on purpose, with eyes open

The Angular viewer blocks the context menu and native drag on the page, and paints tiles as CSS
backgrounds rather than `<img>` tags. This is exactly the naive, browser-side trick this README
opens by dismissing, added anyway as a deliberate, acknowledged tradeoff: it stops nothing for
anyone who opens DevTools or calls the API directly, but it does add friction for the casual
"right-click → Save image" path a non-technical user would otherwise take without a second
thought. It provides zero additional protection on top of the server-side controls above and must
never be mistaken for one.

### Watermarking happens on the way out, not at ingest

Stamping during ingest would produce one identical, un-attributable copy for everyone.
Pre-stamping per user would mean storing N copies of every tile for N viewers. Stamping at
request time means one stored tile serves everyone and every response is still individually
traceable. The cost is CPU per request and losing shared caching — which is why tile responses
are explicitly `Cache-Control: no-store`, since a shared cache holding a tile watermarked for
someone else would leak it.

### How strong the watermark is: a deliberate trade-off

The mark is red at 20% opacity, repeated in a brick pattern with 1.5× text-height gaps. That is a
product decision, not a default nobody chose: readers need to read the page through it, and a
heavier mark made dense pages (code, tables) hard to read.

What it is meant to guarantee is attribution, not deterrence by ugliness:

- Every full tile carries at least one complete copy (viewer, UTC time, trace code), and every
  tile, including the cropped ones at the page edges, carries the pattern at the same size. The
  pattern is laid out per tile, so copies do not line up across tile boundaries, but any capture
  of more than a small fragment of the page includes some of the mark, and any capture that
  contains a whole tile includes a complete copy.
- The trace code leads to the exact sign-in in the audit log, so even a partly legible mark is
  enough.
- It can't be switched off by the reader: it is burned into the pixels on the server.

Deployments that care more about deterrence than comfort can turn it up without code changes:
`watermark-opacity` (up to 0.6) and `watermark-spacing` (down to 0.5).

---

## Architecture

```
upload ──► TileGenerationService ──► {storage}/{docId}/v{version}/page-{n}/tile-{row}_{col}.png
                                              │
viewer ──► POST /api/auth/login ──► httpOnly session cookie (+ CSRF cookie)
       ──► /api/documents/{id}/pages/{n}/tile-urls
                     │
                     └─► SignedUrlService.issueToken(…, hmac(session)) ──► [[url,url,…],[url,…]]
                                                              │
       ──► GET /api/tiles?token=… ──► verify HMAC + expiry ────┘
                                  ──► session live? (Spring Security) and token bound to it?
                                  ──► per-user rate limit
                                  ──► still allowed to view? token's render version current? (else 404 / 410)
                                  ──► server-wide tile work limit (else 503, retried)
                                  ──► load raw tile from disk
                                  ──► WatermarkService stamps viewer, UTC time, trace code
                                  ──► PNG bytes (no-store)
                                              │
       ◄── viewer fetches each tile and paints it at (col*tileSize, row*tileSize)
```

### Source layout

```
src/main/java/com/example/securedocviewer/
├── account/                            # AppUser entity, roles, account service, first-admin bootstrap
├── audit/                              # persisted audit trail: events, search/export, retention
├── document/                           # Document entity, access rules (view/manage/share), lifecycle
├── config/ViewerProperties.java        # tile size, DPI, TTLs, rate limit, signing secret (validated)
├── controller/
│   ├── AuthController.java             # sign in / out, who am I, change password
│   ├── AdminController.java            # sessions (list/revoke), rate-limit usage, audit log
│   ├── UserAdminController.java        # create users, change role, enable/disable, reset password
│   ├── DocumentController.java         # list, upload, rename/visibility, replace PDF, delete, shares
│   ├── UserDirectoryController.java    # username lookup for the share picker
│   ├── PageTileUrlController.java      # issues a signed URL grid for one page
│   ├── TileController.java             # the only endpoint returning pixels
│   └── GlobalExceptionHandler.java     # JSON error mapping (400/401/404/409/429)
├── model/                              # PageInfo, TileUrlGrid, SignedTilePayload, SessionSummary, …
├── security/
│   ├── SecurityConfig.java             # filter chain: sessions, CSRF, role rules
│   ├── SessionKeys.java                # tile binding + admin handle derived from session id
│   ├── SessionAdministration.java      # list / revoke sessions
│   ├── LoginThrottle.java              # failed sign-in throttling
│   └── TileRateLimiter.java            # per-user tile rate limit
└── service/
    ├── TileGenerationService.java      # PDFBox rasterize into staging, then commit / replace / delete
    ├── StorageJanitor.java             # removes tile directories no document points to
    ├── TileGrid.java                   # pure tile-grid math (dependency-free, heavily tested)
    ├── SignedUrlService.java           # HMAC issue / verify
    └── WatermarkService.java           # per-request stamping
```

---

## Running it

Built on Spring Boot 4.1 (Java 25), Angular 22, MySQL 8.4 and PDFBox 3.

**Everything in Docker** (only Docker needed):

```bash
cp .env.example .env                          # fill in DB_PASSWORD, DB_ROOT_PASSWORD, SIGNING_SECRET (32+ chars)
docker compose --profile full up -d --build   # MySQL + API + nginx-served app at http://localhost:8081
```

Only the web container is published (on 127.0.0.1); the API and database are reachable only
inside the compose network. nginx runs as a non-root user (the unprivileged image, listening on
8080 inside the network). Base images and GitHub Actions are pinned by digest / commit SHA, and
Dependabot proposes updates. nginx serves the app with its own strict CSP and proxies `/api`, so the
browser sees a single origin.

**Development** (JDK 25+, Node 24, Docker for MySQL):

```bash
cp .env.example .env        # as above; keep STORAGE_ROOT out of synced folders (OneDrive, Dropbox…)
docker compose up -d        # MySQL 8.4 only, bound to 127.0.0.1
./mvnw spring-boot:run      # Maven wrapper pins Maven 3.9.16; Flyway creates the schema
cd frontend && npx ng serve # http://localhost:4200, proxies /api to :8080
```

On an empty database the first start creates an `admin` account. Its password is
`BOOTSTRAP_ADMIN_PASSWORD` from `.env`, or, if that is empty, a random one printed once in the
startup log. Sign in as admin, create PUBLISHER and READER accounts on the Admin page, and change
the admin password from the Account page. Publishers upload documents (private by default) and
share them with individual users or make them visible to everyone from each document's Manage
page.

### API

Authentication is a session cookie, and every state-changing request needs the `XSRF-TOKEN`
cookie echoed back in an `X-XSRF-TOKEN` header:

```bash
jar=$(mktemp); x() { awk '$6=="XSRF-TOKEN"{print $7}' "$jar"; }

# 1. pick up a CSRF cookie, then sign in
curl -s -c "$jar" localhost:8080/api/auth/me > /dev/null
curl -s -b "$jar" -c "$jar" -H "X-XSRF-TOKEN: $(x)" -H 'Content-Type: application/json' \
  -d '{"username":"pub.one","password":"…"}' localhost:8080/api/auth/login
# → {"username":"pub.one","role":"PUBLISHER"}

# 2. upload and tile a PDF (PUBLISHER or ADMIN); title defaults to the file name,
#    visibility to PRIVATE
curl -s -b "$jar" -H "X-XSRF-TOKEN: $(x)" -F "title=My Document" -F "file=@sample.pdf" \
  localhost:8080/api/documents
# → {"documentId":"…","owner":"pub.one","visibility":"PRIVATE","pageCount":12,"pages":[…],…}

# 2b. share it with one user (owner or admin)
curl -s -b "$jar" -H "X-XSRF-TOKEN: $(x)" -X PUT localhost:8080/api/documents/$DOC/shares/reader.one
# → ["reader.one"]

# 3. get signed tile URLs for page 0
curl -s -b "$jar" localhost:8080/api/documents/$DOC/pages/0/tile-urls
# → {"page":0,"rows":4,"cols":3,"tileSize":512,"tileUrls":[["/api/tiles?token=…",…],…]}

# 4. redeem one, with the same session
curl -s -b "$jar" "localhost:8080/api/tiles?token=…" --output tile.png
```

Worth trying, to see the protections fire: edit one character of a token (401, signature
mismatch), wait past the TTL and retry (401, expired), sign out and retry an unexpired token
(401), redeem the URL with a different user's session or none (401), or make more than
`tile-rate-limit-per-window` tile requests as one user inside the window (429, rate limited, with
`Retry-After` giving the seconds until the next slot frees up).

### Configuration

All under `secure-doc-viewer.*` in `application.yml`; secrets come from the environment or `.env`:

| Key | Default | Meaning |
|---|---|---|
| `storage-root` | `STORAGE_ROOT` (default `./storage`) | Where tiles are written — keep it out of synced folders |
| `tile-size` | `512` | Square tile edge in px |
| `render-dpi` | `150` | Rasterization DPI |
| `signing-secret` | `SIGNING_SECRET` | HMAC key; startup fails if missing or under 32 characters |
| `url-ttl-seconds` | `120` | Signed URL lifetime |
| `max-pages` | `500` | Uploads with more pages are rejected before rendering |
| `max-page-pixels` | `40000000` | Largest page (px at render DPI) accepted; guards against decompression-bomb PDFs |
| `watermark-opacity` / `watermark-spacing` | `0.2` / `1.5` | Watermark ink opacity, and gap between copies as a multiple of the text height |
| `tile-rate-limit-per-window` | `180` | Max tile requests a user may make per window |
| `tile-rate-limit-window-seconds` | `60` | Width of that rolling window |
| `max-concurrent-renders` / `render-queue-timeout-seconds` | `2` / `30` | PDFs rendered at once; further uploads wait this long, then get `503` + `Retry-After` |
| `max-concurrent-tile-renders` | `0` (2 × CPUs) | Tiles watermarked at once across all users; beyond it tile requests get `503` + `Retry-After` and the viewer retries |
| `render-timeout` | `3m` | A PDF that takes longer to render is rejected (`400`) and its render slot freed |
| `session-max-lifetime` | `12h` | Sessions end this long after sign-in, however active (on top of the 30-minute idle timeout) |
| `audit-retention-days` | `180` | Audit events older than this are purged nightly |
| `metrics-allowed-addresses` | `METRICS_ALLOWED_ADDRESSES` (default loopback) | CIDRs allowed to scrape `/actuator/prometheus` |
| `bootstrap-admin.username` / `.password` | `admin` / `BOOTSTRAP_ADMIN_PASSWORD` | First admin, created only on an empty database |

Uploads are capped at 50 MB (`spring.servlet.multipart.max-file-size`; larger files get a JSON
`413`), must start with a `%PDF-` signature, and are streamed to disk rather than held in memory.

`GET /actuator/health` (public, status only, `503` when the database is down) is the only Actuator
endpoint nginx forwards. `GET /actuator/prometheus` serves metrics only to
`metrics-allowed-addresses` — the viewer's own counters are `sdv_tiles_served_total`,
`sdv_tiles_rate_limited_total`, `sdv_sign_in_total{outcome=success|failure|locked}`,
`sdv_render_seconds` and `sdv_render_rejected_total`, next to the usual JVM, HTTP and pool metrics. Every API response carries a strict Content-Security-Policy,
`X-Frame-Options: DENY`, `nosniff`, `Referrer-Policy: no-referrer` (tile URLs carry tokens) and a
Permissions-Policy. Errors are always `{"error": "..."}` JSON; unexpected failures return a generic
`500` with a reference that is logged alongside the full exception.

Sessions time out after 30 minutes of inactivity (`server.servlet.session.timeout`). Set
`SESSION_COOKIE_SECURE=true` wherever the app is served over HTTPS.

Replacing a PDF renders it into a new tile version (`{doc}/v{n}`) and switches the document to it
under a row lock, so readers never see old and new tiles mixed; tile URLs issued for the old
version answer `410 Gone` and the viewer reloads the new one. Admins can hand a document to
another publisher (e.g. before disabling its owner).

### Sign-in lockout

Three counters, each over a rolling 15 minutes. Checking and counting are one atomic step (an
attempt is counted before its password is checked, and handed back if it was right), so a burst of
parallel guesses gets no more tries than a sequence would. Changing your password is throttled
the same way, so a stolen session can't be used to guess the current password. A sign-in is refused (`429` + `Retry-After`) when
any rule it is subject to is over its limit:

| Rule | Limit | Applies to |
|---|---|---|
| account + IP | 5 failures | every attempt — stops guessing one account from one place |
| IP | 20 failures | every attempt — stops one place spraying many accounts |
| account-wide | 20 failures | only attempts from **unrecognised** devices — stops a botnet spreading guesses over many IPs |

A device is *recognised* for an account after a successful sign-in from its address (IPv6 grouped
by /64) within the last 30 days. Only a keyed hash of the address is stored
(`account_known_ip`), and the list is cleared when the password is changed or reset or the account
is disabled. The trade-off, stated plainly: while an account is under a distributed attack, its
owner can still sign in from a usual device, but not from a new one (a new laptop, a hotel
network) until the window passes or an admin presses **Unlock** on the Admin page (audited as
`USER_UNLOCKED`). The lockout audit event records which rule fired (`rule=account-wide`, …).

The failure counters live in memory in each instance; recognised devices are in the database.

### Trust boundary

Throttling and the audit log use the client address, so where it comes from matters:

- The API trusts `X-Forwarded-For` only when `FORWARD_HEADERS_STRATEGY=native`, and then only
  from `TRUSTED_PROXY_REGEX` — in compose, nginx's fixed address `172.28.0.10`. Anything else that
  reaches `app:8080` directly is judged by its own address, whatever headers it sends.
- nginx **overwrites** `X-Forwarded-For` with the TCP peer, so a client can't choose its address.
  It accepts a forwarded address only from the optional HTTPS front end (Caddy, `172.28.0.11`).
- `app:8080` and MySQL are not published; only nginx (and Caddy) listen on the host, on 127.0.0.1.
  If you deploy differently, keep the API reachable only through the proxy.

### HTTPS

```bash
docker compose --profile full --profile tls up -d --build   # https://localhost:8443 (Caddy's local CA)
```

Caddy terminates TLS, adds `Strict-Transport-Security` and forwards to nginx
(`deploy/Caddyfile`). For a real host set `SITE_ADDRESS=docs.example.com` and `TLS_MODE` to an
e-mail address in `.env` to get a Let's Encrypt certificate (publish ports 80/443 instead of the
local-only 8443), and set `SESSION_COOKIE_SECURE=true`.

### Backup and restore

Two things hold state: the MySQL database and the `app-storage` volume with the rendered tiles.
They must be captured **at the same moment**: replacing a PDF deletes the previous tile version
as soon as it commits, so a dump taken before a replace plus a tile archive taken after it would
point documents at tiles that no longer exist. Stop the app for the few seconds a backup takes:

```bash
# backup (the app is stopped so the database and the tiles match)
docker compose --profile full stop app
docker compose exec -T mysql sh -c 'exec mysqldump --single-transaction --routines -u root -p"$MYSQL_ROOT_PASSWORD" "$MYSQL_DATABASE"' > securedocs.sql
docker run --rm -v secure-doc-viewer_app-storage:/data -v "$PWD":/backup alpine tar czf /backup/storage.tgz -C /data .
docker compose --profile full start app

# restore (stop the app first so nothing is written meanwhile)
docker compose --profile full stop app
docker compose exec -T mysql sh -c 'exec mysql -u root -p"$MYSQL_ROOT_PASSWORD" "$MYSQL_DATABASE"' < securedocs.sql
docker run --rm -v secure-doc-viewer_app-storage:/data -v "$PWD":/backup alpine sh -c 'rm -rf /data/* && tar xzf /backup/storage.tgz -C /data'
docker compose --profile full start app
```

Try a restore into a scratch environment before relying on the backups. As a safety net, the
storage janitor never removes a document's other tile versions while its current one is missing.

Keep `.env` (above all `SIGNING_SECRET`) with the backup: a restore under a different secret
still works, but every account's recognised devices are forgotten (their hashes are keyed by it).

---

### Go-live checklist

- Serve over HTTPS: the `tls` profile with `SITE_ADDRESS` set to the domain and `TLS_MODE` to an
  e-mail address (real certificate), ports 80/443 published, and `SESSION_COOKIE_SECURE=true`.
  Add `includeSubDomains` to `HSTS_POLICY` only if every subdomain is HTTPS.
- Keep `app:8080`, MySQL and the metrics endpoint off the network; set `METRICS_ALLOWED_ADDRESSES`
  to the Prometheus server and alert on `sdv_sign_in_total{outcome="locked"}`,
  `sdv_tiles_rate_limited_total` and `sdv_render_rejected_total`.
- Strong, unique `SIGNING_SECRET`, `DB_PASSWORD`, `DB_ROOT_PASSWORD`; change the bootstrap admin
  password at first sign-in. The app forces this only when it generated the password: if you set
  `BOOTSTRAP_ADMIN_PASSWORD`, change it yourself and clear it from `.env` after the first start.
- Scheduled backups as above, plus one restore drill.
- One app instance (see [Limitations](#limitations)).

---

## Tests

```bash
./mvnw verify                              # backend: H2 in MySQL mode, plus MySQL 8.4 via Testcontainers when Docker is running
cd frontend && npx ng test --watch=false   # frontend unit tests
# end-to-end, against the running Docker stack; needs an admin account:
cd frontend && E2E_ADMIN_USER=admin E2E_ADMIN_PASSWORD=… npx playwright test
```

GitHub Actions runs all of this on every pull request — backend, frontend, a known-vulnerability
scan of every Maven and npm dependency (OSV; fails the build on any published advisory), a scan
of the built container images (Trivy; fails on any fixable HIGH/CRITICAL OS or library issue), then the
Playwright journey against a freshly built Docker stack with throwaway secrets — and Dependabot opens weekly
grouped update PRs for Maven, npm, Docker images and Actions.

The end-to-end test checks each screen (sign-in, admin, upload, manage, document list, viewer)
with axe-core for WCAG 2.1 A/AA violations in both light and dark themes, and disables the
accounts it created when it finishes. It creates a publisher, a reader and an outsider; the publisher uploads a PDF and
shares it with the reader, who must see every tile load and turn pages by keyboard, while the
outsider is told the document doesn't exist.

Security integration tests run the real filter chain: sign-in required, identical answers for
wrong password and unknown user, lockout after repeated failures, disabled accounts, CSRF
enforcement and rotation at sign-in, readers blocked from admin and upload, session ids never
exposed, admin revocation, tile URLs only working for the session they were issued to, and role
changes ending existing sessions. Document access tests cover private documents being invisible
(404) to others, sharing and unsharing (including already-issued tile URLs), EVERYONE visibility,
owner-only management, admin override, rename/replace/delete (tiles removed from disk), corrupt
uploads leaving nothing behind, and audit search, filters and CSV export (with formula
neutralisation). Unit tests cover HMAC round-trip, tampered-signature and
payload-splicing rejection, expiry, malformed tokens, the rate limiter, watermark layout and
distinctness between viewers, and tile-grid math. The grid tests include a round-trip property check: slicing an image and reassembling
every tile at its offset must reproduce the source pixel-for-pixel, so edge tiles are proven to
be cropped rather than padded or dropped.

Concurrency is tested directly: parallel wrong passwords get exactly the allowed number of tries;
readers pulling tiles while the owner replaces or deletes the PDF only ever get the tile, `410` or
`404` (never an error or a mixed page); the audit throttle writes one event per interval under
parallel callers; a timed-out render frees its slot and cleans up.

`MySqlIntegrationTest` runs against a real MySQL 8.4 (skipped without Docker; CI has it): all
Flyway migrations, two concurrent PDF replacements serialised by the row lock, and timestamps
stored as UTC with the server and the JVM each set to a different non-UTC zone.

---

## Limitations

Stated plainly, because the honest framing matters more than the feature list:

- **This does not make content uncopyable, and nothing can.** Anything rendered on a screen can
  be photographed or screen-captured. The goal is to raise cost and add attribution, not to
  achieve prevention.
- A determined user with a legitimate session can still request every tile and reassemble them —
  the per-user rate limit only bounds how *fast*, not whether. The defaults favour readers:
  512 px tiles at 180 a minute let someone read ~15 pages a minute without pausing, which also
  means a scripted harvest of a 500-page document takes about half an hour rather than hours.
  Lower `tile-rate-limit-per-window` (or `tile-size`) for documents where that matters more
  than reading speed; documents keep the tile size they were rendered with. The watermark is what makes the
  result traceable regardless.
  **Decision (product owner, 2026-09-19):** these defaults are accepted for go-live and will be
  revisited based on real usage (`sdv_tiles_rate_limited_total` shows how often readers hit the
  limit). Per-document sensitivity levels with tighter limits are a possible follow-up.
- Accounts, documents, shares and the audit trail live in MySQL; sessions and the rate-limit
  counters are still in memory, so they don't span instances. Multiple instances would need a
  shared session store (e.g. Redis) and shared tile storage.
- Sharing is per user or with everyone; there are no groups yet. Users are disabled, never
  deleted (their audit history stays meaningful); the admin list hides disabled accounts by
  default.
- Sign-in is by password only; there is no MFA, including for admins. Passwords are limited to
  72 bytes, BCrypt's maximum.
- Publishers can discover non-admin usernames through the share picker (two-character prefix
  search), by design: they need it to share.
- Pages are images, so screen readers get no text; there is no text layer by design.
- A render that overruns `render-timeout` is rejected and abandoned at its next page boundary.
  It keeps its render slot until it has actually stopped (so abandoned renders can't pile up CPU
  or memory), which means one pathological page can hold one slot until it finishes; watch
  `sdv_render_abandoned_running`.
- Tiles are stored on local disk. Object storage (S3) plus a CDN with signed URLs is the
  production shape; `SignedUrlService` deliberately mirrors the presigned-URL pattern so it maps
  onto CloudFront signed URLs with little change.
- Watermarking every tile per request costs CPU and defeats caching. At scale you would watermark
  at a coarser granularity or cache per (tile, viewer) with a short TTL.

## Possible next steps

- Back storage with S3 and issue CloudFront signed URLs instead of app-issued tokens
- Share sessions across instances (Spring Session + Redis)
- Group-based sharing, access expiry dates, and per-document reading analytics for owners
- Move the rate-limit counters into a shared store (Redis) so limits hold across instances,
  and log/alert on the specific pattern of "every tile-urls page fetched back-to-back" rather
  than just a flat per-minute cap
- Progressive/lazy tile loading (only fetch tiles inside the viewport at current zoom)

## License

The application source code and build files are released under the MIT License (see [LICENSE](LICENSE)). The text and
figures of the book under `book/` are copyright 2026, all rights reserved (see [book/LICENSE.md](book/LICENSE.md));
the code listings quoted in the book are excerpts of the MIT-licensed source code.
