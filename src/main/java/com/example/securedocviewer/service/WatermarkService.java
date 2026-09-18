package com.example.securedocviewer.service;

import org.springframework.stereotype.Service;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
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

    /** Font size used for a standard 256px tile; scales with smaller tiles. */
    private static final int FONT_DIVISOR = 14;
    private static final int MIN_FONT_SIZE = 9;

    /**
     * Tiles are individually watermarked (see {@link com.example.securedocviewer.controller.TileController}),
     * but edge/bottom tiles in the grid are cropped shorter than a full tile
     * ({@link TileGrid#sliceTile}), so a single centered watermark instance
     * could land entirely outside a small cropped tile and leave it
     * unattributed. Instead the mark is repeated in a brick pattern that
     * overshoots the canvas in every direction, so every tile carries some
     * of it — and a full-size tile carries at least one complete, readable
     * copy.
     *
     * <p>The label is split onto two short lines (viewer, then timestamp)
     * rather than one long one: a single-line "viewer · timestamp" label is
     * wider than a whole tile at any readable font size, so no tile could
     * ever contain a complete copy of it.
     */
    public BufferedImage applyWatermark(BufferedImage source, String viewerLabel) {
        int width = source.getWidth();
        int height = source.getHeight();
        BufferedImage stamped = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = stamped.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.drawImage(source, 0, 0, null);

            String[] lines = {viewerLabel, TIMESTAMP_FORMAT.format(Instant.now())};

            int fontSize = Math.max(MIN_FONT_SIZE, Math.min(width, height) / FONT_DIVISOR);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fontSize));
            Layout layout = Layout.of(g.getFontMetrics(), lines);

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.28f));
            g.setColor(Color.RED);
            g.rotate(-Math.PI / 6);

            // Rotation is a rigid transform, so consecutive rows (constant
            // local y, spaced stepY apart) become parallel bands in device
            // space exactly stepY apart, and the +/-span horizontal range
            // (at least the tile's own diagonal) sweeps each band across the
            // whole tile. Bricking alternate rows by half a step wrapped
            // within stepX keeps every row's first copy inside the drawn
            // range. Steps come from the measured text block plus a gap, so
            // neighbouring copies never overprint each other.
            int span = width + height;
            int rowIndex = 0;
            for (int y = -span; y < span; y += layout.stepY(), rowIndex++) {
                int rowOffset = (rowIndex % 2 == 0) ? 0 : layout.stepX() / 2;
                for (int x = -span + rowOffset; x < span; x += layout.stepX()) {
                    for (int i = 0; i < lines.length; i++) {
                        g.drawString(lines[i], x, y + i * layout.lineHeight());
                    }
                }
            }
        } finally {
            g.dispose();
        }

        return stamped;
    }

    /**
     * Spacing for one watermark copy, derived from the rendered size of its
     * text block. Package-private so tests can assert the no-overlap
     * property directly instead of inferring it from pixels.
     */
    record Layout(int blockWidth, int lineHeight, int stepX, int stepY) {

        static Layout of(FontMetrics metrics, String[] lines) {
            int blockWidth = 0;
            for (String line : lines) {
                blockWidth = Math.max(blockWidth, metrics.stringWidth(line));
            }
            int lineHeight = metrics.getHeight();
            int gap = metrics.getHeight();
            return new Layout(blockWidth, lineHeight, blockWidth + gap, lineHeight * lines.length + gap / 2);
        }
    }
}
