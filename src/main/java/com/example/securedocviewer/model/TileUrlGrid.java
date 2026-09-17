package com.example.securedocviewer.model;

/**
 * A row-major grid of signed, single-use-window tile URLs for one page.
 * {@code tileUrls[row][col]} is only ever valid for the session it was
 * issued to and expires a short time after this response is sent — the
 * frontend is expected to fetch it and then start pulling tiles promptly.
 */
public record TileUrlGrid(
        int page,
        int rows,
        int cols,
        int tileSize,
        String[][] tileUrls
) {
}
