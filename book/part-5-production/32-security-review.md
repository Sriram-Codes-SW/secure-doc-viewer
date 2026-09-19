# Chapter 32: Security review and threat modeling

Tag: `book-m5-platform`. Prerequisites: Chapters 15, 16, and 30.

> **Draft status:** sections 32.1 to 32.4 are written; 32.5 and the exercises are next. Review
> IDs (TM, PO) come from the pull request descriptions (PR #1 to #5).

## Learning objectives

By the end of this chapter you can:

- name the assets, actors, and entry points of this app and say which control protects each;
- explain why the project's review rounds found bugs that its tests did not;
- recognize four classes of finding: spoofed inputs, races, resource exhaustion, and information leaks;
- state plainly what the app does not defend against, and why;
- run a small review of your own on a feature you build.

## Beginner tier: thinking like an attacker

### The analogy

Picture a museum. The architect designed the building for visitors: doors, stairs, a gift shop. A
security consultant walks through the same building with a different question: "If I wanted to
leave with a painting, which door would I use?" A threat model is that second walk-through, done
on paper before someone does it for real.

The analogy breaks down in one place. A museum has a handful of doors. A web app has as many
entry points as it has URLs, headers, cookies, and form fields, and an attacker can try thousands
per minute with a script.

### Terms

- **Asset:** something worth protecting. Here: the document content, the accounts, and the audit trail.
- **Actor:** someone who interacts with the system: readers, publishers, administrators, and attackers (who may also hold a valid account).
- **Entry point:** anywhere data crosses into the system from outside: an HTTP endpoint, a header, a cookie, an uploaded file.
- **Trust boundary:** a line across which data stops being trustworthy. The browser is outside it; the network in front of the API is a boundary you must define (see the Trust boundary discussion below).
- **Threat:** a way an actor could harm an asset.
- **Control:** something that reduces a threat, such as a signature check or a rate limit.

### The app's threat model on one page

**Table 32.1 — Assets, threats, and controls**

| Asset | Threat | Control | Where |
|---|---|---|---|
| The PDF | Download it whole | The PDF stops existing after ingest; only tiles remain; no endpoint returns a page or document | `TileGenerationService`, `TileController` |
| A tile | Copy a URL and reuse or share it | HMAC signature, 120-second expiry, session binding | `SignedUrlService`, `SessionKeys` |
| A document | Open one that isn't shared with you | Owner and visibility check on every request; `404`, not `403` | `document/` package |
| An account | Guess passwords | BCrypt, three throttle counters, forced first-password change | `LoginThrottle` |
| The session | Act on someone's behalf from another site | httpOnly cookie, CSRF token | `SecurityConfig` |
| Availability | Exhaust CPU or memory with uploads or tile requests | Page, pixel, and size limits; render pool; tile work cap; rate limit | `TileGenerationService`, `TileRateLimiter` |
| The audit trail | Flood it or lose denials | One `PAGE_VIEWED` per session, page, and 10 minutes; capped `ACCESS_DENIED`; own transaction | `audit/` package |

Every row is a claim you can test. Chapters 15 and 16 built most of these controls; this chapter
asks the harder question of whether they hold under attack.

## Intermediate tier: the review rounds this project went through

The project did not get its security in one pass. Two independent reviewers, whose findings carry
the IDs TM (Senior Technical Manager) and PO (Product Owner), read the app in rounds, and each
round found problems that the previous fixes had introduced or missed. The pull request
descriptions record them. The pattern matters more than any single bug.

**Round 1 (PR #1, PR #2).** Two Critical findings led the list: no real accounts (TM-2, PO-3) and
an unprotected admin surface (TM-1). The fixes were BCrypt accounts stored in MySQL, an
admin-only `/api/admin/**`, sessions listed by an opaque handle and never by id, and tile tokens
that carry a keyed binding instead of the session id (TM-4). PR #2 added ownership, sharing, and
an audit trail. Its description records a bug a test caught: denial events were being rolled back
together with the failed request and never saved, so audit writes moved to their own transaction.

**Round 2 (PR #5, commit `2d82253`).** The reviewer recommended not merging until one High finding
was fixed, and the finding was introduced by the very pull request under review.

> **Incident: the spoofable client address (TM2-1).** The first nginx configuration appended to
> any `X-Forwarded-For` header the client sent. The API used that header as the client's address
> for sign-in throttling, so an attacker could invent a new address on every attempt and never
> reach the lockout. The PR description first claimed forwarded addresses were trusted only behind
> the proxy; the review showed that was false, and the description was corrected. The fix: nginx
> now overwrites the header with the real TCP peer. A Playwright test that goes through nginx
> fails on the old stack ("Expected 429, Received 401") and passes on the new one. Lesson: a
> claim about security is a hypothesis until a test that goes through the real front door has
> tried to break it.

The same round found that managing a document needed a second check (TM2-3): it now requires
ownership *and* the PUBLISHER role (or ADMIN), so a demoted publisher keeps read access only. It
also found the audit log could be flooded (TM2-4, PO2-4).

**Round 3 (TM3-1).** The first fix for password guessing added an account-wide lockout across all
addresses. That let anyone lock any user out by failing 20 times. The replacement is the
recognised-device rule from the README: the account-wide counter applies only to attempts from
unrecognised devices. Its cost is stated plainly there: during a distributed attack, the owner
can still sign in from a usual device but not from a new one until the window passes or an
administrator presses Unlock.

**Later rounds (commits `1ce2c8b`, `f682716`, `782ab6b`, `6cf17fa`).** Atomic sign-in throttling,
a 72-byte password limit (BCrypt's maximum), a render timeout, a session lifetime cap, versioned
tile tokens, bounded renders, a tile work cap, and Tomcat CVE updates. Round 3 follow-ups added a
queue-free render pool and a refund of the rate-limit slot when the server was busy.

## Advanced tier: classes of finding

Reviews find the same shapes of bug repeatedly. Learn the shapes and you can find them in your own code.

### 1. Spoofed inputs and the trust boundary

Anything a client sends can be forged, including headers. The rule the project ended with (README,
"Trust boundary"): the API trusts `X-Forwarded-For` only when `FORWARD_HEADERS_STRATEGY=native`,
and then only from `TRUSTED_PROXY_REGEX`, which in compose is nginx's fixed address `172.28.0.10`.
Anything reaching `app:8080` directly is judged by its own address. nginx overwrites the header
with the TCP peer and accepts a forwarded address only from the optional HTTPS front end, Caddy at
`172.28.0.11`. The live review test (18 of 18 checks passed, per PR #5) showed a spoofed header
sent through nginx, through Caddy, and directly to `app:8080` all still hit the lockout.

### 2. Races

Two requests can interleave. The project found and closed several:

- **Sign-in throttling.** Checking a counter and then incrementing it lets a burst of parallel guesses all pass the check. The fix counts the attempt *before* the password is checked and hands it back if the password was right, so parallel guesses get no more tries than sequential ones. A test proves parallel wrong passwords get exactly the allowed number.
- **Replacing a PDF.** Readers must never see a page half old and half new. The replacement renders into a new `v{n}` tile folder and switches the document to it under a database row lock; old tokens answer `410`. `MySqlIntegrationTest` runs two concurrent replacements against real MySQL 8.4 and checks that the row lock serializes them.

### 3. Resource exhaustion

A valid user can still hurt the service. Uploads are capped at 50 MB, must start with `%PDF-`, and
are rejected over 500 pages or over 40 million pixels per page (a decompression-bomb guard). At
most two PDFs render at once (`503` with `Retry-After` otherwise), a render that exceeds
`render-timeout` (3 minutes) is abandoned, and tile watermarking has a server-wide cap. The
per-user tile rate limit (180 per 60 seconds) bounds harvesting speed, not possibility.

### 4. Information leaks

- **`404` instead of `403`.** A stranger cannot tell whether a private document exists.
- **Identical answers** for a wrong password and an unknown user.
- **Generic `500` responses** with a short reference; the full exception stays in the log.
- **`Referrer-Policy: no-referrer`**, because tile URLs carry tokens that would otherwise travel in the `Referer` header.
- **Session ids never exposed:** the admin UI shows a keyed handle, and tiles carry a keyed binding.

**Listing 32.1 — `SessionKeys.java`, `book-m5-platform` (excerpt: two methods)**

```java
public String tileBinding(String sessionId) {
    return derive("tile-binding:", sessionId, 16);
}

public boolean tileBindingMatches(String sessionId, String binding) {
    return sessionId != null && binding != null && MessageDigest.isEqual(
            tileBinding(sessionId).getBytes(StandardCharsets.UTF_8),
            binding.getBytes(StandardCharsets.UTF_8));
}
```

`MessageDigest.isEqual` compares in constant time, so an attacker cannot learn how many leading
characters were right from the response time. The class derives its second value, the admin
handle, with a different HMAC context (`admin-handle:`), so one value cannot stand in for the other.

The trace code in the watermark has its own story. The admin handle uses Crockford Base32 (no I,
L, O, or U) because, while checking the first version by eye, the developer misread an `I` as an
`l` in a watermark (PR #4). A control a human cannot read back is not much of a control.

## 32.4 What is not defended, and why

The README's Limitations section is part of the security review, not an apology.

- **Screenshots and photographs.** Anything rendered can be captured. The goal is to raise cost and add attribution through the watermark.
- **A determined user with a valid session** can fetch every tile. The rate limit makes this slow (about half an hour for 500 pages at the defaults), not impossible. The product owner accepted these defaults on September 19, 2026 and will revisit them using `sdv_tiles_rate_limited_total`.
- **No MFA, including for admins.** Sign-in is by password only.
- **Right-click blocking** in the browser stops nothing that DevTools cannot undo. It was added as friction, on purpose.
- **No text layer**, so screen readers get nothing from a page image.

## In this project

- Controls: `SecurityConfig.java`, `LoginThrottle.java`, `SessionKeys.java`, and the nginx configuration under `deploy/` and `frontend/`.
- Tests: the security integration tests, the concurrency tests, `MySqlIntegrationTest`, and the Playwright test that goes through nginx.
- Tag: `book-m5-platform`.

*To finish in this chapter:* 32.5 "Doing your own review" (a checklist), a verified `SecurityConfig`
listing for CSRF, CSP, and headers, Try it exercises, Summary, and Further reading. File paths in
"In this project" still need checking against the tag.
