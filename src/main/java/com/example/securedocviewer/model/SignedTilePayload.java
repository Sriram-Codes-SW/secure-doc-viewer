package com.example.securedocviewer.model;

/**
 * The decoded, verified contents of a tile access token: which physical tile
 * it grants access to, a keyed binding to the session it was issued for (never
 * the session id itself), and when it expires.
 * A token that fails signature verification or is past {@code expiresAtEpochSeconds}
 * is rejected before this record is ever constructed.
 */
public record SignedTilePayload(
        String documentId,
        int page,
        int row,
        int col,
        String sessionBinding,
        long expiresAtEpochSeconds
) {
    public String canonicalString() {
        return documentId + "|" + page + "|" + row + "|" + col + "|" + sessionBinding + "|" + expiresAtEpochSeconds;
    }
}
