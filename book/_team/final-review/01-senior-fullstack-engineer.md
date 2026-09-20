# Final review 01: Senior Full-Stack Engineer

Reviewer lens: hands-on correctness and teachability of the code across the stack. Branch `book/draft`, repo `C:\dev\secure-doc-viewer`. No chapter, build file or other reviewer file was touched; nothing was committed; no secret file was opened.

## Verdict

The technical content is in very good shape. 279 captioned listings were machine-checked against `git show <tag>:<path>` (the scratchpad `listingcheck.py`); the 15 it flagged are all explained in their captions (diff-style, placeholders, `{ ... }` stubs, shortened comments). I also spot-checked prose claims and numbers against the code at the tags: test counts, defaults, gate order, sweeps, metrics, Dependabot rules, Compose and nginx. Almost everything is correct. Every Java example I compiled (Ch 3 to 5, 3.2 to 3.9, 4.1 to 4.3, 5.1 to 5.8, plus the Appendix C solutions for Ch 3 to 6) compiles and prints what the text says.

The one serious problem is in the **built PDF, not the Markdown**: multi-column tables run off the right edge of the page and lose their last column. Beyond that there are two exercise defects, one beginner-blocking gap for running older tags, and a handful of polish items. Both uncertain points in the brief are settled (Section "Settled points" below).

## Counts by severity

| Severity | Count |
|---|---|
| Blocker | 1 (PDF only) |
| Major | 3 |
| Minor | 9 |

## Settled points from the brief

1. **Tile rate limit, 180 versus 120.** Both are right, at different tags. `application.yml` has `tile-rate-limit-per-window: 120` at `book-m1-accounts`, `m2`, `m3` and `m4` (comment: "~35 tiles ... 120 lets a reader turn ~3 pages a minute"), and `180` at `book-m5-platform` and `book-m6-final` (with `tile-size: 512`). The change is in commit `cd0f5c2`. The book is consistent with that: Ch 26 says 120 (m1), Ch 30 Section 30.15 says "rose from 120 to 180", and Ch 1, 32, 37 and Appendix C say 180 (final). The arithmetic holds: 180 / 12 tiles = 15 pages a minute; 500 pages x 12 / 180 = 33 minutes; 500 x 35 / 120 = 2.4 hours. No change needed.
2. **Canvas versus CSS divs.** The book is right. `git grep -i canvas book-m6-final -- frontend/src` finds nothing; the Angular viewer positions `div` elements with `blob:` backgrounds (Ch 21 Section 21.9). The `<canvas>` appears only in the m0 static page (`src/main/resources/static/index.html`, Ch 25). At `book-m6-final` the app README (line 6) and the `PageInfo` Javadoc still say `<canvas>`; on `main`, PR #13 (`b44606e`, after the tag) fixed both. Ch 4 Section 4.x's note that the comment "is stale in the repository" is true at the tag only (see FSE-12).

## Blockers

### FSE-01 (PDF only) Multi-column tables are cut off at the right page edge
- **Severity:** Blocker (content is missing from the printed book)
- **Where:** built PDF `book/build/out/secure-doc-viewer-guide.pdf`. Text extraction finds spans past the 595 pt page edge on 89 pages. Confirmed by eye on PDF pages 26 (Part I table of contents), 35, 43, 97, 151 (Table 1, Part II), 174 (Table 12.3, where the "Comes from" column is clipped, for example `UserAdminController.cre`), 259 (Table 18.2, where the file list is clipped, for example `service/TileGridTest.java, service/SignedUrlServiceTest.java, securi`). Other affected pages from the extraction: 31, 66, 70, 71, 96, 99, 100, 105, 131, 157, 159, 161, 162, 164, 176, 185, 187, 201 to 203, 213, 216, 222, 224, 229, 238, 242, 244, 248, 263, 279, 291, 297, 305, 319, 325, 331, 335, 349 to 357, 364, 368, 386, 402, 410 to 415, 428, 433, 448, 465, 468, 477, 482, 490 to 500, 506 to 519, 535 to 540, 551, 560 to 568, 573 to 578, 586, 630, 641, 650.
- **Quote:** `Table 18.2 — Where Chapter 18's ideas live` (file column ends mid-path at the page edge).
- **Problem:** Pandoc/LaTeX tables with a long-text column are wider than the text block, and the overflow is clipped; on many pages the last column is only partly visible or unreadable, and the meaning of the table is lost. The same happens to long inline code in running text: `<java.version>25</java.version>` overflows on PDF page 97 (Section 6.10), and long paths in lists run off the page.
- **Suggested fix:** the layout artist should give tables `longtable` with `p{}` columns (or `tabularx`) and `\raggedright`, let inline code break (`\seqsplit` or `xurl`-style breaking for `\texttt`), then re-extract text and re-run the "beyond page edge" check (`span.x1 > page width - 5`). This must be fixed before the PDF is final.

## Majors

### FSE-02 Exercise 29.4 cannot be answered as written
- **Where:** Ch 29, Exercise 29.4 (`part-4-building-the-app/29-m4-reading.md`, and its solution in `29-m4-reading.solutions.md` and Appendix C).
- **Quote:** "compute `stepX` and `stepY` for a 20-pixel line height, a 120-pixel widest line and `spacing = 2.0`."
- **Problem:** `Layout.of` returns `stepY = lineHeight * lines.length + gap / 2` (verified at `book-m4-reading`, `WatermarkService.java` line 143). The exercise does not give the number of lines, so `stepY` has no unique answer; the solution silently assumes two lines ("With two lines, `stepY = ... = 60`").
- **Suggested fix:** add "for a watermark of two lines" to the exercise text (and state the assumption is two text lines).

### FSE-03 Example 12.2 (`curl` upload) cannot be followed
- **Where:** Ch 12, Section 12.7 to 12.8 area, Example 12.2.
- **Quote:** "once you have signed in and saved the cookies" and `curl -b cookies.txt -H "X-XSRF-TOKEN: <csrf-token>" ...`
- **Problem:** nothing in the book shows how to create `cookies.txt` or obtain the CSRF token from the command line (no `-c` login call, and Appendix D lists only a generic cookie-jar line). At `book-m6-final` a new publisher must also change a temporary password first (Ch 23). A beginner following the example gets a `401`/`403` and no hint why.
- **Suggested fix:** either mark it explicitly as "read-only illustration; Chapter 16 explains the CSRF header, and the browser does all of this for you", or add the two prior `curl -c cookies.txt` steps (sign in, then read the `XSRF-TOKEN` cookie from the jar) using placeholders for the password.

### FSE-04 Running the older tags needs tools the setup chapter never installs
- **Where:** Setup chapter (`front-matter/c-setting-up-your-machine.md`) versus Table IV.3 in `part-4-building-the-app/00-part-introduction.md`; exercises 25.3, 26.6, 27.6, 28.5, 28.6, 29.5 ("On your own copy at `book-m?...`").
- **Quote:** Setup: "You don't install Maven: the project's Maven wrapper (`./mvnw`) downloads the right version"; Table IV.3: `book-m0-mvp` needs "JDK 21 and an installed Maven ... no Maven wrapper".
- **Problem:** verified that `mvnw`, `mvnw.cmd`, `.mvn/` and the `Dockerfile` first exist at `book-m5-platform` (`git ls-tree`). m0 to m4 are `java.version` 21 with Spring Boot 3.3.4. The reader has only JDK 25 and no Maven, and no chapter explains how to install JDK 21 or Maven, or a safe way to use the m6 wrapper on an old checkout. The table itself says an old tag on a newer JDK "may work" (I could not verify Boot 3.3.4 on JDK 25; unverified). Six exercises depend on this.
- **Suggested fix:** add a short "Older milestones" step to the setup chapter or to Table IV.3's notes: install Maven (one line per OS), install JDK 21 alongside 25 and how to select it (`JAVA_HOME`), or explicitly say these exercises are optional read-alongs and give the pure-reading alternative (`git show <tag>:<path>`).

## Minors

### FSE-05 Table and figure labels repeat across Parts
- **Where:** part introductions and front matter. The built PDF shows "Table 1" five times (milestone tags, chapters of Parts I, II, V, VI) and "Figure 1" in Part II; Part IV correctly uses IV.1 to IV.3.
- **Problem:** cross-references by "Table 1" are ambiguous, contrary to STYLE's cross-reference rule.
- **Suggested fix:** number them by Part (Table I.1, II.1, V.1, VI.1; Figure II.1) as Part IV does.

### FSE-06 About 24 tables have their caption below, not above
- **Where:** for example Table 9.1 (`09-sql-and-mysql.md` line 42), and tables in Ch 1, 2, 3, 4, 6, 7, 8, 21 and the part introductions (grep `^\*Table N —` found 24 files with italic captions below the table).
- **Quote:** `*Table 9.1 — The app's tables at book-m6-final*` placed after the table.
- **Problem:** STYLE requires bold captions above tables; the book mixes both.
- **Suggested fix:** move to `**Table N.M — ...**` above the table.

### FSE-07 `npm test` may open watch mode
- **Where:** Ch 24 Section 24.3 ("Run `npm test` in `frontend/`. Both should pass ... run again"); Ch 20 Section 20.4.
- **Problem:** `npm test` runs `ng test`, which in an interactive terminal usually starts in watch mode and does not exit; the book only gives `--watch=false` in Section 24.15 (CI) and Appendix D. A beginner may think it hung. (I did not run it; unverified, based on Angular CLI behavior.)
- **Suggested fix:** add "In a terminal this stays running and re-runs on save; press `q` or Ctrl+C to stop, or use `npx ng test --watch=false` for one run."

### FSE-08 Exercise 25.3 can succeed by accident
- **Where:** Ch 25, Exercise 25.3 and its solution ("HTTP 401").
- **Quote:** "change one character of a token before the dot".
- **Problem:** the payload is unpadded base64url. The final character carries unused bits, and `java.util.Base64` does not reject non-zero trailing bits, so changing only that last character can decode to identical bytes, keep the signature valid and return `200`. The solution's 401 is right for any other character.
- **Suggested fix:** "change a character in the middle of the part before the dot".

### FSE-09 `@Transactional` rollback described too broadly
- **Where:** Ch 14, Section 14.6.
- **Quote:** "commits if it returns normally and rolls back if it throws."
- **Problem:** by default Spring rolls back for unchecked exceptions (`RuntimeException`, `Error`) only; a checked exception commits. A professional reader will object; a beginner may later be bitten.
- **Suggested fix:** "rolls back if it throws an unchecked exception (a `RuntimeException`; checked exceptions do not roll back unless configured)".

### FSE-10 Backup command puts the root password in a process argument
- **Where:** Ch 34, Sections 34.x (backup and restore), lines `mysqldump ... -p"$MYSQL_ROOT_PASSWORD"` and `mysql ... -p"$MYSQL_ROOT_PASSWORD"`.
- **Problem:** Section 10.4 (Listing 10.2 text) teaches that `MYSQL_PWD` keeps the password off the command line, and the project's own health check moved to it; the book's backup commands use `-p"..."`, which is visible to `ps` inside the container and prints MySQL's "password on the command line" warning. Not wrong, but it contradicts the lesson.
- **Suggested fix:** `sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" exec mysqldump --single-transaction --routines -u root "$MYSQL_DATABASE"'`, and the same for restore.

### FSE-11 Ch 2 comment miscounts path depth
- **Where:** Ch 2, Example 2.1.
- **Quote:** `cd src/main/resources    # a relative path, two levels at once` (followed by `cd ../../..  # back up three levels`).
- **Problem:** `src/main/resources` is three levels; the next line correctly goes up three.
- **Suggested fix:** "three levels at once".

### FSE-12 Ch 4 note about the stale `<canvas>` comment needs a time qualifier
- **Where:** Ch 4, note under Listing 4.3.
- **Quote:** "so the comment is stale in the repository".
- **Problem:** true at `book-m6-final`; PR #13 (`b44606e`, merged into `main` after the tag) removed the canvas wording from `PageInfo.java` and the README. A reader who clones `main` will not find the comment.
- **Suggested fix:** "so the comment is stale at `book-m6-final` (a later commit on `main` corrects it)". The `git log --oneline` output in Ch 7 already hedges the top line.

### FSE-13 Ch 35 illustrative metrics page has no way to fetch it in the shipped stack
- **Where:** Ch 35, Sections 35.6 and 35.9.
- **Problem:** the text is honest that the sample page is illustrative, but a reader who wants to see `/actuator/prometheus` cannot: the full stack does not publish the app port (`expose` only) and nginx proxies only `/actuator/health`; loopback-only scraping works only with `./mvnw spring-boot:run` on port 8080. The book does not say which run mode shows the page.
- **Suggested fix:** one sentence: "To see the real page, run the backend with `./mvnw spring-boot:run` and open http://localhost:8080/actuator/prometheus from the same machine."

## Checked and found correct

Covered in depth (chapter or section, what I checked):

- **Setup chapter (S):** versions (JDK 25, Node 24, Docker Compose v2), per-OS steps, the seven tags list, Maven wrapper 3.9.16 (`maven-wrapper.properties` at m6).
- **Ch 1:** 12 tiles per letter page (1,275 x 1,650 / 512 = 3 x 4); 180/60 s, 15 pages a minute, 33 minutes, 2.4 hours; 114 backend and 31 frontend tests (exact recount: 114 `@Test` at `6cf17fa` and at `book-m6-final`, 31 `it(` in `frontend/src`).
- **Ch 2:** `.env` handling, `openssl rand -hex 32`, retry backoff (`50L << min(attempt,4)`, `ATTEMPTS = 8`, about four seconds; Ch 27 corrects the Javadoc's "two seconds"). One nit, FSE-11.
- **Ch 3 to 5:** all 20 "Example" blocks compile with JDK 26 (fragments wrapped in a `main`), and outputs match the text (`3 columns, 4 rows`, `12`, `TileRef[row=2, col=1]`, and so on). Appendix C solutions for 3.1, 3.5, 3.6, 4.2 to 4.5, 5.2, 5.4, 5.5 compile and behave as stated. Ex 1.1, 3.4 (café 4 chars, 5 bytes), 3.6, 4.4 correct.
- **Ch 6:** `pom.xml` facts at m6 (`java.version` 25, PDFBox 3.0.8, `tomcat.version` 11.0.26 with the three advisories in the file's comment, `finalName secure-doc-viewer`, version 0.1.0); wrapper first appears at m5; Ex 6.5 dependency comparison m0 versus m2 matches the two `pom.xml` files.
- **Ch 7:** commands, `git show 2d10e07 --stat` (28 files, 997 insertions), 38 commits with milestone 5 = 15 commits plus its merge (verified with `rev-list`).
- **Ch 8:** curl commands, cookie flags, `SameSite=Strict`, `SDV_SESSION`.
- **Ch 9:** all six tables and their migrations `V1` to `V3` match; every SQL statement in the chapter uses columns that exist; the migration loop through `docker compose exec -T mysql sh -c 'MYSQL_PWD=...'` is valid because the container holds `MYSQL_USER`, `MYSQL_PASSWORD`, `MYSQL_DATABASE`; the Flyway "non-empty schema" warning is accurate.
- **Ch 10:** Compose listings (m1 and m6), profiles, ports 8081/8443, fixed subnet 172.28.0.x, `.dockerignore`; `docker-compose.yml` is byte-identical at m5 and m6.
- **Ch 11 to 14, 16, 18:** run command, bean table (`ViewerProperties` is `@Component` plus `@ConfigurationProperties`), Jackson 3 (`tools.jackson.databind.ObjectMapper` used in `SecurityErrorResponses`), `ddl-auto: none`, `open-in-view: false`, UTC JDBC settings, `PESSIMISTIC_WRITE`, the six scheduled sweeps in Table 14.2 (five are annotated `@Scheduled` in two classes plus `TileRateLimiter.sweep`; verified all six), BCrypt 72-byte limit and 12 to 128 character validation, security headers and CSP string, `SecurityIntegrationTest` has 22 tests, H2 in MySQL mode in `application-test.yml`, Testcontainers `@ServiceConnection`.
- **Ch 17:** 40,000,000-pixel limit arithmetic (Ch 28 openly corrects the config comment's "roughly A1": 40 M px admits A0), 4 bytes per pixel table.
- **Ch 19 to 24:** `idle.ts` boundary maths and Example 24.1 (both new tests behave as described, including the `<=` to `<` experiment); interceptor behavior on 401 versus `AUTH_PROBES`; `provideHttpClient(withInterceptors(...))` and built-in XSRF handling; `CurrentUser` fields used in Solution 24.3; `fitWidth`/`stageStyle`; `package.json` scripts; `npm ci`, `ng serve` on 4200; Appendix D `--watch=false`.
- **Ch 25 to 31:** Table IV.2 test counts (19, 45, 56, 66, 68, 114 by strict `@Test` count; frontend 4, 6, 6, 14, 31, 31); Table IV.3 (`.env.example` exists at m1, MySQL-only compose at m1); token, gate order (Table 30.3 matches `TileController.getTile`: signature, session binding, rate limit, access, version 410, capacity 503; "five gates" wording is gone); Retry-After arithmetic in Solution 26.4; Solutions 25.4, 26.5, 28.1, 28.3 (about 6.5 million pixels), 29.1, 29.3, 31.1, 31.5; Listing 28.4 and 28.6 and 31.1 diffs match `git diff`; `WatermarkService.Layout` at m4; `frontend/package.json` diff m5 to m6 (jsdom 28 to 30, vitest 4.0.8 to 5.0.1).
- **Ch 33 to 36:** first-run commands, service table, `tls` profile, backup and restore volume name `secure-doc-viewer_app-storage` (compose volume `app-storage`, project name = folder name from the setup step), metrics names and types (`sdv_render_seconds` summary, `sdv_render_abandoned_running` gauge), CI Trivy step, Dependabot rules (Listing 36.5 matches).
- **Open items from the brief that are now fixed:** Listing 21.6 numbering; Ch 30 "five gates later"; Table 14.2 lists six sweeps; "two seconds" versus about four (Ch 27 explains); Ch 25/26 pointer to Table IV.3 exists in Ch 25 to 29; Ch 32 to 36 headers say `status: expanded`; Listing 38.4 caption ("two excerpts, in file order"); no "Section 38.12" reference found.
- **PDF pages inspected (images):** 26, 43, 54, 71, 97, 151, 174, 259, 299, 338 (not opened), 369. Also full-text extraction of all 652 pages for the overflow scan and for label checks. Listings wrap correctly with a continuation arrow (pages 43, 54, 299), figure 25.2 is legible (page 369), listing captions and `Path:` lines print as in the source, and listing numbering (for example 21.6) prints correctly.

## Not checked

- Chapters 13, 15, 17, 20, 21 and 23 were verified only at the level of their listings (machine check), selected claims, and exercises; I did not read every advanced-tier paragraph.
- Ch 32 (security review record), Ch 37 (trade-offs), Ch 38 and 39 (patterns) and the Part V introductions: not reviewed beyond the Ch 32 rate-limit sentence, Ch 37's 180 figure, and Listing 38.4's caption. Architecture and history claims belong to the Software Architect and Technical Lead lenses.
- The history claims (bug IDs, PR numbers, dates) were not audited against the dossier or `gh`; only counts that git can prove (commits, test counts, tags) were checked.
- I did not run the app, Docker, Angular, Vitest, Playwright or any backend test (protocol), so every "you should see" output for those tools is verified only by reading code. Java and Node behavior claims for the standalone examples were run for real (JDK 26.0.2, Node 24).
- Runtime behavior of Boot 3.3.4 on JDK 25 (relevant to FSE-04) and of `ng test` watch mode (FSE-07) is unverified.
- TypeScript examples in Ch 19 were not type-checked (no `tsc` available offline); the Appendix C TypeScript solutions are fragments and were only read.
- Exercises with ★★★ "outline" solutions were not all worked; I worked those that are numeric or code-based (25.4, 26.4, 28.3, 29.x, 31.1, 31.5, 35.2, 24.1).
- Accessibility, colors, EPUB and HTML outputs are outside my lens. PDF layout beyond FSE-01 is out of scope.
