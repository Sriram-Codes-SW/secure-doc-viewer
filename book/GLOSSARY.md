# Glossary

Owner: `editor`. Writers request additions in `book/_team/requests.md`:
`- [writer → editor] glossary: **term** — definition. (Ch N, Section N.M)`.
Entries are alphabetical. "First defined" is the chapter where the term is introduced (per the outline; confirmed as chapters land).

| Term | Definition | First defined |
|---|---|---|
| **absolute** | A path is absolute if it starts from the top (`/c/dev/...`) and relative if it starts from where you are (`src/main`). | Ch 2 |
| **access levels** | Packages also matter for access levels, which decide who may use a class, field or method. | Ch 4 |
| **account** | An account is a stored record of who may sign in: a username, a password hash, a role. | Ch 26 |
| **added** | `--diff-filter=A` keeps only commits that added the file. | Ch 7 |
| **address** | An address here means which network interface the program listens on; the name `localhost` means this computer, and its numeric form is `127.0.0.1`. | Ch 2 |
| **admin handle** | A tile request must present both the URL and the session cookie it derives from, so a leaked URL alone is useless. | Ch 26 |
| **administrator** | `pub.one` is a publisher. | Ch 1 |
| **Angular** | An Angular component is one reusable piece of screen. | Ch 21 |
| **Angular CLI** | The Angular CLI (command line interface) is the `ng` program. | Ch 20 |
| **annotation (Java)** | A marker such as `@Service` placed on code that tells a framework or the compiler something about it. | Ch 4, used in Ch 11 |
| **annotations** | Lines that start with `@` are annotations: labels attached to code that tools read. | Ch 4 |
| **application context** | The box that holds all of the app's beans. | Ch 11 |
| **arguments** | Every command has the same shape: the command name, then optional flags (also called options, which change how it behaves), then arguments (what to act on). | Ch 2 |
| **array** | The outer loop walks the rows, the inner loop walks the columns of each row, and each pass creates a token (the signed part of a URL) and stores a URL in `urls`, a table of strings (`String[][]`, a two-dimensional array, a fixed-size row of values). | Ch 3 |
| **arrow function** | A short way to write a function in JavaScript and TypeScript, using `=>`. | Ch 19 |
| **arrow functions** | Small functions are often written as arrow functions, `(x) => expression`, which are the same idea as Java lambdas (Chapter 5). | Ch 19 |
| **assigns** | The `=` assigns: it stores the value on the right in the variable on the left. | Ch 3 |
| **atomic counting** | The design is atomic counting. | Ch 16 |
| **audit log** | The health check is the light, metrics are the gauges, and the audit log is the trip log. | Ch 35 |
| **audit trail** | An append-only record, kept in the database, of important actions such as sign-ins, uploads, views and denied requests. | Ch 1 |
| **authentication** | Proving who you are, for example with a username and password. | Ch 1 |
| **authorization** | Deciding what an authenticated person is allowed to do. | Ch 1 |
| **auto-configuration** | Spring Boot's habit of setting up sensible defaults for the libraries it finds on the classpath. | Ch 11 |
| **axe-core** | axe-core is an engine that inspects a rendered page against the Web Content Accessibility Guidelines (Chapter 21) and reports violations such as low-contrast text, form fields with no label, or missing names on buttons. | Ch 24 |
| **backend** | The part of an application that runs on a server and holds the data and rules. | Ch 1 |
| **BCrypt** | BCrypt is a hashing function built for passwords: it includes the salt in its output and is deliberately slow, which makes guessing billions of passwords expensive. | Ch 15 |
| **bean** | An object Spring creates and manages, from a class labeled with annotations such as `@Component`, `@Service` or `@RestController`. | Ch 11 |
| **Bean Validation** | The Java standard for declaring input rules with annotations such as `@NotBlank`. | Ch 13 |
| **body** | The bottom is the response: a status line, headers, an empty line, and the body. | Ch 8 |
| **boxing** | The map above uses `Integer`, the object form of `int`, and Java converts between them automatically (boxing). | Ch 5 |
| **branch** | A branch is a movable name for a line of work. | Ch 7 |
| **browser** | The program (Chrome, Firefox, Safari, Edge) that requests web pages and runs their code. | Ch 19 |
| **budgets** | The build settings live in `angular.json`, whose `build` target names the entry point (`src/main.ts`), the global stylesheet (`src/styles.css`) and size budgets (limits that turn a warning or an error on when the build output grows too large): in production the initial bundle warns at 500 kB and fails at 1 MB, and any one... | Ch 20 |
| **build** | Together these steps are called a build. | Ch 6 |
| **build context** | When you build an image, Docker sends the project folder to the builder as the build context, and a careless `COPY . .` could copy `.env` into the image, where anyone who receives the image could read it. | Ch 10 |
| **build tool** | A program that compiles code, runs tests and packages the result; Maven for this project's Java. | Ch 6 |
| **built-in accounts** | The product owner, asked directly, chose built-in accounts: Spring Security, BCrypt passwords, three roles (READER, PUBLISHER, ADMIN), an httpOnly session cookie, login throttling and a seeded first admin. | Ch 26 |
| **bundler** | So do the bundler, which gathers your many source files and the libraries they use into a few files a browser can download efficiently, and the test runner. | Ch 20 |
| **bytecode** | The compact instructions the Java compiler produces, which the JVM runs. | Ch 3 |
| **bytes** | The validation allowed passwords of 12 to 128 characters, but the password hashing (BCrypt, Chapter 15) rejects more than 72 bytes, and a character can be more than one byte: 30 emoji can exceed 72 bytes. | Ch 5 |
| **cache** | A place where a copy of something is kept so it can be served again faster. | Ch 1 |
| **Caddy** | nginx overwrites the header with the connection address and accepts a forwarded address only from the optional HTTPS front end, Caddy (a web server that handles certificates, at `172.28.0.11`; Chapter 33). | Ch 32 |
| **catches** | When code throws one, normal execution stops and the exception travels up the chain of callers (Chapter 3's stack trace) until something catches it. | Ch 5 |
| **ceiling division** | This is ceiling division, "divide and round up". | Ch 3 |
| **certificate** | A certificate is a signed statement, issued by an authority the browser trusts, that a public key belongs to a given domain. | Ch 8 |
| **change detection** | Deciding what to redraw is change detection. | Ch 21 |
| **checked** | Others, such as `IOException` (which file operations throw), are checked: a method that can throw one must either catch it or declare `throws IOException`, which you have seen in `DocumentController.upload`. | Ch 5 |
| **CI** | CI (continuous integration) runs your tests on every change. | Ch 31 |
| **class** | `public class Hello { ... }` declares a class, a named container for code. | Ch 3 |
| **client** | A program that asks for things over a network; your browser is a client. | Ch 1 |
| **clone** | To get a copy of an existing repository, you clone it. | Ch 7 |
| **closed** | Files, network connections and database connections must be closed after use, or the program leaks them until it runs out. | Ch 5 |
| **Code review** | Code review is the practice of having someone else read the change before it lands. | Ch 7 |
| **collection** | A collection is an object that holds many values. | Ch 5 |
| **columns** | A table is like a spreadsheet: it has named columns (the kinds of facts) and rows (one entry each). | Ch 9 |
| **commit** | Each saved state is a commit: a snapshot of every tracked file at one moment, with a message describing the change and a unique identifier such as `2d10e07`. | Ch 7 |
| **commits** | A transaction groups several database changes so that either all succeed or none do: it either commits (makes them all permanent) or rolls back (undoes everything it did). | Ch 14 |
| **compiler** | A program that checks source code and translates it into a form a machine can run. | Ch 3 |
| **compiler error** | A compiler error stops the build before anything runs. | Ch 3 |
| **component scanning** | Spring searching your packages for annotated classes and registering them as beans. | Ch 11 |
| **composition** | For most other cases, the project prefers composition: a class *has* another object and uses it, as `RequestActors` has a `SessionKeys`. | Ch 4 |
| **computed** | A computed signal is a value derived from others; it is recalculated only when a signal it read has changed, and is otherwise cached. | Ch 21 |
| **constraint** | SQL's `NULL` means "unknown or missing", like Chapter 5's `null`. | Ch 9 |
| **constructor** | A constructor is special code that runs when you create an object with `new`, and sets up its fields. | Ch 4 |
| **constructor injection** | Dependency injection where an object receives its dependencies as constructor parameters. | Ch 4 |
| **container** | A running, isolated instance of an image: a program packaged with everything it needs. | Ch 10 |
| **containers** | Docker solves this by running programs in containers: isolated processes that bring their own files and settings. | Ch 10 |
| **Content-Security-Policy** | Content-Security-Policy (CSP) is an allow-list that tells the browser what a response may do. | Ch 8 |
| **Continuous integration (CI)** | Continuous integration (CI) runs the tests automatically on every proposed change. | Ch 24 |
| **Contrast** | Contrast is the difference in brightness between text and its background. | Ch 21 |
| **convention over configuration** | Maven relies on convention over configuration: if you put files where it expects them, it needs no instructions. | Ch 6 |
| **cookie** | A small piece of data a server asks the browser to store and send back with later requests. | Ch 8 |
| **coordinates** | Every Maven project or library is identified by three values, its coordinates. | Ch 6 |
| **cron** | A cron expression lists second, minute, hour, day, month and weekday. | Ch 14 |
| **cross-site request forgery (CSRF)** | This is cross-site request forgery (CSRF): another site borrows your credentials. | Ch 16 |
| **CSRF (Cross-Site Request Forgery)** | An attack in which another website makes your browser send a request to a site you are signed in to. | Ch 8 |
| **CSRF token** | The app adds a second: a CSRF token, a secret value that the app's own page reads and sends back in a header on every change (`POST`, `PUT`, `PATCH`, `DELETE`). | Ch 8 |
| **CSS custom properties** | Look-and-feel that every screen shares lives in one global file, `styles.css`, built on CSS custom properties (also called variables): named values, written `--name`, that any rule can read with `var(--name)`. | Ch 21 |
| **database** | The PDF stops being servable after upload; only disconnected tiles remain. | Ch 1 |
| **decompression bomb** | A small file that expands to an enormous amount of data (or pixels) when processed, exhausting memory. | Ch 13 |
| **decorator** | `@Component({ ... })` is a decorator: a label attached to the class below it that tells Angular "this class is a component, configured like so". | Ch 21 |
| **deep link** | A deep link is an address that leads straight to a specific state, for example the viewer at page 3 of a document. | Ch 23 |
| **defense** | A threat is a specific way someone might misuse the system; a defense is what stands in the way. | Ch 1 |
| **Denormalized** | Denormalized means storing a copy of information instead of a link. | Ch 9 |
| **deny by default** | The last line, `anyRequest().denyAll()`, is a deny by default stance: a route you forget to list is closed, not open. | Ch 26 |
| **Dependabot** | Dependabot watches them and opens a pull request when a newer version exists. | Ch 31 |
| **dependency** | A dependency is a library your code uses. | Ch 6 |
| **dependency injection** | Giving an object the other objects it needs instead of letting it create them itself. | Ch 4 |
| **deserialization** | Turning text or bytes, such as JSON, back into an object. | Ch 12 |
| **detached HEAD** | Both put you in detached HEAD state. | Ch 7 |
| **developer tools** | The inspection panel built into browsers that shows requests, code and errors. | Ch 1 |
| **development server** | `ng serve` compiles the app in memory and serves it with a small development server, refreshing the browser whenever you save a file. | Ch 20 |
| **diamond** | First, when you write `new HashMap<>()` the empty angle brackets, called the diamond, tell the compiler to copy the types from the left side, so you write them once. | Ch 5 |
| **digest** | `image: mysql:8.4@sha256:...` names the image, its tag, and a digest: a fingerprint of the exact image contents. | Ch 10 |
| **discriminated union** | A TypeScript union of object types that each carry a fixed tag field, so code can tell them apart. | Ch 19 |
| **DNS** | The browser asks the DNS (Domain Name System), the internet's phone book, which number address belongs to the name `docs.example.com`. | Ch 8 |
| **Docker** | Docker solves this by running programs in containers: isolated processes that bring their own files and settings. | Ch 10 |
| **Docker Compose** | Docker Compose describes one or more containers in a file, `docker-compose.yml`, and starts them with one command. | Ch 10 |
| **Dockerfile** | For the backend, the project builds its own from a Dockerfile: a recipe of steps. | Ch 10 |
| **double-submit cookie** | This is the double-submit cookie pattern, and it is what `SecurityConfig` sets up: | Ch 16 |
| **DPI** | Dots per inch: how many pixels represent one inch of a page when it is rendered as an image. | Ch 1 |
| **Encapsulation** | Encapsulation means hiding a class's fields and exposing only the operations you intend. | Ch 4 |
| **encoded** | Computers store everything as bytes (Chapter 2), so a string must be encoded into bytes to be saved or sent. | Ch 3 |
| **encoding** | The rule that maps characters to bytes; the project's files use UTF-8. | Ch 2 |
| **encryption** | TLS provides three things: encryption (eavesdroppers see noise), integrity (changes are detected) and authentication (a certificate proves you reached the real host). | Ch 8 |
| **entity** | A Java class whose objects are stored as rows in a database table. | Ch 14 |
| **entries** | A map is not directly loopable, so you loop over its entries, each holding a key and a value. | Ch 5 |
| **enum** | An enum declares the complete list, and the compiler rejects anything else. | Ch 4 |
| **environment variable** | A named setting, such as `DB_PASSWORD`, that the operating system hands to every program it starts. | Ch 2 |
| **event binding** | The nav bar therefore exists only when someone is signed in, and the Admin link only for administrators. | Ch 21 |
| **exception** | An object that signals something went wrong and interrupts normal flow until it is handled. | Ch 3 |
| **exception handler** | A method that turns a particular exception into a controlled HTTP response. | Ch 13 |
| **exception handlers** | Spring lets you write a class of exception handlers, methods that catch a given exception type anywhere in the controllers and turn it into a response. | Ch 13 |
| **executable JAR** | The result is an executable JAR: one file that holds the app, its libraries and even the web server, so you start the whole application with a single command, with no separate server to install. | Ch 6 |
| **fast-forward** | Because `main` had no new commits of its own, Git could simply move `main` forward to the branch's commit, which is called a fast-forward. | Ch 7 |
| **fields** | The values an object holds are its fields. | Ch 4 |
| **filter chain** | Every request now passes through a filter chain, a row of checks that each request must pass in order, before it reaches a controller. | Ch 26 |
| **finally** | The finally block always runs, on success and on failure alike. | Ch 5 |
| **fixed delay** | A fixed delay waits that long after the last run finishes. | Ch 14 |
| **flags** | Every command has the same shape: the command name, then optional flags (also called options, which change how it behaves), then arguments (what to act on). | Ch 2 |
| **Flyway** | A tool that applies numbered SQL migration files to a database in order. | Ch 9 |
| **Flyway migration** | The file is the first Flyway migration: a numbered SQL script that Flyway runs once, in order, so every copy of the database gets the same shape. | Ch 26 |
| **folders** | Your files live in folders (also called directories) that nest inside each other. | Ch 2 |
| **for-each loop** | To visit every item, use the for-each loop. | Ch 5 |
| **foreign key** | A foreign key is a column that holds the primary key of a row in another table, creating a link. | Ch 9 |
| **form** | A form collects typed input. | Ch 23 |
| **framework** | A library that supplies the structure of an application and calls your code, rather than the reverse. | Ch 4 |
| **frontend** | The part of an application that runs in the user's browser. | Ch 1 |
| **generic** | The `<Document, String>` part is a generic: it says this repository handles `Document` objects whose identifier is a `String`. | Ch 4 |
| **getter** | You mark fields `private` (only this class can touch them) and offer getter methods to read them and, where change is allowed, setter methods to write them. | Ch 4 |
| **Git** | Git is the version control tool this project uses, and it is the most widely used one in the world. | Ch 7 |
| **Git Bash** | On Windows, install Git for Windows, which includes Git Bash, and use that for every command in this book. | Ch 2 |
| **Gradle** | Gradle describes the build in a script written in a programming language instead of XML, which gives more flexibility and can be faster on large projects. | Ch 6 |
| **guards** | The build budget in `angular.json` (Chapter 20) enforces a size limit on it. | Ch 23 |
| **hash** | It stores a hash: the output of a one-way function that turns a password into a fixed-length string that can't practically be reversed. | Ch 15 |
| **headers** | The top block is the request: a request line (method, path, protocol version), then headers, one per line. | Ch 8 |
| **health check** | The health check is the light, metrics are the gauges, and the audit log is the trip log. | Ch 35 |
| **Hibernate** | The standard Java specification is JPA (Jakarta Persistence); Hibernate is the implementation the project uses (Hibernate 7 with Spring Boot 4). | Ch 14 |
| **HMAC** | A keyed hash: a short code only someone holding the secret key can compute, used to detect tampering. | Ch 17 |
| **home folder** | Your home folder is where your personal files live, and `~` is shorthand for it. | Ch 2 |
| **HTTP** | The rules browsers and servers use to exchange requests and responses. | Ch 8 |
| **httpOnly** | The browser holds only a small cookie that points at it. | Ch 26 |
| **HTTPS** | HTTPS is HTTP inside an encrypted channel created by TLS (Transport Layer Security). | Ch 8 |
| **idempotent** | A method is idempotent if doing it twice has the same effect as once: `PUT` and `DELETE` are, since replacing a file twice with the same file, or deleting an already deleted document, leaves the same end state. | Ch 8 |
| **Identical answers** | `404` instead of `403`. A stranger cannot tell whether a private document exists. | Ch 32 |
| **if statement** | An if statement runs code only when a condition is true. | Ch 3 |
| **image (Docker)** | A read-only template from which containers are started. | Ch 10 |
| **immutable** | A field with no setter and set only in the constructor is immutable: it never changes after creation. | Ch 4 |
| **import** | To use a class from another package, you import it: `import java.util.List;` at the top of a file lets you write `List` instead of `java.util.List`. | Ch 4 |
| **index** | An index is a sorted lookup structure on one or more columns, like the index of a book: the database jumps straight to matching rows. | Ch 9 |
| **Inheritance** | Inheritance means a new type takes everything an existing one has and adds to it. | Ch 4 |
| **inputs** | Angular components normally receive data through inputs and report events through outputs. | Ch 21 |
| **install MySQL directly** | You could install MySQL directly on your computer. | Ch 10 |
| **Instant** | An Instant is a single point on the timeline, independent of time zones. | Ch 5 |
| **integrity** | TLS provides three things: encryption (eavesdroppers see noise), integrity (changes are detected) and authentication (a certificate proves you reached the real host). | Ch 8 |
| **interceptor** | An interceptor is a function that sees every request and response that passes through `HttpClient`. | Ch 22 |
| **interface (TypeScript)** | A description of the shape an object must have. | Ch 4 |
| **interpolation** | `{{ user.username }}` is interpolation: it inserts the value of an expression as text. | Ch 21 |
| **inversion of control** | The principle that a framework, not your code, creates objects and decides when to call them. | Ch 11 |
| **Jackson** | The Java library that converts between objects and JSON. | Ch 12 |
| **JAR** | A JAR (Java ARchive) is a zip file of compiled classes. | Ch 6 |
| **JavaScript** | The programming language that browsers run. | Ch 19 |
| **JDK** | The Java Development Kit: the compiler and tools needed to build Java programs. | Ch 3 |
| **jobs** | A workflow is a YAML file in `.github/workflows/`; it has jobs, each made of steps. | Ch 36 |
| **join** | A join combines rows from two tables in a query. | Ch 9 |
| **join table** | The database refuses a document whose owner does not exist. | Ch 9 |
| **JPA** | The standard Java specification is JPA (Jakarta Persistence); Hibernate is the implementation the project uses (Hibernate 7 with Spring Boot 4). | Ch 14 |
| **JRE** | The JRE (Java Runtime Environment) is the part needed only to run programs, without the compiler; you will meet it in the Dockerfile in Chapter | Ch 3 |
| **jsdom** | The jsdom package is a pretend browser written in JavaScript, so component specs can create elements and read `localStorage` without opening a real browser. | Ch 24 |
| **JSON** | A text format for structured data made of objects, lists, strings, numbers and booleans. | Ch 8 |
| **JSON array** | Two terms first: the query string is the part of a URL after the `?`, and a JSON array is a list written in square brackets. | Ch 12 |
| **JVM** | The Java Virtual Machine: the program that runs compiled Java. | Ch 3 |
| **keys** | Example: the users a document is shared with; sharing twice with the same user changes nothing. | Ch 5 |
| **lambda** | A lambda is a small unnamed method you can pass around. | Ch 5 |
| **layers** | An image is built in layers, each the result of one step; Docker stores each layer once and reuses it, which is why pulling a second image that shares layers is fast. | Ch 10 |
| **lazy loading** | The viewer reads it with `this.route.snapshot.paramMap.get('documentId')`. | Ch 23 |
| **library** | A library is a single tool you pick up when you need it, such as a PDF reader. | Ch 11 |
| **lifecycle hooks** | `OnInit` and `OnDestroy` are lifecycle hooks: methods (`ngOnInit`, `ngOnDestroy`) that Angular calls when the component appears and when it is removed. | Ch 21 |
| **line ending** | The mark for where a line stops: Windows uses two bytes (carriage return plus line feed), macOS and Linux use one (line feed). | Ch 2 |
| **list** | A list keeps items in order and allows duplicates. | Ch 5 |
| **listens** | A server process listens on a port: it asks the operating system to hand it any network request addressed to that number. | Ch 2 |
| **literal** | And a number written directly in code, such as `512`, is a literal. | Ch 3 |
| **localhost** | The name a computer uses for itself; requests to it never leave the machine. | Ch 2 |
| **lock** | A lock makes one wait for the other. | Ch 9 |
| **log** | A running server has no screen to show what it's doing, so it writes log lines to the console. | Ch 11 |
| **main method** | For now, every Java program lives inside one, and the name matches the file. | Ch 3 |
| **manifest** | It checks the session, looks up the page's grid in the manifest (the record of a document's title, page count and tile grid), and issues one signed token per tile, returning a grid of `/api/tiles?token=...` paths. | Ch 25 |
| **many-to-many** | The second kind, many-to-many, needs a third table that holds pairs. | Ch 9 |
| **map** | Example: the users a document is shared with; sharing twice with the same user changes nothing. | Ch 5 |
| **Maven** | Maven is the build tool for this project. | Ch 6 |
| **Maven Central** | Instead of copying its files into the project, you name it by its coordinates, and Maven downloads it from Maven Central, a public repository of Java libraries, into the cache you saw in Section 6.2. | Ch 6 |
| **merge** | When the work is good, you merge it back. | Ch 7 |
| **merge commit** | When both branches have new commits, Git creates a merge commit that has two parents and joins the lines of work. | Ch 7 |
| **merge conflict** | If two branches change the same lines of the same file, Git cannot decide which version wins, and it stops with a merge conflict. | Ch 7 |
| **method** | A named block of code that does one job and can be called by name. | Ch 3 |
| **method reference** | A method reference is a short form of a lambda that just calls one method: `AppUser::getUsername` means the same as `u -> u.getUsername()`. | Ch 5 |
| **metrics** | The health check is the light, metrics are the gauges, and the audit log is the trip log. | Ch 35 |
| **migration** | A versioned change to a database schema, applied once and in order. | Ch 9 |
| **MockMvc** | `@SpringBootTest` builds the full context; `@ActiveProfiles("test")` loads `application-test.yml` (H2 in MySQL mode, a test signing secret and a known bootstrap admin); and `@AutoConfigureMockMvc` provides MockMvc, a tool that sends requests to the controllers without opening a network port. | Ch 18 |
| **module (TypeScript)** | A file that exports some values and imports others. | Ch 19 |
| **multi-stage build** | This is a multi-stage build: the final image is smaller and contains no build tools or source code, so there is less to attack. | Ch 10 |
| **multipart** | The request format that carries files and form fields together, used for uploads. | Ch 8 |
| **MySQL** | This project uses MySQL 8.4, a relational database: one that stores data in tables and links them by keys. | Ch 9 |
| **naive viewer** | A viewer that only hides its download button in the browser, so anyone can still fetch the file. | Ch 1 |
| **network** | The database's data must live in a volume, or every restart would erase the accounts. | Ch 10 |
| **nginx** | A reverse proxy is a program that receives requests from browsers and passes them to the app behind it; nginx is the one this project uses, and Chapter 33 covers it. | Ch 32 |
| **No MFA** | The product owner accepted these defaults on September 19, 2026 and will revisit them using `sdv_tiles_rate_limited_total`. | Ch 32 |
| **No text layer** | It was added as friction, on purpose. | Ch 32 |
| **Node.js** | Node.js (Node for short) is a program that runs JavaScript outside a browser, on your own computer. | Ch 20 |
| **npm** | npm is Node's package manager: a tool that downloads libraries (packages) from a public registry (an online catalog of published packages, at npmjs.com) and puts them in a folder called `node_modules/`. | Ch 20 |
| **object** | An object is one thing built from that blueprint. | Ch 4 |
| **Observable** | An Observable stands for a *stream* of results over time: zero, one, or many values. | Ch 19 |
| **Only then parse** | Only then parse the fields and check the expiry. | Ch 25 |
| **open redirect** | This prevents an open redirect: an attacker could send a victim a link to the real sign-in page with `returnUrl` pointing at a lookalike site, and after signing in the victim would land on it. | Ch 23 |
| **OpenID Connect (OIDC)** | The reviewers suggested delegating sign-in to a real identity provider through OpenID Connect (OIDC), a standard that lets a service such as Google or Keycloak vouch for who a user is; this is often called single sign-on (SSO). | Ch 26 |
| **operator** | A symbol such as `+` or `==` that combines or compares values. | Ch 3 |
| **Operators** | Operators combine values: `+ | Ch 3 |
| **optimistic** | An optimistic approach lets both proceed and detects the conflict at save time. | Ch 14 |
| **Optional** | Optional is a box that is either empty or holds one value. | Ch 5 |
| **origin** | An origin is the combination of scheme, host and port. | Ch 8 |
| **ORM** | An ORM (object-relational mapper) does the translation from declarations you write once. | Ch 14 |
| **OSV** | OSV reads your dependency lists (`pom.xml`, `package-lock.json`) and checks every library against a database of published advisories. | Ch 36 |
| **outputs** | Angular components normally receive data through inputs and report events through outputs. | Ch 21 |
| **owner** | Each document has an owner and a visibility. | Ch 27 |
| **Packages** | Packages group them, like folders. | Ch 4 |
| **parameter** | A named input a method declares; the values passed in are its arguments. | Ch 3 |
| **parameters** | A method is named, reusable code that takes inputs (parameters) and can give back a result (a return value). | Ch 3 |
| **parent** | A parent POM supplies sensible defaults, and one of them matters a great deal: a tested list of matching library versions. | Ch 6 |
| **parts** | The body has several parts, each with its own headers, separated by a boundary marker. | Ch 8 |
| **passed by value** | Parameters are passed by value: the method receives a copy of a number, so changing the parameter inside the method does not change the caller's variable. | Ch 3 |
| **password hash** | An account is a stored record of who may sign in: a username, a password hash, a role. | Ch 26 |
| **path** | The address of a file or folder, written with slashes between the folder names. | Ch 2 |
| **PDF** | Portable Document Format: a file type that describes pages so they look the same everywhere. | Ch 1 |
| **Permissions-Policy** | Without the policy, following a link from a page could leak a token in the `Referer` header. | Ch 8 |
| **phase** | You just ran a phase of Maven's lifecycle, a named step in a fixed sequence. | Ch 6 |
| **phases** | Maven runs a fixed sequence of phases, and asking for a phase runs it and every phase before it. | Ch 6 |
| **pipe** | And the pipe `/` feeds one command's output into another's input, so small tools combine into bigger ones: | Ch 2 |
| **pixels** | A digital image is a grid of pixels, tiny colored squares. | Ch 17 |
| **Playwright** | Playwright drives a real browser (Chromium) against a real running stack: it clicks, types and reads what the page shows, as a person would. | Ch 24 |
| **plugins** | Maven does its work through plugins, small programs it runs at each step. | Ch 6 |
| **PNG** | PNG is a file format that stores such a grid compressed without losing detail, which suits text-heavy pages. | Ch 17 |
| **port** | A numbered door on your computer, from 0 to 65535, at which a server process listens for network requests addressed to that number. | Ch 2 |
| **primary key** | A primary key is a column (or set of columns) whose value identifies each row uniquely. | Ch 9 |
| **private** | The project's repository is `Sriram-Codes-SW/secure-doc-viewer` on GitHub, and it is private, so you can open it (and its *Pull requests* tab, Section 7.7) only if its owner has given your GitHub account access; ask the owner, or use the copy of the code you were given with this book. | Ch 7 |
| **process** | A running program; each one gets its own number. | Ch 2 |
| **profile (Spring)** | A named set of extra settings switched on for a purpose, such as the test profile that points the datasource at an in-memory H2 database. | Ch 11 |
| **profiles** | It uses profiles so that you start only what you need. | Ch 10 |
| **program** | A list of instructions a computer follows. | Ch 3 |
| **programming language** | People write those instructions as source code, text in a programming language. | Ch 3 |
| **Promise** | A Promise stands for one result that will arrive later, or fail. | Ch 19 |
| **prompt** | The text a shell shows to say it is ready for your next command. | Ch 2 |
| **Properties** | Properties are named values used elsewhere in the file or by the parent. | Ch 6 |
| **property binding** | And square brackets, as in `[routerLink]="[...]"`, are a property binding: the attribute's value is computed from an expression instead of being fixed text. | Ch 21 |
| **property placeholder** | A `${NAME:default}` marker in configuration that Spring replaces with an environment or file value. | Ch 11 |
| **protocol** | A protocol is an agreed format for a conversation, like the rules of a phone call: who speaks first, how you say goodbye. | Ch 8 |
| **proxy** | A proxy is a program that receives a request on behalf of another server and forwards it. | Ch 8 |
| **publisher** | `reader.one` is a reader. | Ch 1 |
| **pull request** | On GitHub, a pull request (PR) is a proposal to merge one branch into another. | Ch 7 |
| **query method** | Each extra method is a query method: Spring reads its name and writes the SQL. | Ch 14 |
| **query string** | Two terms first: the query string is the part of a URL after the `?`, and a JSON array is a list written in square brackets. | Ch 12 |
| **rasterize** | To turn a page description (such as a PDF page) into a grid of pixels. | Ch 1 |
| **rasterizing** | Turning a PDF page into a `BufferedImage` is called rasterizing. | Ch 17 |
| **rate limit** | A cap on how many requests a user may make in a period. | Ch 1 |
| **reader** | A reader can open the documents they have access to, and nothing else. | Ch 1 |
| **Recompute** | Recompute the signature from the decoded payload with the server's secret and compare it with the one supplied. | Ch 25 |
| **record (Java)** | A one-line way to declare a class that only carries data; Java generates the constructor and read methods. | Ch 4 |
| **redirection** | The `>` above is redirection: sending a command's output somewhere other than the screen. | Ch 2 |
| **Referrer-Policy: no-referrer** | If an attacker ever got script into a response, the browser would refuse to run it. | Ch 8 |
| **registry** | Docker fetches images from a registry, a public store; Docker Hub is the default. | Ch 10 |
| **relational database** | This project uses MySQL 8.4, a relational database: one that stores data in tables and links them by keys. | Ch 9 |
| **relative** | A path is absolute if it starts from the top (`/c/dev/...`) and relative if it starts from where you are (`src/main`). | Ch 2 |
| **relaxed binding** | Spring's rule that `signing-secret`, `SIGNING_SECRET` and `signingSecret` all name the same setting. | Ch 11 |
| **remote** | A remote is a copy of the repository on another computer, usually GitHub. | Ch 7 |
| **repository** | A Git project is a repository: your files plus a hidden folder named `.git` that holds the whole history. | Ch 7 |
| **reproducible build** | Everything in this chapter serves one goal: a reproducible build, where the same source code produces the same result on any machine and on any day. | Ch 6 |
| **request** | The conversation is always the same shape: the client sends a request, and the server sends back one response. | Ch 8 |
| **request line** | The top block is the request: a request line (method, path, protocol version), then headers, one per line. | Ch 8 |
| **reserved word** | The migration's own comment says why: "Column names avoid ROWS, which is reserved in MySQL 8." A reserved word is a word SQL already uses, so it cannot be used as a plain name. | Ch 9 |
| **resource** | A thing a REST API exposes at a URL, such as a document. | Ch 8 |
| **response** | The conversation is always the same shape: the client sends a request, and the server sends back one response. | Ch 8 |
| **REST** | A style of web API built around URLs for things and HTTP methods for actions. | Ch 8 |
| **return value** | The result a method hands back to the code that called it. | Ch 3 |
| **reverse proxy** | A reverse proxy is a program that receives requests from browsers and passes them to the app behind it; nginx is the one this project uses, and Chapter 33 covers it. | Ch 32 |
| **Right-click blocking** | The product owner accepted these defaults on September 19, 2026 and will revisit them using `sdv_tiles_rate_limited_total`. | Ch 32 |
| **role** | A role is a named set of permissions attached to an account. | Ch 15 |
| **roles** | The app calls them roles, and there are three. | Ch 1 |
| **rolls back** | A transaction groups several database changes so that either all succeed or none do: it either commits (makes them all permanent) or rolls back (undoes everything it did). | Ch 14 |
| **route guard** | A route guard is a function that Angular runs before entering a route. | Ch 23 |
| **route parameter** | `pathMatch: 'full'` means "only when the path is exactly empty". | Ch 23 |
| **router** | When you click a link, Angular's router changes the address in the address bar, swaps which component is shown in the `<router-outlet />` (Chapter 21), and never reloads the browser. | Ch 23 |
| **row lock** | The project asks the database for a row lock when replacing or deleting a document: | Ch 9 |
| **rows** | A table is like a spreadsheet: it has named columns (the kinds of facts) and rows (one entry each). | Ch 9 |
| **runtime error** | A runtime error happens while the program runs. | Ch 3 |
| **safe** | A method is safe if it does not change anything on the server: `GET` is safe, so a browser may repeat it, cache it or prefetch it freely. | Ch 8 |
| **salt** | A salt is random data mixed in per password, so identical passwords hash differently. | Ch 15 |
| **same-origin policy** | Browsers apply the same-origin policy: a script loaded from one origin may not freely read responses from another. | Ch 8 |
| **SameSite=Strict** | The browser holds only a small cookie that points at it. | Ch 26 |
| **schema** | A schema is the set of tables and columns. | Ch 9 |
| **scope** | A scope says when a dependency is needed. | Ch 6 |
| **semantic versioning** | This is semantic versioning. | Ch 20 |
| **semaphore** | `TileWorkLimiter` caps work across everyone with a semaphore, a counter of permits: a task must take a permit to run and returns it when done. | Ch 17 |
| **serialization** | Turning an object into text or bytes, such as JSON, so it can be sent or stored. | Ch 12 |
| **Serve over HTTPS** | Serve over HTTPS with the `tls` profile, `SITE_ADDRESS` set to your domain, `TLS_MODE` set to an email address, ports 80 and 443 published, and `SESSION_COOKIE_SECURE=true`. | Ch 33 |
| **server** | The program that answers requests from clients; here, the Spring Boot app. | Ch 1 |
| **service** | Instead the code goes into a service: a plain class, marked `@Injectable`, that Angular creates once and hands to whoever asks. | Ch 22 |
| **service name** | Inside the Compose network, each service is reachable by its service name, so the backend finds the database at the host name `mysql`, not `localhost`. | Ch 10 |
| **servlet filter** | A servlet filter is a piece of code that every request passes through before it reaches a controller, and every response passes back through. | Ch 15 |
| **session** | The server's record that a particular browser has proven who it is, so the browser does not have to send the password again with every request. | Ch 1 |
| **session fixation** | Please sign in again.` Sign-in also changes the session id (`changeSessionId`), which defeats session fixation, where an attacker plants a known id before you sign in. | Ch 16 |
| **Session fixation protection** | Session fixation protection (`changeSessionId`) gives you a new session id at sign-in, so an attacker who planted an id before sign-in gains nothing. | Ch 26 |
| **set** | Example: the documents on a library page, newest first. | Ch 5 |
| **setter** | You mark fields `private` (only this class can touch them) and offer getter methods to read them and, where change is allowed, setter methods to write them. | Ch 4 |
| **shell** | The program inside a terminal that reads your commands and runs them (for example Bash or PowerShell). | Ch 2 |
| **signal** | Angular's answer, used throughout this project, is the signal: a box that holds a value and remembers who has read it. | Ch 21 |
| **signature** | A value computed from data and a secret key that proves the data was not changed. | Ch 1 |
| **signed token** | The project's signed token is the message plus its fingerprint. | Ch 17 |
| **signed URL** | A URL carrying a signature the server can verify, so it cannot be altered or forged. | Ch 1 |
| **single-page application** | A single-page application (SPA) loads one HTML document once. | Ch 23 |
| **six-character trace code** | Each mark now also carries a six-character trace code that identifies the exact session. | Ch 29 |
| **source code** | The human-readable text of a program, before it is compiled. | Ch 3 |
| **Spring Boot** | Spring is the framework the project uses, and Spring Boot is a layer on top of it that chooses sensible defaults so a program can start with almost no setup. | Ch 11 |
| **Spring Data JPA** | Spring Data JPA sits on top and writes the common queries for you. | Ch 14 |
| **Spring Security** | Spring Security is the framework module that does both jobs. | Ch 15 |
| **SQL** | The language used to query and change a relational database. | Ch 9 |
| **stack trace** | The list of method calls that were active when an exception occurred, printed to help find the cause. | Ch 3 |
| **stacked** | Pull requests 1 to 4 were stacked: each branch started from the previous one, and all four were merged within about a minute of each other. | Ch 7 |
| **staging area** | Git has three places where your work can be: the working folder (the files you edit), the staging area (a list of changes you have chosen to record next) and the repository (the saved history). | Ch 7 |
| **starter (Spring Boot)** | A single dependency that pulls in a matched set of libraries for one job, for example `spring-boot-starter-security`. | Ch 6 |
| **static factory method** | `Viewer.of(...)` is a static factory method: called on the type, not on an object, and it returns a new `Viewer`. | Ch 4 |
| **static methods** | Records can also have static methods, which belong to the type and not to an object. | Ch 4 |
| **statically typed** | Java is statically typed: every variable has a type that says what kind of value it holds, and the compiler checks that you use it correctly. | Ch 3 |
| **status code** | The response's status code is a three-digit number telling the client what happened. | Ch 8 |
| **status line** | The bottom is the response: a status line, headers, an empty line, and the body. | Ch 8 |
| **Stay signed in** | In the last five minutes a countdown banner with Stay signed in appears. | Ch 29 |
| **steps** | A workflow is a YAML file in `.github/workflows/`; it has jobs, each made of steps. | Ch 36 |
| **Storage janitor** | Storage janitor (`StorageJanitor`): removes tile directories that no document points to, including superseded versions. | Ch 34 |
| **stream** | A stream is a pipeline that processes a collection step by step: filter some items, transform others, collect a result. | Ch 5 |
| **string** | A sequence of characters; text. | Ch 3 |
| **stylesheet** | A component has three parts: a class (the data and actions, in TypeScript), a template (the HTML that shows them; HTML is the markup language that describes the structure of a web page, such as headings, buttons and links), and a stylesheet (CSS, the language that sets colors, spacing and layout, that decorates them). | Ch 21 |
| **supply chain** | The libraries you depend on are your supply chain: code written by others, running with your app's privileges. | Ch 6 |
| **Supply-chain risk** | Supply-chain risk is the chance that one of them is flawed or malicious. | Ch 36 |
| **switch** | When you are choosing among many fixed values, a switch reads better than a chain of `if`. | Ch 3 |
| **table** | A table is like a spreadsheet: it has named columns (the kinds of facts) and rows (one entry each). | Ch 9 |
| **tag** | A tag is a permanent name for one commit, like a bookmark. | Ch 7 |
| **template** | A component has three parts: a class (the data and actions, in TypeScript), a template (the HTML that shows them; HTML is the markup language that describes the structure of a web page, such as headings, buttons and links), and a stylesheet (CSS, the language that sets colors, spacing and layout, that decorates them). | Ch 21 |
| **terminal** | A window in which you type commands to the computer instead of clicking. | Ch 2 |
| **test** | A test is code that checks other code. | Ch 3 |
| **test pyramid** | The test pyramid describes a healthy mix: many small, fast tests of single methods at the base; fewer tests that start part of the application in the middle; and very few slow tests of the whole system at the top. | Ch 18 |
| **TestBed** | Angular's TestBed builds a small test-only application: you tell it which providers to use, ask it for objects, and it wires dependencies as in the real app. | Ch 24 |
| **Testcontainers** | Testcontainers starts a real MySQL in Docker for the test and discards it afterward. | Ch 18 |
| **text editor** | You need one more tool: a text editor, a program for changing text files. | Ch 2 |
| **text file** | A file that holds characters (rather than, say, an image). | Ch 2 |
| **thread** | A web server handles many requests at the same time, each on its own thread: a path of execution inside the program. | Ch 5 |
| **threat** | A threat is a specific way someone might misuse the system; a defense is what stands in the way. | Ch 1 |
| **threat model** | A threat model is that exercise, done on paper first and then with real tests. | Ch 32 |
| **tile** | One small square piece of a rendered page image. | Ch 1 |
| **tile binding** | The tile binding goes into signed tile URLs. | Ch 26 |
| **tiles** | Slice. The server cuts each page image into square tiles of 512 by 512 pixels (a pixel is one dot of the image). | Ch 1 |
| **TLS** | The protocol that encrypts traffic between browser and server (the "S" in HTTPS). | Ch 8 |
| **token** | A string that stands for a right to do something; here, a signed tile request. | Ch 3 |
| **trace filter** | The admin audit gains a trace filter, so a leaked screenshot can be matched to one specific sign-in instead of only a username. | Ch 29 |
| **transaction** | A transaction groups several statements so they all succeed or none do. | Ch 9 |
| **transitive dependencies** | These are transitive dependencies: the ones you did not list, but that arrive because something you listed needs them. | Ch 6 |
| **Trivy** | It fails the build on any published advisory. | Ch 36 |
| **type** | A label saying what kind of value something is (text, number, list, a custom shape). | Ch 3 |
| **types** | TypeScript is JavaScript with a layer of labels added, called types. | Ch 19 |
| **TypeScript** | JavaScript plus a type system, checked by a compiler before the code runs. | Ch 19 |
| **unchecked** | Those that extend `RuntimeException` are unchecked: the compiler does not force you to catch them. | Ch 5 |
| **union type** | A type that allows one of several listed alternatives. | Ch 19 |
| **unit test** | A unit test runs a small piece of code in isolation and checks its result. | Ch 24 |
| **unrecognised devices** | account plus address: 5 failures, always applies; | Ch 30 |
| **Untracked** | Untracked means Git can see the file but is not recording it. | Ch 7 |
| **URL** | Signed, short-lived tile URLs. A URL (Uniform Resource Locator) is a web address such as `https://example.com/page`. | Ch 1 |
| **UTC** | UTC (Coordinated Universal Time) is the world's reference time, with no daylight-saving changes. | Ch 5 |
| **UX only** | The doc comment says it plainly: UX only. | Ch 23 |
| **values** | Example: the users a document is shared with; sharing twice with the same user changes nothing. | Ch 5 |
| **variable** | A named place that holds a value. | Ch 3 |
| **Version control** | Version control is a tool that records every change to a project's files so that you can go back to any earlier state; Git is the one this project uses, and Chapter 7 teaches it. | Ch 2 |
| **virtual machine** | Compare this with a virtual machine, which pretends to be a whole computer and boots a whole operating system inside your computer. | Ch 10 |
| **visibility** | Each document has an owner and a visibility: either `PRIVATE` (only the owner, administrators and the users the owner shared it with) or `EVERYONE` (any signed-in user). | Ch 1 |
| **Vitest** | Vitest is the test runner: it finds files ending in `.spec.ts`, runs them, and reports which checks passed. | Ch 24 |
| **volume** | You can start many containers from one image. | Ch 10 |
| **watermark** | Text or a mark drawn onto content to identify who received it. | Ch 1 |
| **When you'd switch** | Every decision below uses the same six headings: The decision, What the project chose, Pros, Cons, The enterprise alternative, and When you'd switch. | Ch 37 |
| **workflow** | A workflow is a YAML file in `.github/workflows/`; it has jobs, each made of steps. | Ch 36 |
| **working directory** | The folder a shell (or a program) is currently "in"; relative paths are read from here. | Ch 2 |
| **working folder** | Git has three places where your work can be: the working folder (the files you edit), the staging area (a list of changes you have chosen to record next) and the repository (the saved history). | Ch 7 |
| **wrapper** | The project therefore ships a wrapper: small scripts, `mvnw` (macOS, Linux and Git Bash) and `mvnw.cmd` (Windows), that download and run one exact Maven version. | Ch 6 |
| **XML** | A `pom.xml` is written in XML, a format where information sits between named tags: `<name>secure-doc-viewer</name>` means "the name is secure-doc-viewer". | Ch 6 |

Where a term is defined in more than one chapter, the earliest chapter is listed; the chapter text is authoritative and the editor reconciles differences.
