<!-- chapter: 16 | part: II | owner: writer-backend | tag: book-m6-final | status: draft -->
# Chapter 16: Spring Security II: defenses

Signing in is only the start. This chapter covers the layers that keep a signed-in session safe and the sign-in endpoint hard to abuse: CSRF protection, per-endpoint authorization, security headers, throttling, session lifetime and the forced password change.

## Learning objectives

By the end of this chapter, you will be able to:

- Explain a CSRF attack and the cookie-plus-header defense the project uses.
- Read the authorization rules in `SecurityConfig` and explain why unreadable documents give 404, not 403.
- Explain what the project's security headers do.
- Describe how `LoginThrottle` counts attempts atomically.
- Explain why `X-Forwarded-For` is trusted only from a proxy.
- Explain what `SessionLifetimeFilter` and `PasswordChangeRequiredFilter` enforce.

**A note on versions.** All listings are quoted from `book-m6-final`, where these defenses are complete. `SpaCsrfTokenRequestHandler.java` is identical at `book-m1-accounts`; the throttling, session-lifetime and forced-password-change code arrived in later milestones, and `SecurityConfig` differs from its milestone 1 form.

## Prerequisites

- Chapter 8: how the web works (headers, cookies)
- Chapter 15: Spring Security I

## Beginner tier: Attacks the browser makes for you

### 16.1 CSRF: the attack and the cookie-plus-header defense

Your browser attaches the session cookie to every request to the app, whoever caused that request. Suppose you're signed in and visit a malicious page containing a hidden form that posts to the app's "delete document" address. The browser dutifully sends your cookie, and the server sees a valid session. This is **cross-site request forgery (CSRF)**: another site borrows your credentials.

The defense is a secret the attacker's page can't know. The server sets a second cookie, `XSRF-TOKEN`, whose value the app's own JavaScript reads and copies into a header (`X-XSRF-TOKEN`) on every state-changing request. A foreign page can make the browser send cookies but can't read this one, so it can't fill in the header. The server rejects any write whose header doesn't match. This is the **double-submit cookie** pattern, and it is what `SecurityConfig` sets up:

```java
@Bean
public CsrfTokenRepository csrfTokenRepository() {
    CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
    repository.setCookieCustomizer(cookie -> cookie.sameSite("Strict").path("/"));
    return repository;
}
```

(`book-m6-final`, `SecurityConfig.java`.) `withHttpOnlyFalse()` is deliberate: unlike the session cookie, this one *must* be readable by JavaScript. It reveals nothing useful on its own, because it's worthless without a valid session cookie as well.

**Where the analogy breaks down.** This isn't a password: it protects against forged requests, not against someone who already controls your browser (script injection defeats it).

The Single-Page-App handler in Listing 16.1 adapts Spring's default to Angular.

**Listing 16.1 — `SpaCsrfTokenRequestHandler.java` (`book-m6-final`, imports omitted)**

*`src/main/java/com/example/securedocviewer/security/SpaCsrfTokenRequestHandler.java`*

```java
final class SpaCsrfTokenRequestHandler implements CsrfTokenRequestHandler {

    private final CsrfTokenRequestHandler plain = new CsrfTokenRequestAttributeHandler();
    private final CsrfTokenRequestHandler xor = new XorCsrfTokenRequestAttributeHandler();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, Supplier<CsrfToken> csrfToken) {
        xor.handle(request, response, csrfToken);
        csrfToken.get();
    }

    @Override
    public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
        String header = request.getHeader(csrfToken.getHeaderName());
        return (StringUtils.hasText(header) ? plain : xor).resolveCsrfTokenValue(request, csrfToken);
    }
}
```

The class comment explains the design: the SPA echoes the raw cookie value in the header, while server-rendered values stay BREACH-protected, and "loading the token on every request makes sure the XSRF-TOKEN cookie is always present". The `SecurityIntegrationTest` case `stateChangingRequestsNeedACsrfToken` shows the result: a write without the token gets `403` with `Missing or invalid CSRF token. Reload the page and try again.`

## Intermediate tier: Who may do what

### 16.2 Authorization rules per endpoint; 404 vs 403

`SecurityConfig` lists the rules in order, first match wins (`book-m6-final`, excerpt):

```java
.authorizeHttpRequests(auth -> auth
        .requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/health/**").permitAll()
        // ...
        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
        .requestMatchers("/api/admin/**").hasRole("ADMIN")
        .requestMatchers(HttpMethod.POST, "/api/documents").hasAnyRole("PUBLISHER", "ADMIN")
        // Refuse readers before a 50 MB replacement body is received.
        .requestMatchers(HttpMethod.PUT, "/api/documents/*/file").hasAnyRole("PUBLISHER", "ADMIN")
        // Per-document owner/admin checks for edits live in DocumentService.
        .requestMatchers("/api/users/**").hasAnyRole("PUBLISHER", "ADMIN")
        .requestMatchers("/api/**").authenticated()
        .requestMatchers("/error").permitAll()
        .anyRequest().denyAll())
```

Two things stand out. The last rule, `anyRequest().denyAll()`, means anything not listed is refused: you must open a door on purpose. And the rules are coarse (by role and path); finer rules such as "only the owner may share this document" live in `DocumentService`, which has the document in hand.

The class comment of `DocumentService` states a related choice: "A document the user can't view is reported as not found, never as forbidden, so its existence isn't revealed." A `403` tells a prober "this id exists but is off-limits"; a `404` tells them nothing. The project applies `403` only when the caller can see the document but not change it.

### 16.3 Security headers: CSP, `X-Frame-Options`, `nosniff`, `Referrer-Policy`

Response headers can instruct the browser to be stricter. The project sets these in `SecurityConfig`:

```java
.headers(headers -> headers
        .contentSecurityPolicy(csp -> csp.policyDirectives(
                "default-src 'none'; frame-ancestors 'none'; base-uri 'none'; form-action 'none'"))
        .referrerPolicy(referrer -> referrer.policy(ReferrerPolicy.NO_REFERRER))
        .permissionsPolicyHeader(permissions -> permissions.policy(
                "camera=(), microphone=(), geolocation=(), payment=()")))
```

(`book-m6-final`, excerpt.) A **Content Security Policy** (CSP) tells the browser what a page may load; the API only returns JSON and PNGs, so `default-src 'none'` forbids everything, and `frame-ancestors 'none'` prevents the response from being framed by another site (clickjacking). `Referrer-Policy: no-referrer` matters here because tile URLs carry signed tokens: the code comment says "never send them onward in a Referer". Spring Security also adds `X-Content-Type-Options: nosniff` by default, which stops browsers guessing a content type. `SecurityHeadersTest` checks these headers (`book-m3-hardening` and later).

## Advanced tier: Abuse, proxies and lifetimes

### 16.4 Throttling sign-in attempts (`LoginThrottle`) and atomic counting

Hashing is slow on purpose, but an attacker can still try many passwords. `LoginThrottle` applies three rolling 15-minute rules: 5 failures for one account from one address, 20 failures from one address across any accounts (password spraying), and 20 failures for one account across all addresses. Listing 16.2 shows the core.

**Listing 16.2 — `LoginThrottle.reserve` (`book-m6-final`)**

*`src/main/java/com/example/securedocviewer/security/LoginThrottle.java`*

```java
public synchronized Instant reserve(String username, String clientIp, boolean recognisedDevice) {
    checkAllowed(username, clientIp, recognisedDevice);
    Instant at = clock.instant();
    recordFailure(username, clientIp, at);
    return at;
}

/** The reserved attempt succeeded: hand back its provisional failure and clear this address's account counter. */
public synchronized void succeeded(String username, String clientIp, Instant reservation) {
    failures.remove(accountKey(username, clientIp));
    removeOne(ipKey(clientIp), reservation);
    removeOne(anyIpKey(username), reservation);
}
```

The design is **atomic counting**. The naive way is: check the count, verify the password, then count a failure. With a burst of parallel requests, all of them pass the check before any is counted, so an attacker gets far more guesses than the limit. Instead, `reserve` checks and counts in one `synchronized` step, *before* the password is verified; a correct password hands the count back with `succeeded`. The class comment says why: "a burst of parallel guesses can't all pass the check before any of them is counted." The test `aBurstOfParallelWrongPasswordsGetsNoMoreThanTheLimit` covers it. The counters are in memory (a restart clears them, per the class comment), and a `@Scheduled` sweep prunes old entries (Chapter 14).

Addresses the account recently signed in from (`KnownDevices`) are exempt from the account-wide rule only, so an attacker hammering a username from elsewhere can't lock its owner out of the usual device. Stored addresses are keyed hashes, not raw IPs.

### 16.5 Trusting `X-Forwarded-For` only from a proxy

Throttling by address needs the real client address. Behind a reverse proxy, the connection comes from the proxy, and the original address arrives in the `X-Forwarded-For` header, which any client can also forge. `application.yml` therefore trusts it only when configured:

```yaml
server:
  forward-headers-strategy: ${FORWARD_HEADERS_STRATEGY:none}
  tomcat:
    remoteip:
      internal-proxies: ${TRUSTED_PROXY_REGEX:127\.0\.0\.1|0:0:0:0:0:0:0:1}
```

(`book-m6-final`, excerpt with comments trimmed.) Its comments explain: set `native` only behind a trusted proxy, "otherwise X-Forwarded-For is ignored, so clients can't spoof their IP to dodge login throttling", and the trusted-proxy pattern is pinned to the proxy's fixed address. Part V returns to deployment.

### 16.6 Session lifetime, idle timeout, revocation

The 30-minute idle timeout ends abandoned sessions, but a session in constant use would never expire. `SessionLifetimeFilter` adds a fixed cap (12 hours by default, `session-max-lifetime`):

```java
if (session.getAttribute(SIGNED_IN_AT) instanceof Instant signedInAt) {
    if (Instant.now().isAfter(signedInAt.plus(maxLifetime))) {
        session.invalidate();
        SecurityContextHolder.clearContext();
    }
}
```

(`book-m6-final`, `SessionLifetimeFilter.java`, excerpt.) The request then continues unauthenticated and gets the normal `401`. `SecurityConfig` registers sessions in a `SessionRegistry` and allows unlimited concurrent sessions (`maximumSessions(-1)`), so an admin can list sessions by an opaque handle and revoke them; a revoked session is refused on its next request with the message `Your session has ended. Please sign in again.` Sign-in also changes the session id (`changeSessionId`), which defeats **session fixation**, where an attacker plants a known id before you sign in.

#### A real incident: the CSRF cookie that vanished at sign-in

Spring's built-in step for rotating the CSRF token deleted the cookie and then re-read the token from the request, which still carried the old cookie, so the browser ended with no token and its first write after signing in failed with `403`. `AuthController` now generates and saves a fresh token itself so exactly one `Set-Cookie` is sent, and `CsrfCookieFlowTest` reproduces the browser's steps (with no test helper) to guard it. The lesson: test security flows the way a browser behaves, because test helpers can hide the very bug you're looking for; that test even needs its own fresh context because the helper "permanently swaps the CSRF filter's repository". <!-- source: AuthController.rotateCsrfToken comment and CsrfCookieFlowTest class comment at book-m6-final; dossier/bugs-and-findings.md#C1 and #C2 -->

### 16.7 Forced password change

Accounts whose password an admin set must choose their own at first sign-in. `PasswordChangeRequiredFilter` enforces it on the server, not just in the UI:

```java
if (session != null && Boolean.TRUE.equals(session.getAttribute(SESSION_ATTRIBUTE))
        && path.startsWith("/api/") && !path.startsWith("/api/auth/")) {
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.getWriter().write("{\"error\":\"You must change your password before continuing.\",\"passwordChangeRequired\":true}");
    return;
}
```

(`book-m6-final`, excerpt.) While the flag is set on the session, everything under `/api` except the auth endpoints returns `403`. Both custom filters are added around `AuthorizationFilter` in `SecurityConfig`. The test `anAdminSetPasswordMustBeChangedBeforeAnythingElseWorks` walks through it.

## In this project

**Table 16.1 — Where Chapter 16's ideas live (`book-m6-final`)**

| Idea | File |
|---|---|
| CSRF | `security/SecurityConfig.java`, `security/SpaCsrfTokenRequestHandler.java` |
| Rules and headers | `security/SecurityConfig.java` |
| Filter-chain JSON errors | `security/SecurityErrorResponses.java` |
| Throttling | `security/LoginThrottle.java`, `security/KnownDevices.java` |
| Session filters | `security/SessionLifetimeFilter.java`, `security/PasswordChangeRequiredFilter.java` |
| Proxy trust, timeouts | `src/main/resources/application.yml` |

## Try it

1. (★) What does `anyRequest().denyAll()` do to a new endpoint you forget to list?
2. (★) Why must the `XSRF-TOKEN` cookie be readable by JavaScript when the session cookie must not be?
3. (★★) Why does `LoginThrottle.reserve` count before the password is checked?
4. (★★) A reader asks for a document id that belongs to someone else. Which status does the server return, and why?
5. (★★★) Explain what would go wrong if `FORWARD_HEADERS_STRATEGY` were `native` on a server directly reachable from the internet.

## Summary

- CSRF protection uses a readable token cookie echoed in a header; foreign sites can't read it.
- Authorization is deny-by-default, with coarse rules in `SecurityConfig` and per-document rules in the service; unreadable documents look nonexistent.
- Security headers restrict what browsers do with the API's responses.
- `LoginThrottle` counts each attempt before verifying it, so parallel guessing is bounded.
- `X-Forwarded-For` is trusted only from a configured proxy.
- Filters enforce a fixed session lifetime and the forced password change on the server.

## Further reading

- *Spring Security Reference Documentation*, "Cross Site Request Forgery (CSRF)." https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html
- *Spring Security Reference Documentation*, "Authorize HttpServletRequests." https://docs.spring.io/spring-security/reference/servlet/authorization/authorize-http-requests.html
- *OWASP Cheat Sheet Series*, "Cross-Site Request Forgery Prevention Cheat Sheet." https://cheatsheetseries.owasp.org/
- *MDN Web Docs*, "Content Security Policy (CSP)." https://developer.mozilla.org/en-US/docs/Web/HTTP/CSP
