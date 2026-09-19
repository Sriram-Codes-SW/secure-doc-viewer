<!-- chapter: 15 | part: II | owner: writer-backend | tag: book-m1-accounts | status: draft -->
# Solutions: Chapter 15

1. (★) `http-only: true` under `server.servlet.session.cookie` in `application.yml`.
2. (★) A salted BCrypt hash, prefixed with the algorithm name such as `{bcrypt}`, in the `password_hash` column.
3. (★★) No. Each emoji takes 4 bytes in UTF-8, so 30 emoji are 120 bytes, above the 72-byte limit; `fitsBcrypt` counts bytes, not characters.
4. (★★) `ROLE_PUBLISHER`. Spring adds the `ROLE_` prefix.
5. (★★★) A session cookie lets the server end a session instantly and keeps the id away from JavaScript when `httpOnly`; but the server must store sessions and browsers send the cookie automatically, which is why CSRF protection is needed. A token in the browser needs no server-side storage, but it can't be revoked easily before it expires and JavaScript can read it. The project chose the session cookie for instant revocation and to keep the id out of scripts' reach.
