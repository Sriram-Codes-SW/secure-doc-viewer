# Solutions: Chapter 32

### Exercise 32.1 ★ Find the control

- PDF: `TileGenerationService` (deletes the source after ingest) and `TileController` (the only pixel endpoint).
- Tile: `SignedUrlService` (HMAC and expiry) and `SessionKeys` (session binding).
- Document: the `document/` package (`DocumentService` checks owner, visibility, and shares).
- Account: `LoginThrottle` (counters) and `SecurityConfig` (registers `PasswordChangeRequiredFilter`).
- Session: `SecurityConfig` (CSRF repository, cookie settings, session management).
- Availability: `TileGenerationService` (limits, render pool, timeout) and `TileRateLimiter`.
- Audit trail: `TileController` (`PAGE_VIEW_AUDIT_INTERVAL`, 10 minutes) and the `audit/` package.

Run `git grep -n ClassName book-m5-platform` to confirm each path.

### Exercise 32.2 ★★ Predict the denial

With `denyAll()` first, every request matches it before any other rule, so every request is refused, including `POST /api/auth/login` and `GET /actuator/health`. The health check failing is the surprise: the compose health check for the app container would report it unhealthy, so `web` (which depends on a healthy `app`) would not start. Most security integration tests fail, because they expect sign-in to work.

### Exercise 32.3 ★★ Threat-model a new feature

A model answer. Asset: the audit history (who viewed what, and when). Actors: administrators (allowed), other roles (not), an attacker with a stolen reader session. Entry points: the export endpoint and its query parameters. Controls: restrict the endpoint to `ADMIN` in `SecurityConfig` (as `/api/admin/**` already is); neutralize spreadsheet formulas in exported cells (the existing CSV export does this); cap the number of rows or the date range per export so one request cannot exhaust memory. Any two of these earn full credit.

### Exercise 32.4 ★★★ Attack the lockout

The first design counted failures per account across all addresses. Anyone could fail 20 times against a victim's username and lock the real owner out (a denial of service against the victim).

The recognized-device design applies that account-wide counter only to attempts from unrecognized devices. The attacker's gain: while an account is under a distributed attack, its owner cannot sign in from a new device (new laptop, hotel network) until the window passes or an administrator unlocks it. An attacker who knows this can time an attack to coincide with a victim's travel.

A mitigation not in the app: a second proof for unrecognized devices, such as an emailed one-time code or MFA, so that a new device can prove itself without waiting for an administrator.

### Exercise 32.5 ★★ Reorder the checks

The user would still be refused, but the server would already have done the expensive work: reading the tile from disk, drawing the watermark, and encoding a PNG. A signed-in attacker over their limit could keep sending requests and force that work on every one, spending the server's CPU while receiving nothing useful. That turns the rate limit from a protection for the server into a cosmetic one, and it would also make the `503` busy-cap fire for everyone else. The third comment in the listing warns against exactly this: the limit is enforced "before the disk read/render so a throttled request doesn't pay that cost". The same comment explains why it runs after authentication: "so unauthenticated requests can't burn a legitimate user's allowance."

### Exercise 32.6 ★★★ Review a new endpoint

A model answer. (1) Asset and users: the page-1 thumbnail of a document; the same people who may view the document (owner, shared users, everyone if visibility is EVERYONE, admins). (2) Forgeable inputs: the document id in the path, the session cookie; every other value must be derived from server state, so nothing else should be accepted from the caller. (3) Checks: authentication (`/api/**` is `authenticated()`); the same document-access check used for the list and the tile requests, answering `404` for "not found" and "not yours"; a rate limit, because a thumbnail is still a page image; and the same watermark, because a thumbnail is still content (or the endpoint shouldn't exist). The check must run on every request, not only when a link is issued. (4) Concurrency: many thumbnails at once cost CPU, so route them through the existing tile work limiter or a similar cap, and refund allowance on `503`. (5) Failure: identical responses for missing and not-shared documents; generic errors; no file paths. Tests: an integration test that a reader without a share gets `404` for a real document id, identical to the answer for a random id; and an end-to-end test through nginx in which an outsider is told the document doesn't exist, as in the project's Playwright test.
