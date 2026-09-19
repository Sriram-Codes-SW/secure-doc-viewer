<!-- chapter: 24 | part: III | owner: writer-frontend | tag: book-m6-final | status: draft -->
# Chapter 24: Testing the frontend

The frontend makes promises: a reader who wasn't given a document never sees it, a page turn asks for exactly one new page of tiles, every screen is readable in light and dark themes. Tests turn those promises into checks that run on every change. This chapter shows the two layers the project uses: fast unit tests with Vitest, and slow, realistic end-to-end tests with Playwright, including automated accessibility checks.

## Learning objectives

By the end of this chapter, you will be able to:

- Explain the difference between a unit test and an end-to-end test, and why the project has both.
- Read a Vitest spec, including its `describe`, `it` and `expect` structure.
- Test a component and a service without a real server using `HttpTestingController`.
- Read the Playwright test and explain what the axe accessibility check verifies.
- Explain what CI runs on the frontend.

## Prerequisites

- Chapter 18: testing the backend (the idea of assertions, test isolation).
- Chapters 19–23: TypeScript, components, services, routing.

## Beginner tier: Two kinds of check

### 24.1 Unit tests with Vitest

A **unit test** runs a small piece of code in isolation and checks its result. **Vitest** is the test runner: it finds files ending in `.spec.ts`, runs them, and reports which checks passed. It plays the role JUnit did for Java (Chapter 18). You run it with `npm test` (which is `ng test`, Chapter 20). The simplest specs in the project test a pure function, like the idle-timeout arithmetic from Chapter 19:

**Listing 24.1 — `idle.spec.ts` (book-m6-final)**

```typescript
import { idleState } from './idle';

describe('idleState', () => {
  const t0 = 1_000_000;

  it('is active well before the timeout', () => {
    expect(idleState(t0 + 10 * 60_000, t0, 1800)).toEqual({ kind: 'active' });
  });

  it('warns in the last five minutes, counting down', () => {
    expect(idleState(t0 + 26 * 60_000, t0, 1800)).toEqual({ kind: 'warning', secondsLeft: 240 });
  });

  it('expires once the timeout has passed', () => {
    expect(idleState(t0 + 30 * 60_000, t0, 1800)).toEqual({ kind: 'expired' });
  });

  it('scales the warning window down for short timeouts', () => {
    expect(idleState(t0 + 30_000, t0, 120)).toEqual({ kind: 'active' });
    expect(idleState(t0 + 70_000, t0, 120)).toEqual({ kind: 'warning', secondsLeft: 50 });
  });
});
```

*Path: `frontend/src/app/core/idle.spec.ts`*

- `describe('idleState', () => { ... })` groups related tests under a name.
- `it('...', () => { ... })` is one test, named as a sentence about behavior. When it fails, the name tells you what broke.
- `expect(actual).toEqual(expected)` compares values, looking inside objects. If they differ, the test fails and shows both.
- `1_000_000` is just a number with underscores to make it readable. Times are passed in as arguments rather than read from the clock, so the tests are instant and repeatable: this is why `idleState` was written as a pure function.
- Notice `describe`, `it` and `expect` are never imported. `tsconfig.spec.json` (Chapter 20) loads `vitest/globals`, which makes them available everywhere in spec files.

> **Note:** Vitest 5.0.1 and its DOM simulator jsdom 30 are used at `book-m6-final`. Tags `book-m1-accounts` to `book-m5-platform` use Vitest 4.0.8 and jsdom 28. The specs quoted here read the same either way.

The **jsdom** package is a pretend browser written in JavaScript, so component specs can create elements and read `localStorage` without opening a real browser.

### 24.2 Testing components and services

Components and services need Angular around them. Angular's **TestBed** builds a small test-only application: you tell it which providers to use, ask it for objects, and it wires dependencies as in the real app. The trick for HTTP is to swap the real network for a fake: `provideHttpClientTesting()` replaces it, and `HttpTestingController` lets the test say exactly which request it expects and what answer to give.

**Listing 24.2 — `auth.guard.spec.ts` (book-m6-final, excerpt)**

```typescript
describe('route guards', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()] });
  });

  function signIn(role: 'READER' | 'ADMIN', mustChangePassword: boolean): void {
    TestBed.inject(SessionService).login('someone', 'pw').subscribe();
    TestBed.inject(HttpTestingController).expectOne('/api/auth/login')
      .flush({ username: 'someone', role, sessionTimeoutSeconds: 1800, mustChangePassword });
  }
  // ... helpers `run` and `path` omitted

  it('sends signed-out users to login with a return URL', () => {
    expect(path(run(authGuard, '/viewer/abc?page=3'))).toBe('/login?returnUrl=%2Fviewer%2Fabc%3Fpage%3D3');
  });

  it('keeps readers out of admin screens', () => {
    signIn('READER', false);
    expect(path(run(roleGuard('ADMIN'), '/admin'))).toBe('/documents');
    expect(run(authGuard, '/documents')).toBe(true);
  });
});
```

*Path: `frontend/src/app/core/auth.guard.spec.ts`*

(Excerpt: the second test in the file, about a password that an administrator set, is omitted, as are the two helper functions.)

- `beforeEach` runs before every test, giving each a fresh setup so tests can't affect each other.
- `TestBed.inject(SessionService)` retrieves the real service, wired to a fake HTTP layer.
- `expectOne('/api/auth/login')` asserts that exactly one request to that address was made, and `.flush({...})` answers it with the given JSON, as if the server had replied. The test never opens a network connection.

The viewer spec uses the same tools to check navigation:

**Listing 24.3 — `viewer.component.spec.ts` (book-m6-final, excerpt)**

```typescript
  it('turns pages with the arrow keys and jumps with Home/End', () => {
    const viewer = create();
    expectGridRequestFor(0);

    viewer.onKeydown(new KeyboardEvent('keydown', { key: 'ArrowRight' }));
    expect(viewer.currentPage()).toBe(1);
    expectGridRequestFor(1);

    viewer.onKeydown(new KeyboardEvent('keydown', { key: 'End' }));
    expect(viewer.currentPage()).toBe(4);
    expectGridRequestFor(4);
    // ... Home key check omitted
  });
```

*Path: `frontend/src/app/features/viewer/viewer.component.spec.ts`*

The helper `expectGridRequestFor(page)` asserts that the viewer asked the API for that page's signed tile URLs. The test proves two things at once: the keyboard moves the page number, and each page turn triggers exactly one new request.

## Intermediate tier: What each spec protects

*On a first read you can skip to "In this project"; Part IV comes back to this.*

### 24.3 Specs as executable requirements

Read the test *names* in the project and you get a list of promises: "opens the page named in ?page= (1-based)", "ignores an out-of-range ?page= and resumes the last page read instead", "leaves keys alone while typing in a field or with modifiers held", "turns pages with a horizontal swipe, but not when zoomed in", "notices a replaced document from its tile URLs and reloads it with a notice", "stops instead of reloading forever when tiles are gone but the document has not changed", and "shows the access-lost state when the document stops being available mid-read". Each corresponds to a behavior from Chapters 22 and 23. The others cover the same ground on other screens: the document list's filter and access labels, the manage screen asking for confirmation before removing someone's access (`http.expectNone(...)` proves no request was sent after the first click), the upload component refusing non-PDFs and oversized files before anything is uploaded, and the admin screen's polling rule.

A pattern worth copying is in the "asks before removing someone" test: it makes a call, asserts that the confirmation state changed, asserts with `expectNone` that *no* `DELETE` request went out, then repeats the call and asserts that the request now exists. Tests can prove that something did *not* happen.

Not everything is testable at unit level. The viewer's real `fetch()` calls, the actual signed URLs, the nginx proxy and the real backend are absent from these specs. That is what the second layer is for.

### 24.4 End-to-end tests with Playwright

**Playwright** drives a real browser (Chromium) against a real running stack: it clicks, types and reads what the page shows, as a person would. The configuration says where the stack lives:

**Listing 24.4 — `playwright.config.ts` (book-m6-final)**

```typescript
export default defineConfig({
  testDir: './e2e',
  timeout: 120_000,
  retries: process.env['CI'] ? 1 : 0,
  reporter: process.env['CI'] ? [['list'], ['html', { open: 'never' }]] : 'list',
  use: {
    baseURL: process.env['E2E_BASE_URL'] ?? 'http://localhost:8081',
    trace: 'retain-on-failure',
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
});
```

*Path: `frontend/playwright.config.ts`*

(Excerpt: the file's leading comment and import are omitted.) The comment in the file says the tests need a running full stack (`docker compose --profile full up -d --build`, served at port 8081) and an admin account supplied through `E2E_ADMIN_USER` and `E2E_ADMIN_PASSWORD` environment variables. Never write a password into a file; the test reads it from the environment and skips itself if it's missing. `retries` allows one automatic retry only in CI, and `trace: 'retain-on-failure'` saves a recording of a failed run for inspection.

The main test, `e2e/secure-viewing.spec.ts`, tells one story: an administrator creates three users (a publisher, a reader, an outsider); the publisher first signs in with a temporary password, is forced to change it, uploads a two-page PDF that the test generates, and shares it with the reader; the reader sees every tile load and turns the page with the keyboard (the URL changes to `?page=2`); and the outsider can neither see the document in their list nor open it by address, getting "hasn't been shared with you". Every user name is unique per run (`e2e-pub-<time>`), and an `afterAll` disables the created accounts, with a warning in the source: never point this suite at production, since it creates accounts.

A second test guards a real incident, described next.

## Advanced tier: Accessibility checks and a regression test

*On a first read you can skip to "In this project"; Part IV comes back to this.*

### 24.5 Accessibility checks with axe-core

**axe-core** is an engine that inspects a rendered page against the Web Content Accessibility Guidelines (Chapter 21) and reports violations such as low-contrast text, form fields with no label, or missing names on buttons. `@axe-core/playwright` lets a Playwright test run it on the current screen. The project wraps it in a helper used on the sign-in, admin, upload, manage, document-list and viewer screens:

**Listing 24.5 — `secure-viewing.spec.ts` (book-m6-final, excerpt: `expectAccessible`)**

```typescript
/** No serious or critical WCAG 2.1 A/AA violations on the current screen, in light and dark themes. */
async function expectAccessible(page: Page, screen: string): Promise<void> {
  for (const colorScheme of ['light', 'dark'] as const) {
    await page.emulateMedia({ colorScheme });
    const results = await new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa']).analyze();
    const serious = results.violations
      .filter((v) => v.impact === 'serious' || v.impact === 'critical')
      .map((v) => `${v.id}: ${v.help} (${v.nodes.map((n) => n.target.join(' ')).join(', ')})`);
    expect(serious, `accessibility on ${screen} (${colorScheme})`).toEqual([]);
  }
  await page.emulateMedia({ colorScheme: 'light' });
}
```

*Path: `frontend/e2e/secure-viewing.spec.ts`*

The loop runs the check twice per screen, once per theme: `emulateMedia({ colorScheme })` makes the browser report the system theme, which triggers the `prefers-color-scheme: dark` block of `styles.css` (Chapter 21). This is why the contrast tokens matter: both palettes are checked. The `withTags` list selects WCAG 2.0 and 2.1 levels A and AA. Only *serious* and *critical* findings fail the test; lesser ones are not gated.

> **Note:** Automated checks catch only a portion of accessibility problems (contrast, missing labels, wrong roles). They can't tell whether a screen makes sense to someone using a screen reader. Treat a green axe run as a floor, not a finish line.

The accessibility check arrived with the e2e suite at `book-m5-platform`, the same milestone in which `styles.css` gained the `--on-accent` token and a darker `--muted` (the git diff of `styles.css` between `book-m4-reading` and `book-m5-platform`).

### 24.6 A regression test for a real incident

Chapter 22 described how a proxy that appended to a client-supplied `X-Forwarded-For` header let an attacker reset the sign-in throttle by changing the header each attempt. The second e2e test is the regression test that keeps it fixed. It sends six wrong-password sign-ins, each claiming a different address, and expects the first five to be refused with 401 and the sixth with 429:

**Listing 24.6 — `secure-viewing.spec.ts` (book-m6-final, excerpt)**

```typescript
  expect(statuses.slice(0, 5)).toEqual([401, 401, 401, 401, 401]);
  expect(statuses[5]).toBe(429);
```

*Path: `frontend/e2e/secure-viewing.spec.ts`*

The test copies the CSRF token from the `XSRF-TOKEN` cookie into the `X-XSRF-TOKEN` header by hand (`postJson`), because a raw API client doesn't have Angular's automatic behavior (Chapter 22). A unit test could not have caught the bug: it lived in the nginx configuration, which only a test against the whole stack exercises.

### 24.7 Running tests in CI

**Continuous integration (CI)** runs the tests automatically on every proposed change. `.github/workflows/ci.yml` has a job named "Frontend tests & build": it installs Node 24, runs `npm ci --no-audit --no-fund` (Chapter 20), `npx ng test --watch=false` (Vitest once, not in watch mode) and `npx ng build --configuration production`. A separate "End-to-end (Docker stack)" job starts the whole stack in Docker, installs Chromium and runs `npx playwright test`; it needs the backend and frontend jobs to pass first. Chapter 36 covers the whole pipeline.

## In this project

| File | First appears | What it does |
|---|---|---|
| `frontend/src/app/core/idle.spec.ts` | book-m4-reading | Pure-function tests |
| `frontend/src/app/features/viewer/viewer.component.spec.ts` | book-m4-reading (more cases at book-m5-platform) | Navigation, replaced-document and access-lost behavior |
| `frontend/src/app/core/auth.guard.spec.ts`, `app.spec.ts`, `document-list.component.spec.ts`, `manage-upload.component.spec.ts`, `admin-dashboard.component.spec.ts` | book-m5-platform for guard, manage/upload and admin; document list at book-m2-documents | Guards, nav visibility, list, manage, upload, admin |
| `frontend/playwright.config.ts`, `frontend/e2e/secure-viewing.spec.ts` | book-m5-platform | End-to-end and accessibility tests |
| `.github/workflows/ci.yml` | book-m5-platform | Runs both layers in CI |

Run the specs yourself with `npm test` in `frontend/`, and see one at a tag with `git show book-m6-final:frontend/src/app/core/idle.spec.ts`.

## Try it

### Exercise 24.1 ★

In Listing 24.1, add a test: with a timeout of 600 seconds and 100 seconds since activity, the state is `active`. Run it.

### Exercise 24.2 ★

What does `expectOne` fail on if the code under test makes two requests to the same URL?

### Exercise 24.3 ★★

Write a spec for `SessionService` that calls `login`, flushes a user, and checks that `isLoggedIn()` becomes true.

### Exercise 24.4 ★★

Why does `expectAccessible` run the check in both light and dark themes?

### Exercise 24.5 ★★★

Explain why the X-Forwarded-For regression can only be caught by the end-to-end layer, and what a unit test could still add.


Solutions are in `24-testing-the-frontend.solutions.md`.

## Summary

- Unit tests (Vitest) are fast and isolated; end-to-end tests (Playwright) are slow but exercise the real stack. The project needs both.
- `describe`, `it` and `expect` structure a spec; pure functions like `idleState` are the easiest to test.
- `TestBed` with `HttpTestingController` tests components and services against a fake server, and can prove a request was not sent.
- Test names read as a list of the app's promises.
- axe-core checks WCAG A/AA in both themes, failing on serious or critical issues; it complements, not replaces, human review.
- A regression test keeps a past incident, the spoofed forwarding header, from returning.
- CI runs `ng test`, a production build and the end-to-end suite on every change.

This closes Part III. Part IV rebuilds the app milestone by milestone, and you now have the vocabulary to follow every frontend change in it.

## Further reading

- Vitest documentation: https://vitest.dev/
- Angular documentation, "Testing" and "HttpClient testing": https://angular.dev/
- Playwright documentation, "Accessibility testing": https://playwright.dev/
- Deque, axe-core documentation: https://www.deque.com/axe/
