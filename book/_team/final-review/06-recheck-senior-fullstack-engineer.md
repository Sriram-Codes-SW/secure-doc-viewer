# Recheck 06: Senior Full-Stack Engineer

Sources rechecked on 2026-09-20 after the fix batch; PDF rechecked against the rebuilt `book/build/out/secure-doc-viewer-guide.pdf` (750 pages, written 14:45). No chapter edited, nothing committed, no secret files opened.

## Result

| Original finding | Status |
|---|---|
| FSE-01 (PDF, tables cut off) | Fixed |
| FSE-02 Exercise 29.4 | Fixed |
| FSE-03 Example 12.2 curl | Fixed (and verified to work) |
| FSE-04 older tags need JDK 21 and Maven | Partly fixed |
| FSE-05 duplicate "Table 1" labels | Fixed |
| FSE-06 captions below tables | Fixed |
| FSE-07 `npm test` watch mode | Fixed |
| FSE-08 Exercise 25.3 last character | Fixed |
| FSE-09 `@Transactional` rollback | Fixed |
| FSE-10 password on the command line in Ch 34 | Fixed |
| FSE-11 Ch 2 "two levels" | Fixed |
| FSE-12 Ch 4 stale canvas note | Fixed |
| FSE-13 Ch 35 how to see the metrics page | Fixed |
| P7-01 keep-alive | Fixed |
| P7-02 stop timeout | Fixed (and my own premise was partly wrong; see below) |
| P7-03 ListBucket / 403 | Fixed |
| P7-04 `discard` missing from the storage seam | Fixed |
| P7-05 Lua sketch | Fixed |
| P7-06 Terraform delete markers | Fixed |
| P7-07 two `proxy_pass` lines and regex example | Fixed |
| P7-08 duplicated sentence | Fixed |
| P7-09 Figure 40.1 | Fixed |
| P7-10 Exercise 40.1 | Fixed |
| P7-11 CloudFront timeout, WAF name | Fixed (values not verifiable by me, sources cited) |
| P7-12 Spring Session property name | Fixed |

Open after this recheck: 0 Blocker, 0 Major, 3 Minor (R-01 to R-03 below).

## Evidence per finding

- **FSE-01 Fixed.** Text scan of all 750 pages: 0 spans beyond the page edge (was 89 pages); 1 span within 25 pt of the edge (page 166, a code line, still inside the page). Page images: PDF 176 (Table 12.3) now wraps all three columns fully; PDF 268 (Table 18.2) wraps the long file paths; PDF 92 (Section 6.10) breaks `<java.version>25</java.version>` across two lines inside the margin. Page 175 shows Example 12.2 printed cleanly.
- **FSE-02 Fixed.** Exercise 29.4 now says "a two-line label (the viewer name on one line and the time and trace code on the second)". Correct: `WatermarkService` builds `String[] lines = {viewerLabel, stamp [+ trace]}` at both m4 and m6, and Appendix C's answer (`stepY = 20*2 + 20 = 60`) matches.
- **FSE-03 Fixed, and it works against the code.** Example 12.2 is now a three-step sequence (get the `XSRF-TOKEN` cookie, sign in with the header, upload with a re-read token). It matches the project README's API section. It is valid at `book-m6-final`: `SpaCsrfTokenRequestHandler` calls `csrfToken.get()` on every request, so even the `401` from `/api/auth/me` sets the cookie; `AuthController.rotateCsrfToken` replaces the token at sign-in, so the "read the token again" comment is right; the `xsrf` awk helper reads cookie-jar column 6/7 correctly (the `HttpOnly` session lines are prefixed `#HttpOnly_` and do not interfere). The text also warns that an account still on a temporary password gets `403 passwordChangeRequired`. The example uses the placeholder `<password>` only.
- **FSE-04 Partly fixed.** The setup chapter now says that `book-m0-mvp` to `book-m4-reading` need JDK 21 and a self-installed Maven 3.9, states that no wrapper exists before m5, and offers a reading alternative (`git show <tag>:<path>`); Table IV.3 is referenced. It still does not say how to install JDK 21 or Maven or how to select the JDK, and the exercises still say "on your own copy". Acceptable as an optional path; see R-03.
- **FSE-05 Fixed.** Part introductions now use I.1, II.1, III.1, IV.1 to IV.3, V.1, VI.1, VII.1 and Figure II.1; no `Table N —` or `Figure N —` labels with a bare number remain in the sources; the built PDF has zero "Table 1" captions.
- **FSE-06 Fixed.** Zero italic-below `*Table N.M —` captions remain in `part-*`, `front-matter`, `appendices`, `tradeoffs` (the old count of 23 in the review scratch copy is a stale build artifact).
- **FSE-07 Fixed.** Section 24.2 now explains watch mode and quotes the builder schema ("Defaults to `true` in TTY environments and `false` otherwise"); Example steps use `npx ng test --watch=false`; Chapter 20's script comment points to it. PDF text search finds `--watch=false` on several pages.
- **FSE-08 Fixed.** Exercise 25.3 and its solution both say to change a middle character and explain the unused-bits reason.
- **FSE-09 Fixed and correct.** Section 14.6 now says rollback on unchecked exceptions only, checked exceptions do not roll back unless `rollbackFor`, and "The project never sets `rollbackFor`". Verified: `git grep rollbackFor` at the tag returns nothing; every application exception (`BadRequestException`, `ForbiddenException`, `TileGoneException`, and the rest) extends `RuntimeException`, and `replaceFile` wraps `IOException` in `UncheckedIOException` inside the transaction.
- **FSE-10 Fixed, commands work.** Ch 34 Listings 34.1 and 34.2 now use `sh -c 'export MYSQL_PWD="$MYSQL_ROOT_PASSWORD"; exec mysqldump ...'` and the matching `mysql` restore; single quotes make the variable expand inside the container, where `MYSQL_ROOT_PASSWORD` exists, so the commands work. Captions honestly say they are adapted from the README (which uses `-p`), and the notes explain the difference and the residual weakness of `MYSQL_PWD` (option file as the stricter alternative). The Ch 9 loop (`MYSQL_PWD="$MYSQL_PASSWORD" mysql -u "$MYSQL_USER" "$MYSQL_DATABASE"`) and the Ch 10 health check (`MYSQL_PWD="$$MYSQL_ROOT_PASSWORD" mysqladmin ping ...`, matching the repo's compose file) are also correct. Note for the listing checker: Listings 34.1/34.2 now show as "BAD" in `listingcheck.py` only because they deliberately differ from the README and say so in their captions.
- **FSE-11 Fixed.** "three levels at once".
- **FSE-12 Fixed.** Ch 4 now says the comment "is stale at `book-m6-final`" and that PR #13 on `main` corrected it. Verified earlier that `main` no longer has the canvas wording in `PageInfo.java` or the README.
- **FSE-13 Fixed and correct.** Ch 35 gives two ways to see the page: the backend from source at port 8080, or `docker compose exec app curl -s http://localhost:8080/actuator/prometheus` (the app image installs `curl` for its health check, and the default `metrics-allowed-addresses` allows loopback), and notes nginx forwards only the bare health path. All three claims verified against the repo.
- **P7-01 Fixed.** Section 40.5 now states the second rule (server keep-alive must exceed the ALB idle timeout), notes nginx's 75 s default and that `nginx.conf` sets none, recommends 330 s for a 300 s ALB timeout; Example 40.1 has `keepalive_timeout 330s`; Table 40.3, section 40.4, the "In this project" table, Common mistakes and the summary all mention it.
- **P7-02 Fixed; correction to my own review.** Section 41.6 now says that on Fargate `stopTimeout` is 30 s by default and at most 120 s, so a three-minute render cannot be guaranteed to finish during a deploy, and offers three answers (keep render plus queue wait under 120 s, accept the cut and let the janitor clean staging, or make uploads an asynchronous durable job). The chapter also now says Spring Boot 4.1.1 shuts down gracefully by default. I had said in the first review that "Spring Boot's default is to stop at once"; that was wrong for Boot 4. I checked the local Maven cache: `spring-boot-web-server-4.1.1-sources.jar`, `ServerProperties`: `private Shutdown shutdown = Shutdown.GRACEFUL;`. So the new text is right and my note was out of date. The 120 s figure is still from the docs as cited, not something I could test.
- **P7-03 Fixed.** Example 41.2 drops the `s3:prefix` condition; the text says `s3:ListBucket` "is not optional" (403 instead of 404 without it), notes that a prefix condition must be tested against a missing tile, and section 40.8 adds "A missing tile must still be a 404" with the 403 explanation; Solution 41.3 now says required, not optional, and adds KMS permissions for SSE-KMS.
- **P7-04 Fixed.** The storage seam now includes `discard(RenderedDocument)`, and the text explains that S3 `commit` and `discard` copy and clean staging.
- **P7-05 Fixed.** Example 40.3 now reads `TIME` inside the script, takes `window_ms, limit, unique_id` as arguments, returns `{0, oldest score}` or `{1, id}`; Table 40.4 and Solution 40.5 agree with it. The Lua is valid.
- **P7-06 Fixed.** Example 41.4 has a second rule with `expiration { expired_object_delete_marker = true }` and a note that such a rule cannot combine with `days` or a tag filter; the Terraform is valid (the two rules use separate `filter { prefix }` blocks). It also adds `deletion_protection` and final snapshot advice.
- **P7-07 Fixed.** The text now says "both `proxy_pass` lines", the `In this project` row says "both `proxy_pass` lines to loopback", and Example 40.1 shows the regex key `~^10\.0\.(1|2)\.` for two ALB subnets, both `set_real_ip_from` lines and both `proxy_pass` locations.
- **P7-08 Fixed.** The sentence occurs once.
- **P7-09 Fixed.** Figure 40.1 now nests the ALB (public subnets) inside the VPC and the tasks, database, cache and S3 gateway endpoint inside a "Private subnets" box; the text description matches; Figure 41.1 covers the network layout and endpoints.
- **P7-10 Fixed.** Exercise 40.1 asks "which of the four"; the solution answers "all but Caddy" with the single-Fargate-task condition and the two-move fallback; the hint points to Figure 41.5, which exists (the migration-order figure).
- **P7-11 Fixed.** Table 41.1 now says CloudFront's origin response timeout is 30 s by default and 1 to 120 s per origin, more only by quota request; the WAF "protection pack" statement now carries a source. I cannot check either against the live AWS docs.
- **P7-12 Fixed.** The Redis keyspace section recommends the `ConfigureRedisAction.NO_OP` bean "which doesn't depend on a property name".

## New material rechecked

- **Ch 24 end-to-end steps.** Steps 1 to 4 are correct against the repo: `playwright.config.ts` default `baseURL` `http://localhost:8081`, script `"e2e": "playwright test"`, the specs read `E2E_ADMIN_USER` (default `admin`) and `E2E_ADMIN_PASSWORD` and skip without it; `npx playwright install chromium`, and CI uses `--with-deps`; the three shell variants (bash, PowerShell, cmd) are right. `npm test` and `ng test --watch=false` as described. One defect, R-01.
- **Part VII sketches now called "Examples"** (40.1 nginx, 40.2 storage seam, 40.3 Lua, 41.1 to 41.4): consistent with STYLE (never tagged as project code) and every caption says "illustrative" or "not in the repository". Listings 40.1 to 40.4 and the other real listings still verify against the repo (listing checker: 289 listings verified; the 13 flagged are placeholders, diffs or captioned adaptations).
- **Nginx and Compose changes** described in Ch 40 would work: the map with `~` regex and `$realip_remote_addr`, `set_real_ip_from` with CIDR, `real_ip_recursive off`, loopback trust (`127.0.0.1` in the app's default proxy list plus `FORWARD_HEADERS_STRATEGY=native`), both `proxy_pass` lines, and the port move (`listen`, `EXPOSE`, health check).

## Open items

### R-01 The e2e Step 2 tells readers to use a generated admin password, which the suite cannot use
- **Severity:** Minor
- **Where:** Ch 24, "Running the end-to-end tests yourself", Step 2.
- **Quote:** "or, if you left that empty, a random one printed once in the app's startup log (`docker compose logs app`)".
- **Problem:** `BootstrapAdmin` creates the admin with `mustChangePassword` set when the password is generated (its own comment: "A generated password was printed to a log: it must be replaced at first sign-in"). The spec's `signIn(browser, ADMIN_USER, ADMIN_PASSWORD)` expects to land on `/documents`; a forced change sends the admin to `/account`, so the test fails. The step's sentence "If you have changed it since, use the password you set" hints at the fix but does not say it is required.
- **Fix:** "If the password was generated, sign in once in the browser and change it first; or set `BOOTSTRAP_ADMIN_PASSWORD` in `.env` before the first start (as CI does)".

### R-02 Exercise 34 still displays the `-p"$MYSQL_ROOT_PASSWORD"` form
- **Severity:** Minor
- **Where:** Ch 34 Try it, the exercise that asks the reader to explain `mysqldump ... -p"$MYSQL_ROOT_PASSWORD" ...` (about line 302), with the solution in Appendix C.
- **Problem:** it is the README's form and is labeled as such nearby, but the chapter's own listing uses `MYSQL_PWD`, so the exercise asks about a command the chapter told the reader to avoid. Not wrong, only inconsistent.
- **Fix:** either say "the README's form" in the exercise or switch it to the Listing 34.1 command.

### R-03 Older-tag setup remains a pointer, not a procedure
- **Severity:** Minor
- **Where:** Setup chapter note and Table IV.3 (the remainder of FSE-04).
- **Problem:** no per-OS steps for installing JDK 21 next to JDK 25 or Maven; Boot 3.3.4 on JDK 25 stays unverified. The optional status and the `git show` alternative make this acceptable.
- **Fix (optional):** add two lines for `JAVA_HOME` selection and a Maven install per OS, or mark the six "run it on your own copy" exercises as optional.

## Not rechecked

- Full re-read of both Part VII chapters (I checked the changed passages and everything my findings named, plus the sections I sampled for the new material).
- Other reviewers' findings, and non-technical fixes (long sentences, positional references, heading levels).
- AWS documentation values (Fargate `stopTimeout` maximum, CloudFront origin timeout range, WAF console naming): the chapter now cites sources for them, but I could not open the pages.
