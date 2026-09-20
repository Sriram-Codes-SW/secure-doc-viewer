<!-- chapter: 0 | part: I | owner: writer-foundations | tag: none | status: expanded -->
# Part I: Foundations

This part gives you the vocabulary and the tools you need to read and build the Secure Document Viewer. You start with no programming experience. By the end you can read most of the project's Java, follow its build, move around its history, explain how a browser talks to a server, read its database migrations, and start its database in a container.

Nothing in this part is theory for its own sake. Every chapter teaches a topic because the app uses it, and every chapter reaches into the real repository for its examples. When Chapter 3 teaches `if` statements, you read the rule that decides whether a password is acceptable. When Chapter 9 teaches indexes, you read the indexes the project added and why. The aim is that no line of the app looks like a foreign language when you meet it again in Parts II to V.

## What the part covers

Table I.1 lists the chapters and says why the app needs each topic.

**Table I.1 — The chapters of Part I**

| Chapter | Topic | Why the app needs it |
|---|---|---|
| 1 | The big picture | The design, the three roles, and the app's honest limits |
| 2 | The command line and your files | Every tool in the book runs from a terminal, and the app's secrets live in environment variables |
| 3–5 | Java 25: values, control flow, classes, records, interfaces, collections, exceptions, time | The backend is written in Java |
| 6 | Maven and the project layout | Maven builds, tests and packages the backend |
| 7 | Git and GitHub | The milestone tags are your checkpoints, and the history holds the project's story |
| 8 | How the web works | The browser and the backend speak HTTP and JSON, and cookies carry the sign-in |
| 9 | SQL and MySQL | Accounts, documents and the audit trail live in a database |
| 10 | Containers and Docker | MySQL and the whole stack run in containers |

## Why this order

Each chapter uses only what earlier ones taught, with a small number of forward pointers that are marked as such. The command line comes first because every later step, from compiling to starting the database, is a command. Java comes next, because reading real code is the fastest way to learn, and Chapter 6 then explains how that code becomes a running program. Git and the web follow, and SQL and Docker come last because they build on the terminal and on the idea of a server.

Notice the rhythm: an idea, then a tiny teaching example you can run, then the same idea in the real code. Snippets labeled *Example* are small teaching code written for this book, and they carry no tag; you can type them into a scratch folder and run them. Snippets labeled *Listing* come from this repository at a named milestone tag, so you can print the exact same file with `git show <tag>:<path>` once Chapter 7 has taught you how.

## Before you start

You need a working setup: a terminal, Git, a JDK, Docker and a code editor. The front matter has a step-by-step guide, [Setting up your machine](../front-matter/c-setting-up-your-machine.md), with a check for each tool. Do it once now. You do not need Node.js yet; Part III asks for it. If a tool misbehaves later, the "Common mistakes" section at the end of each chapter lists the failures beginners hit most, with the symptom you will see and the fix.

Every chapter also ends with exercises graded ★, ★★ and ★★★, and each exercise has a worked solution in Appendix C. If you are short of time, do the ★ exercises in every chapter and the ★★ ones for chapters you found hard; the harder ones reward you when a later part builds on the same idea.

## If you already know some of this

Part I is written for a reader who has never programmed. If you have, skim what you know and slow down at the project-specific parts:

- Chapter 1 explains the design and its limits; read it whatever your background.
- Chapters 3 to 5 can be skimmed for Java syntax, but the incidents (a password limit measured in bytes, a sign-in race, audit times that came from the future) are worth reading.
- Chapter 6 shows the project's real `pom.xml`, including the reason for a pinned web-server version.
- Chapters 7 to 10 each contain a "real incident" from the project's history that later parts refer to.

## What you will have at the end

You will not have built the app yet. You will have a working machine, a small vocabulary that you have used on real snippets from this repository, and the confidence to read a file such as `DocumentController.java` without being lost. You will also have met the app's people (readers, publishers and administrators), its five kinds of would-be attacker, and its seven milestones. Part II starts from there, with the backend.
