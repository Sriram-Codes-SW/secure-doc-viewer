<!-- chapter: 25 | part: IV | owner: writer-app | tag: book-m0-mvp | status: expanded -->
# Solutions for Chapter 25

### Exercise 25.1 ★ Count the tiles

`tileCount(1240, 256)` is (1240 + 255) / 256 = 5 columns using integer division. Four full columns cover 1,024 pixels, so the last column is 1240 - 1024 = 216 pixels wide. `tileCount(1754, 256)` is (1754 + 255) / 256 = 7 rows; six full rows cover 1,536 pixels, so the last row is 218 pixels tall.

### Exercise 25.2 ★ Letter page

At 150 DPI the page is about 1,275 by 1,650 pixels. Columns: (1275 + 127) / 128 = 10 (nine full columns cover 1,152 pixels; the last is 123 pixels wide). Rows: (1650 + 127) / 128 = 13 (twelve full rows cover 1,536; the last is 114 tall). 10 × 13 = 130 tiles, about four times as many as with 256-pixel tiles, which is why tile size affects request counts and rate limits.

### Exercise 25.3 ★★ Tamper with a token

Change a character in the middle of the payload, not the last one, because the last base64url character can carry unused bits and may decode to the same bytes. The client then receives HTTP 401 with a JSON body `{"error": "..."}`. At `book-m0-mvp`, `GlobalExceptionHandler` maps `InvalidTokenException` to 401. A token whose payload was altered fails the signature comparison ("Signature mismatch"). The signature is checked first so that data nobody signed never reaches the parsing code, and the comparison is constant-time so response timing reveals nothing about how much matched.

### Exercise 25.4 ★★ Build a canonical string

The canonical string is `d9|1|0|4|s7|5000` (document, page, row, column, session, expiry, separated by `|`). If the column is changed to 5 and the old signature kept, the payload part of the token now decodes to `d9|1|0|5|s7|5000`. `verifyAndDecode` recomputes the HMAC of that canonical string, gets a value different from the supplied signature, and `constantTimeEquals` fails, so it throws `InvalidTokenException("Signature mismatch — token was tampered with or forged")`. The expiry check is never reached.

### Exercise 25.5 ★★★ Two independent checks

One good answer: the token proves that the server issued it for one tile until a fixed time, but not that the person is still allowed to read. If the user signs out or the session times out, tokens issued under that session are still unexpired, so `TileController` also calls `requireValidSession`. The two failures (bad token, dead session) stay distinguishable. Later milestones add a third question, whether the user may still see the document at all (Chapter 27).

### Exercise 25.6 ★★★ Design a limit

One good answer. Count after the token and session checks (so unauthenticated or forged requests can't consume a real reader's allowance) and before the disk read and watermark (so a refused request doesn't pay their cost). Use a sliding window of recent request times per key. When the count is over the limit, refuse with HTTP 429 (Too Many Requests) and a `Retry-After` header saying when to try again. Keying by session id is weak because a user can sign in again and get a fresh session with a fresh allowance; the project found this (finding `TM-3`) and moved to a per-user key in milestone 1 (Chapter 26).
