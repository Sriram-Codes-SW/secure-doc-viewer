# Blueprint v2: documents, ownership and audit (`book-m2-documents`)

```mermaid
flowchart LR
    B["Angular app: list, upload, viewer, Manage, Admin"]
    subgraph API["Spring Boot app"]
        SEC["SecurityConfig"]
        DC["DocumentController: list, upload, rename, replace, delete, shares"]
        UD["UserDirectoryController: /api/users"]
        PC["PageTileUrlController"]
        TC["TileController"]
        AD["AdminController: audit search and CSV export"]
        DS["DocumentService: view / manage / share rules"]
        TG["TileGenerationService"]
        SJ["StorageJanitor"]
        AU["audit/: AuditLogService, AuditEvent"]
        RL["TileRateLimiter"]
        SU["SignedUrlService"]
    end
    M[("MySQL: users, documents, shares, audit events (Flyway V1, V2)")]
    D[("Disk: tiles")]
    B --> SEC --> DC
    SEC --> UD
    SEC --> PC
    SEC --> TC
    SEC --> AD
    DC --> DS
    PC --> DS
    TC --> DS
    DS -.-> M
    DC --> TG
    TG -.-> D
    SJ -.-> D
    SJ -.-> M
    DC --> AU
    TC --> AU
    AD --> AU
    AU -.-> M
    TC --> RL
    TC --> SU
```

*Figure: Blueprint v2. Text description: documents, shares and the audit trail move into MySQL; one service decides who may view or manage a document and is consulted on the list, the tile-URL request and every tile request.*

## What changed since v1
- New `document/` package (`Document`, `Visibility`, `DocumentService`, `DocumentRepository`, `Viewer`) replaces the in-memory `DocumentRegistry`; migration `V2__documents_shares_audit.sql`.
- Documents have an owner and a visibility plus per-user shares; new endpoints for rename, replace file, delete, and shares.
- The audit log becomes persistent (`audit/` package) with search and CSV export.
- `UserDirectoryController` (share picker), `StorageJanitor` and `FileOperations` added; a Manage page in the frontend.
