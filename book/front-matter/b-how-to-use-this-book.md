# How to use this book

## Reading order

Read Parts I to III in order; Part IV assembles the app milestone by milestone; Part V takes it
to production; Chapter 37 weighs the trade-offs. If you already know a topic, try its exercises
to check, then move on. `OUTLINE.md` lists what each chapter assumes.

## Three tiers in every chapter

Each feature chapter has a **Beginner tier** (the idea, the vocabulary, the basic code), an
**Intermediate tier** (how the pieces talk to each other and why this tool was chosen) and an
**Advanced tier** (security, performance and architecture, with real incidents from this
project). On a first read you can stop after the beginner tier and return later.

## Conventions

- **Bold** marks a term at its first use; the Glossary (Appendix A) defines it.
- Callouts: Note, Tip, Warning and In this project.
- Listings are numbered and name the file and the git tag they come from. Shortened listings say
  so. Code written only to teach is called an Example and carries no tag.
- Figures are Mermaid diagrams with a text description.
- Exercises are graded from one star (check your understanding) to three (extend the app).
  Solutions are in Appendix C; try first.

## Following along with the code

Each milestone in Part IV matches a git tag in the repository: `book-m0-mvp`,
`book-m1-accounts`, `book-m2-documents`, `book-m3-hardening`, `book-m4-reading`,
`book-m5-platform` and `book-m6-final`. Keep one copy of the repository for reading, and do your
exercises on your own branch. The tags up to `book-m4-reading` use Spring Boot 3.3.4 and Java 21;
`book-m5-platform` and `book-m6-final` use Spring Boot 4.1.1 and Java 25.

## Getting stuck

Read the error message from its first line. Appendix E lists common setup problems. Every
chapter's Further reading points to the official documentation.
