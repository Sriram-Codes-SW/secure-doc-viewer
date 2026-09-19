<!-- chapter: 36 | part: V | owner: writer-production | tag: book-m5-platform, book-m6-final | status: draft -->
# Chapter 36: Supply chain and CI

Most of the code in a running app is code you didn't write: libraries, base images, build tools. This chapter shows how the project checks that code automatically on every change, and how it keeps updates coming without letting a surprise upgrade in.

## Learning objectives

By the end of this chapter, you will be able to:

- Explain what supply-chain risk is, with an example from this project.
- Read the project's GitHub Actions workflow and say what each job proves.
- Explain why images are pinned by digest and actions by commit SHA.
- Distinguish what OSV and Trivy each scan.
- Read Dependabot's LTS-only rules and say why each exists.

## Prerequisites

- Chapter 6: Maven (dependencies, `pom.xml`).
- Chapter 7: Git and GitHub (branches, pull requests).
- Chapter 20: npm and `package-lock.json`.
- Chapter 30: the platform milestone (Docker images and the e2e test).

## Beginner tier: What is the supply chain?

### 36.1 The analogy: ingredients in a kitchen

A restaurant is only as safe as its ingredients. The chef can wash every dish perfectly, but if the flour was contaminated at the mill, customers get sick. Your app's ingredients are libraries (Spring, Angular, PDFBox), base images (Java, nginx, MySQL), and the tools that build it. **Supply-chain risk** is the chance that one of them is flawed or malicious.

The analogy breaks down because software ingredients are updated constantly and you inherit their ingredients too: a library you use pulls in libraries of its own (transitive dependencies).

### 36.2 Terms you need

- **Dependency:** a library your code uses, listed in `pom.xml` or `package.json`.
- **Transitive dependency:** a dependency of a dependency.
- **Vulnerability advisory:** a published notice that a version of some software has a flaw.
- **CI (continuous integration):** a server that runs your build and tests automatically on every change.
- **GitHub Actions:** GitHub's CI service. A **workflow** is a YAML file in `.github/workflows/`; it has **jobs**, each made of **steps**.
- **Digest:** a long hash that identifies exactly one version of a container image, unlike a tag such as `24-alpine` which can move.
- **Commit SHA:** the hash that identifies exactly one commit; used to pin an action to reviewed code.
- **Dependabot:** a GitHub service that opens pull requests when newer versions of your dependencies appear.
- **LTS:** long-term support, a release line that receives fixes for years.

## Intermediate tier: The workflow

*On a first read you can skip to "In this project".*

### 36.3 What CI runs

`.github/workflows/ci.yml` at `book-m5-platform` has four jobs, triggered on every pull request and on pushes to `main`. It declares `permissions: contents: read`, so the workflow can't write to the repository, and a `concurrency` group that cancels an older run when a newer one starts.

**Table 36.1 — CI jobs**

| Job | What it runs | What it proves |
|---|---|---|
| Backend tests | `./mvnw -B verify` on Java 25 | Unit and integration tests pass (H2 in MySQL mode; MySQL 8.4 via Testcontainers when Docker is present) |
| Frontend tests and build | `npm ci`, `ng test`, `ng build --configuration production` on Node 24 | Frontend tests pass and the production build compiles |
| Known-vulnerability scan (OSV) | OSV scanner on `pom.xml` and `frontend/package-lock.json` | No Maven or npm dependency, including transitive ones, has a published advisory |
| End-to-end (Docker stack) | Builds the full stack with throwaway secrets, scans the images with Trivy, runs Playwright | The real containers work together and contain no fixable HIGH or CRITICAL vulnerabilities |

The `e2e` job needs the first two to pass (`needs: [backend, frontend]`).

### 36.4 Throwaway secrets

The e2e job creates a `.env` file at run time with random values from `openssl rand -hex`, masks the admin password in the logs (`::add-mask::`), and starts the stack. Nothing real is stored in the repository, and each run uses fresh secrets. On failure it uploads the Playwright report for seven days.

### 36.5 OSV and Trivy

They look at different things:

- **OSV** reads your dependency lists (`pom.xml`, `package-lock.json`) and checks every library against a database of published advisories. It fails the build on any published advisory.
- **Trivy** scans the *built container images*: the operating system packages and libraries inside `secure-doc-viewer-app` and `secure-doc-viewer-web`. It fails on any HIGH or CRITICAL issue that has a fix available (`--ignore-unfixed --exit-code 1`).

A flaw can hide in one and not the other, so both run. The value is proven by the project's own history: Spring Boot 4.1.1 shipped with Tomcat 11.0.24, which carried three critical advisories, and the project pinned Tomcat to 11.0.26 (Chapter 32; `f682716`).

## Advanced tier: Pinning and update rules

*On a first read you can skip to "In this project".*

### 36.6 Pinning by digest and commit SHA

A tag like `mysql:8.4` can point to a different image tomorrow. The project pins each base image by digest, for example `mysql:8.4@sha256:...`, in the compose file and both Dockerfiles, so a rebuild gets exactly the image that was reviewed. GitHub Actions are pinned the same way, by full commit SHA with the version in a comment, for example `actions/checkout@3d3c42e5...  # v7.0.1`. A tag on an action can be moved by whoever controls it; a SHA cannot.

The cost is that updates are no longer automatic. Dependabot pays it.

### 36.7 Dependabot and the LTS-only rules

<!-- source: PR #10 body; dossier/decisions.md D13; PRs #6, #7, #8 -->
`.github/dependabot.yml` asks for weekly grouped update pull requests for Maven, npm, Docker, Docker Compose, and Actions. CI runs the full suite on each. A week's Dependabot output exposed a problem: it proposed MySQL 26.7 (PR #6), Node 25 (PR #7), and a group with TypeScript 7, Vitest 5, and jsdom 30 that could not even install (PR #8). All three were closed and replaced by PR #10, which added rules:

**Table 36.2 — Dependabot rules from PR #10**

| Ecosystem | Rule | Why |
|---|---|---|
| Docker `node` | ignore 25.x, 27.x, 29.x | Odd Node majors never become LTS |
| Docker `eclipse-temurin` | ignore 26.x to 28.x | Java LTS is 25; the next is 29 |
| Compose `mysql` | ignore major bumps | Stay on the 8.4 LTS; 26.7 is an Innovation release; moving to 9.7 LTS is a deliberate upgrade with a migration test |
| npm `typescript` | ignore minor and major | Angular 22 accepts only TypeScript `>=6.0 <6.1`; it moves with Angular |
| npm `npm-other` group | minor and patch only | Majors arrive as separate PRs so one breaking upgrade can't hold back the rest |

Afterward, Vitest 5 (PR #11) and jsdom 30 (PR #12) arrived as separate pull requests and merged. The lesson: automated updates need rules about *which* versions you accept, or they generate work instead of saving it.

### 36.8 A flaky test on `main`

<!-- source: PR #9 body -->
CI on `main` failed once after PR #5 merged. `aRenderThatTakesTooLongIsAbandonedAndFreesItsSlot` checked that every render slot was free at the same instant the second render returned, but the slot frees in a `finally` block just after the caller gets its result. PR #9 changed the test to wait up to 5 seconds for the counters; production behavior was unchanged. The lesson: a test that races the code it tests fails in CI first, because CI machines are differently fast.

## In this project

| Path | First appears | What it does |
|---|---|---|
| `.github/workflows/ci.yml` | `book-m5-platform` | The four jobs in Table 36.1 |
| `.github/dependabot.yml` | `book-m5-platform`; LTS rules by `book-m6-final` | Weekly grouped updates and the rules in Table 36.2 |
| `Dockerfile`, `frontend/Dockerfile`, `docker-compose.yml` | `book-m5-platform` | Digest-pinned base images |

## Try it

### Exercise 36.1 ★ Which job?

For each, name the CI job that would catch it: a failing unit test; a vulnerable Maven library; an outdated OS package in the nginx image; a broken sign-in screen.

### Exercise 36.2 ★★ Why pin?

Explain what could go wrong if `actions/checkout` were referenced by tag and the tag was moved to malicious code.

### Exercise 36.3 ★★★ Write a rule

This is a hypothetical: suppose a future Angular release accepted a newer TypeScript minor version than the one the project uses. Describe how the `typescript` rule in Table 36.2 (ignore minor and major updates) would treat that newer version when Dependabot finds it, and what a person would do when upgrading Angular.

## Summary

- Most of a running app is code you didn't write, so it needs checking on every change.
- CI runs tests, builds, an OSV dependency scan, image scans with Trivy, and a real end-to-end run.
- Digests and commit SHAs make builds reproducible; Dependabot supplies the updates.
- Update rules that follow LTS lines keep automation from generating breaking changes.

Chapter 37 steps back and weighs every major decision in the app.

## Further reading

- GitHub Actions documentation: https://docs.github.com/en/actions
- Dependabot options reference: https://docs.github.com/en/code-security/dependabot/working-with-dependabot/dependabot-options-reference
- OSV-Scanner: https://google.github.io/osv-scanner/
- Trivy: https://trivy.dev/docs/
