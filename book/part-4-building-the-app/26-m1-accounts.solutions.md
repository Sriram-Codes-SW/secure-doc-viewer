<!-- chapter: 26 | part: IV | owner: writer-app | tag: book-m1-accounts | status: draft -->
# Solutions for Chapter 26

### Exercise 26.1 ★ Who may upload?

PUBLISHER and ADMIN (`hasAnyRole("PUBLISHER", "ADMIN")`). A signed-in READER gets HTTP 403 with a JSON
body in the `{"error": "..."}` shape: `SecurityErrorResponses` writes FORBIDDEN through the access-denied
handler. An anonymous caller gets 401 instead.

### Exercise 26.2 ★★ Why deny by default

Rules match top to bottom, and the last one catches everything nobody thought of. If it were
`permitAll()`, every route added later and not listed would be public until someone noticed. With
`denyAll()`, a forgotten route is closed, and you find out in testing rather than in an incident.

### Exercise 26.3 ★★★ Inspect the cookies

One good answer: the session cookie is httpOnly, so JavaScript can't read it, while the CSRF cookie
(`XSRF-TOKEN`) is created with `withHttpOnlyFalse()` so Angular's `HttpClient` can copy its value into a
request header. That is the double-submit pattern: another site can make your browser send the cookies
but can't read the CSRF value to put it in the header. Both cookies are `SameSite=Strict`
(`SecurityConfig.csrfTokenRepository`).
