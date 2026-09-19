<!-- chapter: 19 | part: III | owner: writer-frontend | solutions -->
# Chapter 19 solutions

1. In `DocumentDetail`, `sharedWith: string[] | null` is `null` for anyone who can't manage the document (the comment: "Only present when canManage is true"). It is the only nullable property in that interface. (`DocumentSummary` has the matching `sharedWithCount: number | null`.)

2. Example solution:

   ```typescript
   type Direction = 'next' | 'previous';

   function step(current: number, d: Direction): number {
     return d === 'next' ? current + 1 : current - 1;
   }
   ```

   `step(3, 'back')` is a compile error.

3. In `app.ts`, `checkIdle` stores the state and only tests for `'expired'`, and `app.html` tests for `'warning'`, so neither breaks by itself; the new alternative simply falls through as "not a warning". The compiler complains only where code exhaustively depends on the list, and there is none here. The lesson: unions catch wrong values, not missing handling, unless you write a `switch` with a `never` check. Verify by running `npx tsc --noEmit -p tsconfig.app.json` in a scratch copy.

4. Example solution:

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

5. Example solution:

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
