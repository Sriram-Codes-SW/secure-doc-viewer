<!-- chapter: 30 | part: IV | owner: writer-app | tag: book-m5-platform | status: expanded -->
# Solutions for Chapter 30

### Exercise 30.1 ★ Forwarded header

Any client can send a header with any value. If the backend believed `X-Forwarded-For` from every sender, a client could choose the address that the sign-in throttle and the audit log see, resetting its own lockout at will. Trust belongs to a network position: nginx overwrites the header with the real peer address, and the API accepts forwarded headers only from nginx's fixed address (`TRUSTED_PROXY_REGEX`).

### Exercise 30.2 ★ Layers of an image

Docker caches each layer and reuses it when its inputs are unchanged. Dependencies change rarely (only when `pom.xml` changes) and the source changes on almost every build. Copying `pom.xml` first and downloading dependencies in their own layer means a source-only change rebuilds from the `COPY src` step, skipping the slow download.

### Exercise 30.3 ★★ Lockout abuse

With an account-wide count that applied to every address, an attacker could fail sign-in for a victim's account from several addresses until the total reached 20, and then the victim's own attempts (from their usual computer) were refused: a denial of service. The final rule applies the account-wide count only to unrecognised devices; an address that signed in successfully in the last 30 days is recognised and isn't blocked by it. The cost: a correct password from a new device is refused (429) during an account-wide lockout until an admin unlocks the account.

### Exercise 30.4 ★★ Order of gates

Cheap checks come before expensive ones, and unauthenticated requests must not be able to spend a legitimate user's allowance. The rate limit runs after the signature and session-binding checks (so only the real session's owner is counted) and before the disk read and render, so a throttled request doesn't pay their cost. The access check comes before the disk read because there is no reason to load a tile the user may not see, and because it is the check that turns an unshared or deleted document into a 404. (It also comes after the rate limit in the code, which means even requests for documents the user can't see are counted against the allowance.)

### Exercise 30.5 ★★★ Version inside the token

Only the signed fields of a token are tamper-proof. A version passed as a separate query parameter could be edited by the client to ask for an older or newer render without invalidating the signature, so a stale URL could still be redeemed. Signing the version means the token names exactly the render it was issued for; the server compares it with the document's current version and answers 410 when they differ.

### Exercise 30.6 ★★★ Design a limit

One good answer, following `TileWorkLimiter`: create a component with `private final Semaphore slots = new Semaphore(2, true)` and a method `run(Callable<T> work)` that calls `slots.tryAcquire(...)` with a short wait; on failure throw a "busy" exception that the error handler turns into HTTP 503 with a `Retry-After` header (a message such as "Too many exports are running. Try again in a few seconds."); on success run the work inside `try`/`finally` and release the permit in the `finally`. Add a metric counter for refusals, and make the permit count configuration.
