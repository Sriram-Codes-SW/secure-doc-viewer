# Setting up your machine

You need six things: a terminal, Git, a JDK (Java Development Kit) 25, Node.js 24, Docker, and a code
editor. This chapter walks through each on Windows, macOS, and Linux, and ends with a check that
proves everything works. Installer pages change over time; where they differ from what is written
here, follow the official page and use the version numbers below.

> **Note:** Installing takes 30 to 60 minutes, mostly downloads. You do this once.

**Table S.1 — What you need and how to check it**

| Tool | Version | Check command | You should see |
|---|---|---|---|
| Git | any recent | `git --version` | `git version 2.` and more |
| JDK | 25 | `java -version` | a line containing `25` |
| Node.js | 24 | `node --version` | `v24.` and more |
| npm | comes with Node | `npm --version` | a version number |
| Docker | recent | `docker --version` | `Docker version` and more |
| Docker Compose | v2 | `docker compose version` | `Docker Compose version v2.` |

## Step 1. Open a terminal

A **terminal** is a window where you type commands. Chapter 2 teaches it properly; for now you
only need to open one.

- **Windows:** install Git (Step 2) first. It adds *Git Bash*; open it from the Start menu. This
  book's commands work there. Windows PowerShell also works for most commands.
- **macOS:** press <kbd>Cmd</kbd>+<kbd>Space</kbd>, type *Terminal*, press <kbd>Enter</kbd>.
- **Linux:** open your distribution's terminal application.

## Step 2. Git

**Windows:** download the installer from https://git-scm.com, run it, and accept the defaults.

**macOS:** run `git --version`. If Git is missing, macOS offers to install the developer tools;
accept. Or install with Homebrew (`brew install git`).

**Linux:** use your package manager, for example `sudo apt install git` (Debian, Ubuntu) or
`sudo dnf install git` (Fedora).

Check it:

```bash
git --version
```

Then tell Git who you are. This is stored on your machine and appears in your own commits; use
any name and an address you don't mind showing.

```bash
git config --global user.name "Your Name"
git config --global user.email "you@example.com"
```

## Step 3. JDK 25

The JDK contains the Java compiler and the tools to run Java programs. Several vendors ship it;
Eclipse Temurin is a common, free choice.

**Windows:** download the JDK 25 `.msi` installer from https://adoptium.net, run it, and enable
the options that set `JAVA_HOME` and add Java to `PATH`.

**macOS:** download the JDK 25 `.pkg` from https://adoptium.net and run it, or use
`brew install --cask temurin@25`.

**Linux:** install a JDK 25 package from your package manager if available, or unpack the
Temurin archive and add its `bin` folder to your `PATH`.

Close and reopen your terminal (so it sees the new settings), then check:

```bash
java -version
javac -version
```

Both should report 25. If `java` reports an older version, another JDK comes earlier on your
`PATH` (see Appendix E).

For the final code (`book-m6-final`) and for `book-m5-platform` you don't install Maven: the
project's Maven wrapper (`./mvnw`) downloads the right version (3.9.16) the first time you use it.

> **Note:** The earlier milestones are different. The tags `book-m0-mvp` to `book-m4-reading` were
> built with Spring Boot 3.3.4 and **Java 21**, and they have no Maven wrapper, because `mvnw` first
> appears at `book-m5-platform`. To build and run one of those tags yourself, you need a JDK 21 and a
> Maven 3.9 that you install yourself. You don't have to: you can read those tags without building
> them, with `git show <tag>:<path>` (Chapter 7). Table IV.3 in the Part IV introduction lists what
> each tag needs.

**Optional: build the older tags (JDK 21 and Maven).** Skip this unless you want to run
`book-m0-mvp` to `book-m4-reading` yourself. It keeps JDK 25 for everything else.

1. **Install JDK 21 next to JDK 25.** Use the same vendor as before (Eclipse Temurin, from the
   Adoptium project at https://adoptium.net) or your package manager's JDK 21. On macOS,
   `brew install --cask temurin@21`. On Linux, install your distribution's `openjdk-21` package or
   unpack the Temurin 21 archive in its own folder. Installing a second JDK does not remove the first.
2. **Install Maven 3.9.** Download the binary archive from the Apache Maven project
   (https://maven.apache.org/download.cgi), unpack it, and add its `bin` folder to your `PATH`. On
   macOS, `brew install maven` gives a current 3.9 release; on Linux, your package manager's Maven may
   be older than 3.9, so check the version in step 4 and use the archive if it is.
3. **Point one terminal at JDK 21.** Set `JAVA_HOME` to the JDK 21 folder and put its `bin` folder
   first on the `PATH`, only in the terminal window where you build an older tag. Replace the path
   with the folder your installer created.

   In bash (macOS, Linux, Git Bash on Windows):

   ```bash
   export JAVA_HOME="<path to your JDK 21 folder>"
   export PATH="$JAVA_HOME/bin:$PATH"
   ```

   On macOS you can find the folder with `/usr/libexec/java_home -v 21`.

   In Windows PowerShell:

   ```powershell
   $env:JAVA_HOME = "<path to your JDK 21 folder>"
   $env:Path = "$env:JAVA_HOME\bin;$env:Path"
   ```

   In Windows cmd:

   ```text
   set JAVA_HOME=<path to your JDK 21 folder>
   set PATH=%JAVA_HOME%\bin;%PATH%
   ```

4. **Check it.** In that terminal:

   ```bash
   java -version
   mvn -version
   ```

   `java -version` should report 21, and `mvn -version` should report Maven 3.9 and Java 21. Close
   the terminal when you are done, and the next one uses JDK 25 again.

## Step 4. Node.js 24

Node.js runs JavaScript outside a browser, and npm (installed with it) downloads the libraries
the Angular frontend needs.

**Windows and macOS:** download the Node 24 installer from https://nodejs.org and run it with the
defaults.

**Linux:** use your package manager's Node 24 package, or a version manager such as `nvm`
(`nvm install 24`).

Check it in a new terminal:

```bash
node --version
npm --version
```

## Step 5. Docker

Docker runs the MySQL database (and later the full application) in containers, so you don't
install a database by hand.

**Windows and macOS:** install Docker Desktop from https://www.docker.com/products/docker-desktop
and start it. Wait until it reports that it is running. On Windows it may ask you to enable WSL 2
or virtualization; follow its prompts and restart if asked.

**Linux:** install Docker Engine and the Compose plugin following
https://docs.docker.com/engine/install/ for your distribution. To run `docker` without `sudo`,
add yourself to the `docker` group and sign in to your computer again.

Check it, then run Docker's test container:

```bash
docker --version
docker compose version
docker run --rm hello-world
```

The last command should print a message beginning "Hello from Docker!".

> **Tip:** If `docker` says it cannot connect to the daemon, Docker Desktop isn't running yet.
> Start it and wait a minute.

## Step 6. An editor

Any code editor works. Two common free choices:

- **IntelliJ IDEA Community Edition** (https://www.jetbrains.com/idea/): strongest for Java.
- **Visual Studio Code** (https://code.visualstudio.com/): good for Java, TypeScript, and Markdown.

## Step 7. Get the code

Choose a folder that is **not** inside OneDrive, Dropbox, or another synced folder. Those services
lock and rewrite files while the app is using them. For example, use `C:\dev` on Windows and
`~/dev` on macOS and Linux.

```bash
mkdir -p ~/dev
cd ~/dev
git clone <repository-url> secure-doc-viewer
cd secure-doc-viewer
git tag
```

Replace `<repository-url>` with the address of the repository you were given. In Git Bash on
Windows, if you prefer `C:\dev`, run `mkdir -p /c/dev && cd /c/dev` instead of the first two
lines.

You should see the seven milestone tags:

```text
book-m0-mvp
book-m1-accounts
book-m2-documents
book-m3-hardening
book-m4-reading
book-m5-platform
book-m6-final
```

Keep this clone for reading. When an exercise asks you to change code, make a branch of your own
or a second clone (Chapter 7).

> **Warning:** Don't switch to a tag in a folder where you have unsaved work. Chapter 7 shows how
> to look at a tag safely.

## Step 8. The final check

Run these one after another. Each should print a version.

```bash
git --version
java -version
node --version
npm --version
docker --version
docker compose version
```

If all of them work, you're ready for Chapter 1. If one fails, see Appendix E. Chapter 3 runs
your first Java program, and Chapter 10 starts the database.

## Further reading

- Git: https://git-scm.com/book
- Eclipse Temurin installation: https://adoptium.net/installation/
- Node.js downloads: https://nodejs.org/en/download
- Docker: https://docs.docker.com/get-started/
