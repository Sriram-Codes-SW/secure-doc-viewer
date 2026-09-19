<!-- chapter: 23 | part: III | owner: writer-frontend | tag: book-m6-final | status: draft -->
# Chapter 23: Routing, guards and forms

A single-page application has one HTML page, but readers expect many: a sign-in page, a document list, a viewer you can bookmark. This chapter shows how Angular's router creates that illusion, how guards keep readers out of screens they can't use, how the forms collect input, and how the viewer supports deep links and keyboard navigation.

## Learning objectives

By the end of this chapter, you will be able to:

- Read `app.routes.ts` and say which URL shows which component.
- Explain what a route guard does, and why it is a convenience and not a security control.
- Write a template-driven form with `ngModel` and explain why client-side validation is not security.
- Explain how `?page=N` deep links and the viewer's "resume reading" work.
- Explain how the viewer handles keyboard and swipe input safely.

## Prerequisites

- Chapter 8: URLs, query strings.
- Chapter 16: what the server enforces (authorization on every request).
- Chapters 21–22: components, signals, services and the interceptor.

## Beginner tier: Pages without page loads

### 23.1 Routes and pages (`app.routes.ts`)

A **single-page application** (SPA) loads one HTML document once. When you click a link, Angular's **router** changes the address in the address bar, swaps which component is shown in the `<router-outlet />` (Chapter 21), and never reloads the browser. Think of a hotel lobby with one desk and many rooms: the front door never changes, but the desk directs you to a different room depending on what you ask for.

**Where the analogy breaks down:** a hotel's rooms exist whether or not you visit. Angular creates a route's component when you arrive and destroys it when you leave, so its signals start fresh each time.

The routes are a list that maps URL paths to components:

**Listing 23.1 — `app.routes.ts` (book-m6-final)**

```typescript
export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'documents' },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'documents',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/documents/document-list.component').then((m) => m.DocumentListComponent),
  },
  {
    path: 'documents/upload',
    canActivate: [roleGuard('PUBLISHER', 'ADMIN')],
    loadComponent: () => import('./features/documents/upload.component').then((m) => m.UploadComponent),
  },
  {
    path: 'documents/:documentId/manage',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/documents/manage.component').then((m) => m.ManageDocumentComponent),
  },
  {
    path: 'viewer/:documentId',
    canActivate: [authGuard],
    loadComponent: () => import('./features/viewer/viewer.component').then((m) => m.ViewerComponent),
  },
  // ... admin, account, and a catch-all route (shown in full in the repository)
];
```

*Path: `frontend/src/app/app.routes.ts`*

(Simplified: the `admin` and `account` entries, which follow the same pattern, and the catch-all `{ path: '**', redirectTo: 'documents' }` are omitted.)

- `path: ''` with `redirectTo: 'documents'` sends the bare address to the document list. `pathMatch: 'full'` means "only when the path is exactly empty".
- `:documentId` is a **route parameter**: `viewer/abc123` matches, with `documentId` set to `abc123`. The viewer reads it with `this.route.snapshot.paramMap.get('documentId')`.
- `loadComponent: () => import(...)` is **lazy loading**: the component's code is downloaded only when someone first visits that route, which keeps the initial download small. The build budget in `angular.json` (Chapter 20) enforces a size limit on it.
- `canActivate: [...]` lists **guards**, covered next.

## Intermediate tier: Guards, forms and the trust boundary

*On a first read you can skip to "In this project"; Part IV comes back to this.*

### 23.2 Guards: `auth.guard.ts`

A **route guard** is a function that Angular runs before entering a route. It returns `true` to allow entry, or a redirect.

**Listing 23.2 — `auth.guard.ts` (book-m6-final)**

```typescript
export const authGuard: CanActivateFn = (_route, state) => {
  const sessionService = inject(SessionService);
  const router = inject(Router);

  if (sessionService.isLoggedIn()) {
    // An admin-set password must be replaced first; the server enforces the same rule.
    if (sessionService.mustChangePassword() && !state.url.startsWith('/account')) {
      return router.createUrlTree(['/account'], { queryParams: { required: 1, returnUrl: state.url } });
    }
    return true;
  }
  return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
};

/**
 * UX only: keeps users off screens their role can't use. The server
 * enforces the same rules on every API call regardless of this guard.
 */
export const roleGuard =
  (...roles: Role[]): CanActivateFn =>
  (route, state) => {
    const signedIn = authGuard(route, state);
    if (signedIn !== true) {
      return signedIn;
    }
    return inject(SessionService).hasAnyRole(...roles) ? true : inject(Router).createUrlTree(['/documents']);
  };
```

*Path: `frontend/src/app/core/auth.guard.ts`*

`inject(...)` fetches services inside a plain function (Chapter 22). A signed-out visitor to `/viewer/abc?page=3` is redirected to `/login?returnUrl=%2Fviewer%2Fabc%3Fpage%3D3`, so signing in lands them where they meant to go. `roleGuard('ADMIN')` is a function that *builds* a guard: it first runs `authGuard`, then checks the role. Because `restore()` runs at startup (Chapter 22), the guard knows who is signed in before it first runs.

The doc comment says it plainly: **UX only**. A guard runs in the reader's own browser, which the reader controls. Someone can edit the JavaScript, or skip the frontend entirely and call `/api/admin/...` from the command line. The real check is the server's, on every call (Chapter 16). Guards exist so that honest users don't land on screens that would only show errors.

### 23.3 Forms and client-side validation (and why it's not security)

A **form** collects typed input. The project uses Angular's template-driven forms: importing `FormsModule` lets you tie an input to a class field with `[(ngModel)]`, which keeps them in sync in both directions.

**Listing 23.3 — `login.component.html` (book-m6-final, excerpt: the form)**

```html
  <form (ngSubmit)="submit()">
    <label for="username">Username</label>
    <input
      id="username"
      name="username"
      type="text"
      [(ngModel)]="username"
      autocomplete="username"
      autocapitalize="none"
      spellcheck="false"
      required
    />
    <!-- password input, error message ... -->
    <button type="submit" [disabled]="submitting() || !username.trim() || !password">
      {{ submitting() ? 'Signing in…' : 'Sign in' }}
    </button>
  </form>
```

*Path: `frontend/src/app/features/auth/login.component.html`*

(Simplified: the password field, which is the same shape with `type="password"` and `autocomplete="current-password"`, and the error paragraph are omitted.)

- `(ngSubmit)="submit()"` runs the method when the form is submitted, whether by button or by pressing Enter.
- `[(ngModel)]="username"` reads and writes the class field `username`. `name` is required by `ngModel` inside a form.
- `autocomplete="username"` lets password managers recognize the field; `autocapitalize="none"` and `spellcheck="false"` stop phones from "correcting" a username.
- `[disabled]="..."` disables the button while a request is in flight or a field is empty, preventing double submissions.

On success, the component uses a `returnUrl` from the query string, but only after checking it:

**Listing 23.4 — `login.component.ts` (book-m6-final, excerpt)**

```typescript
/** Only ever navigate within the app — a crafted ?returnUrl=//evil.example must not redirect off-site. */
function safeReturnUrl(returnUrl: string | null): string {
  return returnUrl && returnUrl.startsWith('/') && !returnUrl.startsWith('//') ? returnUrl : '/documents';
}
```

*Path: `frontend/src/app/features/auth/login.component.ts`*

This prevents an **open redirect**: an attacker could send a victim a link to the real sign-in page with `returnUrl` pointing at a lookalike site, and after signing in the victim would land on it. A path starting with `//` is treated by browsers as "another host", so it is refused along with anything not starting with `/`.

Client-side validation such as `required`, the 12-character minimum on the password form (`account.component.ts`), the PDF-only and 50 MB checks in `upload.component.ts`, is for the reader's benefit: fast feedback without a round trip. **None of it is security**, because anyone can bypass the browser and send requests directly. The upload component says so in its comment: the size constant "mirrors the server limit ... the server still enforces it". Chapter 13 covered the server-side validation that does the real work.

> **Note:** The viewer's page-number form uses `novalidate` on purpose: the component's `goToPage` method does all validation, so every bad value (blank, decimal, out of range) produces the same inline message, "Enter a whole number from 1 to N."

## Advanced tier: Deep links, resuming and keyboard

*On a first read you can skip to "In this project"; Part IV comes back to this.*

### 23.4 Deep links and query parameters (page numbers)

A **deep link** is an address that leads straight to a specific state, for example the viewer at page 3 of a document. Query parameters make it possible: `/viewer/<id>?page=3`. At `book-m4-reading` the viewer gained this (commit "Phase 4a: page deep links, keyboard navigation, resume reading"). The rule, from the comment on `initialPage`: `?page=N` (1-based) wins; otherwise resume where this user left off; otherwise page 1.

**Listing 23.5 — `viewer.component.ts` (book-m6-final, excerpt: `initialPage` and `rememberPage`)**

```typescript
  private initialPage(pageCount: number): number {
    const requested = Number(this.route.snapshot.queryParamMap.get('page'));
    if (Number.isInteger(requested) && requested >= 1 && requested <= pageCount) {
      return requested - 1;
    }
    try {
      const saved = Number(localStorage.getItem(this.lastPageKey()));
      return Number.isInteger(saved) && saved >= 0 && saved < pageCount ? saved : 0;
    } catch {
      return 0;
    }
  }

  /** Keeps the URL shareable (?page=N) and remembers the page for next time. */
  private rememberPage(page: number): void {
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { page: page + 1 },
      queryParamsHandling: 'merge',
      replaceUrl: true,
    });
    try {
      localStorage.setItem(this.lastPageKey(), String(page));
    } catch {
      // Storage unavailable (private mode, quota); resuming is just a convenience.
    }
  }
```

*Path: `frontend/src/app/features/viewer/viewer.component.ts`*

Notes on the details:

- The page in the URL is 1-based (people say "page 1"), while the code counts from 0, hence `requested - 1` and `page + 1`.
- The URL value is user-controlled input. It is checked (an integer within range) before it is used; a bad value such as `?page=99` falls back to the remembered page, which a spec confirms (`viewer.component.spec.ts`).
- `replaceUrl: true` updates the address bar without adding a history entry for every page turn, so the Back button leaves the viewer instead of stepping through pages.
- `localStorage` remembers the last page per user and document (key `sdv.lastPage.<username>.<documentId>`). The try/catch exists because browsers can disable storage; in that case the feature quietly doesn't work. It stores a page number, nothing sensitive.

### 23.5 Keyboard navigation

Readers expect arrows and Page Down to turn pages. The viewer listens for keys on the whole document:

**Listing 23.6 — `viewer.component.ts` (book-m6-final, excerpt: `onKeydown`)**

```typescript
  @HostListener('document:keydown', ['$event'])
  onKeydown(event: KeyboardEvent): void {
    const target = event.target as HTMLElement | null;
    if (event.ctrlKey || event.metaKey || event.altKey || !this.manifest() || this.accessLost()
        || (target && /^(INPUT|TEXTAREA|SELECT)$/.test(target.tagName)) || target?.isContentEditable) {
      return;
    }
    const last = this.manifest()!.pageCount - 1;
    const actions: Record<string, () => void> = {
      ArrowRight: () => this.nextPage(),
      PageDown: () => this.nextPage(),
      ArrowLeft: () => this.prevPage(),
      PageUp: () => this.prevPage(),
      Home: () => this.currentPage() !== 0 && this.loadPage(0),
      End: () => this.currentPage() !== last && this.loadPage(last),
      '+': () => this.zoomIn(),
      '=': () => this.zoomIn(),
      '-': () => this.zoomOut(),
    };
    const action = actions[event.key];
    if (action) {
      event.preventDefault();
      action();
    }
  }
```

*Path: `frontend/src/app/features/viewer/viewer.component.ts`*

`@HostListener('document:keydown', ...)` attaches a listener for key presses anywhere on the page. The first `if` lists when to do nothing: with Ctrl, Meta or Alt held (so browser shortcuts such as Ctrl+Plus still work), while the page-number field or another text box has focus (so typing "12" doesn't turn pages), or when there is no document. Otherwise the pressed key is looked up in a `Record` of actions (Chapter 19, Section 19.8). Every page turn goes through the same `loadPage` method as the Prev/Next buttons, which is a security point stated in the code: "there is no shortcut route to a page", so signed URLs, watermarking and the rate limit apply identically.

Touch users get the same via swipe: a horizontal movement of at least 50 pixels that is mostly sideways turns the page, and it is ignored when zoomed in, where a swipe pans instead (`onTouchStart`, `onTouchEnd`; added at `book-m5-platform`).

## In this project

| File | First appears | What it does |
|---|---|---|
| `frontend/src/app/app.routes.ts` | book-m1-accounts | URL to component map, lazy loading, guards |
| `frontend/src/app/core/auth.guard.ts` | book-m1-accounts (password-change redirect at book-m5-platform) | `authGuard`, `roleGuard` |
| `frontend/src/app/features/auth/login.component.*` | book-m1-accounts | Sign-in form, `safeReturnUrl` |
| `frontend/src/app/features/auth/account.component.ts` | book-m1-accounts | Change-password form |
| `frontend/src/app/features/viewer/viewer.component.ts` | book-m1-accounts (deep links and keys at book-m4-reading, swipe at book-m5-platform) | Page jump, deep links, keyboard, swipe |

See one with `git show book-m4-reading:frontend/src/app/features/viewer/viewer.component.ts`.

## Try it

1. ★ Which routes require the `ADMIN` role? Which only require being signed in?
2. ★ What URL does a signed-out visitor to `/documents` get redirected to?
3. ★★ Explain why deleting `authGuard` from a route would not let a reader see a document they aren't allowed to see.
4. ★★ Add a route `about` (lazy-loaded, no guard) for a scratch component that shows a paragraph.
5. ★★★ The viewer ignores key presses when the target is an input. Describe what would go wrong for someone typing a page number if it didn't.

Solutions are in `23-routing-guards-forms.solutions.md`.

## Summary

- The router maps URL paths to components and swaps them without reloading; lazy loading downloads a route's code on first visit.
- Guards run before a route is entered and redirect when the reader isn't signed in or lacks the role; they are a courtesy, not a defense.
- Forms tie inputs to fields with `ngModel`; client-side checks give fast feedback but never replace server validation.
- `returnUrl` is validated to prevent open redirects.
- `?page=N` is user input: validate it, keep it 1-based, and fall back gracefully.
- Keyboard handling steps aside for modifier keys and text fields, and every route to a page passes through the same server-checked path.

Next, Chapter 24 shows how all of this is tested.

## Further reading

- Angular documentation, "Routing", "Route guards" and "Template-driven forms": https://angular.dev/
- OWASP, "Unvalidated Redirects and Forwards Cheat Sheet": https://cheatsheetseries.owasp.org/
- MDN Web Docs, "KeyboardEvent" and "Window: localStorage": https://developer.mozilla.org/
