<!-- chapter: 31 | part: IV | owner: writer-app | tag: book-m6-final | status: draft -->
# Chapter 31: Milestone 6: The final state and keeping it healthy

## Learning objectives

- Explain what changed between `book-m5-platform` and `book-m6-final`, and why the change is small.
- Explain why a test that checks state at the wrong instant is flaky, and how to fix it.
- Read the Dependabot policy and say why the project stays on LTS lines.
- Explain why a dependency update can be refused because of a peer dependency.

## Prerequisites

Chapters 30 (the platform) and 18 (backend testing) and 24 (frontend and end-to-end testing), as
listed in `book/OUTLINE.md`. The code is at `book-m6-final`, the tip of `main` after PRs #9 to #12.
The diff from `book-m5-platform` touches only four paths: `.github/dependabot.yml`,
`frontend/package.json`, its lockfile and `TileGenerationServiceTest`. Versions at this tag: Spring
Boot 4.1.1, Java 25, and (only from this tag) Vitest 5.0.1 and jsdom 30.0.1 in the frontend.
<!-- source: dossier/milestone-briefs.md#m6; git diff --stat book-m5-platform book-m6-final -->

## Beginner tier: What "done" looks like

### 31.1 A milestone with almost no code

The last milestone adds no feature. It is the moment after a release, when the automated systems
around the project start talking. Right after PR #5 merged, CI on `main` failed once, and
Dependabot, the tool that proposes dependency updates, opened three pull requests. Handling them
is part of building an app you plan to keep.
<!-- source: dossier/timeline.md; PR #6, #7, #8, #9 -->

A **dependency** is a library your project uses. **Dependabot** watches them and opens a pull
request when a newer version exists. **CI** (continuous integration) runs your tests on every
change.

## Intermediate tier: A flaky test

*Assumes the beginner tier. This tier covers the failure that CI found, and how the fix works.*

### 31.2 The test that failed once

After PR #5 merged, `main` failed once in CI: the test
`aRenderThatTakesTooLongIsAbandonedAndFreesItsSlot` asserted that every render slot was free at the
same instant the second render returned. The render thread frees its slot in a `finally` block that
runs just after the caller receives its result. On a fast machine the assertion could land in that
gap and read 0 instead of 1. PR #9 changed only the test: it waits up to 5 seconds for the counters.
The fixed test passed five times in a row locally.
<!-- source: PR #9 body; dossier/bugs-and-findings.md#c7 -->

**Listing 31.1 — `TileGenerationServiceTest` (book-m6-final, the added lines)**

*File: `src/test/java/com/example/securedocviewer/service/TileGenerationServiceTest.java`*

```java
// The render thread frees its slot in a finally that runs just after the caller
// has its result, so allow a moment rather than checking at the same instant.
long slotsDeadline = System.currentTimeMillis() + 5_000;
while ((service.availableRenderSlots() != 1 || service.abandonedRendersRunning() != 0)
        && System.currentTimeMillis() < slotsDeadline) {
    Thread.sleep(10);
}
assertEquals(1, service.availableRenderSlots(), "every render slot is free again");
assertEquals(0, service.abandonedRendersRunning(), "no abandoned render still running");
```

The loop checks a condition every 10 milliseconds and stops as soon as it is true, or after 5
seconds. It doesn't slow a passing test down, and it removes the race. Production behavior is
unchanged: the slot frees microseconds after the upload returns.
<!-- source: TileGenerationServiceTest.java diff book-m5-platform..book-m6-final; PR #9 body -->

## Advanced tier: Supply-chain policy

*Assumes the earlier tiers. This tier covers why the project refuses some automatic updates.*

### 31.3 Only stable lines

Dependabot opened three pull requests right after the merge. The owner closed each with a reason.

- **#6, MySQL 8.4 to 26.7:** 26.7 is an Innovation release, not long-term support. The project
  stays on the 8.4 LTS line; moving to the next LTS, 9.7, will be a deliberate upgrade with a
  migration test.
- **#7, Node 24 to 25:** odd-numbered Node majors never become LTS, so the project waits for Node 26.
- **#8, a group with TypeScript 7, Vitest 5 and jsdom 30:** the group could not install, because
  Angular 22 requires TypeScript `>=6.0 <6.1` (a peer dependency of `@angular/build`).
<!-- source: PR #6, #7, #8 comments; dossier/decisions.md#d13 -->

PR #10 recorded the policy in `dependabot.yml` so the tool stops proposing them.

**Listing 31.2 — `.github/dependabot.yml` (book-m6-final, added lines only)**

*File: `.github/dependabot.yml`*

```yaml
# Odd-numbered Node releases never become LTS (24 and 26 are LTS lines).
- dependency-name: node
  versions: ['25.x', '27.x', '29.x']
# Java LTS releases are 25, then 29: skip the non-LTS releases in between.
- dependency-name: eclipse-temurin
  versions: ['26.x', '27.x', '28.x']
# Stay on the MySQL 8.4 LTS line (patches only). ...
- dependency-name: mysql
  update-types: ['version-update:semver-major']
# Angular pins the TypeScript range it supports; TypeScript moves with Angular upgrades.
- dependency-name: typescript
  update-types: ['version-update:semver-major', 'version-update:semver-minor']
```

The `npm-other` group also now takes only minor and patch updates, so a breaking major arrives as its
own pull request and can't hold back the rest. That is why Vitest 5 (PR #11) and jsdom 30 (PR #12)
merged separately.
<!-- source: .github/dependabot.yml at book-m6-final; PR #10 body -->

### 31.4 In this project

**Table 31.1 — Where the concepts live (at `book-m6-final`)**

| Concept | Where |
|---|---|
| Dependency policy | `.github/dependabot.yml` |
| Frontend tool versions | `frontend/package.json` (Vitest 5.0.1, jsdom 30.0.1) |
| The flaky-test fix | `TileGenerationServiceTest` |
| CI | `.github/workflows/ci.yml` |

Table 31.1 lists what changed since Chapter 30.

## Try it

Solutions are in `31-m6-final.solutions.md`.

### Exercise 31.1 ★ Polling loop

Why doesn't the polling loop in Listing 31.1 make a passing test slower?

### Exercise 31.2 ★★ Peer range

A dependency update fails to install because of a peer-dependency range. What should you check first?

### Exercise 31.3 ★★★ Ignore rule

Draft a Dependabot `ignore` rule that keeps a library on its current major version.

## Architecture blueprint v6

Blueprint v6 has no diagram of its own. Per `book/blueprints/v6-final.md`, the architecture is identical to Blueprint v5 (Figure 30.1 in Chapter 30).

**What changed since v5:** no structural change. The non-test changes are the Dependabot policy and the Vitest 5 and jsdom 30 bumps; the flaky-test fix is test code only.

## Decisions and challenges

#### Decision: LTS only

**The decision.** Propose only stable, long-term-support lines, chosen by the product owner
approving the implementer's proposal. **Why.** Non-LTS runtime and database releases carry a
shorter support life, and a migration between database majors deserves a test. **What it costs.**
The project doesn't get the newest features automatically. Open items recorded at this point:
executing the go-live checklist, and possible per-document sensitivity levels.
<!-- source: dossier/decisions.md#d13; dossier/milestone-briefs.md#m6 -->

#### Incident: asserting on state another thread changes

**The problem.** A test read the render-slot counters at the instant the caller got its result.
**How it was found.** CI failed once on `main`. **The fix.** Poll with a timeout (Listing 31.1).
**The lesson.** Never assert on state that another thread changes after your result is returned;
wait for the condition, with a limit.
<!-- source: dossier/bugs-and-findings.md#c7; PR #9 body -->

#### Incident: an update that could not install

**The problem.** A grouped update proposed TypeScript 7 next to Angular 22. **How it was found.**
The install failed on Angular's peer-dependency range. **The fix.** Close the PR, ignore TypeScript
minor and major bumps, and split majors into separate PRs. **The lesson.** Toolchains move together;
an automatic bump of one part can conflict with its neighbor.
<!-- source: PR #8 comment; PR #10 body -->

## Summary

- The final milestone is small on purpose: one test fix and a dependency policy.
- Flaky tests come from asserting at the wrong moment; poll with a deadline.
- Automatic updates need rules: stay on LTS lines and respect peer ranges.

## Further reading

- *GitHub Docs*, "Dependabot options reference." https://docs.github.com/en/code-security/dependabot
- *npm Docs*, "package.json: peerDependencies." https://docs.npmjs.com/cli/configuring-npm/package-json
- *Node.js*, "Releases." https://nodejs.org/en/about/previous-releases
- *MySQL*, "MySQL Release Model." https://dev.mysql.com/doc/refman/8.4/en/mysql-releases.html
