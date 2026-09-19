# Solutions: Chapter 34

### Exercise 34.1 ★ Name the state

- Login sessions: no. They live in memory and users simply sign in again.
- Tiles: yes (`app-storage` volume).
- Audit log: yes (in MySQL).
- `.env` file: yes, kept securely with the backup; `SIGNING_SECRET` keys the recognised-device hashes.
- Source PDFs: they don't exist after ingest, so there is nothing to back up; the tiles hold the content.

### Exercise 34.2 ★★ Break the match

10:00 the dump records document D at version 1. 10:01 the replace renders version 2 and, on commit, deletes version 1's tiles. 10:02 the archive contains only version 2. After a restore the database says D uses version 1, but `D/v1/...` isn't on disk. Each tile request finds nothing, so readers see blank pages or errors for D. The janitor's safety net (it won't prune other versions while the current one is missing) keeps the situation from getting worse, but the content is gone. The runbook prevents this by stopping the app between the two captures.

### Exercise 34.3 ★★★ Design the drill

A good answer names: a scratch environment (separate compose project, ports, and volumes, never production); the steps (start MySQL, load the dump, unpack the tiles, start the app); proof (sign in as a known account, open several documents including one that was replaced, confirm every tile loads, check the audit log's newest event date); an owner; and a schedule (for example before go-live and then quarterly, and after any change to the backup commands).
