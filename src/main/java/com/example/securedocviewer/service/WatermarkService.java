package com.example.securedocviewer.service;

import org.springframework.stereotype.Service;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.time.format.DateTimeFormatter;
import java.time.Instant;
import java.time.ZoneOffset;

/**
 * Stamps an identifying watermark onto a tile image at serve time, not at
 * generation time. That ordering matters: if watermarking happened once
 * during ingest, every viewer would receive an identical, un-attributable
 * copy of the tile, and pre-stamping per user up front would mean storing
 * N copies of every tile for N viewers. Watermarking on the way out means
 * one stored tile serves every viewer, and every response is still
 * individually traceable back to who requested it and when.
 */
@Service
public class WatermarkService {

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);

    public BufferedImage applyWatermark(BufferedImage source, String viewerLabel) {
        BufferedImage stamped = new BufferedImage(
                source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = stamped.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawImage(source, 0, 0, null);

            String label = viewerLabel + " · " + TIMESTAMP_FORMAT.format(Instant.now());

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.22f));
            g.setColor(Color.RED);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.max(10, source.getHeight() / 12)));
            g.rotate(-Math.PI / 6, source.getWidth() / 2.0, source.getHeight() / 2.0);
            g.drawString(label, -source.getWidth() / 4, source.getHeight() / 2);
        } finally {
            g.dispose();
        }

        return stamped;
    }
}
