-- Documents are owned by the account that uploaded them. Visibility is either
-- PRIVATE (owner + explicitly shared users) or EVERYONE (any signed-in user).
CREATE TABLE document (
    id          VARCHAR(36)  NOT NULL,
    title       VARCHAR(200) NOT NULL,
    owner_id    BIGINT       NOT NULL,
    visibility  VARCHAR(20)  NOT NULL,
    page_count  INT          NOT NULL,
    tile_size   INT          NOT NULL,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_document_owner FOREIGN KEY (owner_id) REFERENCES app_user (id)
);

CREATE INDEX ix_document_owner ON document (owner_id);

-- One row per rendered page: the tile grid the viewer needs to lay tiles out.
-- (Column names avoid ROWS, which is reserved in MySQL 8.)
CREATE TABLE document_page (
    document_id    VARCHAR(36) NOT NULL,
    page_index     INT         NOT NULL,
    tile_rows      INT         NOT NULL,
    tile_cols      INT         NOT NULL,
    page_width_px  INT         NOT NULL,
    page_height_px INT         NOT NULL,
    PRIMARY KEY (document_id, page_index),
    CONSTRAINT fk_document_page_document FOREIGN KEY (document_id) REFERENCES document (id) ON DELETE CASCADE
);

-- Explicit per-user grants for PRIVATE documents.
CREATE TABLE document_share (
    document_id VARCHAR(36) NOT NULL,
    user_id     BIGINT      NOT NULL,
    PRIMARY KEY (document_id, user_id),
    CONSTRAINT fk_document_share_document FOREIGN KEY (document_id) REFERENCES document (id) ON DELETE CASCADE,
    CONSTRAINT fk_document_share_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE
);

CREATE INDEX ix_document_share_user ON document_share (user_id);

-- Append-only security/audit trail. Deliberately denormalised (username and
-- document title are copied, not foreign keys) so records stay readable after
-- the account or document they mention is renamed or deleted.
CREATE TABLE audit_event (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    occurred_at    DATETIME(6)  NOT NULL,
    event_type     VARCHAR(40)  NOT NULL,
    username       VARCHAR(64)  NULL,
    session_handle VARCHAR(32)  NULL,
    client_ip      VARCHAR(45)  NULL,
    document_id    VARCHAR(36)  NULL,
    document_title VARCHAR(200) NULL,
    page_index     INT          NULL,
    tile_row       INT          NULL,
    tile_col       INT          NULL,
    detail         VARCHAR(255) NULL,
    PRIMARY KEY (id)
);

CREATE INDEX ix_audit_event_time ON audit_event (occurred_at);
CREATE INDEX ix_audit_event_user_time ON audit_event (username, occurred_at);
CREATE INDEX ix_audit_event_document_time ON audit_event (document_id, occurred_at);
CREATE INDEX ix_audit_event_type_time ON audit_event (event_type, occurred_at);
