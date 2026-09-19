<!-- chapter: 14 | part: II | owner: writer-backend | tag: book-m2-documents | status: draft -->
# Solutions: Chapter 14

### Exercise 14.1 ★ Store roles as text

`@Enumerated(EnumType.STRING)`. Storing the name (`READER`) means reordering or inserting enum constants can never change what an existing row means; a stored number would silently shift.

### Exercise 14.2 ★ Indexes on the audit table

`V2__documents_shares_audit.sql` creates `audit_event` and four indexes: `ix_audit_event_time` (occurred_at), `ix_audit_event_user_time` (username, occurred_at), `ix_audit_event_document_time` (document_id, occurred_at) and `ix_audit_event_type_time` (event_type, occurred_at).

### Exercise 14.3 ★★ A query with no SQL

From the method name. Spring Data reads `findByUsername` and generates `select ... where username = ?` when it builds the repository bean at startup.

### Exercise 14.4 ★★ TransactionTemplate versus @Transactional

`DocumentService` renders PDFs, which is slow. A transaction holds a database connection, so it must wrap only the fast database work; `TransactionTemplate` lets the service choose exactly which code runs inside the transaction, where an annotation would wrap the whole method.

### Exercise 14.5 ★★★ Editing a migration that already ran

Flyway stores a checksum of each applied migration. It detects the changed file and refuses to start. The correct change is a new migration, for example `V4__...sql`, containing the `ALTER` statements.
