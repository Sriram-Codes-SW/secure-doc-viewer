<!-- chapter: 7 | part: I | owner: writer-foundations | tag: book-m6-final | status: draft -->
# Chapter 7: Git and GitHub

The Secure Document Viewer was built in small steps, and every step is preserved. Git is the tool that keeps that history, and the seven `book-m` tags are the checkpoints you'll use to follow the book. This chapter teaches enough Git to get the code, look at any milestone, and experiment without losing your work.

## Learning objectives

By the end of this chapter, you will be able to:

- Explain what version control is and what a commit is.
- Clone the repository and read its history with `git log`.
- Look at the code as it was at a milestone, using tags.
- Explain branches, merges and pull requests.
- Explain why `.gitignore` exists and why secrets are never committed.
- Follow the book's milestones without breaking your own changes.

## Prerequisites

- Chapter 2: The command line and your files

## Beginner tier: A save history

### 7.1 Version control as a save history

Imagine writing a long document and saving copies named `report-final`, `report-final-2` and `report-really-final`. **Version control** replaces that habit: a tool records every meaningful change, who made it, when, and why, and lets you return to any earlier state. **Git** is the version control tool this project uses.

A Git project is a **repository**: your files plus a hidden folder, `.git`, holding the whole history. Each saved state is a **commit**: a snapshot of the project with a message describing the change and a unique identifier, such as `2d10e07`. Commits form a chain, each pointing to the one before.

**Analogy.** A commit is a numbered save point in a video game; you can reload any one. The analogy breaks down because Git also lets several people, and several lines of work, save at once, and it can merge them.

Check that Git is installed:

```bash
git --version
```

### 7.2 Getting the code and reading history

To get a copy of a repository, you **clone** it. The address comes from the project's page on GitHub, the website that hosts Git repositories. Substitute the address you were given:

```bash
git clone <repository-address>
cd secure-doc-viewer
```

Now read the history with `git log`. The `--oneline` flag shows one line per commit:

```bash
git log --oneline
```

You should see something like this, newest first:

```text
a27e069 Merge pull request #12 from ...
144f13a Bump jsdom from 28.1.0 to 30.0.1 in /frontend
...
b6aef4e Add secure document viewer: tiled rendering with signed URLs and per-viewer watermarking
```

The last line is the very first commit: the project began as a small tiled viewer, which is what `book-m0-mvp` marks. <!-- source: git log at book-m6-final -->

To see what one commit changed, use `git show`:

```bash
git show 2d10e07 --stat
```

`--stat` summarizes which files changed. The commit `2d10e07`, "Phase 5: Spring Boot 4 / Java 25, Docker stack, CI, and e2e tests", is the one that introduced the Dockerfile and the Maven wrapper. <!-- source: git log --diff-filter=A for mvnw -->

## Intermediate tier: Tags, branches and pull requests

### 7.3 Tags as bookmarks: the `book-m*` tags

A **tag** is a permanent name for one commit. The project has seven, and they are this book's checkpoints (Table 7.1).

**Table 7.1 — The milestone tags**

| Tag | Milestone |
|---|---|
| `book-m0-mvp` | Tiled viewer with signed URLs and watermarks |
| `book-m1-accounts` | Accounts, roles, sessions, CSRF, sign-in throttling |
| `book-m2-documents` | Ownership, sharing, MySQL and Flyway, audit trail |
| `book-m3-hardening` | Upload and API hardening, secrets, operations |
| `book-m4-reading` | Reading experience, deep links, idle warning, watermark |
| `book-m5-platform` | Spring Boot 4, Docker, CI, production hardening |
| `book-m6-final` | The finished app |

<!-- source: book/README.md milestone table -->

List them, and read any file as it was at a tag without changing anything on disk:

```bash
git tag
git show book-m2-documents:src/main/resources/db/migration/V2__documents_shares_audit.sql
```

The form `git show <tag>:<path>` prints one file as it was at that tag. This is how the book's listings are checked, and it is the safest way to look around: nothing in your folder changes.

### 7.4 Branches, merges, remotes and pull requests

A **branch** is a movable name for a line of work. The default branch, `main`, holds the accepted history. To try something, you create a branch, commit there, and `main` stays untouched. When the work is good, you **merge** it back.

A **remote** is a copy of the repository on another computer, usually GitHub; `git push` sends your commits to it and `git pull` brings new ones back. On GitHub, a **pull request** (PR) is a proposal to merge one branch into another. It shows the changes, lets others comment, and runs automated checks before anything is merged.

This project used one pull request per phase. The milestone tags mark the merges: for example `book-m1-accounts` is the merge of pull request 1 ("Phase 1: real accounts, roles, and admin lockdown"), and `book-m5-platform` is the merge of pull request 5 ("Phase 5: Spring Boot 4 / Java 25, Docker stack, CI, and e2e tests"). You can read a PR's description with `gh pr view 5` if you have GitHub's command-line tool. <!-- source: gh pr list; git rev-list of tags -->

**Code review** is the practice of having someone else read a change before it lands. Later chapters tell the story of what reviews found in this project.

### 7.5 `.gitignore` and why secrets never get committed

Some files must not be recorded: build output, editor settings, and above all secrets. The file `.gitignore` lists patterns Git skips. Excerpt:

**Listing 7.1 — `.gitignore` (book-m6-final, excerpt)**

```text
# Build output
target/
!.mvn/wrapper/maven-wrapper.jar

# Generated tile storage — never commit ingested documents
storage/

# Local secrets (see .env.example)
.env
```

*Path: `.gitignore`*

Each non-comment line is a pattern. `target/` skips Maven's output, `storage/` skips the uploaded documents' tiles (uploaded content must never enter history), and `.env` skips your secrets. A line starting with `!` makes an exception, here keeping the Maven wrapper's jar file.

Why so strict? Git history is permanent and copied to everyone who clones. If you commit a password and delete it in the next commit, it is still in the history. The only safe practice is to never commit it, and to replace any secret that leaks. That is why the project ships `.env.example` with blanks and asks you to make your own `.env` (Chapter 2).

## Advanced tier: Working safely

### 7.6 Following this book with tags without breaking your work

Two commands let you move to a milestone: `git checkout <tag>` and `git switch --detach <tag>`. Both put you in **detached HEAD** state: you're looking at an old commit and are not on any branch. Commits you make there are easy to lose.

The safe routine:

1. Make sure your own work is saved: `git status` shows uncommitted changes. Commit or set them aside first.
2. To read code, prefer `git show <tag>:<path>` (Section 7.3). It changes nothing.
3. To run or edit a milestone, create a branch from the tag and work there:

```bash
git switch -c my-m2 book-m2-documents
```

This creates and switches to a new branch `my-m2` that starts exactly at the tag. Your experiments live on `my-m2`; the tag and `main` stay intact. Exercises in this book always ask you to work on a branch like this, never directly on a tag.

To go back: `git switch main`.

**We simplify here.** Git has many more commands, such as rebasing and stashing. The app's story doesn't need them, and you can look them up when you do.

## In this project

- The seven `book-m*` tags, and the pull requests behind them (`gh pr view 1` to `gh pr view 5`).
- `.gitignore` and `.env.example`: the secrets rule.
- `.github/workflows/ci.yml`: the automated checks that run on every pull request (Chapter 36).
- `.github/dependabot.yml`: a bot that opens pull requests to update dependencies.

## Try it

### Exercise 7.1 ★ Read the first commit

Run `git log --oneline` and find the oldest commit. Then run `git show <id> --stat`. Which folder holds most of the changed files?

*Solution:* Appendix C, Exercise 7.1.

### Exercise 7.2 ★ Time travel without moving

Print `pom.xml` as it was at `book-m0-mvp` and at `book-m6-final`. Which lines mention the Java version, and how do they differ?

*Solution:* Appendix C, Exercise 7.2.

### Exercise 7.3 ★★ Your own branch

Create a branch from `book-m2-documents`, add a file `notes.txt`, commit it, then switch back to `main` and confirm the file is gone. Switch back to your branch and confirm it returned.

*Solution:* Appendix C, Exercise 7.3.

## Summary

- Git records a project's history as commits; tags are permanent names for commits.
- `git show <tag>:<path>` reads any file at any milestone without changing your files.
- Branches isolate work; pull requests propose merges and trigger review and checks.
- `.gitignore` keeps build output and secrets out of history, which is permanent.
- To run or edit a milestone, create a branch from its tag.

## Further reading

- *Pro Git*, by Scott Chacon and Ben Straub, "Git Basics" and "Git Branching." https://git-scm.com/book/en/v2
- *Git Reference Manual*, `git-show` and `gitignore`. https://git-scm.com/docs
- *GitHub Docs*, "About pull requests." https://docs.github.com/en/pull-requests
