# Book team charter

Every agent working on the book reads this first, and again at the start of every work window.

## The book

**Working title:** *Building a Secure Document Viewer: From First Line of Java to Production*

**Reader:** a complete beginner who has never programmed. The book teaches only what is
needed to build *this* app: Java 25, Spring Boot 4, SQL/MySQL, HTTP and the web, TypeScript,
Node/npm, Angular 22, testing (JUnit, Vitest, Playwright), Git, Docker. Topics this app doesn't
use are left out, or mentioned in one sentence at most. By the last chapter the reader has built
the app in this repository and understands every decision behind it.

**Standard:** industry publishing quality. Accurate, consistent, well structured, with numbered
figures and listings, exercises, cross-references, a glossary and an index-ready vocabulary.

## Structure (the lead editor refines this into `book/OUTLINE.md`)

| Part | Folder | Owner |
|---|---|---|
| Front matter (preface, how to use this book, setup) | `book/front-matter/` | editor |
| I. Foundations: programming with Java 25, Maven, Git, how the web works, SQL/MySQL, Docker | `book/part-1-foundations/` | writer-foundations |
| II. The backend: Spring Boot 4, REST, validation, JPA + Flyway, Spring Security, backend testing | `book/part-2-backend/` | writer-backend |
| III. The frontend: TypeScript, Node/npm, Angular 22, HttpClient, routing, frontend and e2e testing | `book/part-3-frontend/` | writer-frontend |
| IV. Building the Secure Document Viewer, milestone by milestone (tags `book-m0` to `book-m6`) | `book/part-4-building-the-app/` | writer-app |
| V. Production: security review, deployment, TLS, backups, metrics, supply chain | `book/part-5-production/` | writer-production |
| The Engineering Trade-offs (its own chapter) | `book/tradeoffs/` | writer-production |
| Style guide, outline, glossary, architecture blueprints (versioned), appendices | `book/` (STYLE, OUTLINE, GLOSSARY), `book/blueprints/`, `book/appendices/` | editor |
| Facts dossier (the single source of truth for history, decisions, bugs, versions) | `book/_team/dossier/` | research |
| Technical and pedagogical review notes | `book/_team/reviews/` | qa |

**Only write inside the folders you own.** To ask for a change in someone else's area, add a
note to `book/_team/requests.md` (append only: `- [from → to] request`).

## Chapter template (every feature chapter)

1. **Learning objectives** (3–6 bullets) and **prerequisites** (earlier chapters).
2. **Beginner tier:** the core concept through a simple analogy, then the foundational terms
   defined (each term also goes into the glossary), then the fundamental setup code explained
   line by line.
3. **Intermediate tier:** how the pieces talk to each other (for example frontend ↔ backend),
   and why this library or pattern was chosen over the obvious alternatives.
4. **Advanced tier:** security, performance and architecture decisions (for example token
   expiry, indexing, error-handling layers), with the real incidents from this project.
5. **In this project:** where the concept lives in the repo, naming files and the milestone tag.
6. **Try it:** exercises, graded ★ / ★★ / ★★★ (solutions go in the appendix, via the editor).
7. **Summary** and **Further reading** (official documentation only).

Part IV milestone chapters also end with:
- **Architecture blueprint vN:** the revised diagram (Mermaid) and what changed since vN-1;
- **Decisions & challenges:** the product decisions made at that milestone, and the bugs and
  reviewer findings, told as a story: the problem, how it was found, the fix, and the lesson.

## Accuracy rules (non-negotiable)

- **Never invent history.** Every decision, bug, number or version must come from the dossier
  or from a primary source (git history, PR descriptions, the code). If it isn't there, don't
  claim it.
- **Code listings are real.** Copy them from the repo at a named tag
  (`git show book-m2-documents:src/main/java/.../DocumentService.java`) and label them with the
  file and tag. If you shorten one, label it "simplified" and say what was left out. Never
  present invented code as the project's code.
- Versions used in this project: Java 25, Spring Boot 4.1.1 (Tomcat pinned to 11.0.26),
  Spring Security 7, Jackson 3, Hibernate 7, Flyway 11, MySQL 8.4, Angular 22, TypeScript 6.0,
  Node 24, Vitest 5, Playwright 1.63, PDFBox 3.0.8, Maven 3.9.16. Check anything else against
  `pom.xml` / `package.json` at the relevant tag.
- When the book simplifies for beginners, say so ("we simplify here; Chapter N gives the full
  picture").

## Sources

- **This repository:** `C:\dev\secure-doc-viewer`. Read files at milestone tags with
  `git show <tag>:<path>`, or export a tag into your own scratch folder with `git archive`.
  **Never** check out another branch or tag in this working tree: other agents are writing here.
- `git log`, `git show`, and PR descriptions: `gh pr view <n>` for PRs 1–12.
- **Memory notes:**
  `<local path>`.
- **The full build conversation** (very large; search it, never load it whole):
  `<local path>`.
- **Review material:** the scratchpad
  `<local path>`,
  for example `previous-review-findings.md`, `pr5-body-new.md`, `twoip/result.txt`.

## Off limits

- **Never open or quote** `reviewer-accounts.json`, `initial-admin-password.txt`,
  `e2e-admin-password.txt`, `.env`, `twoip/pw.env`, or anything else holding a password or
  secret. Never put a real password, secret, token or personal email address in the book.
- **No** git commits, pushes, branch or tag changes: the coordinator commits the `book/` folder.
- **No** Docker, database or running-stack changes, and no sign-ins to the app. Reading code and
  history is enough.
- Don't edit anything outside `book/`.

## Working in windows (important)

Work happens in timed windows, and you may be paused at any moment and resumed later with your
memory intact. So:

- Keep `book/_team/progress/<your-name>.md` up to date. After every section you finish, update
  **Done / In progress / Next / Open questions**. On resume, read it first.
- Write to disk incrementally, section by section. Never hold a whole chapter in your head.
- Prefer finishing a section over starting a new one.

## Voice

Warm, precise, encouraging; second person ("you"); American English; no emojis; no hype. Explain
*why* before *how*. Every analogy is followed by where the analogy breaks down. The lead editor's
`book/STYLE.md` takes precedence once it exists.
