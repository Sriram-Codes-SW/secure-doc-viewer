package com.example.securedocviewer.model;

/**
 * Describes the tile grid for a single rendered page, plus the pixel
 * dimensions the frontend needs to position each tile on the page.
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
