# Review status

Updated: 2026-09-20 01:50 IST (window 5). Counts are OPEN findings after re-checks. Checklist: `CHECKLIST.md`.

| Chapter | Review file | State | Blockers | Major | Minor |
|---|---|---|---|---|---|
| 01 The big picture | 01-the-big-picture.review.md | pass 1 done; term-definition major RESOLVED (PDF, URL, session, cache, DevTools, rate limit now defined); minors not re-checked | 0 | 0 | ~7 |
| 02 Command line and files | 02-command-line-and-files.review.md | majors RESOLVED (PowerShell variants added; version control defined) - window 5 re-check | 0 | 0 | 8 |
| 03 First Java program | 03-first-java-program.review.md | reviewed; clean | 0 | 0 | 8 |
| 04 Classes and objects | 04-classes-and-objects.review.md | listing labels fixed (4.1, 4.5) - RESOLVED | 0 | 0 | 8 |
| 05 | 05-collections-and-exceptions.review.md | FULL PASS (window 5) | 0 | 1 | 7 |
| 06 | 06-maven-and-project-layout.review.md | FULL PASS (window 5) | 0 | 1 | 6 |
| 07 | 07-git-and-github.review.md | FULL PASS (window 5) | 0 | 1 | 7 |
| 08 | 08-how-the-web-works.review.md | FULL PASS (window 5) | 0 | 0 | 6 |
| 09 | 09-sql-and-mysql.review.md | FULL PASS (window 5) | 0 | 0 | 7 |
| 10 | 10-docker-and-compose.review.md | FULL PASS (window 5); earlier caption issue RESOLVED | 0 | 0 | 8 |
| 11 Spring Boot foundations | 11-spring-boot-foundations.review.md | pass 2 done; both majors RESOLVED (chapter complete) | 0 | 0 | 9 |
| 12 REST controllers and JSON | 12-rest-controllers-and-json.review.md | majors RESOLVED (12.4-12.7 written; @RequestParam covered; invented comment gone); Listing 12.1 not re-read | 0 | 0 | 4 |
| 13 Validation and errors | 13-validation-and-errors.review.md | majors RESOLVED (versions note added; terms explained) | 0 | 0 | 7 |
| 14 JPA and Flyway | 14-jpa-and-flyway.review.md | FULL PASS (window 5); strong | 0 | 0 | 8 |
| 15 Spring Security I | 15-spring-security-authentication.review.md | FULL PASS (window 5) | 0 | 0 | 9 |
| 16 Spring Security II | 16-spring-security-defenses.review.md | FULL PASS (window 5) | 0 | 0 | 7 |
| 19 TypeScript | 19-typescript.review.md | pass 2 done; all 3 majors RESOLVED (chapter complete; 19.7-19.10 not fully read) | 0 | 0 | 12 |
| 20 Node, npm, Angular CLI | 20-node-npm-angular-cli.review.md | major RESOLVED (no stale Chapter 22 nginx refs found by grep) | 0 | 0 | 7 |
| 25 M0: The tiled viewer | 25-m0-the-tiled-viewer.review.md | prerequisites, scratchpad-citation and base64url/manifest majors RESOLVED; tier headings now present; solutions file and exercise format still open | 0 | 0 | ~8 |
| 26 M1: Accounts | 26-m1-accounts.review.md | major RESOLVED for reader-facing dossier citations (none left outside comments) | 0 | 0 | 8 |
| 27 M2: Documents and audit | 27-m2-documents.review.md | reviewed; listings verified line by line; strong | 0 | 0 | 9 |
| 28 M3: Hardening | 28-m3-hardening.review.md | major RESOLVED (secret rule now says present since m1) | 0 | 0 | 7 |
| 29 M4: Reading experience | 29-m4-reading.review.md | reviewed; listings verified line by line | 0 | 0 | 7 |
| 32 Security review | 32-security-review.review.md | majors RESOLVED (AI reviewers stated, source comments added, listing label extended); not fully re-read | 0 | 0 | 8 |
| 33 Deployment and TLS | 33-deployment-and-tls.review.md | LISTING PASS ONLY (1 major: CSP abbreviated silently) | 0 | 1 | 0 |
| 36 Supply chain and CI | 36-supply-chain-and-ci.review.md | LIGHT PASS (listings match; 1 major: invented Angular 23 exercise) | 0 | 1 | 1 |
| 37 Engineering trade-offs | 37-engineering-tradeoffs.review.md | BLOCKER RESOLVED (canvas, Kubernetes, dossier cites gone by grep); not fully re-read | 0 | 0 | 7 |

## CORRECTION (window 5): the window-4 'listing check passed' for 21-24 and 33-36 was vacuous (the script parsed 0 listings because caption formats differ). Re-run in window 5 with a caption-based parser: listings in 14-18, 21-24, 30, 31 now genuinely match their tags; Ch 33 Listing 33.1 has one undisclosed abbreviation (CSP); 34.1/34.2 (README paths ambiguous), 35 and 36 have no parsable listings and remain unchecked.

## Window 4 (21:08-21:11 IST)
Listing check by script passed for chapters 05-10, 14-18, 21-24 and 33-36 (only Ch 10 Listing 10.3 differs). Prose of these chapters is NOT reviewed yet (14-18, 21-24, 33-36 have no review file). Writers were fixing majors in 2, 4, 12, 13, 20, 26, 28, 32, 37; those were not re-reviewed.

## Window 5 re-check (01:42-01:47 IST)
Re-checked by grep, not by full re-read. Still open: Ch 36 invented 'Angular 23 requires TypeScript 6.1' exercise (unchanged); Ch 22 has a reader-facing 'project dossier' citation (line ~280; Ch 22 not yet reviewed); Ch 30 not re-checked.

## Open blockers
None (Ch 37 canvas blocker resolved in window 5; repo README line 6 may still say canvas).

## Not yet reviewed (files exist)
Full prose review pending: 17-18, 21-24, 30-31, 33-36 (36 has a light-pass major); `00-part-introduction.md` files. Order next window: 17-18, 21-24, 30-31, 33-35.

## Cross-cutting patterns to fix everywhere
1. Bold defined terms (STYLE 9) and file glossary requests with definitions.
2. Exercises in `### Exercise N.M ★ Title` format with a solutions file.
3. Figure captions italic below the figure; listings numbered sequentially (no 19.3a / 19.3b).
4. Reader-facing text must not cite "the dossier" or internal IDs; keep them in `<!-- source -->` comments.
5. Label every omission in a "simplified" listing; use `// ...` at the actual cut points.
