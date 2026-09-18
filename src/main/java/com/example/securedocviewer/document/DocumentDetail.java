package com.example.securedocviewer.document;

import com.example.securedocviewer.model.PageInfo;

import java.util.List;

/**
 * Everything the viewer needs to lay out a document, plus what the manage
 * screen shows. {@code sharedWith} is only filled in for users who can manage it.
 */
public record DocumentDetail(
        String documentId,
        String title,
        int pageCount,
        String owner,
        Visibility visibility,
        long createdAtEpochSeconds,
        long updatedAtEpochSeconds,
        boolean canManage,
        List<String> sharedWith,
        List<PageInfo> pages
) {
}
