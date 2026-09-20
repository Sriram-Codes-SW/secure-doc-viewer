<!-- chapter: 27 | part: IV | owner: writer-app | tag: book-m2-documents | status: expanded -->
# Solutions for Chapter 27

### Exercise 27.1 ★ Why 404

A 403 would confirm that a document with that id exists. With 404, an outsider can't tell a hidden document from one that was never there, and the response is the only thing an outsider sees, so it is the whole disclosure (Section 27.2).

### Exercise 27.2 ★ Three ways to see a document

(1) `d.visibility = :everyone`, the document is open to everyone signed in; (2) `d.owner.username = :username`, the caller is the owner; (3) `:username in (select s.username from d.sharedWith s)`, the caller is among the users it is shared with. Admins skip this query and use `findTitle` in `DocumentService.titleIfViewable`.

### Exercise 27.3 ★★ Read the rows

- `pub.one` (owner): passes; rename allowed.
- `reader.one` (shared with): passes; rename returns 403 (`ForbiddenException`, and an `ACCESS_DENIED` event with detail `manage`).
- `outsider.one`: fails the check (not everyone, not owner, not in the shares); rename returns 404, because `requireViewable` refuses first (and records `ACCESS_DENIED` with detail `view`).
- Admin: passes through `findTitle` (no visibility test); rename allowed.

### Exercise 27.4 ★★ Why REQUIRES_NEW

By default Spring runs the request's database work in one transaction. In `requireViewable` the code writes the `ACCESS_DENIED` row and then throws `DocumentNotFoundException`. The exception makes Spring roll back the transaction, and the audit row, being part of it, is rolled back too, so the denial is never stored. With `REQUIRES_NEW` the insert runs in a separate transaction that commits when `record` returns, before the exception is thrown, so the caller's rollback can't touch it. The ordering matters: `audit.record(...)` is called before the `throw`.

### Exercise 27.5 ★★ CSV injection

`csv("=1+1")` returns `"'=1+1"` (with the surrounding double quotes): the first character `=` triggers the single-quote prefix, so the spreadsheet shows text instead of computing 2. `csv("Report, final")` returns `"Report, final"`: the value is wrapped in quotes, so the comma is part of the field and doesn't split the row. (`csv("")` and `csv(null)` return an empty string.)

### Exercise 27.6 ★★★ Unshare while reading

One good answer: the next tile request returns 404. The URL is still validly signed and unexpired, but `findTitleIfVisible` finds no share, so the access gate refuses. The reader's already-loaded tiles remain on screen because they are images in the browser. To make the page disappear too, the viewer would need to notice the 404 and discard the tiles and show a message; the design in Chapter 30 adds exactly an "access lost" screen for this. The server can't take back pixels it has already sent, which is why the watermark, the short URL lifetime and the rate limit matter as well.
