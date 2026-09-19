package com.example.securedocviewer.service;

import com.example.securedocviewer.config.ViewerProperties;
import com.example.securedocviewer.exception.BadRequestException;
import com.example.securedocviewer.model.PageInfo;
import com.example.securedocviewer.exception.ServiceBusyException;
import com.example.securedocviewer.exception.TileGoneException;
import io.micrometer.core.instrument.Timer;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

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
 * <p>Layout on disk: {@code {storageRoot}/{documentId}/v{version}/page-{n}/tile-{row}_{col}.png}.
 * Every render gets a new version directory and the document row points at
 * the committed one, so replacing a PDF never mixes old and new tiles.
 * Version 0 is the original unversioned layout ({@code {documentId}/page-{n}}).
 *
 * <p>Rendering is CPU- and memory-heavy, so at most
 * {@code max-concurrent-renders} run at once; PDFBox buffers into temp files
 * rather than the heap and subsamples oversized embedded images.
 */
@Service
public class TileGenerationService {

    /** Holds renders that haven't been committed yet; swept by StorageJanitor if abandoned. */
    public static final String STAGING_DIR = ".staging";

    /** A fully rendered PDF waiting to be committed under a document id, or discarded. */
    public record RenderedDocument(Path stagingDir, int tileSize, List<PageInfo> pages) {
    }

    private final ViewerProperties properties;
    private static final Logger log = LoggerFactory.getLogger(TileGenerationService.class);

    private final Semaphore renderPermits;
    private final ViewerMetrics metrics;
    /**
     * Renders run here so the request thread can give up on one that takes too
     * long. Bounded by the render permits: a permit is held until its render
     * thread has actually stopped, so abandoned renders still count against
     * {@code max-concurrent-renders} and can't pile up CPU or heap behind it.
     */
    private final ExecutorService renderThreads;
    /** Renders the request gave up on that haven't reached their next page boundary yet. */
    private final AtomicInteger abandonedRunning = new AtomicInteger();

    private enum RenderState { RUNNING, DONE, ABANDONED }

    public TileGenerationService(ViewerProperties properties, ViewerMetrics metrics) {
        this.properties = properties;
        this.metrics = metrics;
        int slots = Math.max(1, properties.getMaxConcurrentRenders());
        this.renderPermits = new Semaphore(slots, true);
        this.renderThreads = Executors.newFixedThreadPool(slots, Thread.ofPlatform().name("pdf-render-", 0).daemon().factory());
        metrics.abandonedRendersRunning(abandonedRunning::get);
    }

    /**
     * Renders every page into a fresh staging directory. The upload is
     * streamed to a temporary file and parsed from disk rather than held in
     * memory. Throws {@link BadRequestException} if it isn't a readable PDF or
     * exceeds the page-count or page-size limits — all checked before any
     * page is rendered. On any failure the staging directory is removed.
     */
    public RenderedDocument render(InputStream pdf) throws IOException {
        acquireRenderPermit();
        Timer.Sample timing = metrics.renderStarted();
        boolean handedToRenderThread = false;
        try {
            Path stagingDir = Path.of(properties.getStorageRoot(), STAGING_DIR, UUID.randomUUID().toString());
            Files.createDirectories(stagingDir);
            Path source = stagingDir.resolve("upload.pdf");
            try {
                Files.copy(pdf, source);
            } catch (IOException | RuntimeException e) {
                discardStaging(stagingDir, e);
                throw e;
            }
            AtomicBoolean cancelled = new AtomicBoolean();
            AtomicReference<RenderState> state = new AtomicReference<>(RenderState.RUNNING);
            Future<RenderedDocument> job = renderThreads.submit(() -> {
                try {
                    RenderedDocument rendered = renderStaged(stagingDir, source, cancelled);
                    if (!state.compareAndSet(RenderState.RUNNING, RenderState.DONE)) {
                        // Finished just after the request gave up: nobody will commit it.
                        discardStaging(stagingDir, new CancellationException("abandoned"));
                        throw new CancellationException("Render abandoned after render-timeout");
                    }
                    return rendered;
                } finally {
                    if (state.get() == RenderState.ABANDONED) {
                        abandonedRunning.decrementAndGet();
                    }
                    renderPermits.release();
                    metrics.renderFinished(timing);
                }
            });
            handedToRenderThread = true;
            try {
                return job.get(properties.getRenderTimeout().toMillis(), TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                if (!state.compareAndSet(RenderState.RUNNING, RenderState.ABANDONED)) {
                    return awaitFinished(job); // it finished in the meantime: use it
                }
                // The render stops at its next page boundary, cleans up and only
                // then frees its slot; new uploads wait for (or 503 on) that slot.
                abandonedRunning.incrementAndGet();
                cancelled.set(true);
                job.cancel(true);
                metrics.renderTimedOut();
                log.warn("Rendering took longer than {}; abandoned {}", properties.getRenderTimeout(), stagingDir);
                throw new BadRequestException("This PDF took too long to prepare. Try a smaller or simpler file.");
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof IOException io) {
                    throw io;
                }
                if (cause instanceof RuntimeException runtime) {
                    throw runtime;
                }
                if (cause instanceof Error error) {
                    throw error;
                }
                throw new IOException(cause);
            } catch (InterruptedException e) {
                cancelled.set(true);
                job.cancel(true);
                Thread.currentThread().interrupt();
                throw new ServiceBusyException("Upload interrupted. Try again.", 5);
            }
        } finally {
            if (!handedToRenderThread) {
                renderPermits.release();
                metrics.renderFinished(timing);
            }
        }
    }

    private static RenderedDocument awaitFinished(Future<RenderedDocument> job) throws IOException {
        try {
            return job.get();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof IOException io) {
                throw io;
            }
            if (e.getCause() instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw new IOException(e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceBusyException("Upload interrupted. Try again.", 5);
        }
    }

    private void acquireRenderPermit() {
        try {
            if (!renderPermits.tryAcquire(properties.getRenderQueueTimeoutSeconds(), TimeUnit.SECONDS)) {
                metrics.renderRejected();
                throw new ServiceBusyException("The server is busy rendering other documents. Try again shortly.",
                        properties.getRenderQueueTimeoutSeconds());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceBusyException("Upload interrupted. Try again.", 5);
        }
    }

    private RenderedDocument renderStaged(Path stagingDir, Path source, AtomicBoolean cancelled) throws IOException {
        try {
            requirePdfSignature(source);
            List<PageInfo> pages = new ArrayList<>();
            try (PDDocument document = loadPdf(source)) {
                requireWithinLimits(document);
                PDFRenderer renderer = new PDFRenderer(document);
                // Decode huge embedded images at reduced resolution instead of in full.
                renderer.setSubsamplingAllowed(true);
                for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
                    if (cancelled.get() || Thread.currentThread().isInterrupted()) {
                        throw new CancellationException("Render abandoned after render-timeout");
                    }
                    BufferedImage rendered = renderer.renderImageWithDPI(pageIndex, properties.getRenderDpi());
                    pages.add(tileAndSave(stagingDir.resolve("page-" + pageIndex), pageIndex, rendered));
                }
            }
            // The PDF itself must never be committed alongside its tiles.
            Files.delete(source);
            return new RenderedDocument(stagingDir, properties.getTileSize(), pages);
        } catch (IOException | RuntimeException e) {
            discardStaging(stagingDir, e);
            throw e;
        }
    }

    private static void discardStaging(Path stagingDir, Exception cause) {
        try {
            FileOperations.deleteDirectory(stagingDir);
        } catch (IOException cleanupFailure) {
            // Never mask the real error (e.g. "not a readable PDF"); StorageJanitor removes it later.
            cause.addSuppressed(cleanupFailure);
        }
    }

    /**
     * Moves a render into place as version {@code version} of the document.
     * The target directory is always new, so a failed or partial move can
     * never mix tiles with an existing render; the document row is switched
     * to the new version by the caller, in the same transaction.
     */
    public void commit(RenderedDocument rendered, String documentId, int version) throws IOException {
        Path target = versionRoot(documentId, version);
        if (Files.exists(target)) {
            throw new IOException("Tile version already exists: " + target);
        }
        Files.createDirectories(target.getParent());
        FileOperations.moveDirectory(rendered.stagingDir(), target);
    }

    /** Removes one superseded render. */
    public void deleteVersion(String documentId, int version) throws IOException {
        if (version > 0) {
            FileOperations.deleteDirectory(versionRoot(documentId, version));
            return;
        }
        Path root = documentRoot(documentId);
        if (Files.isDirectory(root)) {
            try (Stream<Path> children = Files.list(root)) {
                for (Path page : children.filter(p -> p.getFileName().toString().startsWith("page-")).toList()) {
                    FileOperations.deleteDirectory(page);
                }
            }
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
    public BufferedImage loadRawTile(String documentId, int version, int page, int row, int col) throws IOException {
        Path tilePath = versionRoot(documentId, version)
                .resolve("page-" + page)
                .resolve("tile-" + row + "_" + col + ".png");

        if (!Files.exists(tilePath)) {
            // The URL was issued for a render that has since been replaced.
            throw new TileGoneException();
        }

        return ImageIO.read(tilePath.toFile());
    }

    /** Cheap first check: real PDFs start with "%PDF-" (a few writers prepend junk, so allow 1 KB). */
    private static void requirePdfSignature(Path file) throws IOException {
        byte[] head = new byte[1024];
        int read;
        try (InputStream in = Files.newInputStream(file)) {
            read = in.readNBytes(head, 0, head.length);
        }
        if (!new String(head, 0, read, StandardCharsets.ISO_8859_1).contains("%PDF-")) {
            throw new BadRequestException("The file is not a readable PDF.");
        }
    }

    private void requireWithinLimits(PDDocument document) {
        int pageCount = document.getNumberOfPages();
        if (pageCount == 0) {
            throw new BadRequestException("The PDF has no pages.");
        }
        if (pageCount > properties.getMaxPages()) {
            throw new BadRequestException("The PDF has " + pageCount + " pages; the limit is "
                    + properties.getMaxPages() + ".");
        }
        double scale = properties.getRenderDpi() / 72.0;
        for (int i = 0; i < pageCount; i++) {
            PDPage page = document.getPage(i);
            PDRectangle box = page.getCropBox();
            boolean rotated = page.getRotation() % 180 != 0;
            double width = (rotated ? box.getHeight() : box.getWidth()) * scale;
            double height = (rotated ? box.getWidth() : box.getHeight()) * scale;
            if (width * height > properties.getMaxPagePixels()) {
                throw new BadRequestException("Page " + (i + 1) + " is too large to render ("
                        + Math.round(width) + "x" + Math.round(height) + " px).");
            }
        }
    }

    private static PDDocument loadPdf(Path file) {
        try {
            // Temp-file stream cache: large PDFs are buffered on disk, not in the heap.
            return Loader.loadPDF(file.toFile(), "", null, null, IOUtils.createTempFileOnlyStreamCache());
        } catch (IOException e) {
            // Covers non-PDF content, truncated files and password-protected PDFs.
            throw new BadRequestException("The file is not a readable PDF.");
        }
    }

    private Path documentRoot(String documentId) {
        return Path.of(properties.getStorageRoot(), documentId);
    }

    Path versionRoot(String documentId, int version) {
        return version == 0 ? documentRoot(documentId) : documentRoot(documentId).resolve("v" + version);
    }
}
