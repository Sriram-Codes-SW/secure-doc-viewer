# QA sweep of the whole book after the fix batch (window 11)

Scope: `part-1` to `part-7`, `tradeoffs/`, `front-matter/`, `appendices/` (all 42 chapter files, Appendix A to E, index, epilogue, six part introductions plus Part VII). No file was edited. Tools: `listingcheck.py` (all chapters), and three new scripts in the scratchpad (`refcheck.py` for Listing/Example/Figure/Table/Section/Exercise references, `figcheck3.py` for figures, `leakcheck.py` for internal-name leaks). Owners are from the FIX-BATCH roster. Overall result: **0 blockers, 0 majors, 9 minors.** Nearly everything in the assignment checked out clean; the batch was applied well.

## Counts

| Item | Actual | Note |
|---|---|---|
| Chapters | 41 (Parts I to VII) | OUTLINE and README say 41 in seven parts: correct |
| Exercises | 242 in the chapters, 242 in Appendix C, titles identical | the coordinator's 242 is right; the front matter quotes no exercise count |
| Listings / Examples | 302 / 53 (`Listing N.M` and `Example N.M` are defined once each; no Listing/Example mix-ups) | illustrative Part VII code is now `Example 40.x/41.x` |
| Figures | 78 captioned (77 numbered plus the Part II opener, now `Figure II.1`) | the front matter quotes no figure count |
| Tables | 94 numbered | part openers now `Table I.1` to `VII.1` |
| Listings checked by script | 289 verified, 13 flagged, all explained below | |
| Heading-level skips | 0 | |
| Positional "above/below/following" references to figures, tables, listings | 0 real (3 numeric "above" hits in Ch 40 are comparisons, not positions) | |
| Figures with a `*Text description:*` line | 78 of 78 | |
| Figure captions in sequence, and each figure mentioned before it appears | all | |
| Reader-facing internal-name leaks (dossier, `_team`, `requests.md`, `OUTLINE.md`, `.solutions.md`, TODO, status headers, agent ids) outside HTML comments | 0 real | the two script hits are the word "status:" in a TypeScript sentence and the phrase "final-review rounds" |

## (1) Stale or wrong statements after the fixes

- **Graceful shutdown:** only Chapter 41 (Section 41.6) discusses it, and it is right: it says Spring Boot 4.1.1 at `book-m6-final` shuts down gracefully by default (30 seconds per phase), and that a three-minute render cannot be guaranteed to finish anyway. No chapter claims "no graceful shutdown" or "in-flight requests are dropped" for m5/m6, and none makes any claim about the m0 to m4 default, so nothing to fix. The Fargate `stopTimeout` maximum of 120 seconds is stated correctly in Section 41.6 and in the summary and glossary. The S3 403-versus-404 behaviour without `s3:ListBucket` is now stated in Section 40.8 and Section 41.2 and the policy Example 41.2 includes `s3:ListBucket`. Minor 1: the Section 40.8 bold lead-in reads "A missing tile must still be a 404", but the app's designed outcome is `410` (or a logged `500`), and the 404 is S3's; reword to "A missing tile must still reach the app as 'not found'".
- **`@Scheduled` counts:** Chapter 14 (Section 14.10, Table 14.2), Chapter 37 (Step 4 of 37.17) and Chapter 40 (Section 40.10) all say six, with the fully qualified `TileRateLimiter` annotation noted; Chapter 14 correctly says three of the six exist at `book-m2-documents` (audit purge, throttle sweep, janitor: verified against the tag). No stale "five" or "three" remains.
- **Minor 2 (stale reference, owner writer-app):** `part-4-building-the-app/27-m2-documents.md` line 323 still says "the integration test in Section 27.11". Section 27.11 is "Files, OneDrive and Windows locks"; the test is Section 27.14 ("The test that pins the behavior"). This was in my window-8 review and was not fixed.
- **Other fixes I re-verified as applied:** Chapter 30's "five gates" heading now says "six gates"; Chapter 38's "Section 38.12" pointers are gone; Chapter 39 table captions are bold above; Chapter 21's Listing 21.6a is gone; the `FileOperations` "about two seconds" comment is qualified as roughly four in Chapters 2, 17 and 27; Chapter 8 has 412; Example 12.2 now shows the cookie and CSRF sequence; Exercise 25.3 says to change a middle character; Exercise 29.4 fixes the line count; the setup chapter and Table IV.3 cover JDK 21 and Maven for m0 to m4, and Chapters 25 to 29 point to Table IV.3; Chapter 24 has `npx playwright install chromium`; Ch 32, 37 (Flyway "11 in the pull request text, 12 as resolved by Spring Boot 4.1.1"), 30 and the Part II introduction all say Flyway 12 correctly.

## (2) Renumbered items and cross-references

`refcheck.py` resolved every `Listing`, `Example`, `Figure`, `Table`, `Section` and `Exercise` reference (case-insensitive, including "and", "to" and comma lists) in all chapters, part introductions, solutions, front matter, appendices, OUTLINE, README, GLOSSARY and STYLE against the definitions. **0 undefined references and 0 Listing-for-Example confusions** (the only two flags are in `STYLE.md` line 383, where "Example 14.1" is used as an illustration of numbering). References to Listings 38.x, 40.x and 41.x, Figures 41.1 to 41.5, Tables 40.x and 41.x, and section numbers in Chapters 40 and 41 all resolve. "Section 40.4" in Chapter 40's solution and Appendix C resolves. The check cannot tell whether a reference that resolves still points at the right content; I spot-checked the Chapter 40/41 cross-references (Example 41.2 from Section 40.8, Section 40.8 from Section 41.2, Figure 41.5 and Table 41.2 from the solutions) and they are correct. The index (`appendices/index-terms.md`) is chapter-level only ("Chapters 16, **28**, 33"), so it has no section numbers to go stale; every chapter number in it and in the glossary is at most 41. Minor 3: Appendix A (glossary) has 482 terms, not the "309" the editor quoted in the FIX-BATCH; check that the number is not quoted anywhere reader-facing (I found none).

## (3) Listings 34.1, 34.2, 36.4 and every listingcheck flag

Captions of Listings 34.1 and 34.2 say "adapted from `README.md` ... passes the password through the environment" and explain the change; Listing 36.4's caption lists exactly what was changed (line breaks, backslashes, indentation, shortened digest). All 13 flags are explained and correct:

| Listing | Reason for the flag | Verdict |
|---|---|---|
| 10.6, 10.7 | image digests replaced by `<digest>` (stated in caption) | correct |
| 13.2, 18.2, 18.4, 26.13 | secrets replaced by placeholders (stated in caption; STYLE 16) | correct |
| 15.1 | `...` in the method signature (caption says other parameters omitted) | correct |
| 28.4, 31.1 | diff listings with `-`/`+` lines (caption says so) | correct |
| 28.6 | handlers reordered (caption says so) | correct |
| 34.1, 34.2 | adapted to `MYSQL_PWD` (caption says so) | correct |
| 36.4 | `docker run` line broken with backslashes, digest shortened (caption says so) | correct |

Minor 4 (owner writer-production): Exercise 34.2 (line 302) still asks the reader to explain the README's old form `-p"$MYSQL_ROOT_PASSWORD"`, while Listing 34.1 now uses `export MYSQL_PWD=...`. Either say "the README's form" in the exercise or use the adapted command, and check the solution in Appendix C (line about "`-T` disables the pseudo-terminal") says the same.

## (4) Figures

- All 78 captions are numbered in sequence within their chapter, each is announced in the text before the diagram, and every one has a `*Text description:*` line. Part introductions use `Figure II.1` and `Table I.1` to `VII.1` (renamed as requested).
- Source comments: `figcheck.py`'s "55 with none" is out of date. Now only **2 figures have no source comment** and **5 have a comment that names no tag**:
  - Missing: Figure 25.1 (Ch 25, needs `at book-m0-mvp`, describes the m0 request flow) and Figure 26.1 (Ch 26, sign-in and the two cookies, needs `at book-m1-accounts`). Owner: writer-app. **Minor 5.**
  - Comment without a tag: Figure 7.1 (git behaviour, no code, so the stated reason is acceptable; keep), Figure 39.6 (the book's own method, acceptable; say "no code" in the comment), and Figures 41.1, 41.2 and 41.4 (their comments cite only AWS documentation pages; each also describes the app, so add "app details at `book-m6-final`" or "design; not built"). Owner: writer-production. **Minor 6.**
  - The 40-series and other new figures (5.x, 6.x, 9.1 to 12.1, 13 to 24, 27 to 38, blueprints 28 to 31, 32 to 37, 38, 39, 40) all carry tagged comments; the earlier gaps listed in my window-9 review are closed.

## (5) PR #5 and Flyway 11 versus 12

Resolved. Chapter 37 (Section 37.16) says "Flyway (11 in the pull request text, 12 as resolved by Spring Boot 4.1.1)"; Chapters 30, 32 (no Flyway mention needed) and the Part II introduction, CHARTER and STYLE say Flyway 12. Chapter 7 says "pull requests 1 to 12 ... a documentation-only pull request, number 13, was merged after the tag"; Chapter 31 and the Part IV introduction say the same. Minor 7 (owner writer-app): Chapter 31 (Section 31.x, line ~489) still quotes the owner's private chat verbatim ("In their words: 'since it is an ultra review, would it not be beneficial...' Later: 'let us merge PR #5 ...'"), and Chapter 37.16 quotes "as new as long as it is a standard version". The dossier calls these private build conversation; paraphrase them (the earlier reviews of Chapters 30 and 37 said the same).

## (6) Internal-name leaks

None in reader text. Source information lives only in HTML comments (`<!-- source: ... dossier ... -->`), which the build does not render. The front matter and appendices no longer name `requests.md`, `OUTLINE.md` or `.solutions.md`. `README.md` (repository documentation, not in the book build) still says "Status: expanded draft" and points to `book/OUTLINE.md`; that is fine for a repository README.

## (7) Front-matter counts

The front matter quotes no exercise, figure, listing or table counts (grep for digits followed by these words found none), so there is nothing to disagree with the actual counts above. `00-about-this-edition.md` says Part VII is a design study, the preface and "How to use" mention Parts VI and VII correctly, and the epilogue's "seven parts" and its closing honesty paragraph ("The cloud design of Part VII was never deployed") are right. The build order (`book/build/order.txt`) puts the index after Appendix E as requested, and the epilogue after Part VII.

## Other minors found by the sweep

- **Minor 8 (owner editor/writer-frontend):** two bare generics outside code formatting: `function firstWhere<T>(items: T[], ...)` in `appendices/appendix-c-exercise-solutions.md` line 881 and `part-3-frontend/19-typescript.solutions.md` line 47 (`<T>` becomes an HTML tag in HTML/EPUB and disappears). Put the whole signature in backticks or a fenced block. (A11Y-04 said grep all chapters: these are the only two.)
- **Minor 9 (owner writer-production):** Chapter 36 (line 97) hedges "whether a failure also blocks..." merging, which is the right honest wording after the branch-protection finding; check the rest of the sentence says branch protection is not enabled on this private repository and that Chapter 36's summary bullets and the Part V introduction do not still say a red run "blocks" a pull request.

## Top issues (all minor)

1. `27-m2-documents.md:323` says Section 27.11; should be 27.14 (writer-app; open since window 8).
2. Exercise 34.2 uses the README's old `-p"$..."` form while Listing 34.1 is adapted to `MYSQL_PWD` (writer-production).
3. Figures 25.1 and 26.1 have no source comment; Figures 41.1, 41.2, 41.4 cite only AWS pages (writer-app, writer-production).
4. Verbatim owner chat quotes remain in Chapter 31 and Chapter 37.16 (writer-app, writer-production).
5. Two bare `<T>` generics in the Ch 19 solutions and Appendix C (editor, writer-frontend).
