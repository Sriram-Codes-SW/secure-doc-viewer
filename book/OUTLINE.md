# Outline: table of contents

*Building a Secure Document Viewer: From First Line of Java to Production*

Owner: `editor`. Status: v1 (2026-09-19). Chapter numbers and file names are **fixed**: writers
use them for cross-references. To change the outline, ask the editor via `book/_team/requests.md`.

Conventions: see `book/STYLE.md`. File name is `NN-slug.md` in the owner folder. "Tag" is the
milestone whose code the chapter quotes most. "Assumes" lists the only earlier chapters the reader
is expected to know; never use anything outside that list without teaching it.

**Version note.** Tags `book-m0-mvp` to `book-m4-reading` were built on Spring Boot 3.3.4 and
Java 21; `book-m5-platform` and `book-m6-final` use Spring Boot 4.1.1 and Java 25. Parts I to III
quote `book-m6-final`. See STYLE.md section 11.5.

## At a glance

| Part | Chapters | Folder | Owner |
|---|---|---|---|
| Front matter | Preface, How to use this book, Setup | `book/front-matter/` | editor |
| I. Foundations | 1–10 | `book/part-1-foundations/` | writer-foundations |
| II. The backend | 11–18 | `book/part-2-backend/` | writer-backend |
| III. The frontend | 19–24 | `book/part-3-frontend/` | writer-frontend |
| IV. Building the app | 25–31 | `book/part-4-building-the-app/` | writer-app |
| V. Production | 32–36 | `book/part-5-production/` | writer-production |
| The Engineering Trade-offs | 37 | `book/tradeoffs/` | writer-production |
| Back matter | Epilogue, Appendices A–E | `book/appendices/` | editor |

## Ordering logic

1. Part I gives the reader a machine that works, a language, and the vocabulary of the web and of
   data, before any framework appears.
2. Part II builds the server-side skills in the order the app needs them: serve requests, store
   data, protect it, test it.
3. Part III does the same for the browser side, and can only start once the reader knows HTTP and
   JSON (Chapter 8) and has a running backend (Part II).
4. Part IV then assembles the real app, one milestone per chapter. Each Part IV chapter re-uses
   ideas taught earlier and points back to them rather than teaching them again.
5. Part V takes the finished app to a real host, then Chapter 37 steps back and weighs every major
   decision.

---

## Front matter (`book/front-matter/`, editor)

| File | Contents |
|---|---|
| `a-preface.md` | Why this book, who it's for, what you'll build, the honest limits of the app |
| `b-how-to-use-this-book.md` | Tiers, callouts, exercises, following along with tags, conventions |
| `c-setting-up-your-machine.md` | JDK 25, Node 24, Git, Docker, an editor; Windows, macOS, Linux; verifying |

---

## Part I. Foundations (`book/part-1-foundations/`, writer-foundations)

Part opener: `00-part-introduction.md`.

### Chapter 1: The big picture (`01-the-big-picture.md`)
Tag: `book-m6-final`. Assumes: nothing.
- 1.1 The problem: showing a document without giving it away
- 1.2 Why hiding a button doesn't protect anything (the naive viewer)
- 1.3 The tile idea: rasterize, slice, deliver one piece at a time
- 1.4 Clients and servers, in one picture
- 1.5 The parts of the app and the tools you'll meet (a map of the book)
- 1.6 What this app can and can't do (honest limits)

### Chapter 2: The command line and your files (`02-command-line-and-files.md`)
Tag: none. Assumes: 1.
- 2.1 What a terminal and a shell are
- 2.2 Folders, paths, and moving around
- 2.3 Creating, reading, copying, deleting files
- 2.4 Environment variables and the PATH
- 2.5 Text files, encodings and line endings (why `.env` files exist)
- 2.6 Ports and processes (what "listening on 8080" means)

### Chapter 3: Your first Java program (`03-first-java-program.md`)
Tag: `book-m6-final` (small pieces). Assumes: 2.
- 3.1 What Java, the JDK and the JVM are
- 3.2 Hello, world: compile and run
- 3.3 Variables, types and operators
- 3.4 Decisions and loops
- 3.5 Methods
- 3.6 Reading errors (compiler errors, stack traces)
- 3.7 A small taste of the app: tile-grid math (`TileGrid`)

### Chapter 4: Classes, objects, records and interfaces (`04-classes-and-objects.md`)
Tag: `book-m6-final`. Assumes: 3.
- 4.1 Classes and objects; fields, constructors
- 4.2 Encapsulation: `private`, getters, immutability
- 4.3 Records for plain data (e.g. `SignedTilePayload`)
- 4.4 Enums for fixed choices (`Role`, `Visibility`)
- 4.5 Interfaces and why we depend on them
- 4.6 Packages and imports (the source layout of the app)
- 4.7 Annotations: a first look

### Chapter 5: Collections, generics, lambdas and exceptions (`05-collections-and-exceptions.md`)
Tag: `book-m6-final`. Assumes: 4.
- 5.1 Lists, sets and maps
- 5.2 Generics in plain words
- 5.3 Lambdas and streams (the small useful subset)
- 5.4 `Optional` and the trouble with `null`
- 5.5 Exceptions: throwing, catching, custom types (`ResourceNotFoundException`)
- 5.6 `try`-with-resources and files (`Path`, `Files`)
- 5.7 Time: `Instant`, UTC and why the app uses it

### Chapter 6: Maven and the shape of a project (`06-maven-and-project-layout.md`)
Tag: `book-m0-mvp`, `book-m6-final`. Assumes: 3, 4.
- 6.1 What a build tool does
- 6.2 `pom.xml` line by line
- 6.3 Dependencies and where they come from
- 6.4 The Maven wrapper (`mvnw`) and why it pins the version
- 6.5 Lifecycle: compile, test, package, verify
- 6.6 Directory layout (`src/main`, `src/test`, `resources`)

### Chapter 7: Git and GitHub (`07-git-and-github.md`)
Tag: whole history. Assumes: 2.
- 7.1 Version control as a save history
- 7.2 Commits, branches, merges
- 7.3 Reading `git log` and `git show`; tags as bookmarks (the `book-m*` tags)
- 7.4 Remotes, pull requests, code review
- 7.5 `.gitignore` and why secrets never get committed
- 7.6 Following this book with tags without breaking your work

### Chapter 8: How the web works (`08-how-the-web-works.md`)
Tag: `book-m1-accounts`. Assumes: 2.
- 8.1 Browsers, servers, requests and responses
- 8.2 URLs, methods (GET, POST, PUT, DELETE) and status codes
- 8.3 Headers, bodies and JSON
- 8.4 Cookies and sessions
- 8.5 Same origin, and why browsers are suspicious
- 8.6 HTTPS and TLS in one page
- 8.7 Looking at real traffic with the browser's developer tools and `curl`

### Chapter 9: SQL and MySQL (`09-sql-and-mysql.md`)
Tag: `book-m2-documents`. Assumes: 2, 8.
- 9.1 Tables, rows, columns, keys
- 9.2 `CREATE TABLE`, types and constraints
- 9.3 `INSERT`, `SELECT`, `UPDATE`, `DELETE`
- 9.4 Relationships: foreign keys and joins
- 9.5 Indexes: why some queries are fast
- 9.6 Transactions and locks (why replacing a PDF takes a row lock)
- 9.7 Migrations: changing a schema safely over time

### Chapter 10: Containers and Docker (`10-docker-and-compose.md`)
Tag: `book-m1-accounts` (database), `book-m5-platform` (full stack). Assumes: 2, 8.
- 10.1 Why containers exist
- 10.2 Images, containers, volumes and networks
- 10.3 Running MySQL 8.4 with Docker
- 10.4 Compose files, profiles and health checks
- 10.5 Environment variables and secrets in containers
- 10.6 Building your own image (a first `Dockerfile`)

---

## Part II. The backend (`book/part-2-backend/`, writer-backend)

Part opener: `00-part-introduction.md`.

### Chapter 11: Spring Boot foundations (`11-spring-boot-foundations.md`)
Tag: `book-m6-final`. Assumes: 3–6, 8.
- 11.1 What a framework is
- 11.2 The first Spring Boot application (`SecureDocViewerApplication`)
- 11.3 Beans and dependency injection
- 11.4 Configuration files: `application.yml`, profiles, environment variables
- 11.5 Starters and auto-configuration
- 11.6 Logging and startup output (reading the log)

### Chapter 12: REST controllers and JSON (`12-rest-controllers-and-json.md`)
Tag: `book-m1-accounts`, `book-m6-final`. Assumes: 8, 11.
- 12.1 What REST means, and what this book means by it
- 12.2 A controller method: `@RestController`, `@GetMapping`
- 12.3 Path variables, query parameters, request bodies
- 12.4 Jackson: turning objects into JSON
- 12.5 Returning bytes: how `TileController` sends PNGs
- 12.6 File uploads (multipart)
- 12.7 Status codes and headers as part of the contract

### Chapter 13: Validation, configuration properties and errors (`13-validation-and-errors.md`)
Tag: `book-m3-hardening`. Assumes: 5, 11, 12.
- 13.1 Never trust input
- 13.2 Bean Validation annotations
- 13.3 Typed configuration (`ViewerProperties`) that fails fast
- 13.4 One JSON error shape: `GlobalExceptionHandler`
- 13.5 Not leaking internals (generic 500 with a reference)
- 13.6 Limits: upload size, page count, decompression bombs

### Chapter 14: Storing data with JPA and Flyway (`14-jpa-and-flyway.md`)
Tag: `book-m1-accounts`, `book-m2-documents`. Assumes: 4, 9, 11.
- 14.1 Objects and tables: the mismatch
- 14.2 Entities and repositories (`AppUser`, `Document`)
- 14.3 Query methods and transactions (`@Transactional`)
- 14.4 Flyway migrations (`V1`, `V2`, `V3`)
- 14.5 Locking rows, optimistic and pessimistic
- 14.6 Testing with H2 vs. real MySQL: why both
- 14.7 Time zones and UTC storage

### Chapter 15: Spring Security I: who are you? (`15-spring-security-authentication.md`)
Tag: `book-m1-accounts`. Assumes: 8.4, 11, 12, 14.
- 15.1 Authentication, authorization, and the difference
- 15.2 The filter chain
- 15.3 Storing passwords: hashing, salting, BCrypt, the 72-byte limit
- 15.4 Sessions and the session cookie (`SDV_SESSION`, httpOnly, SameSite)
- 15.5 Roles: READER, PUBLISHER, ADMIN
- 15.6 Loading users from the database
- 15.7 Same answer for wrong password and unknown user

### Chapter 16: Spring Security II: defenses (`16-spring-security-defenses.md`)
Tag: `book-m1-accounts`, `book-m3-hardening`, `book-m5-platform`. Assumes: 15.
- 16.1 CSRF: the attack and the cookie-plus-header defense
- 16.2 Authorization rules per endpoint; 404 vs 403
- 16.3 Security headers: CSP, `X-Frame-Options`, `nosniff`, `Referrer-Policy`
- 16.4 Throttling sign-in attempts (`LoginThrottle`) and atomic counting
- 16.5 Trusting `X-Forwarded-For` only from a proxy
- 16.6 Session lifetime, idle timeout, revocation
- 16.7 Forced password change

### Chapter 17: Files, images, PDFs and signatures (`17-files-images-pdfs-signatures.md`)
Tag: `book-m0-mvp`, `book-m5-platform`. Assumes: 5, 11, 12.
- 17.1 Pixels, images and PNG
- 17.2 Reading and rendering a PDF with PDFBox
- 17.3 Slicing an image into a grid
- 17.4 Drawing text on an image (watermarks)
- 17.5 Hashes, HMAC and signatures: proving a URL wasn't altered
- 17.6 Threads, pools and limits: bounded work, timeouts
- 17.7 Files on disk safely: staging, atomic move, cleanup

### Chapter 18: Testing the backend (`18-testing-the-backend.md`)
Tag: `book-m6-final`. Assumes: 4, 5, 6, 11–16.
- 18.1 Why we test; the test pyramid
- 18.2 JUnit 5: your first test
- 18.3 Testing pure logic (`TileGridTest`)
- 18.4 Spring integration tests and `MockMvc`
- 18.5 Security tests through the real filter chain
- 18.6 Testcontainers and a real MySQL
- 18.7 Concurrency tests; making flaky tests deterministic

---

## Part III. The frontend (`book/part-3-frontend/`, writer-frontend)

Part opener: `00-part-introduction.md`.

### Chapter 19: TypeScript (`19-typescript.md`)
Tag: `book-m6-final`. Assumes: 3–5, 8.
- 19.1 JavaScript and TypeScript: what each is
- 19.2 Types, interfaces, unions
- 19.3 Functions, arrow functions, modules
- 19.4 `async`, promises and observables (only what the app uses)
- 19.5 Reading TypeScript errors
- 19.6 Types that mirror the API (`document.models.ts`)

### Chapter 20: Node, npm and the Angular toolchain (`20-node-npm-angular-cli.md`)
Tag: `book-m1-accounts`, `book-m6-final`. Assumes: 2, 6, 19.
- 20.1 What Node is; why a frontend needs it
- 20.2 npm, `package.json`, `package-lock.json`
- 20.3 The Angular CLI: `ng serve`, `ng build`, `ng test`
- 20.4 The dev proxy (`proxy.conf.json`) and same origin
- 20.5 Project layout and tsconfig
- 20.6 Semantic versions, `^` and `~`

### Chapter 21: Angular components and templates (`21-angular-components.md`)
Tag: `book-m1-accounts`. Assumes: 19, 20.
- 21.1 Components: a class, a template, a style
- 21.2 Templates: binding, conditionals, loops
- 21.3 Reactivity: signals and change detection (as used in the app)
- 21.4 Component inputs and outputs
- 21.5 Styling, light and dark themes, and accessibility basics
- 21.6 Painting tiles: absolutely positioned `<div>`s with CSS `background-image` from `blob:` URLs (the Angular viewer does not use a canvas; the m0 page did, see Chapter 25)

### Chapter 22: Talking to the backend (`22-http-client-and-services.md`)
Tag: `book-m1-accounts`, `book-m5-platform`. Assumes: 8, 12, 19, 21.
- 22.1 Services and dependency injection in Angular
- 22.2 `HttpClient`: GET, POST, uploads
- 22.3 Interceptors: CSRF header and 401 handling (`session.interceptor.ts`)
- 22.4 Loading, error and empty states
- 22.5 Handling 429 with `Retry-After` and 410 Gone
- 22.6 Session state and idle warnings

### Chapter 23: Routing, guards and forms (`23-routing-guards-forms.md`)
Tag: `book-m1-accounts`, `book-m4-reading`. Assumes: 21, 22.
- 23.1 Routes and pages (`app.routes.ts`)
- 23.2 Guards: `auth.guard.ts`
- 23.3 Forms and client-side validation (and why it's not security)
- 23.4 Deep links and query parameters (page numbers)
- 23.5 Keyboard navigation

### Chapter 24: Testing the frontend (`24-testing-the-frontend.md`)
Tag: `book-m5-platform`, `book-m6-final`. Assumes: 18.1, 19–23.
- 24.1 Unit tests with Vitest
- 24.2 Testing components and services
- 24.3 End-to-end tests with Playwright
- 24.4 Accessibility checks with axe-core
- 24.5 Running tests in CI

---

## Part IV. Building the Secure Document Viewer (`book/part-4-building-the-app/`, writer-app)

Part opener: `00-part-introduction.md`. Each chapter follows the tier template plus **Architecture
blueprint vN** and **Decisions and challenges**. Blueprints are in `book/blueprints/`.

### Chapter 25: Milestone 0: The tiled viewer (`25-m0-the-tiled-viewer.md`)
Tag: `book-m0-mvp`. Blueprint: v0. Assumes: 3–6, 8, 11, 12, 17. (Boot 3.3.4 / Java 21.)
- 25.1 Requirements and the threat we start with
- 25.2 Tile grid math and rendering (`TileGrid`, `TileGenerationService`)
- 25.3 Signed URLs (`SignedUrlService`)
- 25.4 Per-viewer watermarking (`WatermarkService`)
- 25.5 The endpoints (`DocumentController`, `PageTileUrlController`, `TileController`)
- 25.6 A first session service and a one-page viewer (static `index.html`; this page does draw on a `<canvas>`)
- Ends with: Blueprint v0; Decisions and challenges

### Chapter 26: Milestone 1: Accounts, roles and sessions (`26-m1-accounts.md`)
Tag: `book-m1-accounts`. Blueprint: v1. Assumes: 14–16, 19–23, 25.
- 26.1 From "anyone" to real accounts (`AppUser`, `UserAccountService`, `BootstrapAdmin`)
- 26.2 Spring Security configuration (`SecurityConfig`)
- 26.3 Binding tile tokens to a session (`SessionKeys`)
- 26.4 Rate limiting and sign-in lockout (`TileRateLimiter`, `LoginThrottle`)
- 26.5 Admin: sessions, users, audit (`AdminController`)
- 26.6 The Angular frontend appears (tiles now painted as positioned divs with CSS backgrounds instead of a canvas)
- 26.7 First Docker Compose file (MySQL) and migration V1
- Ends with: Blueprint v1; Decisions and challenges

### Chapter 27: Milestone 2: Documents, ownership and audit (`27-m2-documents.md`)
Tag: `book-m2-documents`. Blueprint: v2. Assumes: 26, 9, 14.
- 27.1 Ownership and visibility (`Document`, `Visibility`, `Viewer`)
- 27.2 Sharing and the share picker (`UserDirectoryController`)
- 27.3 Persisting documents; migration V2
- 27.4 The audit trail (`audit/`)
- 27.5 Access rules re-checked on every tile request
- 27.6 Storage janitor and cleaning up
- 27.7 The Manage page
- Ends with: Blueprint v2; Decisions and challenges

### Chapter 28: Milestone 3: Upload and API hardening (`28-m3-hardening.md`)
Tag: `book-m3-hardening`. Blueprint: v3. Assumes: 27, 13, 16.
- 28.1 Upload limits and streaming ingest
- 28.2 Consistent JSON errors without internals
- 28.3 Security headers and the health check
- 28.4 Secrets and configuration hygiene
- 28.5 Frontend upload changes
- Ends with: Blueprint v3; Decisions and challenges

### Chapter 29: Milestone 4: The reading experience (`29-m4-reading.md`)
Tag: `book-m4-reading`. Blueprint: v4. Assumes: 28, 21–23.
- 29.1 Page deep links, keyboard navigation, resume reading
- 29.2 Warning before the idle timeout (`idle.ts`)
- 29.3 A lighter, configurable watermark with a trace code
- 29.4 Audit-log trace filter
- Ends with: Blueprint v4; Decisions and challenges

### Chapter 30: Milestone 5: The platform (`30-m5-platform.md`)
Tag: `book-m5-platform`. Blueprint: v5. Assumes: 29, 10, 24.
- 30.1 Upgrading to Spring Boot 4 and Java 25 (what changed)
- 30.2 The full Docker stack (API, nginx, MySQL) and the Dockerfiles
- 30.3 CI with GitHub Actions
- 30.4 Review-driven hardening: versioned tiles and atomic replace, bounded rendering, tile work limit
- 30.5 Account security: recognized devices, admin unlock, forced password change; migration V3
- 30.6 Trust boundary, metrics, health, HTTPS, backups
- 30.7 Frontend: mobile viewer, access-lost and replaced states
- Ends with: Blueprint v5; Decisions and challenges

### Chapter 31: Milestone 6: The finished app (`31-m6-final.md`)
Tag: `book-m6-final`. Blueprint: v6. Assumes: 25–30.
- 31.1 What changed after m5 (dependency updates, flaky-test fix, Dependabot policy)
- 31.2 A guided tour of the finished repository
- 31.3 Running the whole thing: dev and Docker
- 31.4 What the app does and does not achieve (the README limitations, revisited)
- 31.5 The architecture from v0 to v6
- Ends with: Blueprint v6; Decisions and challenges

---

## Part V. Production (`book/part-5-production/`, writer-production)

Part opener: `00-part-introduction.md`.

### Chapter 32: Security review and threat modeling (`32-security-review.md`)
Tag: `book-m5-platform`. Assumes: 15, 16, 30.
- 32.1 What a threat model is; assets, actors, entry points
- 32.2 The review rounds this project went through
- 32.3 Classes of finding (spoofed headers, races, resource exhaustion, information leaks)
- 32.4 What is not defended, and why (screenshots, MFA)
- 32.5 Doing your own review

### Chapter 33: Deployment and TLS (`33-deployment-and-tls.md`)
Tag: `book-m5-platform`. Assumes: 8.6, 10, 30.
- 33.1 From laptop to server: what changes
- 33.2 nginx as the front door; Caddy and certificates
- 33.3 The compose profiles (`full`, `tls`)
- 33.4 Secure cookies and HSTS
- 33.5 The go-live checklist

### Chapter 34: Backups, restores and operations (`34-backups-and-operations.md`)
Tag: `book-m5-platform`. Assumes: 9, 10, 33.
- 34.1 What state exists and where
- 34.2 Consistent backups of database plus tiles
- 34.3 Restore drills
- 34.4 Retention, purging and the janitor
- 34.5 Living with one instance

### Chapter 35: Health, metrics and alerting (`35-metrics-and-monitoring.md`)
Tag: `book-m5-platform`. Assumes: 11, 33.
- 35.1 Health checks
- 35.2 Metrics with Micrometer and Prometheus
- 35.3 The `sdv_*` counters and what they tell you
- 35.4 Deciding what to alert on
- 35.5 Restricting the metrics endpoint

### Chapter 36: Supply chain and CI (`36-supply-chain-and-ci.md`)
Tag: `book-m5-platform`, `book-m6-final`. Assumes: 6, 7, 20, 30.
- 36.1 What supply-chain risk is
- 36.2 Pinning by digest and commit SHA
- 36.3 Vulnerability scanning (OSV, Trivy)
- 36.4 Dependabot and the stable/LTS-only policy
- 36.5 CI as a gate: what fails the build

---

## The Engineering Trade-offs (`book/tradeoffs/`, writer-production)

### Chapter 37: The engineering trade-offs (`37-engineering-tradeoffs.md`)
Tag: `book-m6-final`. Assumes: everything.

Template (each decision gets a subsection): **The decision** / **What we chose** / **Pros** /
**Cons** / **The enterprise alternative** / **When you'd switch**.
- 37.1 Server-side tiles vs. sending the PDF
- 37.2 Watermark at request time vs. at ingest
- 37.3 Watermark strength: comfort vs. deterrence
- 37.4 App-issued HMAC tokens vs. cloud-signed URLs (S3 plus CDN)
- 37.5 Sessions in memory vs. shared session store
- 37.6 Built-in authentication vs. an identity provider (SSO, MFA)
- 37.7 Local disk tiles vs. object storage
- 37.8 Per-user rate limits vs. per-document sensitivity
- 37.9 MySQL and Flyway vs. alternatives
- 37.10 Client-side blocking (right-click) as friction only
- 37.11 One instance vs. scale-out
- 37.12 Decision table (summary)

---

## Back matter (`book/appendices/`, editor)

| File | Contents |
|---|---|
| `epilogue.md` | Where to go from here |
| `appendix-a-glossary.md` | Generated from `book/GLOSSARY.md` |
| `appendix-b-blueprint-history.md` | Blueprints v0 to v6 side by side |
| `appendix-c-exercise-solutions.md` | Compiled from `NN-slug.solutions.md` files |
| `appendix-d-command-reference.md` | Command line, Git, Maven, npm, Docker cheat sheets |
| `appendix-e-troubleshooting.md` | Common setup and build problems |
| `index-terms.md` | Index-ready vocabulary, generated from the glossary |

## Chapter-to-tag summary

| Tag | Chapters that quote it most |
|---|---|
| `book-m0-mvp` | 6, 17, 25 |
| `book-m1-accounts` | 8, 10, 12, 14–16, 20–23, 26 |
| `book-m2-documents` | 9, 14, 27 |
| `book-m3-hardening` | 13, 16, 28 |
| `book-m4-reading` | 23, 29 |
| `book-m5-platform` | 10, 16, 17, 22, 24, 30, 32–36 |
| `book-m6-final` | 3–5, 11, 18–20, 24, 31, 36, 37 |
