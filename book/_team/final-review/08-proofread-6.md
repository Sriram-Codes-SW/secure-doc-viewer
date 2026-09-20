# Proofread log 08: Parts VI and VII, epilogue, Appendices B, D, E, README, glossary source

Scope: `book/part-6-patterns/*.md`, `book/part-7-cloud/*.md`, `book/appendices/epilogue.md`, `appendix-b-blueprint-history.md` (prose only), `appendix-d-command-reference.md`, `appendix-e-troubleshooting.md`, `book/README.md`, and the definitions in `book/_team/glossary_src.tsv`. Appendix A, Appendix C and the index were not edited. Code, inline code, Mermaid, listings, tables' technical content, quotations and HTML comments were not changed. Nothing was committed and nothing under `book/build/` was touched.

## Terminology and style decisions applied

- Cross-references: "Section", "Sections", "Exercise" capitalized when they name a numbered part (STYLE 15). Lowercase "section 37.5" and similar were found throughout Chapters 39, 40, 41 and the Part VII introduction. Occurrences inside HTML comments were left as they were.
- Serial (Oxford) comma in the glossary definitions (STYLE 3); the file omitted it consistently.
- "sign-in" for the noun (word list): "a login" became "a sign-in" in prose (code names such as `LoginThrottle` untouched).
- American spelling: "recognised-device" became "recognized-device" in prose (Ch 40, Ch 41). Code identifiers (`recognisedDevice`) untouched.
- Product and service names on first mention: "Amazon S3" and "Amazon ECS" in the Chapter 39 cross-reference notes; "Amazon Web Services (AWS)" in the Part VII introduction.
- VPC is "virtual private cloud" (defined in Chapter 40), not "virtual private network" (that is a VPN).
- Bold is only for defined terms and fixed lead-ins (STYLE 9): stray bold emphasis changed to italics.
- Not touched on purpose: the term key "recognised device" in `glossary_src.tsv` line 286 (see query G1).

## Edits made

### Part VI

| File | Where | Before -> After | Type |
|---|---|---|---|
| `part-6-patterns/38-design-patterns.md` | 38.1 | "Gamma, Helm, Johnson and Vlissides" -> "Gamma, Helm, Johnson, and Vlissides" | serial comma |
| same | 38.7, last paragraph | `"has a session ended?". It is *told*` -> `"has a session ended?" It is *told*` | punctuation |
| same | Summary, fifth bullet | "...each job takes) and a sliding window limiter bounds how often a reader may ask, and reserve-then-compensate..." -> semicolons between the three clauses | list punctuation |
| `part-6-patterns/39-architectural-patterns.md` | Prerequisites (7 items) | trailing periods removed so the list matches Chapter 38's fragment style | list punctuation |
| same | 39.1 | "The analogy breaks down in two ways." -> "**Where the analogy breaks down:** in two ways." (STYLE 8 requires the bold lead-in) | template conformance |
| same | 39.5 | "The project **approximates** the pattern" -> "*approximates*" | bold used for emphasis |
| same | 39.6 | "a slow tile render and a login share the same JVM" -> "...a sign-in share..." | word list |
| same | 39.6, 39.9, 39.12, 39.15 to 39.19, Common mistakes, In this project, Exercises 39.3 and 39.6 (23 places) | "section 39.5", "sections 37.5 and 37.7" and so on -> "Section", "Sections" | capitalization of references |
| same | 39.7 | "Chapter 33, exercise 33.3" -> "Chapter 33, Exercise 33.3" | capitalization |
| same | 39.11 note | "applies this pattern to S3" -> "Amazon S3" | product name |
| same | 39.12 note | "injected as environment variables on ECS" -> "on Amazon ECS" | product name |
| `part-6-patterns/39-architectural-patterns.solutions.md` | Exercise 39.6 | "noted in section 39.6" -> "Section 39.6" | capitalization |

### Part VII

| File | Where | Before -> After | Type |
|---|---|---|---|
| `part-7-cloud/00-part-introduction.md` | "Before you start" | "especially section 37.17" -> "Section 37.17" | capitalization |
| same | "An honest frame" paragraph | "without an AWS account" -> "without an Amazon Web Services (AWS) account" (first use of the acronym in the part) | acronym expansion |
| `part-7-cloud/40-aws-production.md` | Prerequisites, 40.1 to 40.10, Exercises (27 places, plus 2 in the solutions file) | lowercase "section N.M" and "sections N.M" -> "Section", "Sections" | capitalization of references |
| same | 40.1 | "...you put in it. (source 1)<!-- ... -->" -> "...you put in it (source 1)<!-- ... -->." so the marker sits before the period like every other marker | source-marker placement |
| same | Figure 40.1 text description | "a virtual private network spanning" -> "a virtual private cloud (VPC) spanning" | terminology |
| same | 40.5, "Idle timeout" | blank line inserted before "Raising it creates a second rule." (a paragraph of about 400 words became two; trailing space removed) | overlong paragraph |
| same | 40.10 | "recognised-device purge" -> "recognized-device purge" | American spelling |
| `part-7-cloud/40-aws-production.solutions.md` | Exercise 40.1 | "(section 40.4)" -> "(Section 40.4)" | capitalization |
| `part-7-cloud/41-aws-operations.md` | Introduction, Prerequisites, 41.2 to 41.11, Exercises (about 25 visible places; the HTML comments were restored) | lowercase "section N.M" -> "Section N.M" (including "section 32.11") | capitalization of references |
| same | 41.2 | "recognised-device hashes" -> "recognized-device hashes" | American spelling |
| same | 41.8 | "recognised-device lockout" -> "recognized-device lockout" | American spelling |
| same | 41.2 | "**no long-lived access keys anywhere**" -> "*no long-lived access keys anywhere*" | bold used for emphasis |
| same | Figure 41.1 text description | "a virtual private network spanning" -> "a virtual private cloud (VPC) spanning" | terminology |
| same | 41.5, "Recovery objectives" | "S3 to within the most recent 15<!-- source ... -->." -> "...most recent 15 minutes<!-- ... -->." (unit was missing; the same fact appears earlier in the section as "15 minutes") | missing unit |
| same | 41.11 | "durable multi-AZ tile storage" -> "Multi-AZ" (the defined term) | capitalization |

### Back matter, README and glossary source

| File | Where | Before -> After | Type |
|---|---|---|---|
| `appendices/appendix-d-command-reference.md` | heading | "(Chapter 10, 30, 33, 34)" -> "(Chapters 10, 30, 33, and 34)" | plural and serial comma |
| same | Node table | "http://localhost:4200" -> `` `http://localhost:4200` `` | URL in code font |
| same | Docker table | "HTTPS front end (Caddy)" -> "HTTPS frontend (Caddy)" | word list |
| `README.md` | part table | "The Engineering Trade-offs" -> "The engineering trade-offs" (matches the Chapter 37 title, sentence case) | capitalization |
| same | bottom code block | bare fence -> `` ```bash `` | fence language (STYLE 11.4) |
| `_team/glossary_src.tsv` | 40 definition lines (4, 5, 8, 16, 19, 20, 22, 24, 28, 31, 48, 51, 62, 72, 88, 94, 96, 99, 115, 139, 140, 178 (two commas), 196, 203, 214, 220, 250, 287, 293, 301, 303, 342, 351, 360, 372, 389, 401, 421, 423, 441) | serial comma added to three-item lists, for example "a list, a set or a map" -> "a list, a set, or a map" | serial comma |

The TSV format was kept intact: tabs preserved, 481 entries, every line has at least two fields, LF endings unchanged, only definitions edited (term keys and alias columns untouched).

### Counts by type

| Type | Count |
|---|---|
| Capitalization of Section/Sections/Exercise references | about 85 |
| Serial comma (1 in prose, 41 in the glossary source) | 42 |
| List punctuation (periods removed, semicolons) | 8 |
| Terminology or word list (sign-in, frontend, VPC, Multi-AZ, engineering trade-offs) | 6 |
| American spelling (recognized) | 3 |
| Product name or acronym expansion | 3 |
| Bold to italics, or bold lead-in fix | 3 |
| Missing unit | 1 |
| Punctuation (quote and period, source-marker placement) | 2 |
| Paragraph split | 1 |
| Fence language, URL in code font, heading plural | 3 |

## Queries for the author (not changed)

**P39-1. Chapter 39, "Try it" (all six exercises).** No exercise has a `*Solution:* Appendix C, Exercise 39.N.` pointer (a solutions file exists), and none has a `*Hint:*`. STYLE 12 requires the pointer. Suggested: add `*Solution:* Appendix C, Exercise 39.1.` and so on after each exercise; for 39.5 and 39.6 add "(a worked outline)".

**P39-2. Chapter 39, Exercise 39.3.** "Choose one of the eight patterns in Sections 39.4 to 39.15." Those sections hold twelve patterns, and the Summary says "about a dozen". Suggested: "one of the twelve patterns in Sections 39.4 to 39.15".

**P39-3. Chapter 39, Further reading.** STYLE 15 allows official documentation only. Martin Fowler's "MonolithFirst" and Michael Nygard's "Documenting Architecture Decisions" are blog posts, and the entries do not follow the house citation format (`*Title*, "Section." URL`). Suggested: keep the Twelve-Factor App, the Spring Security reference and the OWASP page; drop the two blog posts or name them in the text as influences; apply the house format.

**P39-4. Chapter 39, Section 39.5, fourth paragraph.** "`AuthController` has thirteen `private final` fields, twelve of them collaborators, several of them Spring Security internals, and `UserAdminController` revokes a user's sessions itself after a role change, a business rule that sits in a controller." Two facts are chained with stacked appositives. Suggested: "`AuthController` has thirteen `private final` fields, twelve of them collaborators and several of them Spring Security internals. And `UserAdminController` revokes a user's sessions itself after a role change: a business rule that sits in a controller."

**P39-5. Chapter 39, Section 39.17, Step 1.** "no MFA" appears before the term is spelled out (multi-factor authentication appears in Step 2). Suggested: "no multi-factor authentication (MFA)" in Step 1.

**P39-6. Chapter 39 Solutions, Exercise 39.4.** Bare numbers "(37.5)", "(37.7)", "decision 37.11", "the plan in 37.17" are not typed references. Suggested: "(Section 37.5)", "Section 37.11", "Section 37.17".

**P39-7. Chapter 39, Section 39.1.** I turned "The analogy breaks down in two ways." into "**Where the analogy breaks down:** in two ways." to meet STYLE 8; the author may prefer to fold the two points into the standard lead-in wording. Flagged for confirmation.

**P38-1. Chapter 38, Section 38.1.** The sentence before the five-part numbered list ends with a period ("...any pattern you meet."); STYLE 3 wants a colon. Suggested: "Every pattern in this chapter is described in the same five parts, so that you learn to ask the same questions of any pattern you meet:".

**P40-1. Chapter 40, Section 40.5, "Idle timeout".** `**keepalive_timeout**` is bold, but it is a configuration key, and STYLE 9 says identifiers are never bold. The same applies to `**stopTimeout**` in Chapter 41, Section 41.6 (both are also glossary entries). Suggested: code font, with the definition kept in the sentence.

**P40-2. Chapter 40, Section 40.2, term list.** The last two items ("Idle cost, from Section 40.1: ..." and "Infrastructure as code (Chapter 39, Section 39.13) means ...") do not follow the `**Term:** definition` pattern of the other items. Also "ECS" is used in the Task item before Table 40.1 expands it. Suggested: make both bold-term items, and write "Amazon ECS (Elastic Container Service)" in the Task item.

**P40-3. Chapter 40, Section 40.9, first paragraph.** "Amazon ElastiCache runs **Valkey** or Redis OSS (Valkey is an open-source, Redis-compatible in-memory store), a store that keeps its data in memory and that all tasks can share." The appositive reads as describing Redis OSS only. Suggested: "Amazon ElastiCache runs **Valkey** or Redis OSS. Valkey is an open-source, Redis-compatible in-memory store; either one keeps its data in memory, and all tasks can share it."

**P40-4. Chapter 40, Section 40.4.** "At 80 readers the book would take RDS and S3 first" uses "the book" as the speaker and introduces "80 readers" without context (Chapter 41 uses it in Exercises 41.1 and 41.4). Suggested: "For a service of about 80 readers, take RDS and S3 first, and add a second copy..."

**P40-5. Chapter 40, Common mistakes, last bullet.** "unsharing that stops working on open pages" is unclear at this point ("unsharing" is explained in Chapter 41, Section 41.8). Suggested: "revoked shares that stop working on open pages".

**P41-1. Chapter 41, Table 41.1.** Status codes are plain numbers ("a 403 or 404", "401, 403, 404, 410 and 429") whereas the rest of the chapter uses code font (`412`, `403`). Suggested: code font throughout, with a serial comma.

**E-1. Epilogue, "Part III built the browser side".** "the interceptor is where cookies, tokens and errors are handled in one place" does not match Chapter 38, which says the CSRF header is Angular's built-in behavior and the project's interceptor only records activity and handles `401`. Suggested: "the interceptor is where a `401` from any call is handled in one place".

**E-2. Epilogue, "What the app does and does not defend", fifth bullet.** "**Sessions and counters live in memory,** so the application runs as one instance, and its pages are images with no text layer for screen readers." Two unrelated limitations share one bullet. Suggested: split into two bullets.

**B-1. Appendix B, text descriptions for Figures B.4 and B.6.** Both begin "A left-to-right flowchart", but the Mermaid sources are `flowchart TB` (top to bottom). Suggested: "A top-to-bottom flowchart" in both. Figures B.1, B.2, B.3 and B.5 are `LR` and correct. Not changed because the text description is tied to the Mermaid block the owner controls.

**D-1. Appendix D, last table ("End-to-end tests").** Verbs are third person ("Starts", "Shows", "Installs"), whereas every other table uses the imperative ("Start", "Show", "Install"). Suggested: make the last table imperative.

**R-1. README part table.** The "Appendices" row lists "Glossary, blueprint history, exercise solutions" but the book also has the epilogue, Appendix D (command reference), Appendix E (troubleshooting) and the index. Suggested: "Epilogue, glossary, blueprint history, exercise solutions, command reference, troubleshooting, index".

**G1. Glossary source, entry "recognised device" (`_team/glossary_src.tsv` line 286).** The term key is British. It is a bolded term in Chapters 30 and 37 and appears in `index-terms.md` and Appendix C ("recognised-device design"), all outside this section. Changing only the glossary would break the match, so I left it. Suggested: a book-wide change to "recognized device" (code names such as `recognisedDevice` stay).

**G2. Glossary source, entry "interceptor" (line 182).** "used for the CSRF header and for reacting to `401`" contradicts Chapter 38, Section 38.3 (the CSRF header comes from Angular's built-in support, not project code). Suggested: "a function in Angular's HTTP client that sees every request and response; the project's one interceptor records activity and reacts to `401`."

**G3. Glossary source, entry "audit trail" (line 31).** "The permanent, append-only record..." conflicts with the 180-day purge and Chapter 39, Section 39.10 ("append-only is a convention, not a guarantee"). Suggested: "The append-only record, kept for a set retention period (180 days by default), ...".

## Checked and found consistent

- The "(source N)" markers in Chapter 40 (35 sources) and Chapter 41 (20 sources) run in order of first citation, and every number matches the numbered Sources list. Repeated citations (Chapter 41, Section 41.5, source 8) are correct.
- Figure, Listing, Example and Table numbering in Chapters 38 to 41 is continuous and each item is referred to in the text.
- Tier headings, the "you can skip" italic lines and heading levels in Chapters 38 to 41 match STYLE 6 and 7. Headings use sentence case.
- No emojis and no hype words ("simply", "just", "easy", "obviously", "robust", "seamless") found.
- Product names checked: Amazon Web Services, Amazon S3, Amazon RDS, Amazon ECS, AWS Fargate, Elastic Load Balancing, Application Load Balancer, ElastiCache, AWS Secrets Manager, CloudFront, Terraform, GitHub Actions, Spring Session, Redis, Valkey. No misspellings; short forms (S3, RDS, ECS, ALB) follow the full names.
- Appendix E has no findings. Appendix B prose has no findings apart from B-1.

## Not checked

- Technical claims, code, versions and numbers (out of scope).
- Appendix A, Appendix C and the index (generated; only the source files were proofread).
- Whether each glossary term matches its bolded first use in the chapters.
