<!-- chapter: 0 | part: IV | owner: writer-app | tag: book-m6-final | status: draft -->
# Part IV: Building the Secure Document Viewer

Parts I to III gave you the tools: Java, the web, SQL, Spring Boot, TypeScript and Angular. Part IV
puts them to work on one product, milestone by milestone. Each chapter matches a Git tag, so you can
check out the code exactly as it was when the chapter's story ended.

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

Table IV.1 is your map. Every chapter has the same shape: the product goal and requirements, then
three tiers (beginner, intermediate, advanced), then where the code lives, exercises, the
architecture blueprint for that milestone, and the decisions and challenges of the period.

## How the history was gathered

The history in this part comes from the repository (its commits, tags and pull requests) and from the
recorded conversation in which the app was built. Two independent
reviewers examined the product between milestones. They were AI review agents playing a product
owner and a senior technical manager, and the book refers to their findings by ID (for example
`TM-1` or `PO-3`) in code font, with a plain explanation each time.

One caveat matters for reading the tags. Pull requests 1 to 4 were stacked on one another and merged
within a minute, so `book-m1-accounts` to `book-m4-reading` are cumulative snapshots of `main` at those
merges. Each chapter describes what its milestone added.
<!-- source: dossier/timeline.md (tag map and stacking caveat); dossier/milestone-briefs.md -->

## Versions

Milestones 0 to 4 use Spring Boot 3.3.4, Java 21 and PDFBox 3.0.3. Milestones 5 and 6 use Spring Boot
4.1.1, Java 25 and PDFBox 3.0.8. Always confirm versions in `pom.xml` and `package.json` at the tag
you are reading.
<!-- source: coordinator correction in requests.md; dossier/versions.md -->
