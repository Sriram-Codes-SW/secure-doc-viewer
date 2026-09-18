package com.example.securedocviewer.audit;

public enum AuditEventType {
    TILE_VIEWED,
    SIGN_IN,
    SIGN_IN_FAILED,
    SIGN_IN_LOCKED,
    SIGN_OUT,
    PASSWORD_CHANGED,
    SESSION_REVOKED,
    USER_CREATED,
    USER_UPDATED,
    USER_PASSWORD_RESET,
    DOCUMENT_UPLOADED,
    DOCUMENT_REPLACED,
    DOCUMENT_UPDATED,
    DOCUMENT_DELETED,
    DOCUMENT_SHARED,
    DOCUMENT_UNSHARED,
    /** A request for a document or tile the user isn't allowed to see. */
    ACCESS_DENIED,
    /** Recorded at most once per user per rate-limit window, not per rejected request. */
    RATE_LIMITED
}
