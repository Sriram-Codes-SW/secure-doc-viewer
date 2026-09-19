<!-- chapter: 0 | part: I | owner: writer-foundations | tag: none | status: draft -->
# Part I: Foundations

This part gives you the vocabulary and the tools you need to read and build the Secure Document Viewer. You start with no programming experience. By the end you can read most of the project's Java, follow its build, move around its history, explain how a browser talks to a server, read its database migrations, and start its database in a container.

## What the part covers

| Chapter | Topic | Why the app needs it |
|---|---|---|
| 1 | The big picture | The design and its honest limits |
| 2 | The command line and your files | Every tool in the book runs from a terminal |
| 3–5 | Java 25: values, control flow, classes, records, interfaces, collections, exceptions | The backend is written in Java |
| 6 | Maven and the project layout | Maven builds, tests and packages the backend |
| 7 | Git and GitHub | The milestone tags are your checkpoints |
| 8 | How the web works | The browser and the backend speak HTTP and JSON |
| 9 | SQL and MySQL | Accounts, documents and the audit trail live in a database |
| 10 | Containers and Docker | MySQL and the whole stack run in containers |

## Why this order

Each chapter uses only what earlier ones taught. The command line comes first because every later step, from compiling to starting the database, is a command. Java comes next, because reading real code is the fastest way to learn, and Chapter 6 then explains how that code becomes a running program. Git and the web follow, and SQL and Docker come last because they build on the terminal and on the idea of a server.

## What you will have at the end

You won't have built the app yet. You will have a working machine, a small vocabulary that you have used on real snippets from this repository, and the confidence to read a file such as `DocumentController.java` without being lost. Part II starts from there.

Every code snippet labeled as a *Listing* comes from this repository at a named tag. Snippets labeled *Example* are small teaching code written for this book; they are not the project's code.
