# Context brief for the final review

Written 2026-09-20 by a fresh agent from: the memory files, book/_team/CHARTER.md, book/STYLE.md, book/OUTLINE.md, book/README.md, book/_team/reviews/STATUS.md and CHECKLIST.md, the head of book/_team/requests.md, the dossier (DOSSIER.md, versions.md; other dossier files by name and size only), book/build/build.py, header.tex, order.txt, the app README.md, and Chapters 1 and 38 (headings and openings). Nothing here was re-checked against the code at the tags; statements marked (uncertain) need your own check. Work only in `C:\dev\secure-doc-viewer`, branch `book/draft`. The OneDrive copy of the project is stale: never use it.

## 1. What the app is

The Secure Document Viewer is a Spring Boot service plus an Angular frontend that shows PDF documents to signed-in users without ever handing out the PDF. Source: `README.md`.

- **Core idea.** After upload, each PDF page is rasterized on the server (PDFBox), sliced into PNG tiles (512 px by default, 150 DPI), and only the tiles stay on disk. No endpoint returns a whole page or document. The browser fetches tiles one at a time and paints them at (col x tileSize, row x tileSize).
- **Signed tile URLs.** `SignedUrlService` issues an HMAC-SHA256 token over document, page, row, column, render version, a keyed hash of the session, and an expiry (default 120 s). Tokens can't be edited, reused after expiry, or redeemed from another session. The session is checked on every tile request, so sign-out or admin revocation kills outstanding URLs.
- **Per-request watermark.** `WatermarkService` stamps viewer, UTC time and a trace code on each tile at request time (one stored tile serves everyone; responses are `Cache-Control: no-store`). It gives attribution, not prevention. Red, 20% opacity by default.
- **Accounts and access.** Built-in accounts (username plus BCrypt password, 72-byte limit), roles ADMIN, PUBLISHER, READER; no self-signup. Documents have an owner and visibility PRIVATE (owner plus shares) or EVERYONE. Outsiders get `404`, not `403`. Session cookie is httpOnly, with a CSRF cookie and `X-XSRF-TOKEN` header. Sessions: 30 minute idle timeout, 12 h maximum.
- **Sign-in throttling.** Rolling 15 minutes: 5 failures per account+IP, 20 per IP, 20 account-wide from unrecognised devices. Responses `429` plus `Retry-After`. Only a keyed hash of the address is stored.
- **Tile rate limit.** Per user, rolling window. The README says default 180 per 60 s; the dossier says 120 at m1, and memory says it was raised 60 to 120 on 2026-09-18 (uncertain which tag moved it to 180; check `application.yml` at the tag a chapter quotes). Overload returns `503` plus `Retry-After`; URLs for a replaced render return `410`.
- **Other hardening.** Uploads capped at 50 MB, `%PDF-` signature check, `max-pages` 500, `max-page-pixels`, render timeout, bounded concurrent renders, audit trail in MySQL (180-day retention), strict CSP and security headers, actuator health and Prometheus metrics restricted by address, `StorageJanitor` sweeps.
- **Stack (final, `book-m6-final`).** Java 25, Spring Boot 4.1.1 (Tomcat pinned 11.0.26), Spring Security 7, Jackson 3, Hibernate 7, Flyway 12 (12.4.0 per status notes), MySQL 8.4 LTS, PDFBox 3.0.8, Maven 3.9.16 via wrapper, Angular 22, TypeScript 6.0, Node 24, Vitest 5, Playwright 1.63, jsdom 30. Docker Compose profiles `full` and `tls` (Caddy). nginx-unprivileged serves the app and proxies `/api`. Images and GitHub Actions pinned by digest/SHA; Dependabot with LTS-only rules.
- **Older tags.** `book-m0-mvp` to `book-m4-reading` use Spring Boot 3.3.4, Java 21, PDFBox 3.0.3; m0 has no frontend (a static `index.html`, which does use a canvas); Angular first appears at m1; Vitest 4.x until m6. m5 and m6 use Boot 4.1.1, Java 25.
- **Canvas caveat.** The app README still says tiles are reassembled "onto a `<canvas>`"; the book (per a resolved request) says the Angular viewer uses absolutely positioned divs with CSS backgrounds from blob: URLs. Expect the book to be right and the README stale (uncertain; verify in `frontend/` at `book-m6-final`).
- **Project history.** 38 commits, 12 PRs (9 merged, 3 closed), final commit `a27e069`. Two independent review agents (Product Owner and Senior Technical Manager) rated the early app "not production-ready"; the user chose built-in accounts and MySQL in Docker, phased fixes, one PR per phase. Five review rounds on PR #5 found real defects (XFF spoofing, lockout DoS, mixed tiles, sign-in race, 72-byte BCrypt, Tomcat CVEs, endless 410 reload). The user dropped the "ultrareview" because the diff exceeded its limits. The GitHub repository is private; its visibility must never change.
- **Stated limitations** (README): nothing prevents photographing a screen; a legitimate user can still harvest tiles slowly; sessions and rate-limit counters are in memory (single instance only); no groups; no MFA; publishers can discover usernames through the share picker.

## 2. What the book is

*Building a Secure Document Viewer: From First Line of Java to Production.* Branch `book/draft`, status "expanded draft".

- **Audience.** A complete beginner who has never programmed but is intelligent and patient; a professional engineer must also find nothing to correct (STYLE 1). Only what this app needs is taught; other topics get one sentence at most.
- **Structure.** 39 chapters in six parts plus front matter and appendices:
  - Front matter: preface, how to use this book, setting up your machine.
  - Part I Foundations, Ch 1-10 (big picture, command line, Java, classes, collections, Maven, Git, web, SQL/MySQL, Docker).
  - Part II Backend, Ch 11-18 (Spring Boot, REST, validation, JPA/Flyway, Security I and II, files/PDF/signatures, backend testing).
  - Part III Frontend, Ch 19-24 (TypeScript, Node/npm, Angular components, HttpClient, routing/guards/forms, frontend testing).
  - Part IV Building the app, Ch 25-31 (milestones M0 to M6, one per tag).
  - Part V Production, Ch 32-36 (security review, deployment/TLS, backups, metrics, supply chain).
  - Ch 37 Engineering trade-offs (its own folder, `book/tradeoffs/`).
  - Part VI Patterns, Ch 38 design patterns and Ch 39 architectural patterns.
  - Back matter: epilogue, Appendix A glossary, B blueprint history, C exercise solutions, D command reference, E troubleshooting (plus `index-terms.md` in the folder).
  - Each part has a `00-part-introduction.md`.
- **Chapter template** (STYLE 6, 7). Opening paragraph; Learning objectives (3-6 measurable bullets); Prerequisites (earlier chapters only); **Beginner tier** (analogy with a mandatory "Where the analogy breaks down:" paragraph, terms in bold at first use, setup code explained line by line); **Intermediate tier** (how pieces talk, at least one "Why this and not X?"); **Advanced tier** (security, performance, real incidents from the project, each sourced); In this project (files, tags, `git show <tag>:<path>`); Try it; Summary (4-8 bullets); Further reading (official documentation only). Tier headings are `## Beginner tier: ...` and so on; intermediate and advanced carry an italic "you can skip" line. Numbered sections N.M run continuously through the chapter. Part IV milestone chapters add "Architecture blueprint vN" and "Decisions and challenges" between Advanced and In this project. Exempt from tiers: preface, part openers, Ch 37, setup. Target 4,000-8,000 words (Part IV up to 10,000).
- **Exercises.** 3-6 per chapter graded ★, ★★, ★★★ (at least one of each of the first two levels); ★ and ★★ solutions in Appendix C, ★★★ get an outline. Exercises must not need credentials, should name the tag, and tell the reader to work on a new branch.
- **Listings.** "Listing" means code copied exactly from the repo at a named tag (`git show <tag>:<path>`); caption above in bold, `**Listing N.M — \`File.java\` (tag[, simplified/excerpt/annotated ...])**`, and an italic `*Path: ...*` line below the block. "Example" means code written to teach, never tagged, never presented as project code. Tags: `book-m0-mvp`, `book-m1-accounts`, `book-m2-documents`, `book-m3-hardening`, `book-m4-reading`, `book-m5-platform`, `book-m6-final`. Parts I-III quote `book-m6-final` by default.
- **Figures.** Mermaid diagrams in `mermaid` fences, caption below in italics (`*Figure N.M — ...*`), plus a required `*Text description: ...*` line; at most about 15 nodes; no custom colors except the blueprint convention. Rendered to PNG at build time. Status notes count 70 captioned figures (67 Mermaid). Blueprints v0-v6 live in `book/blueprints/`.
- **Glossary and index.** Every bolded term is defined once (first use) and goes into Appendix A; `book/GLOSSARY.md` is the working glossary; `appendices/index-terms.md` supports the index.
- **Tables** have captions above in bold and a header row.

## 3. How it is built and where the final copy lives

- `python book/build/build.py` (needs Docker, Node/npx, host Chrome). It joins the sources listed in `book/build/order.txt` (paths relative to `book/`), strips HTML comments, writes `book/build/out/manuscript.md`, renders Mermaid to PNG with mermaid-cli (`-e png -s 2`) into `out/diagrams/`, gives each diagram alt text taken from its italic figure caption, then runs Pandoc in a Docker image (`sdv-book-pandoc`, from `book/build/Dockerfile`).
- Outputs in `book/build/out/`: `secure-doc-viewer-guide.pdf` (XeLaTeX book class, A4, one-sided 11 pt, TeX Gyre Pagella text, DejaVu Sans Mono code, layout in `header.tex`), `secure-doc-viewer-guide.epub`, `secure-doc-viewer-guide.html` (single self-contained page), and `manuscript.md` (about 1.67 MB). The files on disk were last written 2026-09-20 between 06:51 and 07:00. Check that outputs are newer than the sources; rebuild if not.
- Markdown sources sit in part folders: `book/front-matter/`, `part-1-foundations/`, `part-2-backend/`, `part-3-frontend/`, `part-4-building-the-app/`, `part-5-production/`, `tradeoffs/`, `part-6-patterns/`, `appendices/`. Chapters are `NN-slug.md`, with a metadata comment on line 1 (`<!-- chapter: N | part: I | owner: ... | tag: ... | status: ... -->`), which the build strips.
- Accessibility already in the build: alt text on diagrams from figure captions, `lang=en-US`, a table of contents, `colorlinks`. I found no explicit PDF tagging or EPUB accessibility metadata in `build.py` or `header.tex` (uncertain; the accessibility reviewer should test the actual files). Project memory says the final book must be fully accessible: alt text, tagged/structured PDF, contrast, EPUB accessibility metadata, reading order, no colour-only meaning.
- Post-review pipeline (memory): this review, then a layout artist, a proofreader, a technical reviewer, and the owner as author. Layout is not final, so the three content reviewers should treat visual polish as out of scope.

## 4. Conventions and known, accepted exceptions

Conventions to expect (STYLE): American English, serial comma, second person; no "we" for project history; no hype words ("simply", "just", "easy", "obviously", "robust", "seamless"); no emojis; sentence-case headings; a word list (sign in / sign-in, backend, frontend, database, tile, signed URL, watermark); numerals for 10 and above and for units; cross-references by type and number (never "above/below", never page numbers); bold only at first definition; people in the project referred to by role, never by name or email; history claims carry `<!-- source: ... -->` comments (stripped from the output); Further reading is official documentation only.

Accepted exceptions (do not report as defects):

1. **Placeholder secrets** in listings and examples, such as `<test-signing-secret>`, `<test-admin-password>`, `<db-password>`. They are deliberate (STYLE 16). A listing with a placeholder differs from the tagged file on purpose; a real-looking secret would be a Blocker.
2. **Reordered, combined or diff-style listings** explained in their captions (for example "two excerpts, in file order", "simplified: comments omitted"). Only a missing or misleading explanation is a finding. Listing 38.4 (two excerpts of `DocumentService.java`) was flagged earlier as combining two files; check its current caption.
3. **The PO and TM reviewers in the story were AI agents**, as was the coding agent working with the project owner. The book says so once early in the relevant chapters. Do not report that as odd; do report any place that presents them as human, or names them.
4. **Version differences by tag** (Boot 3.3.4 vs 4.1.1, Vitest 4 vs 5): correct if the Part IV chapter says which versions its tag used.
5. **Simplifications** announced with a pointer ("we simplify here; Chapter N gives the full picture").
6. **British spelling inside quotations** from project text is kept exactly.
7. **Sample usernames** `pub.one`, `reader.one`, `outsider.one`, `admin@example.com` and `example.com` are intended.
8. **Established analogies** (wristband, coat check, spreadsheet, doctor's form) are intended; each must have a "breaks down" paragraph.
9. The app README's wording is not authoritative when the code disagrees; the code at the tag wins.

Open items from the earlier QA window (STATUS.md, window 9). Verify whether each is fixed; report only if still open: Ch 21 Listing 21.6a numbering; Ch 30 "five gates later"; Table 14.2 says five sweeps but there are six (`TileRateLimiter.sweep`); Ch 38 "Section 38.12" should be 38.13; Ch 39 table captions; "FileOperations two seconds" (Ch 2, 17, 27) vs about four; Ch 25/26 pointer to Table IV.3 (how to run an older milestone); Figure 36.2 label imprecise; Figure 39.2 "skip a layer" label on the wrong arrow; Figure 24.1 e2e job dependencies; missing source comments on several blueprint figures; pacing for beginners in Ch 1, 3, 4, 5, 24; analogies missing "breaks down" in Ch 5, 8, 9; Ch 13 "Chapter 16" pointer inside Prerequisites; Ch 32-37 headers possibly still `expanded-draft`.

## 5. Quality bars

1. **Beginner-readable.** Nothing used before it is taught (check the "Assumes" lists in `book/OUTLINE.md`); every term defined at first use; why before how; analogies mapped and limited; honest simplification; hands-on steps a beginner can perform with what the book has taught so far (Ch 8-9 and some Part IV exercises were earlier flagged for needing a running app or database).
2. **Technically true against the code at the tag.** Every listing matches `git show <tag>:<path>` except where captioned; versions, numbers, defaults, class and method names, test counts, file paths, commands and security claims are true at the tag the text names; no overclaiming; no invented history (every decision, bug, number and date traces to the dossier, git or a PR). Numbers to hold to: 114 backend and 31 frontend tests at `6cf17fa`; 165 files and 22,096 lines against ultrareview limits of 500 and 8,000; lockouts 5/20/20; Vitest 4.x until m6.
3. **Consistent.** Chapter numbers, cross-references, figure/listing/table numbering, glossary, word list and tag names agree across the book and with `book/OUTLINE.md`.
4. **Accessible.** Figures have meaningful text alternatives (alt text and the Text description line); no meaning carried by color alone; logical heading structure and reading order; tables have header rows; link text is meaningful; contrast is adequate in the built formats; the PDF is tagged/structured and the EPUB carries accessibility metadata (test the real outputs; uncertain whether they do).
5. **Safe.** No real password, secret, token, session id or personal email address anywhere in the book or the build outputs; attacks are shown only against the reader's local copy.

## 6. Review protocol

- Each reviewer writes exactly one file in `C:\dev\secure-doc-viewer\book\_team\final-review\`: suggested names `01-senior-fullstack-engineer.md`, `02-software-architect.md`, `03-technical-lead.md`, `04-accessibility.md`. One owner per file; do not touch another reviewer's file.
- **Do not edit any chapter, the build, or anything outside your own review file. Do not commit, push, tag, or switch branches or tags in the working tree.** Read old code with `git show <tag>:<path>` or `git archive` into the scratchpad.
- **Never print, copy or quote passwords or secrets. Never open `reviewer-accounts.json`, `initial-admin-password.txt`, `e2e-admin-password.txt`, `.env`, `twoip/pw.env`** or any file holding credentials. If you see a secret by accident, record only its location, never the value.
- Do not run or sign in to the app, or change Docker or the database. Reading code, history and built files is enough.
- Record each finding with these fields:
  - **ID:** your prefix plus a number (for example FSE-07).
  - **Severity:** Blocker (wrong, invented, insecure or unteachable; must fix before publication), Major (misleads, or breaks a house rule in a way that harms learning or trust), Minor (polish).
  - **Where:** chapter and section (for example "Ch 16, Section 16.4") and file path.
  - **Quote:** the exact affected words (short).
  - **Problem:** one or two sentences, with evidence (file at tag, line, or output you saw).
  - **Suggested fix:** concrete replacement text or action.
- Open the file with a short verdict and a table of counts by severity. Then list Blockers, Majors, Minors. End with "Checked and found correct" (brief) and "Not checked" so the author knows the coverage.
- Mark anything you could not verify as "unverified" rather than guessing. Do not re-report the accepted exceptions in section 4.
- Suggested lenses, to avoid duplicate work. Senior Full-Stack Engineer: code listings, commands, versions, exercises that must run, backend and frontend claims. Software Architect: blueprints, figures, Parts IV to VI, Ch 37 trade-offs, architecture and security claims. Technical Lead: coherence and ordering of the teaching, cross-references, process and history claims, terminology consistency, beginner pacing. Accessibility reviewer: section 5, item 4, against the built PDF, EPUB and HTML and the sources.

## 7. Where to look things up

| What | Path |
|---|---|
| App README | `C:\dev\secure-doc-viewer\README.md` |
| Book root | `C:\dev\secure-doc-viewer\book\` |
| Charter (accuracy rules, off-limits files) | `book/_team/CHARTER.md` |
| House style (binding) | `book/STYLE.md` |
| Table of contents, per-chapter "assumes", chapter-to-tag summary | `book/OUTLINE.md` |
| Glossary | `book/GLOSSARY.md`, `book/appendices/appendix-a-glossary.md` |
| Architecture blueprints v0-v6 | `book/blueprints/` |
| Facts dossier (timeline, versions, decisions D1-D15, bugs A-H, reviews, milestone briefs) | `book/_team/dossier/` |
| Earlier QA reviews, status, checklist | `book/_team/reviews/` (`STATUS.md`, `CHECKLIST.md`, one file per chapter) |
| Cross-team requests | `book/_team/requests.md` |
| Writer progress notes | `book/_team/progress/` |
| Trade-offs evidence | `book/_team/tradeoffs-evidence.md` |
| Build script, order, layout, Dockerfile | `book/build/build.py`, `order.txt`, `header.tex`, `Dockerfile` |
| Built outputs | `book/build/out/` |
| Backend source | `src/main/java/com/example/securedocviewer/` (packages account, audit, document, config, controller, model, security, service) |
| Frontend source | `frontend/` (`package.json`, `proxy.conf.json`, `src/`) |
| Config and Flyway migrations | `src/main/resources/application.yml`; migrations such as `V2__documents_shares_audit.sql` (exact folder unverified) |
| CI, Dependabot, compose, images | `.github/workflows/ci.yml`, `.github/dependabot.yml`, `docker-compose.yml`, `Dockerfile`, `frontend/Dockerfile` |
| Code at a milestone | `git show <tag>:<path>`; `git diff --name-status book-m0-mvp book-m1-accounts`; PRs via `gh pr view <n>` (1-12) |

Quick checks: `git tag | grep book` lists the seven tags; the final app commit is `a27e069`. Listing exactness can be checked by comparing a fenced block with `git show <tag>:<path>`; an earlier script `listingcheck.py` did this for some chapters (its location is unverified).
