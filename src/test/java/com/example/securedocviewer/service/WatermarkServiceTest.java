package com.example.securedocviewer.service;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class WatermarkServiceTest {

    private BufferedImage blankWhiteTile(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, size, size);
        g.dispose();
        return image;
    }

    @Test
    void watermarkPreservesDimensions() {
        WatermarkService service = new WatermarkService();
        BufferedImage source = blankWhiteTile(256);

        BufferedImage stamped = service.applyWatermark(source, "alice@example.com");

        assertEquals(source.getWidth(), stamped.getWidth());
        assertEquals(source.getHeight(), stamped.getHeight());
    }

    @Test
    void watermarkActuallyChangesSomePixels() {
        WatermarkService service = new WatermarkService();
        BufferedImage source = blankWhiteTile(256);

        BufferedImage stamped = service.applyWatermark(source, "alice@example.com");

        boolean anyPixelDiffers = false;
        outer:
        for (int x = 0; x < source.getWidth(); x++) {
            for (int y = 0; y < source.getHeight(); y++) {
                if (source.getRGB(x, y) != stamped.getRGB(x, y)) {
                    anyPixelDiffers = true;
                    break outer;
                }
            }
        }

        assertTrue(anyPixelDiffers, "expected the watermark text to change at least one pixel");
    }

    @Test
    void watermarkCoversEveryQuadrantOfAFullTile() {
        // A single centered instance (the old behavior) leaves the corners
        // untouched. The tiled pattern must reach all four quadrants so that
        // no crop of the tile can dodge the mark entirely.
        WatermarkService service = new WatermarkService();
        BufferedImage source = blankWhiteTile(256);

        BufferedImage stamped = service.applyWatermark(source, "alice@example.com");

        assertTrue(quadrantHasMark(source, stamped, 0, 0, 128, 128), "top-left quadrant unmarked");
        assertTrue(quadrantHasMark(source, stamped, 128, 0, 128, 128), "top-right quadrant unmarked");
        assertTrue(quadrantHasMark(source, stamped, 0, 128, 128, 128), "bottom-left quadrant unmarked");
        assertTrue(quadrantHasMark(source, stamped, 128, 128, 128, 128), "bottom-right quadrant unmarked");
    }

    @Test
    void watermarkStillAppearsOnASmallCroppedEdgeTile() {
        // Edge/bottom tiles in the grid are cropped shorter than a full
        // tile (TileGrid.sliceTile), so this simulates the worst case: a
        // sliver far smaller than the font/step sizing was computed for.
        WatermarkService service = new WatermarkService();
        BufferedImage source = blankWhiteTile(256).getSubimage(0, 0, 40, 30);

        BufferedImage stamped = service.applyWatermark(source, "alice@example.com");

        boolean anyPixelDiffers = false;
        outer:
        for (int x = 0; x < source.getWidth(); x++) {
            for (int y = 0; y < source.getHeight(); y++) {
                if (source.getRGB(x, y) != stamped.getRGB(x, y)) {
                    anyPixelDiffers = true;
                    break outer;
                }
            }
        }

        assertTrue(anyPixelDiffers, "expected the watermark to reach a small cropped edge tile");
    }

    @Test
    void adjacentCopiesNeverOverprintEachOther() {
        // Regression: spacing used to be a fixed multiple of the font size
        // (~150px) while the label rendered ~400px wide, so every copy was
        // drawn on top of its neighbours and the mark was unreadable.
        BufferedImage scratch = blankWhiteTile(256);
        Graphics2D g = scratch.createGraphics();
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        String[] lines = {"a.really.long.username@example.com", "2026-09-18 10:51:45"};

        WatermarkService.Layout layout = WatermarkService.Layout.of(g.getFontMetrics(), lines);
        g.dispose();

        assertTrue(layout.stepX() > layout.blockWidth(), "copies on a row overlap horizontally");
        assertTrue(layout.stepY() >= layout.lineHeight() * lines.length, "rows overlap vertically");
    }

    @Test
    void traceCodeChangesTheMarkAndWiderSpacingInksFewerPixels() {
        BufferedImage source = blankWhiteTile(256);

        BufferedImage withoutTrace = new WatermarkService(0.2f, 1.5).applyWatermark(source, "alice");
        BufferedImage withTrace = new WatermarkService(0.2f, 1.5).applyWatermark(source, "alice", "k3Fz9a");
        assertTrue(countChanged(source, withoutTrace) != countChanged(source, withTrace)
                || !sameImage(withoutTrace, withTrace), "trace code made no difference to the mark");

        int dense = countChanged(source, new WatermarkService(0.2f, 0.5).applyWatermark(source, "alice", "k3Fz9a"));
        int airy = countChanged(source, new WatermarkService(0.2f, 4.0).applyWatermark(source, "alice", "k3Fz9a"));
        assertTrue(airy < dense, "wider spacing should ink fewer pixels (dense=" + dense + ", airy=" + airy + ")");
    }

    private static int countChanged(BufferedImage a, BufferedImage b) {
        int changed = 0;
        for (int x = 0; x < a.getWidth(); x++) {
            for (int y = 0; y < a.getHeight(); y++) {
                if (a.getRGB(x, y) != b.getRGB(x, y)) {
                    changed++;
                }
            }
        }
        return changed;
    }

    private static boolean sameImage(BufferedImage a, BufferedImage b) {
        return countChanged(a, b) == 0;
    }

    private boolean quadrantHasMark(BufferedImage source, BufferedImage stamped,
                                     int startX, int startY, int w, int h) {
        for (int x = startX; x < startX + w; x++) {
            for (int y = startY; y < startY + h; y++) {
                if (source.getRGB(x, y) != stamped.getRGB(x, y)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Test
    void differentViewerLabelsProduceDifferentOutput() {
        WatermarkService service = new WatermarkService();
        BufferedImage source = blankWhiteTile(256);

        BufferedImage stampedAlice = service.applyWatermark(source, "alice@example.com");
        BufferedImage stampedBob = service.applyWatermark(source, "bob@example.com");

        boolean anyPixelDiffers = false;
        outer:
        for (int x = 0; x < source.getWidth(); x++) {
            for (int y = 0; y < source.getHeight(); y++) {
                if (stampedAlice.getRGB(x, y) != stampedBob.getRGB(x, y)) {
                    anyPixelDiffers = true;
                    break outer;
                }
            }
        }

        assertTrue(anyPixelDiffers, "expected different viewer labels to render differently");
    }

    @Test
    void aCroppedEdgeTileCarriesExactlyTheTopLeftOfAFullTilesMark() {
        WatermarkService service = new WatermarkService(0.2f, 1.5);
        for (int attempt = 0; attempt < 2; attempt++) { // retry once if the minute ticks over in between
            BufferedImage full = service.applyWatermark(blank(512, 512), "alice", "ABC123", 512);
            BufferedImage edge = service.applyWatermark(blank(512, 100), "alice", "ABC123", 512);
            BufferedImage corner = service.applyWatermark(blank(90, 70), "alice", "ABC123", 512);
            if (sameRegion(full, edge) && sameRegion(full, corner)) {
                return;
            }
        }
        fail("edge tiles are not marked like the matching part of a full tile");
    }

    private static BufferedImage blank(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = image.createGraphics();
        g.setColor(java.awt.Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.dispose();
        return image;
    }

    private static boolean sameRegion(BufferedImage full, BufferedImage part) {
        for (int y = 0; y < part.getHeight(); y++) {
            for (int x = 0; x < part.getWidth(); x++) {
                if (full.getRGB(x, y) != part.getRGB(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }
}
