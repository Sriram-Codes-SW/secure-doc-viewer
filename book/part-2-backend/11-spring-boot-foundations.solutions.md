<!-- chapter: 11 | part: II | owner: writer-backend | tag: book-m6-final | status: draft -->
# Solutions: Chapter 11

1. (★) The cookie name is `SDV_SESSION`, under `server.servlet.session.cookie.name`. The line `http-only: true` makes it unreadable to JavaScript.
2. (★) `DocumentService documents` and `RequestActors actors`.
3. (★★) `BootstrapAdmin` reads `${secure-doc-viewer.bootstrap-admin.username:admin}` (default `admin`) and `${secure-doc-viewer.bootstrap-admin.password:}` (default empty, which makes the class generate a random password).
4. (★★★) Spring needs to know which constructor to call when a class has more than one. `KnownDevices` has a public one for Spring and a second, visible only inside its package, that tests use to pass a fixed clock. `@Autowired` on the public one resolves the choice. A class with a single constructor needs no label, because Spring uses it.
