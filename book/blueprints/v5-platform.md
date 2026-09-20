# Blueprint v5: the platform (`book-m5-platform`)

Stack: Spring Boot 4.1.1, Java 25 (upgraded at this milestone), Docker Compose stack, GitHub Actions.

```mermaid
flowchart TB
    U["User browser"]
    PR["Prometheus: allowed addresses only"]
    subgraph Compose["Docker Compose network"]
        CD["Caddy (tls profile): HTTPS, HSTS"]
        NG["nginx: serves Angular, proxies /api"]
        subgraph APP["app: Spring Boot 4"]
            F["SessionLifetimeFilter, PasswordChangeRequiredFilter"]
            SEC["SecurityConfig, LoginThrottle, KnownDevices"]
            C["Controllers: Auth, Document, PageTileUrl, Tile, Admin, UserAdmin, UserDirectory"]
            DS["DocumentService, TileAccess"]
            TW["TileWorkLimiter, TileRateLimiter"]
            TG["TileGenerationService: staging, versions, bounded renders"]
            VM["ViewerMetrics: /actuator/prometheus"]
            SJ["StorageJanitor"]
        end
        M[("MySQL 8.4: V1, V2, V3")]
        ST[("app-storage volume: tiles")]
    end
    U --> CD --> NG --> F --> SEC --> C
    U --> NG
    C --> DS
    C --> TW
    C --> TG
    DS -.-> M
    TG -.-> ST
    SJ -.-> ST
    PR --> VM
    F ~~~ VM
```

*Figure: Blueprint v5. Text description: the browser reaches nginx (optionally through Caddy for HTTPS); only nginx and Caddy are published; the API and MySQL are internal; tiles are stored in versioned folders on a volume.*

## What changed since v4
- Note: this drawing is a deployment view. It shows what was added or changed at this milestone and the containers around it; `SignedUrlService`, `SessionKeys`, `WatermarkService` and `AuditLogService` still exist but are omitted to keep the drawing readable.
- Spring Boot 3.3.4 to 4.1.1 and Java 21 to 25; `Dockerfile`, `frontend/Dockerfile`, `frontend/nginx.conf`, `deploy/Caddyfile`, the full compose stack, `.github/workflows/ci.yml`, Dependabot, the Maven wrapper.
- Versioned tiles (`{doc}/v{n}`) with atomic replace and `410 Gone` for old tokens (`TileGoneException`; migration `V3__tile_versions_and_account_security.sql`).
- Bounded rendering and a tile work limit (`TileWorkLimiter`, `ServiceBusyException`); metrics (`ViewerMetrics`).
- Account security: `KnownDevices` (recognized devices), admin unlock, forced password change (`PasswordChangeRequiredFilter`), `SessionLifetimeFilter`, `SessionMetadata`.
- Playwright end-to-end tests (`frontend/playwright.config.ts`); CI runs tests and vulnerability scans.
