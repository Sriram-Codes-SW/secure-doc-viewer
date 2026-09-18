package com.example.securedocviewer.service;

import com.example.securedocviewer.config.ViewerProperties;
import com.example.securedocviewer.exception.BadRequestException;
import com.example.securedocviewer.exception.DocumentNotFoundException;
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
 * page. Nobody downloads "the PDF", because the PDF stops existing as a
 * single file the moment it's rendered — only these disconnected tile
 * fragments remain on disk, and none of them means anything by itself.
 *
 * <p>Rendering happens in a staging directory and is only moved into place
 * by {@link #commit} once every page succeeded, so a corrupt or half-rendered
 * upload never leaves tiles behind, and replacing a document's PDF swaps the
 * whole tile set at once.
 *
 * <p>Layout on disk: {@code {storageRoot}/{documentId}/page-{n}/tile-{row}_{col}.png}
 */
@Service
public class TileGenerationService {

    /** Holds renders that haven't been committed yet; swept by StorageJanitor if abandoned. */
    public static final String STAGING_DIR = ".staging";

    /** A fully rendered PDF waiting to be committed under a document id, or discarded. */
    public record RenderedDocument(Path stagingDir, int tileSize, List<PageInfo> pages) {
    }

    private final ViewerProperties properties;

    public TileGenerationService(ViewerProperties properties) {
        this.properties = properties;
    }

    /**
     * Renders every page into a fresh staging directory. Throws
     * {@link BadRequestException} if the bytes aren't a readable PDF; on any
     * failure the staging directory is removed before the exception escapes.
     */
    public RenderedDocument render(byte[] pdfBytes) throws IOException {
        Path stagingDir = Path.of(properties.getStorageRoot(), STAGING_DIR, UUID.randomUUID().toString());
        Files.createDirectories(stagingDir);
        try {
            List<PageInfo> pages = new ArrayList<>();
            try (PDDocument document = loadPdf(pdfBytes)) {
                if (document.getNumberOfPages() == 0) {
                    throw new BadRequestException("The PDF has no pages.");
                }
                PDFRenderer renderer = new PDFRenderer(document);
                for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
                    BufferedImage rendered = renderer.renderImageWithDPI(pageIndex, properties.getRenderDpi());
                    pages.add(tileAndSave(stagingDir.resolve("page-" + pageIndex), pageIndex, rendered));
                }
            }
            return new RenderedDocument(stagingDir, properties.getTileSize(), pages);
        } catch (IOException | RuntimeException e) {
            try {
                FileOperations.deleteDirectory(stagingDir);
            } catch (IOException cleanupFailure) {
                // Never mask the real error (e.g. "not a readable PDF"); StorageJanitor removes it later.
                e.addSuppressed(cleanupFailure);
            }
            throw e;
        }
    }

    /**
     * Moves a render into place as {@code documentId}'s tiles, replacing any
     * existing tiles for that document.
     */
    public void commit(RenderedDocument rendered, String documentId) throws IOException {
        Path target = documentRoot(documentId);
        Path previous = null;
        if (Files.exists(target)) {
            previous = Path.of(properties.getStorageRoot(), STAGING_DIR, documentId + "-replaced-" + UUID.randomUUID());
            FileOperations.moveDirectory(target, previous);
        }
        try {
            FileOperations.moveDirectory(rendered.stagingDir(), target);
        } catch (IOException e) {
            if (previous != null) {
                FileOperations.moveDirectory(previous, target);
            }
            throw e;
        }
        if (previous != null) {
            FileOperations.deleteDirectory(previous);
        }
    }

    public void discard(RenderedDocument rendered) throws IOException {
        FileOperations.deleteDirectory(rendered.stagingDir());
    }

    public void deleteTiles(String documentId) throws IOException {
        FileOperations.deleteDirectory(documentRoot(documentId));
    }

    /**
     * Slices one rendered page into tiles and writes them to {@code pageDir}.
     * Package-private so it can be exercised directly with a synthetic image.
     */
    PageInfo tileAndSave(Path pageDir, int pageIndex, BufferedImage pageImage) throws IOException {
        int tileSize = properties.getTileSize();
        int width = pageImage.getWidth();
        int height = pageImage.getHeight();
        int cols = TileGrid.tileCount(width, tileSize);
        int rows = TileGrid.tileCount(height, tileSize);

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
        Path tilePath = documentRoot(documentId)
                .resolve("page-" + page)
                .resolve("tile-" + row + "_" + col + ".png");

        if (!Files.exists(tilePath)) {
            throw new DocumentNotFoundException(
                    "No such tile: document=%s page=%d row=%d col=%d".formatted(documentId, page, row, col));
        }

        return ImageIO.read(tilePath.toFile());
    }

    private static PDDocument loadPdf(byte[] pdfBytes) {
        try {
            return Loader.loadPDF(pdfBytes);
        } catch (IOException e) {
            // Covers non-PDF content, truncated files and password-protected PDFs.
            throw new BadRequestException("The file is not a readable PDF.");
        }
    }

    private Path documentRoot(String documentId) {
        return Path.of(properties.getStorageRoot(), documentId);
    }
}
