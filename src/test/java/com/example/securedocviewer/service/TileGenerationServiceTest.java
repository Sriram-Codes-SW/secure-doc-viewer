package com.example.securedocviewer.service;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
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

        TileGenerationService service = new TileGenerationService(properties, new ViewerMetrics(new SimpleMeterRegistry()));
        TileGenerationService.RenderedDocument rendered = service.render(new java.io.ByteArrayInputStream(onePageLetterSizedPdf()));

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

        TileGenerationService service = new TileGenerationService(properties, new ViewerMetrics(new SimpleMeterRegistry()));
        TileGenerationService.RenderedDocument rendered = service.render(new java.io.ByteArrayInputStream(onePageLetterSizedPdf()));
        service.commit(rendered, "doc-1", 1);
        PageInfo page = rendered.pages().get(0);

        for (int row = 0; row < page.rows(); row++) {
            for (int col = 0; col < page.cols(); col++) {
                BufferedImage tile = service.loadRawTile("doc-1", 1, page.page(), row, col);
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
        TileGenerationService service = new TileGenerationService(properties, new ViewerMetrics(new SimpleMeterRegistry()));

        assertThrows(BadRequestException.class,
                () -> service.render(new java.io.ByteArrayInputStream("definitely not a pdf".getBytes(java.nio.charset.StandardCharsets.UTF_8))));

        try (var staged = Files.list(tempDir.resolve(TileGenerationService.STAGING_DIR))) {
            assertEquals(0, staged.count(), "failed render left a staging directory behind");
        }
    }

    @Test
    void eachRenderIsCommittedToItsOwnVersionAndOldVersionsCanBeRemoved(@TempDir Path tempDir) throws IOException {
        ViewerProperties properties = new ViewerProperties();
        properties.setStorageRoot(tempDir.toString());
        properties.setTileSize(200);
        properties.setRenderDpi(72);
        TileGenerationService service = new TileGenerationService(properties, new ViewerMetrics(new SimpleMeterRegistry()));

        service.commit(service.render(new java.io.ByteArrayInputStream(onePageLetterSizedPdf())), "doc-1", 1);
        service.commit(service.render(new java.io.ByteArrayInputStream(onePageLetterSizedPdf())), "doc-1", 2);
        assertNotNull(service.loadRawTile("doc-1", 1, 0, 0, 0));
        assertNotNull(service.loadRawTile("doc-1", 2, 0, 0, 0));

        service.deleteVersion("doc-1", 1);

        assertFalse(Files.exists(tempDir.resolve("doc-1").resolve("v1")), "superseded version survived");
        assertThrows(com.example.securedocviewer.exception.TileGoneException.class,
                () -> service.loadRawTile("doc-1", 1, 0, 0, 0));
        assertNotNull(service.loadRawTile("doc-1", 2, 0, 0, 0));
        // A version directory is never reused, so a partial move can't mix tiles.
        assertThrows(IOException.class, () -> service.commit(
                service.render(new java.io.ByteArrayInputStream(onePageLetterSizedPdf())), "doc-1", 2));
        try (var staged = Files.list(tempDir.resolve(TileGenerationService.STAGING_DIR))) {
            assertEquals(1, staged.count(), "the refused render stays in staging for discard/janitor");
        }
    }

    @Test
    void rendersBeyondTheConcurrencyLimitAreTurnedAwayWithRetryAfter(@TempDir Path tempDir) throws Exception {
        ViewerProperties properties = new ViewerProperties();
        properties.setStorageRoot(tempDir.toString());
        properties.setMaxConcurrentRenders(1);
        properties.setRenderQueueTimeoutSeconds(1);
        TileGenerationService service = new TileGenerationService(properties, new ViewerMetrics(new SimpleMeterRegistry()));

        // Hold the only permit with an upload whose stream never ends until released.
        java.util.concurrent.CountDownLatch release = new java.util.concurrent.CountDownLatch(1);
        java.io.InputStream blocking = new java.io.InputStream() {
            @Override
            public int read() throws IOException {
                try {
                    release.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return -1;
            }
        };
        Thread holder = new Thread(() -> {
            try {
                service.render(blocking);
            } catch (Exception ignored) {
                // an empty upload is rejected once released; irrelevant here
            }
        });
        holder.start();
        Thread.sleep(200);

        com.example.securedocviewer.exception.ServiceBusyException busy = assertThrows(
                com.example.securedocviewer.exception.ServiceBusyException.class,
                () -> service.render(new java.io.ByteArrayInputStream(onePageLetterSizedPdf())));
        assertEquals(1, busy.getRetryAfterSeconds());
        release.countDown();
        holder.join(5000);
    }

    @Test
    void deleteTilesRemovesTheDocumentDirectory(@TempDir Path tempDir) throws IOException {
        ViewerProperties properties = new ViewerProperties();
        properties.setStorageRoot(tempDir.toString());
        properties.setTileSize(200);
        properties.setRenderDpi(72);
        TileGenerationService service = new TileGenerationService(properties, new ViewerMetrics(new SimpleMeterRegistry()));
        service.commit(service.render(new java.io.ByteArrayInputStream(onePageLetterSizedPdf())), "doc-1", 1);

        service.deleteTiles("doc-1");

        assertFalse(Files.exists(tempDir.resolve("doc-1")));
    }

    @Test
    void tooManyPagesIsRejectedBeforeRendering(@TempDir Path tempDir) throws IOException {
        ViewerProperties properties = new ViewerProperties();
        properties.setStorageRoot(tempDir.toString());
        properties.setMaxPages(2);
        TileGenerationService service = new TileGenerationService(properties, new ViewerMetrics(new SimpleMeterRegistry()));

        BadRequestException e = assertThrows(BadRequestException.class,
                () -> service.render(new java.io.ByteArrayInputStream(pdfWithPages(3, PDRectangle.A6))));
        assertTrue(e.getMessage().contains("limit is 2"), e.getMessage());
    }

    @Test
    void oversizedPagesAreRejectedBeforeRendering(@TempDir Path tempDir) throws IOException {
        ViewerProperties properties = new ViewerProperties();
        properties.setStorageRoot(tempDir.toString());
        properties.setRenderDpi(150);
        properties.setMaxPagePixels(1_000_000);
        TileGenerationService service = new TileGenerationService(properties, new ViewerMetrics(new SimpleMeterRegistry()));

        // Letter at 150 DPI is 1275x1650 = ~2.1M px.
        BadRequestException e = assertThrows(BadRequestException.class,
                () -> service.render(new java.io.ByteArrayInputStream(pdfWithPages(1, PDRectangle.LETTER))));
        assertTrue(e.getMessage().startsWith("Page 1 is too large"), e.getMessage());
    }

    @Test
    void theUploadedPdfIsNotKeptWithTheTiles(@TempDir Path tempDir) throws IOException {
        ViewerProperties properties = new ViewerProperties();
        properties.setStorageRoot(tempDir.toString());
        properties.setTileSize(200);
        properties.setRenderDpi(72);
        TileGenerationService service = new TileGenerationService(properties, new ViewerMetrics(new SimpleMeterRegistry()));

        service.commit(service.render(new java.io.ByteArrayInputStream(onePageLetterSizedPdf())), "doc-1", 1);

        try (var files = Files.walk(tempDir.resolve("doc-1"))) {
            assertTrue(files.noneMatch(f -> f.toString().endsWith(".pdf")), "the source PDF was committed with the tiles");
        }
    }

    private static byte[] pdfWithPages(int pages, PDRectangle size) throws IOException {
        try (PDDocument document = new PDDocument()) {
            for (int i = 0; i < pages; i++) {
                document.addPage(new PDPage(size));
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        }
    }

    @Test
    void aRenderThatTakesTooLongIsAbandonedAndFreesItsSlot(@org.junit.jupiter.api.io.TempDir Path storage) throws Exception {
        ViewerProperties slow = new ViewerProperties();
        slow.setStorageRoot(storage.toString());
        slow.setSigningSecret("test-signing-secret-0123456789-abcdef");
        slow.setMaxConcurrentRenders(1);
        slow.setRenderTimeout(java.time.Duration.ofMillis(1));
        TileGenerationService service = new TileGenerationService(slow, new ViewerMetrics(new SimpleMeterRegistry()));

        byte[] manyPages = pdfWithPages(40);
        com.example.securedocviewer.exception.BadRequestException e = assertThrows(
                com.example.securedocviewer.exception.BadRequestException.class,
                () -> service.render(new java.io.ByteArrayInputStream(manyPages)));
        assertTrue(e.getMessage().contains("too long"));

        // The only slot is free again at once, and the abandoned render cleans up after itself.
        slow.setRenderTimeout(java.time.Duration.ofMinutes(1));
        TileGenerationService.RenderedDocument ok = service.render(new java.io.ByteArrayInputStream(pdfWithPages(1)));
        assertEquals(1, ok.pages().size());
        Path staging = storage.resolve(TileGenerationService.STAGING_DIR);
        long deadline = System.currentTimeMillis() + 10_000;
        while (System.currentTimeMillis() < deadline) {
            try (var dirs = java.nio.file.Files.list(staging)) {
                if (dirs.count() == 1) {
                    break; // only the successful render is left
                }
            }
            Thread.sleep(100);
        }
        try (var dirs = java.nio.file.Files.list(staging)) {
            assertEquals(1, dirs.count(), "abandoned render left its staging directory behind");
        }
    }

    private static byte[] pdfWithPages(int pages) throws IOException {
        try (org.apache.pdfbox.pdmodel.PDDocument document = new org.apache.pdfbox.pdmodel.PDDocument()) {
            for (int i = 0; i < pages; i++) {
                document.addPage(new org.apache.pdfbox.pdmodel.PDPage(org.apache.pdfbox.pdmodel.common.PDRectangle.A4));
            }
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        }
    }
}
