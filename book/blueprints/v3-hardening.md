# Blueprint v3: upload and API hardening (`book-m3-hardening`)

No new components; existing ones gain guards. The diagram shows the request path with the new layers.

```mermaid
flowchart LR
    B["Angular app"]
    subgraph API["Spring Boot app"]
        SEC["SecurityConfig: now also sets security headers"]
        DC["DocumentController: size and signature checks, streaming ingest"]
        TG["TileGenerationService: page and pixel limits"]
        GEH["GlobalExceptionHandler: one JSON error shape, generic 500"]
        VP["ViewerProperties: validated limits"]
        H["Health check"]
    end
    M[("MySQL")]
    D[("Disk")]
    B --> SEC --> DC --> TG
    TG -.-> D
    DC -.-> M
    DC --> GEH
    TG --> VP
    SEC --> H
```

*Figure: Blueprint v3. Text description: the same request path as v2 with upload limits, streamed ingest, a uniform error contract, security headers and a health check added.*

## What changed since v2
- Files changed: `DocumentController`, `DocumentService`, `TileGenerationService`, `GlobalExceptionHandler`, `SecurityConfig`, `ViewerProperties`, `application.yml`, `pom.xml`, plus the upload page in the frontend.
- Upload limits and streaming ingest; JSON errors without internals; security headers; a health check.
