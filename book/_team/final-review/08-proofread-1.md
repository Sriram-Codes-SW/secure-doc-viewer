# Proofread 1: front matter and Part I

Scope: `book/front-matter/*.md` (4 files) and `book/part-1-foundations/*.md` (part introduction, Chapters 1-10, and the 10 `.solutions.md` files). Every word of prose was read. No code block, inline code, Mermaid diagram, listing, path, command, table technical content, quotation, or HTML comment was changed. Nothing was committed and nothing under `book/build/` was touched.

## Summary

- Edits made: 78 (spelling 3, capitalization 3, terminology 7, broken Markdown 1, serial comma 64).
- Queries for the author: 13.
- The prose is very clean: no doubled words, no comma splices worth fixing, no unclosed bold or italics, no malformed tables, no headings with trailing punctuation. The two systematic problems are the missing serial comma (STYLE 3) and structure items listed as queries (tier "you can skip" lines, analogy lead-in).

## Edits

### Spelling and capitalization

| File | Location | Before -> after |
|---|---|---|
| `front-matter/00-about-this-edition.md` | table row "Copyright and license" | licence -> license (noun, American) |
| `front-matter/00-about-this-edition.md` | "What this edition is" | "labelled as an example ... labelled illustrative" -> "labeled ... labeled" |
| `front-matter/00-about-this-edition.md` | "Accessibility" | "labelled in words" -> "labeled in words" |
| `front-matter/a-preface.md` | "Why a real project" | "(a git tag)" -> "(a Git tag)" |
| `front-matter/b-how-to-use-this-book.md` | Conventions | "the git tag it was copied from" -> "the Git tag" |
| `part-1-foundations/08-how-the-web-works.md` | 8.1 | "HyperText Transfer Protocol" -> "Hypertext Transfer Protocol" |

### Terminology (word list, STYLE 3.1)

| File | Location | Before -> after |
|---|---|---|
| `front-matter/a-preface.md` | "What you will build" | "an HTTPS front end" -> "an HTTPS frontend" |
| `front-matter/c-setting-up-your-machine.md` | Step 5 (Linux) | "log in again" -> "sign in to your computer again" |
| `02-command-line-and-files.md` | Table 2.3 | "HTTPS front end" -> "HTTPS frontend" |
| `02-command-line-and-files.md` | 2.8 | "start-up fails" -> "startup fails" (matches "startup" in 2.9) |
| `02-command-line-and-files.md` | Summary | "git-ignored" -> "Git-ignored" |
| `03-first-java-program.md` | 3.2 | "The file name must match" -> "The filename must match" |
| `03-first-java-program.md` | 3.4 | "upper-case copy" -> "uppercase copy" |
| `05-collections-and-exceptions.md` | 5.4 | "here to upper case" -> "here to uppercase" |
| `06-maven-and-project-layout.md` | 6.6 | "PDFBox file name" -> "PDFBox filename" |
| `08-how-the-web-works.md` | 8.9 | "TLS front end" -> "TLS frontend" |
| `10-docker-and-compose.md` | 10.6 | "HTTPS front end" -> "HTTPS frontend" |
| `10-docker-and-compose.md` | 10.7 | "git-ignored file" -> "Git-ignored file" |
| `10-docker-and-compose.md` | 10.10 | "HTTPS front end" -> "HTTPS frontend" |
| `10-docker-and-compose.md` | In this project | "TLS front end", "HTTPS front end" -> "frontend" |
| `10-docker-and-compose.md` | Summary | "git-ignored" -> "Git-ignored" |

### Broken Markdown

| File | Location | Fix |
|---|---|---|
| `03-first-java-program.md` | 3.2, after the "openjdk version" output | Removed a stray empty ```` ```text ```` fence (two lines) between two paragraphs |

### Serial (Oxford) comma added (STYLE 3: "Java, Maven, and Git")

- `front-matter/a-preface.md`: "Docker, and Git"; "SQL, and Docker" (Part I bullet); "metrics, and supply-chain hygiene" (Part V bullet).
- `front-matter/c-setting-up-your-machine.md`: "Docker, and a code editor".
- `00-part-introduction.md`: "Docker, and a code editor"; "readers, publishers, and administrators".
- Ch 1 (8): "(about an hour, applying the idea), and"; "`reader.one`, `pub.one`, and `outsider.one`"; "sessions, and the audit log"; "a watermark, and checks"; "accounts, documents, and permissions"; "the tiles, and the database"; "SQL, and containers"; heading 1.8 "signed addresses, watermarks, and checks".
- Ch 2 (3): objective "move, and delete"; heading 2.3 "copying, and deleting files"; Summary "`ls`, and `cd`".
- Ch 3 (3): intro "compile, and run"; objective "compile, and run"; objective "`switch`, and loops".
- Ch 4 (4): intro "loops, and methods"; intro "packages, and annotations"; objective "getters, and a method"; Exercise 4.3 "ready, or failed".
- Ch 5 (3): objective "a set, and a map"; objective "catch, and define"; 5.4 "`anyMatch`, and `toList`".
- Ch 6 (4): intro "runs the tests, and packages"; objective "package, and verify"; Figure 6.2 text description "package, and verify"; Exercise 6.1 "`artifactId`, and version"; 6.12 "Java 21 to 25, and PDFBox".
- Ch 7 (5): objective "remotes, and pull requests"; 7.1 "`report-final-2`, and"; 7.3 "staging area (...), and the repository"; Figure 7.1 text description; Summary "staging area, and repository".
- Ch 8 (3): 8.9 "integrity (...), and authentication"; Summary "`same-site`, and `secure`"; Summary "integrity, and authentication".
- Ch 9 (6): objectives "columns, and keys" and "`UPDATE`, and `DELETE` ... `ORDER BY`, and `LIMIT`"; 9.1 "`V1`, `V2`, and `V3`"; 9.11 "(Chapter 7), and tested"; Summary "`UPDATE`, and `DELETE`"; Exercise 9.5 "`document_share`, and `app_user`".
- Ch 10 (8): intro "volumes, and Docker Compose"; objectives "a volume, and a network" and "networks, and health checks"; 10.6 "(the frontend server), and `tls`"; 10.10 "a web server, and an optional"; In this project "web server, and TLS frontend" and "`storage/`, and `target/`"; Summary "localhost only, and scanning".

## Terminology decisions applied

- sign in (verb) / sign-in (noun, adjective): no "log in" or "login" outside code; the one operating-system "log in" (Linux `docker` group) became "sign in to your computer".
- frontend and backend as one word (including "HTTPS frontend", "TLS frontend"); no "front end", "back end".
- filename (not "file name"); file path kept.
- startup (noun) in prose; "set up" is a verb and "setup" a noun ("a working setup"): already consistent.
- Git (proper noun) in prose, including compounds ("Git tag", "Git-ignored"); lowercase only in commands and code comments.
- American spelling: license, labeled, uppercase; British spellings left only inside listings and quotations ("recognised", "serialised", "denormalised" in SQL and Java comments).
- Serial comma in every list I saw, including in headings and figure descriptions.
- Product names checked: Spring Boot, Spring Security, JavaScript, TypeScript, GitHub, MySQL, Docker Compose, Node.js (first use) then Node, npm, Maven, PDFBox, Flyway: consistent throughout.
- Cross-reference capitalization (Chapter, Section, Listing, Figure, Table, Exercise, Appendix, Part): consistent throughout the section.

## Queries for the author (not changed)

**Q1. Serial comma, remaining instances.** I fixed the ones I saw (64), but three-item lists without the comma are pervasive in Part I prose (roughly 10-25 per chapter is likely; for example Ch 1 "Windows, macOS and Linux" patterns in 3.1, Ch 4 Table 4.1, Ch 8 "scheme, host and port", Ch 9 "quickly, safely and for many users"). Suggest a mechanical final pass, or a decision that STYLE 3 yields to the actual usage.

**Q2. Chapter titles without the serial comma.** Ch 4 "Classes, objects, records and interfaces" and Ch 5 "Collections, generics, lambdas and exceptions" (also in `OUTLINE.md` and the metadata). Not changed because titles are cross-referenced; suggested: "Classes, objects, records, and interfaces" and "Collections, generics, lambdas, and exceptions" everywhere at once.

**Q3. Tier "you can skip" lines missing (STYLE 7).** None of Ch 1-10 has the italic line under `## Intermediate tier` or `## Advanced tier` (Ch 4 and Ch 5 have a longer pacing note instead, which serves the purpose). Suggested, for example under Ch 2 Advanced: `*On a first read you can skip to "In this project"; Part IV comes back to this.*`

**Q4. Analogy lead-in (STYLE 8.3).** The analogy paragraphs start with a bold "**Analogy.**" and contain "The analogy breaks down..." inside the same paragraph (Ch 1 has a separate sentence, Ch 2, 3, 4, 6, 7, 8, 9, 10 the same). STYLE requires a separate paragraph beginning with the bold lead-in **Where the analogy breaks down:**. Ch 5 has no analogy for collections (the dictionary remark in 5.1 is the nearest). Suggested rewrite pattern: split the paragraph after "...in the kitchen" and begin the next with **Where the analogy breaks down:**.

**Q5. Ch 1, intro paragraph 2, exercise levels.** "★ (a few minutes...), ★★ (about an hour...)" differs from STYLE 12 and "How to use this book" (5 to 15 minutes; 30 to 60 minutes). Suggested: "★ (5 to 15 minutes, checking your understanding), ★★ (30 to 60 minutes, applying the idea), and ★★★ (open-ended)".

**Q6. Where YAML is defined.** Ch 2, Section 2.6 says "**YAML** is a settings format where indentation shows nesting (Chapter 8)", and Ch 10, Section 10.4 says "the indentation-based format from Chapter 8". Chapter 8 shows a YAML listing (8.3) but does not define YAML; the definition is in Chapter 2. Suggested: Ch 2 drop "(Chapter 8)"; Ch 10 "the indentation-based format from Chapter 2".

**Q7. Ch 3, Exercise 3.5.** "at most 200 characters (the size of the `title` column, which is 200 characters; Chapter 9 explains columns)" repeats itself. Suggested: "at most 200 characters (the size of the `title` column; Chapter 9 explains columns)".

**Q8. Ch 4, learning objective 7.** "Read a `package` line, an `import` line and a source-folder layout, and the four access levels." Verb does not fit the last object. Suggested: "Read a `package` line, an `import` line, and a source-folder layout, and name the four access levels."

**Q9. Ch 9, Section 9.3, numbered list broken by unindented code blocks.** Steps 2 and 3 are followed by code blocks that are not indented under the list item, so some renderers restart the numbering (the "3." item may render as "1."). Suggested: indent the three code blocks and their following paragraphs by three spaces so they stay inside the list items.

**Q10. Text description lines.** In Part I the label is italic and the sentence is plain (`*Text description:* Six boxes ...`), while STYLE 13.1 shows the whole line in italics. Consistent within the section; decide once for the book (probably fine as is, since the build reads the caption line).

**Q11. Preface, organization list.** "**The Engineering Trade-offs**" is capitalized as a title, while Chapter 37 is "Engineering trade-offs" (sentence case). Suggested: "**Engineering trade-offs (Chapter 37)**" or lowercase.

**Q12. Ch 4, Section 4.3 (PageInfo note).** The text says pull request number 13 was "merged into `main` after the tag" and corrected the `PageInfo.java` comment. The listing (correctly) still shows the tag's comment. Prose is fine; only flagging that a reader who clones `main` sees a different comment than Listing 4.3, which the paragraph already explains. No action needed unless the author wants a "Note" callout.

**Q13. Ch 2, Section 2.10 and Listing 2.3.** The chapter itself points out that the class comment ("about two seconds") understates the code (about four seconds). This is the known "FileOperations two seconds" open item; the chapter text is now correct and self-explaining. Chapters 17 and 27 should be checked for the same figure (outside my section).

## Recurring problems (ranked)

1. Missing serial comma in three-item lists (most frequent; 64 fixed, more remain).
2. Structural template items: no italic "you can skip" lines; analogy "breaks down" text not in a separate bold lead-in paragraph.
3. Word-list drift: "front end" (5 places), "git" lowercase in prose (4), "file name" (2), "start-up".
4. British spelling in the front matter (licence, labelled) while the body is American.
5. One stray empty code fence (Ch 3) and one numbered list interrupted by code (Ch 9).
