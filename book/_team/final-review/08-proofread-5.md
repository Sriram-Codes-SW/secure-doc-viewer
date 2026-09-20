# Proofread 5: Part V (Chapters 32-36) and Chapter 37

Scope: `book/part-5-production/*.md` (introduction, Chapters 32-36, solutions) and `book/tradeoffs/37-*.md`. Read in full. Code blocks, inline code, Mermaid, listings, tables' technical content, quotations and HTML comments left untouched.

## Terminology decisions applied

- American spelling everywhere in own prose: recognised -> recognized, unrecognised -> unrecognized (about 30 places; the README's British form appears only in prose, never in a quotation).
- "front end" (noun/adjective) -> "frontend" (word list).
- "file names" -> "filenames"; "login sessions" -> "sign-in sessions"; "logout" -> "sign-out" (word list).
- Cross-reference words capitalized: "section 32.5" -> "Section 32.5", "sections" -> "Sections", "exercise 33.3" -> "Exercise 33.3".
- Serial comma added in titles and lists.
- Punctuation moved inside closing quotation marks (American style) where the quotation was a phrase, not code.
- Dates: comma after the year in mid-sentence ("September 20, 2026, the project's").

## Edits

Total: 74 edits (spelling 3 categories counted per file below).

### 00-part-introduction.md
1. Table V.1: "Backups, restores and operations" -> "Backups, restores, and operations" (serial comma, matches Chapter 34 title).
2. Table V.1: "Health, metrics and alerting" -> "Health, metrics, and alerting" (serial comma, matches Chapter 35 title).

### 32-security-review.md
1. 32.2 "matters in section 32.5" -> "Section 32.5".
2. 32.12 "incident in section 32.5" -> "Section 32.5".
3. 32.13 "questions from section 32.12" -> "Section 32.12".
4. 32.13 "(sections 17.5 and 25.6)" -> "(Sections 17.5 and 25.6)".
5. 32.13 "finding from section 32.5" -> "Section 32.5".
6. In this project table "reviewed in section 32.13" -> "Section 32.13".
7. Exercise 32.6 "questions from section 32.12" -> "Section 32.12".
8. Round 3 "recognised-device rule" -> "recognized-device rule".
9. Round 3 "unrecognised devices" -> "unrecognized devices".
10. Exercise 32.4 "recognised-device design's" -> "recognized-device design's".
11. 32.6 "optional HTTPS front end, Caddy" -> "frontend".
12. 32.13 "relying on the front end" -> "the frontend".
13. 32.13 quoted comments: `allowance", and ... cost". An` -> `allowance," and ... cost." An` (punctuation inside quotes).
14. 32.14 `"the page was replaced, reload", reloaded` -> `reload," reloaded`.
15. 32.11 "on September 19, 2026 and will revisit" -> "September 19, 2026, and will revisit".
16. 32.12 "file names" -> "filenames".

### 32-security-review.solutions.md
17. "recognised"/"unrecognised" -> American (3 places, Exercise 32.4).

### 33-deployment-and-tls.md
18. Prerequisites "(section 8.6" -> "(Section 8.6".
19-23. Section 33.3 list: "sections 33.8 and 33.12", "sections 33.5 and 33.9", "section 33.10", "section 33.11" -> capital S (four edits).
24. 33.9 "Chapter 37 (section 37.11)" -> "Section".
25. 33.15 "protection in section 33.6" -> "Section".
26. 33.4 "the web front end" -> "web frontend".
27. Table 33.1 "HTTPS front end with HSTS" -> "frontend".
28. 33.2 HSTS term: `"...from now on".` -> `"...from now on."` (period inside quotes).
29. 33.11 "recognised-device hashes" -> "recognized-device hashes".
30. Heading 33.13: "Non-root, memory limits, health checks and start order" -> "..., health checks, and start order".

### 34-backups-and-operations.md
31. Title: "Backups, restores and operations" -> "Backups, restores, and operations".
32. Advanced tier heading: "Retention, cleanup and living with one instance" -> "Retention, cleanup, and living with one instance".
33. recognised -> recognized (replace all: objectives, Table 34.1 twice, 34.6, Table 34.2, 34.10, In this project; 8 places).
34-40. "section 37.7", "section 34.7", "section 34.4", "section 32.11", "section 22.8", "sections 37.5, 37.7, and 37.11" -> capitalized (7 edits, one includes 34.4 text).
41. Quotation punctuation in 34.8: `persisted", and ... conservative". Here` -> `persisted," and ... conservative." Here`.
42. 34.11 sentence fragment: "Where `backup.sh` contains the four backup commands, ..." -> "The `backup.sh` script contains the four backup commands, ...".
43. Exercise 34.1 "the login sessions" -> "the sign-in sessions".

### 34-backups-and-operations.solutions.md
44. "**Login sessions:**" -> "**Sign-in sessions:**".
45. "recognised-device hashes" -> "recognized-device hashes".
46. "(section 37.7)" and "(section 37.9)" -> "Section" (one edit, two places).

### 35-metrics-and-monitoring.md
47. Title: "Health, metrics and alerting" -> "Health, metrics, and alerting".
48-52. "(section 16.6)" (twice), "(section 35.9)", "section 35.9", "section 35.6" -> "Section" (five places).
53. Pattern note: "Rates, errors and durations" -> "Rates, errors, and durations".
54. Bold used as emphasis: "an **illustrative** sample" -> "an *illustrative* sample" (STYLE: no bold for emphasis).

### 36-supply-chain-and-ci.md
55. "OSV scan in section 36.5" -> "Section 36.5" (number itself queried, see Q6).
56. "pinned (section 36.7)" -> "Section 36.7" (number itself queried, see Q6).
57. "On September 20, 2026 the project's" -> "September 20, 2026, the project's".
58. Figure 36.2 text description: "tests, scans and end-to-end run" -> "tests, scans, and end-to-end run".

### 37-engineering-tradeoffs.md
59. recognised/Recognised -> recognized/Recognized (heading 37.14, Table 37.1, text; about 12 places).
60. "By the end of this chapter you can:" -> "By the end of this chapter, you can:".
61. Prerequisites: "sessions and the defenses" -> "sessions, and the defenses".
62. Prerequisites: "monitoring and the supply chain" -> "monitoring, and the supply chain".
63. 37.1 `ingest". Only` -> `ingest." Only`.
64. 37.1 `flipbook readers use". Beyond` -> `use." Beyond`.
65. 37.3 `nobody chose", and` -> `nobody chose," and`.
66. 37.5 "rate limiting counters" -> "rate-limiting counters".
67. 37.11 broken sentence: "a managed database, The README names" -> "and a managed database. The README names".
68. 37.12 "the front end has to echo" -> "the frontend has to echo".
69. 37.13 "(exercise 33.3)" -> "(Exercise 33.3)".
70. 37.13 "(Chapter 40, section 40.5" -> "Section".
71. 37.17 Step 6 "(Chapter 40, section 40.5" -> "Section".
72. 37.17 intro "sections 37.1 to 37.16" -> "Sections".
73. Table 37.1 "Sessions vs tokens" -> "Sessions vs. tokens" (matches the headings).
74. Try it: added the missing blank line before Exercise 37.2, 37.3 and 37.4 headings (three edits; source hygiene, gfm handles it but a blank line is the standard).

### 37-engineering-tradeoffs.solutions.md
75. "so logout and unsharing" -> "so sign-out and unsharing".

Counts by type: capitalization of cross-reference words 33; spelling (British -> American) about 30 places in 8 edits; punctuation (quotation marks, serial comma, comma after year, hyphen) 19; terminology per word list 8; broken sentence or fragment 2; Markdown 4 (bold emphasis, blank lines before headings).

## Queries for the author (not changed)

Q1. **Ch 32, Section 32.10, item list.** The list "Five details in the listing deserve a second look" has bold lead-ins on items 1, 4 and 5 but not on items 2 and 3. Suggest bolding "CSRF" and "Session fixation protection" or none of them.

Q2. **Ch 32, Section 32.9.** "The trace code in the watermark has its own story. The admin handle uses Crockford Base32 (no I, L, O, or U) because ... the implementer misread an `I` as an `l` in a watermark." The first sentence is about the trace code, the second names the admin handle. Suggest "The trace code uses Crockford Base32 ..." if that is what the source says (unverified).

Q3. **Ch 32-36, first analogy paragraph ("The analogy breaks down ...").** STYLE 8 requires a paragraph beginning with the bold lead-in **Where the analogy breaks down:**. Chapters 32 (32.1), 33 (33.1), 34 (34.1), 35 (35.1) and 36 (36.1) use plain sentences ("The analogy breaks down in one place."). Suggest the bold lead-in in each.

Q4. **Ch 32-36, Try it ordering.** Exercises are not in ascending order: 32.4 is three stars and 32.5 is two; 33.3 is three stars, then 33.4 and 33.5 are two. STYLE does not require ascending order; confirm it is intended.

Q5. **Ch 35 and 36, heading level.** "## Common mistakes" is an unnumbered `##` between the Advanced tier and "In this project" in Chapters 35 and 36, while Chapters 32-34 have it as a numbered `###` (32.15, 33.15, 34.10) inside the Advanced tier. Suggest the same treatment everywhere (numbered `###`, for example 35.14 and 36.12). Also Ch 35 has "**To see the real page**" as a bold lead-in in running text (Section 35.6); STYLE reserves bold for defined terms and fixed lead-ins; suggest italics or plain text.

Q6. **Ch 36, wrong section numbers (cross-references).** Section 36.3 says "the OSV scan in Section 36.5", but OSV is in Section 36.6 ("OSV and Trivy: two different scans"; 36.5 is the backend and frontend jobs). Section 36.5 says actions "are pinned (Section 36.7)", but pinning is Section 36.8 (36.7 is throwaway secrets). Suggest 36.6 and 36.8.

Q7. **Ch 37, bare section numbers.** The chapter refers to its own sections as "(37.5)", "See 37.11", "Using 37.12", "(see 37.8)" without the word Section, about 30 times (and in the solutions file). STYLE 15 says "Section 37.5". Suggest either a global change to "Section 37.5" or a one-time note in "How to read this chapter" that "37.n" means Section 37.n.

Q8. **Ch 37, Section 37.1.** "Only disconnected tiles remain, and no endpoint returns a page or document". "Disconnected" is unclear for a beginner. Suggest "Only the tiles remain, and no endpoint ...".

Q9. **Ch 37, Section 37.6.** "OIDC" is used in "identity provider (OIDC or SSO)" before it is expanded (in "The enterprise alternative", a paragraph later), and SSO and MFA are defined twice (in Cons, then again in The enterprise alternative). Suggest defining each once at first use.

Q10. **Ch 37, Sections 37.8 and 37.9.** The reviewers are named "the AI product-owner review agent" and "the technical-manager review agent"; Chapter 32 uses "Product Owner reviewer" and "Senior Technical Manager review agent" (capitalized). Suggest "the Product Owner review agent" and "the Senior Technical Manager review agent" for consistency.

Q11. **Ch 37, Section 37.16.** "GA" ("the latest GA versions") is not expanded. Suggest "generally available (GA)" or a quoted-source note.

Q12. **Ch 37, Step 0 of Section 37.17.** After the inserted sentences about `TileWorkLimiter` and the render permits, "Each is correct on one instance and wrong on three" no longer clearly refers to the in-memory state listed earlier (the text says the per-instance caps are "correct"). Suggest moving the "Each ... three" sentence before "Two more per-instance limits exist by design" and ending the paragraph with the caps.

Q13. **Ch 37, Section 37.17 Step 4.** "On every instance they all run." reads awkwardly; suggest "They all run on every instance."

Q14. **Ch 37, learning objectives.** Objectives use the form "By the end of this chapter, you can:" with lower-case, semicolon-ended bullets; Chapters 32-36 use "you will be able to:" with capitalized bullets ending in periods. Chapter 37 is exempt from the tier template, but the objectives could match.

Q15. **Ch 34, Section 34.11.** "Choose the time when nobody reads:" is ambiguous. Suggest "Choose a time when nobody is reading:".

Q16. **Ch 33, Section 33.9.** "host name" (also Ch 33 solutions, Exercise 33.5). The word list has no entry; industry usage is "hostname". Suggest adding "hostname" to the word list and changing.

Q17. **Ch 32, Section 32.5 and Ch 37 various.** "the recognised-device rule from the README" and README's own spelling: the README uses British spelling ("recognised"); the book now uses "recognized". If the app's UI or logs use "recognised", make sure no visible label is quoted with the American form.

## Top recurring problems

1. Cross-reference words in lowercase ("section 32.5", "sections 37.1 to 37.16", "exercise 33.3"): 33 places.
2. British spelling of "recognised/unrecognised" in prose: about 30 places.
3. Missing serial comma in headings, titles, lists.
4. Closing quotation marks placed outside commas and periods (British style).
5. "front end" instead of "frontend"; a few "login"/"logout"/"file names" against the word list.
6. Structural: the "analogy breaks down" lead-in not bold; bare "37.n" references; unnumbered "Common mistakes" headings in Chapters 35 and 36.
