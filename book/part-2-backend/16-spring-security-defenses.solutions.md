<!-- chapter: 16 | part: II | owner: writer-backend | tag: book-m6-final | status: draft -->
# Solutions: Chapter 16

1. (★) Under `/api/**` it falls under the `authenticated()` rule, so a signed-in user reaches it. Outside `/api` and the listed paths, `anyRequest().denyAll()` refuses it. Either way, an endpoint with a role requirement gets none until you add a rule.
2. (★) The app's JavaScript must copy the CSRF token into a header, so it has to read the cookie. The session cookie is only ever sent back by the browser, so scripts have no need to read it, and hiding it protects it from theft.
3. (★★) So parallel attempts can't all pass the check before any is counted. Counting first, and handing the count back on success, bounds the number of guesses to the limit.
4. (★★) `404`. A `403` would confirm the id exists; `404` reveals nothing.
5. (★★★) With `native` on a directly reachable server, any client could send a forged `X-Forwarded-For` header and the server would believe it, so an attacker could pick a new fake address for every attempt and avoid the per-address throttle.
