# Appendix D: Command reference

Commands used in the book, grouped by tool. Run project commands from the repository root unless
noted. `bash` syntax works in Git Bash on Windows, macOS, and Linux.

## Command line (Chapter 2)

| Command | What it does |
|---|---|
| `pwd` | Print the folder you are in |
| `ls` | List files (`ls -la` shows hidden files and details) |
| `cd <folder>` | Move into a folder (`cd ..` goes up one level) |
| `mkdir -p <folder>` | Create a folder, and any missing parents |
| `cat <file>` | Print a file |
| `cp <from> <to>` / `mv <from> <to>` | Copy / move or rename |
| `rm <file>` | Delete a file (there is no undo) |
| `echo $NAME` | Print an environment variable |

## Git (Chapter 7)

| Command | What it does |
|---|---|
| `git clone <url>` | Copy a repository to your machine |
| `git status` | Show changed files |
| `git log --oneline --decorate` | Show history with tags |
| `git tag` | List tags (the seven `book-m*` milestones) |
| `git show <tag>:<path>` | Print a file as it was at a tag, without changing your files |
| `git switch -c <branch>` | Create and move to a new branch |
| `git add <file>` and `git commit -m "<message>"` | Stage and save a change |
| `git diff <tagA> <tagB> --stat` | Summarize what changed between two tags |

## Maven (Chapter 6)

| Command | What it does |
|---|---|
| `./mvnw spring-boot:run` | Start the backend (Flyway creates the schema; needs MySQL running) |
| `./mvnw verify` | Compile, run all backend tests and package |
| `./mvnw test` | Run tests only |

On Windows PowerShell use `.\mvnw.cmd` in place of `./mvnw`. The wrapper (`mvnw`) exists from
`book-m5-platform` onward; earlier tags need Maven installed.

## Node, npm, and Angular (Chapters 19 to 24)

Run these inside the `frontend` folder.

| Command | What it does |
|---|---|
| `npm ci` | Install exactly the versions in `package-lock.json` |
| `npx ng serve` | Start the dev server at `http://localhost:4200`, proxying `/api` to port 8080 |
| `npx ng build` | Build the production app |
| `npx ng test --watch=false` | Run the unit tests once |
| `npx playwright test` | Run the end-to-end tests against the running Docker stack (needs an admin account set through environment variables) |

## Docker and Compose (Chapters 10, 30, 33, and 34)

| Command | What it does |
|---|---|
| `docker compose up -d` | Start MySQL only (development) |
| `docker compose --profile full up -d --build` | Build and start MySQL, the API, and the web container |
| `docker compose --profile full --profile tls up -d --build` | Add the HTTPS frontend (Caddy) |
| `docker compose ps` | Show running services |
| `docker compose logs -f <service>` | Follow a service's log |
| `docker compose down` | Stop and remove containers (volumes are kept) |
| `docker compose --profile full stop app` | Stop the app, for example before a backup |

## curl (Chapter 8)

| Command | What it does |
|---|---|
| `curl -i <url>` | Show the response headers and body |
| `curl -s -c <jar> -b <jar> <url>` | Use a cookie jar to keep cookies between calls |
| `curl -X POST -H "Content-Type: application/json" -d '<json>' <url>` | Send JSON |

Never put a real password in a command you save; use placeholders such as `<password>`.

## End-to-end tests (Chapter 24)

Run these in the `frontend` folder, with the full Docker stack running. The password is one you set or find yourself; never write a real password into a file you commit.

| Command | What it does |
|---|---|
| `docker compose --profile full up -d --build` | Start MySQL, the API, and the web container (run from the repository root) |
| `docker compose logs app` | Show the API's log, which contains a generated first-administrator password once, on the first start of an empty database |
| `npm ci` | Install the frontend's dependencies exactly as locked |
| `npx playwright install chromium` | Download the browser the tests drive (on Linux, add `--with-deps` to install its system libraries too) |
| `export E2E_ADMIN_PASSWORD='<password>'` | Set the administrator password for the tests in bash; in PowerShell use `$env:E2E_ADMIN_PASSWORD='<password>'`, in Windows cmd `set E2E_ADMIN_PASSWORD=<password>` |
| `npx playwright test` | Run the end-to-end tests against the running stack |
