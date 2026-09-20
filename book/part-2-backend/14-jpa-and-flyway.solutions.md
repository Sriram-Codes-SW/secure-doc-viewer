<!-- chapter: 14 | part: II | owner: writer-backend | tag: book-m2-documents | status: expanded -->
# Solutions: Chapter 14

### Exercise 14.1 ★ Why store the enum as text?

`@Enumerated(EnumType.STRING)` on the `role` field of `AppUser`. It stores the enum constant's *name* (`READER`, `PUBLISHER`, `ADMIN`). The default, `EnumType.ORDINAL`, stores the constant's *position*: 0, 1, 2. If you inserted a new role between `READER` and `PUBLISHER`, every stored `1` (formerly `PUBLISHER`) would now mean the new role, and every stored `2` would mean `PUBLISHER`, so existing accounts would silently change role, with no error. Text is also readable when you look at the table.

### Exercise 14.2 ★ Find the audit indexes

`V2__documents_shares_audit.sql` creates `audit_event` and four indexes: `ix_audit_event_time` on `occurred_at` (newest-first listing and date ranges), `ix_audit_event_user_time` on `(username, occurred_at)` (filter by user), `ix_audit_event_document_time` on `(document_id, occurred_at)` (filter by document) and `ix_audit_event_type_time` on `(event_type, occurred_at)` (filter by event type). Each pairs a filter column with the time, so a filtered list that is sorted by time can be read straight from the index.

### Exercise 14.3 ★★ Read a query method

`existsByUsername(String username)` becomes roughly `select ... from app_user where username = ? limit 1`, and the result is `true` if any row comes back. `findAllByOrderByUsernameAsc()` becomes `select ... from app_user order by username asc`, with no `where` because there is nothing between `By` and `OrderBy`. Another example: `countByOwner_Username` in `DocumentRepository` is `count` (the operation), `By` (conditions follow), `Owner_Username` (the `username` field of the `owner` relationship), that is `select count(*) from document d join app_user u on d.owner_id = u.id where u.username = ?`.

### Exercise 14.4 ★★ Annotation or template?

`UserAccountService` does quick database work in every method, so wrapping each whole method in a transaction with `@Transactional` is simple and correct. `DocumentService.upload` and `replaceFile` do a slow PDF render and file moves between the database steps. With `@Transactional` on the whole method, the transaction, and the database connection it holds, would stay open for the entire render, so a few simultaneous uploads could exhaust the connection pool. `TransactionTemplate` lets the service choose the transactional part precisely: render outside, then `tx.execute(...)` around the short database work. If you swapped them, the account service would work but with more code, and the document service would hold connections during rendering.

### Exercise 14.5 ★★★ Add a migration on a scratch copy

A worked outline. `V4__document_notes.sql` containing `ALTER TABLE document ADD COLUMN notes VARCHAR(500) NULL;`. On the first start, Flyway compares the migrations on disk with its history table, sees `V4` is new, applies it and records it with a checksum. After you edit `V4` and start again, Flyway recomputes the file's checksum, finds it differs from the recorded one, and refuses to start with a message that mentions a checksum mismatch for the applied migration. It is protecting every database that already ran the old version, which would otherwise disagree with this one. To make the change you wanted, restore `V4` to its original text (or, on a scratch database only, drop the database and start over), and put the new change in `V5`. On a real database you never repair by editing history.

### Exercise 14.6 ★★★ Predict the race

Without the lock, both requests render, then both read `tile_version = 1`, both compute the next version as 2, and both try to move their tiles into `v2`: they collide or mix files, and one update overwrites the other, so the document could end up pointing at tiles that are a mixture. With the row lock, `findByIdForUpdate` makes the second transaction wait at `select ... for update` until the first commits. The first sees version 1 and produces `v2`; the second then reads version 2 and produces `v3`. On disk, after the first replace removes `v1` and the second removes `v2`, only `v3` remains, and the row's `tile_version` is 3. `MySqlIntegrationTest.concurrentReplacementsAreSerialisedByTheRowLock` asserts exactly this. It needs a real MySQL because the test is about the database's own row-lock behavior, which the in-memory H2 imitation is not a trustworthy stand-in for (the class comment names "the row lock that serialises PDF replacement" among the things H2 can't vouch for).
