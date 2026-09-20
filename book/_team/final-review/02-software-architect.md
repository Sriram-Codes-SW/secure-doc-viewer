# Final review: Software Architect

Reviewer: Software Architect lens (blueprints, Parts IV to VI, Chapter 37 trade-offs, architecture and security claims). Branch `book/draft`. Code read with `git show <tag>:<path>` and `git grep <tag>`. Nothing edited except this file; nothing committed; no secret read or quoted.

## Verdict

The architecture writing is honest and mostly accurate against the code. Chapter 37 is balanced (every row separates recorded project reasoning from general industry practice), Chapter 39 says where the code only approximates a pattern, the security claims that I traced to code hold, and the AI-reviewer story is told correctly in Chapters 25, 32 and the part openers. The problems are (1) a PDF layout defect that removes content from the printed book (tables run off the page), (2) a few places where the architecture prose is slightly stronger than the code (layering, "signed URL proves you may fetch", the not-defended list), and (3) one story-consistency slip ("the product owner's review" for the AI reviewer). The PDF defect is the only Blocker. It appears only in the PDF, not in the markdown.

| Severity | Count |
|---|---|
| Blocker | 1 |
| Major | 5 |
| Minor | 13 |

PDF pages inspected (PDF page index, not printed number; printed = PDF minus 15 in this region): 449 (Fig 30.1 blueprint v5), 533 (Fig 37.1), 535 (Table 37.1), 557 (Fig 39.2), 561 (Listing 39.2), 566 (Table 39.3, 39.4), 574 (Appendix A glossary). Text of pages 380 to 600 was also scanned by script for words beyond the right margin.

## Blockers

### SA-01 (PDF only) Tables are wider than the page and are cut off, losing content
- **Where:** built PDF, many tables. Confirmed visually: Table 37.1 (Ch 37, PDF p.535: the fourth column "Enterprise alternative" is entirely missing and the third column is cut mid-word, "shared store to scale", "New device locked out during an atta"); Table 39.3 (PDF p.566: the "What it costs" column, the whole point of the worked example, is cut to "Wh", "No", "An"); Table 39.4 (PDF p.566: "One line" text cut); Appendix A glossary (PDF p.574 to 578: every definition is cut at the right edge). A margin scan also flags the tables on PDF pages 443, 468, 477, 490, 498, 500, 506, 510, 519, 535, 537, 540, 551, 566, 567 (Table 32.1, the "In this project" tables, Table 34.1 and others).
- **Quote:** (Table 39.3) "(c) account-wide only for unknown devices | The botnet, without hurting the owner on a usual device | Storage of hashed addresses; ..." prints only up to "usual device"; the cost column is not visible.
- **Problem:** pipe tables with long cells are not wrapped in the LaTeX output (the page text of PDF p.535 contains none of the words "Managed", "Sensitivity" or "Enterprise alternative" from Table 37.1). A reader cannot read the trade-off table's alternatives, the lockout costs, or glossary definitions. The markdown and the HTML/EPUB are not affected (not checked in detail).
- **Suggested fix:** in `book/build/header.tex` / the Pandoc invocation, make tables use `longtable` with `p{}` column widths (or Pandoc `--columns`/relative-width pipe-table dashes so cells wrap), rebuild, and re-scan for words beyond the margin. Confirm Table 37.1 shows all four columns.

## Majors

### SA-02 (PDF only) Blueprint and other wide flowcharts are too small to read
- **Where:** Ch 30, "Architecture blueprint v5" (PDF p.449, Fig 30.1); same diagram in Ch 31 (Fig 31.1, PDF p.462-463); Fig 39.2 (PDF p.557); by extension all `flowchart LR` blueprints v0 to v6.
- **Quote:** *Figure 30.1 — Blueprint v5 (`book-m5-platform`)*
- **Problem:** the diagram is a single 1568 px wide image placed at full text width (about 453 pt); the node labels come out at roughly 4 pt by eye (not measured numerically), unreadable in print. The blueprint is the main deliverable of each Part IV chapter, so it must be legible.
- **Suggested fix:** render blueprints top-to-bottom (`flowchart TB`) or split into two figures; or place them on a landscape page; keep node labels short (move the long lists into the "What changed" text).

### SA-03 The "what is not defended" list is incomplete, and backups are presented without their security cost
- **Where:** Ch 32, Section 32.11 (`part-5-production/32-security-review.md`); Ch 34, Section 34.11 (`34-backups-and-operations.md`).
- **Quote:** 32.11 lists five items (screenshots, patient user, no MFA, right-click blocking, no text layer). 34.11: "...and then copies them off the machine."
- **Problem:** the README's Limitations also state that publishers can discover non-admin usernames through the share picker, that there are no groups, and that sessions and counters are in memory. More importantly the book never says that tiles on the volume, and therefore every backup, are unwatermarked, unencrypted copies of every document (Ch 34.7 says "Backups contain password hashes and document content" only inside the drill, and nothing warns about the scheduled copy-off-machine advice). Anyone with the volume, the backup or a shell in the container has the content, which is outside the "never handed out" promise. Also nothing says that tile tokens travel in the URL query string (so they appear in proxy and access logs for their 120 s life; the session binding limits the harm, but 32.9 mentions only `Referer`).
- **Suggested fix:** add to 32.11: "Operators and anyone with the tile volume or a backup hold unwatermarked copies (no encryption at rest); the share picker lets publishers discover usernames; the token is in the URL and can appear in logs (bound to the session, 120 s)." Add one sentence in 34.11: "treat backups as the documents themselves: restrict access, encrypt them, and delete old ones."

### SA-04 Layering and modularity verification is narrower than the claim; two package cycles and more layer shortcuts exist
- **Where:** Ch 39, Sections 39.5 and 39.6, Fig 39.2 (`part-6-patterns/39-architectural-patterns.md`).
- **Quote:** "Verify the rule that matters most, which is that dependencies point down. No class outside the `controller` package imports a controller"; "accounts don't know about documents; documents know about accounts, audit, and tiles."
- **Problem:** verified at `book-m6-final`: true that no non-controller class imports a controller (only a Javadoc link in `WatermarkService`), true that `account` imports nothing from `document`, and the two controller shortcuts and the 11 `TileController` collaborators are correct. But the check stops at the controller boundary. (a) `service` <-> `document`: `StorageJanitor` (service) imports `document.DocumentRepository`, while `DocumentService` (document) imports `service.TileGenerationService`. (b) `account` <-> `security`: `UserAccountService` uses `security.KnownDevices` (fully qualified name, so an import-line count misses it) while `security` imports `account`. (c) `AuthController` has 13 collaborators (more than `TileController`) including Spring Security internals, `LoginThrottle` and `KnownDevices`, and `UserAdminController` performs session revocation itself (`sessions.revokeAllFor(...)` after a role change), i.e. a business rule in the controller. Fig 39.2 omits the `security` package entirely. 39.6 says the audit package "imports from `security` only once", which is right but hides that the cycle exists.
- **Suggested fix:** in 39.5 add "Below the controllers the layering is looser: `StorageJanitor` (service) reads `DocumentRepository` while `DocumentService` calls the tile service, and `UserAccountService` calls `KnownDevices` in `security` while `security` reads accounts; nothing enforces direction." Add `security` to Fig 39.2 or say the figure leaves it out. Keep the "convention, not machinery" sentence.

### SA-05 "The product owner's review" is used for the AI reviewer, and "the product owner" is also the human
- **Where:** Ch 29, "Decisions and challenges" ("Decision: a lighter watermark"); Ch 30, "Incident: audit times in the future" (`part-4-building-the-app/29-m4-reading.md` line ~697, `30-m5-platform.md` line ~1081).
- **Quote:** "The product owner's review found the mark too dense" and "The product owner's re-review (`PO2-5`)".
- **Problem:** everywhere else the book reserves "the product owner" for the human who made decisions and calls the AI role "Product Owner reviewer / review agent" (Part IV intro, Ch 32.4, 37.8). In these two decision blocks a reader takes the AI finding for the human's own review. Ch 29 line 29 does clarify once ("an AI agent playing the product owner") but the decision blocks are read in isolation.
- **Suggested fix:** "The Product Owner review agent's review found..." and "The Product Owner review agent's re-review (`PO2-5`)". Do the same wherever a `PO-n` or `PO2-n` id is credited to "the product owner" (also check Ch 28 line 40 "The product owner's review added a user-facing complaint").

### SA-06 "A signed URL proves you may fetch this one tile" overstates what the token does
- **Where:** Ch 39, Section 39.9, Table 39.1 and Fig 39.4 caption.
- **Quote:** "*Figure 39.4 — The hybrid: a session proves who you are, a signed URL proves you may fetch this one tile*"; "possessing the URL is what grants the right to fetch that one thing".
- **Problem:** at `book-m6-final` `TileController.getTile` re-checks access on every request (`documents.tileAccessIfViewable(...)`), plus session binding, rate limit and version. The HMAC token therefore does not grant the right: it scopes and time-limits a request that must also carry the session cookie and still pass the live access check. Chapter 32.13 says this correctly ("Every check is independent"); 39.9 contradicts it by calling the URL a capability URL. A reader designing a CDN variant (37.4) could conclude the token alone authorizes.
- **Suggested fix:** caption: "a session proves who you are; the signed URL pins the request to one tile, one render and one session for 120 seconds; the server still re-checks access on every tile". In the text: "It is capability-shaped (unforgeable, expiring, scoped) but not a capability: the server never treats it as sufficient."

## Minors

### SA-07 "About six times faster" contradicts the 4.4x implied elsewhere
- **Where:** Ch 30, Decision "512-pixel tiles and 180 requests a minute" (line ~1075) vs Ch 37.8 and Ch 32.11.
- **Quote:** "A scraper is about six times faster; the README's Limitations say so."
- **Problem:** six is pixels per minute (180 x 512^2 / 120 x 256^2), which Ch 30 line 874 states correctly. In time to harvest 500 pages, 2.4 hours becomes 33 minutes (Ch 37.8), about 4.4 times. The README does not say "six times".
- **Suggested fix:** "A scraper pulls about six times more pixels a minute, a 500-page harvest drops from about 2.4 hours to about 33 minutes".

### SA-08 Part IV chapters place the blueprint and Decisions after Try it, not before In this project
- **Where:** Ch 25 to 31 (for example Ch 25: Try it at line 733, Blueprint 771, Decisions 810; Ch 27: In this project 695, Try it 713, Blueprint 746).
- **Problem:** the brief's template says "Architecture blueprint vN" and "Decisions and challenges" sit between Advanced and In this project. In the sources they follow Try it. Ch 26 keeps "In this project" as a numbered subsection (26.15). Also, in the PDF (p.449) the reader meets Exercise 30.3 to 30.6 and then "Architecture blueprint v5" as if part of the exercises.
- **Suggested fix:** move both sections before "Common mistakes"/"In this project" in each Part IV chapter, or update the template note in STYLE if the current order is intended.

### SA-09 Blueprints v5 and v6 drop the components that define the app
- **Where:** `book/blueprints/v5-platform.md`, `v6-final.md` and the copies in Ch 30/31.
- **Quote:** "Controllers: Auth, Document, PageTileUrl, Tile, Admin, UserAdmin, UserDirectory"
- **Problem:** v0 to v2 show `SignedUrlService`, `WatermarkService`, `SessionKeys`, `AuditLogService`; v5/v6 show none of them, and `TileController` is not tied to signing or watermarking. `UserAccountService`, `SessionKeys`, `SessionMetadata` are also absent. Facts stated in the diagram are right (compose addresses, published ports on 127.0.0.1, Prometheus by allowed address, MySQL V1 to V3, versioned tile folders); the omission makes v5/v6 a deployment diagram, not the application blueprint. The blueprint README already admits v3/v4 are shallow.
- **Suggested fix:** add a node "Tile pipeline: SignedUrlService, SessionKeys, WatermarkService" and "AuditLogService" to v5/v6, or state in the caption that v5/v6 show deployment and only new components.

### SA-10 Listing 38.4 caption still names one file for two files
- **Where:** Ch 38, Section 38.5.
- **Quote:** "**Listing 38.4 — `DocumentService.java` (`book-m6-final`, two excerpts, in file order)**" followed by `*Path: ...document/DocumentService.java*`
- **Problem:** the second block is `AuditLogService.ROW_MAPPER`, identified only in prose below. The caption and the Path line say both come from `DocumentService.java`, contradicting "in file order".
- **Suggested fix:** "Listing 38.4 — `DocumentService.java` and `AuditLogService.java` (`book-m6-final`, one excerpt from each)" with a Path line for each, or split into 38.4 and 38.4a.

### SA-11 "Bulkhead" claim ignores the shared CPU and heap
- **Where:** Ch 38, Section 38.9.
- **Quote:** "a burst of slow uploads can't consume the permits that tile serving needs, and the reverse."
- **Problem:** true for permits (two separate semaphores: `TileGenerationService.renderPermits`, `TileWorkLimiter.slots`), but both kinds of work run in the same JVM and share CPU, heap and Tomcat threads (Ch 39.6 says so). A render can still slow tile serving.
- **Suggested fix:** add "The compartments are separate permit pools, not separate resources: both draw on the same CPU and memory, so this limits how many, not how much."

### SA-12 Scale-out plan inventory of in-memory state and scheduled jobs is not complete
- **Where:** Ch 37, Section 37.17, Steps 0 and 4.
- **Quote:** "The janitor, the audit purge, and the device purge use `@Scheduled`".
- **Problem:** at `book-m6-final` there are five `@Scheduled` methods (`StorageJanitor` 6 h, `AuditLogService` hourly sweep and nightly purge, `KnownDevices` purge, `LoginThrottle` sweep every 5 min). The in-memory list omits `SessionMetadata` (per-session map behind the admin session list), `SessionAdministration`/registry, and per-instance caps (`TileWorkLimiter`, render permits) whose semantics change with three instances (a "server-wide" cap becomes per instance).
- **Suggested fix:** add the two per-instance sweeps to Step 4 (harmless but present) and `SessionMetadata` to Step 0/Step 1; add a sentence that `TileWorkLimiter` and render permits stay per instance and that this is correct for CPU protection.

### SA-13 Ch 39 "In this project" first-appears column is imprecise
- **Where:** Ch 39, "In this project" table.
- **Problem:** `docker-compose.yml` exists from `book-m1-accounts` (MySQL only); the app `Dockerfile`, `frontend/Dockerfile`, nginx and Caddy are m5, so "`docker-compose.yml`, `Dockerfile` | `book-m5-platform`" needs "full stack from". Controllers exist at m0, and m0 already is a client-server design (static page), so "controllers | m1" is late for section 39.4. `AuditLogService` starts at m1 in `service/` and moves to `audit/` at m2.
- **Suggested fix:** "docker-compose.yml (MySQL only from m1; full stack from m5)"; "controller/ from m0, Angular from m1"; "AuditLogService: m1 (service/), audit/ package from m2".

### SA-14 Story ends at PR #12 and 38 commits, but the repository has a merged PR #13 the book depends on
- **Where:** Part IV intro ("pull requests numbered 1 to 12"), Ch 7, Ch 21/26 canvas statements.
- **Quote:** "In total the repository has 38 commits and pull requests numbered 1 to 12."
- **Problem:** `gh pr list` shows 13 PRs (10 merged, 3 closed). PR #13 ("Docs: the viewer places tiles as positioned elements, not on a canvas") merged after `book-m6-final`; the README at the tag still says "reassembles the tiles onto a `<canvas>`" (verified) and Ch 21.9/26 say "not on a canvas". The book never tells the reader that the README at the tag is stale on this point.
- **Suggested fix:** "At `book-m6-final` (commit `a27e069`) the repository has 38 commits and pull requests 1 to 12; a later documentation-only pull request (#13) corrected the README's mention of a canvas. At the tag, trust the code (Chapter 21), not that sentence."

### SA-15 Table 36.1 says the OSV scan proves "no published advisory"
- **Where:** Ch 36, Table 36.1.
- **Quote:** "No Maven or npm dependency, including transitive ones, has a published advisory"
- **Problem:** the CI comment says the job fails on any published vulnerability, but the scan can only report advisories present in the OSV database at run time. Chapter 32 itself shows a framework that shipped a vulnerable Tomcat before it was flagged.
- **Suggested fix:** "No Maven or npm dependency, including transitive ones, has an advisory known to the OSV database on the day the job ran".

### SA-16 (PDF only) Figure 37.1 shows the target above "today"
- **Where:** Ch 37, Section 37.17, PDF p.533.
- **Problem:** the text says the figure "contrasts today's single instance with the target"; in the PDF the "Design exercise: three instances" box is drawn first (above) and "Today: one instance" below it, and the two are stacked without a link, so reading order is reversed.
- **Suggested fix:** declare the `LATER` subgraph after `NOW`, or split into two figures 37.1a and 37.1b, "Today" first.

### SA-17 Epilogue and closing claims read as more finished than the project is
- **Where:** `appendices/epilogue.md`.
- **Quote:** "You now have a working, hardened web application"; "Part V made it real."; "the first version of nearly every protection had a gap that someone found by trying to get around it."
- **Problem:** the reader has followed a book about the app; the app itself is a private repository whose go-live checklist is listed as open (Ch 31.16) and whose independent review was by AI agents plus the owner (no human penetration test). "Nearly every" is not counted anywhere. The Chapter 37/32 honesty is stronger than this closing.
- **Suggested fix:** "a hardened reference implementation" and one sentence: "It has not been run in production or tested by an outside security team; the go-live checklist in the README is the remaining work." Replace "nearly every" with "many".

### SA-18 The worked framework example does not follow its own step 2
- **Where:** Ch 39, Sections 39.16 and 39.17.
- **Quote:** "List at least two or three real options, including 'do nothing' and 'the simplest thing'."
- **Problem:** the lockout example's options (a), (b), (c) are three increments of one design; "do nothing" and the qualitatively different options the book itself names (MFA, an identity provider, a CAPTCHA-style delay) are absent, and the example is a retrofit (the text says the facts come from PR #5 and commits), not a decision made with the framework. It is honest but is a weaker demonstration than the chapter implies.
- **Suggested fix:** add "(d) do nothing" and "(e) MFA or identity provider (out of scope: 37.6)" to Step 2/Table 39.3 with one line of cost each, and say "retold with the framework".

### SA-19 Flyway version differs from the PR text the chapter cites
- **Where:** Ch 37, Section 37.16; Ch 30 (line ~52); Part II intro.
- **Quote:** "bringing Spring Security 7, Jackson 3, Hibernate 7, and Flyway 12, all 'the latest GA versions checked on Maven Central'"
- **Problem:** PR #5's body says "Flyway 11". The book's "Flyway 12" is consistent with the resolved artifact (`flyway-core` 12.4.0 in the local Maven repository for Boot 4.1.1) but the sentence cites the PR. A reader who opens the PR sees 11.
- **Suggested fix:** "and Flyway (11 in the pull request text, 12 as resolved by Spring Boot 4.1.1)", or drop the number from the quoted list.

## Checked and found correct

- **Ch 37:** the 16 sections use the six headings consistently; recorded-versus-opinion labelling is applied throughout (37.1, 37.4, 37.6, 37.9, 37.12, 37.13, 37.16). Verified at `book-m6-final`: `SDV_SESSION`, `SameSite=Strict`, 30 min idle, 12 h absolute; `SignedUrlService` HMAC-SHA256 over 7 fields with 120 s TTL; token bound to session (401 via `InvalidTokenException`), `410` for old render, `429`/`503` mapping; staged PDF deleted in `TileGenerationService` (`Files.delete(source)`); `watermark-opacity` 0.2 / 0.05 to 0.6, spacing 1.5; tile-size 256 to 512 and rate 120 to 180 at m5 (m1 to m4: 256 and 120); 500-page harvest arithmetic (17,500/120 = 2.4 h; 6,000/180 = 33 min); `SIGNING_SECRET` keys `SignedUrlService`, `SessionKeys` and `KnownDevices`; startup fails below 32 characters; role change or disable revokes sessions; commit hashes and dates in the source comments (`51ea941` Sep 19, `82c24b6`, `f682716`, `66f7152`, `cd0f5c2`, `f468678`, `b6aef4e`, `2d10e07`, `2d82253`). The pre-PR #1 session id in `sessionStorage` and the `X-Session-Id` header are real (Angular baseline, commit `32d040f`). 37.17 scale-out order is sound and the coupling statements (S3 touches watermarking, signing, janitor, backups, row lock) are right.
- **Ch 39:** Listings 39.1 to 39.3 match `book-m6-final` line for line (checked by script); filter registration order and "both custom filters arrived in m5" hold; `audit_event` insert plus purge only, `REQUIRES_NEW` in `AuditLogService`; `PAGE_VIEWED` 10-minute and `ACCESS_DENIED` 5-second caps; metric names map to `sdv.tiles.served`, `sdv.tiles.rate_limited`, `sdv.tiles.busy`, `sdv.render.rejected`, `sdv.render.timed_out`, `sdv.render` (seconds); compose ports on 127.0.0.1, `USER app`, CI `permissions: contents: read`; `.anyRequest().denyAll()`; `SESSION_COOKIE_SECURE` default false; nginx `try_files` and overwrite of `X-Forwarded-For`; nginx has no rate limiting (so "reverse proxy, not an API gateway" is right). Fig 39.2's two dotted arrows point at the right repositories. The twelve-factor table honestly marks "stateless" as No.
- **Ch 32:** Table 32.1 rows match the code; the check order in Listing 32.3 and Fig 32.2 matches `TileController`; the trust-boundary chain (Caddy 172.28.0.11, nginx 172.28.0.10, `set_real_ip_from`, `FORWARD_HEADERS_STRATEGY` default `none`) matches `nginx.conf`, `Caddyfile`, `application.yml`; `max-concurrent-renders` 2, `render-timeout` 3 m; AI reviewers and the human product owner are distinguished correctly.
- **Ch 34/36:** state table, consistent-backup logic, restore-drill steps and the README runbook agree; OSV and Trivy commands, digest and SHA pinning agree with `ci.yml`.
- **Blueprints v0 to v3, v5:** class names and file lists checked against `git ls-tree` at m0 (`SessionController`, `SessionService`, `DocumentRegistry`, static page with `canvas`), m1 (`AuthController`, `UserAdminController`, `AdminController`, `V1` migration, `DocumentRegistry` still in memory, Angular 22, TypeScript 6.0, Vitest 4), m2 (`document/`, `audit/`, `StorageJanitor`, `UserDirectoryController`), m5 (Boot 4.1.1, Tomcat pin, `KnownDevices`, filters, `TileWorkLimiter`, `Caddyfile`, `nginx.conf`, V3 migration). v6 statement about `ec6c1c5` (PR #9, test-only), PR #10 to #12 matches `git log`.
- **History:** PO and TM reviewers presented as AI agents in Part IV intro, Ch 25, 32.4, Part V intro, preface; the ultrareview story (165 files, 22,096 lines, limits 500/8,000) is consistent between Ch 30 and 31.
- Earlier open items now fixed: Fig 39.2 arrows, Ch 38 "Section 38.13" pointers, Ch 39 table captions, chapter headers of Ch 32 to 37 (now `expanded`), Table 14.2 "six".

## Not checked

- Chapters 33, 35 read only for trust-boundary, scaling and metric claims (grep-level), not line by line; Ch 36 and Ch 34 read in part (Tables 36.1, Sections 34.3, 34.7, 34.11, 34.12).
- Ch 25 to 31: read the "Decisions and challenges" sections of 25, 26, 27, 28, 29, 30, 31 and the blueprint of 25; the rest of each chapter (beginner, intermediate and advanced tiers, listings, exercises) was not reviewed here (Senior Full-Stack Engineer and Technical Lead lenses).
- Ch 38: Sections 38.2 to 38.14 read; listings other than those cited not compared with the tag (38.1, 38.2, 38.3, 38.5 to 38.9 unverified byte for byte; the code facts around them, `createDelegatingPasswordEncoder`, `HttpSessionEventPublisher`, `@EventListener`, `RenderState`, `MAX_CONCURRENT_TILE_FETCHES = 6`, `Deque<Instant>` window, were confirmed).
- Blueprint v4 and Appendix B not read; v1 to v3 endpoints were not compared with every controller mapping.
- Exercises and solutions (`*.solutions.md`, Appendix C) were not reviewed.
- Only seven PDF pages were viewed as images; figure legibility (SA-02) is an estimate, not measured. EPUB and HTML outputs were not opened (SA-01 may or may not apply to them). Accessibility of the PDF (tagging, alt text) is out of my lens.
- The counts 114 backend and 31 frontend tests were not reproduced (`@Test` count at `6cf17fa` is 116 by grep, which includes annotations that may not all be executed; frontend `it(` count is 31).
- The PR bodies were read only for the passages cited (PR #1 and #5); the claim about Postgres being considered and rejected (37.9) and the rate-limit sign-off wording were not traced to the PR text.
