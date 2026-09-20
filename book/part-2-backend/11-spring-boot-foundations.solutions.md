<!-- chapter: 11 | part: II | owner: writer-backend | tag: book-m6-final | status: expanded -->
# Solutions: Chapter 11

### Exercise 11.1 ★ Find the cookie settings

The cookie name is `SDV_SESSION`, under `server.servlet.session.cookie.name`. The line `http-only: true` makes it unreadable to JavaScript. The placeholder `${SESSION_COOKIE_SECURE:false}` on the `secure` line controls whether it is sent only over HTTPS: the environment variable `SESSION_COOKIE_SECURE` if set, otherwise `false`.

### Exercise 11.2 ★ List the dependencies

`DocumentController` receives `DocumentService documents` and `RequestActors actors`. `DocumentService` receives `DocumentRepository`, `AppUserRepository`, `TileGenerationService`, `AuditLogService` and `PlatformTransactionManager`. Going one level further, `RequestActors` needs `SessionKeys`, which needs `ViewerProperties`, and `TileGenerationService` needs `ViewerProperties` and `ViewerMetrics`. A diagram has `DocumentController` at the top with arrows down to those two, and so on, as in Figure 11.1. Notice that `ViewerProperties` is at the bottom of several branches: one shared bean serves them all.

### Exercise 11.3 ★★ Find the `@Value` settings

`BootstrapAdmin`'s constructor has `@Value("${secure-doc-viewer.bootstrap-admin.username:admin}")`, so the username defaults to `admin`, and `@Value("${secure-doc-viewer.bootstrap-admin.password:}")`, so the password defaults to empty, which the class treats as "generate a random one". In `application.yml`, the block under `secure-doc-viewer.bootstrap-admin` sets them from the environment variables `BOOTSTRAP_ADMIN_USERNAME` (default `admin`) and `BOOTSTRAP_ADMIN_PASSWORD` (default empty).

### Exercise 11.4 ★★ Who wins?

The port is 9090. Spring Boot's relaxed binding maps the environment variable `SERVER_PORT` onto the property `server.port` (upper case and underscores are accepted for dotted, dashed names), and an environment variable overrides the value in `application.yml`. To check on your own copy, start the program with the variable set and read the port the framework reports in its startup output, or request `http://localhost:9090/actuator/health`. Remember to clear the variable afterward, or a later "I changed the YAML and nothing happened" will be this exercise coming back.

### Exercise 11.5 ★★★ Explain the `@Autowired` in `KnownDevices`

A worked outline. Spring needs to know which constructor to call. When a class has exactly one constructor, Spring uses it. `KnownDevices` has two: a public one taking a `JdbcTemplate` and `ViewerProperties` (which fills in the real system clock) and a second one, visible only in its own package, that also takes a `Clock`. The tests use the second one to pass a fixed clock and move time by hand (Chapter 18). If you removed `@Autowired` from the public constructor, Spring would find two constructors and no marker, and, unless there is a no-argument constructor to fall back on, startup would fail with an error saying it could not find a default constructor. The second constructor exists for testability: it lets the class depend on a `Clock` supplied from outside without complicating the production wiring.

### Exercise 11.6 ★★★ Break the startup on purpose

A worked outline, since the exact text depends on your versions.

- **Unset the signing secret.** Failure at step 3 of Section 11.2 (creating objects): binding `ViewerProperties` fails validation, and the error mentions `SIGNING_SECRET must be set`. This is the easiest to act on, because the message was written by the project and names the variable.
- **`server.port` set to text.** Failure in step 1 or when the web server is created: Spring can't convert the value to a number, and the error names the property and the value. Also easy: the property name is in the message.
- **Delete `@Service` from a class.** Failure at step 3: `DocumentController` (or another class) asks for a bean of that type and none exists, so startup stops with an error that names the type and the class that needed it. This one is easy to act on too, if you read the "Caused by" chain from the bottom.

What makes a message easy to act on: it names the thing that is wrong (the property, the type) and, where the project wrote it, says what to do. Restore each change before the next experiment.
