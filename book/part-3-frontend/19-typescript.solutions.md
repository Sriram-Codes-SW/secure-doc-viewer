<!-- chapter: 19 | part: III | owner: writer-frontend | solutions -->
# Chapter 19 solutions

### Exercise 19.1 ★ The nullable property

In `DocumentDetail`, `sharedWith: string[] | null` is `null` for anyone who can't manage the document (the comment: "Only present when canManage is true"). It is the only nullable property in that interface. (`DocumentSummary` has the matching `sharedWithCount: number | null`.)

### Exercise 19.2 ★ A Direction type

Example solution:

```typescript
type Direction = 'next' | 'previous';

function step(current: number, d: Direction): number {
  return d === 'next' ? current + 1 : current - 1;
}
```

`step(3, 'back')` is a compile error.

### Exercise 19.3 ★★ Extend IdleState

In `app.ts`, `checkIdle` stores the state and only tests for `'expired'`, and `app.html` tests for `'warning'`, so neither breaks by itself; the new alternative falls through as "not a warning". The compiler complains only where code exhaustively depends on the list, and there is none here. The lesson: unions catch wrong values, not missing handling, unless you write a `switch` with a `never` check. Verify by running `npx tsc --noEmit -p tsconfig.app.json` in a scratch copy.

### Exercise 19.4 ★★ A fetchOrNull helper

Example solution:

```typescript
async function fetchOrNull(url: string, signal: AbortSignal): Promise<Response | null> {
  try {
    return await fetch(url, { signal, cache: 'no-store' });
  } catch {
    return null;
  }
}
```

An aborted request also gives `null`, so the caller must check `signal.aborted` to tell the two apart, as the viewer does.

### Exercise 19.5 ★★★ A generic firstWhere

Example solution:

```typescript
function firstWhere<T>(items: T[], test: (item: T) => boolean): T | null {
  for (const item of items) {
    if (test(item)) {
      return item;
    }
  }
  return null;
}

const firstLoaded = firstWhere(tiles, (t) => t.status === 'loaded');
```

### Exercise 19.6 ★★ Tile arithmetic

Columns: 600 ÷ 256 rounds up to 3. Rows: 800 ÷ 256 rounds up to 4. So there are 3 × 4 = 12 tiles. The `left` values are 0, 256 and 512; the `top` values are 0, 256, 512 and 768. Widths are 256, 256 and `min(256, 600 − 512)` = 88; heights are 256, 256, 256 and `min(256, 800 − 768)` = 32. The bottom-right tile (row 3, column 2 when counting from 0) is at left 512, top 768, and is 88 pixels wide and 32 pixels tall. Its key is `"3-2"`.
