<!-- chapter: 15 | part: II | owner: writer-backend | tag: book-m1-accounts | status: draft -->
# Solutions: Chapter 15

### Exercise 15.1 ★ The unreadable session cookie

The setting `http-only: true` (it is configuration, not an annotation) under `server.servlet.session.cookie` in `application.yml`.

### Exercise 15.2 ★ What the database stores

A salted BCrypt hash, prefixed with the algorithm name such as `{bcrypt}`, in the `password_hash` column.

### Exercise 15.3 ★★ Thirty emoji versus 72 bytes

No. Each emoji takes 4 bytes in UTF-8, so 30 emoji are 120 bytes, above the 72-byte limit; `fitsBcrypt` counts bytes, not characters.

### Exercise 15.4 ★★ The role string Spring builds

`ROLE_PUBLISHER`. Spring adds the `ROLE_` prefix.

### Exercise 15.5 ★★★ Cookie or browser token

A session cookie lets the server end a session instantly and keeps the id away from JavaScript when `httpOnly`; but the server must store sessions and browsers send the cookie automatically, which is why CSRF protection is needed. A token in the browser needs no server-side storage, but it can't be revoked easily before it expires and JavaScript can read it. The project chose the session cookie for instant revocation and to keep the id out of scripts' reach.
