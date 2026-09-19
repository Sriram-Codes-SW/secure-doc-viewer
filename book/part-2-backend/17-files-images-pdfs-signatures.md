<!-- chapter: 17 | part: II | owner: writer-backend | tag: book-m6-final | status: draft -->
# Chapter 17: Files, images, PDFs and signatures

The heart of the Secure Document Viewer is that the browser never receives your PDF. The server turns each page into a picture, cuts it into small square tiles, stamps the viewer's identity on each tile, and serves them through links that cannot be forged. This chapter teaches the pieces: images, slicing, signatures, bounded work and safe file handling.

## Learning objectives

By the end of this chapter, you will be able to:

- Explain what pixels and a PNG image are.
- Explain how a page image is sliced into a grid of tiles, and why edge tiles are cropped.
- Explain what a hash, an HMAC and a signed token are.
- Read `SignedUrlService` and say what each check protects against.
- Explain why the server limits how much work runs at once.
- Describe how uploaded files are staged, committed and cleaned up.

## Prerequisites

- Chapter 5: collections and exceptions
- Chapter 11: Spring Boot foundations
- Chapter 12: REST controllers and JSON

## Beginner tier: Pictures and grids

### 17.1 Pixels, images and PNG

A digital image is a grid of **pixels**, tiny colored squares. A page rendered at 150 dots per inch (the project's `render-dpi: 150` in `application.yml`) is about 1,240 by 1,754 pixels for A4 paper, which is the arithmetic of width in inches times DPI. **PNG** is a file format that stores such a grid compressed without losing detail, which suits text-heavy pages. In Java, `BufferedImage` is the in-memory grid you read pixels from and draw on.

Why pictures at all? A PDF in the browser can be saved, copied and searched. A stack of image tiles can't be saved as a document with one click; it must be reassembled. The project is honest that this raises the effort of copying rather than making it impossible.

### 17.2 Reading and rendering a PDF

Turning a PDF page into a `BufferedImage` is called **rasterizing**. The project uses the Apache PDFBox library (version 3.0.8, from `pom.xml`), which can open a PDF and draw a page at a chosen DPI. `TileGenerationService` does the work. Listing 17.1 shows the heart of it, and the checks that guard it.

**Listing 17.1 — `TileGenerationService.java` (`book-m6-final`, simplified: the surrounding methods, error handling and the tile-saving helper are omitted; the two excerpts are from different places in the class)**

*`src/main/java/com/example/securedocviewer/service/TileGenerationService.java`*

```java
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

// ... elsewhere in the class ...

private void requireWithinLimits(PDDocument document) {
    int pageCount = document.getNumberOfPages();
    if (pageCount == 0) {
        throw new BadRequestException("The PDF has no pages.");
    }
    if (pageCount > properties.getMaxPages()) {
        throw new BadRequestException("The PDF has " + pageCount + " pages; the limit is "
                + properties.getMaxPages() + ".");
    }
    // ... a per-page pixel-size check follows ...
}
```

The method opens the PDF, checks it against the limits before drawing anything, and creates a `PDFRenderer`. Then it loops over the pages: `renderImageWithDPI(pageIndex, properties.getRenderDpi())` draws one page as a `BufferedImage` at 150 DPI, and `tileAndSave` (not shown) slices it with `TileGrid` and writes the tiles into a per-page directory in a staging area. The `try (...)` closes the PDF when the loop ends, even on error. Before each page the loop checks a `cancelled` flag, so a render that exceeds `render-timeout` can be abandoned and free its slot (Section 17.6). Two further limits appear in the source. `requireWithinLimits` rejects a PDF with no pages or more than `max-pages`, and, in the part omitted here, a page whose rendered size would exceed `max-page-pixels`; each failure is a `BadRequestException`, which Chapter 13's handler turns into a `400`. And the PDF is loaded with a temporary-file cache (a comment in `loadPdf` says: "large PDFs are buffered on disk, not in the heap").

### 17.3 Slicing an image into a grid

Serving a whole page as one image would hand over everything in one request. Tiles are small: `tile-size: 512` pixels square at the final tag. `TileGrid` is the pure arithmetic, kept separate so it can be tested without any PDF.

**Listing 17.2 — `TileGrid.java` (`book-m0-mvp`, identical at `book-m6-final`; imports and the class comment are omitted)**

*`src/main/java/com/example/securedocviewer/service/TileGrid.java`*

```java
public final class TileGrid {

    private TileGrid() {
    }

    /** Number of tiles needed to cover {@code lengthPx} pixels: ceil(lengthPx / tileSize). */
    public static int tileCount(int lengthPx, int tileSize) {
        if (lengthPx <= 0 || tileSize <= 0) {
            throw new IllegalArgumentException("lengthPx and tileSize must both be positive");
        }
        return (lengthPx + tileSize - 1) / tileSize;
    }

    /** Extracts a single tile, cropped to the image bounds at the right/bottom edges. */
    public static BufferedImage sliceTile(BufferedImage page, int row, int col, int tileSize) {
        int x = col * tileSize;
        int y = row * tileSize;
        if (x >= page.getWidth() || y >= page.getHeight()) {
            throw new IllegalArgumentException("Tile (" + row + "," + col + ") is outside the image bounds");
        }
        int w = Math.min(tileSize, page.getWidth() - x);
        int h = Math.min(tileSize, page.getHeight() - y);
        return page.getSubimage(x, y, w, h);
    }
}
```

`tileCount` rounds up: 1,240 pixels with 512-pixel tiles needs `(1240 + 511) / 512 = 3` tiles across, because integer division in Java drops the remainder (Chapter 3), so adding `tileSize - 1` first is the standard round-up trick. `sliceTile` computes the top-left corner from row and column, then takes the smaller of a full tile and what remains, so the last tiles are cropped rather than padded. The class comment states the payoff: reassembling every tile at `(col * tileSize, row * tileSize)` "reproduces the source image exactly, with no seams and no bleed". The private constructor stops anyone creating an object of a class that only holds static methods.

### 17.4 Drawing text on an image (watermarks)

A **watermark** is text drawn into the picture. Here it shows the viewer, a UTC timestamp and a short trace code that matches the session column of the audit log, so a leaked screenshot points back to a sign-in (comment in `application.yml`). Its look is configurable: `watermark-opacity: 0.2` and `watermark-spacing: 1.5`, the gap between copies as a multiple of the text height. `WatermarkService` does the drawing, and `TileController` applies it to each tile at request time (Chapter 12). The class comment gives the reason for stamping on the way out rather than during upload: "one stored tile serves every viewer, and every response is still individually traceable back to who requested it and when."

**Listing 17.3 — `WatermarkService.applyWatermark` (`book-m6-final`, simplified: the long explanatory comments, the tile-sizing lines and the closing lines are omitted)**

*`src/main/java/com/example/securedocviewer/service/WatermarkService.java`*

```java
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
        // ... choose the font size and compute the spacing (Layout) ...
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        g.setColor(Color.RED);
        g.rotate(-Math.PI / 6);
        // ... a loop draws each line at every step of a brick pattern ...
    } finally {
        g.dispose();
    }

    return stamped;
}
```

Reading it through: the method creates a new image the same size as the tile and gets a `Graphics2D`, Java's drawing object. It first draws the original tile onto it, then builds two lines of text, the viewer's name and the UTC time (plus the trace code when one is given). It sets a translucent red ink (`opacity`, 0.2 by default in `application.yml`) and rotates the drawing surface by 30 degrees (`-Math.PI / 6` radians). A loop, left out here, repeats the two lines across the whole tile in a brick pattern. The `finally` calls `g.dispose()` to release the drawing resources even if something fails.

The repetition is deliberate. Edge tiles are cropped shorter than full ones (Section 17.3), so a single centered mark could land entirely outside a small tile. The source comment explains: repeating the mark "so every tile carries some of it — and a full-size tile carries at least one complete, readable copy". The settings are also clamped inside the service (opacity between 0.05 and 0.6, spacing between 0.5 and 6.0), so a mistaken configuration can't make the mark invisible or overwhelming.

## Intermediate tier: Proving a link wasn't altered

### 17.5 Hashes, HMAC and signatures: proving a URL wasn't altered

The browser asks for tiles by URL, and anyone could type a different tile or document id into it. The server needs a way to hand out links only it can create. The tool is an **HMAC** (hash-based message authentication code): a fixed-length fingerprint computed from a message and a secret key with a hash function (HmacSHA256 here). Without the key, nobody can produce the right fingerprint for a message, and change one character of the message and the fingerprint changes completely.

The project's **signed token** is the message plus its fingerprint. `SignedUrlService` issues and verifies it (Listing 17.4).

**Listing 17.4 — `SignedUrlService.java` (`book-m6-final`, simplified: `parseCanonical`, the helper methods and comments are omitted)**

*`src/main/java/com/example/securedocviewer/service/SignedUrlService.java`*

```java
public String issueToken(String documentId, int page, int row, int col, int tileVersion, String sessionBinding) {
    long expiresAt = Instant.now().getEpochSecond() + properties.getUrlTtlSeconds();
    SignedTilePayload payload = new SignedTilePayload(documentId, page, row, col, tileVersion, sessionBinding, expiresAt);
    String payloadEncoded = base64Url(payload.canonicalString().getBytes(StandardCharsets.UTF_8));
    String signature = base64Url(hmac(payload.canonicalString()));
    return payloadEncoded + "." + signature;
}

public SignedTilePayload verifyAndDecode(String token) {
    String[] parts = token.split("\\.", 2);
    if (parts.length != 2) {
        throw new InvalidTokenException("Malformed token");
    }
    // ... decode the payload; a bad encoding throws InvalidTokenException ...
    String expectedSignature = base64Url(hmac(canonical));
    if (!constantTimeEquals(expectedSignature, providedSignature)) {
        throw new InvalidTokenException("Signature mismatch — token was tampered with or forged");
    }

    SignedTilePayload payload = parseCanonical(canonical);
    if (Instant.now().getEpochSecond() > payload.expiresAtEpochSeconds()) {
        throw new InvalidTokenException("Token expired at " + Instant.ofEpochSecond(payload.expiresAtEpochSeconds()));
    }

    return payload;
}
```

Read it as a recipe. `issueToken` fills in one tile's details (document, page, row, column, render version, the session it's bound to) plus an expiry time (`url-ttl-seconds: 120`, two minutes), writes them as one canonical string, encodes it with base64url (a way to write bytes using only URL-safe characters), and appends the HMAC. `verifyAndDecode` recomputes the HMAC from the received payload and compares. A mismatch means the token was altered or forged, so it is refused with `InvalidTokenException`, which `GlobalExceptionHandler` maps to `401`. Only then is the payload parsed and the expiry checked.

Two details matter. `constantTimeEquals` compares with `MessageDigest.isEqual`, which takes the same time however many characters match, so an attacker can't learn the signature one character at a time by timing responses ("Avoids leaking timing information", per the source). And the signature covers every field, which the class comment states directly: holders "cannot forge a new one, extend it, or repurpose it for a different tile". Anyone with a token can use it until it expires, so `TileController` adds independent checks: the session it's bound to, the rate limit and current access (Chapter 12 and Chapter 16). The secret key never leaves the server; `ViewerProperties` refuses to start without one at least 32 characters long (Chapter 13).

## Advanced tier: Limits and safe files

### 17.6 Threads, pools and limits: bounded work

Watermarking and encoding a tile uses CPU. The per-user rate limit (Chapter 16's cousin, `TileRateLimiter`) bounds each reader, but many readers together could still starve the server. `TileWorkLimiter` caps work across everyone with a **semaphore**, a counter of permits: a task must take a permit to run and returns it when done.

**Listing 17.5 — `TileWorkLimiter.run` (`book-m6-final`, class comment and imports omitted)**

*`src/main/java/com/example/securedocviewer/service/TileWorkLimiter.java`*

```java
public <T> T run(Callable<T> work) throws Exception {
    if (!slots.tryAcquire(WAIT_MILLIS, TimeUnit.MILLISECONDS)) {
        metrics.tileBusy();
        throw new ServiceBusyException("The server is busy. Retrying shortly.", 1);
    }
    try {
        return work.call();
    } finally {
        slots.release();
    }
}
```

If no permit frees up within `WAIT_MILLIS` (2,000 ms), the request is refused with `ServiceBusyException`, which becomes `503` with `Retry-After`, and the viewer retries. The `finally` block returns the permit even if the work throws; forgetting it would slowly leak permits until the server refused everything. The number of permits comes from `max-concurrent-tile-renders`, where `0` means twice the CPU count. The same idea limits PDF rendering (`max-concurrent-renders: 2`, with a `render-timeout` of 3 minutes) so a hostile file can't hold a slot forever, as `application.yml` explains. The design: slow down and fail politely under load, rather than fall over.

### 17.7 Files on disk safely: staging, atomic move, cleanup

Uploads write files and a database row, and these can't be one transaction. `DocumentService`'s comment describes the order: "render to staging, then commit tiles and metadata". In `upload`, the tiles are rendered into a staging area; `tiles.commit` moves them into the document's directory; then the database row is saved; and if any step fails, `tiles.discard` or `tiles.deleteTiles` removes what was written. Replacing a PDF writes into a fresh version directory (`v2`, `v3`) and only then switches the row's `tile_version`, so readers never see a mix of old and new tiles; the old version is deleted after the switch. The tile token carries the version so a stale link gets `410 Gone` instead of the wrong tile.

Cleanup can itself fail, so a scheduled `StorageJanitor` (Chapter 14) removes directories nothing points to. It is deliberately conservative: it touches only directories whose names look like document ids, and only ones older than an hour. The MySQL test `concurrentReplacementsAreSerialisedByTheRowLock` checks the end state on disk: only the newest version directory remains (Chapter 18).

## In this project

**Table 17.1 — Where Chapter 17's ideas live**

| Idea | File | Tag |
|---|---|---|
| Grid arithmetic | `service/TileGrid.java` | `book-m0-mvp`, `book-m6-final` |
| Rendering and tiling | `service/TileGenerationService.java` | `book-m6-final` |
| Watermark | `service/WatermarkService.java` | `book-m6-final` |
| Signed tokens | `service/SignedUrlService.java` | `book-m6-final` |
| Bounded work | `service/TileWorkLimiter.java` | `book-m6-final` |
| Disk cleanup | `service/StorageJanitor.java` | `book-m6-final` |

## Try it

### Exercise 17.1 ★ Count the tiles

How many 512-pixel tiles cover a 1,000-pixel-wide page? Check with `TileGrid.tileCount`.

### Exercise 17.2 ★ Cropping edge tiles

Why does `sliceTile` crop edge tiles instead of padding them?

### Exercise 17.3 ★★ Tamper with a token

Alter one character of a token's payload and send it (to your own local copy of the app). Which exception and status result?

### Exercise 17.4 ★★ Release in a finally block

Why does `TileWorkLimiter` release its permit in a `finally` block?

### Exercise 17.5 ★★★ Two checks on a copied link

A token is valid for 120 seconds and is copied to a friend's browser within that time. Name two independent checks in the app that still refuse it.

## Summary

- An image is a grid of pixels; PNG stores it without loss.
- `TileGrid` rounds tile counts up and crops edge tiles so tiles reassemble exactly.
- Watermarks are drawn per tile at request time, so each viewer gets different pixels.
- An HMAC over every field, with a secret key, makes tokens unforgeable; constant-time comparison and expiry finish the job.
- A semaphore bounds concurrent work; excess requests get `503` with `Retry-After`.
- Files are staged, committed by version and cleaned by a conservative janitor.

## Further reading

- *Apache PDFBox Documentation*. https://pdfbox.apache.org/
- *Java Platform SE API*, `javax.crypto.Mac`. https://docs.oracle.com/en/java/javase/25/docs/api/java.base/javax/crypto/Mac.html
- *RFC 2104*, "HMAC: Keyed-Hashing for Message Authentication." https://www.rfc-editor.org/rfc/rfc2104
- *RFC 4648*, "The Base16, Base32, and Base64 Data Encodings" (base64url). https://www.rfc-editor.org/rfc/rfc4648
