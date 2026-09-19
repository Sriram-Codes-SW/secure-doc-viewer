<!-- chapter: 9 | part: I | owner: writer-foundations | tag: book-m2-documents | status: draft -->
# Chapter 9: SQL and MySQL

From milestone 2 on, the Secure Document Viewer keeps its accounts, documents, shares and audit trail in a database, so they survive restarts and can be queried safely by many users at once. This chapter teaches the SQL language and the ideas behind relational databases, using the project's real migration files as examples.

## Learning objectives

By the end of this chapter, you will be able to:

- Explain tables, rows, columns and keys.
- Read a `CREATE TABLE` statement, including types and constraints.
- Write `INSERT`, `SELECT`, `UPDATE` and `DELETE` statements.
- Explain foreign keys and write a simple join.
- Explain what an index is and why the project adds them.
- Explain transactions and row locks.
- Explain what a migration is and why the schema is changed only through them.

## Prerequisites

- Chapter 2: The command line and your files
- Chapter 8: How the web works

## Beginner tier: Tables and queries

### 9.1 Tables, rows, columns, keys

Files on disk are fine for a PDF, but a poor place to answer "which documents can `reader.one` open?". A **database** is a program that stores data in an organized form and answers questions about it quickly, safely and for many users at once. This project uses **MySQL** 8.4, a **relational database**: one that stores data in tables and links them by keys. The language you use to talk to it is **SQL** (Structured Query Language).

A **table** is like a spreadsheet: it has named **columns** (the kinds of facts) and **rows** (one entry each). The `app_user` table has a column for username and one for role, and one row per account.

A **primary key** is a column (or set of columns) whose value identifies each row uniquely. It can't be empty or repeat. In `app_user`, the key is `id`. A **foreign key** is a column that holds the primary key of a row in another table, creating a link. In `document`, the column `owner_id` holds the `id` of the owner's row in `app_user`.

**Analogy.** A table is a spreadsheet tab and a foreign key is a cell that says "see row 7 of the Users tab". The analogy breaks down because a database enforces the link: it refuses to store an `owner_id` that points to nobody.

### 9.2 `CREATE TABLE`, types and constraints

Every table starts with a `CREATE TABLE` statement. Here is the very first migration of the project, the accounts table.

**Listing 9.1 — `V1__create_app_user.sql` (book-m2-documents)**

```sql
-- Accounts that can sign in. Usernames are stored lower-cased by the
-- application, so the unique constraint is effectively case-insensitive.
CREATE TABLE app_user (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    username      VARCHAR(64)  NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_app_user_username UNIQUE (username)
);
```

*Path: `src/main/resources/db/migration/V1__create_app_user.sql`*

Line by line:

- `--` starts a comment.
- `CREATE TABLE app_user ( ... );` defines the table. Each line inside is a column: a name, a **type**, and optional rules.
- `BIGINT` is a large whole number; `VARCHAR(64)` is text up to 64 characters; `BOOLEAN` is true or false; `DATETIME(6)` is a date and time with microsecond precision.
- `NOT NULL` means the column can't be empty. SQL's `NULL` means "unknown or missing", like Chapter 5's `null`.
- `AUTO_INCREMENT` makes the database assign the next number to `id`, so each new account gets a fresh key.
- `DEFAULT TRUE` supplies a value when the row doesn't give one.
- `PRIMARY KEY (id)` declares the key.
- `CONSTRAINT uk_app_user_username UNIQUE (username)` is a **constraint**, a rule the database enforces: no two accounts may share a username. Enforcing it in the database is safer than only checking in Java, because two simultaneous account-creation requests can't both slip through.

Notice `password_hash`, not `password`. The database never stores the password itself, only a one-way scrambled form (Chapter 15).

### 9.3 INSERT, SELECT, UPDATE, DELETE

Four statements cover everyday work. Example 9.1 uses the table above; the values are made up for teaching, and in the app Java code sends these statements for you (Chapter 14).

**Example 9.1 — The four everyday statements**

```sql
INSERT INTO app_user (username, password_hash, role, created_at)
VALUES ('pub.one', '<password-hash>', 'PUBLISHER', NOW(6));

SELECT username, role FROM app_user WHERE enabled = TRUE ORDER BY username;

UPDATE app_user SET role = 'ADMIN' WHERE username = 'pub.one';

DELETE FROM app_user WHERE username = 'pub.one';
```

- `INSERT` adds a row; you name the columns and give matching values. Columns you leave out use their default (`enabled` becomes true).
- `SELECT` reads. It lists columns, then `FROM` a table, then an optional `WHERE` filter and an `ORDER BY`.
- `UPDATE` changes existing rows, and `WHERE` decides which. Forget `WHERE` and every row changes.
- `DELETE` removes rows, with the same warning.

The project's query behind the share picker, "the first 20 enabled usernames starting with a prefix", is generated from a method name in Chapter 14, and it turns into a `SELECT ... WHERE ... LIKE ... ORDER BY ... LIMIT 20`. <!-- source: AppUserRepository.findTop20ByEnabledTrueAndUsernameStartingWithOrderByUsernameAsc at book-m6-final -->

## Intermediate tier: Relationships and speed

### 9.4 Relationships: foreign keys and joins

Documents and users are related: every document has one owner, and a document can be shared with many users, each of whom can see many documents. The second kind, **many-to-many**, needs a third table that holds pairs. The project's second migration creates these tables.

**Listing 9.2 — `V2__documents_shares_audit.sql` (book-m2-documents, simplified: two tables and the indexes omitted)**

```sql
CREATE TABLE document (
    id          VARCHAR(36)  NOT NULL,
    title       VARCHAR(200) NOT NULL,
    owner_id    BIGINT       NOT NULL,
    visibility  VARCHAR(20)  NOT NULL,
    page_count  INT          NOT NULL,
    tile_size   INT          NOT NULL,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_document_owner FOREIGN KEY (owner_id) REFERENCES app_user (id)
);

-- ...

CREATE TABLE document_share (
    document_id VARCHAR(36) NOT NULL,
    user_id     BIGINT      NOT NULL,
    PRIMARY KEY (document_id, user_id),
    CONSTRAINT fk_document_share_document FOREIGN KEY (document_id) REFERENCES document (id) ON DELETE CASCADE,
    CONSTRAINT fk_document_share_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE
);
```

*Path: `src/main/resources/db/migration/V2__documents_shares_audit.sql`*

Reading it:

- `document.owner_id` is a foreign key to `app_user.id`. The database refuses a document whose owner doesn't exist.
- `document_share` is a **join table**: each row says "this user may open this document". Its primary key is the *pair* `(document_id, user_id)`, so the same share can't be recorded twice, which is exactly Chapter 5's set behavior, enforced by the database.
- `ON DELETE CASCADE` means: when the referenced row is deleted, delete these rows too. Deleting a document removes its shares automatically, so no orphaned shares remain.

A **join** combines rows from two tables in a query. This teaching query lists documents with their owners' usernames:

**Example 9.2 — A join**

```sql
SELECT d.title, u.username AS owner
FROM document d
JOIN app_user u ON u.id = d.owner_id
WHERE d.visibility = 'EVERYONE';
```

`d` and `u` are short aliases for the tables, and `ON` says how rows match: the document's `owner_id` equals the user's `id`. The project's Java query in `DocumentRepository` (Listing 4.5's neighbor) does the same thing with `join fetch d.owner`. <!-- source: DocumentRepository.java at book-m6-final -->

### 9.5 Indexes: why some queries are fast

Without help, finding all documents for one owner means reading every row. An **index** is a sorted lookup structure on one or more columns, like the index of a book: the database jumps straight to matching rows. The cost is extra storage and slightly slower writes, so you add indexes for queries you actually run. The project's second migration adds several.

**Listing 9.3 — `V2__documents_shares_audit.sql` (book-m2-documents, excerpt: indexes)**

```sql
CREATE INDEX ix_document_owner ON document (owner_id);

CREATE INDEX ix_document_share_user ON document_share (user_id);

CREATE INDEX ix_audit_event_time ON audit_event (occurred_at);
CREATE INDEX ix_audit_event_user_time ON audit_event (username, occurred_at);
```

*Path: `src/main/resources/db/migration/V2__documents_shares_audit.sql`*

`ix_document_owner` speeds up "documents owned by this user", and `ix_document_share_user` speeds up "documents shared with this user", which the library page needs on every load. The audit table is append-only and grows without limit, so its indexes let an administrator filter by time or by user without scanning millions of rows. An index on `(username, occurred_at)` serves both "this user's events" and "this user's events in this time range".

The per-tile access check runs for every single tile and is written to be, in the source's own words, "one indexed query, no entity loading". <!-- source: DocumentRepository.findTileAccessIfVisible comment at book-m6-final -->

## Advanced tier: Safety over time

### 9.6 Transactions and locks

A **transaction** groups several statements so they all succeed or none do. Replacing a PDF means writing new tile files, then switching the document to the new version; if it fails halfway, the reader must still see a consistent document. A transaction guarantees that.

Two people changing the same document at once is a second problem. A **lock** makes one wait for the other. The project asks the database for a **row lock** when replacing or deleting a document:

**Listing 9.4 — `DocumentRepository.java` (book-m6-final, excerpt)**

```java
    /** Row lock for replace/delete, so concurrent changes to one document are serialised. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Document d where d.id = :id")
    Optional<Document> findByIdForUpdate(@Param("id") String id);
```

*Path: `src/main/java/com/example/securedocviewer/document/DocumentRepository.java`*

`PESSIMISTIC_WRITE` means "assume there will be a conflict and lock the row now". While one request holds the lock, a second request for the same document waits its turn. The chapter on JPA (Chapter 14) covers the details and the alternatives.

### 9.7 Migrations: changing a schema safely over time

A **schema** is the set of tables and columns. It has to change as the app grows, and the changes must apply identically on your machine, in tests and in production. A **migration** is a numbered SQL file that makes one change. **Flyway**, the migration tool the project uses, runs the files in order when the app starts, skips the ones it has already applied, and records what ran in its own table.

The project has three migrations, and the name pattern matters: `V1__create_app_user.sql`, `V2__documents_shares_audit.sql`, `V3__tile_versions_and_account_security.sql`. `V` plus a version, two underscores, and a description. The third one changes existing tables:

**Listing 9.5 — `V3__tile_versions_and_account_security.sql` (book-m6-final, excerpt)**

```sql
ALTER TABLE document ADD COLUMN tile_version INT NOT NULL DEFAULT 0;

-- Admin-set passwords (new accounts, resets) must be changed at first sign-in.
ALTER TABLE app_user ADD COLUMN must_change_password BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE app_user ADD COLUMN last_sign_in_at DATETIME(6) NULL;
```

*Path: `src/main/resources/db/migration/V3__tile_versions_and_account_security.sql`*

`ALTER TABLE ... ADD COLUMN` changes a table that already holds rows. The `DEFAULT` values matter: existing rows need something to put in the new column, and `NOT NULL` without a default would fail. (`V3` is part of `book-m5-platform` and later; `book-m2-documents` has `V1` and `V2` only.) <!-- source: git ls-tree of tags -->

The rule is: **never edit a migration that has already run somewhere.** Flyway checks that applied files haven't changed and refuses to start if they have. To change the schema, add a new migration. The configuration sets `hibernate.ddl-auto: none` with the comment "Flyway owns the schema; Hibernate never alters it", so there is exactly one authority. <!-- source: application.yml at book-m6-final -->

## In this project

- `src/main/resources/db/migration/`: the three migrations, `V1` to `V3`.
- `document/Document.java`, `account/AppUser.java`: Java classes that map onto `document` and `app_user`.
- `document/DocumentRepository.java`: queries, including the row lock.
- `docker-compose.yml`: starts MySQL 8.4 (Chapter 10).

## Try it

### Exercise 9.1 ★ Read a table

Open `V2__documents_shares_audit.sql` at `book-m2-documents` and find the column that records who owns a document. What is its type, and why can't it be empty?

*Hint:* look for `NOT NULL` and the foreign key.

*Solution:* Appendix C, Exercise 9.1.

### Exercise 9.2 ★ Write a query

Write a `SELECT` that lists the titles of all documents whose visibility is `PRIVATE`, newest first.

*Solution:* Appendix C, Exercise 9.2.

### Exercise 9.3 ★★ Write a migration

On a branch, write `V4__add_document_description.sql` that adds an optional `description` column of up to 500 characters to `document`. Why must the column allow `NULL`, or have a default?

*Solution:* Appendix C, Exercise 9.3.

## Summary

- A relational database stores data in tables linked by keys, and SQL is the language for it.
- `CREATE TABLE` defines types and constraints; the database enforces them.
- `INSERT`, `SELECT`, `UPDATE` and `DELETE` cover everyday work; `WHERE` matters.
- Foreign keys link tables, joins combine them, and indexes make chosen queries fast.
- Transactions and locks keep concurrent changes safe; migrations change the schema in numbered, never-edited steps.

## Further reading

- *MySQL 8.4 Reference Manual*, "Data Types" and "SQL Statements." https://dev.mysql.com/doc/refman/8.4/en/
- *Flyway Documentation*, "Migrations." https://documentation.red-gate.com/flyway
