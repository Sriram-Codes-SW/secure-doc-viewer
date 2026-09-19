# Setting up your machine

You need five things: a JDK (Java Development Kit) 25, Node.js 24, Git, Docker and a code
editor. The steps cover Windows, macOS and Linux. Installer pages change; if they differ from
what is written here, follow the official page.

> **Note:** This chapter is a first draft. The editor will add click-by-click detail once the
> writers confirm the versions each chapter uses.

## 1. A terminal

- **Windows:** install Git for Windows (Section 2); it includes Git Bash, and this book's
  commands work there. PowerShell also works for most commands.
- **macOS:** open Terminal.
- **Linux:** any terminal.

## 2. Git

Download from https://git-scm.com, then verify:

```bash
git --version
```

## 3. JDK 25

Install a JDK 25 distribution (Eclipse Temurin is a common choice) or use your platform's package
manager. Verify:

```bash
java -version
javac -version
```

Both should report version 25. The project's Maven wrapper (`./mvnw`) downloads Maven 3.9.16 for
you, so you don't install Maven separately.

## 4. Node.js 24

Install Node 24, which includes npm. Verify:

```bash
node --version
npm --version
```

## 5. Docker

Install Docker Desktop (Windows, macOS) or Docker Engine with the Compose plugin (Linux). Verify:

```bash
docker --version
docker compose version
```

## 6. An editor

Any editor works. IntelliJ IDEA Community and Visual Studio Code both support Java and
TypeScript.

## 7. Get the code

```bash
git clone <repository-url> secure-doc-viewer
cd secure-doc-viewer
git tag
```

You should see the seven `book-m*` tags. Keep this clone for reading and make a second one for
your exercises.

> **Warning:** Keep the storage folder out of synced folders such as OneDrive or Dropbox. They
> lock and rewrite files while the app is using them.

## Verify everything

Each command above should print a version. Chapter 3 runs your first program.
