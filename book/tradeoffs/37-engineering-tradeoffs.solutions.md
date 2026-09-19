# Solutions: Chapter 37

### Exercise 37.1 ★ Where is the choice?

Examples: 37.1 tiles, no PDF: README "Why this design" and `TileGenerationService`. 37.2 watermark timing: README "Watermarking happens on the way out" and `WatermarkService`. 37.4 URL signing: `SignedUrlService`. 37.5 sessions: README Limitations. 37.7 tile storage: `storage-root` in the README configuration table and `StorageJanitor`. 37.11 one instance: README "Go-live checklist".

### Exercise 37.2 ★★ Why S3 forces a rethink

If tiles move to S3 behind a CDN with signed URLs, the CDN serves tile bytes and the app no longer sees each request. Consequences:

- **Watermarking (37.2):** stamping happens in the app at serve time. At the edge, either you stamp somewhere else (an image service or edge function) or you serve unstamped tiles, which loses attribution.
- **Session checks (37.4):** the app checks the session on every tile request. A CDN signed URL is checked by the CDN, which knows nothing about your session, so logout and unsharing no longer cut off outstanding URLs until they expire. You would shorten lifetimes or add an edge authorizer.
- **Rate limiting (37.8):** per-user counters in the app no longer see tile traffic.

### Exercise 37.3 ★★ Order of change

One defensible order: (1) shared sessions and counters in Redis (37.5), (2) tile storage on S3 (37.7), (3) managed database (37.9), (4) more instances behind a load balancer (37.11), (5) CDN signed URLs (37.4). Reasoning: a second instance is unsafe until sessions, counters, and tiles are all shared, so those come first; the CDN change comes last because it changes watermarking and session checks. Any order that puts 37.11 after 37.5 and 37.7 and justifies it is acceptable.

### Exercise 37.4 ★★★ A switch trigger as an alert

Example for 37.5 to 37.11 (needing a second instance): alert when average CPU of the app container stays above 80% for 15 minutes while `sdv_tiles_rate_limited_total` is flat (load is real reader traffic, not throttled harvesting). For 37.8: alert when the rate of `sdv_tiles_rate_limited_total` per hour exceeds an agreed share of `sdv_tiles_served_total`, meaning readers are hurt by the limit. The answer must name a metric from Chapter 35 and a threshold with a time window.
