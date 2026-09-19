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
