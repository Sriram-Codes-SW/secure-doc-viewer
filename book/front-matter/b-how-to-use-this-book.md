# How to use this book

## Reading order

Read Parts I to III in order. Part IV assembles the app milestone by milestone and assumes what
those parts taught. Part V takes the app to production, and Chapter 37 weighs the trade-offs. Part VI (Chapters 38 and 39)
names the patterns you have been using, and Part VII (Chapters 40 and 41) sketches the next step: a
cloud design that the project never deployed.

If you already know a topic, try its exercises first. If you can do the starred exercises without
looking anything up, skim the chapter and move on. Each chapter's Prerequisites section lists the
earlier chapters it assumes, and the table of contents lists the sections of every chapter.

Set up your machine (the next chapter of the front matter) before you start Chapter 1's exercises.

## Three tiers in every chapter

Each feature chapter has three tiers.

- The **Beginner tier** gives the core idea through an everyday analogy, defines the terms you need
  and explains the basic code line by line.
- The **Intermediate tier** shows how the pieces talk to each other, for example how the browser
  and the server exchange messages, and explains why this tool was chosen over the obvious
  alternatives.
- The **Advanced tier** covers security, performance and architecture decisions, told through real
  incidents from this project.

On a first read you can stop after the beginner tier of each chapter and come back for the rest
later. A later tier may use anything from the earlier tiers of the same chapter and from all
earlier chapters; a beginner tier assumes only beginner material.

## Conventions

- **Bold** marks a term at its first use. The glossary (Appendix A) collects definitions of the terms,
  and the index at the end of the book lists, for each term, the chapters that use it, with the
  chapter that defines it in bold.
- Callouts come in four kinds: **Note** (useful, not essential), **Tip** (a habit that saves
  time), **Warning** (something that can lose data or break security) and **In this project**
  (where the idea lives in the repository).
- Listings are numbered by chapter, for example Listing 4.2. The caption names the file and the git
  tag it was copied from. A shortened listing says "simplified" or "excerpt" and says what was
  left out. Code written only to teach is called an Example and carries no tag.
- Figures are diagrams written in text (Mermaid). Each has a caption and a one-sentence text
  description.
- Commands are shown without a prompt so you can copy them. They use `bash` syntax, which works in
  Git Bash on Windows, in Terminal on macOS and in a Linux terminal. Where Windows PowerShell
  differs, both forms are given.
- Example names and addresses are fake, and passwords and secrets are always shown as
  placeholders in angle brackets. Never type a real password into a command you save.

## How to use the exercises

Every feature chapter ends with a *Try it* section of three to six exercises graded by stars.

- **One star** checks your understanding or asks for a small, guided change. Expect 5 to 15
  minutes.
- **Two stars** applies the idea to a new case in the project, with some independence. Expect 30
  to 60 minutes.
- **Three stars** asks you to extend, investigate or design something, and may have more than one
  good answer. Expect an hour or more.

Work each exercise before you read its solution. When you get stuck, reread the section the
exercise points to, then try a smaller version of the problem, then look at the hint. Solutions are
in Appendix C, grouped by chapter. One-star and two-star exercises have full solutions. Three-star
exercises have a worked outline and the key decisions ("one good answer is..."), because your own
answer may be different and still right.

Exercises that change code ask you to work on a branch of your own, never on a tag directly. None
of them needs anyone else's account, credentials or deployment.

## Following along with the code

The repository has seven milestone tags, one for each chapter of Part IV. Table F.1 lists them.

**Table F.1 — The milestone tags**

| Tag | Milestone | Chapter |
|---|---|---|
| `book-m0-mvp` | Tiled viewer with signed URLs and watermarks | 25 |
| `book-m1-accounts` | Accounts, roles, sessions, CSRF, sign-in throttling | 26 |
| `book-m2-documents` | Ownership, sharing, MySQL and Flyway, audit trail | 27 |
| `book-m3-hardening` | Upload and API hardening, secrets, operations | 28 |
| `book-m4-reading` | Reading experience, deep links, idle warning, watermark | 29 |
| `book-m5-platform` | Spring Boot 4, Docker, CI, production hardening | 30 |
| `book-m6-final` | The finished app | 31 |

A tag is a bookmark on a moment in the project's history. You can read a file exactly as it was at
a tag without changing anything on your machine:

```bash
git show book-m2-documents:src/main/java/com/example/securedocviewer/document/DocumentService.java
```

To browse a whole tag, make a separate copy of the repository for reading and switch it to the
tag there. Chapter 7 explains how, and why to keep your own work on a branch. Parts I to III use
the final code (`book-m6-final`) for most examples, and each chapter says which tag its listings
come from.

The first five tags were built with Spring Boot 3.3.4 and Java 21; the last two use Spring Boot
4.1.1 and Java 25. The book teaches the final stack, and each Part IV chapter tells you where the
older versions differ.

## Getting stuck

Read the error message from its first line; most of the time it says what is wrong. Appendix E
lists common setup and build problems with their causes and fixes. Appendix D is a command cheat
sheet. Each chapter's Further reading points to the official documentation, which is the
authority when the book and a tool disagree.
