# Building a Secure Document Viewer

*From first line of Java to production*: a beginner's guide that ends with the app in this
repository.

> **Status: draft in progress** on the `book/draft` branch. See `book/OUTLINE.md` for the
> table of contents. Team working notes live in `book/_team/`.

## How the book is organized

| Part | Contents |
|---|---|
| Front matter | Preface, how to use this book, setting up your machine |
| I. Foundations | Programming with Java 25, Maven, Git, how the web works, SQL and MySQL, Docker |
| II. The backend | Spring Boot 4, REST APIs, validation, JPA and Flyway, Spring Security, testing |
| III. The frontend | TypeScript, Node and npm, Angular 22, talking to the backend, testing |
| IV. Building the Secure Document Viewer | The app built milestone by milestone, with the architecture blueprint revised at each step, plus the decisions and challenges along the way |
| V. Production | Security review, deployment, TLS, backups, metrics, supply chain |
| The Engineering Trade-offs | Every major decision: the choice, its pros and cons, and the enterprise alternative |
| Appendices | Glossary, blueprint history, exercise solutions |

## Following along with the code

Each milestone in Part IV matches a git tag in this repository:

| Tag | Milestone |
|---|---|
| `book-m0-mvp` | Tiled viewer with signed URLs and watermarks |
| `book-m1-accounts` | Accounts, roles, sessions, CSRF, sign-in throttling |
| `book-m2-documents` | Ownership, sharing, MySQL and Flyway, audit trail |
| `book-m3-hardening` | Upload and API hardening, secrets, operations |
| `book-m4-reading` | Reading experience, deep links, idle warning, watermark |
| `book-m5-platform` | Spring Boot 4, Docker, CI, production hardening |
| `book-m6-final` | The finished app |

```
git checkout book-m2-documents   # the code as it was at milestone 2
```
