<!-- chapter: 20 | part: III | owner: writer-frontend | solutions -->
# Chapter 20 solutions

1. `npm run e2e` (`playwright test`). Per `playwright.config.ts`, the end-to-end tests run against a full running stack served at `http://localhost:8081` and need an admin account supplied through environment variables.

2. `^22.1.8` accepts 22.1.8 and any newer 22.x.y, so 22.9.0 is accepted. 23.0.0 is not (a new major).

3. `secure: false` turns off certificate verification for the proxy's connection to the target. In development the target is `http://localhost:8080` on your own machine, so nothing travels over a network. A production proxy would forward across a real network, where skipping verification would allow someone to impersonate the backend. (Note that production here uses nginx over a private container network, not this dev proxy.)

4. A `dist/` folder appears, containing `dist/frontend/browser/` with `index.html` and JavaScript and CSS files whose names include a content hash, for example `main-XXXXXXXX.js` (the exact names vary). The Dockerfile copies that `browser` folder into nginx.

5. Without a lock file, `npm install` may pick the newest 6.0.x available (for example a later patch than the one you tested with) and writes a new lock file. `npm ci` refuses to run because its whole purpose is to install exactly what a lock file records; with none, there is nothing to reproduce.
