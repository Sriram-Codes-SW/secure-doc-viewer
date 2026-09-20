# Review status

Updated: window 8. Part I re-check: my Part I majors are essentially all RESOLVED after the restructuring; one new stale reference (Ch 3 line ~485 "Section 3.4" should be 3.10). Window 8 added full reviews of Ch 25, 26, 27, 30 (new cross-book finding: Flyway 12.4.0, not 11). Part III (19, 21, 22) and Part V (32, 37) done in window 8. Ch 28, 29, 31 re-checked: caption-format, trace-code, secrets and blueprint-v6 majors are all RESOLVED in the files (statuses now `expanded`). Ch 7 merge wording RESOLVED. Ch 23 open-redirect explanation RESOLVED. Ch 32-37 still carry `status: expanded-draft`.

Earlier note (window 7, later): Full prose reviews of the EXPANDED text are done for: Part I (introduction and Ch 1-10), Ch 15, 18, 20, 23, 24, 28, 29, 31, 33, 34, 35, 36. Review files for Ch 11-14, 16, 17, 19, 21, 22, 25-27, 30, 32, 37 still describe the pre-expansion text (or were only re-checked by grep) and must be redone as those chapters are marked expanded. Counts are OPEN findings. Listing exactness is checked by the coordinator's script. Checklist: `CHECKLIST.md`.

| Chapter | Review file | State | Blockers | Major | Minor |
|---|---|---|---|---|---|
| Part I intro | 00-part-introduction.review.md | window 7, full | 0 | 0 | 7 |
| 01 The big picture | 01-the-big-picture.review.md | window 7, expanded, full | 0 | 2 | 9 |
| 02 Command line and files | 02-command-line-and-files.review.md | window 7, expanded, full; earlier majors RESOLVED | 0 | 1 | 8 |
| 03 First Java program | 03-first-java-program.review.md | window 7, expanded, full | 0 | 1 | 8 |
| 04 Classes and objects | 04-classes-and-objects.review.md | window 7, expanded, full; earlier label majors RESOLVED | 0 | 2 | 8 |
| 05 Collections and exceptions | 05-collections-and-exceptions.review.md | window 7, expanded, full; earlier compile major RESOLVED | 0 | 1 | 9 |
| 06 Maven and project layout | 06-maven-and-project-layout.review.md | window 7, expanded, full; earlier `verify` major RESOLVED | 0 | 0 | 10 |
| 07 Git and GitHub | 07-git-and-github.review.md | window 8 re-check: merge-decision major RESOLVED (owner approved; agents advised); other items open | 0 | 0 | 9 |
| 08 How the web works | 08-how-the-web-works.review.md | window 7, expanded, full | 0 | 1 | 9 |
| 09 SQL and MySQL | 09-sql-and-mysql.review.md | window 7, expanded, full | 0 | 1 | 9 |
| 10 Containers and Docker | 10-docker-and-compose.review.md | window 7, expanded, full; earlier caption issue RESOLVED | 0 | 0 | 9 |
| 11 Spring Boot foundations | 11-spring-boot-foundations.review.md | pre-expansion; earlier majors RESOLVED; redo when expanded | 0 | 0 | 9 |
| 12 REST controllers and JSON | 12-rest-controllers-and-json.review.md | pre-expansion; majors RESOLVED (grep re-check) | 0 | 0 | 4 |
| 13 Validation and errors | 13-validation-and-errors.review.md | pre-expansion; majors RESOLVED (grep re-check) | 0 | 0 | 7 |
| 14 JPA and Flyway | 14-jpa-and-flyway.review.md | pre-expansion full pass; strong | 0 | 0 | 8 |
| 15 Spring Security I | 15-spring-security-authentication.review.md | window 7, expanded, full; strong; earlier minors mostly RESOLVED | 0 | 0 | 9 |
| 16 Spring Security II | 16-spring-security-defenses.review.md | pre-expansion full pass | 0 | 0 | 7 |
| 17 Files, images, PDFs, signatures | 17-files-images-pdfs-signatures.review.md | pre-expansion full pass | 0 | 0 | 8 |
| 18 Testing the backend | 18-testing-the-backend.review.md | window 8 re-check: secrets major RESOLVED (placeholders `<test-signing-secret>`, `<test-admin-password>`); `checkAllowed` claim withdrawn earlier | 0 | 0 | 8 |
| 19 TypeScript | 19-typescript.review.md | window 8, expanded; earlier majors RESOLVED; renumbering 19.1-19.14 checked: no stale citations anywhere in the book | 0 | 0 | 9 |
| 20 Node, npm, Angular CLI | 20-node-npm-angular-cli.review.md | window 7, expanded, full; earlier majors RESOLVED; strong | 0 | 0 | 9 |
| 21 Angular components | 21-angular-components.review.md | window 8, expanded; contrast figures and code claims verified; Listing 21.6a numbering/no Path line | 0 | 0 | 10 |
| 22 HttpClient and services | 22-http-client-and-services.review.md | window 8, expanded; nginx/CSP/e2e claims verified; renumbering 22.1-22.15 checked; reader-facing "dossier" citation RESOLVED | 0 | 0 | 10 |
| 23 Routing, guards, forms | 23-routing-guards-forms.review.md | window 8 re-check: open-redirect major RESOLVED (text now says the attack needs the raw string handed to the browser) | 0 | 0 | 9 |
| 24 Testing the frontend | 24-testing-the-frontend.review.md | window 7, expanded, full; arithmetic verified; strong | 0 | 0 | 9 |
| 25 M0: The tiled viewer | 25-m0-the-tiled-viewer.review.md | window 8, expanded, full; earlier majors RESOLVED; new major: no way to run an older milestone | 0 | 1 | 7 |
| 26 M1: Accounts | 26-m1-accounts.review.md | window 8, expanded, full; Exercise 26.5 verified correct at m1; same run-old-milestone major | 0 | 1 | 8 |
| 27 M2: Documents and audit | 27-m2-documents.review.md | window 8, expanded, full; 27.6 picker note verified against dossier V1 and code; strong | 0 | 0 | 9 |
| 30 M5: Platform | 30-m5-platform.review.md | window 8, expanded, full; major: "Flyway 11" is wrong (Boot 4.1.1 ships Flyway 12.4.0); Table 30.1 implies nothing about "before" | 0 | 1 | 10 |
| 28 M3: Hardening | 28-m3-hardening.review.md | window 8 re-check: caption-format major RESOLVED (10 listings, all `*Path:*` after); status `expanded` | 0 | 0 | 9 |
| 29 M4: Reading experience | 29-m4-reading.review.md | window 8 re-check: trace code now "an invented example value" and captions fixed: majors RESOLVED | 0 | 0 | 10 |
| 31 M6: Final state | 31-m6-final.review.md | window 8 re-check: caption format and blueprint v6 (Figure 31.1) RESOLVED | 0 | 0 | 10 |
| 32 Security review | 32-security-review.review.md | window 8, expanded, full; earlier majors RESOLVED; header still `expanded-draft` | 0 | 0 | 12 |
| 33 Deployment and TLS | 33-deployment-and-tls.review.md | window 7, expanded, full; earlier CSP major RESOLVED | 0 | 0 | 9 |
| 34 Backups and operations | 34-backups-and-operations.review.md | window 7, expanded, full; strong | 0 | 0 | 9 |
| 35 Metrics and monitoring | 35-metrics-and-monitoring.review.md | window 7, expanded, full; strong; settles the Ch 32 `PAGE_VIEWED` question | 0 | 0 | 9 |
| 36 Supply chain and CI | 36-supply-chain-and-ci.review.md | window 7, expanded, full; earlier Angular 23 major RESOLVED | 0 | 0 | 8 |
| 37 Engineering trade-offs | 37-engineering-tradeoffs.review.md | window 8, expanded, full (file is `book/tradeoffs/37-...`); earlier blocker RESOLVED; new major: "Flyway 11" should be 12.4.0 | 0 | 1 | 10 |

## Open blockers
None.

## Themes across the expanded chapters
1. Hands-on steps that need a running app or a database before the book has taught how (Part I: Ch 8, 9; Part IV: Ch 28-29 exercises at old tags). Give a working route or mark "after Chapter 10", and add a "how to run milestone N" box to the Part IV introduction.
2. (RESOLVED for Part IV in window 8; Part V not re-counted) Listing caption format: Part IV (25-31) and Part V (33-36) captions differed from the house format (`*Path: ...*` italic line AFTER the block); several code blocks are uncaptioned and unnumbered. Fix so the coordinator's script and the editor's tooling can read them.
3. Header status values: `expanded-draft` (32, 33, 34, 35, 36, 37) should be `expanded` (Part I to IV now fixed).
4. (RESOLVED) Secrets and real-looking values (STYLE 16): Ch 18 (test secret and bootstrap password in application-test.yml), Ch 29 (a trace code from a real dev session). Replace with placeholders.
5. (RESOLVED) History accuracy: Ch 7 (merge decision).
6. Pacing for beginners: Ch 1 (25 new terms in the Beginner tier), Ch 3 (bytes/BCrypt), Ch 4-5 (advanced material), Ch 24 (TestBed in the Beginner tier).
7. Every analogy needs a "where it breaks down" sentence (missing: Ch 5 dictionary and Optional box, Ch 8 DNS and protocol, Ch 9 book index).
8. Bold each term only at its first definition (editor's duplicate list); use plain text plus a cross-reference later.

9. Cross-book version fact: "Flyway 12" (CHARTER, STYLE 11.5, Part II intro, Ch 30, Ch 37) said "Flyway 11"; corrected to Flyway 12 (12.4.0). RESOLVED by the coordinator.

## Still to do
- Full expanded-text reviews for Ch 11-14, 16, 17 (still pre-expansion reviews) and the Part II-V introductions.
- Re-check Part I majors that remain open in the review files (Ch 1, 2, 3, 4, 5, 8, 9) only after writer-foundations reports further fixes.
- The Ch 22 "project dossier" citation is RESOLVED (window 8).
