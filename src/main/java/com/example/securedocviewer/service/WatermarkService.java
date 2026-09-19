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

    private final float opacity;
    private final double spacing;

    /** Defaults; used by unit tests. */
    public WatermarkService() {
        this(0.2f, 1.5);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public WatermarkService(com.example.securedocviewer.config.ViewerProperties properties) {
        this(properties.getWatermarkOpacity(), properties.getWatermarkSpacing());
    }

    WatermarkService(float opacity, double spacing) {
        this.opacity = Math.max(0.05f, Math.min(0.6f, opacity));
        this.spacing = Math.max(0.5, Math.min(6.0, spacing));
    }

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
        return applyWatermark(source, viewerLabel, null);
    }

    /**
     * @param traceCode short code identifying the viewer's session (see
     *                  SessionKeys#adminHandle), so a leaked capture can be
     *                  matched to one specific sign-in in the audit log, not
     *                  just to a username. May be null.
     */
    public BufferedImage applyWatermark(BufferedImage source, String viewerLabel, String traceCode) {
        return applyWatermark(source, viewerLabel, traceCode, Math.max(source.getWidth(), source.getHeight()));
    }

    /**
     * @param nominalTileSize the document's full tile edge; cropped edge tiles are
     *                        smaller, but their mark must match the rest of the page
     */
    public BufferedImage applyWatermark(BufferedImage source, String viewerLabel, String traceCode, int nominalTileSize) {
        int width = source.getWidth();
        int height = source.getHeight();
        BufferedImage stamped = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = stamped.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.drawImage(source, 0, 0, null);

            String stamp = TIMESTAMP_FORMAT.format(Instant.now());
            String[] lines = {viewerLabel, traceCode == null ? stamp : stamp + " · " + traceCode};

            // Sized from a 256 px reference so bigger tiles get more copies, not bigger text.
            int fontSize = Math.max(MIN_FONT_SIZE, Math.min(nominalTileSize, 256) / FONT_DIVISOR);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fontSize));
            Layout layout = Layout.of(g.getFontMetrics(), lines, spacing);

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
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
            // The grid is anchored at the tile origin (rows and columns at fixed
            // multiples of the steps), so a cropped edge tile carries exactly the
            // top-left part of a full tile's pattern, at the same size.
            int span = width + height;
            for (int y = Math.floorDiv(-span, layout.stepY()) * layout.stepY(); y < span; y += layout.stepY()) {
                int rowOffset = (Math.floorDiv(y, layout.stepY()) % 2 == 0) ? 0 : layout.stepX() / 2;
                int firstX = Math.floorDiv(-span - rowOffset, layout.stepX()) * layout.stepX() + rowOffset;
                for (int x = firstX; x < span; x += layout.stepX()) {
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
            return of(metrics, lines, 1.0);
        }

        static Layout of(FontMetrics metrics, String[] lines, double spacing) {
            int blockWidth = 0;
            for (String line : lines) {
                blockWidth = Math.max(blockWidth, metrics.stringWidth(line));
            }
            int lineHeight = metrics.getHeight();
            int gap = (int) Math.round(metrics.getHeight() * spacing);
            return new Layout(blockWidth, lineHeight, blockWidth + gap, lineHeight * lines.length + gap / 2);
        }
    }
}
