package com.example.securedocviewer.service;

import com.example.securedocviewer.config.ViewerProperties;
import com.example.securedocviewer.model.DocumentManifest;
import com.example.securedocviewer.model.PageInfo;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
        DocumentManifest manifest = service.ingest("Test Doc", onePageLetterSizedPdf());

        assertEquals(1, manifest.pageCount());
        PageInfo page = manifest.pages().get(0);

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
        DocumentManifest manifest = service.ingest("Test Doc", onePageLetterSizedPdf());
        PageInfo page = manifest.pages().get(0);

        for (int row = 0; row < page.rows(); row++) {
            for (int col = 0; col < page.cols(); col++) {
                BufferedImage tile = service.loadRawTile(manifest.documentId(), page.page(), row, col);
                assertNotNull(tile);
                assertTrue(tile.getWidth() > 0 && tile.getHeight() > 0);
                // Edge tiles are cropped shorter/narrower than tileSize; interior tiles are full-size.
                assertTrue(tile.getWidth() <= properties.getTileSize());
                assertTrue(tile.getHeight() <= properties.getTileSize());
            }
        }
    }
}
