package com.example.securedocviewer.service;

import com.example.securedocviewer.config.ViewerProperties;
import com.example.securedocviewer.exception.DocumentNotFoundException;
import com.example.securedocviewer.model.DocumentManifest;
import com.example.securedocviewer.model.PageInfo;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Turns an uploaded PDF into a directory of per-tile PNGs, one subfolder per
 * page, plus an in-memory-derivable manifest of the resulting grid. This
 * mirrors what the flipbook-style readers we discussed actually do: nobody
 * downloads "the PDF", because the PDF stops existing as a single file the
 * moment it's rendered — only these disconnected tile fragments remain on
 * disk, and none of them means anything by itself.
 *
 * <p>Layout on disk: {@code {storageRoot}/{documentId}/page-{n}/tile-{row}_{col}.png}
 */
@Service
public class TileGenerationService {

    private final ViewerProperties properties;

    public TileGenerationService(ViewerProperties properties) {
        this.properties = properties;
    }

    public DocumentManifest ingest(String title, byte[] pdfBytes) throws IOException {
        String documentId = UUID.randomUUID().toString();
        Path docRoot = storageRoot(documentId);
        Files.createDirectories(docRoot);

        List<PageInfo> pages = new ArrayList<>();

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFRenderer renderer = new PDFRenderer(document);
            int pageCount = document.getNumberOfPages();

            for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
                BufferedImage rendered = renderer.renderImageWithDPI(pageIndex, properties.getRenderDpi());
                PageInfo pageInfo = tileAndSave(documentId, pageIndex, rendered);
                pages.add(pageInfo);
            }
        }

        return new DocumentManifest(documentId, title, pages.size(), pages);
    }

    /**
     * Slices one rendered page into tiles and writes them to disk.
     * Package-private so it can be exercised directly with a synthetic image.
     */
    PageInfo tileAndSave(String documentId, int pageIndex, BufferedImage pageImage) throws IOException {
        int tileSize = properties.getTileSize();
        int width = pageImage.getWidth();
        int height = pageImage.getHeight();
        int cols = TileGrid.tileCount(width, tileSize);
        int rows = TileGrid.tileCount(height, tileSize);

        Path pageDir = storageRoot(documentId).resolve("page-" + pageIndex);
        Files.createDirectories(pageDir);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                BufferedImage tile = TileGrid.sliceTile(pageImage, row, col, tileSize);
                File tileFile = pageDir.resolve("tile-" + row + "_" + col + ".png").toFile();
                ImageIO.write(tile, "png", tileFile);
            }
        }

        return new PageInfo(pageIndex, rows, cols, tileSize, width, height);
    }

    /**
     * Loads the raw (un-watermarked) tile image for a given tile coordinate.
     * Watermarking happens afterward, per-request, in {@code WatermarkService} —
     * this method never returns a copy that's safe to serve directly.
     */
    public BufferedImage loadRawTile(String documentId, int page, int row, int col) throws IOException {
        Path tilePath = storageRoot(documentId)
                .resolve("page-" + page)
                .resolve("tile-" + row + "_" + col + ".png");

        if (!Files.exists(tilePath)) {
            throw new DocumentNotFoundException(
                    "No such tile: document=%s page=%d row=%d col=%d".formatted(documentId, page, row, col));
        }

        return ImageIO.read(tilePath.toFile());
    }

    private Path storageRoot(String documentId) {
        return Path.of(properties.getStorageRoot(), documentId);
    }
}
