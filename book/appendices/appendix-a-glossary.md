# Appendix A: Glossary

Definitions of terms bolded at first use. Generated from `book/GLOSSARY.md`.

| Term | Definition | First defined |
|---|---|---|
| **absolute lifetime** | `SessionLifetimeFilter` adds an absolute lifetime: a session ends a fixed time after sign-in, however active it is. | Ch 16 |
| **Angular CLI** | The Angular CLI (command line interface) is the `ng` program. | Ch 20 |
| **annotation (Java)** | A marker such as `@Service` placed on code that tells a framework or the compiler something about it. | Ch 4, used in Ch 11 |
| **application context** | The box that holds all of the app's beans. | Ch 11 |
| **architectural pattern** | An architectural pattern is like the plan of the city: where the roads run, where the gates are, which districts may talk to which, and where the water comes in. | Ch 39 |
| **arrow function** | A short way to write a function in JavaScript and TypeScript, using `=>`. | Ch 19 |
| **Assertions** | Assertions are the methods that do the throwing. | Ch 18 |
| **atomic** | An atomic step is one that cannot be interrupted halfway: it happens completely or not at all, and no other thread can see it half done. | Ch 5 |
| **audit log** | The health check (Chapter 30) is the light, metrics are the gauges, and the audit log is the trip log. | Ch 35 |
| **audit trail** | An append-only record, kept in the database, of important actions such as sign-ins, uploads, views and denied requests. | Ch 1 |
| **authentication** | Proving who you are, for example with a username and password. | Ch 1 |
| **authorization** | Deciding what an authenticated person is allowed to do. | Ch 1 |
| **auto-configuration** | Spring Boot's habit of setting up sensible defaults for the libraries it finds on the classpath. | Ch 11 |
| **axe-core** | axe-core is an engine that inspects a rendered page against the Web Content Accessibility Guidelines (Chapter 21) and reports violations such as low-contrast text, form fields with no label, or missing names on buttons. | Ch 24 |
| **backend** | The part of an application that runs on a server and holds the data and rules. | Ch 1 |
| **BCrypt** | BCrypt is a hash function built for passwords. | Ch 15 |
| **bean** | An object Spring creates and manages, from a class labeled with annotations such as `@Component`, `@Service` or `@RestController`. | Ch 11 |
| **Bean Validation** | The Java standard for declaring input rules with annotations such as `@NotBlank`. | Ch 13 |
| **branch** | A branch is a movable name for a line of work. | Ch 7 |
| **browser** | The program (Chrome, Firefox, Safari, Edge) that requests web pages and runs their code. | Ch 19 |
| **build** | Together these steps are called a build. | Ch 6 |
| **build tool** | A program that compiles code, runs tests and packages the result; Maven for this project's Java. | Ch 6 |
| **bytecode** | The compact instructions the Java compiler produces, which the JVM runs. | Ch 3 |
| **cache** | A place where a copy of something is kept so it can be served again faster. | Ch 1 |
| **canonical string** | The canonical string is the seven fields joined in a fixed order with `/` between them. | Ch 17 |
| **capability URL** | The signed URL is an example of a capability URL: possessing the URL is what grants the right to fetch that one thing. | Ch 39 |
| **certificate** | A certificate is a signed statement, issued by an authority the browser trusts, that a public key belongs to a given domain. | Ch 8 |
| **client** | A program that asks for things over a network; your browser is a client. | Ch 1 |
| **code review** | Code review is the practice of having someone else read the change before it lands. | Ch 7 |
| **collection** | A collection is an object that holds many values. | Ch 5 |
| **columns** | A table is like a spreadsheet: it has named columns (the kinds of facts) and rows (one entry each). | Ch 9 |
| **commit** | Each saved state is a commit: a snapshot of every tracked file at one moment, with a message describing the change and a unique identifier such as `2d10e07`. | Ch 7 |
| **compiler** | A program that checks source code and translates it into a form a machine can run. | Ch 3 |
| **compiler error** | A compiler error stops the build before anything runs. | Ch 3 |
| **component scanning** | Spring searching your packages for annotated classes and registering them as beans. | Ch 11 |
| **composite primary key** | The composite primary key `(document_id, user_id)` means the same user can't be shared the same document twice. | Ch 14 |
| **composition** | For most other cases, the project prefers composition: a class *has* another object and uses it, as `RequestActors` has a `SessionKeys`. | Ch 4 |
| **computed** | A computed signal is a value derived from others; it is recalculated only when a signal it read has changed, and is otherwise cached. | Ch 21 |
| **conditional expression** | The condition `cond ? a : b` is the conditional expression, the same as in Java: "if `cond`, then `a`, otherwise `b`". | Ch 19 |
| **constructor** | A constructor is special code that runs when you create an object with `new`, and sets up its fields. | Ch 4 |
| **constructor injection** | Dependency injection where an object receives its dependencies as constructor parameters. | Ch 4 |
| **container** | A running, isolated instance of an image: a program packaged with everything it needs. | Ch 10 |
| **Content Security Policy (CSP)** | A Content Security Policy (CSP) tells the browser what a response is allowed to load or do. | Ch 16 |
| **contrast** | Contrast is the difference in brightness between text and its background, measured as a ratio. | Ch 21 |
| **controller** | A controller is a class that answers web requests; Chapter 12 is about them. | Ch 11 |
| **convention over configuration** | Maven relies on convention over configuration: if you put files where it expects them, it needs no instructions. | Ch 6 |
| **cookie** | A small piece of data a server asks the browser to store and send back with later requests. | Ch 8 |
| **cron expression** | A cron expression lists second, minute, hour, day of month, month and weekday: `0 30 3 * * *` means "second 0 of minute 30 of hour 3, every day". | Ch 14 |
| **CSRF (Cross-Site Request Forgery)** | An attack in which another website makes your browser send a request to a site you are signed in to. | Ch 26 |
| **CSV injection** | An attack in which a spreadsheet cell that starts with `=`, `+`, `-` or `@` is run as a formula when an exported CSV file is opened. | Ch 27 |
| **decompression bomb** | A small file that expands to an enormous amount of data (or pixels) when processed, exhausting memory. | Ch 13 |
| **deep link** | An address that leads straight to a specific state, for example the viewer at page 3 of a document. | Ch 23 |
| **defense** | A threat is a specific way someone might misuse the system; a defense is what stands in the way. | Ch 1 |
| **denormalized** | Denormalized means storing a copy of information instead of a link. | Ch 9 |
| **deny by default** | The last line, `anyRequest().denyAll()`, is a deny by default stance: a route you forget to list is closed, not open. | Ch 26 |
| **Dependabot** | Dependabot is a GitHub service that reads your dependency files and opens a pull request when a newer version exists. | Ch 31 |
| **dependency** | A dependency is a library your code uses. | Ch 6 |
| **dependency injection** | Giving an object the other objects it needs instead of letting it create them itself. | Ch 4 |
| **deserialization** | Turning text or bytes, such as JSON, back into an object. | Ch 12 |
| **design pattern** | A design pattern is the software version: a named, reusable solution to a problem that recurs in code. | Ch 38 |
| **deterrent** | The design is a deterrent, not a vault: anyone who can see pixels can photograph a screen. | Ch 25 |
| **developer tools** | The inspection panel built into browsers that shows requests, code and errors. | Ch 1 |
| **diamond** | First, when you write `new HashMap<>()` the empty angle brackets, called the diamond, tell the compiler to copy the types from the left side, so you write them once. | Ch 5 |
| **digest** | `image: mysql:8.4@sha256:...` names the image, its tag, and a digest: a fingerprint of the exact image contents. | Ch 10 |
| **discriminated union** | A TypeScript union of object types that each carry a fixed tag field, so code can tell them apart. | Ch 19 |
| **Docker** | Docker solves this by running programs in containers: isolated processes that bring their own files and settings. | Ch 10 |
| **Docker Compose** | Docker Compose describes one or more containers in a file, `docker-compose.yml`, and starts them with one command. | Ch 10 |
| **Dockerfile** | For the backend, the project builds its own from a Dockerfile: a recipe of steps. | Ch 10 |
| **double-submit cookie** | This is the double-submit cookie pattern: the same value arrives twice, once in a cookie the browser sends automatically and once in a header that only the real app can write. | Ch 16 |
| **DPI** | Dots per inch: how many pixels represent one inch of a page when it is rendered as an image. | Ch 1 |
| **encapsulation** | Encapsulation means hiding a class's fields and exposing only the operations you intend. | Ch 4 |
| **encoding** | The rule that maps characters to bytes; the project's files use UTF-8. | Ch 2 |
| **entity** | A Java class whose objects are stored as rows in a database table. | Ch 14 |
| **environment variable** | A named setting, such as `DB_PASSWORD`, that the operating system hands to every program it starts. | Ch 2 |
| **event log** | An event log is an ordered record of things that happened, added to and never edited. | Ch 39 |
| **exception** | An object that signals something went wrong and interrupts normal flow until it is handled. | Ch 3 |
| **executable JAR** | The result is an executable JAR: one file that holds the app, its libraries and even the web server, so you start the whole application with a single command, with no separate server to install. | Ch 6 |
| **exponential backoff** | The sleep is an exponential backoff: `50L << Math.min(attempt, 4)` shifts the number 50 left by the attempt number, capped at 4, so the waits are 50, 100, 200, 400 milliseconds, then 800 for the remaining tries. | Ch 27 |
| **factory method** | A factory method is a static method that builds an object, so callers don't need to know how. | Ch 38 |
| **fast-forward** | Because `main` had no new commits of its own, Git could simply move `main` forward to the branch's commit, which is called a fast-forward. | Ch 7 |
| **finally** | The finally block always runs, on success and on failure alike. | Ch 5 |
| **fixture** | `DOC` is a fixture: a realistic sample of the data the API would return, typed with the same `DocumentDetail` interface the app uses (Chapter 19). | Ch 24 |
| **Flexbox** | Flexbox (`display: flex`) lays children out in a row and lets you align and space them. | Ch 21 |
| **fluent interface** | A fluent interface is a builder whose methods return the object itself, so calls chain into one readable sentence. | Ch 38 |
| **Flyway** | A tool that applies numbered SQL migration files to a database in order. | Ch 9 |
| **Flyway migration** | The file is the first Flyway migration: a numbered SQL script that Flyway runs once, in order, so every copy of the database gets the same shape. | Ch 26 |
| **foreign key** | A foreign key is a column that holds the primary key of a row in another table, creating a link. | Ch 9 |
| **framework** | A library that supplies the structure of an application and calls your code, rather than the reverse. | Ch 4 |
| **frontend** | The part of an application that runs in the user's browser. | Ch 1 |
| **gateway** | Put a reverse proxy (Chapter 16), sometimes called a gateway when it does more, at the edge. | Ch 39 |
| **generic** | The `<Document, String>` part is a generic: it says this repository handles `Document` objects whose identifier is a `String`. | Ch 4 |
| **getter** | You mark fields `private` (only this class can touch them) and offer getter methods to read them and, where change is allowed, setter methods to write them. | Ch 4 |
| **Git** | Git is the version control tool this project uses, and it is the most widely used one in the world. | Ch 7 |
| **Gradle** | Gradle describes the build in a script written in a programming language instead of XML, which gives more flexibility and can be faster on large projects. | Ch 6 |
| **hash** | Instead the database stores a hash: the output of a one-way function that turns a password into a fixed-length string of characters. "One-way" means that given the hash you can't practically get back the password. | Ch 15 |
| **Hibernate** | Hibernate is the implementation the project uses (Hibernate 7 with Spring Boot 4). | Ch 14 |
| **HMAC** | A keyed hash: a short code only someone holding the secret key can compute, used to detect tampering. | Ch 15 |
| **home folder** | Your home folder is where your personal files live, and `~` is shorthand for it. | Ch 2 |
| **host** | Your own computer, which runs Docker, is called the host. | Ch 10 |
| **HSTS** | Caddy terminates TLS (it holds the certificate and speaks HTTPS to browsers) and adds an HSTS header, which tells browsers to use only HTTPS for this site from then on. | Ch 30 |
| **HTTP** | The rules browsers and servers use to exchange requests and responses. | Ch 8 |
| **if statement** | An if statement runs code only when a condition is true. | Ch 3 |
| **ignore rule** | Each block is an ignore rule: it names a dependency and says which versions or kinds of update Dependabot must not propose. | Ch 31 |
| **image (Docker)** | A read-only template from which containers are started. | Ch 10 |
| **index** | An index is a sorted lookup structure on one or more columns, like the index of a book: the database jumps straight to matching rows. | Ch 9 |
| **inheritance** | Inheritance means a new type takes everything an existing one has and adds to it. | Ch 4 |
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
| **join** | A join combines rows from two tables in a query. | Ch 9 |
| **JRE** | The JRE (Java Runtime Environment) is the part needed only to run programs, without the compiler; you will meet it in the Dockerfile in Chapter | Ch 3 |
| **jsdom** | The jsdom package is a pretend browser written in JavaScript, so component specs can create elements and read `localStorage` without opening a real browser. | Ch 24 |
| **JSON** | A text format for structured data made of objects, lists, strings, numbers and booleans. | Ch 8 |
| **JVM** | The Java Virtual Machine: the program that runs compiled Java. | Ch 3 |
| **lambda** | A lambda is a small unnamed method you can pass around. | Ch 5 |
| **latch** | A latch is a counter starting at 1: `await()` blocks until the count reaches zero, and `countDown()` lowers it, releasing everyone at once. | Ch 18 |
| **least privilege** | This is the principle of least privilege: give a process only what it needs. | Ch 36 |
| **library** | A library is a single tool you pick up when you need it, such as a PDF reader. | Ch 11 |
| **lifecycle hooks** | `OnInit` and `OnDestroy` are lifecycle hooks: methods (`ngOnInit`, `ngOnDestroy`) that Angular calls when the component appears and when it is removed. | Ch 21 |
| **line ending** | The mark for where a line stops: Windows uses two bytes (carriage return plus line feed), macOS and Linux use one (line feed). | Ch 2 |
| **list** | A list keeps items in order and allows duplicates. | Ch 5 |
| **literal** | And a number written directly in code, such as `512`, is a literal. | Ch 3 |
| **localhost** | The name a computer uses for itself; requests to it never leave the machine. | Ch 2 |
| **LTS** | The same file has a rule for container images: it ignores Node's non-LTS lines (LTS, "long-term support", marks the releases that receive fixes for years; the file's comment says odd-numbered Node releases never become LTS, and it skips Node 25, 27 and 29). | Ch 20 |
| **magic number** | This is a magic number check: many file formats begin with a short recognizable marker. | Ch 28 |
| **many-to-many** | The second kind, many-to-many, needs a third table that holds pairs. | Ch 9 |
| **Maven** | Maven is the build tool for this project. | Ch 6 |
| **Maven wrapper** | The Maven wrapper (`mvnw`) is a small script committed to the repository that downloads the exact Maven version the project uses, so every developer and every CI run builds the same way without installing Maven themselves. | Ch 30 |
| **method** | A named block of code that does one job and can be called by name. | Ch 3 |
| **method reference** | A method reference is a short form of a lambda that just calls one method: `AppUser::getUsername` means the same as `u -> u.getUsername()`. | Ch 5 |
| **microservices** | The opposite extreme is microservices, where each module is its own deployable that talks to the others over the network. | Ch 39 |
| **migration** | A versioned change to a database schema, applied once and in order. | Ch 9 |
| **modular monolith** | A modular monolith is a single deployable whose inside is divided into modules with clear responsibilities and limited knowledge of one another. | Ch 39 |
| **module (TypeScript)** | A file that exports some values and imports others. | Ch 19 |
| **multi-stage build** | This is a multi-stage build: the final image is smaller and contains no build tools or source code, so there is less to attack. | Ch 10 |
| **multipart** | The request format that carries files and form fields together, used for uploads. | Ch 12 |
| **naive viewer** | A viewer that only hides its download button in the browser, so anyone can still fetch the file. | Ch 1 |
| **Node.js** | Node.js (Node for short) is a program that runs JavaScript outside a browser, on your own computer. | Ch 20 |
| **npm** | npm is Node's package manager: a tool that downloads libraries (packages) from a public registry (an online catalog of published packages, at npmjs.com) and puts them in a folder called `node_modules/`. | Ch 20 |
| **object** | An object is one thing built from that blueprint. | Ch 4 |
| **open redirect** | The classic attack is an open redirect: a site that, after sign-in, sends the browser to whatever address the link names, so a genuine sign-in page can be used to send a victim on to a lookalike site. | Ch 23 |
| **operator** | A symbol such as `+` or `==` that combines or compares values. | Ch 3 |
| **Optional** | Optional is a box that is either empty or holds one value. | Ch 5 |
| **origin** | An origin is the combination of scheme, host and port. | Ch 8 |
| **OSV** | OSV ("Open Source Vulnerabilities") reads your dependency lists and checks every library and version against a database of published advisories. | Ch 36 |
| **owner** | The owner is the account that uploaded the document. | Ch 27 |
| **parameter** | A named input a method declares; the values passed in are its arguments. | Ch 3 |
| **passed by value** | Parameters are passed by value: the method receives a copy of a number, so changing the parameter inside the method does not change the caller's variable. | Ch 3 |
| **path** | The address of a file or folder, written with slashes between the folder names. | Ch 2 |
| **pattern-itis** | A beginner who has just learned a pattern is tempted to use it everywhere, a habit sometimes called pattern-itis. | Ch 38 |
| **PDF** | Portable Document Format: a file type that describes pages so they look the same everywhere. | Ch 1 |
| **Playwright** | Playwright drives a real browser (Chromium) against a real running stack: it clicks, types and reads what the page shows, as a person would. | Ch 24 |
| **PNG** | PNG is a file format that stores such a grid compressed *without losing detail*, which suits pages full of text and sharp lines. | Ch 17 |
| **port** | A numbered door on your computer, from 0 to 65535, at which a server process listens for network requests addressed to that number. | Ch 2 |
| **primary key** | A primary key is a column (or set of columns) whose value identifies each row uniquely. | Ch 9 |
| **process** | A running program; each one gets its own number. | Ch 2 |
| **profile (Spring)** | A named set of extra settings switched on for a purpose, such as the test profile that points the datasource at an in-memory H2 database. | Ch 11 |
| **program** | A list of instructions a computer follows. | Ch 3 |
| **Promise** | A Promise stands for one result that will arrive later, or fail. | Ch 19 |
| **prompt** | The text a shell shows to say it is ready for your next command. | Ch 2 |
| **property binding** | And square brackets, as in `[routerLink]="[...]"`, are a property binding: the attribute's value is computed from an expression instead of being fixed text. | Ch 21 |
| **property placeholder** | A `${NAME:default}` marker in configuration that Spring replaces with an environment or file value. | Ch 11 |
| **protocol** | A protocol is an agreed format for a conversation, like the rules of a phone call: who speaks first, how you say goodbye. | Ch 8 |
| **proxy** | A proxy is a program that receives a request on behalf of another server and forwards it. | Ch 8 |
| **pull request** | On GitHub, a pull request (PR) is a proposal to merge one branch into another. | Ch 7 |
| **query method** | Each extra method is a query method: Spring reads the method's *name* and writes the SQL. | Ch 14 |
| **query parameters** | Web addresses can carry small pieces of data after a question mark, called query parameters. | Ch 29 |
| **race condition** | This is a race condition: the result depends on the timing of things that happen at once. | Ch 16 |
| **rasterize** | To turn a page description (such as a PDF page) into a grid of pixels. | Ch 1 |
| **rate** | What you want is a rate, how fast the counter rises. | Ch 35 |
| **rate limit** | A cap on how many requests a user may make in a period. | Ch 1 |
| **ratio** | You can also divide one series by another to get a ratio, which is often the most useful signal: | Ch 35 |
| **record (Java)** | A one-line way to declare a class that only carries data; Java generates the constructor and read methods. | Ch 4 |
| **RED** | A common way to watch a service is by three questions, known as RED: the Rate of requests, the Errors, and the Duration. | Ch 39 |
| **redirection** | The `>` above is redirection: sending a command's output somewhere other than the screen. | Ch 2 |
| **regression test** | A regression test is a test written so that a fixed bug cannot come back unnoticed. | Ch 24 |
| **relational database** | This project uses MySQL 8.4, a relational database: one that stores data in tables and links them by keys. | Ch 9 |
| **relative** | A path is absolute if it starts from the top (`/c/dev/...`) and relative if it starts from where you are (`src/main`). | Ch 2 |
| **relaxed binding** | Spring's rule that `signing-secret`, `SIGNING_SECRET` and `signingSecret` all name the same setting. | Ch 11 |
| **remote** | A remote is a copy of the repository on another computer, usually GitHub. | Ch 7 |
| **repository** | A Git project is a repository: your files plus a hidden folder named `.git` that holds the whole history. | Ch 7 |
| **reserved word** | The migration's own comment says why: "Column names avoid ROWS, which is reserved in MySQL 8." A reserved word is a word SQL already uses, so it cannot be used as a plain name. | Ch 9 |
| **resource** | A thing a REST API exposes at a URL, such as a document. | Ch 8 |
| **REST** | A style of web API built around URLs for things and HTTP methods for actions. | Ch 8 |
| **return value** | The result a method hands back to the code that called it. | Ch 3 |
| **role** | A role is a named set of permissions attached to an account. | Ch 15 |
| **route guard** | A route guard is a function that Angular runs before entering a route. | Ch 23 |
| **runtime error** | A runtime error happens while the program runs. | Ch 3 |
| **salt** | The fix for both is a salt: random data mixed into each password before hashing, so the same password produces a different hash every time. | Ch 15 |
| **same-origin policy** | Browsers apply the same-origin policy: a script loaded from one origin may not freely read responses from another. | Ch 8 |
| **schema** | A schema is the set of tables and columns. | Ch 9 |
| **scope** | A scope says when a dependency is needed. | Ch 6 |
| **secure by default** | Secure by default means the safe setting is the one you get if you configure nothing. | Ch 39 |
| **serialization** | Turning an object into text or bytes, such as JSON, so it can be sent or stored. | Ch 12 |
| **server** | The program that answers requests from clients; here, the Spring Boot app. | Ch 1 |
| **service** | A service is a class that holds the rules of the application, such as who may open a document. | Ch 11 |
| **service layer** | A service layer keeps rules in one place, so that two controllers can't implement "who may open this" differently; the price is a class that grows large, and `DocumentService` is the project's largest for that reason. | Ch 38 |
| **servlet** | A servlet is Java's name for a piece of code that handles web requests, and the web server (Tomcat, in this project) hands each request to your application through a chain of servlet filters. | Ch 15 |
| **session** | The server's record that a particular browser has proven who it is, so the browser does not have to send the password again with every request. | Ch 1 (in depth: Ch 8) |
| **session fixation protection** | Session fixation protection (`changeSessionId`) gives you a new session id at sign-in, as in step 6 of the previous section. | Ch 26 |
| **shell** | The program inside a terminal that reads your commands and runs them (for example Bash or PowerShell). | Ch 2 |
| **signal** | Angular's answer, used throughout this project, is the signal: a box that holds a value and remembers who has read it. | Ch 21 |
| **signature** | A value computed from data and a secret key that proves the data was not changed. | Ch 1 |
| **signed URL** | A URL carrying a signature the server can verify, so it cannot be altered or forged. | Ch 1 |
| **single-page-app fallback** | `try_files $uri $uri/ /index.html` is the single-page-app fallback: a deep link like `/viewer/123` isn't a real file, so nginx serves `index.html` and Angular's router takes over. | Ch 33 |
| **sliding window** | The limit is a sliding window: it remembers the times of a user's recent tile requests, drops those older than the window, and refuses if the count reaches the limit. | Ch 26 |
| **sliding window log** | The standard name for this technique is a sliding window log: the window always covers the last N seconds, and the list is the log. | Ch 38 |
| **source code** | The human-readable text of a program, before it is compiled. | Ch 3 |
| **spec** | Vitest is the test runner: it finds files ending in `.spec.ts` (a spec is a file of tests, short for specification), runs them, and reports which checks passed. | Ch 24 |
| **Spring Boot** | Spring Boot is a layer on top of it that chooses sensible defaults, so a program can start with almost no setup. | Ch 11 |
| **Spring Data JPA** | Spring Data JPA sits on top and writes the common queries for you. | Ch 14 |
| **Spring Security** | Spring Security is the part of the Spring family that does both jobs. | Ch 15 |
| **SQL** | The language used to query and change a relational database. | Ch 9 |
| **SQL injection** | This attack is called SQL injection, and it has caused some of the worst data breaches on record. | Ch 9 |
| **stack trace** | The list of method calls that were active when an exception occurred, printed to help find the cause. | Ch 3 |
| **Stage one (`AS build`)** | Stage one (`AS build`) uses the full JDK, which has the compiler. | Ch 33 |
| **starter (Spring Boot)** | A single dependency that pulls in a matched set of libraries for one job, for example `spring-boot-starter-security`. | Ch 6 |
| **state machine** | The pattern: a state machine names the possible states and the allowed moves between them, and makes each move atomic. | Ch 38 |
| **static factory method** | `Viewer.of(...)` is a static factory method: called on the type, not on an object, and it returns a new `Viewer`. | Ch 4 |
| **statically typed** | Java is statically typed: every variable has a type that says what kind of value it holds, and the compiler checks that you use it correctly. | Ch 3 |
| **status code** | The response's status code is a three-digit number telling the client what happened. | Ch 8 |
| **stream** | A stream is a pipeline that processes a collection step by step: filter some items, transform others, collect a result. | Ch 5 |
| **string** | A sequence of characters; text. | Ch 3 |
| **stub** | It is a stub, a stand-in with just enough behavior. | Ch 24 |
| **supply chain** | The libraries you depend on are your supply chain: code written by others, running with your app's privileges. | Ch 6 |
| **table** | A table is like a spreadsheet: it has named columns (the kinds of facts) and rows (one entry each). | Ch 9 |
| **tag** | A tag is a permanent name for one commit, like a bookmark. | Ch 7 |
| **template** | A component has three parts: a class (the data and actions, in TypeScript), a template (the HTML that shows them; HTML is the markup language that describes the structure of a web page, such as headings, buttons and links), and a stylesheet (CSS, the language that sets colors, spacing and layout, that decorates them). | Ch 21 |
| **template method** | The pattern: in a template method, the fixed skeleton is written once and the variable step is supplied by the caller; in Java and Spring it is usually supplied as a *callback*, a lambda passed in. | Ch 38 |
| **terminal** | A window in which you type commands to the computer instead of clicking. | Ch 2 |
| **test** | A test is code that checks other code. | Ch 3 |
| **test isolation** | This is test isolation: the outcome of one test must never depend on another having run. | Ch 24 |
| **Testcontainers** | Testcontainers is a library that starts a real service, here MySQL, in a Docker container (Chapter 10) for the duration of a test and throws it away afterward. | Ch 18 |
| **text file** | A file that holds characters (rather than, say, an image). | Ch 2 |
| **thread** | A web server handles many requests at the same time, each on its own thread: a path of execution inside the program. | Ch 5 |
| **threat** | A threat is a specific way someone might misuse the system; a defense is what stands in the way. | Ch 1 |
| **threat model** | A threat model is that exercise, done on paper first and then with real tests. | Ch 32 |
| **throttling** | Throttling limits how many attempts are allowed in a period. | Ch 16 |
| **tile** | One small square piece of a rendered page image. | Ch 1 |
| **TLS** | The protocol that encrypts traffic between browser and server (the "S" in HTTPS). | Ch 8 |
| **token** | A string that stands for a right to do something; here, a signed tile request. | Ch 3 |
| **transaction** | A transaction groups several statements so they all succeed or none do. | Ch 9 |
| **transitive dependencies** | These are transitive dependencies: the ones you did not list, but that arrive because something you listed needs them. | Ch 6 |
| **Trivy** | Trivy scans something different: the *built container images*. | Ch 36 |
| **trust boundary** | The trust boundary is the line between what the project controls (the server) and what it doesn't (the browser). | Ch 23 |
| **twelve-factor app** | The twelve-factor app is a published methodology for building services that deploy cleanly. | Ch 39 |
| **type** | A label saying what kind of value something is (text, number, list, a custom shape). | Ch 3 |
| **TypeScript** | JavaScript plus a type system, checked by a compiler before the code runs. | Ch 19 |
| **unchecked** | Those that extend `RuntimeException` are unchecked: the compiler does not force you to catch them. | Ch 5 |
| **union type** | A type that allows one of several listed alternatives. | Ch 19 |
| **untracked** | Untracked means Git can see the file but is not recording it. | Ch 7 |
| **untrusted input** | Untrusted input is any data that comes from outside your program: a file the user uploads, a value typed in a form, a header or address in a request. "Untrusted" doesn't mean the sender is malicious. | Ch 28 |
| **upsert** | `remember` is an upsert: `insert ... on duplicate key update` inserts a row or, if one exists for this user and address, refreshes its timestamp; it is safe when two sign-ins race. | Ch 30 |
| **URL** | Uniform Resource Locator: a web address such as `https://example.com/page`. | Ch 1 |
| **user enumeration** | That is called user enumeration, and it is the first step in many attacks: guessing passwords for a known account is far easier than guessing both parts. | Ch 15 |
| **UTC** | UTC (Coordinated Universal Time) is the world's reference time, with no daylight-saving changes. | Ch 5 |
| **value object** | A value object is a small immutable object defined by its values, with no identity of its own. | Ch 38 |
| **variable** | A named place that holds a value. | Ch 3 |
| **version control** | Version control is a tool that records every change to a project's files so that you can go back to any earlier state; Git is the one this project uses, and Chapter 7 teaches it. | Ch 2 |
| **visibility** | Each document has an owner and a visibility: either `PRIVATE` (only the owner, administrators and the users the owner shared it with) or `EVERYONE` (any signed-in user). | Ch 1 |
| **Vitest** | Vitest is the test runner: it finds files ending in `.spec.ts` (a spec is a file of tests, short for specification), runs them, and reports which checks passed. | Ch 24 |
| **watermark** | Text or a mark drawn onto content to identify who received it. | Ch 1 |
| **workflow** | A workflow is a YAML file in `.github/workflows/`. | Ch 36 |
| **working directory** | The folder a shell (or a program) is currently "in"; relative paths are read from here. | Ch 2 |
| **wrapper** | The project therefore ships a wrapper: small scripts, `mvnw` (macOS, Linux and Git Bash) and `mvnw.cmd` (Windows), that download and run one exact Maven version. | Ch 6 |
| **XML** | A `pom.xml` is written in XML, a format where information sits between named tags: `<name>secure-doc-viewer</name>` means "the name is secure-doc-viewer". | Ch 6 |

