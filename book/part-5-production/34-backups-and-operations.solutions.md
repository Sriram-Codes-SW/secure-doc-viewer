# Solutions: Chapter 34

### Exercise 34.1 ★ Name the state

- **Login sessions:** no. They live in the app's memory; after a restore, users sign in again.
- **Tiles:** yes (`app-storage` volume). They are the only copy of the content.
- **Audit log:** yes. It is in MySQL, so the dump covers it.
- **`.env` file:** yes, kept with the backup and protected like it. `SIGNING_SECRET` keys the recognised-device hashes, and the database passwords are needed to start MySQL against the old data.
- **Source PDFs:** nothing to back up; they are deleted after ingest, so the tiles hold the content.

### Exercise 34.2 ★ Read a command

`docker compose exec -T mysql` runs a command in the running `mysql` container; `-T` disables the pseudo-terminal so redirecting the output to a file doesn't mix in terminal control characters. `sh -c '...'` starts a shell inside the container. The single quotes stop the host shell from expanding `$MYSQL_ROOT_PASSWORD` and `$MYSQL_DATABASE`; the container's shell expands them from the container's own environment, so the password doesn't appear in your terminal or history. `exec mysqldump` replaces the shell with the dump program. `--single-transaction` reads a consistent snapshot of the InnoDB tables without long locks; `--routines` includes stored routines; `-u root -p"..."` gives the user and password; the last word is the database name. `> securedocs.sql` writes the dump to a file on the host.

### Exercise 34.3 ★★ Break the match

At 10:00 the dump records document D at version 1. At 10:01 the replace renders version 2, switches D to it, and deletes version 1's tiles. At 10:02 the archive holds only version 2. After a restore, the database says D uses version 1, but `D/v1/...` isn't on disk, so every tile request for D finds nothing and the reader sees blank pages or errors. The janitor's safety net notices that D's current version is missing, logs a warning, and refuses to prune D's other versions (here, version 2), so an operator can still recover the content, for example by pointing the document at version 2. It does not repair anything by itself. The runbook prevents the whole situation by stopping the app around both captures.

### Exercise 34.4 ★★ Read the janitor

(a) `notes` doesn't match the document-id pattern, so the janitor skips it and never deletes it. (b) A document-id-shaped folder with no matching document is an orphan, but it is only ten minutes old, which is inside `MIN_AGE` (one hour), so it is left alone. It may be a render in progress. (c) The same folder at two days old is an orphan and old enough, so `tryDelete` removes it (and counts it in the log line). If the delete fails because a file is locked, it logs a warning and retries at the next sweep, six hours later.

### Exercise 34.5 ★★★ Design the drill

A good answer names: a scratch environment (a separate compose project with its own folder name, ports, and volumes, never production); the steps (start MySQL, load the dump, unpack the tiles, start the app); proof (Flyway validates, a known account signs in, several documents open including one that was replaced, every tile loads, the newest audit event has the expected date); what is timed (start of restore to first watermarked tile, giving the real recovery time); the frequency (before go-live, then at a stated interval such as quarterly, and after any change to the backup commands or the storage layout); and cleanup (delete the scratch containers, volumes, and backup files, because they contain password hashes and document content).

### Exercise 34.6 ★★★ Plan a change

Two changes, in the spirit of the chapter: (1) the tile store needs a way to take a point-in-time snapshot (storage-level snapshots, or object storage with versioned keys) so that the archive doesn't depend on the files being still while it is read; (2) the database and the tile store need a common reference point, for example a recorded database position or snapshot time that says which tile versions belong to it, so a restore can be checked or made consistent afterward. Chapter 37's alternatives point the same way: object storage such as S3 (section 37.7) for versioned tiles, and a managed database (section 37.9) with point-in-time recovery. Even then, the "current tile version" pointer in the database still has to match the tiles, so consistency needs design, not just a different product.
