-- Each render of a document's PDF lives in its own directory ({doc}/v{n});
-- the row points at the committed version, so replacing a PDF switches
-- versions atomically in one transaction. 0 = the original unversioned layout.
ALTER TABLE document ADD COLUMN tile_version INT NOT NULL DEFAULT 0;

-- Admin-set passwords (new accounts, resets) must be changed at first sign-in.
ALTER TABLE app_user ADD COLUMN must_change_password BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE app_user ADD COLUMN last_sign_in_at DATETIME(6) NULL;

-- Addresses an account has recently signed in from successfully (30 days).
-- They are exempt from the account-wide lockout, so failed guesses from
-- elsewhere cannot lock the real user out of their usual device. Stored as
-- a keyed hash of the address (IPv6 grouped by /64), never the raw IP.
CREATE TABLE account_known_ip (
    user_id         BIGINT      NOT NULL,
    ip_hash         VARCHAR(64) NOT NULL,
    last_success_at DATETIME(6) NOT NULL,
    PRIMARY KEY (user_id, ip_hash),
    CONSTRAINT fk_account_known_ip_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE
);
