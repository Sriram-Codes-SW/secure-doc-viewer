package com.example.securedocviewer.document;

/** A row in the document library. {@code sharedWithCount} is only filled in for users who can manage it. */
public record DocumentSummary(
        String documentId,
        String title,
        int pageCount,
        String owner,
        Visibility visibility,
        long createdAtEpochSeconds,
        long updatedAtEpochSeconds,
        boolean canManage,
        Integer sharedWithCount
) {
}
