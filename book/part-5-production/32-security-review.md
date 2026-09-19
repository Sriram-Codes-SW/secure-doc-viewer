<!-- chapter: 32 | part: V | owner: writer-production | tag: book-m5-platform | status: draft -->
# Chapter 32: Security review and threat modeling

This chapter teaches you to look at your own app the way an attacker does, and walks through the
review rounds that hardened the Secure Document Viewer before it was declared ready for a real
host. It matters because every control in Parts II to IV answers a question this chapter asks
systematically: what could go wrong, and who would make it go wrong?

## Learning objectives

By the end of this chapter, you will be able to:

- Name the assets, actors, and entry points of this app and say which control protects each.
- Explain why the project's review rounds found bugs that its tests had not.
- Recognize four classes of finding: forged inputs, races, resource exhaustion, and information leaks.
- Diagnose why a claim about security is only a hypothesis until a test tries to break it.
- State what the app does not defend against, and why.
- Run a small review of your own on a feature you build.

## Prerequisites

- Chapter 8: HTTP, headers, cookies, and status codes.
- Chapter 10: Docker and Docker Compose (containers that talk over a private network).
- Chapter 15: Spring Security basics (accounts, roles, sessions).
- Chapter 16: CSRF, throttling, and access checks.
- Chapter 24: end-to-end tests with Playwright (a tool that drives a real browser).
- Chapter 30: the platform milestone (`book-m5-platform`).

Chapter 33 teaches nginx and Caddy properly. This chapter needs only the idea that they are
programs standing between the browser and the app; each is glossed where it first appears.

## Beginner tier: Thinking like an attacker

### 32.1 The analogy: hiring a lock-picker

Imagine you fitted every door in your shop with a good lock. Before opening day, you hire a
locksmith and say: "Try to get in without the key." The locksmith doesn't admire your locks. They
test the window latch you forgot, the back door you propped open, and the key you left under the
mat. A **threat model** is that exercise, done on paper first and then with real tests.

The analogy breaks down in one place. A shop has a handful of doors. A web app has as many entry
points as it has URLs, headers, cookies, and form fields, and an attacker can try thousands per
minute with a script.

### 32.2 Terms you need

- **Asset:** something worth protecting. Here: the document content, the accounts, and the audit trail.
- **Actor:** someone who interacts with the system: readers, publishers, administrators, and attackers (who may also hold a valid account).
- **Entry point:** anywhere data crosses into the system from outside: an HTTP endpoint, a header, a cookie, an uploaded file.
- **Trust boundary:** a line across which data stops being trustworthy. The browser is outside it. Section 32.6 shows where the project drew the line in front of the API.
- **Threat:** a way an actor could harm an asset.
- **Control:** something that reduces a threat, such as a signature check or a rate limit.
- **CVE:** a public identifier for a known vulnerability in software you use, such as "CVE-2026-1234". Section 32.5 mentions three that hit a web server library the app depends on.
- **BCrypt:** a deliberately slow way to store passwords so that a stolen database is hard to crack. It only looks at the first 72 bytes of a password, which matters in section 32.5.

### 32.3 The app's threat model on one page

Table 32.1 lists each asset with its threat, the control, and where it lives. You met most of
these controls in Chapters 15 and 16.

**Table 32.1 — Assets, threats, and controls**

| Asset | Threat | Control | Where |
|---|---|---|---|
| The PDF | Download it whole | The PDF stops existing after ingest; only tiles remain; no endpoint returns a page or document | `TileGenerationService`, `TileController` |
| A tile | Copy a URL and reuse or share it | HMAC signature, 120-second expiry, session binding | `SignedUrlService`, `SessionKeys` |
| A document | Open one that isn't shared with you | Owner and visibility check on every request; `404`, not `403` | `document/` package |
| An account | Guess passwords | BCrypt, three throttle counters, forced first-password change (`PasswordChangeRequiredFilter`) | `LoginThrottle`, `SecurityConfig` |
| The session | Act on someone's behalf from another site | httpOnly cookie, CSRF token | `SecurityConfig` |
| Availability | Exhaust CPU or memory with uploads or tile requests | Page, pixel, and size limits; render pool; tile work cap; rate limit | `TileGenerationService`, `TileRateLimiter` |
| The audit trail | Flood it or lose denials | One `PAGE_VIEWED` per session, document, and page every 10 minutes; capped `ACCESS_DENIED`; own transaction | `TileController`, `audit/` package |

Every row is a claim you can test.

## Intermediate tier: The review rounds this project went through

*On a first read you can skip to "In this project"; Part IV tells the same story milestone by milestone.*

### 32.4 Who reviewed, and how

<!-- source: dossier/DOSSIER.md "Cautions for writers"; dossier/reviews.md -->
The project used two review roles, and the honest description matters. Both were **AI review
agents**, briefed to act as independent third parties: a "Product Owner reviewer" and a "Senior
Technical Manager reviewer". They read the code and wrote findings with IDs (PO-3, TM-2, and so
on), and a human product owner made the product calls. They were not human colleagues. What they
found was real, and the fixes were verified against the code, but treat them as a review
technique, not as an authority.

### 32.5 The rounds

<!-- source: dossier/bugs-and-findings.md; dossier/decisions.md D7, D11; PR #1 to #5 bodies -->
**Round 1 (PR #1, PR #2).** Two Critical findings led the list: sign-in accepted any username with
no password (TM-2, PO-3), and the admin surface was unprotected (TM-1). The fixes were BCrypt
accounts stored in MySQL, an admin-only `/api/admin/**`, sessions listed by an opaque handle and
never by id, and tile tokens that carry a keyed binding instead of the session id (TM-4). PR #2
added ownership, sharing, and an audit trail. Its description records a bug a test caught: denial
events were being rolled back together with the failed request and never saved, so audit writes
moved to their own database transaction.

**Round 2 (PR #5: reviewed at `2d10e07`, fixed in `2d82253`).** The Senior Technical Manager
reviewer recommended not merging until one High finding was fixed, and the finding was
introduced by the pull request under review.

A **reverse proxy** is a program that receives requests from browsers and passes them to the app
behind it; **nginx** is the one this project uses, and Chapter 33 covers it. A proxy tells the app
who the original caller was in a header called `X-Forwarded-For`.

<!-- source: PR #5 body, "Correction"; commit 2d82253; dossier/decisions.md D11 -->
> **Incident: the spoofable client address (TM2-1).** The first nginx configuration appended to
> any `X-Forwarded-For` header the client sent. The API used that header as the client's address
> for sign-in throttling, so an attacker could invent a new address on every attempt and never
> reach the lockout. The PR description first claimed forwarded addresses were trusted only
> behind the proxy; the review showed that was false, and the description was corrected. The fix:
> nginx now overwrites the header with the real connection address. A Playwright test that goes
> through nginx fails on the old stack ("Expected 429, Received 401") and passes on the new one.
> Lesson: a claim about security is a hypothesis until a test that goes through the real front
> door has tried to break it.

The same round found that managing a document needed a second check (TM2-3): it now requires
ownership *and* the PUBLISHER role (or ADMIN), so a demoted publisher keeps read access only. It
also found the audit log could be flooded (TM2-4, PO2-4).

<!-- source: PR #5 body "TM3-1"; commit 82c24b6; dossier/decisions.md D7 -->
**Round 3 (TM3-1, commit `82c24b6`).** The first fix for password guessing added an account-wide
lockout across all addresses. That let anyone lock any user out by failing 20 times. The
replacement is the recognised-device rule from the README: the account-wide counter applies only
to attempts from unrecognised devices. Its cost is stated plainly there: during a distributed
attack, the owner can still sign in from a usual device but not from a new one until the window
passes or an administrator presses Unlock.

<!-- source: commits 1ce2c8b, f682716, 782ab6b, 6cf17fa; dossier/bugs-and-findings.md -->
**Later rounds (commits `1ce2c8b`, `f682716`, `782ab6b`, `6cf17fa`).** Atomic sign-in throttling,
a 72-byte password limit (BCrypt's maximum), a render timeout, a session lifetime cap, versioned
tile tokens, bounded renders, a tile work cap, and an upgrade of Tomcat (the web server library
inside Spring Boot) from 11.0.24 to 11.0.26 to close three published CVEs. Follow-ups added a
queue-free render pool and a refund of the rate-limit slot when the server was busy.

## Advanced tier: Classes of finding

*On a first read you can skip to "In this project"; the four classes are worth returning to when you review your own code.*

Reviews find the same shapes of bug repeatedly. Learn the shapes and you can find them in your own code.

### 32.6 Class 1: forged inputs

Anything a client sends can be forged, including headers. The rule the project ended with is the
README's "Trust boundary": the API trusts `X-Forwarded-For` only when `FORWARD_HEADERS_STRATEGY=native`,
and then only from `TRUSTED_PROXY_REGEX`, which in compose is nginx's fixed address `172.28.0.10`.
Anything reaching `app:8080` directly is judged by its own address.

<!-- source: PR #5 body "Live two-IP lockout test"; dossier/decisions.md D7 -->
nginx overwrites the header with the connection address and accepts a forwarded address only
from the optional HTTPS front end, **Caddy** (a web server that handles certificates, at
`172.28.0.11`; Chapter 33). In the two-address live test, all 18 checks passed: a spoofed header
sent through nginx and directly to `app:8080` still hit the lockout, and the spoofed address
never appeared in the audit log.

### 32.7 Class 2: races

Two requests can interleave. The project found and closed two:

- **Sign-in throttling.** Checking a counter and then incrementing it lets a burst of parallel guesses all pass the check. The fix counts the attempt *before* the password is checked and hands it back if the password was right, so parallel guesses get no more tries than sequential ones. A test proves parallel wrong passwords get exactly the allowed number.
- **Replacing a PDF.** Readers must never see a page half old and half new. The replacement renders into a new `v{n}` tile folder and switches the document to it under a database row lock; old tokens answer `410`. `MySqlIntegrationTest` runs two concurrent replacements against real MySQL 8.4 and checks that the row lock serializes them.

### 32.8 Class 3: resource exhaustion

A valid user can still hurt the service. Uploads are capped at 50 MB, must start with `%PDF-`, and
are rejected over 500 pages or over 40 million pixels per page (a guard against a "decompression
bomb": a tiny file that expands into enormous pages). At most two PDFs render at once (`503` with
`Retry-After`, a header that tells the client how many seconds to wait), a render that exceeds
`render-timeout` (3 minutes) is abandoned, and tile watermarking has a server-wide cap. The
per-user tile rate limit (180 per 60 seconds) bounds harvesting speed, not possibility.

### 32.9 Class 4: information leaks

- **`404` instead of `403`.** A stranger cannot tell whether a private document exists.
- **Identical answers** for a wrong password and an unknown user.
- **Generic `500` responses** with a short reference; the full exception stays in the log.
- **`Referrer-Policy: no-referrer`**, because tile URLs carry tokens that would otherwise travel in the `Referer` header.
- **Session ids never exposed:** the admin UI shows a keyed handle, and tiles carry a keyed binding (Listing 32.1).

**Listing 32.1 — `SessionKeys.java`, `book-m5-platform` (excerpt: two of the class's methods; the constructor, the admin handle, and the private helpers are omitted)**

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
characters were right from the response time. The omitted admin handle uses the same HMAC
computation with a different context string (`admin-handle:`), so one value cannot stand in for
the other.

<!-- source: PR #4 body 4c; dossier/decisions.md D9 -->
The trace code in the watermark has its own story. The admin handle uses Crockford Base32 (no I,
L, O, or U) because, while checking the first version by eye, the implementer misread an `I` as
an `l` in a watermark (PR #4). A control a human cannot read back is not much of a control.

### 32.10 Reading the filter chain that enforces it

Most of Table 32.1 comes together in one file. Read Listing 32.2 with the table beside you.

**Listing 32.2 — `SecurityConfig.java`, `book-m5-platform` (simplified: the imports and class Javadoc; the `.securityContext(...)` and `.exceptionHandling(...)` calls; the two `addFilter...` calls that register `SessionLifetimeFilter` and `PasswordChangeRequiredFilter`; `.requestCache(...)`; the source comments; and the bean methods for the password encoder, session registry, and CSRF repository are omitted)**

```java
http
    .csrf(csrf -> csrf
            .csrfTokenRepository(csrfTokenRepository)
            .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler()))
    .sessionManagement(session -> session
            .sessionFixation(fixation -> fixation.changeSessionId())
            .maximumSessions(-1)
            .sessionRegistry(sessionRegistry)
            .expiredSessionStrategy(errors))
    .headers(headers -> headers
            .contentSecurityPolicy(csp -> csp.policyDirectives(
                    "default-src 'none'; frame-ancestors 'none'; base-uri 'none'; form-action 'none'"))
            .referrerPolicy(referrer -> referrer.policy(ReferrerPolicy.NO_REFERRER))
            .permissionsPolicyHeader(permissions -> permissions.policy(
                    "camera=(), microphone=(), geolocation=(), payment=()")))
    .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/health/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/actuator/prometheus")
                    .access(fromAddresses(properties.getMetricsAllowedAddresses()))
            .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.POST, "/api/documents").hasAnyRole("PUBLISHER", "ADMIN")
            .requestMatchers(HttpMethod.PUT, "/api/documents/*/file").hasAnyRole("PUBLISHER", "ADMIN")
            .requestMatchers("/api/users/**").hasAnyRole("PUBLISHER", "ADMIN")
            .requestMatchers("/api/**").authenticated()
            .requestMatchers("/error").permitAll()
            .anyRequest().denyAll())
    .formLogin(AbstractHttpConfigurer::disable)
    .httpBasic(AbstractHttpConfigurer::disable)
    .logout(AbstractHttpConfigurer::disable);
```

The omitted `PasswordChangeRequiredFilter` is what enforces the "forced first-password change"
row in Table 32.1: until an account whose password an administrator set has chosen its own, the
API answers `403 passwordChangeRequired`. Five details in the listing deserve a second look:

1. **The last rule is `denyAll()`.** A URL nobody thought about is refused, not allowed. Rules are checked top to bottom and the first match wins, so specific rules come first.
2. **CSRF** (cross-site request forgery: another website making your browser send a request with your cookie) is stopped by a token that Angular copies from a cookie JavaScript may read (`withHttpOnlyFalse()`, in the omitted bean) into the `X-XSRF-TOKEN` header. The session cookie is the **httpOnly** one, meaning JavaScript cannot read it at all. The CSRF cookie is `SameSite=Strict`, meaning the browser sends it only for requests that start on the app's own site.
3. **Session fixation protection** (`changeSessionId`) gives you a new session id at sign-in, so an id planted before sign-in is worthless afterward.
4. **`maximumSessions(-1)` means unlimited sessions, but registered ones.** The registry is what lets an administrator list and revoke them.
5. **The CSP** (Content Security Policy, a header telling the browser what a page may load) is `default-src 'none'`. The API returns only JSON and PNG tiles, so nothing it serves needs to run a script or be framed. The Angular app has its own, different policy set by nginx (Chapter 33).

The session is a server-side HTTP session in a cookie. The project's records do not show a
comparison with keeping tokens in the browser, so this book states the outcome only; Chapter 37
weighs the options.

### 32.11 What is not defended, and why

The README's Limitations section is part of the security review, not an apology.

- **Screenshots and photographs.** Anything rendered can be captured. The goal is to raise cost and add attribution through the watermark.
- **A determined user with a valid session** can fetch every tile. The rate limit makes this slow (about half an hour for 500 pages at the defaults), not impossible. The product owner accepted these defaults on September 19, 2026 and will revisit them using `sdv_tiles_rate_limited_total`.
- **No MFA** (multi-factor authentication: a second proof of identity beyond a password), including for admins.
- **Right-click blocking** in the browser stops nothing that DevTools cannot undo. It was added as friction, on purpose.
- **No text layer**, so screen readers get nothing from a page image.

### 32.12 Doing your own review

You don't need a security team to start. Take one feature and ask these five questions in order.

1. **What is the asset, and who is allowed to touch it?** Write both down. If you can't, you can't review it.
2. **Where does input come from, and which of it can the caller forge?** Include headers, cookies, file names, and file contents, not only form fields.
3. **What does the code check, and where?** Find the check for *every* path that reaches the asset. The app checks document access on the list, the manifest, URL issuing, and every tile request; a check missing from one path is the classic bug.
4. **What happens if two requests arrive at once, or a thousand?** Look for check-then-act sequences and for work that has no upper bound.
5. **What does a failure reveal?** Compare the response for "doesn't exist" and "not allowed", and read error messages for paths, SQL, and stack traces.

Then write a test for each answer that surprised you, and make it go through the real front door,
as the incident in section 32.5 taught.

## In this project

| Path | First appears | What it does |
|---|---|---|
| `src/main/java/com/example/securedocviewer/security/SecurityConfig.java` | `book-m1-accounts`, hardened by `book-m5-platform` | The filter chain (Listing 32.2) |
| `src/main/java/com/example/securedocviewer/security/SessionKeys.java` | `book-m1-accounts` | Tile binding and admin handle (Listing 32.1) |
| `src/main/java/com/example/securedocviewer/security/LoginThrottle.java` | `book-m1-accounts` | The three lockout counters |
| `frontend/nginx.conf` | `book-m5-platform` | Overwrites `X-Forwarded-For`; the front door |
| `MySqlIntegrationTest` (under `src/test/java`) | `book-m5-platform` | Concurrent replacement and UTC checks on real MySQL |

See any of them with `git show book-m5-platform:<path>`.

## Try it

### Exercise 32.1 ★ Find the control

For each row of Table 32.1, name the file in the repository that holds the control. Hint: use `git grep` for the class name in the last column, at tag `book-m5-platform`.

### Exercise 32.2 ★★ Predict the denial

In a scratch copy of `SecurityConfig.java` at `book-m5-platform`, move `.anyRequest().denyAll()` to the top of the `authorizeHttpRequests` rules. Predict which requests fail, including the health check, then run the tests and compare.

### Exercise 32.3 ★★ Threat-model a new feature

Write a one-paragraph threat model for a feature "download a document's audit history as CSV": assets, actors, entry points, and two controls.

### Exercise 32.4 ★★★ Attack the lockout

Explain why the project's first account-wide lockout (all addresses) was itself a vulnerability, describe what an attacker gains from the recognised-device design's trade-off, and suggest one mitigation that isn't in the app.

## Summary

- A threat model lists assets, actors, entry points, threats, and controls; Table 32.1 is this app's.
- The project needed several rounds because fixes create new bugs: the forwarded-address fix came from a mistaken claim, and the first lockout let anyone lock out any user.
- The reviewers were AI agents briefed as independent third parties; their value was a second, adversarial reading.
- Four classes recur: forged inputs, races, resource exhaustion, and information leaks.
- The app states what it does not defend against: screenshots, a patient user with a valid session, and missing MFA.
- Claims about security need tests that try to break them through the real entry point.

Chapter 33 takes the app to a real host and puts nginx and Caddy in front of it.

## Further reading

- Spring Security reference documentation: https://docs.spring.io/spring-security/reference/
- MDN Web Docs, web security topics (CSP, cookies, `Referrer-Policy`): https://developer.mozilla.org/en-US/docs/Web/Security
- OWASP Application Security Verification Standard: https://owasp.org/www-project-application-security-verification-standard/
