<!-- chapter: 25 | part: IV | owner: writer-app | tag: book-m0-mvp | status: draft -->
# Solutions for Chapter 25

### Exercise 25.1 ★ Count the tiles

`tileCount(1240, 256)` is (1240 + 255) / 256 = 5 columns using integer division. Four full columns cover
1,024 pixels, so the last column is 1240 - 1024 = 216 pixels wide. `tileCount(1754, 256)` is
(1754 + 255) / 256 = 7 rows; six full rows cover 1,536 pixels, so the last row is 218 pixels tall.

### Exercise 25.2 ★★ Tamper with a token

The client receives HTTP 401 with a JSON body `{"error": "..."}`. At `book-m0-mvp`,
`GlobalExceptionHandler` maps `InvalidTokenException` to 401. A token whose payload was altered fails
the signature comparison ("Signature mismatch"). The signature is checked first so that data nobody
signed never reaches the parsing code, and the comparison is constant-time so response timing
reveals nothing about how much matched.

### Exercise 25.3 ★★★ Two independent checks

One good answer: the token proves that the server issued it for one tile until a fixed time, but not that the
person is still allowed to read. If the user signs out or the session times out, tokens issued under
that session are still unexpired, so `TileController` also calls `requireValidSession`. The two
failures (bad token, dead session) stay distinguishable. Later milestones add a third question, whether the
user may still see the document at all (Chapter 27).
