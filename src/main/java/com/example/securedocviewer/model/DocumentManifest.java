package com.example.securedocviewer.model;

import java.util.List;

/**
 * Metadata returned to the client after a document has been tiled.
 * Deliberately contains no direct file paths or tile URLs — those are only
 * ever handed out as short-lived signed URLs via a separate endpoint, per
 * page, once a viewer requests to see that page.
 */
public record DocumentManifest(
        String documentId,
        String title,
        int pageCount,
        List<PageInfo> pages
) {
}
