# Chapter 9 solutions

### Exercise 9.1 ★ Read a table

The column is `owner_id`, type `BIGINT`, declared `NOT NULL` and a foreign key to `app_user (id)`. It can't be empty because every document must have an owner: the owner and admins manage it, and the access rules depend on it.

### Exercise 9.2 ★ Write a query

```sql
SELECT title FROM document WHERE visibility = 'PRIVATE' ORDER BY created_at DESC;
```

### Exercise 9.3 ★★ Write a migration

```sql
ALTER TABLE document ADD COLUMN description VARCHAR(500) NULL;
```

Existing rows have no description, so the new column must allow `NULL` (or have a default); otherwise the migration would fail on a database that already contains documents. Never edit `V2`; add `V4`.

### Exercise 9.4 ★★ Count and group

```sql
SELECT role, COUNT(*) AS accounts FROM app_user GROUP BY role;

SELECT enabled, COUNT(*) AS accounts FROM app_user GROUP BY enabled;
```

The first returns one row per role with its count; the second returns up to two rows, one for `enabled` true (shown as 1) and one for false (0).

### Exercise 9.5 ★★ Join three tables

```sql
SELECT d.title, u.username AS shared_with
FROM document d
JOIN document_share s ON s.document_id = d.id
JOIN app_user u ON u.id = s.user_id
ORDER BY d.title, u.username;
```

To include documents shared with nobody, use `LEFT JOIN` for both joins (`FROM document d LEFT JOIN document_share s ... LEFT JOIN app_user u ...`); those documents then appear with `NULL` in `shared_with`.

### Exercise 9.6 ★★★ Design a table

One good answer:

```sql
CREATE TABLE document_comment (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    document_id VARCHAR(36)   NOT NULL,
    author_id   BIGINT        NOT NULL,
    body        VARCHAR(2000) NOT NULL,
    created_at  DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_comment_document FOREIGN KEY (document_id) REFERENCES document (id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_author FOREIGN KEY (author_id) REFERENCES app_user (id)
);

CREATE INDEX ix_comment_document_time ON document_comment (document_id, created_at);
```

Key decisions: comments on a deleted document have no meaning, so `ON DELETE CASCADE` on `document_id` (as `document_page` and `document_share` do). Users are disabled, never deleted, so the author foreign key needs no cascade, and the default refusal to delete a user with comments is consistent with that policy. If comments had to survive as evidence, the audit table's approach applies instead: copy the author's username and the document title into the row and drop the foreign keys (denormalize), at the cost of copies that can go stale. The index serves "comments of this document in time order." The type sizes and the 2,000-character limit are choices you should be able to defend.
