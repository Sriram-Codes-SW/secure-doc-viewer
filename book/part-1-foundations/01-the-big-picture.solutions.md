# Chapter 1 solutions

### Exercise 1.1 ★ Count the tiles

Columns: 2,000 / 512 = 3.9, rounded up to 4. Rows: 3,000 / 512 = 5.86, rounded up to 6. Total: 4 x 6 = 24 tiles. The tiles in the last column (2,000 - 3 x 512 = 464 px wide) and the last row (3,000 - 5 x 512 = 440 px high) are cropped shorter; the rest are 512 x 512.

### Exercise 1.2 ★ Client or server?

Hiding the download button: the client can do it, but it protects nothing. Checking token expiry: only the server, because the client could otherwise change the clock or the token. Blocking an unauthorized user: only the server.

### Exercise 1.3 ★★ Read the limits

One good answer: "A valid session can request every tile" is accepted because readers need normal reading speed (about 15 pages a minute, derived as 180 tiles per minute divided by about 12 tiles per page); the watermark makes the result traceable instead. "No text layer" is accepted because a text layer would be the copyable text the design avoids.

### Exercise 1.4 ★ Authentication or authorization?

Checking your password: authentication (it establishes who you are). Refusing a document that was never shared with you: authorization (it decides what you may do). Ending your session after 30 idle minutes: neither is a perfect fit; it is session management, which protects authentication by making a forgotten sign-in expire. Accept an answer of "authentication" with the reasoning that the session stops proving who you are.

### Exercise 1.5 ★★ Trace a page view

After sign-in and opening the document, the browser makes: one request for the page's tile addresses (`/api/documents/{documentId}/pages/2/tile-urls`), then one request per tile, `/api/tiles?token=<signed-token>`, 12 of them for a 3 by 4 grid. That is 13 requests. With a smaller tile size the grid has more tiles, so more requests: for example 256-pixel tiles on a 1,275 by 1,650 page make 5 columns by 7 rows, 35 tiles, and 36 requests.

### Exercise 1.6 ★★★ Argue for a different trade-off

One good answer. For the skimming customer: 1,024-pixel tiles give 2 by 2, four tiles per page, so at 180 requests a minute a reader can move through about 45 pages a minute, and a script needs about 2,000 tiles / 180 per minute, roughly 11 minutes, for 500 pages. The cost: a harvest becomes much faster, so the watermark is doing more of the work. For the sensitive customer: 256-pixel tiles give 35 tiles per page, and with 120 requests a minute reading drops to about 3 pages a minute while a harvest of 500 pages takes about 2.4 hours (17,500 tiles / 120 per minute). The cost: readers hit the limit and see blank pages while tiles load, which is exactly what an early review found. Note that a document keeps the tile size it was rendered with, so changing the setting affects only documents uploaded afterward.
