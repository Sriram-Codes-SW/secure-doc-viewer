package com.example.securedocviewer.service;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class TileGridTest {

    private BufferedImage noiseImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Random random = new Random(42);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, random.nextInt(0xFFFFFF));
            }
        }
        return image;
    }

    @Test
    void tileCountRoundsUpForPartialTiles() {
        assertEquals(1, TileGrid.tileCount(1, 256));
        assertEquals(1, TileGrid.tileCount(256, 256));
        assertEquals(2, TileGrid.tileCount(257, 256));
        assertEquals(13, TileGrid.tileCount(612, 50));
        assertEquals(16, TileGrid.tileCount(792, 50));
    }

    @Test
    void tileCountRejectsNonPositiveInputs() {
        assertThrows(IllegalArgumentException.class, () -> TileGrid.tileCount(0, 256));
        assertThrows(IllegalArgumentException.class, () -> TileGrid.tileCount(100, 0));
    }

    @Test
    void edgeTilesAreCroppedNotPadded() {
        BufferedImage page = noiseImage(612, 792);

        BufferedImage interior = TileGrid.sliceTile(page, 0, 0, 50);
        assertEquals(50, interior.getWidth());
        assertEquals(50, interior.getHeight());

        BufferedImage rightEdge = TileGrid.sliceTile(page, 0, 12, 50); // 612 - 600 = 12px wide
        assertEquals(12, rightEdge.getWidth());

        BufferedImage bottomEdge = TileGrid.sliceTile(page, 15, 0, 50); // 792 - 750 = 42px tall
        assertEquals(42, bottomEdge.getHeight());
    }

    @Test
    void sliceTileRejectsOutOfBoundsCoordinates() {
        BufferedImage page = noiseImage(100, 100);
        assertThrows(IllegalArgumentException.class, () -> TileGrid.sliceTile(page, 0, 2, 50));
        assertThrows(IllegalArgumentException.class, () -> TileGrid.sliceTile(page, 2, 0, 50));
    }

    /**
     * The important guarantee: tiles are a lossless partition of the page.
     * Reassembling every tile at its grid offset must reproduce the source
     * image pixel-for-pixel — no seams, no overlap, no dropped edge strips.
     */
    @Test
    void tilesReassembleIntoTheOriginalImageExactly() {
        int width = 613; // deliberately not a multiple of the tile size
        int height = 457;
        int tileSize = 64;
        BufferedImage original = noiseImage(width, height);

        int cols = TileGrid.tileCount(width, tileSize);
        int rows = TileGrid.tileCount(height, tileSize);

        BufferedImage reassembled = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = reassembled.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                BufferedImage tile = TileGrid.sliceTile(original, row, col, tileSize);
                g.drawImage(tile, col * tileSize, row * tileSize, null);
            }
        }
        g.dispose();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                assertEquals(original.getRGB(x, y), reassembled.getRGB(x, y),
                        "pixel mismatch at (" + x + "," + y + ")");
            }
        }
    }
}
