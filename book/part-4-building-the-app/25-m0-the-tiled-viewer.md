<!-- chapter: 25 | part: IV | owner: writer-app | tag: book-m0-mvp | status: outline -->
# Chapter 25: Milestone 0: The tiled viewer

> Draft in progress. Sections 25.1 to 25.3 are written. Sections 25.4 to 25.6, the tier
> wrapper, Blueprint v0 and Decisions and challenges are still to do
> (see `_team/progress/writer-app.md`).

## Learning objectives

- Explain why the first version of the viewer never sends the PDF file to the browser.
- Compute how many tiles cover a page and why the last row and column are shorter.
- Describe how a signed URL proves a tile request is genuine without a database lookup.
- Read `TileGrid`, `SignedUrlService` and `TileController` at `book-m0-mvp` and say what each guards.

## Prerequisites

Chapters 3–6 (Java), 8 (Spring Boot), 11–12 (HTTP and REST) and 17 (testing basics), as
listed in `book/OUTLINE.md`. At this milestone the project uses Spring Boot 3.3.4, Java 21 and
PDFBox 3.0.3 (`pom.xml` at `book-m0-mvp`); the upgrade to Spring Boot 4 comes in Chapter 30.

### 25.1 Requirements and the threat we start with

The project starts from one product goal: let a person read a PDF in a browser without being
able to walk away with the file. The first commit describes the approach: serve documents
without exposing the source file, turn each page into an image, cut that image into tiles at
ingest time, hand out the tiles one at a time through short-lived signed URLs tied to a
session, stamp each tile with the identity of the person requesting it, and reassemble the
tiles on a canvas in the browser.
<!-- source: commit b6aef4e message; dossier/timeline.md#milestone-0 -->

Read that as a list of small defenses, each closing one door:

- **No source file.** There is no URL that returns the PDF, so there is nothing to save.
- **Tiles, not pages.** A page arrives as many small images, so one request never yields a whole page.
- **Signed, short-lived URLs.** A leaked link stops working soon and can't be edited to point elsewhere.
- **Watermark at request time.** Every tile carries the viewer's identity, so a screenshot is traceable.

This is a deterrent, not a vault. Anyone who can see pixels can photograph the screen. The
project's README is explicit about such limits, and the book keeps that honesty: the goal is
to raise the cost of casual copying and to make leaks attributable.

At this tag the product is backend only. It has controllers, services, a session service, an
in-memory `DocumentRegistry`, five unit-test classes and one static `index.html`. There is no
database and no Angular yet.
<!-- source: git ls-tree -r book-m0-mvp; dossier/timeline.md#milestone-0 -->

### 25.2 Tile grid math (`TileGrid`)

**Analogy.** Think of a bathroom wall covered with square tiles. You count how many tiles fit
along the width, rounding up, because a partial tile still has to be cut and placed. **Where
the analogy breaks down:** a real tiler fills gaps with grout; the viewer never pads. Edge
tiles are simply smaller, so putting every tile back at its own position rebuilds the page
exactly.

The math lives in its own class so it can be tested without any PDF library. Two ideas:

1. **Tile count.** For `lengthPx` pixels and tiles of `tileSize` pixels you need
   ceil(lengthPx / tileSize) tiles. Integer arithmetic gives that as
   `(lengthPx + tileSize - 1) / tileSize`.
2. **Cropping.** A tile starts at `(col * tileSize, row * tileSize)` and is `tileSize` wide
   unless the image ends first, in which case it is the remaining width.

**Listing 25.1 — `TileGrid.java` (book-m0-mvp, simplified: Javadoc and imports removed)**

```java
public final class TileGrid {

    private TileGrid() {
    }

    public static int tileCount(int lengthPx, int tileSize) {
        if (lengthPx <= 0 || tileSize <= 0) {
            throw new IllegalArgumentException("lengthPx and tileSize must both be positive");
        }
        return (lengthPx + tileSize - 1) / tileSize;
    }

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

Line by line: the private constructor stops anyone from creating a `TileGrid` object, because
the class only holds functions. `tileCount` rejects zero or negative sizes early, so bad input
fails loudly instead of dividing by zero. `sliceTile` computes the top-left corner, refuses a
tile outside the image, then uses `Math.min` to shorten the last row and column.

The first commit message says a round-trip property test proves the tiles partition a page
without loss. That is why the class has no dependencies: `TileGridTest` can use a synthetic
image instead of a rendered PDF.
<!-- source: commit b6aef4e message; TileGrid.java and TileGridTest.java at book-m0-mvp -->

**Try it (preview).** Compute `tileCount(1000, 256)`. The answer is 4, and the last tile is
232 pixels wide (1000 - 3 * 256).

### 25.3 Signed URLs (`SignedUrlService`)

**Analogy.** A signed URL is like a concert wristband stamped with a seal only the venue owns.
Staff don't consult a guest list at every door; they check the seal. **Where the analogy
breaks down:** a wristband works all night, while a token names one tile and expires at a
fixed time. And a stolen wristband works for whoever wears it, just as a copied URL works
until it expires. That is why Chapter 26 binds tokens to a session more tightly.

The service issues a token that grants access to exactly one tile of one page of one
document, for one session, until a fixed expiry. The token is two base64url strings joined by
a dot: the payload, and an HMAC-SHA256 signature over it. **HMAC** (hash-based message
authentication code) mixes a secret key into a hash, so only a holder of the key can produce a
matching signature. Change any field of the payload and the signature no longer matches.

**Listing 25.2 — `SignedUrlService.java` (book-m0-mvp, simplified: Javadoc, `parseCanonical` and the private helpers removed)**

```java
public String issueToken(String documentId, int page, int row, int col, String sessionId) {
    long expiresAt = Instant.now().getEpochSecond() + properties.getUrlTtlSeconds();
    SignedTilePayload payload = new SignedTilePayload(documentId, page, row, col, sessionId, expiresAt);
    String payloadEncoded = base64Url(payload.canonicalString().getBytes(StandardCharsets.UTF_8));
    String signature = base64Url(hmac(payload.canonicalString()));
    return payloadEncoded + "." + signature;
}

public SignedTilePayload verifyAndDecode(String token) {
    String[] parts = token.split("\\.", 2);
    if (parts.length != 2) {
        throw new InvalidTokenException("Malformed token");
    }

    String payloadEncoded = parts[0];
    String providedSignature = parts[1];

    byte[] payloadBytes;
    try {
        payloadBytes = Base64.getUrlDecoder().decode(payloadEncoded);
    } catch (IllegalArgumentException e) {
        throw new InvalidTokenException("Malformed token payload");
    }
    String canonical = new String(payloadBytes, StandardCharsets.UTF_8);

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

Walk through `verifyAndDecode` in order, because the order is the design:

1. **Shape.** Split at the first dot. Two parts, or reject.
2. **Decode** the payload. Text that isn't base64url is rejected.
3. **Recompute** the signature from the decoded payload with the server's secret and compare
   it with the one supplied. The comparison uses `MessageDigest.isEqual`, a constant-time
   check, so an attacker can't learn how many leading characters matched from response timing.
4. **Only then parse** the fields and check the expiry.

The signature is verified before the payload is parsed, so untrusted data reaches the
parsing code only if the server signed it.

The service deliberately does not check that the session is still alive. Its Javadoc explains
why: a tampered or expired token and a revoked session are different failures, and separate
checks keep them distinguishable. `TileController` performs the second check (Section 25.5).
<!-- source: SignedUrlService.java and TileController.java at book-m0-mvp -->

<!-- TODO 25.4 watermark, 25.5 endpoints (TileController: verifyAndDecode, then sessionService.requireValidSession, loadRawTile, applyWatermark, PNG, CacheControl.noStore), 25.6 session service + index.html, tiers wrapper, blueprint v0, decisions -->
