<!-- chapter: 20 | part: III | owner: writer-frontend | tag: book-m6-final | status: draft -->
# Chapter 20: Node, npm and the Angular toolchain

Browsers understand JavaScript, but you write TypeScript, split across dozens of files, using libraries other people wrote. Something has to fetch those libraries, compile your code, bundle it into a few files, and serve it while you work. That something is a set of tools built on Node.js and npm, and this chapter shows how the Secure Document Viewer's `frontend/` folder uses them.

## Learning objectives

By the end of this chapter, you will be able to:

- Explain what Node.js and npm are and why a browser application needs them at build time but not at run time.
- Read `package.json` and `package-lock.json` and say what each is for.
- Run the project's scripts and say what `ng serve`, `ng build` and `ng test` do.
- Explain how the development proxy keeps the browser and the API on the same origin.
- Interpret `^` and `~` version ranges.

## Prerequisites

- Chapter 10: containers and Docker (images and build stages).
- Chapter 2: the command line (you will type commands).
- Chapter 6: Maven and project layout (npm plays the same role as Maven for the frontend).
- Chapter 8: how the web works (origins, cookies).
- Chapter 19: TypeScript.

## Beginner tier: Tools that turn source into a website

### 20.1 What Node is; why a frontend needs it

**Node.js** (Node for short) is a program that runs JavaScript outside a browser, on your own computer. Nobody uses Node to show pages to readers. It's used as a workshop: the compiler that turns TypeScript into JavaScript is itself a JavaScript program, so it runs on Node. So do the bundler and the test runner.

Think of a print shop. Readers only ever see the finished pamphlet, but to produce it the shop needs presses, cutters and staplers. Node is the electricity that runs the machines. Once the pamphlets are printed, you can switch the machines off.

**Where the analogy breaks down:** a print shop's pamphlets contain no trace of the machines. A web bundle does contain some code from the libraries you used (Angular itself is shipped to every reader). What stays behind is the tooling: the compiler and test runner never reach the reader.

That is why the project's production image uses two stages (Chapter 10). In the `frontend/Dockerfile`, the first stage starts from a Node image, installs dependencies, and builds. The second stage is only nginx, a web server, holding the finished files. The final image contains no Node at all. The project uses Node 24 (`node:24-alpine`).

### 20.2 npm, `package.json`, `package-lock.json`

**npm** is Node's package manager: a tool that downloads libraries (**packages**) from a public registry and puts them in a folder called `node_modules/`. It plays the role that Maven played for Java (Chapter 6), and `package.json` is the counterpart of `pom.xml`.

**Listing 20.1 — `package.json` (book-m6-final)**

```json
{
  "name": "frontend",
  "version": "0.0.0",
  "scripts": {
    "ng": "ng",
    "start": "ng serve",
    "build": "ng build",
    "watch": "ng build --watch --configuration development",
    "test": "ng test",
    "e2e": "playwright test"
  },
  "private": true,
  "packageManager": "npm@11.19.0",
  "dependencies": {
    "@angular/common": "^22.1.0",
    "@angular/compiler": "^22.1.0",
    "@angular/core": "^22.1.0",
    "@angular/forms": "^22.1.0",
    "@angular/platform-browser": "^22.1.0",
    "@angular/router": "^22.1.0",
    "rxjs": "~7.8.0",
    "tslib": "^2.3.0"
  },
  "devDependencies": {
    "@angular/build": "^22.1.8",
    "@angular/cli": "^22.1.8",
    "@angular/compiler-cli": "^22.1.0",
    "@axe-core/playwright": "^4.13.0",
    "@playwright/test": "^1.63.0",
    "jsdom": "^30.0.1",
    "prettier": "^3.8.1",
    "typescript": "~6.0.2",
    "vitest": "^5.0.1"
  }
}
```

*Path: `frontend/package.json`*

- `"scripts"` names shortcuts you run with `npm run <name>` (or `npm start` and `npm test`, which npm treats specially). `npm test` runs `ng test`.
- `"private": true` stops the project from being published to the public registry by accident.
- `"packageManager"` records which npm version the project expects.
- `"dependencies"` are libraries the shipped app uses: Angular's pieces, RxJS (Chapter 19), and `tslib`, a small helper library the compiled code shares.
- `"devDependencies"` are tools used only while building and testing: the Angular CLI and build tooling, TypeScript, the test tools (`vitest`, `jsdom`, Chapter 24), Playwright and its accessibility add-on, and Prettier, a code formatter (`.prettierrc` sets a 100-column width and single quotes).

> **Note:** Vitest 5.0.1 and jsdom 30 appear only at `book-m6-final`. Tags `book-m1-accounts` to `book-m5-platform` use Vitest 4.0.8 and jsdom 28, and Playwright and the accessibility add-on arrive at `book-m5-platform`.

`package-lock.json` is a much longer file (about 8,000 lines at this tag) that the tools write for you. `package.json` says "Angular 22.1 or newer, below 23"; the lock file records the exact version that was actually installed, such as `rxjs` 7.8.2, along with a fingerprint (an `integrity` hash) of each downloaded file. Commit both. Don't edit the lock file by hand.

### 20.3 The Angular CLI: `ng serve`, `ng build`, `ng test`

The **Angular CLI** (command line interface) is the `ng` program. Three commands cover almost everything:

```bash
npm install     # once: download everything into node_modules
npm start       # ng serve: dev server at http://localhost:4200, reloads on save
npm test        # ng test: run the Vitest specs
npm run build   # ng build: produce the deployable files under dist/
```

`ng serve` compiles the app in memory and serves it with a small **development server**, refreshing the browser whenever you save a file. `ng build` does the same once, with optimization: shrinking and hashing filenames so browsers can cache them for a year (the nginx setup in Chapters 30 and 33 relies on that). The build settings live in `angular.json`, whose `build` target names the entry point (`src/main.ts`), the global stylesheet (`src/styles.css`) and size budgets: in production the initial bundle warns at 500 kB and fails at 1 MB, and any one component's styles warn at 4 kB and fail at 8 kB.

## Intermediate tier: Same origin in development and production

*On a first read you can skip to "In this project"; Part IV comes back to this.*

### 20.4 The dev proxy (`proxy.conf.json`) and same origin

An **origin** is the combination of scheme, host and port: `http://localhost:4200` and `http://localhost:8080` are different origins. Browsers keep origins apart; a page from one may not freely call another. Cookies and the CSRF protection from Part II (Chapter 16) assume the page and the API share an origin.

In development the pieces run on two ports: `ng serve` on 4200 and Spring Boot on 8080. The dev server bridges them with a **proxy**: it forwards any request whose path starts with `/api` to the backend, so the browser thinks it is talking to one server.

**Listing 20.2 — `proxy.conf.json` (book-m6-final)**

```json
{
  "/api": {
    "target": "http://localhost:8080",
    "secure": false,
    "changeOrigin": false,
    "logLevel": "warn"
  }
}
```

*Path: `frontend/proxy.conf.json`*

`angular.json` points the dev server at this file (`"proxyConfig": "proxy.conf.json"`). `target` is where Spring Boot listens; `secure: false` means it doesn't insist on a valid HTTPS certificate for the target (it's plain HTTP on your own machine); `changeOrigin: false` leaves the `Host` header as the browser sent it.

The frontend code itself stays free of this: `core/config.ts` sets `API_BASE_URL = ''`, so every call is a relative URL such as `/api/documents`. In production the same job is done by nginx, whose `location ^~ /api/` block forwards to the backend (Chapters 30 and 33). The same code therefore works unchanged in both places.

### 20.5 Project layout and tsconfig

Reading `frontend/` from the top:

| Path | Purpose |
|---|---|
| `src/main.ts` | The entry point: starts the app |
| `src/index.html` | The one HTML page; contains `<app-root>` |
| `src/styles.css` | Global styles and theme tokens (Chapter 21) |
| `src/app/` | The application: `core/` (session, guards), `features/` (screens) |
| `public/` | Files copied unchanged (the favicon) |
| `e2e/` | Playwright tests (Chapter 24) |
| `tsconfig*.json` | TypeScript settings |

Three `tsconfig` files exist: `tsconfig.json` (shared settings), `tsconfig.app.json` (the app: all `src/**/*.ts` except specs) and `tsconfig.spec.json` (the specs, with Vitest's global functions such as `describe` and `it` available). The shared file sets `"target": "ES2022"` (the JavaScript version to produce), `"module": "preserve"` (leave `import` statements for the bundler), `isolatedModules`, and the extra checks `noImplicitOverride`, `noImplicitReturns`, `noFallthroughCasesInSwitch` and `noPropertyAccessFromIndexSignature`. As Chapter 19 noted, it does not set `"strict": true`.

### 20.6 Semantic versions, `^` and `~`

Most packages number releases `MAJOR.MINOR.PATCH`, such as 22.1.8. By convention a patch fixes bugs, a minor adds features without breaking anything, and a major may break things. This is **semantic versioning**.

- `^22.1.0` accepts any 22.x.y that is 22.1.0 or newer (minor and patch updates, never 23).
- `~6.0.2` accepts only 6.0.x from 6.0.2 up (patches only).

The project uses `~` on TypeScript because each Angular release supports a narrow range of TypeScript versions, and on RxJS to stay on the 7.8 line. The lock file, not the range, decides what you actually get: TypeScript is `~6.0.2` in `package.json` and 6.0.3 in the lock.

## Advanced tier: Reproducible and safe installs

*On a first read you can skip to "In this project"; Part IV comes back to this.*

### 20.7 `npm install` versus `npm ci`

`npm install` may update the lock file if ranges allow newer versions. `npm ci` installs exactly what the lock file says and fails if `package.json` and the lock file disagree. The Dockerfile uses `npm ci --no-audit --no-fund`, so an image rebuilt next month contains the same packages that were tested. The base images are also pinned by digest, a fingerprint of the exact image, so a rebuild can't silently pick up a different one.

### 20.8 Automated updates, deliberately limited

Dependencies age, and old ones carry known security problems. The project uses GitHub's Dependabot (`.github/dependabot.yml`) to open weekly pull requests: Angular packages grouped together, other npm packages grouped as minor and patch updates, and major upgrades as separate pull requests so one breaking change can't hold back the rest. It never proposes TypeScript major or minor upgrades, because TypeScript moves with Angular, and it ignores Node's non-LTS lines (per the file's own comment, odd-numbered Node releases never become LTS). The history shows this working: the Vitest 4 to 5 upgrade (pull request 11) and the jsdom 28 to 30 upgrade (pull request 12) arrived as separate, reviewable changes, and because they arrived in those pull requests, after `book-m5-platform`, Vitest 5 appears only at `book-m6-final`. Chapter 36 covers the supply chain in full.

## In this project

| File | First appears | What it does |
|---|---|---|
| `frontend/package.json`, `package-lock.json` | book-m1-accounts | Scripts, dependencies, exact versions |
| `frontend/angular.json` | book-m1-accounts | Build, serve and test targets; size budgets; proxy setting |
| `frontend/proxy.conf.json` | book-m1-accounts | Dev-time forwarding of `/api` |
| `frontend/tsconfig.json`, `tsconfig.app.json`, `tsconfig.spec.json` | book-m1-accounts | Compiler settings |
| `frontend/Dockerfile` | book-m5-platform | Node build stage, nginx serving stage, `npm ci` |
| `.github/dependabot.yml` | book-m5-platform (see Chapter 36) | Weekly grouped update pull requests |

Try `git show book-m6-final:frontend/package.json`, and compare it with `git show book-m1-accounts:frontend/package.json` to see what testing added.

## Try it

1. ★ Which of the six `scripts` in Listing 20.1 needs a running stack besides Node? (Hint: read `frontend/playwright.config.ts`.)
2. ★ What range of versions does `^22.1.8` accept? Would 22.9.0 be accepted? 23.0.0?
3. ★★ Explain in your own words why `"secure": false` in Listing 20.2 is acceptable in development but would be a concern for a production proxy.
4. ★★ Run `npm ci` in `frontend/`, then `npm run build`. What folder appears, and what do the filenames inside look like?
5. ★★★ Suppose `package.json` says `~6.0.2` but the lock file is deleted. Explain what `npm install` may now do and why `npm ci` refuses to run.

Solutions are in `20-node-npm-angular-cli.solutions.md`.

## Summary

- Node runs JavaScript tools on your machine; the reader's browser never sees it, and the production image contains no Node.
- `package.json` declares what the project needs; `package-lock.json` records exactly what was installed.
- `ng serve`, `ng build` and `ng test` are the daily commands.
- The dev proxy forwards `/api` to Spring Boot so the browser sees one origin, and nginx does the same in production.
- `^` allows minor and patch updates; `~` allows only patches.
- `npm ci` and digest-pinned images make builds reproducible; grouped Dependabot pull requests keep them current.

Next, Chapter 21 opens `src/app/` and builds screens from components.

## Further reading

- Node.js documentation: https://nodejs.org/docs/latest/api/
- npm documentation, "package.json" and "npm ci": https://docs.npmjs.com/
- Angular documentation, "Angular CLI" and "Workspace configuration": https://angular.dev/tools/cli
- Semantic Versioning 2.0.0: https://semver.org/
