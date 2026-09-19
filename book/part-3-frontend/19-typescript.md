<!-- chapter: 19 | part: III | owner: writer-frontend | tag: book-m6-final | status: draft -->
# Chapter 19: TypeScript

Everything you see in the Secure Document Viewer's browser window is driven by TypeScript, the language the frontend is written in. In this chapter you learn enough of it to read every file under `frontend/src/app/`, starting from the small data shapes that mirror the backend's JSON and ending with the `async` code that fetches page tiles.

## Learning objectives

By the end of this chapter, you will be able to:

- Explain what TypeScript adds to JavaScript and why the project compiles it before the browser sees it.
- Write an `interface`, a union type, and a typed function, and read the ones in `document.models.ts` and `idle.ts`.
- Compare `Promise`, `async`/`await`, and `Observable`, and say which the app uses where.
- Read a TypeScript compiler error and find the line it is really about.
- Check that a frontend type matches the JSON the backend sends.

## Prerequisites

- Chapter 4: classes, objects, records and interfaces (Java's version of the ideas here).
- Chapter 5: collections, generics, lambdas and exceptions.
- Chapter 8: how the web works (a browser sends HTTP requests and receives JSON).

## Beginner tier: A language that checks your work

### 19.1 JavaScript and TypeScript: what each is

A **browser** runs one programming language natively: **JavaScript**. Every interactive web page, including this one, ultimately runs JavaScript. JavaScript lets you write `total + 1` without ever saying whether `total` holds a number, some text, or nothing at all, and it finds out only when the line runs, in front of a user.

**TypeScript** is JavaScript with a layer of labels added, called **types**. A type says what kind of value a name holds: `number`, `string`, or a shape you define. A program called the **compiler** reads your TypeScript before anything runs, checks that every use matches its label, and then removes the labels, producing plain JavaScript for the browser. The browser never sees a type.

Think of a form at a doctor's office with boxes marked "date of birth" and "phone number". The boxes don't make you honest, but the clerk can spot at once that you wrote a phone number in the date box. TypeScript is the clerk, and the compiler runs the check at your desk, before the form is sent.

**Where the analogy breaks down:** a clerk checks the form once, at the counter, and after that the paperwork is trusted. TypeScript checks only your own code. Data that arrives later from the network, such as the JSON from the backend, is not checked at all when the program runs, because the labels are gone by then. Section 19.6 returns to this.

If you know Java (Chapters 3–5), much of this will feel familiar. The syntax differs in one visible way: the type comes after the name, separated by a colon.

**Example 19.1 — Java and TypeScript side by side**

```java
int pageCount = 12;
```

```typescript
const pageCount: number = 12;
```

`const` means the name can't be pointed at a different value later. `let` is the version that can. The project uses `const` almost everywhere.

The project's TypeScript version is 6.0, pinned in `package.json` as `"typescript": "~6.0.2"` (the `~` is explained in Chapter 20, Section 20.6).

### 19.2 Types, interfaces, unions

The basic types are `string`, `number`, `boolean`, arrays written `string[]`, and the two "nothing" values `null` and `undefined`. An **interface** names a shape: which properties an object has and what type each one is. It is the closest thing to a Java record (Chapter 4), except that it exists only for the compiler.

**Listing 19.1 — `document.models.ts` (book-m6-final, excerpt: `PageInfo` and `DocumentSummary`)**

```typescript
export type Visibility = 'PRIVATE' | 'EVERYONE';

export interface PageInfo {
  page: number;
  rows: number;
  cols: number;
  tileSize: number;
  pageWidthPx: number;
  pageHeightPx: number;
}

/** A row in the document library. */
export interface DocumentSummary {
  documentId: string;
  title: string;
  pageCount: number;
  owner: string;
  visibility: Visibility;
  createdAtEpochSeconds: number;
  updatedAtEpochSeconds: number;
  canManage: boolean;
  /** Only present when canManage is true. */
  sharedWithCount: number | null;
}
```

*Path: `frontend/src/app/features/documents/document.models.ts`*

Reading it top to bottom:

- `export` makes the name available to other files (Section 19.3).
- `type Visibility = 'PRIVATE' | 'EVERYONE'` defines a **union type**: a value that must be one of the listed alternatives. Here the alternatives are two exact pieces of text. Writing `'PUBLIC'` where a `Visibility` is expected is a compile error, and so is a typo like `'PRIVAT'`. The `|` reads as "or".
- `interface PageInfo { ... }` lists six properties, all numbers.
- `sharedWithCount: number | null` is another union: a number, or `null`. The comment says when it's `null`. The compiler forces every piece of code that reads this property to handle both cases; you can see that in `accessLabel` in `document-list.component.ts`, which checks `doc.sharedWithCount === null` first.
- `/** ... */` is a documentation comment; editors show it when you hover over the name.

The same union idea appears for the three user roles: `export type Role = 'READER' | 'PUBLISHER' | 'ADMIN';` in `session.service.ts`. The backend has an enum with the same three constants (Chapter 15); on this side, a union of strings plays that part.

A union can also mix *shapes*. `idle.ts` describes the three states of the idle-timeout countdown (Chapter 22) this way:

**Listing 19.2 — `idle.ts` (book-m6-final, excerpt: the type)**

```typescript
export type IdleState =
  | { kind: 'active' }
  | { kind: 'warning'; secondsLeft: number }
  | { kind: 'expired' };
```

*Path: `frontend/src/app/core/idle.ts`*

Each alternative carries a `kind` label. Only the `warning` alternative has `secondsLeft`. When code checks `state.kind === 'warning'`, the compiler knows that inside that branch `state.secondsLeft` exists, and outside it, doesn't. This pattern is a **discriminated union**, and it turns "which fields are valid right now?" from a comment into something the compiler enforces. You can see it used in `app.html`: `@if (state.kind === 'warning') { ... {{ formatCountdown(state.secondsLeft) }} ... }`.

### 19.3 Functions, arrow functions, modules

A function declares typed parameters and, after the parameter list, a return type.

**Listing 19.3 — `idle.ts` (book-m6-final, excerpt: the function)**

```typescript
export function idleState(nowMs: number, lastActivityMs: number, timeoutSeconds: number): IdleState {
  if (timeoutSeconds <= 0) {
    return { kind: 'active' };
  }
  const secondsLeft = Math.ceil(timeoutSeconds - (nowMs - lastActivityMs) / 1000);
  if (secondsLeft <= 0) {
    return { kind: 'expired' };
  }
  return secondsLeft <= Math.min(IDLE_WARNING_SECONDS, timeoutSeconds / 2)
    ? { kind: 'warning', secondsLeft }
    : { kind: 'active' };
}
```

*Path: `frontend/src/app/core/idle.ts`*

The function takes three numbers, returns an `IdleState`, and has no side effects: given the same three numbers it always gives the same answer. The compiler checks every `return` against `IdleState`; returning `{ kind: 'warnng' }` would be rejected. Notice `{ kind: 'warning', secondsLeft }`: when a variable has the same name as the property, TypeScript lets you write it once (`secondsLeft` instead of `secondsLeft: secondsLeft`).

Small functions are often written as **arrow functions**, `(x) => expression`, which are the same idea as Java lambdas (Chapter 5). `this.tiles().filter((t) => t.status === 'loaded')` passes an arrow function that answers "is this tile loaded?" for each element.

A **module** is a file that lists what it shares with `export` and pulls in what it needs with `import`. That's how `viewer.component.ts` uses code from other files:

```typescript
import { API_BASE_URL } from '../../core/config';
import { buildTileViewModels, TileViewModel } from './tile-view-model';
```

The path after `from` is where the code lives: `./` is "this folder" and `../` is "one folder up". Modules replace Java's `package` and `import` pair with a single mechanism where the file path is the address.

<!-- WRITER NOTE: sections 19.4 (async/promises/observables), 19.5 (reading errors), the Intermediate and Advanced tiers, In this project, Try it, Summary, Further reading are still to write. See book/_team/progress/writer-frontend.md. -->
