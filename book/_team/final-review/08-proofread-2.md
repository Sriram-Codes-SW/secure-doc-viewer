# Proofread 2: Part II (book/part-2-backend)

Scope: `00-part-introduction.md`, Chapters 11-18 and their `.solutions.md` files (17 files, about 52,500 words). Every file was read in full. Code blocks, inline code, Mermaid diagrams, listing contents, HTML comments, quoted text and file paths were not changed. Nothing was committed and nothing under `book/build/` was touched.

Overall verdict: the prose is in very good condition. No spelling errors, doubled words, unclosed bold or italics, malformed tables or heading-level errors were found. Almost all edits are word-list (STYLE 3.1) and small mechanical fixes.

## Terminology decisions applied

| Term | Decision | Reason |
|---|---|---|
| startup (noun/adjective) | "startup", not "start-up" | Chapter 11 and the rest of Part II already use "startup" (for example "fail at startup"). |
| sign in / sign-in | "login", "logout" in running prose replaced by "sign-in", "sign-out" | STYLE 3.1 word list. Code names (`/api/auth/login`, `LoginThrottle`, `login(...)`) untouched. |
| administrator / admin | "administrator" in prose; "admin" kept for UI, code and class names (`admin page`, `ADMIN`, `AdminController`) | STYLE 3.1. |
| filename | "filename", not "file name" | STYLE 3.1. |
| recognized | "recognized device" (American) in prose | STYLE 3. Code (`isRecognised`, `recognisedDevice`) and quoted source text left as is. |
| numerals | 10 and above as numerals ("12 threads", "20 things", "19") | STYLE 3. |
| hype words | "easy" replaced in Chapter 11 and 15 solutions | STYLE 2 (no "easy"). "just" inside a quoted source comment (Chapter 14, Section 14.8) kept. |

## Edits made (34 rows)

Type key: WL = word-list/terminology, SP = spelling (American), GR = grammar, PU = punctuation, NU = number style, MD = Markdown.

| # | File | Section | Before -> After | Type |
|---|---|---|---|---|
| 1 | 11-spring-boot-foundations.md | 11.2, step 4 | "Run start-up tasks" -> "Run startup tasks" | WL |
| 2 | 11-spring-boot-foundations.md | Table 11.4 | "Start-up task" -> "Startup task" | WL |
| 3 | 11-spring-boot-foundations.md | Summary | "a start-up runner is" -> "a startup runner is" | WL |
| 4 | 11-spring-boot-foundations.solutions.md | Exercise 11.6 | "Also easy: the property name" -> "Also clear: the property name" | WL (hype) |
| 5 | 11-spring-boot-foundations.solutions.md | Exercise 11.6 | "This one is easy to act on too" -> "This one is straightforward to act on too" | WL (hype) |
| 6 | 11-spring-boot-foundations.solutions.md | Exercise 11.6 | "What makes a message easy to act on" -> "...straightforward to act on" | WL (hype) |
| 7 | 12-rest-controllers-and-json.md | 12.9 | "publishers and admins, returns" -> "publishers and administrators, returns" | WL |
| 8 | 13-validation-and-errors.md | Prerequisites (end) | Added missing blank line between the last list item and `## Beginner tier` | MD |
| 9 | 13-validation-and-errors.md | Table 13.3, row 1 | "publishers and admins may upload" -> "publishers and administrators may upload" | WL |
| 10 | 13-validation-and-errors.md | 13.9 | "the file *name* or the declared content type" -> "the *filename* or ..." | WL |
| 11 | 14-jpa-and-flyway.md | 14.5 | "The file name follows a rule" -> "The filename follows a rule" | WL |
| 12 | 15-spring-security-authentication.md | 15.2 | "handles a logout address" -> "handles a sign-out address" | WL |
| 13 | 15-spring-security-authentication.md | 15.7 | "and admins can additionally manage" -> "and administrators can additionally manage" | WL |
| 14 | 15-spring-security-authentication.md | 15.8 | "We simplify here about one thing:" -> "We simplify one thing here:" | GR |
| 15 | 15-spring-security-authentication.md | 15.10 | "a demo login is a decision" -> "a demo sign-in is a decision" | WL |
| 16 | 15-spring-security-authentication.solutions.md | Exercise 15.5 | "\"immediately\" is easy." -> "\"immediately\" is simple." | WL (hype) |
| 17 | 16-spring-security-defenses.md | 16.3 | "Note the order matters:" -> "Note that the order matters:" | GR |
| 18 | 16-spring-security-defenses.md | 16.6 intro | "recognised-device exemption" -> "recognized-device exemption" | SP |
| 19 | 16-spring-security-defenses.md | Figure 16.1 caption | "recognised-device exemption" -> "recognized-device exemption" | SP |
| 20 | 16-spring-security-defenses.md | Figure 16.1 text description | "recognised ... A recognised device ... an unrecognised one" -> "recognized ... A recognized device ... an unrecognized one" (3 words) | SP |
| 21 | 16-spring-security-defenses.md | 16.6, after Figure 16.1 | "including a recognised device" -> "including a recognized device" | SP |
| 22 | 16-spring-security-defenses.md | 16.6, lockout paragraph | bold term "**recognised device**" -> "**recognized device**" | SP |
| 23 | 16-spring-security-defenses.md | Table 16.3 | "Throttling and recognised devices" -> "Throttling and recognized devices" | SP |
| 24 | 16-spring-security-defenses.md | Summary | "recognised devices stop a lockout" -> "recognized devices stop a lockout" | SP |
| 25 | 16-spring-security-defenses.md | 16.10 | `"doesn't exist".**` -> `"doesn't exist."**` (period inside the quotation marks) | PU |
| 26 | 16-spring-security-defenses.solutions.md | Exercise 16.6 | "list of recognised devices" -> "list of recognized devices" | SP |
| 27 | 16-spring-security-defenses.solutions.md | Exercise 16.5 | "all twelve threads" -> "all 12 threads" | NU |
| 28 | 16-spring-security-defenses.solutions.md | Exercise 16.4(b) | "or the admin role" -> "or the administrator role" | WL |
| 29 | 17-files-images-pdfs-signatures.md | 17.5 | "and change a single character of the message and the fingerprint changes completely" -> "and if you change a single character of the message, the fingerprint changes completely" (broken parallel structure) | GR |
| 30 | 17-files-images-pdfs-signatures.solutions.md | Exercise 17.6, step 4 | "the new file name" -> "the new filename" | WL |
| 31 | 18-testing-the-backend.md | Opening paragraph | "twelve parallel password guesses don't become twelve guesses" -> "12 parallel ... 12 guesses" | NU |
| 32 | 18-testing-the-backend.md | 18.4 | "asserts twenty things ... the other nineteen" -> "asserts 20 things ... the other 19" | NU |
| 33 | 18-testing-the-backend.md | 18.11 | "makes a render time out, and then asserted" -> "made a render time out, and then asserted" (tense agreement) | GR |
| 34 | 18-testing-the-backend.solutions.md | Exercise 18.5 | "the twelve requests may run" and "the twelve requests all pass" -> "the 12 requests ..." (2 places) | NU |

(Rows 20 and 34 each cover several words.)

### Edit counts by type

- Word list / terminology (WL): 16 (of which 4 are hype-word removals)
- Spelling, American English (SP): 8 rows (10 individual words)
- Grammar (GR): 4
- Number style (NU): 4 rows
- Punctuation (PU): 1
- Markdown (MD): 1

## Queries for the author (nothing below was changed)

**Q1. Chapter 11, Section 11.3 (and Chapter 14, Section 14.2): "repository" is defined but not in bold.**
Sentence: "A **controller** is a class that answers web requests ... A **service** is a class that holds the rules ... A repository is a class that reads and writes the database; Chapter 14 covers it." and, in Chapter 14, "A repository is an interface through which you load and save entities."
Problem: STYLE 9 wants each new term in bold at first use, and the surrounding terms are bold. Also confirm that "repository" was not already bolded in Chapter 9 or 4.
Suggested: bold "**repository**" in Section 11.3 (first use) and leave Chapter 14 plain, or bold it in Chapter 14 only and reword the Chapter 11 sentence to avoid a definition.

**Q2. Chapter 14, Section 14.9, "A real incident: audit events from the future": sentence fragment and tense shift.**
Sentence: "*The fix:* pin the JDBC connection to UTC and show UTC in the admin screen so it matches the watermark and the CSV export. Also add a test that runs the JVM in Asia/Kolkata ..."
Problem: the incident is told in the past tense, but the last sentence is an imperative fragment.
Suggested: "*The fix:* the team pinned the JDBC connection to UTC and showed UTC in the admin screen so it matches the watermark and the CSV export. It also added a test that runs the JVM in Asia/Kolkata against a MySQL server set to `-03:00` and checks that stored values are UTC."

**Q3. Chapter 16, Figure 16.1 (Mermaid): the node label "Recognised device?" is British spelling.**
Everything else in Part II prose now reads "recognized" (edits 18-24, 26). The Mermaid diagram was off limits. Suggested: change the label to "Recognized device?" (the code identifiers `recognisedDevice` and `isRecognised` are correctly left alone).

**Q4. Chapter 16, Section 16.7, "The incident": tense inconsistency.**
Sentence: "The fix made nginx overwrite the header with the real peer address, and later the app trusts the header only from the proxy's fixed address."
Suggested: "The fix made nginx overwrite the header with the real peer address, and a later change made the app trust the header only from the proxy's fixed address."

**Q5. Chapter 17, Section 17.7, "Versions make replacement safe": awkward negation.**
Sentence: "Tokens were found to *not* yet sign the version at first, so an old link silently served the new render; now the version is one of the signed fields (Table 17.3)."
Suggested: "At first, tokens did *not* sign the version, so an old link silently served the new render; now the version is one of the signed fields (Table 17.3)."

**Q6. Bold used for emphasis rather than for defined terms or fixed lead-ins (STYLE 9).**
Recurring pattern across the part: bolded "lesson" sentences at the end of incidents (for example Chapter 11, Section 11.9 "**a framework release can lag ...**"; Chapter 16, Section 16.6 "**the lesson generalizes: ...**"; Chapter 18, Section 18.8) and bolded numbers in a table (Chapter 17, Table 17.2: "**216**", "**218**"). Bold lead-ins such as "**The problem.**" and "**The lesson.**" are allowed by STYLE 14 (incident structure), but the bolded lesson sentences themselves are not covered. Suggested: decide as a book-wide rule; either allow bold for the lesson sentence in incidents (and add it to STYLE 9/14) or switch these to italics. Not changed because it is widespread and affects tone.

**Q7. Chapter 12, Section 12.1: "REST" and "resource" not bold at first use.**
"REST (representational state transfer) is a style ... Each thing the server holds is called a resource ..." The chapter bolds later terms (Jackson, serialization, path variable). Confirm whether REST and resource were bolded in Chapter 8; if not, bold them here.

**Q8. Chapter 13, front of chapter: the five-term list is formatted as a definition list with bold code.**
"**`Accept` header:**" puts inline code inside bold. STYLE 9/11 says code identifiers are never bold. Suggested: "**Accept header**" (no backticks) or "*`Accept` header*: ..." and likewise "**`Retry-After`:**". Left as is because it may be a deliberate exception for header names.

**Q9. Chapter 18, Section 18.1 heading: "Why we test, and the test pyramid".**
STYLE 2 restricts "we" to things the writer and reader do together. This use is a generic "we"; it is acceptable, but "Why to test" is awkward. Suggested (optional): "Why tests matter, and the test pyramid".

**Q10. Chapter 15, Section 15.5, paragraph after Figure 15.1: "Two things to notice."**
The paragraph lists two sentences but the second holds two ideas ("every failure ... same 401, while a success creates a session with a new id and a new CSRF token") plus a semicolon-joined pointer to the excerpts. Suggested: end the paragraph after the second thing and start a new sentence: "The next three excerpts follow the figure step by step."

## Items checked and left unchanged on purpose

- British spellings found only in code, code comments inside listings, class names or quoted source text ("serialised", "serialises", `isRecognised`) were kept per STYLE 3.
- "publishers and admins" inside the Chapter 17 Listing 17.8 code (exception message) is code and unchanged.
- The `--`, `-` and en dash uses in ranges ("Chapters 11 to 16", "1-64") follow the current text; no en dash is required by the text as written ("11 to 16" is a spelled range).
- Hyphens and dashes: em dashes appear only in captions and one listing comment inside quoted code; no prose em dashes were found.
- Cross-reference capitalization ("Chapter", "Section", "Listing", "Figure", "Table", "Exercise") is consistent throughout Part II. Every figure and table checked is referred to in the text before it appears.
- Heading capitalization is sentence case everywhere; tier headings and fixed headings match STYLE 6.
- Product names (Spring Boot, Spring Security, Hibernate, Flyway, MySQL, JSON, HTTP, PDF, HMAC, BCrypt, PDFBox, Testcontainers, JUnit, Mockito, H2) are spelled consistently.
- Chapter 13's "A note on versions" states its mixed tags (m3 and m6); Chapter 14 and 15 do the same. No inconsistency found.
- Not verified (outside proofreading scope): technical accuracy of listings against the tags, test counts, and the "six sweeps" statement in Table 14.2 (the text now says six and lists six).

## Top recurring problems

1. Word-list drift in prose: "admin(s)" where "administrator(s)" is required, "file name", "login/logout" in running prose, "start-up".
2. British spelling "recognised" (and its forms) in prose in Chapter 16 and its solutions.
3. Numbers of 10 or more spelled out ("twelve", "twenty", "nineteen") in Chapters 16 and 18.
4. Hype words ("easy") in solutions files.
5. Occasional tense or parallel-structure slips in incident narratives (Chapters 14, 16, 17, 18) that the queries above cover.
