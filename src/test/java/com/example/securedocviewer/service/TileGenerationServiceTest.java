package com.example.securedocviewer.service;

import com.example.securedocviewer.config.ViewerProperties;
import com.example.securedocviewer.exception.BadRequestException;
import com.example.securedocviewer.model.PageInfo;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class TileGenerationServiceTest {

    /** US Letter at 72 DPI renders to exactly 612x792 pixels — used to make the tile-grid math predictable. */
    private byte[] onePageLetterSizedPdf() throws IOException {
        try (PDDocument document = new PDDocument()) {
            document.addPage(new PDPage(PDRectangle.LETTER));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        }
    }

    @Test
    void ingestComputesTileGridFromRenderedPageSize(@TempDir Path tempDir) throws IOException {
        ViewerProperties properties = new ViewerProperties();
        properties.setStorageRoot(tempDir.toString());
        properties.setTileSize(50);
        properties.setRenderDpi(72);

        TileGenerationService service = new TileGenerationService(properties);
        TileGenerationService.RenderedDocument rendered = service.render(onePageLetterSizedPdf());

        assertEquals(1, rendered.pages().size());
        PageInfo page = rendered.pages().get(0);

        assertEquals(612, page.pageWidthPx());
        assertEquals(792, page.pageHeightPx());
        assertEquals(13, page.cols(), "ceil(612/50)"); // 12 full tiles + 1 partial column
        assertEquals(16, page.rows(), "ceil(792/50)"); // 15 full tiles + 1 partial row
    }

    @Test
    void everyTileInTheGridIsActuallyWrittenAndReadable(@TempDir Path tempDir) throws IOException {
        ViewerProperties properties = new ViewerProperties();
        properties.setStorageRoot(tempDir.toString());
        properties.setTileSize(200);
        properties.setRenderDpi(72);

        TileGenerationService service = new TileGenerationService(properties);
        TileGenerationService.RenderedDocument rendered = service.render(onePageLetterSizedPdf());
        service.commit(rendered, "doc-1");
        PageInfo page = rendered.pages().get(0);

        for (int row = 0; row < page.rows(); row++) {
            for (int col = 0; col < page.cols(); col++) {
                BufferedImage tile = service.loadRawTile("doc-1", page.page(), row, col);
                assertNotNull(tile);
                assertTrue(tile.getWidth() > 0 && tile.getHeight() > 0);
                // Edge tiles are cropped shorter/narrower than tileSize; interior tiles are full-size.
                assertTrue(tile.getWidth() <= properties.getTileSize());
                assertTrue(tile.getHeight() <= properties.getTileSize());
            }
        }
    }

    @Test
    void aFileThatIsNotAPdfIsRejectedAndLeavesNothingOnDisk(@TempDir Path tempDir) throws IOException {
        ViewerProperties properties = new ViewerProperties();
        properties.setStorageRoot(tempDir.toString());
        TileGenerationService service = new TileGenerationService(properties);

        assertThrows(BadRequestException.class,
                () -> service.render("definitely not a pdf".getBytes(java.nio.charset.StandardCharsets.UTF_8)));

        try (var staged = Files.list(tempDir.resolve(TileGenerationService.STAGING_DIR))) {
            assertEquals(0, staged.count(), "failed render left a staging directory behind");
        }
    }

    @Test
    void committingOverAnExistingDocumentReplacesItsTiles(@TempDir Path tempDir) throws IOException {
        ViewerProperties properties = new ViewerProperties();
        properties.setStorageRoot(tempDir.toString());
        properties.setTileSize(200);
        properties.setRenderDpi(72);
        TileGenerationService service = new TileGenerationService(properties);

        service.commit(service.render(onePageLetterSizedPdf()), "doc-1");
        Path stalePage = tempDir.resolve("doc-1").resolve("page-7");
        Files.createDirectories(stalePage); // stands in for a page the new PDF no longer has

        service.commit(service.render(onePageLetterSizedPdf()), "doc-1");

        assertFalse(Files.exists(stalePage), "old tiles survived the replace");
        assertNotNull(service.loadRawTile("doc-1", 0, 0, 0));
        try (var staged = Files.list(tempDir.resolve(TileGenerationService.STAGING_DIR))) {
            assertEquals(0, staged.count(), "replace left staging debris behind");
        }
    }

    @Test
    void deleteTilesRemovesTheDocumentDirectory(@TempDir Path tempDir) throws IOException {
        ViewerProperties properties = new ViewerProperties();
        properties.setStorageRoot(tempDir.toString());
        properties.setTileSize(200);
        properties.setRenderDpi(72);
        TileGenerationService service = new TileGenerationService(properties);
        service.commit(service.render(onePageLetterSizedPdf()), "doc-1");

        service.deleteTiles("doc-1");

        assertFalse(Files.exists(tempDir.resolve("doc-1")));
    }
}
