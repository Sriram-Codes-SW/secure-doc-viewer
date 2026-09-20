# Appendix E: Troubleshooting

Each entry gives the symptom, the usual cause, and the fix. The entries are grouped by where you are
in the book: setup, starting the app, using the app, and tests. Appendix D lists the commands the
fixes use.

## Setup

**`java -version` shows a version other than 25.**
Another JDK is earlier on your `PATH`, or the terminal was opened before you installed Java.
Close and reopen the terminal. On Windows check the *Environment Variables* dialog, and confirm
`JAVA_HOME` points to the JDK 25 folder. Tags up to `book-m4-reading` were built for Java 21; JDK
25 runs the project at `book-m5-platform` and later.

**`node --version` is not v24.**
An older Node is on your `PATH`. Reinstall Node 24, or use a version manager (`nvm use 24`).

**`docker: Cannot connect to the Docker daemon` or similar.**
Docker Desktop is not running. Start it and wait until it reports it is running. On Linux, start
the service (`sudo systemctl start docker`) and check you're in the `docker` group.

**Odd file errors while running the app, or storage files vanish.**
The project is inside a synced folder (OneDrive, Dropbox). Move the clone to a plain folder such
as `C:\dev` or `~/dev`, and keep `STORAGE_ROOT` out of synced folders as well.

**`./mvnw` says permission denied (macOS, Linux).**
Run `chmod +x mvnw` once.

## Starting the app

**`docker compose` fails with a message such as "Set DB_PASSWORD in .env".**
Compose needs the values in your `.env` file. Copy `.env.example` to `.env` and fill in
`DB_PASSWORD`, `DB_ROOT_PASSWORD` and `SIGNING_SECRET`. Never commit `.env`.

**The backend refuses to start and mentions the signing secret.**
`SIGNING_SECRET` is missing or shorter than 32 characters; startup fails on purpose.

**The backend cannot connect to the database.**
MySQL isn't running or isn't healthy yet. Run `docker compose up -d`, then `docker compose ps`,
and wait until MySQL reports healthy. Check that `DB_HOST` and `DB_PORT` match.

**"Port already in use" (8080, 4200, 3306, 8081).**
Another program owns the port. Stop it, or change the port in your configuration. Chapter 2
shows how to see what is listening.

**You can't sign in as `admin` on a fresh database.**
On an empty database the first start creates `admin`. Its password is `BOOTSTRAP_ADMIN_PASSWORD`
from `.env`, or, if that is empty, a random one printed once in the startup log. If you missed
the log, reset the database volume for a local experiment (this deletes all data) and start again.

## Using the app

**Sign-in answers `429 Too Many Requests`.**
The sign-in throttle stopped repeated failures. Wait for the time in the `Retry-After` header, or
have an administrator use *Unlock* (from `book-m5-platform`).

**A request fails with `401` or `403` right after you change something.**
State-changing requests need the CSRF cookie echoed in the `X-XSRF-TOKEN` header, and a valid
session. Sign in again and retry (Chapter 16).

**Tiles show a countdown or don't load.**
The per-user tile rate limit (`429`) or the server work limit (`503`) applied; the viewer retries
when the window allows (Chapter 22).

**A document seems to be missing.**
A document you can't view answers `404` on purpose, so you can't tell whether it exists. Ask the
owner to share it.

## Tests

**A test passes alone but fails in the full run.**
Look for shared state or timing. Chapter 18 covers making tests deterministic; the project once
fixed a flaky render-slot assertion in `TileGenerationServiceTest`.

**MySQL integration tests are skipped.**
`MySqlIntegrationTest` needs Docker running; without it the test is skipped rather than failed.

**Playwright says its browser executable does not exist.**
The browser that the end-to-end tests drive has not been downloaded. Run `npx playwright install chromium` in the `frontend` folder (on Linux, `npx playwright install --with-deps chromium`).

**The end-to-end tests fail at the first sign-in.**
The tests need the administrator password of the running stack in the `E2E_ADMIN_PASSWORD` environment variable. It is either the `BOOTSTRAP_ADMIN_PASSWORD` you put in your own `.env` before the first start, or the one-time password printed in the API's log (`docker compose logs app`) when the app generated it. If you started the stack before setting either, reset the local database volume (this deletes all local data) and start again.

**A frontend test command seems to hang.**
`ng test` can stay in watch mode, waiting for changes. Add `--watch=false` to run once and exit, as in Appendix D.
