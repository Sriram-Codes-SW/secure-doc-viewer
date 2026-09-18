# Secure Document Viewer

A small Spring Boot service that serves PDF documents to authenticated viewers **without ever
handing out the PDF**. Pages are rasterized server-side, sliced into image tiles, and delivered
one tile at a time through short-lived HMAC-signed URLs that are bound to a login session and
watermarked per viewer at request time. The browser reassembles the tiles onto a `<canvas>`.

This is the architecture commercial e-magazine and flipbook readers use. The project exists to
implement that pattern end to end and to be explicit about what it does and does not achieve.

---

## Why this design

A naive "protected" viewer hides the download button in CSS and blocks right-click in JavaScript.
Both live entirely in the browser, so both are undone in about ten seconds with DevTools. The
protection here is structural instead — it lives on the server, where the client cannot reach it:

| Concern | How it is handled |
|---|---|
| "Save the PDF" | The PDF stops existing as a servable file after ingest. Only disconnected PNG tiles remain on disk. There is no endpoint that returns a whole page, let alone a whole document. |
| "Copy the tile URL" | Every tile URL carries an HMAC-SHA256 signature over document + page + row + col + session binding + expiry. Change any field and the signature fails. |
| "Reuse the URL later" | Tokens expire (default 120s). Expiry is inside the signed payload, so it cannot be edited. |
| "Share the URL with someone else" | Tokens carry a keyed hash of the issuing session (never the session id itself), and a tile request must also present that session's httpOnly cookie. A URL pasted into another browser or account gets 401. |
| "Keep using URLs after logout" | The session is checked on every tile request, independently of token expiry, so signing out — or an admin revoking the session — kills every outstanding URL immediately. |
| "Open a document that wasn't shared with me" | Every document has an owner and a visibility: PRIVATE (owner + users it is shared with) or EVERYONE. Anyone else gets `404` — not `403` — so they can't even tell it exists. The check runs on the list, the manifest, when tile URLs are issued, and again on every single tile request, so unsharing or deleting a document cuts off pages that are already open. |
| "Just sign in as someone else" | Real accounts: username + BCrypt password, created by an admin (no self-signup). Failed sign-ins are throttled per account and per client IP (`429` + `Retry-After`). Roles — READER, PUBLISHER, ADMIN — are enforced server-side on every endpoint. |
| "Script every tile of every page in one burst" | `/api/tiles` is rate-limited per user (default 120 tile requests per 60s window — about three pages a minute). A valid signature, unexpired token, and live session still only get throttled access — bulk harvesting becomes slow and boundable instead of instant. Throttled requests get `429` with a `Retry-After` header, and the viewer shows a countdown and loads the rest of the page when the window allows. |
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

---

## Architecture

```
upload ──► TileGenerationService ──► {storage}/{docId}/page-{n}/tile-{row}_{col}.png
                                              │
viewer ──► POST /api/auth/login ──► httpOnly session cookie (+ CSRF cookie)
       ──► /api/documents/{id}/pages/{n}/tile-urls
                     │
                     └─► SignedUrlService.issueToken(…, hmac(session)) ──► [[url,url,…],[url,…]]
                                                              │
       ──► GET /api/tiles?token=… ──► verify HMAC + expiry ────┘
                                  ──► session live? (Spring Security) and token bound to it?
                                  ──► per-user rate limit
                                  ──► load raw tile from disk
                                  ──► WatermarkService stamps viewer id + timestamp
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
├── model/                              # DocumentManifest, PageInfo, TileUrlGrid, SignedTilePayload, …
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

Requires JDK 21+, Maven, Node.js, and Docker (for MySQL).

```bash
cp .env.example .env        # then fill in DB_PASSWORD, DB_ROOT_PASSWORD, SIGNING_SECRET (32+ chars),
                            # and point STORAGE_ROOT at a folder that is NOT synced (OneDrive, Dropbox…)
docker compose up -d        # MySQL 8.4, bound to 127.0.0.1 only
mvn spring-boot:run         # Flyway creates the schema on first start
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
# → {"page":0,"rows":8,"cols":6,"tileSize":256,"tileUrls":[["/api/tiles?token=…",…],…]}

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
| `tile-size` | `256` | Square tile edge in px |
| `render-dpi` | `150` | Rasterization DPI |
| `signing-secret` | `SIGNING_SECRET` | HMAC key; startup fails if missing or under 32 characters |
| `url-ttl-seconds` | `120` | Signed URL lifetime |
| `max-pages` | `500` | Uploads with more pages are rejected before rendering |
| `max-page-pixels` | `40000000` | Largest page (px at render DPI) accepted; guards against decompression-bomb PDFs |
| `watermark-opacity` / `watermark-spacing` | `0.2` / `1.5` | Watermark ink opacity, and gap between copies as a multiple of the text height |
| `tile-rate-limit-per-window` | `120` | Max tile requests a user may make per window |
| `tile-rate-limit-window-seconds` | `60` | Width of that rolling window |
| `audit-retention-days` | `180` | Audit events older than this are purged nightly |
| `bootstrap-admin.username` / `.password` | `admin` / `BOOTSTRAP_ADMIN_PASSWORD` | First admin, created only on an empty database |

Uploads are capped at 50 MB (`spring.servlet.multipart.max-file-size`; larger files get a JSON
`413`), must start with a `%PDF-` signature, and are streamed to disk rather than held in memory.

`GET /actuator/health` (public, status only, `503` when the database is down) is the one Actuator
endpoint exposed. Every API response carries a strict Content-Security-Policy,
`X-Frame-Options: DENY`, `nosniff`, `Referrer-Policy: no-referrer` (tile URLs carry tokens) and a
Permissions-Policy. Errors are always `{"error": "..."}` JSON; unexpected failures return a generic
`500` with a reference that is logged alongside the full exception.

Sessions time out after 30 minutes of inactivity (`server.servlet.session.timeout`). Set
`SESSION_COOKIE_SECURE=true` wherever the app is served over HTTPS.

---

## Tests

```bash
mvn test                                   # backend (uses in-memory H2 in MySQL mode)
cd frontend && npx ng test --watch=false   # frontend
```

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

---

## Limitations

Stated plainly, because the honest framing matters more than the feature list:

- **This does not make content uncopyable, and nothing can.** Anything rendered on a screen can
  be photographed or screen-captured. The goal is to raise cost and add attribution, not to
  achieve prevention.
- A determined user with a legitimate session can still request every tile and reassemble them —
  the per-user rate limit only bounds how *fast*, not whether. The watermark is what makes the
  result traceable regardless.
- Accounts, documents, shares and the audit trail live in MySQL; sessions and the rate-limit
  counters are still in memory, so they don't span instances. Multiple instances would need a
  shared session store (e.g. Redis) and shared tile storage.
- Sharing is per user or with everyone; there are no groups yet.
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

MIT
