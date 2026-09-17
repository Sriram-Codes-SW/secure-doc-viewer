package com.example.securedocviewer.service;

import org.junit.jupiter.api.Test;

import java.awt.Color;
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
}
