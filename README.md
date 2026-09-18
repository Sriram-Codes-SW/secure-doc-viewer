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
| "Copy the tile URL" | Every tile URL carries an HMAC-SHA256 signature over document + page + row + col + session + expiry. Change any field and the signature fails. |
| "Reuse the URL later" | Tokens expire (default 120s). Expiry is inside the signed payload, so it cannot be edited. |
| "Share the URL with someone else" | Tokens are bound to the issuing session id, and that session is re-validated on every tile request. |
| "Keep using URLs after logout" | Session revocation is checked independently of token expiry, so logging out kills every outstanding URL immediately. |
| "Script every tile of every page in one burst" | `/api/tiles` is rate-limited per session (default 120 tiles/60s window — about three pages a minute). A valid signature, unexpired token, and live session still only get throttled access — bulk harvesting becomes slow and boundable instead of instant. Throttled requests get `429` with a `Retry-After` header, and the viewer shows a countdown and loads the rest of the page when the window allows. |
| "Screenshot it anyway" | Not prevented — see [Limitations](#limitations). Every served tile is watermarked with the requesting viewer's identity and a UTC timestamp, so a leaked capture is attributable. The mark (viewer on one line, timestamp on the next) is repeated in a non-overlapping pattern across each tile rather than stamped once in the centre, so every tile carries it and a full tile holds at least one complete, readable copy. |

### The client also blocks right-click and warns on DevTools — on purpose, with eyes open

`index.html` blocks the canvas's context menu/drag and shows a soft warning when DevTools looks
open (a window-size heuristic — easy to evade, and it only warns, it never blocks anything). This
is exactly the naive, browser-side trick this README opens by dismissing, added anyway as a
deliberate, acknowledged tradeoff: it stops nothing for anyone who disables JS, opens a console,
or hits `/api/tiles` directly, but it does add friction for the casual "right-click → Save image"
path a non-technical user would otherwise take without a second thought. It provides zero
additional protection on top of the server-side controls above and must never be mistaken for one.

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
viewer ──► /api/session/login ──► sessionId   │
       ──► /api/documents/{id}/pages/{n}/tile-urls
                     │
                     └─► SignedUrlService.issueToken() ──► [[url,url,…],[url,…]]
                                                              │
       ──► GET /api/tiles?token=… ──► verify HMAC + expiry ────┘
                                  ──► re-check session is live
                                  ──► load raw tile from disk
                                  ──► WatermarkService stamps viewer id + timestamp
                                  ──► PNG bytes (no-store)
                                              │
       ◄── browser paints each tile at (col*tileSize, row*tileSize) on <canvas>
```

### Source layout

```
src/main/java/com/example/securedocviewer/
├── config/ViewerProperties.java        # tile size, DPI, TTLs, signing secret
├── controller/
│   ├── SessionController.java          # login / logout (stands in for real auth)
│   ├── DocumentController.java         # upload + tile, fetch manifest
│   ├── PageTileUrlController.java      # issues a signed URL grid for one page
│   ├── TileController.java             # the only endpoint returning pixels
│   └── GlobalExceptionHandler.java     # 401 / 404 mapping
├── model/                              # DocumentManifest, PageInfo, TileUrlGrid, SignedTilePayload
├── security/SessionService.java        # in-memory sessions, revocable
└── service/
    ├── TileGenerationService.java      # PDFBox rasterize + slice + persist
    ├── TileGrid.java                   # pure tile-grid math (dependency-free, heavily tested)
    ├── SignedUrlService.java           # HMAC issue / verify
    ├── WatermarkService.java           # per-request stamping
    └── DocumentRegistry.java           # in-memory manifest index
```

---

## Running it

Requires JDK 21+ and Maven.

```bash
mvn spring-boot:run
```

Then open <http://localhost:8080>, log in with any username, and upload a PDF. The status line
narrates each step (tiles requested, tiles painted) so the mechanism is visible while it runs.

### API

```bash
# 1. log in
curl -X POST "localhost:8080/api/session/login?username=alice"
# → {"sessionId":"…","username":"alice"}

# 2. upload and tile a PDF
curl -X POST localhost:8080/api/documents \
  -H "X-Session-Id: $SESSION" \
  -F "title=My Document" -F "file=@sample.pdf"
# → {"documentId":"…","pageCount":12,"pages":[{"page":0,"rows":8,"cols":6,…}]}

# 3. get signed tile URLs for page 0
curl localhost:8080/api/documents/$DOC/pages/0/tile-urls -H "X-Session-Id: $SESSION"
# → {"page":0,"rows":8,"cols":6,"tileSize":256,"tileUrls":[["/api/tiles?token=…",…],…]}

# 4. redeem one
curl "localhost:8080/api/tiles?token=…" --output tile.png
```

Worth trying, to see the protections fire: edit one character of a token (401, signature
mismatch), wait past the TTL and retry (401, expired), log out and retry an unexpired token
(401, session revoked), or request more than `tile-rate-limit-per-window` distinct tiles for the
same session inside the window (429, rate limited, with `Retry-After` giving the seconds until the
next slot frees up).

### Configuration

All under `secure-doc-viewer.*` in `application.yml`:

| Key | Default | Meaning |
|---|---|---|
| `storage-root` | `./storage` | Where tiles are written |
| `tile-size` | `256` | Square tile edge in px |
| `render-dpi` | `150` | Rasterization DPI |
| `signing-secret` | *(demo value)* | HMAC key — supply via env/secrets manager in any real deployment |
| `url-ttl-seconds` | `120` | Signed URL lifetime |
| `session-ttl-seconds` | `1800` | Session lifetime |
| `tile-rate-limit-per-window` | `120` | Max tiles a session may redeem per window |
| `tile-rate-limit-window-seconds` | `60` | Width of that rolling window |

---

## Tests

```bash
mvn test
```

Covers HMAC round-trip, tampered-signature rejection, payload-splicing rejection, wrong-secret
rejection, expiry, session revocation, watermark distinctness between viewers, and tile-grid
math. The grid tests include a round-trip property check: slicing an image and reassembling
every tile at its offset must reproduce the source pixel-for-pixel, so edge tiles are proven to
be cropped rather than padded or dropped.

---

## Limitations

Stated plainly, because the honest framing matters more than the feature list:

- **This does not make content uncopyable, and nothing can.** Anything rendered on a screen can
  be photographed or screen-captured. The goal is to raise cost and add attribution, not to
  achieve prevention.
- A determined user with a legitimate session can still request every tile and reassemble them —
  the per-session rate limit only bounds how *fast*, not whether. The watermark is what makes the
  result traceable regardless.
- Sessions and manifests are in-memory, so both are lost on restart and neither survives more
  than one instance. Real deployments need a shared store (Postgres/Redis) for both.
- `SessionController` is not authentication. It mints a session for any username with no
  password check. Swap in Spring Security and a real identity provider.
- Tiles are stored on local disk. Object storage (S3) plus a CDN with signed URLs is the
  production shape; `SignedUrlService` deliberately mirrors the presigned-URL pattern so it maps
  onto CloudFront signed URLs with little change.
- Watermarking every tile per request costs CPU and defeats caching. At scale you would watermark
  at a coarser granularity or cache per (tile, viewer) with a short TTL.

## Possible next steps

- Back storage with S3 and issue CloudFront signed URLs instead of app-issued tokens
- Replace the in-memory session store with Spring Security + Redis
- Persist manifests in Postgres so documents survive restart
- Move the rate-limit counters into a shared store (Redis) so limits hold across instances,
  and log/alert on the specific pattern of "every tile-urls page fetched back-to-back" rather
  than just a flat per-minute cap
- Progressive/lazy tile loading (only fetch tiles inside the viewport at current zoom)

## License

MIT
