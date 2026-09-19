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

A **browser** runs one programming language natively: **JavaScript**. Every interactive web page, including the Secure Document Viewer's, ultimately runs JavaScript. JavaScript lets you write `total + 1` without ever saying whether `total` holds a number, some text, or nothing at all, and it finds out only when the line runs, in front of a user.

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

Each alternative carries a `kind` label. Only the `warning` alternative has `secondsLeft`. When code checks `state.kind === 'warning'`, the compiler knows that inside that branch `state.secondsLeft` exists, and outside it, doesn't. This pattern is a **discriminated union**, and it turns "which fields are valid right now?" from a comment into something the compiler enforces. The screen that shows the countdown makes exactly this kind of check on `state.kind` before it reads `state.secondsLeft`. Angular's template syntax, which that screen is written in, is the subject of Chapter 21; for now, only notice the check.

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

`IDLE_WARNING_SECONDS` is a constant defined a few lines above the function in the same file (`export const IDLE_WARNING_SECONDS = 5 * 60;`, so 300 seconds), left out of this excerpt. The function takes three numbers, returns an `IdleState`, and has no side effects (it changes nothing outside itself and reads nothing but its inputs): given the same three numbers it always gives the same answer. The compiler checks every `return` against `IdleState`; returning `{ kind: 'warnng' }` would be rejected. Notice `{ kind: 'warning', secondsLeft }`: when a variable has the same name as the property, TypeScript lets you write it once (`secondsLeft` instead of `secondsLeft: secondsLeft`).

Small functions are often written as **arrow functions**, `(x) => expression`, which are the same idea as Java lambdas (Chapter 5). `this.tiles().filter((t) => t.status === 'loaded')` passes an arrow function that answers "is this tile loaded?" for each element.

A **module** is a file that lists what it shares with `export` and pulls in what it needs with `import`. That's how `viewer.component.ts` uses code from other files:

**Listing 19.3a — `viewer.component.ts` (book-m6-final, excerpt: two of its import lines, not adjacent in the file)**

```typescript
import { API_BASE_URL } from '../../core/config';
import { buildTileViewModels, TileViewModel } from './tile-view-model';
```

The path after `from` is where the code lives: `./` is "this folder" and `../` is "one folder up". Modules replace Java's `package` and `import` pair with a single mechanism where the file path is the address.

### 19.4 `async`, promises and observables (only what the app uses)

A browser must never freeze while it waits for the network. So operations that take time don't return their result; they return a stand-in for it. TypeScript has two stand-ins that matter here.

A **Promise** stands for one result that will arrive later, or fail. An `async` function returns a promise, and inside it the keyword `await` pauses that function (not the browser) until the promise settles. Think of ordering at a coffee counter: you get a buzzer immediately, keep talking with friends, and collect the drink when the buzzer goes off.

**Where the analogy breaks down:** a buzzer goes off once, and so does a promise. But a promise can also end in failure, and a real buzzer has no such state. In code, a failed `await` throws an exception, which you catch with the `try`/`catch` you know from Java (Chapter 5).

An **Observable** stands for a *stream* of results over time: zero, one, or many values. It comes from a library called RxJS, which Angular's `HttpClient` uses (Chapter 22). You start it by calling `.subscribe(...)` with functions for "a value arrived" and "it failed". The project's rule of thumb: Angular's `HttpClient` gives Observables; the browser's `fetch()` gives Promises. You see the Promise side in `viewer.component.ts`:

**Listing 19.4 — `viewer.component.ts` (book-m6-final, excerpt: fetching one tile inside `fetchPendingTiles`)**

```typescript
        let response: Response;
        try {
          response = await fetch(tile.url, { signal, cache: 'no-store' });
        } catch {
          if (signal.aborted) {
            return;
          }
          this.updateTile(tile.key, { status: 'failed' });
          continue;
        }
```

*Path: `frontend/src/app/features/viewer/viewer.component.ts`*

`fetch(...)` returns a promise of a `Response`. `await` waits for it. If the network fails, the promise is rejected and the `catch` block runs. `signal` is an `AbortSignal`, which lets the viewer cancel every in-flight request at once when the reader turns the page, so a cancelled fetch is not treated as an error. The annotation `let response: Response` is needed because the value is assigned inside `try`.

Several `await` loops can also run side by side. The viewer starts a fixed number of "workers" and waits for all of them:

```typescript
await Promise.all(Array.from({ length: MAX_CONCURRENT_TILE_FETCHES }, () => worker()));
```

`Array.from({ length: 6 }, () => worker())` creates six calls to `worker()`, each returning a promise (the constant is 6). `Promise.all` gives back one promise that settles when all six are done. Chapter 22 explains why a small pool.

Where a function can only pass a value on later, callers can convert. `session.service.ts` uses `firstValueFrom(...)` to turn an HTTP Observable into a Promise, because Angular's startup hook (`provideAppInitializer`, Chapter 22) waits for a promise.

### 19.5 Reading TypeScript errors

The compiler's messages look intimidating, but they follow a pattern: a code (such as `TS2322`), a sentence saying what it found versus what it expected, and often extra lines drilling into why. Read the first line and the last line; the middle is detail.

**Example 19.2 — A typical error and how to read it**

```text
error TS2322: Type '"PUBLIC"' is not assignable to type 'Visibility'.
```

Read it as: "you gave me `'PUBLIC'`, but this place accepts only a `Visibility`, and `'PUBLIC'` isn't one." The fix is on the line the error names: either the value is a typo, or the union (Listing 19.1) needs a new alternative, which then makes the compiler point at every place that must handle it.

The project turns on a few extra checks in `tsconfig.json` (Chapter 20, Section 20.5). Two are worth knowing now: `noImplicitReturns` (a function whose branches sometimes return a value and sometimes don't is an error) and `noFallthroughCasesInSwitch` (a `switch` case that runs into the next one is an error).

> **Note:** This project's `tsconfig.json` does not set `"strict": true`, the umbrella switch many TypeScript projects use. It sets individual checks instead. Don't assume every strictness check is on when you add code.

### 19.6 Types that mirror the API

The backend (Part II) sends JSON. The frontend describes what it expects with interfaces, one per response shape, kept in `*.models.ts` files. `DocumentDetail` in `document.models.ts`, for example, lists the fields the viewer needs: the page list, the `tileVersion`, and `canManage`.

Here is the backend's side of the same shape, the Java record from Part II:

**Listing 19.3b — `DocumentSummary.java` (book-m6-final)**

```java
public record DocumentSummary(
        String documentId,
        String title,
        int pageCount,
        String owner,
        Visibility visibility,
        long createdAtEpochSeconds,
        long updatedAtEpochSeconds,
        boolean canManage,
        Integer sharedWithCount
) {
}
```

*Path: `src/main/java/com/example/securedocviewer/document/DocumentSummary.java`*

(The file also has a package line and a documentation comment, omitted here.) Compare it with Listing 19.1 line by line: `String` becomes `string`, `int` and `long` both become `number` (JavaScript has one number type), `boolean` stays, the Java enum `Visibility` becomes a union of its constant names, and `Integer sharedWithCount`, which can be `null` in Java, becomes `number | null`. The names are identical because Jackson, the backend's JSON library, uses the record's field names as JSON keys.

Two rules keep these honest:

- **Field names and types must match the backend's response exactly.** `createdAtEpochSeconds: number` is a number of seconds since 1970 because that is what the backend sends; the frontend converts it for display (`new Date(epochSeconds * 1000)` in `document-list.component.ts`). When the backend changes a field, the interface must change in the same commit.
- **Nullable on the wire means `| null` in the type.** `sharedWithCount: number | null` exists because the count is only present when the reader can manage the document.

Now the limit promised in Section 19.1: these interfaces are a promise, not a check. `this.http.get<DocumentSummary[]>(this.base)` tells the compiler what to expect; nothing verifies the actual JSON at run time. If the backend sent a different shape, the compiler would stay silent and the page would break in front of a user. That is why the project's tests (Chapter 24) feed realistic JSON to the components, and why the server, not the frontend, is the authority on what is allowed (Chapter 23).

## Intermediate tier: Generics and small type tools in real code

*On a first read you can skip to "In this project"; Part IV comes back to this.*

### 19.7 Generics: one function, many types

You met generics in Java (`List<String>`, Chapter 5). TypeScript's are the same idea. `HttpClient.get<T>` returns `Observable<T>`, and `signal<T>` (Chapter 21) holds a `T`. The project also defines one. In `manage.component.ts`, several server actions need the same busy flag and error notice, so one helper handles them:

**Listing 19.5 — `manage.component.ts` (book-m6-final, excerpt: method `run`)**

```typescript
  private run<T>(request: Observable<T>, onSuccess: (value: T) => void): void {
    this.busy.set(true);
    this.notice.set(null);
    request.subscribe({
      next: (value) => {
        this.busy.set(false);
        onSuccess(value);
      },
      error: (err: HttpErrorResponse) => {
        this.busy.set(false);
        this.confirmingDelete.set(false);
        this.notice.set({ kind: 'error', text: err.error?.error ?? 'Something went wrong.' });
      },
    });
  }
```

*Path: `frontend/src/app/features/documents/manage.component.ts`*

`<T>` is a placeholder filled in at each call: for `unshare` the request is an `Observable<string[]>`, so `T` is `string[]` and `onSuccess` must accept a `string[]`; for `replaceFile` it's `DocumentDetail`. The compiler checks each call separately.

### 19.8 Small type tools the app uses

- `Partial<TileState>` means "an object with any subset of `TileState`'s properties". `updateTile(key, patch)` in the viewer uses it so callers can change just `{ status: 'failed' }`.
- `Record<string, () => void>` means "an object whose keys are strings and whose values are functions taking nothing". The viewer's keyboard handler (Chapter 23) uses it to map key names to actions.
- `as const` on the audit event list in `admin.models.ts` freezes an array into exact literal types, and `(typeof AUDIT_EVENT_TYPES)[number]` derives a union of its entries, so the list and the type can't drift apart.
- `interface TileState extends TileViewModel` adds two properties (`src`, `status`) to an existing shape, as Java's `extends` does.

## Advanced tier: What types can't prevent

*On a first read you can skip to "In this project"; Part IV comes back to this.*

### 19.9 Stale answers: guarding async code with a generation counter

Types can't prevent a whole class of bugs that async code invites: an answer arriving after it has stopped being relevant. A reader flips from page 3 to page 4 while page 3's tiles are still downloading. If page 3's late responses were allowed to write into the page state, page 4 would show page 3's pixels.

The viewer prevents this with a plain counter, `loadGeneration`, incremented on every page change. Each fetch remembers the value it started with and drops its result if the value has moved on (`if (generation !== this.loadGeneration) { return; }`). It also aborts the old requests through the `AbortController` and revokes the temporary blob URLs of old tiles so memory isn't leaked (`resetPageState`). The code comments in `viewer.component.ts` give the reason: outstanding requests would otherwise keep spending the reader's rate-limit budget (Chapter 22). Nothing here is enforced by the compiler; it takes discipline, and `viewer.component.spec.ts` (Chapter 24) checks the visible consequences.

### 19.10 Why `fetch()` for tiles and not `HttpClient` or plain image URLs

The obvious alternatives are to let the browser load tiles itself from an image URL, or to use `HttpClient` like every other call. The comment above `MAX_CONCURRENT_TILE_FETCHES` gives the project's reason: only `fetch()` exposes the response status, and a throttled (429) or expired (401) tile is otherwise indistinguishable from a blank one, so the page silently renders with holes. Chapter 22 shows the outcome handling; the point here is that these language choices (Promise-based `fetch`, `AbortSignal`) follow from what information the code needs.

## In this project

| File | First appears | What it shows |
|---|---|---|
| `frontend/src/app/features/documents/document.models.ts` | book-m1-accounts | Interfaces and unions mirroring the API |
| `frontend/src/app/core/idle.ts` | book-m4-reading | Discriminated union and a pure function |
| `frontend/src/app/core/session.service.ts` | book-m1-accounts | `Role` union, `Observable`, `firstValueFrom` |
| `frontend/src/app/features/viewer/viewer.component.ts` | book-m1-accounts | `async`/`await`, `fetch`, `Promise.all`, `AbortController` |
| `frontend/src/app/features/documents/manage.component.ts` | book-m2-documents | The generic helper `run<T>` |
| `frontend/src/app/features/admin/admin.models.ts` | book-m1-accounts | `as const` and derived union types |

See any of them at a tag with `git show book-m6-final:frontend/src/app/core/idle.ts`. The viewer grew over the milestones: its fetch loop and `AbortController` exist at book-m1-accounts, while page memory, swipe, and replaced-document handling arrive in later tags.

## Try it

### Exercise 19.1 ★ The nullable property

In `document.models.ts`, which property of `DocumentDetail` can be `null`, and for whom?

### Exercise 19.2 ★ A Direction type

Write a type `Direction` that allows only `'next'` or `'previous'`, and a function `step(current: number, d: Direction): number`.

### Exercise 19.3 ★★ Extend IdleState

In a scratch copy, add a fourth alternative `{ kind: 'paused' }` to `IdleState`. Does the compiler complain anywhere? Why or why not?

### Exercise 19.4 ★★ A fetchOrNull helper

Rewrite the `try`/`catch` in Listing 19.4 as a helper `fetchOrNull(url, signal): Promise<Response | null>`. How can a caller tell an abort from a network failure?

### Exercise 19.5 ★★★ A generic firstWhere

Write a generic function `firstWhere<T>(items: T[], test: (item: T) => boolean): T | null` and use it to find the first loaded tile.


Solutions are in `19-typescript.solutions.md`.

## Summary

- TypeScript is JavaScript plus labels that a compiler checks and then erases; the browser runs plain JavaScript.
- Interfaces describe shapes, and unions restrict a value to a list of alternatives, which the app uses for roles, visibility and idle states.
- Discriminated unions let the compiler track which fields exist in which state.
- Promises and `await` handle a single delayed result, Observables handle streams; the app uses Observables for `HttpClient` and Promises for `fetch`.
- Generics such as `run<T>` share behavior across types while the compiler checks each use.
- Types mirror the API by agreement only; nothing checks JSON at run time.
- Async code needs discipline, such as the generation counter, that types can't provide.

Next, Chapter 20 introduces Node and npm, the tools that compile and run all of this.

## Further reading

- TypeScript Handbook, "Everyday Types" and "Narrowing": https://www.typescriptlang.org/docs/handbook/
- MDN Web Docs, "Using promises" and "Fetch API": https://developer.mozilla.org/
- RxJS documentation, "Observable": https://rxjs.dev/guide/observable
