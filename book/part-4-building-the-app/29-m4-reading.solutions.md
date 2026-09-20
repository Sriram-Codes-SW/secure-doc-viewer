<!-- chapter: 29 | part: IV | owner: writer-app | tag: book-m4-reading | status: expanded -->
# Solutions for Chapter 29

### Exercise 29.1 ★ Idle state

With 10 minutes (600 s) left of a 1,800 s timeout, `idleState` returns `{ kind: 'active' }`, because the warning window is
`min(300, 1800 / 2) = 300` seconds and 600 is more than 300. With 4 minutes (240 s) left it returns
`{ kind: 'warning', secondsLeft: 240 }`.

### Exercise 29.2 ★ Keys while typing

Arrow keys move the text cursor inside an input. If the viewer also turned pages, you couldn't edit the number you were typing. `onKeydown` returns early when the event target is an `INPUT`, `TEXTAREA` or `SELECT`, when it is editable content, and when Ctrl, Cmd or Alt is held.

### Exercise 29.3 ★★ Which page opens?

Page 7 (the stored zero-based 6 is page 7 when counting from 1). `?page=25` is invalid for a 20-page document, so `initialPage` ignores it and falls back to storage. If the address has no `page` and storage is empty, `Number(null)` is 0 for the first read, which fails the `>= 1` test. The stored read of `null` also gives 0, which is a valid index, so page 1 opens.

### Exercise 29.4 ★★ Spacing arithmetic

`gap = round(20 * 2.0) = 40`. `stepX = blockWidth + gap = 120 + 40 = 160`. With two lines, `stepY = lineHeight * 2 + gap / 2 = 40 + 20 = 60`.

### Exercise 29.5 ★★ Clamped spacing

The constructor computes `Math.max(0.5, Math.min(6.0, spacing))`. For 20, `Math.min(6.0, 20)` is 6.0 and `Math.max(0.5, 6.0)` is 6.0. A bad setting can't make the mark absurdly sparse, and 0.5 is the floor at the dense end.

### Exercise 29.6 ★★★ Design a trace lookup

One good answer. `trim()` removes the outer spaces, giving `abc 123`; `toUpperCase()` gives `ABC 123`; `replaceAll("[^0-9A-Z]", "")` removes the inner space, giving `ABC123`. The query is `session_handle like ?` with the argument `ABC123%`, so it matches every audit row whose session handle starts with those six characters, that is, that session's events. Without the normalization, a `%` or `_` typed or pasted into the box would act as a wildcard in the `LIKE` pattern (`%` would match every row), turning a precise lookup into a broad scan; and uppercase and stray spaces would cause false misses. The parameter placeholder `?` already prevents SQL injection, so this is a separate protection.
