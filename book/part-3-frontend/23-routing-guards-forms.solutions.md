<!-- chapter: 23 | part: III | owner: writer-frontend | solutions -->
# Chapter 23 solutions

1. `admin` uses `roleGuard('ADMIN')`. `documents/upload` uses `roleGuard('PUBLISHER', 'ADMIN')`. Signed-in only (`authGuard`): `documents`, `documents/:documentId/manage`, `viewer/:documentId`, and `account`. `login` has no guard.

2. `/login?returnUrl=%2Fdocuments` (the guard passes the attempted URL, percent-encoded, in `returnUrl`). `auth.guard.spec.ts` checks the same behavior with `/viewer/abc?page=3`.

3. The guard only decides which screen the browser shows. The document's tiles come from the API, which checks on every request that the signed-in user may see the document (the viewer shows "no longer available" on a 404). Removing the guard would let a signed-out visitor see the viewer's empty shell, but the API would still refuse them.

4. Example:

   ```typescript
   {
     path: 'about',
     loadComponent: () => import('./features/about/about.component').then((m) => m.AboutComponent),
   },
   ```

   placed before the catch-all `**` route (routes match in order).

5. Typing "1" or "-" in the page box would also trigger the key actions: "-" would zoom out, and arrow keys, meant to move the text cursor, would turn pages. Worse, `preventDefault()` would block the character from being typed at all.
