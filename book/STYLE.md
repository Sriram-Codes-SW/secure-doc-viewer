# House style guide

*Building a Secure Document Viewer: From First Line of Java to Production*

This guide is binding for every file under `book/`. Where it and the charter disagree on a
matter of style, this guide wins; on accuracy, the charter's rules always win. If something
isn't covered here, follow *The Chicago Manual of Style* (18th edition) for prose and the
*Microsoft Writing Style Guide* for technical usage, then ask the editor through
`book/_team/requests.md` so the answer can be added here.

Owner: `editor`. Last updated: 2026-09-19.

---

## 1. Who we are writing for

The reader has **never programmed**. They are intelligent, motivated and patient, but they
don't know what a variable, a terminal, a port or a database is. Every chapter is written for
that person, and every chapter is also accurate enough that a professional engineer reading it
would find nothing to correct.

Three consequences:

1. **Nothing is used before it is taught.** If a chapter needs a concept from later in the
   book, either teach the minimum right there (and say that a later chapter goes deeper) or
   move the material. `book/OUTLINE.md` lists what each chapter may assume.
2. **Only what this app needs.** A topic the app doesn't use is left out, or gets one sentence
   at most ("Java also has a feature called X; this project doesn't need it").
3. **Honest simplification.** When you simplify, say so and point to where the full picture is:
   "We simplify here; Chapter 21 gives the full picture."

## 2. Voice and tone

- **Warm, precise, encouraging.** Write as a patient senior colleague sitting next to the
  reader. Encourage through clarity, not through cheerleading.
- **Second person.** Address the reader as "you". Use "we" only for things you and the reader
  do together in the text ("Let's run the tests again"). Never use "we" for the project's
  history: the reader didn't make those decisions. Write "the project chose MySQL", "the
  reviewer found", "the team fixed", or name the role (product owner, reviewer).
- **Why before how.** Every new tool, pattern or line of configuration is motivated before it
  is shown: what problem does it solve, and what would go wrong without it?
- **No hype.** Don't write "powerful", "blazing fast", "magic", "seamless", "robust", "simply",
  "just", "easy", "obviously", "of course" or "trivial". What is obvious to you is the reader's
  hardest page.
- **No emojis**, no exclamation marks in running text (one in a genuinely surprising result is
  tolerable), no rhetorical-question strings.
- **Present tense, active voice.** "The server signs the URL", not "The URL is signed by the
  server". Past tense is for the project's history ("In milestone 2 the reviewer found...").
- **Contractions are welcome** (it's, don't, you'll). They keep the tone human.
- **Short paragraphs.** One idea per paragraph, rarely more than five sentences. Prefer
  sentences under 25 words; vary rhythm, but break any sentence that needs a second reading.
- **Be honest about limits.** This project is explicit about what its protections do *not*
  achieve (see the README's "Limitations"). The book keeps that honesty.

## 3. American English and mechanics

- **American spelling:** behavior, color, license (noun and verb), recognize, serialize,
  canceled, modeling, gray, catalog. When quoting project text that uses British spelling
  (the repository has "behaviour", "recognised", "neutralisation"), keep the quotation exact
  and don't comment on it; your own prose stays American.
- **Serial (Oxford) comma:** "Java, Maven, and Git".
- **Numbers:** spell out zero through nine in prose; use numerals for 10 and above, and always
  for measurements, versions, ports, limits and anything with a unit: 5 failures, 512 px,
  120 seconds, port 8080, Java 25. Use a thin non-breaking space in prose only if your editor
  makes it easy; a normal space is fine.
- **Dates:** "September 19, 2026" in prose; ISO `2026-09-19` in tables, logs and code.
- **Quotation marks:** straight quotes in Markdown source (`"` and `'`); punctuation goes inside
  quotation marks except when the quoted text is code or literal input.
- **Dashes:** in prose, prefer a comma, colon or parentheses. The em dash (—, no spaces) is
  reserved for captions ("Listing 4.2 — ...") and the occasional strong break. Use an en dash
  for ranges: 3–6 bullets, Chapters 12–14.
- **Headings** use sentence case: "Signing a tile URL", not "Signing A Tile URL".
- **Lists:** introduce a list with a full sentence ending in a colon. Items are either all
  sentences (with periods) or all fragments (no final periods; semicolons optional in short
  inline lists). Use numbered lists only for sequences.

### 3.1 Word list

Use the left column. The same word must always name the same thing; this also keeps the
vocabulary index-ready.

| Use | Not | Notes |
|---|---|---|
| sign in (verb), sign-in (noun, adjective) | log in, login, logon | Code keeps its own names: `/api/auth/login`, `LoginThrottle`. |
| sign out, sign-out | log out, logout | |
| administrator (prose), admin (UI and code) | | The role is `ADMIN` in code font. |
| backend, frontend | back-end, front end | |
| browser | web browser (after first use) | |
| command line, terminal | shell (until Chapter 2 defines it), console | |
| database | DB | `DB_PASSWORD` stays as-is in code. |
| email | e-mail | |
| filename, file path | file name | |
| GitHub, Git, npm, Node.js (first use), Node | github, NPM, NodeJS | |
| HTTP, HTTPS, URL, JSON, PDF, CSV, TLS, HMAC | Http, url, Json | Plurals: URLs, PDFs (no apostrophe). |
| internet, web | Internet, Web | |
| Java 25, JDK, JVM | java, jdk | |
| JavaScript, TypeScript | Javascript, Typescript, JS/TS | |
| MySQL, Spring Boot, Spring Security, Angular | mysql, SpringBoot, AngularJS | AngularJS is a different, obsolete product. |
| open source (noun), open-source (adjective) | | |
| sign in throttling, lockout | brute force protection | "Throttle" is the project's word. |
| tile, tile URL, signed URL, token | chunk, piece, link | Keep to the project's vocabulary. |
| watermark | stamp (except as a verb) | |
| web server, app server | webserver | |

## 4. Files and folders

- **Chapters:** `NN-slug.md`, where `NN` is the two-digit chapter number from
  `book/OUTLINE.md` (numbering runs continuously through the whole book, not per part) and
  `slug` is lowercase, ASCII, kebab-case, at most five words. Example:
  `book/part-2-backend/14-jpa-and-flyway.md`.
- **Part openers:** `00-part-introduction.md` in each part folder (one to two pages: what the
  part covers, why it's in this order, what you'll have built by the end).
- **Exercise solutions:** `NN-slug.solutions.md`, next to the chapter, in the writer's own folder.
  The editor compiles these into Appendix C (see section 12).
- **Front matter:** `book/front-matter/a-preface.md`, `b-how-to-use-this-book.md`,
  `c-setting-up-your-machine.md`.
- **Appendices:** `book/appendices/appendix-a-glossary.md`, `appendix-b-blueprint-history.md`,
  and so on.
- **Blueprints:** `book/blueprints/vN-slug.md` (v0 to v6).
- Never rename a published chapter file; ask the editor, who updates the outline and links.

Every chapter file starts with a metadata comment, then the title:

```markdown
<!-- chapter: 14 | part: II | owner: writer-backend | tag: book-m2-documents | status: draft -->
# Chapter 14: Storing data with JPA and Flyway
```

`status` is one of `outline`, `draft`, `in-review`, `revised`, `final`. `tag` is the milestone
tag whose code the chapter quotes most (use `book-m6-final` for Parts I–III unless the outline
says otherwise).

## 5. Heading levels

| Level | Use | Numbered? | Example |
|---|---|---|---|
| `#` | Chapter title, once per file | Yes, "Chapter N:" | `# Chapter 14: Storing data with JPA and Flyway` |
| `##` | The template's fixed parts (section 6) | No | `## Beginner tier: Tables as spreadsheets` |
| `###` | Numbered sections inside a tier | Yes, N.M | `### 14.3 Your first entity` |
| `####` | Subsections | No | `#### Why not plain JDBC?` |

- Section numbers run continuously through the chapter (14.1, 14.2, ... across all tiers), so
  "Section 14.5" is unambiguous.
- Don't go deeper than `####`. If you need a fifth level, the section is too big: split it.
- Never put two headings in a row with nothing between them; write at least one sentence.
- Don't put code, links or bold in headings (a class name in backticks is allowed when the
  section is about that class).

## 6. The chapter template, spelled out

Every feature chapter follows this skeleton in this order. Headings in `##` are fixed text,
except the tier titles, which you complete after the colon.

```markdown
<!-- chapter: 14 | part: II | owner: writer-backend | tag: book-m2-documents | status: draft -->
# Chapter 14: Storing data with JPA and Flyway

One or two sentences: what this chapter does and why it matters for the app.

## Learning objectives

By the end of this chapter, you will be able to:

- Explain ...            (3–6 bullets, each starting with a measurable verb:
- Write ...               explain, write, run, compare, configure, test, diagnose)

## Prerequisites

- Chapter 9: SQL and MySQL (tables, rows, primary keys)
- Chapter 12: REST controllers and JSON

## Beginner tier: <the core idea in plain words>

### 14.1 <The analogy>
### 14.2 <Terms you need>
### 14.3 <The fundamental setup code, line by line>

## Intermediate tier: <how the pieces talk to each other>

### 14.4 ...
### 14.5 Why this and not <the obvious alternative>

## Advanced tier: <security, performance and architecture>

### 14.6 ...
### 14.7 <A real incident from this project>

## In this project

## Try it

## Summary

## Further reading
```

What goes in each part:

1. **Opening paragraph** (no heading): what the reader will build or understand and where it
   fits in the Secure Document Viewer. Two to four sentences.
2. **Learning objectives:** 3–6 bullets with measurable verbs. Avoid "understand" and "learn";
   say what the reader will be able to *do*.
3. **Prerequisites:** earlier chapters only, each with a few words on what it provides. If the
   reader needs something installed, point to the setup chapter section.
4. **Beginner tier:**
   - the core concept through one analogy (section 8), with where it breaks down;
   - the foundational terms, each defined in bold at first use (section 9);
   - the fundamental setup code explained line by line (section 11.3).
   A reader who stops at the end of the beginner tier should be able to follow Part IV at a
   basic level.
5. **Intermediate tier:** how the pieces talk to each other (frontend and backend, service and
   repository, browser and server), with a sequence diagram where there is a conversation
   between parts. At least one "Why this and not X?" section that names the obvious
   alternative, states its real advantages, and explains why this project chose otherwise.
6. **Advanced tier:** security, performance and architecture decisions, told with the real
   incidents from this project (section 14). Every incident cites its source.
7. **In this project:** a short table or list naming the exact files (full paths from the
   repository root, in code font), the milestone tag where each first appears, and one line
   on what each does. End with the command to see it: `git show book-m2-documents:<path>`.
8. **Try it:** exercises (section 12).
9. **Summary:** 4–8 bullets restating the key ideas in new words, not copies of the
   objectives. Then one sentence pointing to the next chapter.
10. **Further reading:** official documentation only (section 15).

**Part IV milestone chapters** use the same template and add two sections between
"Advanced tier" and "In this project":

```markdown
## Architecture blueprint vN

## Decisions and challenges
```

- **Architecture blueprint vN:** embed the diagram from `book/blueprints/vN-*.md` (copy the
  Mermaid block exactly; don't redraw it) as a numbered figure, then "What changed since
  vN-1" as a short list. Blueprint v0 in Chapter for `book-m0-mvp` has "What's here" instead.
- **Decisions and challenges:** the product decisions made at the milestone, then the bugs and
  reviewer findings told as stories (section 14).

**Chapters that are not feature chapters** (the preface, part openers, the trade-offs chapter,
setup) don't use the tier structure. The trade-offs chapter has its own template in
`book/OUTLINE.md`.

**Length:** aim for 4,000–8,000 words per chapter. Part IV chapters may run to 10,000. If a
chapter passes 10,000, propose a split to the editor.

## 7. Tiers

Tiers let one book serve a reader on their first pass and on their second. They are
signposted, never hidden:

- Each tier is an `##` heading beginning with its name: `## Beginner tier: ...`,
  `## Intermediate tier: ...`, `## Advanced tier: ...`. Nothing else goes before the colon.
- Right under each intermediate and advanced tier heading, one italic line tells the
  first-time reader what they can do:

  ```markdown
  ## Advanced tier: Keeping tile URLs from being replayed

  *On a first read you can skip to "In this project"; Part IV comes back to this.*
  ```

- A later tier may assume everything in the earlier tiers of the same chapter, and everything
  in any tier of earlier chapters. A beginner tier may assume only the beginner tiers of
  earlier chapters.
- Don't label individual paragraphs or callouts with tiers.
- **Part IV and Part V chapters use the tier headings too** (decision of 2026-09-19). The three
  `##` tier headings are required in every feature chapter, including milestone chapters
  (Chapters 25–31) and production chapters (Chapters 32–36). The tier headings are unnumbered;
  the numbered N.M sections sit beneath them, numbered continuously through the chapter. In a
  milestone chapter the order is: Learning objectives, Prerequisites, Beginner tier,
  Intermediate tier, Advanced tier, Architecture blueprint vN, Decisions and challenges, In this
  project, Try it, Summary, Further reading. Put the milestone's requirements and core new code
  in the beginner tier, how the parts talk and why this design in the intermediate tier, and
  security, performance and incidents in the advanced tier. Only the trade-offs chapter (37),
  part openers and front matter are exempt.
- **Prerequisite chapter numbers** must match `book/OUTLINE.md` exactly. For orientation: the
  web and HTTP are Chapter 8, SQL is 9, Docker is 10, Spring Boot is 11, REST is 12, JPA and
  Flyway is 14, Spring Security is 15 and 16, backend testing is 18, TypeScript is 19, Angular
  is 21 to 23, and frontend testing is 24.

## 8. Analogies

Analogies are how a complete beginner gets a foothold. Rules:

1. **One analogy per concept**, from everyday life (a library, a coat check, a post office, a
   concert wristband). Don't mix two analogies in one explanation.
2. **Map it explicitly.** Say which real thing corresponds to which part of the analogy; for
   more than three pairs, use a small table.
3. **Always say where it breaks down.** Immediately after the analogy, write a paragraph that
   begins with the bold lead-in **Where the analogy breaks down:** and names at least one way
   the real system behaves differently. This is not optional: an analogy without its limits
   teaches something false.
4. **Reuse deliberately.** If an earlier chapter's analogy fits, extend it and name the
   chapter, rather than inventing a new one. The editor keeps a list of established analogies
   in section 8.1.
5. Never use an analogy in place of the precise definition; use it before the definition.

Example:

> A signed tile URL works like a wristband at a concert. The box office (the server) checks
> your ticket once, then gives you a wristband printed with today's date and a hologram that
> only the box office can make. Security at each door (each tile request) doesn't phone the
> box office; it checks the hologram and the date.
>
> **Where the analogy breaks down:** a wristband works for anyone wearing it, but a tile URL
> is also bound to your session: pasted into another browser, it's refused. And a wristband
> lasts all night, while a tile URL expires after two minutes.

### 8.1 Established analogies (keep consistent)

| Concept | Analogy | Introduced in |
|---|---|---|
| Signed tile URL | Concert wristband with a hologram and a date | Chapter 25 |
| Session cookie | Coat-check ticket | Chapter 8 |
| Database table | Spreadsheet with strict columns | Chapter 9 |
| Type checking | Doctor's-office form with typed fields | Chapter 19 |

Writers: propose additions through `requests.md`. Chapter numbers follow `book/OUTLINE.md`.

## 9. Defining terms

- **Bold at first use, in the whole book.** The first time a term appears, put it in bold and
  define it in the same sentence or the next: "A **port** is a numbered door on a computer
  that a program listens at." After that, the term is plain text everywhere.
- Before bolding, check `book/GLOSSARY.md`. If the term is already defined in an earlier
  chapter, don't redefine it; if the reader might have forgotten, remind them in a clause with
  a cross-reference: "the session cookie (Section 6.4)".
- **Every bolded term goes into the glossary.** Add a request to `book/_team/requests.md`:

  ```markdown
  - [writer-backend → editor] glossary: **entity** — a Java class whose objects are stored as rows in a database table. (Ch 14, Section 14.3)
  ```

- Bold is used for **defined terms** and for the fixed lead-ins in this guide (**Note:**,
  **Where the analogy breaks down:** and the like). Don't use bold for emphasis; use italics,
  sparingly.
- Code identifiers (class names, methods, config keys) are never bold; they go in code font.
- Acronyms: spell out at first use with the acronym in parentheses, then use the acronym:
  "Cross-Site Request Forgery (CSRF)". Exceptions that never need expanding after Chapter 8:
  HTTP, URL, PDF, JSON.

## 10. Callouts

Four callout types, all as block quotes with a bold lead-in. Use them sparingly: no more than
one per screen of text, and never two in a row.

```markdown
> **Note:** Extra information that is useful but not essential to the main thread.

> **Tip:** A practical shortcut or habit that saves time or trouble.

> **Warning:** Something that can lose data, break security or waste hours. Say what goes
> wrong and how to avoid it.

> **In this project:** Where the concept you just read about lives in the repository, with the
> file path and tag, e.g. `src/main/java/com/example/securedocviewer/service/SignedUrlService.java`
> (from `book-m0-mvp`).
```

- **Note** is the default. If you are unsure whether something is a Warning, it's a Note.
- **Warning** is for real consequences (lost data, a security hole, a broken build), not mild
  inconvenience.
- **In this project** (the callout) is a short in-line pointer while teaching; the
  `## In this project` section at the end of the chapter is the full list. Don't confuse them.
- A callout is one to four sentences. If it's longer, it belongs in the text.

## 11. Code

### 11.1 Two kinds of code, never mixed up

| Kind | Caption | Rule |
|---|---|---|
| **Listing**: code from this repository | `Listing N.M — \`File.java\` (tag)` | Copied exactly from `git show <tag>:<path>`. Never edited except as labeled. |
| **Example**: code written to teach | `Example N.M — Short description` | Must not look like it's the project's code; never carries a tag. |

This distinction is how the book keeps the charter's accuracy rule. If a reader can't tell
whether code is the project's, it's wrong.

### 11.2 Captions and labels

Captions go **above** the code block, in bold, as their own paragraph. Numbering is per
chapter and per kind: Listing 14.1, 14.2, ...; Example 14.1, 14.2, ....

```markdown
**Listing 14.2 — `DocumentService.java` (book-m2-documents)**

​```java
@Service
public class DocumentService {
    ...
}
​```

*Path: `src/main/java/com/example/securedocviewer/document/DocumentService.java`*
```

- Use the plain filename in the caption and the full path in the italic line under the block.
- **Shortened listings** say so in the caption and say what was removed:
  `**Listing 14.3 — \`DocumentService.java\` (book-m2-documents, simplified: imports and two
  helper methods omitted)**`. Mark each cut in the code with a comment on its own line in the
  file's comment syntax: `// ...` (Java, TypeScript), `-- ...` (SQL), `# ...` (YAML, shell).
- **Excerpts** of a few contiguous lines use "excerpt" and give the method or area:
  `(book-m6-final, excerpt: method verify)`.
- **Annotated listings:** to explain lines, you may add markers `// [1]`, `// [2]` at the end of
  lines and follow the block with a numbered list. Add "annotated" to the caption. Markers are
  the only change allowed without "simplified".
- **Configuration and SQL** are listings too when they come from the repository:
  `Listing 14.5 — \`V2__documents_shares_audit.sql\` (book-m2-documents)`.
- **Never** include a real secret. If a file contains one (it shouldn't), don't quote it. Use
  placeholders in angle brackets: `<your-signing-secret>`, `<db-password>`.

### 11.3 Explaining code line by line

In the beginner tier, the fundamental setup code is explained line by line. Either annotate
(above) or walk through it in prose, quoting the fragment in code font before explaining it.
Explain every token a beginner hasn't seen before; after Part I, you may group routine lines
("the import lines bring in the classes we use; Chapter 3 explained imports").

### 11.4 Code formatting

- Always put a language on the fence: `java`, `typescript`, `html`, `css`, `sql`, `yaml`,
  `json`, `xml`, `bash`, `powershell`, `text`, `mermaid`, `properties`, `dockerfile`.
- Keep lines at or under 90 characters in Examples. Listings keep the original lines; if a
  line is very long, it's fine to shorten the listing and say so.
- **Commands** go in their own block, without a prompt character, so they can be copied:

  ```bash
  ./mvnw spring-boot:run
  ```

  Show output in a separate `text` block introduced by "You should see something like:".
  Trim output with `...` on its own line.
- **Platform differences:** show `bash` (macOS, Linux, and Git Bash on Windows) first. When the
  Windows PowerShell command differs, add it in a second block labeled
  "On Windows (PowerShell):". The setup chapter tells Windows readers to install Git Bash, so
  one `bash` block often serves all three.
- **Inline code** (backticks) for: filenames and paths, class, method, variable and field
  names, commands, config keys, environment variables, HTTP header names, URLs and endpoint
  paths, literal values, and status codes when discussed as values (`404`). Write the status
  meaning in prose the first time: "`404 Not Found`".
- **UI labels** are in italics with their exact capitalization: click *Upload*, open the
  *Admin* page. Keyboard keys use `<kbd>`: <kbd>Ctrl</kbd>+<kbd>C</kbd>.

### 11.5 Versions at older milestone tags

The book teaches the project's final stack (Java 25, Spring Boot 4.1.1, Spring Security 7,
Jackson 3, Hibernate 7, Flyway 11, Angular 22, TypeScript 6.0, Node 24). But tags
`book-m0-mvp` to `book-m4-reading` were built on **Spring Boot 3.3.4 and Java 21**; the
upgrade happens at `book-m5-platform`.

- Parts I–III quote `book-m6-final` by default.
- A Part IV chapter quoting `book-m0` to `book-m4` says once, near its start, which versions the
  tag used, and notes any listing that would differ on the final stack (a renamed starter, a
  changed package), with a cross-reference to the milestone 5 chapter.
- Check any other version against `pom.xml` or `frontend/package.json` *at that tag*.

## 12. Exercises ("Try it")

Every feature chapter has 3–6 exercises, at least one at each of the first two levels.

| Level | Meaning | Typical size |
|---|---|---|
| ★ | Check understanding or make a small, guided change. | 5–15 minutes |
| ★★ | Apply the idea to a new case in the project, with some independence. | 30–60 minutes |
| ★★★ | Extend, investigate or design; may be open-ended. | An hour or more |

Format:

```markdown
## Try it

### Exercise 14.1 ★ Find the owner column

Open `V2__documents_shares_audit.sql` at `book-m2-documents` and find the column that records
who owns a document. What type is it, and why can't it be empty?

*Hint:* look for `NOT NULL`.

*Solution:* Appendix C, Exercise 14.1.
```

- Exercises are numbered N.M per chapter, independently of listings.
- Use the black star `★` (U+2605) only; no other symbols.
- Say which tag to work from if it matters. Exercises that change code tell the reader to work
  on their own branch (`git switch -c exercise-14-2`), never on a tag directly.
- Exercises never require real credentials, sign-ins to someone else's deployment, or anything
  the charter puts off limits.
- **Solutions:** write them in `NN-slug.solutions.md` next to your chapter, same numbering and
  headings (`### Exercise 14.1 ★ Find the owner column`). ★ and ★★ get full solutions; ★★★ get
  a worked outline and the key decisions ("one good answer is..."). Solutions that show code
  follow the Listing/Example rules. The editor compiles them into Appendix C and may copyedit
  them; don't edit Appendix C directly.

## 13. Figures and tables

### 13.1 Figures

All diagrams are **Mermaid**, in a `mermaid` fence, so they live in text and render on GitHub.

```markdown
​```mermaid
sequenceDiagram
    participant B as Browser
    participant S as Server
    B->>S: GET /api/documents
    S-->>B: 200 OK (JSON list)
​```

*Figure 14.1 — The browser asks the server for the document list.*
*Text description: the browser sends one GET request; the server answers with a JSON list.*
```

- The caption goes **below** the figure, in italics: `*Figure N.M — Sentence-case caption.*`
- Add a one-sentence **text description** on the next line for readers who can't see the
  diagram (screen readers, print). Required.
- Pick the right diagram: `flowchart` for structure and decisions, `sequenceDiagram` for a
  conversation over time (the most useful kind in this book), `erDiagram` for database schema,
  `stateDiagram-v2` for lifecycles. Use `classDiagram` only when the class relationships are
  the point.
- At most about 15 nodes. A bigger diagram is two diagrams.
- Labels are plain words (quote any label with punctuation: `A["POST /api/auth/login"]`). Use
  the same names as the code for components (`SignedUrlService`) and plain words for people
  and places (Browser, Server, Database).
- No custom colors or themes; renderers differ and print is grayscale. The only styling allowed
  is the blueprint convention in `book/blueprints/README.md`.
- Every figure is referred to in the text before it appears ("Figure 14.1 shows...").

### 13.2 Tables

- Caption **above** the table, in bold: `**Table 14.1 — Roles and what they can do**`.
- Every table has a header row. Keep to five columns or fewer; left-align text.
- Refer to every table in the text.
- Small reference tables inside callouts or the "In this project" section don't need numbers.
  Anything you'd cross-reference does.

## 14. Telling the project's story (incidents and decisions)

The advanced tiers and Part IV's "Decisions and challenges" use real events. Rules:

- **Source everything.** Every incident, decision, number, date or version comes from the
  dossier (`book/_team/dossier/`) or a primary source (git history, a PR description, the
  code). Put the source in an HTML comment at the end of the paragraph:
  `<!-- source: PR #5; commit 782ab6b; dossier/incidents.md#render-pool -->`. If you can't
  source it, don't write it.
- **Incident structure**, as `####` subsections or bold lead-ins:
  **The problem**, **How it was found**, **The fix**, **The lesson**. The lesson generalizes
  beyond this app.
- **Decision structure:** **The decision**, **The options considered**, **Why this one**,
  **What it costs**. Name who decided when the sources say so (for example, "the product
  owner").
- Refer to people by role (the product owner, the reviewer, the threat-modeling review), never
  by personal name or email address. Review-finding IDs from the sources (such as `TM2-5` or
  `PO2-7`) may be quoted in code font, with a plain-words explanation.
- No blame, no drama. Bugs are normal; the story is how they were found and what was learned.
- **The reviewers were AI agents.** The Product Owner (PO) and Technical Manager (TM) reviews
  were carried out by Claude agents, briefed as independent third parties. Never present them
  as human reviewers, and never invent credentials, job histories or personalities for them.
  Say once, early in the chapter, that these reviewers were AI review agents; after that "the
  TM review" and "the PO review" are fine. Implementation and code review also involved an AI
  coding agent working with the project owner; say so plainly where it matters. Decisions
  attributed to "the product owner" are the project owner's decisions.
- **Cite the dossier, not the scratchpad.** History claims cite files in `book/_team/dossier/`
  (`timeline.md`, `decisions.md`, `bugs-and-findings.md`, `reviews.md`, `versions.md`), a commit
  hash or a PR number. Never cite the coordinator's scratchpad or temporary folders, agent
  identifiers, or the raw conversation file, in the book text or in source comments. If the
  dossier lacks a fact, ask `research` through `requests.md`.
  Source comments (HTML comments) may name dossier files; the visible text never mentions "the
  dossier", agent identifiers, review-finding IDs without a plain-words explanation, or any
  internal file. Reviewers are described as AI agents.

## 15. Cross-references and links

- Refer by number and type, capitalized: Chapter 14, Section 14.3, Listing 14.2, Figure 14.1,
  Table 14.1, Exercise 14.1, Appendix C, Part II. Never "above", "below", "the next listing"
  (except within the same paragraph) or page numbers.
- Link chapter references with relative Markdown links the first time in a section:
  `[Chapter 14](../part-2-backend/14-jpa-and-flyway.md)`. Use the chapter file named in
  `book/OUTLINE.md`, even if it doesn't exist yet.
- Cross-reference forward sparingly and backward freely. A forward reference must never be
  needed to understand the current text.
- Refer to milestone code by tag in code font: `book-m2-documents`.
- **Further reading** lists official documentation only (the project's own docs: Oracle/OpenJDK,
  spring.io, angular.dev, dev.mysql.com, docs.docker.com, git-scm.com, typescriptlang.org,
  developer.mozilla.org for web standards, owasp.org for security guidance, IETF RFCs). Format:

  ```markdown
  - *Spring Data JPA Reference Documentation*, "Defining Query Methods." https://docs.spring.io/...
  ```

  Link to the documentation for the version this book uses where one exists. No blogs, videos,
  Stack Overflow or vendor marketing.

## 16. Examples, names and security hygiene

- Example usernames: `pub.one`, `reader.one`, `outsider.one` (these follow the project's README
  and tests). Example domains: `example.com`, `docs.example.com`. Example email:
  `admin@example.com`.
- Passwords and secrets are always placeholders in angle brackets. Never quote or paraphrase
  the contents of any file the charter puts off limits, and never write a real password,
  secret, token, session id or personal email address, even an expired one.
- Tokens in examples are visibly fake and truncated: `token=eyJ...` style is fine, but prefer
  `token=<signed-token>`.
- When you show an attack (for example tampering with a token to see a `401`), show it against
  the reader's own local copy of the app.

## 17. Before you mark a chapter "draft"

Checklist:

- [ ] Metadata comment and `# Chapter N:` title match `book/OUTLINE.md`.
- [ ] Objectives, prerequisites, three tiers, In this project, Try it, Summary, Further reading
      (plus blueprint and Decisions and challenges in Part IV).
- [ ] Nothing used before it's taught (check the outline's "assumes" lists).
- [ ] Every analogy has "Where the analogy breaks down".
- [ ] Every bolded term is new and has a glossary request.
- [ ] Every listing is copied from a named tag; simplified ones say so; Examples carry no tag.
- [ ] Every figure, table and listing is numbered, captioned and referred to in the text.
- [ ] Every historical claim has a `<!-- source: ... -->` comment.
- [ ] Exercises graded ★/★★/★★★; solutions file written.
- [ ] American spelling; word list followed; no emojis; no secrets.
- [ ] Progress file updated.
