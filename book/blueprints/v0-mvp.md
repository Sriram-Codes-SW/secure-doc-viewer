# Blueprint v0: the tiled viewer (`book-m0-mvp`)

Stack at this tag: Spring Boot 3.3.4, Java 21, PDFBox; no database, no accounts.

```mermaid
flowchart LR
    B["Browser: static index.html with a canvas"]
    subgraph API["Spring Boot app"]
        SC["SessionController: /api/session/login, /logout"]
        DC["DocumentController: POST and GET /api/documents"]
        PC["PageTileUrlController: GET .../tile-urls"]
        TC["TileController: GET /api/tiles"]
        SS["SessionService (in memory)"]
        DR["DocumentRegistry (in memory)"]
        TG["TileGenerationService + TileGrid"]
        SU["SignedUrlService (HMAC)"]
        WM["WatermarkService"]
    end
    D[("Disk: storage/docId/page-n/tile-row_col.png")]
    B --> SC --> SS
    B --> DC --> TG
    DC --> DR
    TG -.-> D
    B --> PC --> SU
    PC --> SS
    B --> TC
    TC --> SU
    TC --> SS
    TC --> TG
    TC --> WM
```

*Figure: Blueprint v0. Text description: the browser signs in by name only, uploads a PDF that is tiled to disk, asks for signed tile URLs, and redeems each one at the tile endpoint, which checks signature and session and stamps a watermark.*

## What's here
- Signing in takes only a username and returns a session id, sent back in an `X-Session-Id` header. This is a stand-in for real authentication.
- Tile URLs are HMAC-signed and bound to that session id; `TileController` also checks the session is still live and sets `Cache-Control: no-store`.
- Documents and sessions live in memory (`DocumentRegistry`, `SessionService`); only tiles are on disk.
