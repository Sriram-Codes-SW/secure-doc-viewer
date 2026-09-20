<!-- chapter: 0 | part: IV | owner: writer-app | tag: book-m6-final | status: expanded -->
# Part IV: Building the Secure Document Viewer

Parts I to III gave you the tools: Java, the web, SQL, Spring Boot, TypeScript and Angular. Part IV
puts them to work on one product, milestone by milestone. It is the heart of the book, because it is
the only part where you watch the same application change: a small, insecure prototype becomes a
system that has survived several rounds of independent review.

Each chapter matches a Git **tag**, a named snapshot of the repository. You can read the code exactly as
it was when a chapter's story ended, run it, and compare it with the tag before. Nothing in this part
is invented for teaching. Every listing is copied from the repository at its tag, and every incident
and decision comes from the project's own record.

## What you will do in this part

By the end of Part IV you will have read, chapter by chapter, the reasons behind every major
decision in the application. You will be able to:

- explain the tiled-viewer design, and what it does and doesn't prevent;
- follow a request through authentication, authorization, throttling and audit;
- read the migrations and see how the schema grew;
- explain why each defense was added, and what it cost;
- recognize the pattern of a review finding, a fix and a test that pins the fix.

The chapters are written for someone who has finished Parts I to III, but each chapter re-teaches the
one or two ideas it needs, briefly, and points back to the chapter that teaches them fully.

## The story in one page

The project began on September 17, 2026 as a single commit: a backend that serves PDFs as watermarked
tiles through signed URLs (Chapter 25). The next day it gained an Angular frontend, and then
two independent reviews, followed by a plan to fix everything they found, phase by phase. The phases
were merged as pull requests, the last and largest of them on September 19, and the milestones in the
table are the snapshots of that history.
<!-- source: timeline (dates, PRs, commit counts) -->

**Table IV.1 — The milestones**

| Chapter | Tag | Story in one line |
|---|---|---|
| 25 | `book-m0-mvp` | Tiles, signed URLs and per-viewer watermarks |
| 26 | `book-m1-accounts` | Real accounts, roles, sessions and CSRF protection |
| 27 | `book-m2-documents` | Ownership, sharing, MySQL persistence and an audit trail |
| 28 | `book-m3-hardening` | Upload limits, one error format, security headers, health |
| 29 | `book-m4-reading` | Deep links, keyboard, idle warning, a traceable watermark |
| 30 | `book-m5-platform` | Spring Boot 4, Docker, CI and the review rounds |
| 31 | `book-m6-final` | A flaky test fixed and a dependency policy |

Table IV.1 is your map. In total the seven tags cover 38 commits and pull requests numbered 1 to 12, and
a thirteenth pull request, described below, came after the last tag. Pull
requests 1 to 5 delivered milestones 1 to 5. Pull requests 6 to 8 were automatic dependency updates
that the project declined, and pull requests 9 to 12 are the small changes of milestone 6. A thirteenth
pull request merged later, after the last tag, and is not part of any milestone: it corrected the
README and a code comment that still described the reader as a canvas, so the code at the tags and the
code on the main branch differ in those two places.
<!-- source: timeline; PR #1-#12 -->

**Table IV.2 — How the tests grew**

| Tag | Backend tests | Frontend tests | Other |
|---|---|---|---|
| `book-m0-mvp` | five unit-test classes | none (one static page) | |
| `book-m1-accounts` | 45 | 4 | 13 security integration tests among the 45 |
| `book-m2-documents` | 56 | 6 | |
| `book-m3-hardening` | 66 | 6 | |
| `book-m4-reading` | 68 | 14 | |
| `book-m5-platform` | 114 | 31 | end-to-end tests with accessibility checks |
| `book-m6-final` | same as m5 | same as m5 | one flaky test fixed |

Table IV.2 uses the counts that the pull request descriptions report. (The m0 row counts test
classes, because the project reports no test total at that point. The m6 row means that no test was
added; one was made reliable.)
<!-- source: PR #1-#5 test plans; timeline -->

## Who made the decisions

The project was built by a person, the **product owner**, working with an AI coding assistant, the
**implementer**. The product owner set goals, approved plans and made the product decisions: which
sign-in approach, which database, whether to adopt a newer framework, whether to accept a trade-off.
The implementer wrote code, proposed options, ran tests and fixed what reviews found.

Between milestones, two independent reviewers examined the product as outsiders. Both were **AI review
agents**: one played a product owner, one played an experienced senior technical manager, and each was
told to review like a third party with no knowledge of the implementation. They filed numbered
findings. The book refers to them by those identifiers in code font, for example `TM-1` (a
technical-manager finding) or `PO-3` (a product-owner finding), and it explains each in plain words the
first time. Later review rounds use identifiers such as `TM2-1` or `PO2-5` for findings from the second
round of review, and `TM3-1` for the third.

Two things follow from that. First, the reviewers' findings are not gospel: each was checked, and
several led to a discussion or a different fix than the one suggested. Second, the reviewers were
themselves tools with limits, and the chapters say when a review missed something or when a fix
created a new problem. A good review process is not a source of truth; it is a source of leads, each
of which you verify.
<!-- source: decisions D15 and reviews record -->

## The shape of every chapter

Every chapter in this part has the same parts, in this order.

1. **Learning objectives** and **prerequisites**, so you know what the chapter assumes.
2. **The beginner tier.** The core concept through a plain-language explanation and an analogy (every
   analogy is followed by where it breaks down), the terms defined, and the fundamental code explained
   line by line.
3. **The intermediate tier.** How the pieces talk to each other, and why this design was chosen over
   the obvious alternatives.
4. **The advanced tier.** The security, performance and architecture decisions, with the real
   incidents from this project.
5. **Common mistakes.** Real symptoms and their fixes.
6. **In this project.** A table naming the files, so you can find each idea in the repository.
7. **Try it.** Exercises graded from one star to three stars, with solutions in a separate file next
   to the chapter.
8. **The architecture blueprint** for the milestone: a diagram of the whole system at that tag, and
   what changed since the previous one.
9. **Decisions and challenges.** The product decisions and the bugs and review findings of the
   milestone, told as stories.
10. **Summary** and **further reading**, which lists only official documentation.

Decisions are told with the same four parts each time: the decision, the options considered, why this
one, and what it costs. Incidents are told as the problem, how it was found, the fix and the lesson.
The lesson is always something you can carry to another project.

**Reading the listings.** A listing has a caption, then the code, then a line that starts with `Path:`
naming the file in the repository. The caption names the tag. When a listing is shortened, the caption
says "simplified" and says what was removed; a line with only three dots marks removed code. If a
listing came from more than one place in the file, it says so.

## Following along with the code

You can do this part with only a book, but it is much better with the code beside you. If you have
cloned the repository, you can look at any file at any tag without changing your working copy:

```bash
git show book-m2-documents:src/main/java/com/example/securedocviewer/document/Visibility.java
```

To see what a milestone changed, compare it with the one before:

```bash
git diff --stat book-m1-accounts book-m2-documents
```

The `--stat` option summarizes the changes as a list of files and line counts, which is a good first
look before reading any diff in full. To run the app at a tag, check the tag out into a **detached
HEAD** (a state where you are looking at a snapshot and not on a branch):

```bash
git switch --detach book-m2-documents
```

Do your experiments there, and return to your own branch with `git switch -` when you finish. Chapter
7 covers branches and checkouts. Use throwaway values for every secret, and never put a real password
in a file you might commit.

**How to run an older milestone.** The setup differs between tags, because the project's tooling grew
along with the code. Table IV.3 lists what each group of tags needs. The setup steps are the same ideas
you met in Chapters 6, 10 and 19; only the requirements change.

**Table IV.3 — What you need to run each tag**

| Tags | Backend | Database | Frontend | Secrets file |
|---|---|---|---|---|
| `book-m0-mvp` | JDK 21 and an installed Maven (`mvn spring-boot:run`); no Maven wrapper | None: sessions and documents live in memory | None: one static page at `http://localhost:8080` | None: a demo signing secret is in `application.yml` |
| `book-m1-accounts` to `book-m4-reading` | JDK 21 and an installed Maven; no Maven wrapper | MySQL 8.4 from `docker compose up -d` (Docker required) | Node and `npm ci` in `frontend/`, then the Angular dev server, which proxies `/api` to the backend | A `.env` file copied from `.env.example`: database passwords and a signing secret of at least 32 characters |
| `book-m5-platform` and `book-m6-final` | JDK 25, and the Maven wrapper (`./mvnw`) that these tags include | `docker compose up -d`, or the whole stack with `docker compose --profile full up -d --build` | Built and served by the Docker stack, or the dev server as before | The same `.env` file (see `.env.example`) |

A few practical notes follow from the table.

- **Before milestone 5 there is no Maven wrapper.** The wrapper (`mvnw`, `mvnw.cmd`, `.mvn/`) arrived
  with pull request #5. At the earlier tags, install Maven yourself and use `mvn` instead of `./mvnw`.
- **Match the Java version to the tag.** Milestones 0 to 4 are built for Java 21 and milestones 5 and 6
  for Java 25. Running an older tag on a newer JDK may work, but the book's statements are only
  guaranteed for the versions in `pom.xml` at that tag.
- **The `.env` file is never committed.** Copy `.env.example` to `.env` and fill in throwaway values. The
  app refuses to start if the signing secret is missing or shorter than 32 characters (from
  milestone 1 on). On an empty database it creates an `admin` account, using the bootstrap password from
  `.env` or, if that is empty, one that it prints once in the startup log.
- **Tests.** Run the backend tests with `mvn test` (or `./mvnw verify` at milestone 5 and later; those
  need Docker running, because some tests start a real MySQL). For the frontend tests and the end-to-end
  run, use the commands in Chapter 24.
- **Storage.** Tiles are written under `storage/` (or the folder named by `STORAGE_ROOT`). Keep it out of
  any folder that a cloud-sync program watches (Chapter 27 tells why).
- **Start from a clean database** when you switch between tags, because each tag's migrations expect the
  database to look the way that tag left it. Removing the Compose volume (`docker compose down -v`)
  gives you an empty one.
<!-- source: git ls-tree and diffs between book-m4-reading and book-m5-platform (mvnw, .mvn, Dockerfile new in m5); pom.xml java.version at m0 and m5; README at m0 and m5; .env.example and ViewerProperties at m1 -->


**A caveat about the tags.** Pull requests 1 to 4 were *stacked*: each was started from the previous
one, and all four were merged into `main` within a minute of one another. So `book-m1-accounts` to
`book-m4-reading` are cumulative snapshots of `main` at those merges. This is why each chapter's
listings come from the tag but its diff from the previous tag can contain more than the pull request's
own commits: read each chapter's "what changed" note rather than assuming.
<!-- source: timeline (tag map and stacking caveat) -->

## Versions

Milestones 0 to 4 use Spring Boot 3.3.4, Java 21 and PDFBox 3.0.3. Milestones 5 and 6 use Spring Boot
4.1.1, Java 25, PDFBox 3.0.8 and a pinned Tomcat 11.0.26. The frontend uses Angular 22 from milestone 1
on. Always confirm versions in `pom.xml` and `package.json` at the tag you are reading; the frontend's
test runner, for instance, is Vitest 4 up to milestone 5 and Vitest 5 only at milestone 6.
<!-- source: coordinator correction in requests.md; versions record -->

## An honest word about what this app is

The application in this part is a **deterrent**, not a vault. Anything a person can see on their
screen, they can photograph. What the design achieves is narrower and worth having: the PDF file is
never handed out; every tile is individually authorized, throttled and watermarked with the viewer's
identity and a code that ties it to one sign-in; and access can be revoked at once. Chapter 25 states
the honest limits at the start, Chapter 32 collects them into a threat model, and Chapter 37 weighs
the trade-offs the project made. Read Part IV with that in mind: the value of the story is not that
the app became unbreakable, but that each weakness was found, named, fixed and pinned by a test.

Now, to the first milestone.
