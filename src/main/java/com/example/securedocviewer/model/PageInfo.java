package com.example.securedocviewer.model;

/**
 * Describes the tile grid for a single rendered page, plus the pixel
 * dimensions the frontend needs to lay tiles out on a &lt;canvas&gt;.
 */
public record PageInfo(
        int page,
        int rows,
        int cols,
        int tileSize,
        int pageWidthPx,
        int pageHeightPx
) {
}
