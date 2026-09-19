<!-- chapter: 17 | part: II | owner: writer-backend | tag: book-m6-final | status: draft -->
# Solutions: Chapter 17

1. (★) `tileCount(1000, 512)` is `(1000 + 511) / 512 = 1511 / 512 = 2` with integer division.
2. (★) So that reassembling every tile at `(col * tileSize, row * tileSize)` reproduces the page exactly, with no padding and no seams.
3. (★★) `SignedUrlService.verifyAndDecode` recomputes the HMAC, finds a mismatch and throws `InvalidTokenException("Signature mismatch ...")`, which `GlobalExceptionHandler` maps to `401`.
4. (★★) So the permit is returned even if the work throws; otherwise permits would leak and the server would eventually refuse every request.
5. (★★★) Any two of: the token is bound to the session it was issued to, so a different browser's session doesn't match (`InvalidTokenException`); the friend has no signed-in session at all (`401`); the per-user rate limit; the current access check on the document.
