# Chapter 1 solutions

### Exercise 1.1 ★ Count the tiles

Columns: 2,000 / 512 = 3.9, rounded up to 4. Rows: 3,000 / 512 = 5.86, rounded up to 6. Total: 4 x 6 = 24 tiles. The tiles in the last column (2,000 - 3 x 512 = 464 px wide) and the last row (3,000 - 5 x 512 = 440 px high) are cropped shorter; the rest are 512 x 512.

### Exercise 1.2 ★ Client or server?

Hiding the download button: the client can do it, but it protects nothing. Checking token expiry: only the server, because the client could otherwise change the clock or the token. Blocking an unauthorized user: only the server.

### Exercise 1.3 ★★ Read the limits

One good answer: "A valid session can request every tile" is accepted because readers need normal reading speed (about 15 pages a minute, derived as 180 tiles per minute divided by about 12 tiles per page); the watermark makes the result traceable instead. "No text layer" is accepted because a text layer would be the copyable text the design avoids.
