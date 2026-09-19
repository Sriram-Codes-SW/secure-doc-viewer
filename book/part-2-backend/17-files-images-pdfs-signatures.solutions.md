<!-- chapter: 17 | part: II | owner: writer-backend | tag: book-m6-final | status: draft -->
# Solutions: Chapter 17

### Exercise 17.1 ★ Count the tiles

`tileCount(1000, 512)` is `(1000 + 511) / 512 = 1511 / 512 = 2` with integer division.

### Exercise 17.2 ★ Cropping edge tiles

So that reassembling every tile at `(col * tileSize, row * tileSize)` reproduces the page exactly, with no padding and no seams.

### Exercise 17.3 ★★ Tamper with a token

`SignedUrlService.verifyAndDecode` recomputes the HMAC, finds a mismatch and throws `InvalidTokenException("Signature mismatch ...")`, which `GlobalExceptionHandler` maps to `401`.

### Exercise 17.4 ★★ Release in a finally block

So the permit is returned even if the work throws; otherwise permits would leak and the server would eventually refuse every request.

### Exercise 17.5 ★★★ Two checks on a copied link

Any two of: the token is bound to the session it was issued to, so a different browser's session doesn't match (`InvalidTokenException`); the friend has no signed-in session at all (`401`); the per-user rate limit; the current access check on the document.
