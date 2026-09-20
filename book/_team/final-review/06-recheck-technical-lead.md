# Recheck 06: Technical Lead (TL-01..20 and P7-01..18)

Evidence base: current sources under `book/`, the current `book/build/out/manuscript.md` (14:24), and the rebuilt `secure-doc-viewer-guide.pdf` (750 pages, 14:45), plus the EPUB and HTML (14:24-14:25). Checks were run by script (`bookcheck.py`, `xref.py`, my numbering script, PDF text and bookmark extraction with pymupdf) and by reading. No chapter was edited and nothing was committed.

## Result

| Status | Count |
|---|---|
| Fixed | 29 |
| Partly fixed | 9 |
| Not fixed | 2 |

Still open, by severity: **Blocker 0, Major 2, Minor 7.**

One new problem matters more than any old one: **the built PDF, EPUB and HTML do not match the sources for the About this edition page** (NEW-1 below). Because of it, the title-page metadata and licence work is only partly delivered.

## Open items (what still needs action)

**Major**
- **NEW-1 / TL-14 (Partly): built files show a stale About page.** The source `front-matter/00-about-this-edition.md` and `manuscript.md` have rows "Author: Claude (Anthropic)", "Edition: First edition, version 1.0" and "Copyright and licence". The built PDF (page 5), HTML and `build/out/diagrams/rendered-pdf.md` instead show "Book version: Draft edition (see the build date printed with this edition)" and no author, edition or copyright/licence row. `rendered-pdf.md` and `manuscript.md` were written in the same second (14:24:20) with different About tables, so the build writes the PDF/HTML/EPUB version from a different template or stale metadata (the filled values are in `build/out/metadata-filled.yaml`, not `build/metadata.yaml` alone). The PDF title page does carry the author line and the PDF metadata has the author, title, subject and keywords, so only the About page is wrong. Fix: rebuild with the filled metadata and confirm page 5 of the PDF reads "First edition, version 1.0" and shows the copyright and licence line.
- **P7-12 (Not fixed, made worse): Part VII chapters grew, not shrank.** Chapter 40 body is now 9,681 words plus a 391-word Sources list (10,072 total; was 8,058), Chapter 41 is 7,414 plus 246 (was 5,292). Seven paragraphs in Chapter 40 and five in Chapter 41 are over 200 words, the longest 373 and 490 words. Chapter 40 is now above the Part IV ceiling of 10,000 words. The additions (keep-alive, restore-drill, retention, split-brain and alternatives material) are useful, but the request to trim, split and move attribute-level detail out of the Intermediate tier was not carried out. Fix: split the 5-7 longest paragraphs; move the attribute names and quota figures to tables.

**Minor**
- **TL-03 / P7-05 (Partly): the index is present but weak.** `appendices/index-terms.md` is now in `order.txt` and prints as "Index" (PDF p. 736-750, 441 entries, one bullet per term). It lists chapter numbers, not page numbers, with the defining chapter in bold. Long entries are cut with "and others" (for example "session: Chapters **1**, 4, 8, 11, 12, 14, 15, 16, 17, 18, and others", "tile: Chapters 1, 2, ... and others"), so it cannot answer "where is this discussed". Some entries have no bold at all ("access analyzer: Chapter 41", "actor: Chapters 4, 32", "tile"), although the preface says the bold marks the defining chapter. It is usable as a term-to-chapter finder, not as a back-of-book index. Fix: either full chapter lists for terms with 12 or fewer chapters and a note that a longer list gives the key chapters only, or page numbers.
- **TL-04 / TL-17 (Partly): glossary quality is good, "first defined" is wrong for some rows.** 482 rows, alphabetically sorted, no duplicates, and definitions are one clear sentence (I read 40 at random: `framework`, `relaxed binding`, `atomic`, `lambda`, `JPQL`, `audit log`, `task role`, `open redirect`, `point-in-time recovery` and others; none was a fragment, an analogy or a code line). The earlier fragments (audit log, Assertions, magic number, digest, fixture) are rewritten. Missing terms (unit test, integration test, UUID, SBOM, XSS, CORS, ARN, KMS, EventBridge Scheduler, AWS Backup, Managed Service for Prometheus) are now present. Remaining defects: 26 rows have no matching bold term in any chapter (for example `absolute path`, `checked exception`, `RPO`, `RTO`, `stopTimeout`, `S3 Block Public Access`, `secret rotation`), so the glossary claim "terms this book bolds" is not literally true for them; and some "first defined" values name the first mention, not the definition: `task` says Ch 2 although the only bolded definition is in Ch 40 (section 40.2), `bucket` and `CDN` say Ch 25 (defined in Ch 40), `CIDR` says Ch 35 (Ch 40), `certificate authority` says Ch 30. Fix: regenerate "first defined" from the first bold occurrence; bold the 26 terms where they are defined or drop the row.
- **TL-12 (Partly): "above/below".** 32 "above" and 12 "below" remain in prose (was 58 and 27). The sampled remaining cases are mostly quotations ("scheduled backups as above", "the check above") or spatial meanings; a few real cross-references may remain. Fix: a last search for `above|below` next to figure/listing/table/section words.
- **TL-13 (Partly): tone words.** "simply" 3 (was 12), "easy" 5 (was 19), "obviously" 1, "just" 10 (was 61), "robust" 0. The remaining hits should be checked one by one against the word list.
- **TL-09 (nearly fixed): one bold stub remains.** Table 25.1 is announced by a bold "**Table 25.1** condenses it." in the prose just before its caption; Tables 27.1 and 31.2 are fixed. Fix: make it plain text.
- **TL-19 (Not fixed, optional): no "at a glance" box in Chapter 30.** The chapter is still the densest; no pacing aid was added.
- **NEW-2 (minor, from checking P7-13): star levels are in words only in Part VII.** The PDF shows the "Level: one star" line only for Part VII (12 hits in the PDF text); Chapters 1-39 show ★ symbols with no words in the printed text, although the fix batch said the build adds the words for screen readers. If that is in the PDF's accessibility layer it is fine; visually the book is still inconsistent (P7-13).

## Status of every finding

### TL-01 to TL-20 (03-technical-lead.md)

| ID | Status | Evidence |
|---|---|---|
| TL-01 Appendix C structure | **Fixed** | `appendix-c-exercise-solutions.md`: 1 H1, 41 H2 ("Chapter N solutions", N = 1..41), 242 H3, 0 H4. PDF bookmarks: one "Appendix C" with 41 chapter entries, no duplicates, no stray top-level "Solutions:" entries; PDF pages 681-734 carry a running header and no blank orphan pages (only sparse pages in the whole PDF are two "Further reading" pages, 150 and 587). |
| TL-02 internal file names | **Fixed** | Manuscript and PDF: 0 hits for `_team`, `requests.md`, `OUTLINE.md`, `solutions.md`, `GLOSSARY.md`, `book/blueprints`, `dossier`, `in-review`, `TODO`. Ch 25-31 point to Appendix C. |
| TL-03 index / Acknowledgments | **Partly** | Acknowledgments written (thanks to open-source projects, states the author is Claude, an AI, working with the project owner; no personal names; no empty heading). Index present in `order.txt` and in the PDF (441 entries) but chapter-level only with truncation (see above). The how-to-use promise ("lists, for each term, the chapters where it appears in bold") is now true in wording. |
| TL-04 glossary quality and gaps | **Partly** | See above: quality fixed, missing terms added; first-defined and unbolded terms remain. |
| TL-05 Playwright / e2e | **Fixed** | Ch 24 now has `npx playwright install chromium`, `E2E_ADMIN_PASSWORD='<your-admin-password>' npm run e2e` (bash) and the PowerShell form, with a placeholder, and the Linux `--with-deps` note. |
| TL-06 Setup vs older tags | **Fixed** | Step 3 now says the wrapper is for `book-m5-platform` and `book-m6-final`, and a Note says m0-m4 need JDK 21 and a self-installed Maven 3.9, can be read with `git show`, and points to Table IV.3. |
| TL-07 Text descriptions | **Fixed** | 77 numbered figures plus Part introduction figures: 84 captions, 84 followed by a "Text description" (0 missing). |
| TL-08 "blocks the pull request" | **Fixed** | Ch 36 now: GitHub shows the failed status on the pull request; whether it blocks merging depends on branch protection or rulesets, and states that the project's private repository could not enable it. |
| TL-09 table caption placement | **Fixed** (one stub) | 0 italic captions below tables (was 23); "Table 1"-style captions in the front matter replaced by Table I.1, II.1, III.1, V.1, VI.1, VII.1; duplicate stubs for 27.1 and 31.2 gone; one stub remains for 25.1 (Minor above). No duplicate or undefined table, figure or listing numbers (`n.py`: 0 DUP; `xref.py`: clean). |
| TL-10 Ch 32 prerequisite forward reference | **Fixed** | `bookcheck.py` no longer reports it (2 problems left, both the front-matter placeholder "Ch0"). |
| TL-11 Part IV template order | **Fixed** | Ch 25-31 order is now: Common mistakes, Architecture blueprint, Decisions and challenges, In this project, Try it, Summary, Further reading; Ch 26's "In this project" is an H2. |
| TL-12 above/below | **Partly** | 44 left (from 85), few are cross-references. |
| TL-13 tone words | **Partly** | See counts above. |
| TL-14 title page and edition information | **Partly** | PDF title page: title, subtitle, "Claude (Anthropic)". PDF metadata: author, subject, keywords, PDF 2.0. But the printed About page lacks Edition and Copyright/licence (NEW-1). Source and manuscript are correct. |
| TL-15 contents length | **Fixed** | Contents is 3 pages (pp. 2-4), 59 top-level entries, bookmark depth 3; chapters can be scanned. |
| TL-16 exercise titles vs Appendix C | **Fixed** | 242 exercises and 242 solutions, 0 missing, 0 extra, 0 title mismatches (was 19). |
| TL-17 first-defined column | **Partly** | Same as TL-04. |
| TL-18 repeated bold definitions | **Fixed** | "unit test" is bold once in the chapters (Ch 18). I did not re-audit every other term. |
| TL-19 Ch 30 pacing | **Not fixed** | No at-a-glance box; optional. |
| TL-20 Ch 14 tag note | **Fixed** | Ch 14 line 394: "This chapter's own tag, `book-m2-documents`, has only three of the six" and Table 14.2 is labelled `book-m6-final`. |

### P7-01 to P7-18 (05-part7-technical-lead.md)

| ID | Status | Evidence |
|---|---|---|
| P7-01 source comments invisible / Exercise 40.2 | **Fixed** | Both chapters end with a visible "## Sources" list (391 and 246 words), statements carry "(source N)" tags, the callouts explain them, and Exercise 40.2's hint points to "(source N) tags ... at the end of the chapter"; "source comment" no longer appears in the reader text. |
| P7-02 "Listing" vs "Example" | **Fixed** | 0 "Listing" captions containing "illustrative" or "not in the repository"; Examples 40.1-40.3 and 41.1-41.4 carry the labels (seven, one more than before: a new nginx sketch). |
| P7-03 Ch 37 health check | **Fixed** | Ch 37 Step 6 now says "point its health check at a health path (Chapter 40, section 40.5, explains why the readiness path, not the plain `/actuator/health` ...)" and 37.13 line 303 was adjusted. |
| P7-04 Appendix C H1s | **Fixed** | See TL-01. |
| P7-05 index / acknowledgments | **Partly** | See TL-03. |
| P7-06 glossary | **Partly** | See TL-04; AWS rows accurate and now include ARN, KMS, EventBridge Scheduler, AWS Backup, Managed Service for Prometheus. |
| P7-07 duplicated sentence | **Fixed** | The callout contains the sentence once. |
| P7-08 AWS terms not in glossary, double bold | **Fixed** | Rows exist; "Task role and task execution role" merged into one list item in 40.2 and glossary lists them as Ch 40 (the definition is now in Ch 40 where they are first used). One residual: `task` still says Ch 2 (see TL-04). |
| P7-09 sidecar, CDN, access analyzer | **Fixed** | "Sidecar" and "CDN" are defined in the 40.2 term list before use (lines 49-50); "access analyzer" is defined in place ("a tool that evaluates bucket and role policies and flags access you did not intend (source 20)"). |
| P7-10 wrong location / attribution | **Fixed** | No "at the end of this chapter"; the last-address rule is attributed to "Chapter 40, section 40.6". |
| P7-11 HSTS claim | **Fixed** | Table 40.1 now says "the ALB adds no HSTS unless you configure it" (softened to what the docs say; see source 40.5). |
| P7-12 Chapter 40 length and density | **Not fixed** | Grew (see above). |
| P7-13 exercise format inconsistent | **Partly** | Still only Part VII shows "Level" lines; the build adds words for screen readers (verify in the accessibility review). |
| P7-14 Text description only in Part VII | **Fixed** | Same as TL-07. |
| P7-15 "closes the book" | **Fixed** | No matches in OUTLINE, Chapter 39 or the preface. |
| P7-16 Terraform / Further reading | **Fixed** | Ch 41 Further reading now lists the Terraform AWS provider documentation, IAM User Guide and EventBridge Scheduler guide. |
| P7-17 Part VII opener | **Fixed** | Opener now has "What the part covers", "An honest frame", "Before you start" (Chapters 32-37, 39), "How to read this part", "What you will have"; Table VII.1. |
| P7-18 `status: in-review` | **Fixed** | All Part VII files say `status: expanded`, `owner: writer-production`. |
| P7-19 Ch 33 "See also" placement | **Fixed** | It now follows the checklist (line 403), before 33.15. |
| P7-20 Ch 32 forward ref and titles | **Fixed** | See TL-10 and TL-16. |

(P7-19 and P7-20 were in my file although the request lists P7-01..18; they are included for completeness.)

## Other requested checks

- **Front matter order (PDF):** About this edition (p. 5), Preface, How to use this book, Setting up your machine, then Part I. About page shows the wrong edition/licence rows (NEW-1).
- **Part introductions:** Table I.1, II.1, III.1, V.1, VI.1, VII.1 and Figure II.1 all exist with the roman-numeral scheme; no "Table 1" or "Figure 1" remain. The `n.py` check lists Figure II.1 as "undefined" only because my script matches numeric figure captions; the caption is present.
- **Internal-name leaks:** none in the PDF text (all patterns searched: 0 hits).
- **Numbering across the book:** 77 figures, 302 listings, 103 tables; no duplicates or gaps in the checked series; listing/example naming consistent.
- **Not re-checked in this pass:** every technical claim in the new Part VII material (AWS claims remain unverified by me), the EPUB/HTML beyond the About page, and the other reviewers' findings.

---

# Closure check (final PDF 747 pages 17:49, EPUB and HTML 17:26-17:27, current manuscript)

Method: PDF text and bookmarks (pymupdf), EPUB and HTML contents, the current sources, `bookcheck.py`, `xref.py`, my numbering script, word and paragraph counts. No edits, no commits.

## Item by item

| Item from the recheck | Status | Evidence |
|---|---|---|
| NEW-1 / TL-14: About page rows in PDF, HTML and EPUB | **Closed** | PDF p. 5 now has a table with Author "Claude (Anthropic)", Edition "First edition, version 1.0", "Copyright and licence" (copyright 2026, all rights reserved; listings under the MIT License), Code version, Software versions, Language; p. 6 adds an "Accessibility" section. HTML contains "First edition", "copyright 2026" and "Accessibility" and no "Draft edition". EPUB `EPUB/text/ch001.xhtml` has the same three and no "Draft edition"; its package file lists `dc:creator` Claude (Anthropic) and the accessibility metadata (features, access modes, summary, hazard). PDF metadata author is set. A scan of the PDF for "Draft edition" finds nothing. |
| P7-12: Chapter 40 and 41 length and paragraph size | **Closed with a residue (Minor)** | Chapter 40 is 9,455 words including its 391-word Sources list (body 9,064; was 10,072 and 9,681), so it is back under the 10,000 Part IV ceiling; Chapter 41 is 7,186 (body 6,939; was 7,660). Paragraphs over 200 words (tables excluded): Chapter 40 has 5 (was 7), longest still 373 words; Chapter 41 has 4, longest 230. The trim helped but did not split the longest paragraphs; the chapters remain the densest in Part VII. Acceptable for publication because each tier carries a "skim on a first read" line. |
| TL-04 / TL-17: glossary, unbolded rows, first-defined | **Closed** | 480 rows, alphabetical, no duplicate terms. The earlier wrong first-defined values are corrected: `task`, `bucket`, `CDN`, `CIDR` now say Ch 40 (where the bold definitions are); `certificate authority` says Ch 33 (bold at 33.x). The 12 rows my matcher could not match to a bolded phrase (`absolute path`, `checked exception`, `unchecked exception`, `relative path`, `safe method`, `SPA fallback`, `secret rotation`, `off-site backup`, `reserve then compensate`, `stacked pull requests`, `session-based authentication`, `token-based authentication`) are bolded in shortened forms in the chapters (for example "**absolute**", "**checked**"); I found no glossary row without a bolded source. Row quality from my earlier 40-row sample stands. |
| TL-03 / P7-05: index | **Closed** | `index-terms.md` is in `order.txt` and prints as "Index" (from PDF p. 736, bookmark present). About 480-499 entries with full chapter lists, consecutive chapters as ranges ("Chapters 1–2, 8, 12–13, ..."), the defining chapter in bold, and "See also" cross-references (`Angular ... See also Angular CLI, interceptor, route guard, signal, template`; the "and others" truncation is gone: 0 occurrences). It still names chapters, not pages, which the index's own introduction says; for a 747-page book that is workable but less convenient. One wording slip: How to use this book says the index lists "the chapters where it appears in bold", while the index intro says bold marks the defining chapter. Cosmetic. |
| TL-12: "above/below" | **Partly, acceptable** | PDF body: 35 "above", 16 "below" (was 85 in total at the start, 44 at the recheck; the current count is 51 in the PDF because the PDF also contains the index and glossary text). Sampled hits are spatial, quoted from the README, or "the level below" in definitions. Not a publication risk. |
| TL-13: tone words | **Closed** | In the body (glossary excluded, code excluded): "simply" 1 (inside a quotation: "on startup we simply ask the server who we are"), "easy" 4 (all "easy to act on" or "easy" as a quality judgment in an error-message section), "obviously" 1 ("obviously wrong files", literal), "robust" 0, "just" 6, "easily" 0. None is a minimiser aimed at the reader in an instruction. |
| TL-09: bold stub before Table 25.1 | **Closed** | Manuscript line 13396 now reads "Table 25.1 condenses it." in plain text; the caption "**Table 25.1 — ...**" follows once. No duplicate table numbers in the whole manuscript (numbering script: 0 DUP, 77 figures, 302 listings, 104 tables; `xref.py`: clean; the "Figure II.1 undefined" line is my script's numeric-only pattern, the caption exists). |
| TL-06: Setup chapter note on older tags | **Closed** | The Note and a new "Optional: build the older tags (JDK 21 and Maven)" procedure: install JDK 21 next to JDK 25 (macOS `brew install --cask temurin@21`, Linux `openjdk-21`, Windows installer), install Maven 3.9 per OS, point one terminal at JDK 21 with `JAVA_HOME` (bash, PowerShell and cmd forms), and confirm with `java -version` and `mvn -version`; ends by pointing to Table IV.3. |
| Front-matter order | **Closed** | PDF bookmarks and contents: About this edition (p. 5), Preface (p. 7), How to use this book (p. 10), Setting up your machine (p. 13), Part I (p. 18), then the chapters; back matter ends with Appendix C (p. 677), D (p. 731), E (p. 734), Index (p. 736). |
| Contents length | **Closed** | Contents is 3 pages (pp. 2-4). No sparse or blank pages except two "Further reading" pages (151, 585). Appendix C has one H1 and 41 chapter entries, no stray "Solutions:" top-level entries. |
| Also re-checked: leaks, numbering, figures | **Closed** | PDF text: 0 hits for `_team`, `requests.md`, `OUTLINE.md`, `solutions.md`, `GLOSSARY.md`, `dossier`, `TODO`, `in-review`, `To be written`, raw `**` or `<!--`. Every figure caption is followed by a Text description (84 descriptions for 78 captions including the part-introduction figures; none missing). `bookcheck.py`: no chapter problems (only the front-matter "Ch0" placeholders). |

## Remaining, all Minor (none blocks publication)

1. Chapters 40 and 41 still contain a handful of 200-370-word paragraphs (longest 373 words, in Chapter 40); a further split would help beginners.
2. How to use this book: change "the chapters where it appears in bold" to "the chapters where it is used, with the chapter that defines it in bold".
3. The index gives chapter numbers only, not page numbers.
4. About 50 "above/below" remain in the PDF, most spatial or quoted; a last skim for figure, listing or table references is optional.
5. In the PDF the exercise stars are symbols only (the About page says so and says the exercise text states the level); the Level lines appear only in Part VII. This is disclosed, and the EPUB and HTML label the stars in words.
6. AWS statements in Part VII remain unverified by me (the Sources lists make them checkable by a reader or a later technical review).

## Verdict

**Ready to publish, with no must-fix items from my lens.** Every Blocker and Major from my first review and from the Part VII review is closed or reduced to the minor residues above; the delivered PDF, EPUB and HTML now agree with the sources on title, author, edition, licence and accessibility statement; structure (front-matter order, one Appendix C, 41 chapters, index, glossary) is sound; and no internal working text reaches the reader. I would ask the coordinator only to (a) fix the one-line index sentence in How to use this book if a last rebuild happens anyway, and (b) confirm with the author the copyright and licence wording on the About page, since I did not verify the intended rights statement. The technical claims about AWS need the separate technical review the pipeline already includes.
