# Solutions: Chapter 37

### Exercise 37.1 ★ Where is the choice?

Examples: Section 37.1 tiles, no PDF: README "Why this design" and `TileGenerationService`. Section 37.2 watermark timing: README "Watermarking happens on the way out" and `WatermarkService`. Section 37.4 URL signing: `SignedUrlService`. Section 37.5 sessions: README Limitations. Section 37.7 tile storage: `storage-root` in the README configuration table and `StorageJanitor`. Section 37.11 one instance: README "Go-live checklist."

### Exercise 37.2 ★★ Why S3 forces a rethink

If tiles move to S3 behind a CDN with signed URLs, the CDN serves tile bytes and the app no longer sees each request. Consequences:

- **Watermarking (Section 37.2):** stamping happens in the app at serve time. At the edge, either you stamp somewhere else (an image service or edge function) or you serve unstamped tiles, which loses attribution.
- **Session checks (Section 37.4):** the app checks the session on every tile request. A CDN signed URL is checked by the CDN, which knows nothing about your session, so sign-out and unsharing no longer cut off outstanding URLs until they expire. You would shorten lifetimes or add an edge authorizer.
- **Rate limiting (Section 37.8):** per-user counters in the app no longer see tile traffic.

### Exercise 37.3 ★★ Order of change

One defensible order: (1) shared sessions and counters in Redis (Section 37.5), (2) tile storage on S3 (Section 37.7), (3) managed database (Section 37.9), (4) more instances behind a load balancer (Section 37.11), (5) CDN signed URLs (Section 37.4). Reasoning: a second instance is unsafe until sessions, counters, and tiles are all shared, so those come first; the CDN change comes last because it changes watermarking and session checks. Any order that puts Section 37.11 after Sections 37.5 and 37.7 and justifies it is acceptable.

### Exercise 37.4 ★★★ A switch trigger as an alert

Example for Sections 37.5 to 37.11 (needing a second instance): alert when average CPU of the app container stays above 80% for 15 minutes while `sdv_tiles_rate_limited_total` is flat (load is real reader traffic, not throttled harvesting). For Section 37.8: alert when the rate of `sdv_tiles_rate_limited_total` per hour exceeds an agreed share of `sdv_tiles_served_total`, meaning readers are hurt by the limit. The answer must name a metric from Chapter 35 and a threshold with a time window.

### Exercise 37.5 ★★ Cookie or token?

With tokens: the client (mobile app) stores the token itself and sends it in a header on each request, so there is no cookie and therefore no automatic sending; the CSRF attack that cookies invite doesn't apply in the same way, though storing the token safely on the device becomes the app's job. Revocation gets harder: a signed token is valid until it expires unless the server keeps a list of revoked tokens, so "end this user's sessions now" (sign-out everywhere, a role change, an admin revoke) needs extra work or short token lifetimes plus refresh tokens. What the project would build or give up: a sign-in endpoint that issues tokens, verification on every request (Spring Security supports it), a decision about where to store and how to rotate refresh tokens, and a rethink of the tile binding, which currently ties every tile URL to the server session (Chapter 32) and would need to bind to the token instead. It would also give up the immediate server-side revocation that the admin's Sessions page relies on. The browser app could keep its cookie; the two mechanisms could coexist on different endpoints.

### Exercise 37.6 ★★★ Critique the plan

Step 3 (tiles to shared storage) hides the most work: it touches URL signing, watermarking, the janitor, backups, and the atomic replace (which depends on a row lock plus a file layout under a version folder, so an object store needs an equivalent of "switch the document to version n" that readers never see half-done). A defensible reordering to reach two instances earlier: run two instances with a *shared network file system* mounted at the storage root instead of object storage, and steps 1, 2, 4, 5, and 6 (shared sessions, counters, one job runner, the same secret, a balancer). That avoids rewriting tile serving at first, at the price of a shared file system's own limits and of postponing the CDN. Another valid answer: run three instances only for the read path, with one designated instance handling uploads and replacements, so the replace logic needn't change. Any answer must name the extra work and keep sessions and counters shared before the balancer goes in front.
