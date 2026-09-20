# Proofread 4: Chapters 25 to 31, their solutions files, and the blueprints

Scope: `book/part-4-building-the-app/25-*.md` to `31-*.md` (chapters and `.solutions.md` files) and `book/blueprints/*.md` (prose only). Proofread against `book/STYLE.md`. No commits, no builds, nothing under `book/build/` touched. Code blocks, inline code, Mermaid, HTML comments, quoted text, tables' technical content, and numbers that are facts were left unchanged.

## Terminology decisions applied

- sign in (verb) / sign-in (noun, adjective): "Login accepted any username" became "Sign-in accepted..." (Ch 25); "login throttling" became "sign-in throttling" (Ch 26); "signing out" for "logging out" (Ch 25); "login screen" became "sign-in screen" (Ch 29). Code identifiers, endpoint paths, and quoted comments keep `login`.
- American spelling: "recognised" became "recognized" everywhere in prose (Ch 30, 30 solutions, about 12 places). `isRecognised` (code) untouched. Blueprint v5 already used "recognized".
- email: "e-mail address" became "email address" (Ch 30).
- Serial (Oxford) comma applied to three-or-more-item lists wherever found (see counts). This was regex-assisted plus reading, so a few may remain.
- Tags in captions and headers of tables and figures set in code font (for example `book-m1-accounts`), matching Figure 25.2, 26.2, and the STYLE rule "milestone code by tag in code font". Listing captions were left as the STYLE example shows them (tag not in code font).
- Closing quotation marks: periods and commas moved inside the quotation mark (American style, STYLE section 3) where the quotation is prose, not code.
- Bold used for emphasis changed to italics (STYLE section 9); bold kept for defined terms and fixed lead-ins.
- "**The lesson:**" (colon inside bold) made "**The lesson.**" to match every other lead-in in Part IV.
- Numbers for limits and units as numerals: "limit of five" became "limit of 5"; "Eighteen of eighteen checks" became "All 18 checks".
- Compound modifiers hyphenated: "40-million-pixel limit", "256-pixel tiles".
- The AI reviewers: names vary across the section (see Query Q1); no edit made.

## Edits made

Format: file, place, before -> after.

### Chapter 25 (`25-m0-the-tiled-viewer.md`)

1. Section 25.3, "35 tiles per page" bold -> italics (emphasis, not a defined term).
2. Section 25.4, `"...serve directly", because` -> `"...serve directly," because` (quote punctuation).
3. Section 25.7, `"...losing shared caching", which` -> `caching," which`.
4. Section 25.8, "or logging out and retrying" -> "or signing out and retrying".
5. Section 25.10, "**one after another**" -> "*one after another*" (bold for emphasis).
6. Section 25.11, `"would make it slow". The` -> `"would make it slow." The`.
7. Figure 25.1 caption: "(book-m0-mvp)" -> "(`book-m0-mvp`)".
8. Table 25.2 caption: "(at book-m0-mvp)" -> "(at `book-m0-mvp`)".
9. Section 25.1: "five unit-test classes and one" -> "five unit-test classes, and one".
10. Challenge, "Login accepted any username" -> "Sign-in accepted any username".

### Chapter 25 solutions
No edits.

### Chapter 26 (`26-m1-accounts.md`)

1. Section 26.1, "login throttling and a seeded first admin" -> "sign-in throttling, and a seeded first admin".
2. Table 26.1, Table 26.2, Table 26.3 captions, Figure 26.1 caption: tag put in code font (4 edits).
3. Section 26.3, `"...created or changed". Its` -> `changed." Its`.
4. Section 26.7, `"...for the SPA to read". In` -> `read." In`.
5. Section 26.11, `"...from everywhere", and per address` -> `everywhere," and`; `"...capped too". A` -> `too." A`; `"...letting a request through".` -> `through."`.
6. Section 26.11, "a limit of five." -> "a limit of 5.".
7. Section 26.12, `"...anyone who had it".` -> `it."`.
8. Section 26.13, `"...exposed to the network". The` -> `network." The`; `"...on the command line". And` -> `line." And`.
9. Section 26.13, "spring configuration imports" -> "Spring configuration imports" (product name capitalization).
10. Section 26.14, `"...no test CSRF helper". Its` -> `helper." Its`.
11. Serial commas (8): "Unknown user, wrong password, and disabled account"; "manage accounts, sessions, and the audit log"; "sign-in, document list, upload, viewer, and admin pages"; "Login, form login, HTTP basic, and Spring's own logout"; "disable accounts, and reset passwords"; "throttling, lockout, and password-change logic"; and item 1 above.

### Chapter 26 solutions (`26-m1-accounts.solutions.md`)

1. Exercise 26.3: added the missing blank line before "(All of them also need an acceptable password...)" so it is a paragraph and not a lazy continuation of the last list item (broken Markdown).

### Chapter 27 (`27-m2-documents.md`)

1. Objectives: "stored, filtered and exported" -> "stored, filtered, and exported".
2. Section 27.1: "no delete, rename or replace" -> "delete, rename, or replace"; "truncated ids and no filters" -> "truncated ids, and no filters"; "no owner, date or search" -> "owner, date, or search".
3. Table 27.1 and Table 27.2 captions: tag in code font (2 edits).
4. Section 27.2 analogy: `"no such drawer", not "that drawer is locked".` -> `"no such drawer," not "that drawer is locked."`.
5. Section 27.4: `say "this class is a table".` -> `table."`.
6. Section 27.5: `"...for the audit record". Empty means "not visible or not there", and` -> `record." ... there," and`.
7. Section 27.8: `"...whoever called me". The` -> `me." The`.
8. Section 27.9: "`=`, `+`, `-` or `@`" -> "`=`, `+`, `-`, or `@`".
9. Section 27.7: "by type, user and document" -> "by type, user, and document".
10. Section 27.11: `"...never-hand-out-the-document design". The` -> `design." The`; "**The lesson:** keep" -> "**The lesson.** Keep".
11. Section 27.12: "**and**" bold -> "*and*" (emphasis); `"deliberately conservative". A` -> `conservative." A`; `"a period of time, six hours". One` -> `hours." One`.
12. Section 27.13: "the owner, the date and a visibility badge" -> "the owner, the date, and a visibility badge".
13. Decisions: "filters, paging, export and a retention purge" -> "export, and a retention purge".
14. Summary: "an owner, a visibility and optional shares" -> "a visibility, and optional shares".

### Chapter 27 solutions
No edits.

### Chapter 28 (`28-m3-hardening.md`)

1. Prerequisites: "3a, 3b and 3c" -> "3a, 3b, and 3c".
2. Section 28.1: "roles, ownership and an audit trail" -> "ownership, and an audit trail"; "type, page count or size" -> "page count, or size".
3. Table 28.1 and Table 28.3 captions: tag in code font (2 edits).
4. Section 28.7: "an SQL statement or a file path" -> "an SQL statement, or a file path".
5. Section 28.8: `"the server still enforces it". Browser` -> `it." Browser`.
6. Section 28.9: `"...the allowed sources are: none". A page` -> `none." A page`; "an image or a font" -> "an image, or a font".
7. Section 28.10: `"is the app alive?". Spring` -> `alive?" Spring`; "the database's name, disk space or any component" -> "disk space, or any component"; "(401, 403 or 404)" -> "(401, 403, or 404)".
8. Exercise 28.3: "40 million pixel limit" -> "40-million-pixel limit".
9. Summary: "the signature, the page count and the page pixels" -> "the page count, and the page pixels".

### Chapter 28 solutions (`28-m3-hardening.solutions.md`)

1. Exercise 28.2: "class names or other internals" -> "class names, or other internals".
2. Exercise 28.5: "`X-Content-Type-Options: nosniff` and `X-Frame-Options: DENY`" -> "nosniff`, and `X-Frame-Options...".

### Chapter 29 (`29-m4-reading.md`)

1. Prerequisites: "4a, 4b and 4c" -> "4a, 4b, and 4c"; "services and testing" -> "services, and testing".
2. Section 29.2: bare code fence given the language `text` (STYLE 11.4: always a language on the fence).
3. Section 29.3: "`?page=0` or `?page=9999`" -> "`?page=0`, or `?page=9999`"; "the signed-in username and the document id" -> "username, and the document id"; `"resuming is just a convenience".` -> `convenience."`.
4. Section 29.4: `"...pass me the event object".` -> `object."`; "`nextPage`, `prevPage`, `loadPage`," -> "`nextPage`, `prevPage`, and `loadPage`,".
5. Section 29.5: "find a login screen" -> "find a sign-in screen".
6. Section 29.8: "**Stay signed in**" -> "*Stay signed in*" (UI labels in italics, STYLE 11.4).
7. Section 29.9: "Worked example." -> "**Worked example.**" (matches every other chapter).
8. Section 29.10: "a UTC timestamp and a short trace code" -> "timestamp, and".
9. Section 29.12: "`I`, `l`, `O` and `0`" -> "`O`, and `0`"; "except I, L, O and U" -> "O, and U"; Decisions "no I, L, O or U" -> "O, or U".
10. Blueprint v4 paragraph: "the deep links and the keyboard handling ... the trace code and the trace filter" -> Oxford commas (2).
11. Table 29.2 caption: tag in code font.

### Chapter 29 solutions (`29-m4-reading.solutions.md`)

1. Exercise 29.2: "`INPUT`, `TEXTAREA` or `SELECT`" and "Ctrl, Cmd or Alt" -> serial commas.

### Chapter 30 (`30-m5-platform.md`)

1. "recognised" -> "recognized" in prose (heading of 30.11, Table 30.2 text, body, Decisions) via replace-all; "Recognised devices" -> "Recognized devices". Code `isRecognised` untouched.
2. Section 30.1: "no continuous integration and no Maven wrapper" -> "integration, and no"; "containers, a CI pipeline and end-to-end tests" -> "pipeline, and end-to-end tests"; same in Summary; Summary "digest pins and health checks make images small, safe and repeatable" -> Oxford commas.
3. Table 30.1: header "After (book-m5-platform)" -> code font; "Hibernate 7 and Flyway 12" -> "Hibernate 7, and Flyway 12". Table 30.4 caption: tag in code font.
4. Section 30.3: "stopped, restarted and have data" -> "restarted, and have"; "(`web`, which is nginx) and an optional" -> "nginx), and an optional".
5. Section 30.4: `"whatever 25-jdk means today". A tag` -> `today." A tag`; comma splice "pinning doesn't freeze the image, it makes updates deliberate" -> "...image; it makes updates deliberate".
6. Section 30.5: "plain HTML, CSS and JavaScript" -> "CSS, and JavaScript".
7. Section 30.8: `a "Correction". The technical` -> `"Correction." The`; "**replaces**" -> "*replaces*" (emphasis); `regular expression rules". A separate` -> `rules." A separate`; "**The lesson:** a forwarded-address" -> "**The lesson.** A forwarded-address".
8. Section 30.9: "an e-mail address" -> "an email address".
9. Section 30.10: `"must change password", and is` -> `password," and is`.
10. Section 30.11: "Eighteen of eighteen checks passed." -> "All 18 checks passed."
11. Section 30.12: "session, document, page and interval" -> "page, and interval"; "verified a token, checked a session, read a tile and stamped it" -> Oxford; "reading, watermarking and encoding a PNG" -> Oxford.
12. Section 30.15: "with 256 pixel tiles" -> "with 256-pixel tiles".
13. Section 30.16: "**The lesson:** store instants" -> "**The lesson.** Store instants"; "sign-in outcomes, render time and rejected renders" -> Oxford; Section 30.1 tests line "114 backend, 31 frontend and an end-to-end run" -> "31 frontend, and".

### Chapter 30 solutions (`30-m5-platform.solutions.md`)

1. Exercise 30.3: "unrecognised" / "recognised" -> "unrecognized" / "recognized" (2 places).

### Chapter 31 (`31-m6-final.md`)

1. Prerequisites: "its lock file and `TileGenerationServiceTest`" -> "its lock file, and".
2. Section 31.2: "Spring, PDFBox and a MySQL driver; ... Angular, Vitest and dozens more" -> Oxford commas; "oil changes, recalls and inspections" -> Oxford.
3. Section 31.3: "npm, Docker, Compose and GitHub Actions" -> Oxford.
4. Section 31.4: "major, minor and patch" -> "major, minor, and patch".
5. Section 31.5: "**Table 31.1**" bold inside a sentence -> plain "Table 31.1".
6. Section 31.6: "only shrinks the gap, not closes it" -> "only shrinks the gap and does not close it" (ungrammatical parallel).
7. Table 31.2 and Table 31.3 captions: tag in code font.
8. Heading 31.10: "a limit of five" -> "a limit of 5".
9. Section 31.12: `"drop this once Boot manages 11.0.25+".` -> `11.0.25+."`.
10. Blueprint v6 section: "policy, tooling and one test" -> "tooling, and"; "**What changed since v5:**" -> "What changed since v5:" (plain, matching Chapters 26, 27).
11. Exercise 31.1: "`~6.0.2` and `5.0.1`" -> "`~6.0.2`, and `5.0.1`"; Section 31.11: "two, three or four bytes" -> "three, or four bytes".

### Chapter 31 solutions
No edits.

### Blueprints

1. `README.md`: "They are a design and not a milestone, so it is not part of the v0 to v6 series." -> "so they are not part of" (agreement).
2. `v1-accounts.md`: "MySQL 8.4 and an Angular 22 frontend" -> "MySQL 8.4, and"; "rate-limit usage and the audit log" -> "usage, and".
3. `v2-documents.md`: "replace file, delete and shares" -> "delete, and shares".
4. `v3-hardening.md`: three serial-comma edits ("unsupported media type, and a catch-all"; "`Referrer-Policy: no-referrer`, and a Permissions-Policy"; "security headers, and a health check added").
5. `v4-reading.md`: "keyboard navigation, and resuming"; "Crockford base32 string" -> "Crockford Base32 string" (matches Chapter 29 capitalization).
6. `v5-platform.md`: "`WatermarkService`, and `AuditLogService` still exist".
7. `v6-final.md`: "dependency updates, and the Dependabot policy only".

### Edit counts (approximate, by type)

- Quotation-mark punctuation (period or comma inside): about 31
- Serial (Oxford) commas: about 45
- Terminology and spelling (sign-in, recognized, email, Spring, Base32): about 20
- Table, figure, and heading tag in code font: 13
- Bold-for-emphasis or UI-label markup fixed: 7
- Numbers and hyphenation ("limit of 5", "All 18 checks", "40-million-pixel", "256-pixel"): 6
- Broken Markdown (missing blank line, unlabeled fence): 2
- Grammar or agreement (comma splice, "not closes", "it is not part"): 3
- Lead-in consistency ("The lesson:" to "The lesson.", "Worked example." bold): 6

## Queries for the author (not edited)

Q1. **AI reviewers named inconsistently.** Ch 26.1: "an AI agent playing a product owner (PO) and one playing a senior technical manager (TM)". Ch 27.1: "AI review agents playing a product owner and a senior technical manager", later "the AI product-owner reviewer". Ch 28: "The technical review (an AI review agent playing a senior technical manager)" and "The AI product-owner reviewer". Ch 29: "The AI product-owner reviewer (an AI agent playing the product owner)". Ch 30: "the technical review" and "the AI product-owner reviewer's re-review". Ch 31: "The technical manager and product owner reviewers, which were AI review agents". Suggested single form after the first mention: "the PO review" / "the TM review" (STYLE 14), with "the AI product-owner reviewer" dropped or used everywhere. Risk of confusing the AI "product owner (PO)" with the project owner who decides ("The product owner, asked directly, chose built-in accounts", Ch 26.1). Suggested: in Chapters 26 to 31 write "the project owner" for decisions if that is who is meant, or keep "the product owner" but add once, in Ch 26.1, "the product owner (the project's owner, not the AI reviewer)".

Q2. **Ch 25, Section 25.2, defined terms not bold.** "A tile is one rectangular piece...", "A token is...", "A session is...", "A watermark is..." are defined without bold, though "Rasterizing" and "pixels" are bold, and Section 25.1 already used "tile", "session", and "watermark". Similar: Ch 26.2 bolds "account", "password hash", "httpOnly", "SameSite=Strict" but not "role", "session", "cookie"; Ch 28.4 defines "decompression bomb" without bold; Ch 29.2 "deep link" not bold; Ch 31.2 "LTS", "flaky test", "dependency" not bold, and "ultrareview" is bolded at Section 31.15 although first used in Section 31.9 (and in Ch 30). Also "sliding" (Ch 26.6 and Ch 29.5) and "admin handle" (Ch 26.10 and Ch 29.11) are bold twice. Suggest a bold-at-first-use pass with the glossary.

Q3. **Ch 25, Section 25.1 heading** "Requirements and the threat we start with": STYLE 2 says never use "we" for project history. Suggested: "Requirements and the starting threat".

Q4. **Ch 25, Section 25.6, step 3 of the numbered list.** "The comparison uses `MessageDigest.isEqual`, a constant-time comparison, a check whose running time does not depend on how many characters match: an ordinary string comparison stops..." is hard to parse (three appositives). Suggested: "The comparison uses `MessageDigest.isEqual`, a constant-time comparison: its running time does not depend on how many characters match. An ordinary string comparison stops at the first difference, so an attacker who measures response times could learn how many leading characters were right."

Q5. **Ch 26, Section 26.1, second bullet.** "The admin endpoints needed only a valid session, and they listed every live session id, and the session id was the only credential" (two "and" clauses). Suggested: "The admin endpoints needed only a valid session and listed every live session id, and the session id was the only credential (`TM-1`, `PO-2`)."

Q6. **Ch 26, Section 26.7, last paragraph.** "Section 26.14 tells the bug that made it necessary." Suggested: "Section 26.14 tells the story of the bug that made it necessary."

Q7. **Ch 26, Section 26.13.** Sentence "And the service is only for development: ..." starts a sentence with "And" after a very long sentence and follows a list-like run. Suggested: "The service is only for development: the app itself still runs from your editor at this tag."

Q8. **Ch 29, Section 29.4, last sentence.** "The tests for this milestone cover these behaviors (Section 29.11)." Section 29.11 is "The trace code"; no section of the chapter covers the tests. Suggested: point to Table 29.2 or the "In this project" section (test list), or to the exercises.

Q9. **Ch 29, Section 29.7, last paragraph.** "Table 29.1 rows are the same four cases the project's `idle.spec.ts` asserts: 10 minutes active, 26 minutes warning..., 30 minutes expired, and the scaled short-timeout case." Table 29.1 has rows for 10, 25, 26, and 30 minutes and no short-timeout row, so "the same four cases" is inaccurate. Suggested: "Three of the rows in Table 29.1 (10, 26, and 30 minutes) are cases the project's `idle.spec.ts` asserts; the fourth test covers the scaled short-timeout case."

Q10. **Ch 29, Section 29.11 and Ch 30, Section 30.13.** Code blocks taken from the repository have no caption or tag: the `TileController` excerpt (`String traceCode = ...`) in Section 29.11, and the `canonicalString` line in Section 30.13. STYLE 11.1 says code from the repository is a Listing with a caption and tag. Suggested: add "Listing 29.x" and "Listing 30.x" captions with an "excerpt" label, or mark them as Examples if written for teaching. Also Ch 28.8, the unlabeled `next: (event) => {...}` snippet under "Progress in two stages".

Q11. **Ch 29, Section 29.3, prose UI labels.** "exactly as a click on Next would" (29.2); "press Delete and then confirm" (27.13); "Manage page", "replace PDF" control: STYLE 11.4 puts UI labels in italics with exact capitalization (*Next*, *Delete*). Left unchanged except "Stay signed in", which I italicized. Suggest a pass.

Q12. **Ch 30, Section 30.3.** "an optional HTTPS front end (`tls`, which is Caddy)": STYLE word list says "frontend" (never "front end"), but here Caddy is a proxy in front of nginx, and "frontend" is the Angular app elsewhere. Suggested: "an optional HTTPS proxy (`tls`, which is Caddy)".

Q13. **Ch 30, Section 30.6, bullet `env_file`.** "loads secrets from a file that is not in git (the secrets rule from Chapter 28)". Chapter 28 (upload and API hardening) does not introduce the secrets rule; the git-ignored `.env` appears in Chapter 26 (Section 26.13). Suggested: "(the `.env` rule from Section 26.13)".

Q14. **Ch 30, Section 30.6, bullet about health check.** "A **health check** is a command Docker runs repeatedly; the app's calls the health endpoint from Chapter 28." Missing word. Suggested: "the app's health check calls the health endpoint from Chapter 28."

Q15. **Ch 30, Section 30.9.** "an **HSTS** header" is bold but never spelled out. STYLE 9: spell out at first use. Suggested: "an **HTTP Strict Transport Security (HSTS)** header, which tells browsers...". Similar: "CSP" in Ch 28.9 is used ("the CSP contains", "What does the CSP mean?") without the expansion "Content Security Policy (CSP)" in prose; "SPA" in Ch 26.7 appears in a quote and in prose without expansion.

Q16. **Ch 30, Section 30.15, numbers.** "about 30 minutes to copy a 500-page document by script" (paragraph on the options laid out for the product owner) versus "about 33 minutes" a few lines earlier and in the Decisions section. Confirm which the source records, or make both "about 33 minutes".

Q17. **Ch 30, Section 30.14, item 2.** `answers 400 "This PDF took too long to prepare. Try a smaller or simpler file.", and tells the render to stop` mixes quote and comma. Suggested: "answers 400 with the message "This PDF took too long to prepare. Try a smaller or simpler file." and tells the render to stop at its next page boundary."

Q18. **Ch 31, Section 31.1.** "CI" is used in the third paragraph before Section 31.2 defines it. (It was also defined in Chapter 30 Section 30.1.) Suggested: "continuous integration (CI)" at first use in this chapter, or a cross-reference.

Q19. **Ch 27, Section 27.5, "Access is checked in four places".** Text lists list, manifest, tile URLs, tile request: consistent. No change. (Noted only because the STATUS open item "five gates later" for Ch 30 was not seen: Ch 30 says "now six gates" with Table 30.3 listing six.)

Q20. **Code identifiers with British spelling** (left alone, they are code): `recognisedDevice` (Ch 31, Listing 31.4), `isRecognised` (Ch 30, Listing 30.7 and prose). The prose refers to `isRecognised` in code font. No change needed unless the repository is renamed.

Q21. **List punctuation.** Several fragment-only lists end each item with a period (for example Ch 31, Section 31.3 and Section 31.16; blueprint "What changed" lists). STYLE 3: fragment lists have no final periods. Not edited because the lists are internally consistent; a global decision is needed.

Q22. **Ch 28, Section 28.1, list parallelism.** "uploading a corrupt or non-PDF file returned a raw internal error, gave no progress feedback, and the title was not prefilled". Last clause changes subject. Suggested: "...returned a raw internal error, gave no progress feedback, and did not prefill the title".

Q23. **Ch 26, Section 26.2 vocabulary list and Ch 25 Section 25.6**: the first list item uses "An **account**..." and the next uses "A **password hash**..." (bold), while items 3 to 5 have no bold; see Q2.

Q24. **Decisions sections use the STYLE order "The decision, The options considered, Why this one, What it costs" inconsistently.** Ch 25 "Decision: tiles plus signed URLs" puts "Why this one" before "The options considered"; Ch 26 puts them in the STYLE order; Ch 27 to 31 vary. Cosmetic.

## Coverage notes

Every file in scope was read completely, including all `.solutions.md` files and all eight blueprint files. Mermaid blocks, code blocks, and HTML comments were read but not edited. The Oxford-comma pass combined careful reading with regex searches; a few three-item lists inside long paragraphs may still lack the serial comma. British-spelling and word-list searches (`login`, `log in`, `e-mail`, `front end`, `back end`, `-ise`, `-our`) found only code, endpoint paths, and the one "front end" in Query Q12.
