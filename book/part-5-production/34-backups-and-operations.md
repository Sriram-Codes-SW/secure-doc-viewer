<!-- chapter: 34 | part: V | owner: writer-production | tag: book-m5-platform | status: draft -->
# Chapter 34: Backups, restores and operations

This chapter answers a question every real deployment must answer before the first user arrives: if the server's disk dies tonight, what do you get back tomorrow? You'll learn what state the app holds, how to capture it consistently, and why a backup you have never restored is only a hope.

## Learning objectives

By the end of this chapter, you will be able to:

- List every place the app keeps state and say what is lost if each disappears.
- Explain why the database and the tile files must be backed up at the same moment.
- Run the project's backup and restore commands and say what each does.
- Plan a restore drill and judge whether it succeeded.
- Describe what the storage janitor and the audit purge remove, and what they never remove.

## Prerequisites

- Chapter 9: SQL and MySQL (dumps, transactions).
- Chapter 10: Docker volumes.
- Chapter 33: the compose stack and its services.

## Beginner tier: What is a backup?

### 34.1 The analogy: a photograph of a whiteboard

A backup is a photograph of a whiteboard before someone erases it. If two people are writing on the board while you take the photo, one half of the picture is from before an edit and the other half from after, and the photo tells a story that never happened. Keeping the board still while you photograph it gives a picture you can trust.

The analogy breaks down because a real backup is a set of files, and *restoring* it, putting the whiteboard back exactly as photographed, is a separate skill you have to practice.

### 34.2 Terms you need

- **Backup:** a copy of your data kept somewhere else so a failure doesn't destroy it.
- **Restore:** copying that data back and starting the app on it.
- **Dump:** a text file of the SQL commands needed to rebuild a database (made here with `mysqldump`).
- **Volume:** a Docker-managed folder that outlives the container (Chapter 10). This app uses `mysql-data` and `app-storage`.
- **Restore drill:** restoring a backup into a scratch environment to prove it works.
- **Consistent:** every part of the backup describes the same moment.

### 34.3 What state exists and where

Two things hold state (README, "Backup and restore"):

**Table 34.1 — Where state lives**

| State | Where | If lost |
|---|---|---|
| Accounts, documents, shares, audit trail, recognised-device hashes | MySQL, volume `mysql-data` | Everything except the tile images |
| Rendered tiles | Volume `app-storage` (`/data/storage`) | Documents exist but show nothing |
| Sessions and rate-limit counters | Memory of the app | Nothing durable: users sign in again |
| Secrets | `.env` file | Tokens and recognised devices |

The source PDF is not in the table: the app deletes it after ingest (Chapter 32), so the tiles are the only copy of the content that the app keeps.

## Intermediate tier: A consistent backup

*On a first read you can skip to "In this project"; return here when you set up scheduled backups.*

### 34.4 Why the two must match

<!-- source: README "Backup and restore"; PR #5 body "Final-review fixes (66f7152)"; dossier/bugs-and-findings.md -->
Replacing a PDF renders new tiles into a new version folder, switches the document to it, and then deletes the previous version's tiles as soon as the switch commits. Suppose you dump the database, a publisher then replaces a document, and you archive the tiles afterward. Your dump says the document uses version 1; your archive contains only version 2. After a restore, the document points at tiles that no longer exist.

The Senior Technical Manager reviewer (an AI review agent; see Chapter 32) found this in the final review, and the runbook was changed to stop the app for the few seconds a backup takes so nothing can change between the two captures (commit `66f7152`). The trade-off is stated openly: a short outage, in return for backups that are correct without any cleverness.

### 34.5 The backup and restore commands

These are copied from the README. `$MYSQL_ROOT_PASSWORD` and `$MYSQL_DATABASE` are read inside the container, so the password never appears on your command line.

**Listing 34.1 — Backup, from `README.md`, `book-m6-final`**

```bash
docker compose --profile full stop app
docker compose exec -T mysql sh -c 'exec mysqldump --single-transaction --routines -u root -p"$MYSQL_ROOT_PASSWORD" "$MYSQL_DATABASE"' > securedocs.sql
docker run --rm -v secure-doc-viewer_app-storage:/data -v "$PWD":/backup alpine tar czf /backup/storage.tgz -C /data .
docker compose --profile full start app
```

Line by line: stop only the app (the database stays up, because the dump needs it); dump the database to `securedocs.sql`; use a throwaway `alpine` container that mounts the tile volume and the current folder to pack the tiles into `storage.tgz`; start the app again.

**Listing 34.2 — Restore, from `README.md`, `book-m6-final`**

```bash
docker compose --profile full stop app
docker compose exec -T mysql sh -c 'exec mysql -u root -p"$MYSQL_ROOT_PASSWORD" "$MYSQL_DATABASE"' < securedocs.sql
docker run --rm -v secure-doc-viewer_app-storage:/data -v "$PWD":/backup alpine sh -c 'rm -rf /data/* && tar xzf /backup/storage.tgz -C /data'
docker compose --profile full start app
```

The restore replays the SQL, empties the tile volume, and unpacks the archive into it. The volume name `secure-doc-viewer_app-storage` is the compose project name plus the volume name; it changes if your project folder has a different name.

Keep `.env` with the backup. A restore under a different `SIGNING_SECRET` still works, but every account's recognised devices are forgotten, because their hashes are keyed by that secret (README).

### 34.6 Restore drills

<!-- source: PR #5 body "Final-review fixes"; dossier/bugs-and-findings.md -->
A backup is only proven by a restore. The project did one: a backup was taken per the runbook and restored into a scratch MySQL and a scratch volume (PR #5). The README's instruction is the general rule: try a restore into a scratch environment before relying on the backups.

A drill has four steps: restore into a separate environment (never over production); start the app; sign in and open a document; and check that every tile loads. If any tile is missing, the two halves of the backup did not match, which is the exact failure section 34.4 describes.

## Advanced tier: Retention, cleanup and living with one instance

*On a first read you can skip to "In this project".*

### 34.7 What gets deleted automatically

Two jobs delete data on a schedule, and both are careful:

- **Storage janitor** (`StorageJanitor`): removes tile directories that no document points to, including superseded versions. It is conservative: only old folders named like a document id. As a safety net for restores, it never removes a document's other tile versions while its current one is missing (commit `66f7152`), so a bad restore can be repaired instead of being "cleaned up" into total loss.
- **Audit purge:** events older than `audit-retention-days` (default 180) are purged nightly. Users are disabled, never deleted, so old audit entries keep meaning.

### 34.8 Living with one instance

<!-- source: README Limitations, Go-live checklist -->
The app runs as a single instance. Sessions and rate-limit counters live in memory and tiles live on local disk, so a restart signs everyone out and resets throttle counters, and a second instance would not share either. Chapter 37 (sections 37.5, 37.7, and 37.11) sets out what Redis and S3 would change. For now: plan restarts for quiet hours, and expect the short outage during a backup.

## In this project

| Path | First appears | What it does |
|---|---|---|
| `README.md` ("Backup and restore") | `book-m5-platform` | The runbook in Listings 34.1 and 34.2 |
| `src/main/java/com/example/securedocviewer/service/StorageJanitor.java` | `book-m2-documents` | Removes unreferenced tile folders |
| `docker-compose.yml` (`mysql-data`, `app-storage`) | `book-m5-platform` | The two volumes to back up |

## Try it

### Exercise 34.1 ★ Name the state

For each of these, say whether a backup must capture it: the login sessions, the tiles, the audit log, the `.env` file, the source PDFs.

### Exercise 34.2 ★★ Break the match

Describe, step by step, what happens if you dump the database at 10:00, a publisher replaces a document at 10:01, and you archive the tiles at 10:02. What does the reader see after a restore?

### Exercise 34.3 ★★★ Design the drill

Write a restore-drill checklist for your own deployment: where you restore, how you prove tiles and accounts came back, and how often you repeat it.

## Summary

- The app has two durable stores, the database and the tile volume; sessions and counters are memory only.
- They must be captured at the same moment, so the runbook stops the app briefly.
- A restore drill is the only proof a backup works.
- The janitor and the audit purge delete on purpose, and the janitor refuses to prune when a document's current tiles are missing.

Chapter 35 shows how to watch the running app with health checks and metrics.

## Further reading

- MySQL 8.4 reference manual, `mysqldump`: https://dev.mysql.com/doc/refman/8.4/en/mysqldump.html
- Docker documentation, volumes: https://docs.docker.com/engine/storage/volumes/
