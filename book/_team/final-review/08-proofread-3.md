# Proofread 3: Part III (Chapters 19-24, solutions) and Part IV opener

Scope: `book/part-3-frontend/*.md` (part introduction, Chapters 19-24, six solutions files) and `book/part-4-building-the-app/00-part-introduction.md`. Every file was read in full. Code blocks, Mermaid diagrams, inline code, listings, HTML comments and quoted text were left untouched.

## Edits made

| # | File | Location | Before -> After | Type |
|---|---|---|---|---|
| 1 | part-3/00-part-introduction.md | "A few ideas" list | "content-security policy" -> "content security policy" | hyphenation |
| 2 | part-3/19-typescript.md | 19.7, after Listing 19.7 | "a cancelled fetch" -> "a canceled fetch" | American spelling |
| 3 | part-3/19-typescript.md | 19.12, after Listing 19.12 | "subscriptions are cancelled" -> "are canceled" | American spelling |
| 4 | part-3/19-typescript.md | 19.5 step 6 | "found by two-step lookup" -> "found by a two-step lookup" | grammar (missing article) |
| 5 | part-3/19-typescript.md | "In this project" table (7 rows) | bare `book-mN-...` tags -> code font | tag formatting (STYLE 15) |
| 6 | part-3/19-typescript.md | line after "In this project" table | "exist at book-m1-accounts" -> "exist at `book-m1-accounts`" | tag formatting |
| 7 | part-3/20-node-npm-angular-cli.md | "In this project" table (6 rows) | bare tags -> code font | tag formatting |
| 8 | part-3/20-node-npm-angular-cli.solutions.md | Exercise 20.5 | "may pick ... and writes a new lock file" -> "and write" | grammar (parallel verbs) |
| 9 | part-3/21-angular-components.md | "In this project" table (4 rows) | bare tags -> code font | tag formatting |
| 10 | part-3/22-http-client-and-services.md | 22.6 | "screen assistants announce it" -> "screen readers announce it" | terminology (matches Ch 21) |
| 11 | part-3/22-http-client-and-services.md | 22.7, 404 bullet | 'an "no longer available to you" state' -> 'a "no longer ..." state' | grammar (a/an) |
| 12 | part-3/22-http-client-and-services.md | Common mistakes, last bullet | "(Chapter 19, Section 19.12)" -> "(Chapter 19, Section 19.9)" | wrong cross-reference (19.9 is the "types are a promise" section; 19.12 is the generation counter) |
| 13 | part-3/22-http-client-and-services.md | heading 22.15 | "The Content-Security Policy" -> "The Content Security Policy" | capitalization/term |
| 14 | part-3/22-http-client-and-services.md | 22.15 first sentence | "A Content-Security-Policy (CSP) is a header" -> "A Content Security Policy (CSP) is a header" | term (mechanism vs header name) |
| 15 | part-3/22-http-client-and-services.md | 22.11 | "content-security policy" -> "content security policy" | hyphenation |
| 16 | part-3/22-http-client-and-services.md | 22.12 | "sets a Content-Security-Policy for the app's pages" -> "sets a Content Security Policy ..." | term |
| 17 | part-3/22-http-client-and-services.md | "In this project" table (7 rows) | bare tags -> code font | tag formatting |
| 18 | part-3/23-routing-guards-forms.md | 23.6 | "travelled" -> "traveled" | American spelling |
| 19 | part-3/23-routing-guards-forms.md | 23.3 | "(Section 23.10)" -> "(Section 23.11)" | wrong cross-reference (23.10 is template-driven vs reactive forms; number conversion of `?page=` is 23.11) |
| 20 | part-3/23-routing-guards-forms.md | "In this project" table (5 rows) | bare tags -> code font | tag formatting |
| 21 | part-3/24-testing-the-frontend.md | 24.10 Step 2 | "(Chapter 23, section 23.15)" -> "Section 23.15" | capitalization of "Section" |
| 22 | part-3/24-testing-the-frontend.md | 24.10 story item 5 | "Every user name" -> "Every username" | terminology |
| 23 | part-3/24-testing-the-frontend.md | "In this project" table (7 rows) | bare tags -> code font | tag formatting |

Counts by type: American spelling 3; grammar 3; cross-reference corrections 2; capitalization/terminology 7; hyphenation 2 (included in the 7 term edits above where noted); tag formatting (code font in six "In this project" tables plus one sentence) 7 edit operations touching about 36 table rows. Total distinct edit operations: 23.

Part IV opener (`part-4-building-the-app/00-part-introduction.md`): no typographical errors found; no edits.

## Queries for the author (not changed)

Q1. Ch 19, Section 19.5 (after Table 19.2): "Only the interior ones are full-size." With a 2-column by 3-row grid there are no interior tiles; only (0,0) and (1,0) are full-size. Suggested: "Only the tiles that are not in the last row or last column are full-size."

Q2. Ch 19, Table 19.2: the table writes "1000 − 512" and "1300 − 1024" without thousands separators, while the prose above writes "1,000" and "1,300". Suggest "1,000" and "1,300" in the table (technical content unchanged).

Q3. Ch 19, Section 19.7: "Chapter 22 explains why a small pool." is a fragment. Suggested: "Chapter 22 explains why the pool is small."

Q4. Ch 19, Section 19.7: "Where a function can only pass a value on later, callers can convert." is hard to parse for a beginner. Suggested: "A function that hands out its result through an Observable can be converted for callers that need a Promise. `session.service.ts` uses `firstValueFrom(...)` ..."

Q5. Ch 19, Section 19.11: the bullet list has no introducing sentence, and a listing plus a further bullet (`as const`) interrupts it. Suggested: add "Four more tools appear in the project:" before the list (there are five bullets) and move the `as const` bullet to its own paragraph before Listing 19.11.

Q6. Ch 22, Section 22.1: "`list()` returns `Observable<DocumentSummary[]>` (Chapter 19): a promise of a list of summaries." Chapter 19 (19.7) says an Observable is a stream, not a promise. Suggested: "a stand-in for a list of summaries that will arrive later."

Q7. Ch 22 Summary: "the sign-in and me endpoints are exempt" is unclear. Suggested: "the sign-in and who-am-I (`/api/auth/me`) endpoints are exempt."

Q8. Ch 23, Section 23.8: "We checked this against Angular 22.1.7's own URL parser" uses "we" for something the author did; STYLE 2 reserves "we" for reader-plus-author actions. Suggested: "The author checked this ..." (as Ch 19 and 20 phrase similar experiments).

Q9. Ch 23, Table 23.2, row `viewer/abc`: the Why cell "Not absolute; refused to be safe" reads oddly. Suggested: "Not an absolute path; refused to be safe".

Q10. Ch 24, Section 24.1 (paragraph after Figure 24.1): "The frontend jobs on the left are fast ...; the end-to-end job on the right is slow" refers to layout, but Figure 24.1 is drawn top to bottom, and the frontend job is singular. Suggested: "The frontend job is fast and isolated; the end-to-end job is slow and realistic."

Q11. Ch 24, Section 24.10, list "five steps", item 5: the sentence about `page.fill(...)`/`page.click(...)` and the `afterAll` clean-up is folded into item 5 of the numbered list. Suggested: end item 5 after "hasn't been shared with you", and put the helper and clean-up sentences in a paragraph after the list.

Q12. Ch 24: "e2e" (for example "the e2e suite", "the second e2e test", `e2e/` folder) is used in prose alongside "end-to-end", and "e2e" is never introduced. Suggested: write "end-to-end" in prose and keep `e2e` only for the folder and script names, or define it once in Section 24.1.

Q13. Chapters 19, 20, 21, 22, 23, 24 "Try it": the exercises are not in ascending star order (for example 19.5 is three stars followed by 19.6 at two stars; 20.5/20.6, 21.5/21.6, 22.5/22.6, 23.5/23.6, 24.5/24.6 all end with a two-star exercise after a three-star one). STYLE 12 does not require ordering, but a reader may expect it. Renumbering would need the solutions files and Appendix C updated, so no change was made.

Q14. Part IV opener, "The shape of every chapter": the list order (Common mistakes; In this project; Try it; Architecture blueprint; Decisions and challenges; Summary) differs from the STYLE 6/7 milestone order (Architecture blueprint and Decisions and challenges come before In this project). "Common mistakes" is not in the STYLE template either. Please check the list against the actual order in Chapters 25-31.

Q15. Part IV opener, "The story in one page": "pull requests numbered 1 to 12, and a thirteenth pull request, described below, came after the last tag" then repeats the thirteenth-PR explanation in the next paragraph. Suggested: delete the first mention ("and a thirteenth pull request, described below, came after the last tag") to avoid saying it twice and to avoid "below" (STYLE 15).

Q16. Part III opener and Ch 19 (Section 19.1): "A **browser** runs" and "An **Angular** component" bold terms that were already defined earlier in the book (Chapter 8) or are product names; STYLE 9 says bold only at first definition. Check against GLOSSARY.md.

## Terminology decisions applied

- sign in (verb) / sign-in (noun, adjective); "log in" appears only in code names and endpoints (`login`, `/api/auth/login`); no prose violations found in this section.
- frontend, backend, email, database, filename: no violations found (the word "setup" is used as a noun; "set up" as a verb is not used in this section).
- username (one word) for the prose noun; changed one "user name".
- screen reader (not "screen assistant").
- American spelling: canceled, traveled. British spellings inside code comments (`colours` in a CSS comment in Listing 21.9) are left as quoted project text.
- Content Security Policy for the mechanism; `Content-Security-Policy` (hyphens) only for the HTTP header name.
- Tags (`book-mN-...`) in code font in prose and "In this project" tables; caption parentheses follow the STYLE example (plain).
- Cross-reference form "Section N.M", "Chapter N", "Listing N.M" checked throughout; one lowercase "section" corrected.
