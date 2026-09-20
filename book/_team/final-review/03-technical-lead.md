# Final review 03: Technical Lead (delivery quality and team practice)

Reviewer lens: whole-book learning path, cross-references and numbering, process chapters against the repo at `book-m6-final`, operability, consistency, leftover internal wording. Branch `book/draft`, built outputs dated 2026-09-20 06:51-07:00 (no source `.md` outside `_team/` is newer than the PDF). Findings marked **[PDF only]** appear in the built PDF but are not visible in the Markdown sources as a defect.

## Verdict

The book is in good shape as a learning path. Chapter structure, tier headings, exercise counts and grading, prerequisites ordering, milestone/version statements, listing/figure numbering and the CI, Dependabot and version claims all check out, and every open item from QA window 9 that I re-tested is fixed. What stops it being publication-ready is delivery and finish, not content: the built PDF has a broken Appendix C structure, the front matter promises an index and acknowledgments that do not exist, one appendix and every Part IV chapter leak internal file names, the glossary is incomplete and partly auto-extracted nonsense, and the "reproduce the environment" story has a gap (Playwright browsers). No Blocker; eight Majors.

| Severity | Count |
|---|---|
| Blocker | 0 |
| Major | 8 |
| Minor | 12 |

PDF pages inspected: 1, 2-15 (text of the contents pages), 16, 18, 28, 151-153, 300, 353-356, 514-515 (Figure 36.1, Listing 36.2), 598 (Chapter 19 solutions), 601 (Chapter 20/21 solutions), 613, 621, 633, 651; full-text scan of all 652 pages for leftover markers (`_team`, `requests.md`, `OUTLINE.md`, `solutions.md`, `TODO`, `dossier`, raw Markdown); bookmark outline (599 entries) read in full.

## Major

**TL-01 [PDF only] Appendix C breaks the book structure: stray top-level headings and duplicate entries**
- Where: Appendix C, `book/appendices/appendix-c-exercise-solutions.md` (lines 503-1539); PDF bookmarks and contents, PDF pages about 597-646.
- Quote: `# Solutions: Chapter 11` ... `# Chapter 19 solutions` ... `# Solutions for Chapter 25` ... `# Solutions: Chapter 38`.
- Problem: The per-chapter solution files were concatenated with their own H1s. Chapters 11-18 ("Solutions: Chapter N"), 19-24 ("Chapter N solutions") and 25-31 ("Solutions for Chapter N") and 38 each become a new top-level section, so the contents shows 22 extra top-level entries, three different naming styles, and duplicate rows for Chapters 19-24 (once as an H2, once as an H1). Sections 1-10, 32-37 and 39 have no H1, so they appear only as H2. In the PDF each H1 starts a new page; Chapter 19 solutions page (PDF 613) is a heading and a blank page, and "Chapter 21 solutions" is stranded as an orphan heading at the foot of the Chapter 20 solutions page (PDF 616). The printed order is also wrong: "Solutions: Chapter 38" appears after Chapter 37 as a top-level entry.
- Suggested fix: In the assembly step, strip or demote each solution file's H1 (keep only the `## Chapter N solutions` heading), use one naming pattern, and rebuild. Then confirm the PDF outline has Appendix C with 39 children and no orphans.

**TL-02 Reader-facing text refers to internal team files**
- Where: Appendix E intro (`book/appendices/appendix-e-troubleshooting.md`; PDF p. 651); every Part IV chapter's Prerequisites (Chapters 25-31); How to use this book; Appendix A and B intros.
- Quote: "Writers add entries through `book/_team/requests.md`." / "as listed in `book/OUTLINE.md`" / "Generated from `book/GLOSSARY.md`" / "Solutions are in `26-m1-accounts.solutions.md`." (Ch 25-31 Try it, PDF pp. 368, 386, 402, 415, 428, 448, 462).
- Problem: The first is a working-process instruction that has no meaning to a reader and names the team folder. `OUTLINE.md`, `GLOSSARY.md` and the `*.solutions.md` files are authoring artifacts; the solutions are actually in Appendix C, and the reader of the PDF or EPUB has no `OUTLINE.md`. Chapters 1-24 correctly say "Appendix C".
- Suggested fix: Delete the Appendix E sentence. Replace `book/OUTLINE.md` with "the prerequisites list at the start of each Part" or "Chapters N to M". Replace "Solutions are in `NN-....solutions.md`" with "Solutions are in Appendix C, Chapter N solutions." Replace "Generated from `book/GLOSSARY.md`" with nothing.

**TL-03 The front matter promises an index that the book does not have, and the Acknowledgments section is empty**
- Where: How to use this book, "Conventions" (`book/front-matter/b-how-to-use-this-book.md`, line ~125 of the manuscript); `book/front-matter/a-preface.md` line 91; `build/order.txt`; PDF p. 18.
- Quote: "the index terms list helps you find things again" / (Acknowledgments body is the comment) `<!-- To be written by the editor at final assembly. -->`
- Problem: `appendices/index-terms.md` is not in `order.txt`, and it is only an unsorted per-chapter word list with no page numbers, so the PDF has no index and the sentence describes something the reader cannot find. The Acknowledgments heading is in the contents and on its own PDF page (p. 18) with no text, and the hidden comment shows it is unfinished.
- Suggested fix: Either generate a real back-of-book index (LaTeX `\index` or a term-to-section table) or drop the sentence and the file. Write the Acknowledgments (a few lines are enough) or delete the heading.

**TL-04 Glossary is incomplete and many definitions are not definitions**
- Where: Appendix A (`book/appendices/appendix-a-glossary.md`).
- Quote: `audit log | The health check (Chapter 30) is the light, metrics are the gauges, and the audit log is the trip log.` / `Assertions | Assertions are the methods that do the throwing.` / `magic number | This is a magic number check: many file formats begin with a short recognizable marker.` / `absolute lifetime | SessionLifetimeFilter adds an absolute lifetime: ...` / `fixture | DOC is a fixture: ...` / `digest | image: mysql:8.4@sha256:... names the image, its tag, and a digest ...`
- Problem: The house rule (STYLE, brief section 2) is that every bolded term is defined once and appears in Appendix A. The glossary has 273 rows; the chapters bold well over 1,000 spans. Core terms bolded and defined in the text are missing (**unit test**, **integration test**, **system test**, **UUID**, **JPA**/object-relational mapping, **XSS**, **CORS**, **SBOM**, **metric**, **reverse proxy** as a term). Several definitions were mechanically extracted from the sentence containing the term and read as fragments, analogy sentences (audit log) or code lines (fixture, digest, interpolation, lifecycle hooks); a few are circular. "Git ... is the most widely used one in the world" is an unsourced claim. "unit test" is bolded as a definition twice (Ch 18 and Ch 24), against the bold-once rule. "Assertions" is capitalized inconsistently with the rest.
- Suggested fix: Have the author rewrite the 30-40 weakest definitions by hand (list above, plus the "circ" ones: branch, index, role, owner, origin, proxy, schema), add the missing terms, and delete the "Generated from" line. Un-bold the second definition of "unit test".

**TL-05 Operability: reader cannot run the end-to-end tests as written**
- Where: Chapter 24 (Section 24.x on Playwright), Chapter 20 Exercise 20.1, Appendix D and E; Setting up your machine.
- Quote: (absent) The only appearance of the browser download step is inside the CI listing in Chapter 36 (`npx playwright install --with-deps chromium`).
- Problem: Chapter 24 says to run the tests against the full stack and shows `playwright.config.ts`, but nowhere tells a local reader to run `npx playwright install chromium` (checked: `git show book-m6-final:frontend/package.json` defines `"e2e": "playwright test"` only; the config uses the bundled Chromium `Desktop Chrome` device, not the host Chrome). A first `npm run e2e` fails with "browser not installed". It also needs `E2E_ADMIN_PASSWORD` set to the first-run admin password, which Chapter 24 mentions only obliquely (the book must not print the value, but should say where to get it: the startup log or the `.env` bootstrap setting).
- Suggested fix: In Chapter 24 add the exact sequence: `cd frontend`, `npm ci`, `npx playwright install chromium`, set `E2E_ADMIN_PASSWORD` (from the Compose stack's bootstrap setting), `docker compose --profile full up -d --build`, `npm run e2e`. Add the same to Appendix D and an Appendix E entry for "browser not installed".

**TL-06 Setup chapter contradicts Table IV.3 and the Part IV exercises on Maven and Java**
- Where: `book/front-matter/c-setting-up-your-machine.md` Step 3; Table IV.3; Chapters 25-29 Try it.
- Quote: "You don't install Maven: the project's Maven wrapper (`./mvnw`) downloads the right version" versus Table IV.3: "JDK 21 and an installed Maven (`mvn spring-boot:run`); no Maven wrapper" and "At the earlier tags, install Maven yourself".
- Problem: Setup installs only JDK 25 and no Maven; Chapters 25-29 (m0-m4) exercises and the table need JDK 21 plus a separately installed Maven 3.9.x. The setup chapter never mentions a second JDK, how to switch (`JAVA_HOME`), or that the wrapper is missing before m5, and Table IV.3 is not pointed to from the Setup chapter (the earlier "Ch 25/26 pointer to Table IV.3" item is fixed in Part IV, but a beginner who did Setup exactly cannot run Chapter 25). The exercises that ask the reader to run an old tag are therefore not reproducible from the book's own setup.
- Suggested fix: Add a boxed "If you want to run milestones 0-4" note to Setup (JDK 21 from Temurin, Maven 3.9.x install per OS, or simply read those tags with `git show`) and reference Table IV.3. State in Chapters 25-29 which exercises are read-only.

**TL-07 Required "Text description" line exists for only 6 of about 70 figures**
- Where: whole book; all Mermaid figures; only the six blueprint figures in Appendix B carry it.
- Quote: `*Figure: Blueprint v0. Text description: ...` (6 hits in `manuscript.md`; 0 for Figure 1.1, 36.1, etc.).
- Problem: The brief (section 2, figures) and the accessibility requirement make the `*Text description: ...*` line mandatory. 69 numbered figures in chapters have only a caption (which the build reuses as alt text). Also the Appendix B captions use a different form (`*Figure: Blueprint v0. ...*`, unnumbered) from `*Figure N.M — ...*`. The Accessibility reviewer will also raise this; I record it because it is the biggest gap against the book's own convention.
- Suggested fix: Add a one-to-three sentence Text description under every figure (the mechanism is already in the surrounding prose) or, if the decision is that caption-as-alt is enough, remove the requirement from STYLE and the brief and make the Appendix B figures numbered.

**TL-08 Unverified process claim: CI "blocks the pull request"**
- Where: Ch 36, before Figure 36.1 (manuscript line 20664); also Chapters 7 and 31 wording about gates.
- Quote: "Any red node fails the run and blocks the pull request."
- Problem: The repository is private on a plan that does not offer branch protection (`gh api .../branches/main/protection` returns "Upgrade to GitHub Pro or make this repository public"), and no chapter says required checks were configured. A red run does not by itself block a merge. This overstates the team practice and could mislead a reader into thinking CI is an enforced gate here.
- Suggested fix: "Any red job fails the run and shows a red mark on the pull request. Whether that also blocks merging depends on branch protection (required status checks), which this project's private repository could not enable; on your own repository, turn it on." Add one sentence in Chapter 7 or 36 that a merge gate is a setting you configure, not a property of CI.

## Minor

**TL-09 Table caption placement inconsistent (house rule: caption above, bold)**
- Where: Tables 1.1, 1.2, 2.1-2.3, 3.1, 4.1-4.4, 6.1-6.3, 7.1, 8.1-8.2, 9.1-9.2, 21.1 and about 23.1-23.2 (23 tables), Appendix D/E tables; How to use this book "Table 1 - The milestone tags".
- Quote: `*Table 1.1 — The parts of the app and the tools that build them*` placed after the table.
- Problem: 23 tables use an italic caption below, 65 use a bold caption above; How to use this book numbers its table "Table 1" (no chapter prefix); Tables 27.1 and 31.2 first appear as a bold stub "**Table 27.1** shows ..." immediately before the real caption, which duplicates the label in the PDF.
- Suggested fix: Move the captions above in bold, change "Table 1" to "Table A.1" or drop the number, and remove the bold stubs before Tables 27.1 and 31.2.

**TL-10 Ch 32 Prerequisites refer forward**
- Where: Ch 32, `book/part-5-production/32-security-review.md` line 29.
- Quote: "Chapter 33 teaches nginx and Caddy properly. This chapter needs only the idea that they are programs standing between the browser and the app."
- Problem: `bookcheck.py` flags a prerequisite of a later chapter. It is glossed, so this is acceptable in substance, but it sits inside Prerequisites, where the house rule is earlier chapters only. Also line 107 says "nginx (Chapter 30)" while the frontend/nginx chapters are 22 and 33.
- Suggested fix: Move the sentence to the start of the Beginner tier as "A note on terms" and check the "Chapter 30" reference (nginx as the API front end is first taught in Chapter 22/33).

**TL-11 Part IV chapter shape differs from the template**
- Where: Ch 25-31 (order: Advanced, Common mistakes, In this project, Try it, Architecture blueprint, Decisions and challenges, Summary); Ch 26 has "In this project" as a `###` subsection (26.15) rather than a `##` heading.
- Problem: The template puts Blueprint and Decisions between Advanced and In this project, and Try it before Summary. Here Try it precedes the blueprint, and Ch 26's "In this project" is not a top-level section so it does not appear in the contents.
- Suggested fix: Either reorder or record the actual order in STYLE and How to use this book; promote 26.15 to `##`.

**TL-12 "above" and "below" used as cross-references (house rule: never)**
- Where: about 85 occurrences, for example Ch 2 line 885 ("The `>` above is redirection"), Ch 4 ("Callers above it"), Ch 1 line 624; Setup ("the version numbers below").
- Problem: STYLE forbids "above/below" in favor of "Listing N.M" or "Section N.M". Many are harmless spatial uses (file paths `..` "the folder above"), but at least 20 are cross-references.
- Suggested fix: Search for `\b(above|below)\b` outside code and replace with the labelled reference.

**TL-13 Tone words**
- Where: "simply" x12 (Ch 7, 26, 29 and others), "easy" x19, "just" x61, "obviously" x1, "robust" x1 (Ch 26 line 14267, "older, robust defense").
- Problem: The word list bans "simply", "just" (as minimizer), "easy", "obviously", "robust". Some uses are literal or quoted, most are not.
- Suggested fix: Sweep and rewrite, keeping only literal or quoted uses.

**TL-14 Title page and front matter lack edition information**
- Where: PDF p. 1, `header.tex`/`build.py` metadata.
- Problem: Title page shows only the title and subtitle. No author or owner line, no edition/date, no statement of which tags and versions the book describes (Boot 4.1.1 / Java 25 / Angular 22 at `book-m6-final`), no license/copyright or "code is in a private repository" line. PDF metadata `author` is empty. For a delivered book this is the first page a reader sees.
- Suggested fix: Add author/owner line, version and date (for example "Describes `book-m6-final`, September 2026"), and a short copyright and license page; set PDF `author`.

**TL-15 Contents is 14 pages and shows every tier heading**
- Where: PDF pp. 2-15; 599 bookmark entries (Part IV-VI chapters list ten or more H2 rows each; Appendix C lists 39+22 rows).
- Problem: The tier headings ("Beginner tier", "Intermediate tier", "Advanced tier", "Learning objectives", "Prerequisites", "Summary", "Further reading") repeat for every chapter and bury the chapter titles; a reader cannot scan the chapter list.
- Suggested fix: `tocdepth` 1 (chapters and parts) in the front contents, with a separate detailed contents online (EPUB/HTML) if wanted. Layout is another team's concern; I record it because it affects usability of the learning path.

**TL-16 Exercise titles differ between chapters and Appendix C for Chapters 20, 21, 23, 24**
- Where: Ex. 20.1-20.5, 21.1-21.4, 23.1-23.5, 24.1-24.5 (18 exercises).
- Quote: Ch 20.1 "Which script needs more than Node?" versus Appendix C "Which script needs a stack"; Ch 23.5 "Keys and text fields" versus "Typing a page number"; Ch 24.1 "Add a boundary test" versus "Add a timeout test".
- Problem: Different titles make it hard to match solution to exercise, and the pair 24.1 hints at a different task ("boundary" versus "timeout"). Content otherwise matches (232 exercises, 232 solutions, none missing, no extras, star levels agree).
- Suggested fix: Use the chapter's title verbatim in the solution file for these four chapters, and check that 24.1's solution answers the exercise as worded.

**TL-17 Glossary "first defined" column mixes chapters**
- Where: Appendix A rows such as `audit log | ... | Ch 35` while `audit trail` is `Ch 1`; `annotation (Java) | Ch 4, used in Ch 11`.
- Problem: The first-defined chapter is where the extractor found the sentence, not where the term first appears in bold; "audit log" (used from Chapter 1) is dated Ch 35.
- Suggested fix: Regenerate "first defined" from the first bold occurrence after fixing TL-04.

**TL-18 Glossary/index terms file and appendices not consistent with the "bold at first use only" rule**
- Where: `unit test` (Ch 18 line 8838 and Ch 24 line 12307), `regression test`, `end-to-end test` appear bold in more than one chapter.
- Problem: Repeated bold definition dilutes the rule and creates duplicate glossary candidates.
- Suggested fix: Un-bold the later occurrence and add "(Chapter 18)" as a pointer.

**TL-19 Chapter 30 is much longer than the Part IV target**
- Where: Chapter 30 is about 8,100 words (`bookcheck.py` reports it within range, but it is the outlier; Part IV target is up to 10,000), 20 pages in the PDF (pp. 432-451).
- Problem: Not a defect against the stated limit; noted because the difficulty ramp peaks here and the pacing of Chapter 30 is the densest in the book (six gates, nine tiers of review rounds). A beginner may stall.
- Suggested fix: Consider a "Chapter 30 at a glance" box at the start of the Advanced tier with the six gates in one line each.

**TL-20 Chapter 13 mixes tags in one chapter**
- Where: Ch 13 "A note on versions" (m3 for most listings, m6 for four).
- Problem: Handled and explained, so within the accepted exception; but Chapter 14 is also tagged m2 while the file `book/part-2-backend/14-jpa-and-flyway.md` quotes m6 sweeps (Table 14.2). Readers checking out the tag in the chapter header (`book-m2-documents`) will not find `TileRateLimiter.sweep`.
- Suggested fix: Add the same explanatory sentence Chapter 13 has to Chapter 14 near Table 14.2.

## Checked and found correct

- Structure: every chapter 1-36, 38, 39 has Learning objectives, Prerequisites, three tier headings, Summary and Further reading (Ch 37 exempt; Ch 26's "In this project" is an H3, see TL-11). 39 chapters, six parts, front matter and five appendices in `order.txt` are in the right order.
- Prerequisite ordering: `bookcheck.py` reports one forward reference only (Ch 32, TL-10). Beginner tiers of Chapters 1-5 introduce almost no undefined tooling (checked Angular, Spring, Flyway, Maven, Docker, SQL, HMAC, JPA counts; each mention is one-line glossed).
- Exercises: 6 per chapter for 38 chapters (232 total), at least one ★ and one ★★ in every chapter, ★★★ present; Appendix C has a solution for every exercise, no duplicates and no orphans; all Part IV chapters name a tag; the "own branch, no credentials" rule is stated in How to use this book.
- Numbering: across the whole `manuscript.md` no duplicate or out-of-order figure or listing numbers, no gaps in Listings or Figures per chapter, no undefined Figure or Listing references (294 listings, 69 figures, 65 tables with above-captions). `xref.py` reports no broken Section or Chapter references. Sections N.M are continuous.
- QA window 9 items re-verified as fixed: Listing 21.6a is gone; "five gates later" is now "six gates"; Table 14.2 says six sweeps and lists six; Ch 38 refers to "Section 38.10/38.13/38.14" and those headings exist; Ch 13 Prerequisites no longer point to Chapter 16; the FileOperations "two seconds" discrepancy is now explained (about four seconds, and I confirmed `50L << Math.min(attempt, 4)` at `book-m6-final`); Ch 32-37 headers no longer say `expanded-draft` (`status: expanded`); Ch 5's analogies carry an "Unlike ..." limit.
- Versions and process facts against the repo: Boot 3.3.4/Java 21/PDFBox 3.0.3 at m1-m4, Boot 4.1.1/Java 25/PDFBox 3.0.8 at m5-m6; Angular `^22.1.0`, TypeScript `~6.0.2`, `packageManager npm@11.19.0` from m1; Vitest `^4.0.8` through m5 and `^5.0.1` at m6; Maven 3.9.16 in the wrapper; `tile-rate-limit-per-window: 180` at m6 (Ch 13 correctly says 120 earlier); `mvnw` first at m5; CI has exactly the four jobs, `needs: [backend, frontend]` on e2e, `permissions: contents: read`, `concurrency` cancel-in-progress, pinned action SHAs, Playwright with Chromium, Trivy on images, OSV scan (Table 36.1 and Figure 36.1 match `ci.yml`). Table IV.3 matches the repository contents at each tag (files present at m1/m4/m5; `.env.example` at m1+).
- Test counts (45, 56, 66, 68, 114 backend; 4, 6, 6, 14, 31 frontend) are consistent between Table IV.2 and the chapters; 114 and 31 are used everywhere.
- Prose: no gmail address, no personal name, no Claude/Anthropic/model names, no `dossier`, `TODO`, `TBD`, `expanded-draft`, `QA window`, or `agent id` in the built manuscript or PDF; the AI-agent reviewer disclosure appears once early and then consistently ("an AI review agent"), never presented as human. British spelling "colour"/"behaviour": none. No emojis observed.
- PDF: text extraction found no raw Markdown (`**`, backticks, `<!--`) and no `mermaid` fences; 73 images embedded; printed page numbers agree with the contents (front matter roman, body arabic, Chapter 1 printed page 13 = PDF page 28); figures and listings legible at 55-65 dpi (Figure 36.1, Listing 36.2 sample).

## Not checked

- Listing-by-listing exactness against `git show` (Senior Full-Stack Engineer's lens; I ran no `listingcheck.py`).
- Blueprint and figure correctness, Chapter 37 trade-off reasoning, architecture and security claims (Architect's lens). I read Chapters 1, 13, 14, 18 (tables), 24 (parts), 26 (outline), 32 (prerequisites) and skimmed the rest by search, not end to end; I did not re-read all 250,000 words.
- Whether every claim in the history sections traces to the dossier or a PR (sampled only: test counts, the nine-concurrent-password probe, the time-zone bug, the X-Forwarded-For fix, PR 5 review rounds appear consistently across chapters 2, 5, 14, 16, 22, 24, 30).
- Accessibility of the built PDF, EPUB and HTML (contrast, tagging, EPUB metadata): Accessibility reviewer's lens. I did not open the EPUB or HTML.
- Whether Table IV.3's "Secrets file" cell and the `.env` instructions match `.env.example` in every field; no secrets were opened.
- Command correctness per OS in Chapters 2, 7, 10 and Setup beyond the Maven/JDK/Playwright points above; I did not run any command or the app.
- Appendix B and D contents beyond headings, the epilogue beyond its opening 60 lines, and the index-terms file beyond its head.
- Page flow beyond the pages listed above; layout polish is out of scope.
