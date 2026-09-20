<!-- chapter: 16 | part: II | owner: writer-backend | tag: book-m6-final | status: expanded -->
# Solutions: Chapter 16

### Exercise 16.1 ★ What does `denyAll()` do?

`GET /api/does-not-exist` from a signed-in reader matches the rule `/api/**` with `authenticated()`. The reader is authenticated, so security lets the request through; no controller has that path, so Spring raises a "no resource" error, and `GlobalExceptionHandler` turns it into a `404` with `{"error": "Not found."}` (`ErrorContractTest` checks this). `GET /somewhere-else` matches no earlier rule, so it reaches `anyRequest().denyAll()`. For an anonymous caller that produces the `401` "Sign-in required." (the authentication entry point); for a signed-in caller it produces `403`. Either way the request never reaches application code.

### Exercise 16.2 ★ Two cookies, two rules

The app's JavaScript has to copy the CSRF token into the `X-XSRF-TOKEN` header, so it must be able to read the `XSRF-TOKEN` cookie. The session cookie is only ever sent back by the browser and is never needed by scripts, so it can be `httpOnly` and out of their reach, which protects the real credential if a script gets injected. The difference is safe because the CSRF token alone can't do anything: a forged request also needs the session cookie, which a foreign page can make the browser send but can't read, and it needs the header, which a foreign page can't write.

### Exercise 16.3 ★★ Read the headers on your own copy

Run, against your own copy:

```bash
curl -i http://localhost:8080/api/auth/me
```

You should find `Content-Security-Policy`, `Referrer-Policy: no-referrer`, `Permissions-Policy`, `X-Content-Type-Options: nosniff` and `X-Frame-Options: DENY`, matching the rows of Table 16.1 (`SecurityHeadersTest` asserts the same list). Which would you notice last is a judgment: most people would say `Permissions-Policy`, because the API never uses those browser features anyway, so nothing visibly breaks when it's missing; the headers whose absence has real consequences are the CSP and `Referrer-Policy`, because the signed tile URLs would leak through the latter.

### Exercise 16.4 ★★ 404 or 403?

- (a) `404`. `DocumentService` reports a document the caller can't view as not found, so its existence isn't revealed.
- (b) `403`. The reader can see the document, so hiding it would be pointless, but sharing needs ownership (as a publisher) or the admin role, and `requireManageable` throws `ForbiddenException`.
- (c) `403`. `SecurityConfig` requires the `ADMIN` role for `/api/admin/**`, and the access-denied handler writes the JSON.
- (d) `401`. Nobody is signed in, so the authentication entry point answers `Sign-in required.`

(a) and (b) are decided in the service; (c) and (d) are decided by the rules in `SecurityConfig`, before a controller runs.

### Exercise 16.5 ★★★ Break the throttle in a scratch copy

A worked outline. With check-then-act, all twelve threads pass `checkAllowed` at nearly the same instant, because no failure has been recorded yet, and each then spends about a tenth of a second in BCrypt before recording its failure. Far more than five passwords are actually verified, so the count of `401` responses is well above `MAX_FAILURES_PER_ACCOUNT` and the assertion `assertEquals(MAX_FAILURES_PER_ACCOUNT, guessed, ...)` fails. The exact number varies by run, which is itself the sign of a race. The original passes because `reserve` is `synchronized` and counts before the password is verified, so the sixth simultaneous caller finds the limit already reached and gets `429`. Put the code back and confirm that the test passes every time.

### Exercise 16.6 ★★★ Design a lockout that can't be abused

One good answer.

- **Count atomically.** Reserve an attempt before checking the PIN, as `LoginThrottle.reserve` does, and give it back on success. Do the check and the count under one lock or one atomic database update (for example, an `UPDATE ... SET attempts = attempts + 1 WHERE attempts < 3` and look at the number of rows changed).
- **Scope the count.** Count per account *and* per address, so a single address can't burn everyone's attempts and one account's attempts from one address can't be reset by other addresses.
- **Protect the owner.** Don't let unknown addresses lock the owner out of a device they've used successfully: keep a short list of recognised devices (stored as keyed hashes, not raw addresses), as `KnownDevices` does.
- **Recovery.** A locked-out legitimate owner needs a path that an attacker can't trigger for them: an administrator unlock, or a reset link sent to a verified channel, plus an audit event recording the lock and unlock. Accept and document the trade-off, as the project does for a new device during a lockout.
- **Store.** In memory is fine for a single instance and a short window (a restart clears it); use the database if attempts must survive restarts or be shared between instances.
