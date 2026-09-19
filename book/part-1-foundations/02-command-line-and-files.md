<!-- chapter: 2 | part: I | owner: writer-foundations | tag: none | status: draft -->
# Chapter 2: The command line and your files

Nearly every tool in this book is started by typing a command. This chapter teaches the small set of terminal skills you need: moving between folders, working with files, setting environment variables, and understanding what it means for a program to listen on a port. The app uses all of these: its secrets live in environment variables, and its server listens on port 8080.

## Learning objectives

By the end of this chapter, you will be able to:

- Explain what a terminal and a shell are.
- Move between folders and list their contents with commands.
- Create, read, copy and delete files from the command line.
- Read and set an environment variable, and explain the `PATH`.
- Explain why the project keeps secrets in a `.env` file.
- Explain what "listening on port 8080" means.

## Prerequisites

- Chapter 1: The big picture

## Beginner tier: Talking to your computer in text

### 2.1 What a terminal and a shell are

Until now you have probably given your computer instructions by clicking. A *terminal* is a window where you type instructions instead. A *shell* is the program inside the terminal that reads what you type, runs it, and prints the result. The shell shows a *prompt*, a short line of text that says it is ready.

Why bother? Clicking can't be written down and repeated. A command can be pasted into a document, run by a teammate, or run by a machine in the middle of the night. The project's build, its tests and its automated checks are all commands.

This book uses `bash` syntax. On macOS and Linux, open the Terminal app. On Windows, install Git for Windows, which includes *Git Bash*, and use that. Where PowerShell differs, the book shows both.

**Analogy.** A terminal is like a phone call to a very literal assistant: you say exactly what you want, and it does exactly that. The analogy breaks down because the assistant never asks "are you sure?". Commands such as delete run immediately and usually can't be undone.

### 2.2 Folders, paths and moving around

Your files live in *folders* (also called directories) that nest inside each other. A *path* is the address of a file or folder. Paths use forward slashes in `bash`: `/c/dev/secure-doc-viewer` on Git Bash for Windows, or `/Users/you/dev/secure-doc-viewer` on macOS.

The shell is always "in" one folder, called the *working directory*. Table 2.1 lists the commands you use to look around.

**Table 2.1 — Navigation commands**

| Command | What it does |
|---|---|
| `pwd` | Print the working directory |
| `ls` | List files in the working directory |
| `cd folder` | Move into `folder` |
| `cd ..` | Move up one level |
| `cd ~` | Move to your home folder |

A path is *absolute* if it starts from the top (`/c/dev/...`) and *relative* if it starts from where you are (`src/main`). The name `.` means "this folder" and `..` means "the folder above".

Example 2.1 shows a short session. The lines starting with `#` are comments for you; the shell ignores them.

**Example 2.1 — Moving around**

```bash
pwd                      # where am I?
cd secure-doc-viewer     # go into the project folder
ls                       # what is here?
cd src/main/resources    # a relative path, two levels at once
cd ../../..              # back up three levels
```

### 2.3 Creating, reading, copying and deleting files

Table 2.2 gives the essential file commands.

**Table 2.2 — File commands**

| Command | What it does |
|---|---|
| `mkdir notes` | Create a folder |
| `cat file.txt` | Print a file's contents |
| `cp a.txt b.txt` | Copy a file |
| `mv a.txt notes/` | Move (or rename) a file |
| `rm b.txt` | Delete a file, permanently |
| `rm -r notes` | Delete a folder and everything in it |

`rm` doesn't use a recycle bin. Read a delete command twice before pressing <kbd>Enter</kbd>.

Two shortcuts save time: press <kbd>Tab</kbd> to complete a name, and <kbd>↑</kbd> to bring back the previous command.

## Intermediate tier: How programs find their settings

### 2.4 Environment variables and the PATH

An *environment variable* is a named value that the operating system hands to every program it starts. You can set and read one in the shell:

**Example 2.2 — An environment variable**

```bash
export GREETING="hello"
echo $GREETING
```

The first line sets `GREETING`. The second prints its value. Variables set this way last only for that terminal window.

Programs use environment variables for settings that change from one computer to another, such as a database address or a password. The app reads several: the file `.env.example` in the repository lists them, with empty values where a secret belongs. Here is an excerpt.

**Listing 2.1 — `.env.example` (book-m6-final, excerpt: the database block)**

```text
# MySQL
DB_HOST=localhost
DB_PORT=3306
DB_NAME=securedocs
DB_USERNAME=securedocs
DB_PASSWORD=
DB_ROOT_PASSWORD=
```

*Path: `.env.example`*

Each line is `NAME=value`. The password lines are empty on purpose: you fill them in on your own machine, in a copy named `.env`.

One variable deserves special mention. `PATH` is a list of folders. When you type `java`, the shell searches each folder in `PATH` for a program with that name. If a tool "is not found" although you installed it, its folder is usually missing from `PATH`. You can see yours with `echo $PATH`.

### 2.5 Text files, encodings and line endings (why `.env` files exist)

A *text file* holds characters. An *encoding* is the rule that maps characters to bytes; the project's files use UTF-8. A *line ending* marks where a line stops: Windows uses two bytes for it (carriage return plus line feed) and macOS and Linux use one (line feed). Git and editors can convert between them, which matters in Chapter 7.

Now the reason for `.env`. The app needs secrets, such as the database password and the key used to sign tile URLs. Secrets must never be committed to version control, because history is permanent and shared. So the project keeps a template, `.env.example`, in the repository, and asks you to copy it to `.env`, which the project's `.gitignore` excludes. The real values stay on your machine. <!-- source: .gitignore and .env.example at book-m6-final -->

Copying the template is one command:

```bash
cp .env.example .env
```

Then open `.env` in a text editor and fill in the blanks with values you choose. Never paste a real password into a book, a chat or a commit.

## Advanced tier: Ports and processes

### 2.6 Ports and processes (what "listening on 8080" means)

A running program is a *process*. Your computer can run many at once, and each gets its own number. A *port* is a numbered door on your computer, from 0 to 65535. A server process *listens* on a port: it asks the operating system to hand it any network request addressed to that number.

Two programs can't listen on the same port on the same address. If you start the backend and see an error that port 8080 is already in use, another process holds it. In this project:

- The backend listens on `8080` (`server.port` in `application.yml`).
- MySQL listens on `3306`.
- In the full Docker stack, the web front door is published on `8081`.

<!-- source: src/main/resources/application.yml and docker-compose.yml at book-m6-final -->

The address `localhost` means "this computer". A request to `localhost:8080` never leaves your machine. The project's `docker-compose.yml` publishes MySQL as `127.0.0.1:3306`, which means only your own computer can connect to it, never the network. That is a security choice: the database has no reason to be reachable from elsewhere. Chapter 10 explains the file.

## In this project

At `book-m6-final`, the ideas from this chapter appear in:

- `.env.example` and `.gitignore`: the secrets template and the rule that keeps `.env` out of Git.
- `src/main/resources/application.yml`: uses placeholders such as `${DB_HOST:localhost}`, which read an environment variable and fall back to a default. Chapter 13 explains the syntax.
- `docker-compose.yml`: the ports.

## Try it

### Exercise 2.1 ★ Explore the repository

Open a terminal in your copy of the repository. Use `pwd`, `ls` and `cd` to find `application.yml`, then print it with `cat`. How many levels deep is it below the project folder?

*Solution:* Appendix C, Exercise 2.1.

### Exercise 2.2 ★ Set and use a variable

Set an environment variable `TILE_SIZE=512`, print it with `echo`, then open a new terminal window and print it again. What happens, and why?

*Hint:* variables set with `export` last only for one terminal.

*Solution:* Appendix C, Exercise 2.2.

### Exercise 2.3 ★★ Why is `.env` ignored?

Find the line in `.gitignore` that excludes `.env`. Write two sentences explaining what could go wrong if it were missing.

*Solution:* Appendix C, Exercise 2.3.

## Summary

- A terminal runs a shell; you give it text commands that can be repeated and shared.
- Paths locate files; `pwd`, `ls` and `cd` navigate, and `rm` deletes permanently.
- Environment variables carry settings and secrets into programs; `PATH` tells the shell where programs live.
- The project keeps secrets in a git-ignored `.env` copied from `.env.example`.
- A server listens on a port; the backend uses 8080 and MySQL uses 3306.

## Further reading

- *Git for Windows*, "Git Bash." https://gitforwindows.org/
- *GNU Bash Reference Manual*. https://www.gnu.org/software/bash/manual/bash.html
