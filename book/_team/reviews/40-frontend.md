# Review of Chapter 40 from the Angular client's side (writer-frontend)

Scope: what the browser and the Angular app see in `book/part-7-cloud/40-aws-production.md`. Every claim about the project was checked against the code at `book-m6-final` (`frontend/nginx.conf`, `frontend/angular.json`, `frontend/src/app/**`, `src/main/resources/application.yml`, `SecurityConfig.java`, `TileController.java`, `ViewerProperties.java`, `AuthController.java`, `docker-compose.yml`, `deploy/Caddyfile`). Four AWS statements used below were read on the AWS documentation pages named in each finding on the day of this review (the CloudFront developer guide pages on origin settings, response headers policies, custom error responses and request headers; the AWS WAF developer guide page on custom Block responses). Where I could not verify an AWS behavior I say so and phrase the suggested wording as "check".

Verdict: the chapter is accurate about the backend and about nginx as it exists, and its recommendation to keep tiles app-served is right for reasons that are stronger than the chapter says. The gap is the browser side: the chapter says almost nothing about cookies, CSRF, cache headers, the single-origin rule or the SPA fallback, and the two optional edge sections (40.9 and 40.18) recommend CloudFront for the static files without saying how the browser then reaches `/api`. No blockers. Six majors, twelve minors.

## What is right (checked)

- `frontend/nginx.conf` is as described: listens on 8080 (non-root `nginx-unprivileged` base image), `^~ /api/` proxy with `X-Forwarded-For` overwritten, `proxy_read_timeout 300s`, `proxy_request_buffering off`, an exact-match `location = /actuator/health`, hashed assets `expires 1y` with `public, immutable`, and `location /` with `no-cache` and the security headers. Listing 40.1 is faithful.
- `SESSION_COOKIE_SECURE`, `FORWARD_HEADERS_STRATEGY: native` and the default `TRUSTED_PROXY_REGEX` are as quoted (Listings 40.2 and 40.3).
- No WebSocket, `EventSource` or `Upgrade` handling exists in `frontend/src` or `nginx.conf` (searched); the app is plain request/response HTTP (`fetch` for tiles, `HttpClient` XHR for everything else). The chapter's ALB and CloudFront advice needs no WebSocket support.
- Recommendation in 40.9 to keep tiles app-served: correct, and `TileController` already says why in a comment (see M4).

## Majors

### M1. CloudFront "in front of the static files only" breaks the same-origin design unless `/api` goes through it too (40.9, 40.18, Table 40.1 is silent)

Evidence: the frontend is written for one origin. `frontend/src/app/core/config.ts` sets `API_BASE_URL = ''` and its comment says same-origin is what lets the `HttpOnly` session cookie and the CSRF cookie work "without CORS"; `nginx.conf` opens with "so browser, session cookie and CSRF cookie all share one origin"; the CSP has `connect-src 'self'`; the session cookie is `SameSite=Strict`. Section 40.9 ends with "Consider CloudFront in front of the *static frontend files* only" and 40.18 calls CloudFront suited to the hashed bundles, but neither says what URL the browser then uses for `/api`. There are only three consistent designs, and they have very different costs:

1. **Nginx keeps serving the static files** (the chapter's main design). Same origin, no change to the browser. Simplest.
2. **One CloudFront distribution for everything**: a default behavior for the static origin and an `/api/*` behavior whose origin is the ALB. Same origin is preserved, but CloudFront is now in front of the API, which brings M3, M4, M6 and m5 below.
3. **Static files on their own hostname** (S3/CloudFront) and the API on another. This is cross-origin: it needs CORS with credentials, the cookies must be given a `Domain` (today they are host-only, see M5), `SameSite=Strict` still works only within one registrable domain, the custom `X-XSRF-TOKEN` header makes every write a preflighted request, and the CSP `connect-src`/`script-src` must name the other host. This undoes the design the project chose.

Suggested wording (new paragraph at the end of 40.18, and a one-line pointer in 40.9):

> **CloudFront and the one-origin rule.** The Angular app calls `/api/...` with relative URLs and relies on the browser treating the page and the API as one origin: the session cookie is host-only and `SameSite=Strict`, the CSRF token travels in a cookie the script reads and a header it sets, and the Content-Security-Policy allows connections only to `'self'` (Chapter 22). Putting only the static files behind CloudFront on their own hostname would break all three. If you use CloudFront, put the page and the API behind the same hostname, as two behaviors of one distribution (`/api/*` to the load balancer, everything else to the static origin), and read the consequences in Table 40.3. If that list is too long for the benefit, keep serving the static files from nginx in the task, which is what the rest of this chapter assumes.

Add a small table (Table 40.3) listing, for a single distribution with an `/api/*` behavior: response timeout (M3), error pages (M2), cache policy (M4), client address (M6), headers policy (m2), WAF (M7).

### M2. SPA fallback on S3 + CloudFront can rewrite the API's real error codes, which the app depends on (40.18)

Evidence: today nginx returns the `index.html` fallback only for paths that are not `/api/`, not `/actuator/health` and not a static asset extension: `try_files $uri $uri/ /index.html` in `location /`, while `location ~* \.(?:js|css|woff2?|ico)$` uses `try_files $uri =404`, so a missing hashed bundle is a real 404. With an S3 origin, the usual SPA fix is a CloudFront custom error response (403 or 404 from S3 turned into `/index.html` with status 200). AWS's custom-error-response procedure configures these on the distribution (the console "Error Pages" tab; the `CustomErrorResponse` type is a property of the distribution), not on a cache behavior; the pages I read do not spell out whether it also applies to responses from a second origin such as the ALB, so test it. If it does, it is dangerous here, because the frontend reads the real status codes of API responses:

- `session.interceptor.ts` reacts to 401 and to `403` with `passwordChangeRequired` in the body;
- the viewer treats a tile `404` as "access lost" (`accessRevoked`), `410` as "replaced", `429`/`503` as throttle, `401` as expired token;
- `login.component.ts` turns `401` and `429` into messages.

An `index.html` with status 200 in place of any of these would make `HttpClient` try to parse HTML as JSON and the viewer show wrong states.

Suggested wording:

> **Deep links on S3.** nginx answers unknown addresses with `index.html` so that a bookmarked `/viewer/<id>?page=3` loads the app and the router takes over (Chapter 23). S3 has no such rule. The usual CloudFront answer, a custom error response that turns a 403 or 404 into `index.html` with status 200, is configured on the distribution, not on one behavior; the frontend needs the API's real 401, 403, 404, 410 and 429 responses, so verify that the rule does not touch `/api/*`, or, safer, attach a small CloudFront Function to the static behavior that rewrites only extension-less paths to `/index.html` and leaves everything else, including a missing `.js` file, as a real 404.

### M3. A 3-minute upload render cannot pass through CloudFront's default response timeout (40.5 vs 40.18)

Evidence: `application.yml` has `render-timeout: 3m`, and `nginx.conf` sets `proxy_read_timeout 300s` "rendering a long PDF can take a while". Section 40.5 correctly raises the ALB idle timeout for this. The CloudFront origin settings page says the origin response timeout defaults to 30 seconds (and a timeout increase is a quota request); for `POST` and `PUT` CloudFront drops the connection and does not retry. The upload request sends no bytes while rendering, so a CloudFront in front of `/api/*` would end it at 30 seconds by default, unless the quota is raised and the distribution's origin is set accordingly. The browser then shows "Upload failed." (`upload.component.ts` line 82: `err.error?.error ?? 'Upload failed.'`) although the server may still finish the render, and a reader who retries creates a duplicate document.

Suggested wording (in 40.5 after the idle-timeout paragraph, and as a row in Table 40.3):

> If CloudFront also sits in front of the API (Section 40.18), it has its own origin response timeout, 30 seconds by default, and a longer value needs a quota increase; the browser would see a failed upload while the render continues on the server. Either request the increase and set the distribution's origin timeout above `render-timeout`, or do not route uploads through CloudFront.

### M4. Tile responses are `no-store` on purpose; the chapter never says so, and any CDN in front of `/api` must not cache them (40.9)

Evidence: `TileController` (line 194) returns `CacheControl.noStore()` with the comment "Deliberately not cacheable beyond a moment: a shared cache holding onto a watermarked-for-someone-else tile would leak it." Tile addresses are `/api/tiles?token=...` (the token is in the query string, expiry `url-ttl-seconds: 120`, `ViewerProperties.urlTtlSeconds`), and the viewer fetches them with `fetch(url, { cache: 'no-store' })`, so browsers do not keep them either. Two dangers on AWS: (a) a CDN whose cache key ignores the query string, cookies or authorization would serve one reader's watermarked tile to another; (b) even a correct cache key wastes the design, since each tile is unique to a viewer and a session and expires in two minutes. Section 40.9 discusses signed URLs versus app tokens but not this.

Suggested wording (add to 40.9 after the first paragraph):

> **Never cache tiles at a shared edge.** The app already sends `Cache-Control: no-store` on every tile, with a code comment explaining that a shared cache holding a tile stamped for one reader could hand it to another. Each tile URL is also unique to one session and expires after 120 seconds, so a shared cache would gain nothing. If you put CloudFront in front of `/api/*`, attach a cache policy that disables caching for that behavior (CloudFront has a managed policy for this) instead of relying on the origin header alone, and forward the session cookie and the query string.

Related minor: the token is in the URL, so it appears in ALB access logs, CloudFront logs and WAF logs. It is valid for two minutes and bound to a session, so the risk is small, but say it in 40.14 and set log retention deliberately.

### M5. The chapter has no section on cookies, CSRF or CORS, though this is where a new domain, ALB or CDN bites first (40.5, 40.6, 40.19)

Evidence, all at `book-m6-final`:

- `application.yml`: session cookie `SDV_SESSION`, `http-only: true`, `same-site: strict`, `secure: ${SESSION_COOKIE_SECURE:false}` (default **false**), no `Domain` attribute (host-only).
- `SecurityConfig.csrfTokenRepository`: `CookieCsrfTokenRepository.withHttpOnlyFalse()` with `sameSite("Strict")` and `path("/")`; the code sets no `Secure` flag on it, so Spring decides from the request (`request.isSecure()`, per Spring Security's `CookieCsrfTokenRepository`; I did not run it). Behind a TLS-terminating ALB, `isSecure()` is true only if the forwarded protocol is trusted, which needs `FORWARD_HEADERS_STRATEGY=native`, a matching `TRUSTED_PROXY_REGEX`, and nginx's `map $realip_remote_addr $forwarded_proto`, which today trusts `X-Forwarded-Proto` only from 172.28.0.11 and otherwise sets `$scheme`, that is `http`. Chapter 40.6 says to add the ALB subnets to that map, which is right; it should say that the visible symptom of getting it wrong is cookies without `Secure`.
- The frontend copies `XSRF-TOKEN` into `X-XSRF-TOKEN` on same-origin writes (Angular's default, see `app.config.ts`).

Consequences for AWS: a new hostname means every reader signs in again and the cookies do not carry over; the cookies are host-only, so a separate `api.` hostname would not receive them (M1); CORS is unnecessary as long as the page and API share a hostname; and the two cookie flags must be verified in the browser after deployment.

Suggested wording (new subsection, for example 40.6a "What the browser holds: the two cookies"):

> The browser holds two cookies from this app. `SDV_SESSION` is `HttpOnly` (script cannot read it), `SameSite=Strict`, host-only (no `Domain`), and `Secure` only when `SESSION_COOKIE_SECURE=true`, which is `false` by default. `XSRF-TOKEN` is readable by script on purpose (Angular copies it into the `X-XSRF-TOKEN` header of each write), `SameSite=Strict`, path `/`; the code does not set its `Secure` flag, so Spring derives it from whether the request looks secure. Behind an ALB that terminates TLS, the request looks secure only if the forwarded protocol is trusted (Section 40.6). After deployment, open the browser's developer tools, Application tab, and check that both cookies show `Secure`. Because the cookies are host-only, keep one hostname for the page and the API: this avoids CORS entirely, and moving to a new hostname signs every reader out once.

Also add "cookies missing `Secure` after the move" to Common mistakes.

### M6. If CloudFront (or any second proxy) sits before the ALB, the trust chain of 40.6 picks the wrong address (40.6, 40.18)

Evidence: 40.6 designs nginx to take the *last* address in `X-Forwarded-For` (`real_ip_recursive off`), which is right when the ALB is the only hop. With CloudFront in front of the ALB the last address is CloudFront's edge, not the reader, so `LoginThrottle` and `TileRateLimiter` keys and the audit log would see edge addresses, and many readers behind one edge would share a lockout. CloudFront offers the viewer address in a `CloudFront-Viewer-Address` header when configured through an origin request policy (AWS "Add CloudFront request headers", read for this review; the value is `address:port`). I did not verify how CloudFront edits `X-Forwarded-For`, nor the name of the managed prefix list of CloudFront's origin-facing addresses (one exists; check it before recommending it).

Suggested wording (add to the end of 40.6):

> **With CloudFront in front of the ALB, the chain has one more link,** and "take the last address" would return the edge's address. You would have to trust the CloudFront-to-ALB hop explicitly, take the viewer address from the header CloudFront supplies for it (`CloudFront-Viewer-Address`, through an origin request policy), and allow the ALB to accept connections only from CloudFront (check AWS's managed prefix list for CloudFront's origin-facing addresses), or anyone could send that header directly. Repeat the forged-header test end to end.

### M7. A WAF rate rule would break the viewer's rate-limit handling unless it answers with 429 and `Retry-After` (40.18)

Evidence: the viewer distinguishes `429`/`503` (waits `Retry-After`, shows a countdown, resumes) from every other failure (marks the tile failed, shows "Some parts of this page could not be loaded", offers Retry). AWS WAF's default Block response is 403 (AWS WAF developer guide, "Sending custom responses for Block actions"); the same page says a Block action can return a custom status code and custom response headers (any header name except `content-type`); I did not check which status codes are supported, so verify that 429 is. Also, one page turn fetches about a dozen tiles with up to six requests in flight (`MAX_CONCURRENT_TILE_FETCHES = 6`), so an address-based rate rule is easy to trip, and a rule keyed on address hits everyone behind one office or NAT address (the chapter mentions the office case but not what the reader then sees).

Suggested wording (add to 40.18 after "one office address shared by many people can trip an address rule"):

> What the reader sees matters too. The viewer waits and retries only on `429` and `503`; a WAF block answers `403` by default, and the viewer would show failed tiles and a Retry button instead of a countdown. If you add a rate-based rule, configure its Block action with a custom `429` response and a `Retry-After` header (check that WAF supports that status code), and set the limit well above what one reader's page turns produce.

## Minors

### m1. HSTS: "add `Strict-Transport-Security` in `nginx.conf`" needs the per-location caveat (40.5)

Today HSTS comes from Caddy (`deploy/Caddyfile`, `HSTS_POLICY`, default `max-age=31536000`), not nginx. The header comment of `nginx.conf` says security headers are set per location because `add_header` does not merge across levels. A server-level HSTS would silently vanish in any location that has its own `add_header` (both the static-asset regex location and `location /` do). Suggested wording: "Add the header inside each location that sets its own headers, not once at server level, because nginx does not inherit `add_header` into a location that defines its own (see the comment at the top of `nginx.conf`). API responses come from the app; check the app's own headers before adding a second copy."

### m2. A table of "which header is set where, today and on AWS" would answer the editor's question in one place

Today: `location /` (HTML only) sets CSP, `X-Content-Type-Options`, `X-Frame-Options: DENY`, `Referrer-Policy: no-referrer`, `Permissions-Policy`; static assets get only `Cache-Control: public, immutable` and `nosniff`; `/api/` responses carry the backend's own headers (nginx adds none, by design, to avoid duplicates). On AWS: with nginx kept, nothing moves; with CloudFront, a response headers policy can add HSTS, CSP, `X-Frame-Options` and others, is attached per cache behavior, and can either keep the origin's header or overwrite it (CloudFront developer guide, "Add or remove HTTP headers in CloudFront responses with a policy"); attach one to the static behavior only and leave `/api/*` alone, so the app's stricter API headers stay authoritative. Note `frame-ancestors` is honored only from a header, not from a `<meta>` tag, so it must be in whichever layer sends the header.

### m3. CSP consequence of moving tiles off the app

`img-src 'self' blob: data:` and `connect-src 'self'` mean that tile bytes fetched from another origin (an S3 or CloudFront address) would be blocked by the browser unless the CSP is loosened. This is one more reason for 40.9's recommendation; add one sentence there.

### m4. Cache headers for static files on S3

nginx today decides caching by rule: `Cache-Control: no-cache` for pages (`location /`), `public, immutable` for hashed bundles, `expires 1y`; Angular's `outputHashing: "all"` (`angular.json`) makes bundle names change with content. On S3 there is no server rule: each object needs its `Cache-Control` set at upload (`index.html` short or revalidated, hashed files long and immutable), and a CloudFront cache policy must respect it. If `index.html` is cached at the edge after a deploy, readers keep loading the old bundle names. Suggested sentence for 40.18: "Set `Cache-Control` explicitly when uploading: `no-cache` on `index.html`, one year and `immutable` on the hashed files, as `nginx.conf` does today, and invalidate at most `index.html`."

### m5. Rolling deploys and the lazy-loaded routes: "users no longer notice a rolling deploy" (40.16) is too strong

The SPA is baked into each task's nginx image. `app.routes.ts` loads every screen with `loadComponent: () => import(...)`, so the browser fetches hashed chunk files on demand; `nginx.conf` returns a real 404 for a missing `.js` (`try_files $uri =404`); the frontend has no handler for a failed route load (searched: no `NavigationError` or router error handler in `frontend/src`). During a rolling update the ALB spreads requests over old and new tasks, so a browser can receive `index.html` from a new task and then ask an old task for `chunk-NEW.js`, which the old image doesn't have (or the reverse when old tabs ask a new task for an old chunk). The symptom is a screen that fails to open until the reader reloads. The statement in 40.16 that "with shared sessions, users no longer notice a rolling deploy" is true for sessions, not for the static files.

Suggested wording (replace the last sentence of 40.16 and add a Common mistake): "Shared sessions mean a rolling deploy does not sign anyone out. Static files are a separate matter: the browser may load `index.html` from one version and a lazy-loaded chunk from another while old and new tasks run side by side. Serve the static files from a location that keeps the previous version's hashed files for a while (S3 with additive uploads, behind the same hostname), or accept that a reader who hits it must reload, and tell support."

Related, short: a task being replaced can return 502 or 504 to a request in flight; the interceptor handles only 401/403, the document list shows "Could not load documents.", and the viewer retries only on 429/503. Mention connection draining (the load balancer's deregistration delay; check its default) and the health check grace period of 40.16.

### m6. Session timeout and the idle banner: one sentence linking the two clocks (40.10)

`AuthController` reports `sessionTimeoutSeconds` from `getMaxInactiveInterval()`, and the frontend's `idleState` warns in the last five minutes of that. With Spring Session and ElastiCache the max inactive interval must stay 30 minutes (`server.servlet.session.timeout: 30m`), or the banner would warn at the wrong time; the absolute limit is `session-max-lifetime: 12h` in `SessionLifetimeFilter`, which the chapter already says needs no change. Also state that the ALB's 60-second idle timeout is a connection timer, unrelated to the session's 30-minute idle timer; beginners confuse them. If a cache failover drops sessions, readers get a 401 on their next call, and the interceptor sends them to sign-in with `returnUrl` set to the page they were on (Chapter 22); nothing else is lost.

### m7. The frontend code itself does not change; say so (40.4)

Add to "What stays the same": every Angular call is a relative `/api/...` URL (`API_BASE_URL = ''`); nothing under `frontend/src/app` needs to change for ECS behind an ALB, only `nginx.conf` (trusted addresses, listen port, upstream address, HSTS), the `Dockerfile` (`EXPOSE 8080` is cosmetic but should match) and the pipeline. That is a reassuring, testable statement.

### m8. Two ways to avoid the port clash in 40.6

The chapter moves nginx to 8081 in the shared task and changes `proxy_pass` to loopback. The alternative, keeping nginx on 8080 as today and moving the Spring app to another port with `SERVER_PORT` (Spring's relaxed binding of `server.port`), leaves the frontend image and the ALB target unchanged. I did not run it, so present it as an option to check. Also `docker-compose.yml`'s `web` health check calls `http://127.0.0.1:8080/`; keep it consistent with whichever you choose.

### m9. `<base href="/">` means the app must live at the root of a hostname

`src/index.html` has `<base href="/">` and the router uses root-relative paths. Serving under a path prefix (for example a CloudFront path pattern such as `/app/*`) would need a different build base. One sentence in 40.18.

### m10. WebSocket-free assumption: state it (40.5)

Add: "The app uses no WebSockets or server-sent events: tiles are ordinary `fetch` requests, the admin sessions list is polled every five seconds while the page is visible, and uploads are ordinary `POST` requests with progress events. The load balancer needs no special settings for long-lived connections; only the render wait discussed above matters." (Verified by search of `frontend/src` and `nginx.conf`.)

### m11. Upload progress and the "Step 2" wait

`upload.component.ts` shows "Step 1 of 2: uploading" and, once every byte is sent, "Step 2 of 2: preparing pages" with an indeterminate `<progress>`; that second wait is exactly the 3-minute render that 40.5 protects from the ALB idle timeout. A sentence connecting the two ("this is the wait the browser shows as Step 2") helps a reader see why the timeout matters to a person, not only to a server.

### m12. Logs contain tile tokens (see M4) and the `X-Forwarded-For` lists

If ALB access logs are enabled (40.14), set retention and access controls; they contain full request URLs, including `/api/tiles?token=...` and client addresses, which are personal data under many rules. One sentence.

## Suggested additions to Common mistakes (40.x)

- **Serving the page and the API from different hostnames.** Symptom: sign-in appears to work but every later call is a 401, or writes fail with 403. Fix: one hostname (M1, M5).
- **Cookies without `Secure` after the move.** Symptom: `SDV_SESSION` or `XSRF-TOKEN` shown without the Secure flag. Fix: `SESSION_COOKIE_SECURE=true` and a trusted forwarded protocol (M5).
- **A CDN error page that turns API errors into `index.html`.** Symptom: sign-in errors and "no longer available" screens replaced by odd failures. Fix: scope the rewrite to the static behavior (M2).
- **Caching `/api/tiles`.** Symptom: readers see other readers' watermarks. Fix: no shared caching of `/api/*` (M4).
- **Rolling deploy with the SPA baked into the task image.** Symptom: a screen fails to open after a deploy until reload. Fix: keep old hashed files available or accept and document (m5).

## Priority for the editor

1. M1 (same origin) and M5 (cookies): the chapter's audience will otherwise design a broken split.
2. M2 (fallback rewrite), M3 (30 s timeout), M4 (`no-store` on tiles): each is a concrete failure a reader could cause with the chapter's own optional edge advice.
3. M6, M7, m5 (deploy skew).
4. Everything else: single sentences.

None of these needs a change to the code at `book-m6-final`; all are additions or corrections to the chapter text.
