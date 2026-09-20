<!-- chapter: 26 | part: IV | owner: writer-app | tag: book-m1-accounts | status: expanded -->
# Solutions for Chapter 26

### Exercise 26.1 ★ Who may upload?

PUBLISHER and ADMIN (`hasAnyRole("PUBLISHER", "ADMIN")`). A signed-in READER gets HTTP 403 with a JSON body in the `{"error": "..."}` shape: `SecurityErrorResponses` writes FORBIDDEN through the access-denied handler. An anonymous caller gets 401 instead.

### Exercise 26.2 ★ Why deny by default

Rules match top to bottom, and the last one catches everything nobody thought of. If it were `permitAll()`, every route added later and not listed would be public until someone noticed. With `denyAll()`, a forgotten route is closed, and you find out in testing rather than in an incident.

### Exercise 26.3 ★★ Normalize and validate

- `Alice` becomes `alice`: accepted.
- ` bob ` becomes `bob` (trimmed and lower-cased): accepted, because it is 3 characters.
- `a` stays `a`: rejected, fewer than 3 characters.
- `Carol!` becomes `carol!`: rejected, because `!` is not in `[a-z0-9._-]`.
- `dave.smith` stays `dave.smith`: accepted.
(All of them also need an acceptable password of 12 to 128 characters and a role, and must not already exist.)

### Exercise 26.4 ★★ Sliding window

Yes, it is refused. The window is 60 seconds and all 120 earlier requests happened within the last 20 seconds, so the count is 120, which reaches the limit. The oldest recorded request was at about second 0 and leaves the window at second 60; now is second 20, so `Retry-After` is about 40 seconds (the code computes the time between the window's cutoff and the oldest timestamp, rounds up to whole seconds and uses at least 1).

### Exercise 26.5 ★★★ Bind a token

The signature check passes: the token really was issued by the server, and it hasn't expired. The next check compares the token's session binding, `HMAC("tile-binding:" + Alice's session id)`, with the binding recomputed from *Bob's* own session id (`SessionKeys.tileBindingMatches`, a constant-time comparison). They differ, so the request is refused as issued to a different session. Signing alone proves only that the server issued the token; without the binding, anyone holding the URL could use it until it expires.

### Exercise 26.6 ★★★ Inspect the cookies

One good answer: the session cookie (`SDV_SESSION`) is httpOnly, so JavaScript can't read it, while the CSRF cookie (`XSRF-TOKEN`) is created with `withHttpOnlyFalse()` so Angular's `HttpClient` can copy its value into the `X-XSRF-TOKEN` request header. That is the double-submit pattern: another site can make your browser send the cookies but can't read the CSRF value to put it in the header. Both cookies are `SameSite=Strict`.
