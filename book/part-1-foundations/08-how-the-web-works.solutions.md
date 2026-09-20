# Chapter 8 solutions

### Exercise 8.1 ★ Match the code

A document you may not see: `404`. A 60 MB upload: `413 Content Too Large` (limit 50 MB). Too many tile requests: `429 Too Many Requests` with a `Retry-After` header.

### Exercise 8.2 ★ Read a URL

Scheme `https`, host `docs.example.com`, path `/api/tiles`, query string `token=abc`.

### Exercise 8.3 ★★ Watch the cookie

The cookie `SDV_SESSION` shows `HttpOnly` and `SameSite=Strict`. `Secure` is set only if `SESSION_COOKIE_SECURE=true`, so on plain local HTTP it is off, matching the default in Listing 8.3.

### Exercise 8.4 ★ Write a request by hand

```text
GET /api/documents HTTP/1.1
Host: localhost:8080
Accept: application/json
```

With no session cookie the app answers `401` (with a JSON body containing an `error` field), because the request is not authenticated.

### Exercise 8.5 ★★ Two cookies

You should see `SDV_SESSION` (marked HttpOnly, so JavaScript cannot read it) and a CSRF cookie (`XSRF-TOKEN`, not HttpOnly, so the frontend can read it). The design needs both because they do different jobs: the session cookie proves who you are and must stay hidden so a script cannot steal it; the CSRF token proves that a change request came from the app's own page, which is why the page has to read it and send it back in a header. The CSRF token is useless without the session cookie, so being readable does no harm.

### Exercise 8.6 ★★★ Explain the header

One good answer, for `Cache-Control: no-store` on tiles: it stops browsers and shared caches from keeping a tile. Without it, a cache that many people share could store a tile watermarked for one viewer and serve it to another, which would both leak content and put the wrong name on the watermark. The cost is that tiles are re-fetched instead of reused, so each view takes a little more time and server work. For `Referrer-Policy: no-referrer`: it stops the browser from telling the next site which page the visitor came from; without it, a link followed from a page whose URL contains a signed token could leak the token; the cost is that other sites lose the referral information they might otherwise use for analytics.
