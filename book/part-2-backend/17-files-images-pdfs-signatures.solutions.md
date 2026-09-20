<!-- chapter: 17 | part: II | owner: writer-backend | tag: book-m6-final | status: expanded -->
# Solutions: Chapter 17

### Exercise 17.1 ★ Count the tiles

Width: 8.5 × 150 = 1,275 pixels. Height: 11 × 150 = 1,650 pixels. Columns: `tileCount(1275, 512)` is `(1275 + 511) / 512 = 1786 / 512 = 3` (integer division drops the remainder), so 3 columns, and the last column is 1,275 − 1,024 = 251 pixels wide. Rows: `tileCount(1650, 512)` is `(1650 + 511) / 512 = 2161 / 512 = 4`, so 4 rows, and the last row is 1,650 − 1,536 = 114 pixels tall. That is 12 tiles, matching the "~12 tiles" in the comment in `application.yml`.

### Exercise 17.2 ★ Why crop, not pad?

Because reassembling the tiles at `(col * tileSize, row * tileSize)` must reproduce the page exactly, and the page itself has no pixels beyond its right and bottom edges. A padded tile would carry a strip of white that doesn't belong to the page: the viewer would show a light band beyond the page's edge, most visible against a dark background; each tile would also claim a size that doesn't match `PageInfo`'s page width and height; and the padding would be extra pixels to store, send, and watermark. Cropping keeps the data exactly as large as the page.

### Exercise 17.3 ★★ Tamper with a token

Changing a character of the signature (after the dot) makes the recomputed HMAC differ from the supplied one, so `verifyAndDecode` throws `InvalidTokenException("Signature mismatch ...")`, which `GlobalExceptionHandler` turns into `401` with that message in the JSON. Changing a character before the dot changes the payload, so the HMAC recomputed from the altered payload no longer matches the original signature, and the result is the same `401`. (If your change makes the payload invalid base64url, you get `Malformed token payload` instead, also `401`.) The signature covers the whole payload, so any edit to either half is detected; the only way to get a valid token is to ask the server to issue one.

### Exercise 17.4 ★★ Why `finally`?

`finally` runs whether the work returns normally or throws. If the permit were released only after a normal return, every exception thrown inside the work (a corrupt image, a failure while writing the PNG) would keep its permit forever. The permit count is fixed (twice the CPU count, by default), so after that many failures the semaphore would be empty and every tile request would wait 2 seconds and then get `503`. The server would look busy while doing nothing, and only a restart would fix it.

### Exercise 17.5 ★★★ Walk the leaked link

A worked outline. The independent checks that still refuse the friend's request:

- **Expiry:** after 120 seconds the token is refused (`Token expired ...`, `401`), whatever else is true.
- **A live signed-in session:** if the friend isn't signed in, Spring Security answers `401` before the controller runs.
- **Session binding:** the token contains a value derived from the *original* session's id. Even if the friend signs in as the *same user*, their session has a different id, so `tileBindingMatches` fails and `TileController` throws `InvalidTokenException("This tile link was issued to a different session.")`. This is the single check that refuses the link even when the friend is the same person on another device.
- **Fresh access check:** if the document has been unshared or deleted since, the access check answers `404`.

The rate limit doesn't refuse it on its own (one request is under the limit), and the watermark doesn't refuse anything; it makes a leaked tile traceable to the session that requested it.

### Exercise 17.6 ★★★ Design the failure paths

One good answer.

1. Validate the uploaded file and read it into a temporary file (no shared state changed).
2. Produce the thumbnail into a **staging** folder or temporary file (invisible to readers). If this fails, delete the staging file; nothing else has changed.
3. **Atomically rename** the staged file to its final name in the storage folder, with a unique name (for example including a version or random id) so it can't overwrite the current thumbnail. If the rename fails, delete the staging file.
4. Update the database column to point at the new filename in one short transaction. If it fails, delete the newly renamed file (nothing points at it).
5. After the transaction commits, delete the *old* thumbnail file. If that fails, log it and leave it for a janitor: the row no longer points at it, so it is unreachable garbage, not an error.

The key decisions: write in a place nobody looks and make it visible in one atomic step; change the database only after the file exists; delete the old file only after the database points at the new one; and give every failure path a cleanup, with a scheduled sweep as the last resort, as `StorageJanitor` does for tiles.
