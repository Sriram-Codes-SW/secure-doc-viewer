# Blueprint v6: the finished app (`book-m6-final`)

```mermaid
flowchart LR
    U["User browser"]
    subgraph Compose["Docker Compose network"]
        CD["Caddy (profile tls): HTTPS, HSTS"]
        NG["nginx: serves Angular, proxies /api, CSP, sets X-Forwarded-For"]
        subgraph APP["app (Spring Boot 4)"]
            F["Filters: SessionLifetimeFilter, PasswordChangeRequiredFilter"]
            SEC["SecurityConfig + LoginThrottle + KnownDevices"]
            C["Controllers: Auth, Document, PageTileUrl, Tile, Admin, UserAdmin, UserDirectory"]
            DS["DocumentService + TileAccess"]
            TG["TileGenerationService: staging, versions v(n), bounded renders"]
            TW["TileWorkLimiter + TileRateLimiter"]
            VM["ViewerMetrics: /actuator/prometheus"]
            SJ["StorageJanitor"]
        end
        M[("MySQL 8.4 (V1, V2, V3)")]
        ST[("app-storage volume: tiles")]
    end
    PR["Prometheus (allowed addresses only)"]
    U --> CD --> NG --> F --> SEC --> C
    U --> NG
    C --> DS
    C --> TW
    DS -.-> M
    C --> TG
    TG -.-> ST
    SJ -.-> ST
    PR --> VM
```

*Figure: Blueprint v6. Text description: the architecture is unchanged from Blueprint v5: the browser reaches nginx (optionally through Caddy for HTTPS); only these are published; the API and MySQL are internal; tiles live in versioned folders on a volume.*

The architecture is identical to Blueprint v5 (see `v5-platform.md`), including its note that the drawing is a deployment view. Between `book-m5-platform` and `book-m6-final` the non-test changes are `.github/dependabot.yml` (propose only stable/LTS lines) and `frontend/package.json` with its lock file (Vitest 5, jsdom 30 bumps). The history also contains a fix to a flaky assertion in `TileGenerationServiceTest` (commit `ec6c1c5`, PR #9), which is test code only.

## What changed since v5
- No structural change: a test fix, dependency updates and the Dependabot policy only.
