package com.example.securedocviewer.service;

import java.awt.image.BufferedImage;

/**
 * Pure, dependency-free tile-grid math, deliberately kept separate from
 * {@link TileGenerationService} so the slicing logic can be unit-tested
 * against a synthetic image without dragging in PDF rendering.
 *
 * <p>The grid is row-major and top-left anchored. Tiles along the right and
 * bottom edges are cropped short rather than padded, so reassembling every
 * tile at {@code (col * tileSize, row * tileSize)} reproduces the source
 * image exactly, with no seams and no bleed.
 */
public final class TileGrid {

    private TileGrid() {
    }

    /** Number of tiles needed to cover {@code lengthPx} pixels: ceil(lengthPx / tileSize). */
    public static int tileCount(int lengthPx, int tileSize) {
        if (lengthPx <= 0 || tileSize <= 0) {
            throw new IllegalArgumentException("lengthPx and tileSize must both be positive");
        }
        return (lengthPx + tileSize - 1) / tileSize;
    }

    /** Extracts a single tile, cropped to the image bounds at the right/bottom edges. */
    public static BufferedImage sliceTile(BufferedImage page, int row, int col, int tileSize) {
        int x = col * tileSize;
        int y = row * tileSize;
        if (x >= page.getWidth() || y >= page.getHeight()) {
            throw new IllegalArgumentException("Tile (" + row + "," + col + ") is outside the image bounds");
        }
        int w = Math.min(tileSize, page.getWidth() - x);
        int h = Math.min(tileSize, page.getHeight() - y);
        return page.getSubimage(x, y, w, h);
    }
}
