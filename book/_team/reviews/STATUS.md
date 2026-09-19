# Review status

Updated: 2026-09-19 20:53 IST (window 3). Counts are OPEN findings after re-checks. Checklist: `CHECKLIST.md`.

| Chapter | Review file | State | Blockers | Major | Minor |
|---|---|---|---|---|---|
| 01 The big picture | 01-the-big-picture.review.md | pass 1 done; term-definition major RESOLVED (PDF, URL, session, cache, DevTools, rate limit now defined); minors not re-checked | 0 | 0 | ~7 |
| 02 Command line and files | 02-command-line-and-files.review.md | reviewed | 0 | 2 | 8 |
| 03 First Java program | 03-first-java-program.review.md | reviewed; clean | 0 | 0 | 8 |
| 04 Classes and objects | 04-classes-and-objects.review.md | reviewed; clean | 0 | 0 | 8 |
| 05 | 05-collections-and-exceptions.review.md | LIGHT PASS (listings match tag; prose not reviewed) | 0 | 0 | 0 |
| 06 | 06-maven-and-project-layout.review.md | LIGHT PASS (listings match tag; prose not reviewed) | 0 | 0 | 0 |
| 07 | 07-git-and-github.review.md | LIGHT PASS (listings match tag; prose not reviewed) | 0 | 0 | 0 |
| 08 | 08-how-the-web-works.review.md | LIGHT PASS (listings match tag; prose not reviewed) | 0 | 0 | 0 |
| 09 | 09-sql-and-mysql.review.md | LIGHT PASS (listings match tag; prose not reviewed) | 0 | 0 | 0 |
| 10 | 10-docker-and-compose.review.md | LIGHT PASS (Listing 10.3 caption/ENV minor issues) | 0 | 0 | 2 |
| 11 Spring Boot foundations | 11-spring-boot-foundations.review.md | pass 2 done; both majors RESOLVED (chapter complete) | 0 | 0 | 9 |
| 12 REST controllers and JSON | 12-rest-controllers-and-json.review.md | reviewed; partial (12.4+ unwritten) | 0 | 3 | 4 |
| 13 Validation and errors | 13-validation-and-errors.review.md | reviewed | 0 | 2 | 7 |
| 19 TypeScript | 19-typescript.review.md | pass 2 done; all 3 majors RESOLVED (chapter complete; 19.7-19.10 not fully read) | 0 | 0 | 12 |
| 20 Node, npm, Angular CLI | 20-node-npm-angular-cli.review.md | reviewed; high quality | 0 | 1 | 7 |
| 25 M0: The tiled viewer | 25-m0-the-tiled-viewer.review.md | prerequisites, scratchpad-citation and base64url/manifest majors RESOLVED; tier headings now present; solutions file and exercise format still open | 0 | 0 | ~8 |
| 26 M1: Accounts | 26-m1-accounts.review.md | reviewed; strong | 0 | 2 | 8 |
| 27 M2: Documents and audit | 27-m2-documents.review.md | reviewed; listings verified line by line; strong | 0 | 0 | 9 |
| 28 M3: Hardening | 28-m3-hardening.review.md | reviewed; listings verified line by line | 0 | 1 | 7 |
| 29 M4: Reading experience | 29-m4-reading.review.md | reviewed; listings verified line by line | 0 | 0 | 7 |
| 32 Security review | 32-security-review.review.md | not changed since pass 1 | 0 | 5 | 8 |
| 36 Supply chain and CI | 36-supply-chain-and-ci.review.md | LIGHT PASS (listings match; 1 major: invented Angular 23 exercise) | 0 | 1 | 1 |
| 37 Engineering trade-offs | 37-engineering-tradeoffs.review.md | reviewed; item 4 downgraded (source deletion is real) | 1 (canvas claim) | 4 | 7 |

## Window 4 (21:08-21:11 IST)
Listing check by script passed for chapters 05-10, 14-18, 21-24 and 33-36 (only Ch 10 Listing 10.3 differs). Prose of these chapters is NOT reviewed yet (14-18, 21-24, 33-36 have no review file). Writers were fixing majors in 2, 4, 12, 13, 20, 26, 28, 32, 37; those were not re-reviewed.

## Open blockers
- Ch 37, 37.1: says the browser reassembles tiles on a `<canvas>`; at book-m6-final the Angular viewer uses positioned divs with CSS backgrounds. Root cause is a stale README line 6 (also a stale Javadoc in `PageInfo.java`). Owner: writer-production; README fix requested via editor.

## Not yet reviewed (files exist)
05, 06, 07, 08 (Part I); 30, 31 (Part IV; their listings already verified line by line, prose not yet read); `00-part-introduction.md` files; solutions files for 05-08 and later. Next window: start with 30 and 31 prose, then 05-08.

## Cross-cutting patterns to fix everywhere
1. Bold defined terms (STYLE 9) and file glossary requests with definitions.
2. Exercises in `### Exercise N.M ★ Title` format with a solutions file.
3. Figure captions italic below the figure; listings numbered sequentially (no 19.3a / 19.3b).
4. Reader-facing text must not cite "the dossier" or internal IDs; keep them in `<!-- source -->` comments.
5. Label every omission in a "simplified" listing; use `// ...` at the actual cut points.
