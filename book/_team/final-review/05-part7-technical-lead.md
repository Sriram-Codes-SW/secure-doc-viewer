# Final review 05: Part VII (Chapters 40 and 41), Technical Lead lens

Scope: `book/part-7-cloud/00-part-introduction.md`, `40-aws-production.md`, `41-aws-operations.md` and their solutions files; their fit in the whole book; and a regression check of my earlier top findings (TL-01 to TL-04) after the editor regenerated Appendix A, Appendix C and the index. I read both chapters in full before opening `book/_team/reviews/40-*.md`, which I did not open. I worked from the sources: the built PDF, EPUB, HTML and `manuscript.md` in `book/build/out/` predate Part VII (written 06:51-07:00; `order.txt` and many sources are newer), so I built a scratch manuscript from `order.txt` at `book/build/out/review-scratch/ms.md` (gitignored) for numbering checks. PDF pages were not re-inspected because the PDF does not contain Part VII; a rebuild is needed before the layout, accessibility and PDF checks can be redone.

## Verdict

Part VII is well made and fits the book. It is honest about being a design, it is consistent with the repo at `book-m6-final` (7 of 7 checkable listings match `git show`; `listingcheck.py`, `figcheck.py` and `xref.py` report nothing wrong), its numbering is clean (Figures 40.1-40.3, 41.1; Listings 40.1-40.9, 41.1-41.4; Tables 40.1-40.2, 41.1-41.2, VII.1; Exercises 40.1-40.6, 41.1-41.4, all with solutions in Appendix C), all three tiers, Common mistakes, In this project, Try it, Summary and Further reading are present, every figure has a Text description, and the cross-references from Chapters 33, 34, 35, 36, 37 and 39 all point at sections that exist and say what the "See also" lines claim. The findings are about reader-visible promises the build cannot keep, a broken house convention, one contradiction with Chapter 37, and glossary gaps. The old top findings are not worse, but three of the four are still open.

| Severity | Count |
|---|---|
| Blocker | 0 |
| Major | 6 (3 new in Part VII, 3 carried from my first review) |
| Minor | 14 |

## Major

**P7-01 The chapters say each claim's documentation page "is named in a source comment", but the build removes source comments, so the reader can never see it; Exercise 40.2 cannot be done**
- Where: Part VII opener "An honest frame"; Ch 40 callout and Exercise 40.2; Ch 41 callout; `40-aws-production.solutions.md` Exercise 40.2. Build: `build.py` line 27 strips all `<!-- ... -->`.
- Quote: "the page is named in a source comment beside each claim" / "*Hint:* look at the listing captions and the source comments." / Solution: "the source comments beside the sentences name the documentation page and the date".
- Problem: Ch 40 carries 25 source comments and Ch 41 many more, and they are the only place the AWS documentation page for each statement is named. In the PDF, EPUB and HTML they do not exist. The reader is told to look for them, and an exercise depends on them. The visible evidence of the "checked against the official documentation" promise is therefore only the dated sentence and seven Further reading links.
- Suggested fix: Either add a visible "Sources for this chapter" list at the end of each chapter (page title, publisher, date checked), numbered and referenced inline as "(source 4)", or keep comments and remove every sentence that points the reader to them. Rewrite Exercise 40.2's hint to "look at the listing captions and the list of sources", and its solution likewise.

**P7-02 Teaching code is captioned "Listing", which the book defines as code copied from the repo at a tag**
- Where: Listings 40.5, 40.9, 41.1, 41.2, 41.3, 41.4.
- Quote: "**Listing 41.2 — An illustrative least-privilege policy for the task role (not in the repository)**".
- Problem: How to use this book and STYLE define "Listing" as tagged repo code and "Example" as code written to teach with no tag (44 Examples exist elsewhere). Six Part VII items are teaching sketches called Listings, with "(not in the repository)" tacked on, so the reader must read the caption to learn that the label they rely on does not apply. `listingcheck.py` reports them as "n/a (no tag or path line)". Later text calls them "Listing 41.2" as if they were project code (Common mistakes: "Fix: one prefix... (Listing 41.2)").
- Suggested fix: Rename to "Example 40.5", "Example 40.9" (a separate Example counter, as elsewhere), "Example 41.1" to "41.4", keep "illustrative, never run" in the caption, and update the in-text references and Summary/In this project rows. The four real-code listings 40.1-40.4, 40.6-40.8 stay as Listings.

**P7-03 Chapter 37's plan and Chapter 40 disagree on the load-balancer health check, and Chapter 37 has no pointer**
- Where: Ch 37 Step 6 (`tradeoffs/37-engineering-tradeoffs.md`, section 37.17); Ch 40 section 40.5; Ch 35 "See also".
- Quote: Ch 37: "point its health check at `/actuator/health`". Ch 40: "`/actuator/health` includes the database, so during a database failover every task would report `503`... Point the check at `/actuator/health/readiness`."
- Problem: Ch 40 says it builds on 37.17, and a reader following the plan of 37.17 first would configure the check Ch 40 warns against. Ch 35 and 37 both carry "See also" pointers into Part VII, but the one that matters here is missing at Step 6. Ch 40 also never says that it corrects Ch 37.
- Suggested fix: In Ch 37 Step 6 add "(Chapter 40, Section 40.5 explains why the readiness probe is the better target)" or change the step to the readiness path; in Ch 40 section 40.5 add "This corrects the simpler check of Section 37.17".

**P7-04 (carried, TL-01, partly fixed) Appendix C still has 16 stray top-level headings**
- Where: `book/appendices/appendix-c-exercise-solutions.md`.
- Quote: `## Chapter 1 solutions` immediately followed by `# Chapter 1 solutions` (Chapters 1-10 and 19-24).
- Problem: The regeneration removed the stray H1s for Chapters 11-18, 25-31 and 38-41 (there are now 42 `##` headings and 16 leftover `#`, down from 22), but Chapters 1-10 and 19-24 still emit both an `##` and an `#`. The new Chapter 40 and 41 sections are clean. In the PDF the leftovers still start new pages and appear twice in the contents.
- Suggested fix: Strip the H1 line from those 16 per-chapter solution files (or in the assembly script) and rebuild.

**P7-05 (carried, TL-03, unchanged) No index though promised; Acknowledgments still empty**
- Where: `front-matter/b-how-to-use-this-book.md` ("the index terms list helps you find things again"); `build/order.txt` (no `index-terms.md`); `front-matter/a-preface.md` line 93 (`<!-- To be written by the editor at final assembly. -->`). Part VII adds no index terms because `index-terms.md` is a bare word list.
- Suggested fix: as before: generate a real index or delete the sentence; write or remove Acknowledgments.

**P7-06 (carried, TL-04) Glossary quality unchanged; new AWS entries are good but incomplete**
- Where: `book/appendices/appendix-a-glossary.md` (309 terms, 36 added).
- Quote (unchanged, still fragments): `Assertions | Assertions are the methods that do the throwing.` / `audit log | The health check (Chapter 30) is the light...` / `magic number | This is a magic number check...`. Still missing: unit test, integration test, UUID, SBOM, XSS, CORS.
- Problem: The 36 new rows read as proper definitions (checked: Multi-AZ, S3, NAT gateway, VPC endpoint, IAM role, task role, Valkey, Lua script, S3 Versioning) and are accurate. The old weak rows and gaps remain.
- Suggested fix: hand-edit the 30-40 weakest rows and add the missing terms (see P7-08 for the Part VII gaps).

## Minor

**P7-07 Duplicated sentence in Chapter 40's opening callout**
- Where: Ch 40, "Read this first" callout.
- Quote: "Where a claim could not be verified, the text says so. Code marked "illustrative" is a sketch that was never run. Where a claim could not be verified, the text says so."
- Fix: delete the second copy.

**P7-08 Bolded terms with no glossary row, double bolding, and wrong "first defined"**
- Where: Ch 40 section 40.2 and Table 40.1; Ch 41.
- Problem: Bold terms with no row in Appendix A: **ARN**, **KMS**, **Amazon EventBridge Scheduler**, **AWS Backup**, **Amazon Managed Service for Prometheus**, **Elastic Load Balancing**, and **subnets** (only "subnet" exists, so it is fine). Bold more than once: **idle cost** (40.1 and 40.2), **AWS Backup** and **Amazon Managed Service for Prometheus** (Ch 40 Table 40.1 and Ch 41), **Multi-AZ** and **Amazon Route 53** (defined in a list and again in text). "task role" and "task execution role" are listed as first defined in Ch 40, but their definitions and bold are in section 41.2, and Table 40.1 uses "IAM task role" before that.
- Fix: add the missing rows, bold once, correct the "first defined" column.

**P7-09 Words used before they are glossed**
- Where: Ch 40 sections 40.4-40.5, 40.9; Ch 41 section 41.9.
- Quote: "In the sidecar layout that server is nginx" (40.5; "sidecar" is glossed only in 41.4); "a CDN" (40.5, 40.4 Common mistakes; glossed in 41.8); "use an access analyzer" (41.9, never explained); "CIDR form" (40.6); "DNS" (40.3 Table row, only "AWS's DNS service" in Route 53's gloss).
- Fix: gloss each at first use in one clause, in Chapter 40 for the first three.

**P7-10 Wrong location and misattributed rule**
- Where: Ch 40 section 40.3; Ch 41 Table 41.1.
- Quote: "Chapter 41, Figure 41.1, at the end of this chapter, shows a different order" / "Chapter 32's 'take the last address' rule".
- Problem: Figure 41.1 is in Chapter 41 (section 41.11), not at the end of Chapter 40. Chapter 32 teaches overwriting the header at nginx, not taking the last address (a search of Chapters 16, 32 and 33 finds no "last address"); that rule is introduced in Ch 40 section 40.6.
- Fix: "Chapter 41, Figure 41.1, shows..." and "the last-address rule of Chapter 40, Section 40.6".

**P7-11 Table 40.1 states as fact what the text says is unverified**
- Where: Table 40.1 row 2 ("the ALB does not add HSTS") versus section 40.5 ("nothing verified here says the ALB does").
- Fix: "the ALB is not documented to add HSTS, so add it in nginx".

**P7-12 Chapter 40 is long and dense for a book aimed at beginners**
- Where: Ch 40 (8,058 words with metadata comments removed, not 6,200; Advanced tier 2,687 words; Intermediate 2,172); several 220-350-word paragraphs (sizing, replace order, janitor, session metadata, scheduled jobs); 9 sentences over 60 words; Ch 41 has 8.
- Problem: It is within the Part IV ceiling (10,000) and the tiers say "skim on a first read", but the Intermediate tier already carries attribute names (`routing.http.xff_header_processing.mode`), regular-expression map keys and connection-pool arithmetic, so the difficulty ramp after Part VI is steeper than the tier labels suggest. Only the analogy and a term list sit in the Beginner tier.
- Fix: Split the longest paragraphs; move the configuration attribute names into a table; put a one-paragraph "what you need on a first read" at the top of the Intermediate and Advanced tiers.

**P7-13 Part VII exercise format differs from the rest of the book**
- Where: Ch 40-41 Try it.
- Problem: Only Part VII has an italic "*Level: N stars.*" line, a hint and a per-exercise "*Solution:* Appendix C, Exercise N" pointer. Chapters 1-39 give a title with ★, one prompt and, in a few, a hint; Part IV chapters point to `NN-slug.solutions.md`. The Part VII form is better (levels in words help accessibility) but makes the book inconsistent.
- Fix: keep it, and either back-port the Level line and pointer to every chapter, or record in STYLE that Part VII sets the new standard.

**P7-14 Text description present only in Part VII and Appendix B**
- Where: whole book; the scratch manuscript has 10 "Text description" lines (6 in Appendix B, 4 in Part VII) against 74 figure captions.
- Problem: unchanged from TL-07: the four Part VII figures comply, so Part VII is now the exception, and the lack in Chapters 1-39 is more visible.
- Fix: as TL-07.

**P7-15 Stale "closes the book" statements**
- Where: `book/OUTLINE.md` item 6 ("Part VI closes the book by naming the design patterns..."); `part-6-patterns/39-architectural-patterns.md` last lines ("This is the end of the book's design vocabulary. The epilogue ties the parts together."); Preface says Part VII "closes the book".
- Fix: OUTLINE: "Part VI names...; Part VII asks what a move to AWS would take". Ch 39: "Part VII applies these ideas to a cloud design; the epilogue then ties the parts together." Preface: "closes the main text".

**P7-16 Chapter 41 has no primary source for Terraform and its Further reading omits Chapter 40-41 tools**
- Where: Ch 41 section 41.7 and Further reading.
- Problem: The chapter picks Terraform and gives a 40-line excerpt, but Further reading has no Terraform documentation link (STYLE allows official documentation only, so add the official docs), and no link for Amazon EventBridge Scheduler, IAM or GitHub OIDC in the AWS link list beyond one. Also Terraform, AWS CDK and "infrastructure as code" (Chapter 39) are not in the glossary.
- Fix: add the Terraform AWS provider documentation and the IAM User Guide; add "Terraform" and "sidecar" to the glossary.

**P7-17 Part VII opener departs from the other openers**
- Where: `part-7-cloud/00-part-introduction.md`.
- Problem: Part V and VI openers have "What the part covers", "How to read this part", "What you will have..." headings (Part VI's table is captioned "Table 1", Part VII's "Table VII.1"). Part VII has three headings only. The "Read Chapters 37 and 39 first" advice omits Chapters 32-36, which Ch 40's Prerequisites list.
- Fix: add "Before you start: Chapters 32-37 and 39" and unify table captions ("Table VI.1", "Table VII.1").

**P7-18 Metadata still says `status: in-review`**
- Where: line 1 of `00-part-introduction.md`, `40-aws-production.md`, `41-aws-operations.md` and both solutions files (`owner: editor`).
- Problem: The comment is stripped from the output, so nothing reaches the reader, but every other chapter says `status: expanded`. No leaked words (dossier, agent ids, TODO) appear in the reader-facing text of Part VII.
- Fix: set the status when the review is closed.

**P7-19 Ch 33 "See also" placement and wording**
- Where: Ch 33 section 33.14, after "The README's go-live checklist turns the chapter into steps. With the reason for each:".
- Problem: The "See also" note is inserted between the lead-in sentence and the checklist it introduces, splitting them.
- Fix: move it after the checklist.

**P7-20 Prior finding still open: Chapter 32 prerequisite refers to Chapter 33; exercise titles differ from Appendix C**
- Where: Ch 32 line 29 (TL-10); 19 exercises in Chapters 20, 21, 23, 24 (TL-16).
- Problem: `bookcheck.py` still reports the Ch 32 forward reference; titles mismatch grew from 18 to 19 rows (Appendix C Exercise 20.1 "Which script needs a stack" versus "Which script needs more than Node?").
- Fix: as before.

## Regression check of my earlier top findings

| Earlier ID | Status now |
|---|---|
| TL-01 Appendix C stray headings | Partly fixed: 22 to 16 stray H1s; Chapters 1-10 and 19-24 still duplicated. Chapters 40 and 41 clean. Not worse. |
| TL-02 internal file names | Unchanged: Appendix E line 4 (`book/_team/requests.md`), `book/OUTLINE.md` in Chapters 25-31, `NN-....solutions.md` in Chapters 25-31, "Generated from `book/GLOSSARY.md`". Part VII adds none of these (it points to Appendix C correctly). |
| TL-03 index / acknowledgments | Unchanged (P7-05). |
| TL-04 glossary quality | Unchanged; 36 new AWS rows are good (P7-06). |
| TL-05/06 Playwright, Maven-for-old-tags in Setup | Unchanged. |
| TL-07 Text description | Unchanged outside Part VII. |
| TL-09 table caption placement | Unchanged (23 italic captions below); Part VII captions are correct (bold, above). Tables 27.1 and 31.2 still have the bold stub. |
| TL-08 "blocks the pull request" | Unchanged. |

## Checked and found correct

- Learning path: Chapter 40 prerequisites (8, 10, 14, 16, 32-36, 37/37.17, 39) match OUTLINE "Assumes" and are all earlier chapters; Chapter 41 prerequisites (40, 32-37, 39) likewise. `bookcheck.py`: 42 chapters found, none missing, no new problems.
- References into earlier chapters resolve: 37.2, 37.4, 37.17, Figure 37.2, 39.11, 39.13, 39.14, Exercise 33.3, Chapter 32's 410 render-version check and X-Forwarded-For incident, Chapter 16's `synchronized` throttle, Chapter 34's `SIGNING_SECRET` handling. The eleven "See also" lines from Chapters 33, 34, 35, 36, 37 and 39 all point at the right section (40.5, 40.7, 40.8, 40.9, 41.2, 41.4, 41.6, 41.8).
- Numbering: no duplicate, gap or out-of-order figure, listing or table number in Part VII; sections 40.1-40.10 and 41.1-41.11 are continuous and match OUTLINE exactly; forward references (40 to 41.4, 41.5, 41.7, 41.8, 41.10, 41.11; 41 back to 40.5) all resolve.
- Template: three tier headings with "skim" lines, analogies with a "breaks down" paragraph (40.1 and 41.1), Common mistakes, In this project, Try it (6 and 4 exercises; at least one of each of ★ and ★★; ★★★ present), Summary (7 bullets each), Further reading.
- Exercises and Appendix C: 242 exercises, 242 solutions, no missing or extra; the solutions for 40.1-41.4 answer the exercises as worded and use the same star levels; Exercise 41.2's answer (moves C, D, E and F for three copies) agrees with Figure 41.1.
- Listings 40.1-40.4 and 40.6-40.8 verified against `book-m6-final` (7 of 7 ok).
- Consistency with front matter: OUTLINE (41 chapters, seven parts), book README ("41 chapters in seven parts"), preface, how-to-use and epilogue ("How the seven parts fit together", "Part VII pointed past the single server") all agree with the new part, apart from P7-15.
- Reader-facing wording: no "dossier", agent ids, TODO or draft status text in Part VII; no banned tone words, "we", or above/below cross-references; ★ levels are also written in words; figures have Text descriptions; no prices or benchmarks, as promised.

## Not checked

- The built PDF, EPUB and HTML for Part VII (they predate it), so layout, contents entries, page flow, alt text rendering and contrast for the new pages are unchecked.
- Correctness of the AWS statements (service limits, defaults, attribute names, S3 and ElastiCache behavior): I had no access to the AWS documentation in this task and did not use the web; every AWS claim is "unverified by me". The app-side claims I compared to the repo were only those in the listings and a few named settings.
- `book/_team/reviews/40-*.md` (deliberately unread), the dossier, and whether all Chapter 40 and 41 statements about the code (for example `BootstrapAdmin`, `SessionAdministration.getAllPrincipals()`, `server.shutdown`, the 75% JVM memory setting) match `book-m6-final`; only the quoted listings were compared.
- The remaining chapters of Parts I-VI, which I did not re-read for this task beyond the searches above.
