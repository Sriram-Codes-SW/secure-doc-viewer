<!-- chapter: 21 | part: III | owner: writer-frontend | tag: book-m6-final | status: draft -->
# Chapter 21: Angular components and templates

Every screen of the Secure Document Viewer is an Angular component: the top bar, the sign-in form, the document list, and the page viewer. In this chapter you learn what a component is, how its template shows data and reacts to change, and how the app's stylesheet gives every screen a consistent look in light and dark themes.

## Learning objectives

By the end of this chapter, you will be able to:

- Explain the three parts of a component (class, template, style) and read `app.ts`.
- Write a template that shows a value, a condition, and a list, using `{{ }}`, `@if` and `@for`.
- Explain what a signal is, and use `signal`, `set`, `update` and `computed`.
- Read the theme tokens in `styles.css` and explain how dark mode and contrast are handled.
- Explain how the viewer paints page tiles without an `<img>` tag.

## Prerequisites

- Chapter 19: TypeScript (classes, types, arrow functions).
- Chapter 20: Node, npm and the Angular toolchain (`ng serve`, project layout).

## Beginner tier: A screen is a small machine with three parts

### 21.1 Components: a class, a template, a style

An **Angular** component is one reusable piece of screen. Think of a name badge holder at a conference: the plastic holder is always the same, and you slide a different card in. The component is the holder; the data it displays is the card.

**Where the analogy breaks down:** a badge holder is passive, and you swap the card by hand. A component watches its data and redraws itself when the data changes.

A component has three parts: a **class** (the data and actions, in TypeScript), a **template** (the HTML that shows them; HTML is the markup language that describes the structure of a web page, such as headings, buttons and links), and a **stylesheet** (CSS, the language that sets colors, spacing and layout, that decorates them). The top-level component of the app is short enough to read whole:

**Listing 21.1 — `app.ts` (book-m6-final, excerpt: imports and decorator)**

```typescript
import { LowerCasePipe } from '@angular/common';
import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { IdleState, idleState } from './core/idle';
import { SessionService } from './core/session.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, LowerCasePipe],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App implements OnInit, OnDestroy {
  readonly idle = signal<IdleState>({ kind: 'active' });
```

*Path: `frontend/src/app/app.ts`*

Line by line:

- `@Component({ ... })` is a **decorator**: a label attached to the class below it that tells Angular "this class is a component, configured like so".
- `selector: 'app-root'` is the custom HTML tag that shows this component. `src/index.html` contains `<app-root></app-root>`, and that is where the whole application appears.
- `standalone: true` means the component lists its own dependencies rather than relying on a shared module. (Angular 22 treats components as standalone by default; the project states it explicitly.)
- `imports: [...]` lists the other building blocks that this component's template uses: other components, pipes (formatters, Section 21.2), and directives (extra behavior attached to an element, such as `routerLink`, which turns an ordinary link into an in-app one). If the template uses `routerLink` but `RouterLink` isn't listed here, the compiler complains.
- `templateUrl` and `styleUrl` point to the files `app.html` and `app.css` beside it.
- `export class App implements OnInit, OnDestroy` defines the class. `OnInit` and `OnDestroy` are **lifecycle hooks**: methods (`ngOnInit`, `ngOnDestroy`) that Angular calls when the component appears and when it is removed. `App` starts a one-second timer in the first and stops it in the second.
- `readonly idle = signal<IdleState>(...)` is a piece of reactive data; Section 21.3 explains it.

The app starts in `main.ts` with `bootstrapApplication(App, appConfig)`, which creates the root component and hands it the app-wide settings (Chapter 22).

### 21.2 Templates: binding, conditionals, loops

A **template** is HTML with extra syntax that connects it to the class. Here is the top-level template:

**Listing 21.2 — `app.html` (book-m6-final)**

```html
<header class="topbar">
  <div class="brand">Secure Doc Viewer</div>

  @if (sessionService.user(); as user) {
    <nav>
      <a routerLink="/documents" routerLinkActive="active">Documents</a>
      @if (sessionService.isAdmin()) {
        <a routerLink="/admin" routerLinkActive="active">Admin</a>
      }
    </nav>
    <div class="account">
      <a class="account-link" routerLink="/account" routerLinkActive="active">
        {{ user.username }} <span class="role-badge">{{ user.role | lowercase }}</span>
      </a>
      <button (click)="logout()">Sign out</button>
    </div>
  }
</header>

@if (idle(); as state) {
  @if (state.kind === 'warning') {
    <div class="idle-banner" role="alert">
      You'll be signed out in {{ formatCountdown(state.secondsLeft) }} because of inactivity.
      <button (click)="staySignedIn()">Stay signed in</button>
    </div>
  }
}

<main>
  <router-outlet />
</main>
```

*Path: `frontend/src/app/app.html`*

The template syntax comes in a handful of forms:

- `{{ user.username }}` is **interpolation**: it inserts the value of an expression as text. Angular escapes it, so a username containing `<script>` appears as harmless text, not code.
- `{{ user.role | lowercase }}` pipes the value through a **pipe**, a small formatter. `lowercase` is why the imports list has `LowerCasePipe`.
- `@if (condition) { ... }` shows its block only when the condition is true. `@if (sessionService.user(); as user)` also names the value `user` for use inside the block. The nav bar therefore exists only when someone is signed in, and the Admin link only for administrators.
- `(click)="logout()"` is an **event binding**: when the button is clicked, call the class's `logout` method.
- `routerLink="/documents"` makes a link that changes pages without reloading the browser (Chapter 23), and `<router-outlet />` is the spot where the current page's component appears.
- The `role="alert"` attribute tells screen readers to announce the banner when it appears (Section 21.5).

Two more forms appear in other templates. `@for` repeats a block for each item, and needs a `track` expression so that Angular can tell items apart when the list changes:

**Listing 21.3 — `document-list.component.html` (book-m6-final, excerpt: the card grid)**

```html
  <div class="doc-grid">
    @for (doc of filtered(); track doc.documentId) {
      <article class="doc-card">
        <a class="doc-open" [routerLink]="['/viewer', doc.documentId]">
          <h2>{{ doc.title }}</h2>
```

*Path: `frontend/src/app/features/documents/document-list.component.html`*

`track doc.documentId` says each card is identified by its document id, so when the list is filtered, Angular keeps the cards that remain instead of rebuilding all of them. And square brackets, as in `[routerLink]="[...]"`, are a **property binding**: the attribute's value is computed from an expression instead of being fixed text. The viewer uses the same form to move tiles, for example `[style.top.px]="tile.top"` (Section 21.6) and `[class.pending]="tile.status !== 'loaded'"`.

## Intermediate tier: Signals and how a screen stays current

*On a first read you can skip to "In this project"; Part IV comes back to this.*

### 21.3 Reactivity: signals and change detection

If the class holds `count = 0` as a plain variable and later sets it to 1, how does the screen learn about it? Angular's answer, used throughout this project, is the **signal**: a box that holds a value and remembers who has read it. You read a signal by calling it like a function (`idle()`), and change it with `set` or `update`:

```typescript
readonly zoom = signal(1);          // create, starting at 1
this.zoom.set(1.4);                 // replace the value
this.zoom.update((z) => z + 0.2);   // compute the new value from the old
```

Because templates call the signal (`{{ zoom() }}`), Angular knows that this template depends on `zoom`, and redraws just that part when it changes. Deciding what to redraw is **change detection**. A **computed** signal is a value derived from others; it is recalculated only when a signal it read has changed, and is otherwise cached. The document list uses one for its search box:

**Listing 21.4 — `document-list.component.ts` (book-m6-final, excerpt)**

```typescript
  readonly documents = signal<DocumentSummary[]>([]);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly query = signal('');

  /** Client-side filter on title and owner; the server already returned only what this user may see. */
  readonly filtered = computed(() => {
    const q = this.query().trim().toLowerCase();
    return q
      ? this.documents().filter((d) => d.title.toLowerCase().includes(q) || d.owner.toLowerCase().includes(q))
      : this.documents();
  });
```

*Path: `frontend/src/app/features/documents/document-list.component.ts`*

Typing in the search box sets `query`, which invalidates `filtered`, which the `@for` in Listing 21.3 reads, so the grid updates. No code says "redraw the grid"; the dependencies do it. The viewer relies on this heavily: page number, zoom, tiles, the throttle countdown and error messages are all signals (`viewer.component.ts`), and its `stageStyle` is a `computed` that turns page size and zoom into the CSS the page needs.

> **Note:** Signals also drive who-is-signed-in state. `SessionService` (Chapter 22) exposes `user`, `isAdmin` and others as read-only or computed signals, which is why `app.html` can write `sessionService.isAdmin()` and the nav bar updates itself on sign-in and sign-out.

### 21.4 Component inputs and outputs

Angular components normally receive data through **inputs** and report events through **outputs**. This app doesn't need them: a search of `frontend/src/app` finds none. Each screen is a routed page that gets its data from services (Chapter 22) and its parameters from the URL (Chapter 23), so the components don't pass data to one another. We simplify here; Angular's documentation covers `input()` and `output()` if you build reusable widgets later.

### 21.5 Styling, light and dark themes, and accessibility basics

Component stylesheets are scoped: rules in `app.css` affect only the `App` component. Look-and-feel that every screen shares lives in one global file, `styles.css`, built on **CSS custom properties** (also called variables): named values, written `--name`, that any rule can read with `var(--name)`.

**Listing 21.5 — `styles.css` (book-m6-final, excerpt: the tokens)**

```css
:root {
  --bg: #f6f7f9;
  --surface: #ffffff;
  --fg: #16181d;
  /* Text colours meet WCAG AA (4.5:1) on both --bg and --surface. */
  --muted: #5d6470;
  --border: #e2e4e9;
  --accent: #2458d6;
  /* Text on an --accent background. */
  --on-accent: #ffffff;
  --danger: #c1272f;
}

@media (prefers-color-scheme: dark) {
  :root {
    --bg: #101114;
    --surface: #191b1f;
    --fg: #eceef1;
    --muted: #9aa0aa;
    --border: #2a2d33;
    --accent: #5b8dfd;
    --on-accent: #0b1020;
    --danger: #f0787e;
  }
}
```

*Path: `frontend/src/styles.css`*

`:root` selects the whole page, so these values are available everywhere. Components never write a color; they write `var(--surface)` or `var(--border)` (see `app.css`). The `@media (prefers-color-scheme: dark)` block applies only when the reader's operating system is set to dark mode, and redefines the same names, so one line of change swaps the whole theme. There is no switch in the app: it follows the system.

**Contrast** is the difference in brightness between text and its background. The Web Content Accessibility Guidelines (WCAG) level AA asks for a ratio of at least 4.5:1 for normal text, so people with low vision or a dim screen can read it. Two things at `book-m5-platform` show why tokens matter: `--muted` was darkened from `#6b7280` to `#5d6470` in light mode, and the button text color changed from a hard-coded white to `var(--on-accent)`, because white text on the lighter dark-mode accent (`#5b8dfd`) would not have enough contrast (the git diff of `styles.css` between `book-m4-reading` and `book-m5-platform` shows both). Chapter 24 shows the automated check that keeps this true.

Accessibility basics visible in the templates: form fields are paired with `<label for="...">`; the search box has `aria-label="Search documents"`; error text uses `role="alert"` and status messages `role="status"`, so screen readers announce them.

## Advanced tier: How the viewer paints a page

*On a first read you can skip to "In this project"; Part IV comes back to this.*

### 21.6 CSS backgrounds instead of images or a canvas

A PDF page is cut into rectangular tiles (Part II). The Angular viewer places each tile as an absolutely positioned `<div>` whose CSS background is the tile's picture:

**Listing 21.6 — `viewer.component.html` (book-m6-final, excerpt: the tiles)**

```html
      @for (tile of tiles(); track tile.key) {
        <div
          class="tile"
          [class.pending]="tile.status !== 'loaded'"
          [style.top.px]="tile.top"
          [style.left.px]="tile.left"
          [style.width.px]="tile.width + 1"
          [style.height.px]="tile.height + 1"
          [style.background-image]="tile.src ? 'url(' + tile.src + ')' : null"
        ></div>
      }
```

*Path: `frontend/src/app/features/viewer/viewer.component.html`*

`tile.src` is a temporary `blob:` address the viewer creates for the bytes it fetched (Chapter 22). Each `div` is one tile, positioned by `top` and `left` in page pixels. The milestone-zero prototype drew the page on a `<canvas>` element (`book-m0-mvp`'s static page; Chapter 25); the Angular viewer, from its first appearance at `book-m1-accounts`, uses divs. The stylesheet's comment states the design intent:

**Listing 21.7 — `viewer.component.css` (book-m6-final, excerpt)**

```css
/*
 * Deliberately no <img> tags: page content is painted as CSS
 * background-image on plain divs, and every layer disables selection and
 * native drag. This is the exact client-side technique discussed for
 * why flipbook readers don't offer "Save Image As" on right-click. It is
 * a UX nicety, not real protection — real protection is the short-lived
 * signed URL, per-request watermark, and rate limit behind each tile.
 */
.stage {
  position: relative;
  background: #fff;
  user-select: none;
  -webkit-user-select: none;
  -webkit-user-drag: none;
}
```

*Path: `frontend/src/app/features/viewer/viewer.component.css`*

Read it honestly: this stops a casual right-click "Save image as", nothing more. Anyone can still open the browser's developer tools. The protection that matters is on the server (Chapters 16 and 17).

A second detail from the same file shows how small rendering bugs are solved. The stage is scaled by a fractional amount for zoom, and that left hairline gaps between neighboring tiles. The template makes each tile one pixel wider and taller (`tile.width + 1`), so neighbors overlap by a pixel; the CSS comment above `.tile` explains why.

## In this project

| File | First appears | What it does |
|---|---|---|
| `frontend/src/app/app.ts`, `app.html`, `app.css` | book-m1-accounts (idle banner from book-m4-reading) | Root component: top bar, idle banner, router outlet |
| `frontend/src/styles.css` | book-m1-accounts (contrast tokens at book-m5-platform) | Global theme tokens, dark mode, buttons |
| `frontend/src/app/features/documents/document-list.component.*` | book-m2-documents | Signals, `computed` filter, `@for` |
| `frontend/src/app/features/viewer/viewer.component.*` | book-m1-accounts | Tiles as divs with CSS backgrounds |

View one with `git show book-m6-final:frontend/src/styles.css`.

## Try it

1. ★ In `app.html`, which condition decides whether the Admin link appears? Which class property does it read?
2. ★ Change the accent color in a scratch copy of `styles.css` and reload `ng serve`. Which screens change?
3. ★★ Add a signal `showHint` to a scratch component with a button that toggles it (`update((v) => !v)`) and an `@if` that shows a paragraph.
4. ★★ Use a contrast checker to compute the ratio of `--muted` on `--surface` in both themes. Does each pass 4.5:1?
5. ★★★ Explain why `track doc.documentId` is better than tracking by position when the list is filtered.

Solutions are in `21-angular-components.solutions.md`.

## Summary

- A component is a class, a template and a stylesheet, shown through its selector.
- Templates show values with `{{ }}`, decide with `@if`, repeat with `@for`, and respond with `(event)` bindings.
- Signals hold values, `computed` derives new ones, and Angular redraws only what read a changed signal.
- The app has no component inputs or outputs; screens get data from services and the URL.
- Theme colors are CSS custom properties, redefined under `prefers-color-scheme: dark`; contrast fixes are made in the tokens.
- Tiles are positioned divs with CSS backgrounds, a convenience layer, not protection.

Next, Chapter 22 connects these screens to the backend.

## Further reading

- Angular documentation, "Components", "Templates" and "Signals": https://angular.dev/
- MDN Web Docs, "Using CSS custom properties" and "prefers-color-scheme": https://developer.mozilla.org/
- W3C, "Web Content Accessibility Guidelines (WCAG) 2.1": https://www.w3.org/TR/WCAG21/
