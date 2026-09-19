package com.example.securedocviewer.model;

/**
 * The decoded, verified contents of a tile access token: which physical tile
 * of which render it grants access to, a keyed binding to the session it was issued for (never
 * the session id itself), and when it expires.
 * A token that fails signature verification or is past {@code expiresAtEpochSeconds}
 * is rejected before this record is ever constructed.
 */
public record SignedTilePayload(
        String documentId,
        int page,
        int row,
        int col,
        /** The render the URL was issued for; a replaced document refuses it (410). */
        int tileVersion,
        String sessionBinding,
        long expiresAtEpochSeconds
) {
    public String canonicalString() {
        return documentId + "|" + page + "|" + row + "|" + col + "|" + tileVersion + "|" + sessionBinding + "|" + expiresAtEpochSeconds;
    }
}
