<!-- chapter: 21 | part: III | owner: writer-frontend | solutions -->
# Chapter 21 solutions

### Exercise 21.1 ★ The Admin link condition

`@if (sessionService.isAdmin()) { ... }`. It reads `sessionService.isAdmin`, a computed signal in `SessionService` that is true when the signed-in user's role is `ADMIN`.

### Exercise 21.2 ★ Change the accent colour

Every screen whose CSS uses `var(--accent)`: buttons, the active navigation underline, the Manage link in the viewer header, and the idle banner tint. Only the tokens changed, so no component file needs editing. (Remember the dark theme redefines `--accent` in its own block.)

### Exercise 21.3 ★★ Toggle a signal

Example:

   ```typescript
   readonly showHint = signal(false);
   ```

   ```html
   <button (click)="showHint.update((v) => !v)">Toggle hint</button>
   @if (showHint()) {
     <p>This is the hint.</p>
   }
   ```

### Exercise 21.4 ★★ Check colour contrast

Compute with any WCAG contrast checker. Light: `#5d6470` on `#ffffff` and on `#f6f7f9`; dark: `#9aa0aa` on `#191b1f` and on `#101114`. The comment in `styles.css` states the project's aim of at least 4.5:1 on `--bg` and `--surface`; check each pair yourself and record the ratios.

### Exercise 21.5 ★★★ Why track by id

Tracking by position would treat "the third card" as the same item before and after filtering, so Angular would reuse and rewrite existing cards' contents. Tracking by `documentId` gives every card a stable identity, so Angular keeps the cards that remain, removes the ones that disappear, and doesn't rebuild the rest.
