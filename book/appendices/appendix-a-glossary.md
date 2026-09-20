# Appendix A: Glossary

Definitions of the terms this book bolds at first use, in alphabetical order. The last column names the chapter where each term is first defined.

| Term | Definition | First defined |
|---|---|---|
| **absolute lifetime** | A limit on how long a session may last after sign-in, however active it is; the app sets it to 12 hours. | Ch 16 |
| **absolute path** | A path that starts from the top of the file system, such as `/c/dev/secure-doc-viewer`, and so means the same wherever you are. | Ch 2 |
| **access analyzer** | An AWS tool that reports which resources, such as buckets, can be reached from outside your account. | Ch 41 |
| **access level** | A Java keyword (`public`, `private`, package-private) that decides who may use a class, field or method. | Ch 4 |
| **actor** | In a threat model, a person or program that can interact with the system, such as a reader, a publisher or an attacker. | Ch 32 |
| **Actuator** | The Spring Boot library that adds operational endpoints such as health checks and metrics. | Ch 28 |
| **adapter** | A design pattern in which a small class translates one interface into the one your code expects. | Ch 38 |
| **administrator** | The role that may manage users, sessions and the audit log, and see every document; in code, `ADMIN`. | Ch 1 |
| **alert** | A rule that notifies people when a metric shows a problem. | Ch 35 |
| **Amazon CloudFront** | The AWS content delivery network, which serves files from locations near the reader. | Ch 41 |
| **Amazon CloudWatch** | The AWS service that collects logs and metrics from ECS and the load balancer and raises alarms. | Ch 40 |
| **Amazon ECR** | Elastic Container Registry, the AWS registry that stores container images. | Ch 40 |
| **Amazon ECS** | Elastic Container Service, the AWS service that runs containers. | Ch 40 |
| **Amazon ElastiCache** | An AWS-managed in-memory store (Valkey or Redis OSS) that all tasks can share. | Ch 40 |
| **Amazon EventBridge Scheduler** | An AWS service that starts a task or another target on a schedule. | Ch 40 |
| **Amazon Managed Service for Prometheus** | An AWS-managed store that accepts Prometheus metrics, so you do not run a Prometheus server yourself. | Ch 40 |
| **Amazon RDS** | Relational Database Service: AWS runs a database such as MySQL for you, including patching, backups and failover. | Ch 40 |
| **Amazon Route 53** | The AWS DNS service. | Ch 40 |
| **Amazon S3** | Simple Storage Service: AWS object storage that keeps files (objects) by key in buckets, with no real directories and no atomic folder rename. | Ch 40 |
| **Angular** | The framework, written in TypeScript, in which the app's browser side is built from components, services and routes. | Ch 21 |
| **Angular CLI** | The `ng` command-line program that creates, serves, builds and tests an Angular project. | Ch 20 |
| **annotation** | A label starting with `@` that you attach to Java code so that a framework or the compiler can read it. | Ch 4 |
| **application context** | Spring's container: the object that creates, holds and connects all of the application's beans. | Ch 11 |
| **Application Load Balancer (ALB)** | An AWS service that receives HTTPS requests, spreads them over healthy tasks and health-checks each one. | Ch 40 |
| **architectural pattern** | A named, large-scale shape for arranging a whole system, such as layers, a gateway or an event log. | Ch 39 |
| **argument** | A value you give to a command or to a method call so that it knows what to act on. | Ch 2 |
| **ARN (Amazon Resource Name)** | The unique address of one AWS resource, such as a secret or a role. | Ch 40 |
| **arrow function** | A short way to write a function in JavaScript and TypeScript, using `=>`. | Ch 19 |
| **asset** | In a threat model, something worth protecting, such as the documents, the signing secret or the accounts. | Ch 32 |
| **atomic** | Describes a step that happens completely or not at all, so that nothing else can see it half done. | Ch 5 |
| **audit log** | The persisted record of who did what and when, which administrators can search and export; the audit trail is its content. | Ch 35 |
| **audit trail** | The permanent, append-only record of important actions such as sign-ins, uploads, views and denied requests, kept so that events can be investigated later. | Ch 1 |
| **authentication** | Proving who you are, for example with a username and password. | Ch 1 |
| **authorization** | Deciding what an authenticated person is allowed to do. | Ch 1 |
| **auto-configuration** | Spring Boot's habit of setting up sensible defaults for the libraries it finds on the classpath. | Ch 11 |
| **Availability Zone (AZ)** | One or more separate data centers inside an AWS Region; using two means a failure in one does not take you down. | Ch 40 |
| **AWS Backup** | An AWS service that schedules and manages backups and restores of databases and storage. | Ch 40 |
| **AWS Certificate Manager (ACM)** | The AWS service that issues public TLS certificates and renews them. | Ch 40 |
| **AWS Fargate** | A way to run ECS containers without managing servers. | Ch 40 |
| **AWS Secrets Manager** | The AWS service that stores secrets and can inject them into a container as environment variables at start. | Ch 40 |
| **AWS WAF** | A web application firewall that attaches rules to a load balancer or a CloudFront distribution. | Ch 41 |
| **axe-core** | A library that scans a rendered page for accessibility problems, such as low contrast or missing labels. | Ch 24 |
| **backend** | The part of an application that runs on a server and holds the data and the rules. | Ch 1 |
| **BCrypt** | A password-hashing function that is deliberately slow and reads at most 72 bytes of the password. | Ch 15 |
| **bean** | An object that Spring creates and manages, from a class labeled with an annotation such as `@Service`. | Ch 11 |
| **Bean Validation** | The Java standard for declaring input rules with annotations such as `@NotBlank` and `@Size`. | Ch 13 |
| **blame** | A Git command that shows which commit last changed each line of a file. | Ch 7 |
| **boxing** | Java's automatic conversion between a primitive value such as `int` and its object form such as `Integer`. | Ch 5 |
| **branch** | A movable name for a line of work in Git. | Ch 7 |
| **browser** | The program, such as Chrome, Firefox, Safari or Edge, that requests web pages and runs their code. | Ch 19 |
| **bucket** | A named container for objects in Amazon S3. | Ch 40 |
| **build** | The sequence of steps that turns source code into a tested, packaged program. | Ch 6 |
| **build context** | The folder that Docker sends to the builder when you build an image. | Ch 10 |
| **build tool** | A program that compiles code, runs tests and packages the result; Maven is the one this project uses for Java. | Ch 6 |
| **bulkhead** | A design pattern that gives each kind of work its own limit, so that overload in one does not sink the rest. | Ch 38 |
| **bytecode** | The compact instructions that the Java compiler produces and the JVM runs. | Ch 3 |
| **cache** | A place where a copy of something is kept so that it can be served again faster. | Ch 1 |
| **camel case** | A naming style with no spaces in which each word after the first starts with a capital letter, such as `tileCount`. | Ch 3 |
| **canonical string** | The fixed, unambiguous text that is signed when a tile token is made: the payload fields joined in a set order. | Ch 17 |
| **capability URL** | A URL whose possession is what lets you fetch the one thing it names; a signed tile URL is an example. | Ch 39 |
| **CDN (content delivery network)** | A network of servers around the world that keeps copies of files near readers so that they load quickly. | Ch 40 |
| **ceiling division** | Division that rounds up, used to count how many tiles are needed to cover a page. | Ch 3 |
| **certificate** | A signed statement, issued by an authority the browser trusts, that a public key belongs to a given domain. | Ch 8 |
| **certificate authority** | An organization that browsers trust to issue certificates. | Ch 33 |
| **chain of responsibility** | A design pattern in which a request passes along a line of handlers, each of which may handle it, change it or pass it on. | Ch 38 |
| **change detection** | Angular's process of deciding which parts of the screen to redraw when data changes. | Ch 21 |
| **checked exception** | A Java exception, such as `IOException`, that a method must catch or declare with `throws`. | Ch 5 |
| **CI (continuous integration)** | The practice of running tests and checks automatically on every proposed change. | Ch 6 |
| **CIDR** | A compact way to write a range of network addresses, such as `10.0.0.0/16`. | Ch 40 |
| **class** | A named blueprint in Java that describes the data and the behavior of a kind of object. | Ch 3 |
| **clickjacking** | An attack that hides a real page inside a frame on a hostile page so that you click on it without knowing. | Ch 16 |
| **client** | A program that asks for things over a network; your browser is a client. | Ch 1 |
| **clone** | To copy an existing Git repository, with its history, to your machine. | Ch 7 |
| **code review** | The practice of having someone else read a change before it is merged. | Ch 7 |
| **collection** | A Java object that holds many values, such as a list, a set or a map. | Ch 5 |
| **commit** | A saved state in Git: a snapshot of every tracked file at one moment, with a message and a unique identifier. | Ch 7 |
| **commit SHA** | The long hexadecimal identifier that uniquely names a Git commit. | Ch 36 |
| **compiler** | A program that checks source code and translates it into a form that a machine can run. | Ch 3 |
| **compiler error** | A message from the compiler that stops the build before anything runs. | Ch 3 |
| **component scanning** | Spring's search of your packages for annotated classes to register as beans. | Ch 11 |
| **composite primary key** | A primary key made of more than one column, such as a document and a user together. | Ch 14 |
| **composition** | Building a class by giving it other objects to use, instead of inheriting from another class. | Ch 4 |
| **computed signal** | An Angular signal whose value is calculated from other signals and recalculated only when they change. | Ch 21 |
| **concurrency** | Several things happening at overlapping times, for example many requests handled by many threads. | Ch 13 |
| **conditional expression** | The `condition ? a : b` form, which gives `a` when the condition is true and `b` otherwise. | Ch 19 |
| **constant-time comparison** | A check whose running time does not depend on how many characters match, so that an attacker cannot learn a secret from timing. | Ch 17 |
| **constraint** | A rule that a database enforces on a column or table, such as `NOT NULL` or `UNIQUE`. | Ch 9 |
| **constructor** | Special code that runs when you create an object with `new` and sets up its fields. | Ch 4 |
| **constructor injection** | Dependency injection in which an object receives its dependencies as constructor parameters. | Ch 4 |
| **container** | A running, isolated instance of an image: a program packaged with everything it needs. | Ch 10 |
| **Content-Security-Policy (CSP)** | A response header that tells the browser which sources a page may load scripts, images and connections from. | Ch 8 |
| **contrast** | The difference in brightness between text and its background, measured as a ratio. | Ch 21 |
| **control** | In a security review, a measure that reduces a risk, such as a rate limit or a check. | Ch 32 |
| **controller** | A class that receives web requests and returns responses. | Ch 11 |
| **convention over configuration** | The design habit of choosing sensible defaults so that you configure only what differs; Maven and Spring Boot both follow it. | Ch 6 |
| **cookie** | A small piece of data that a server asks the browser to store and send back with later requests. | Ch 8 |
| **coordinates** | The three values (group, artifact and version) that identify a Maven project or library. | Ch 6 |
| **copy-on-write** | A technique in which a change makes a new copy and leaves the old one untouched, so that readers never see a half-changed value. | Ch 39 |
| **CORS (cross-origin resource sharing)** | The browser mechanism by which a server says which other origins may read its responses. | Ch 8 |
| **counter** | A metric that only goes up, such as the number of tiles served. | Ch 35 |
| **CPU** | The processor that does a computer's calculations. | Ch 17 |
| **CRLF** | The two-byte Windows line ending: carriage return followed by line feed. | Ch 2 |
| **Crockford Base32** | A way of writing bytes with 32 easily read characters, used for the watermark's trace code. | Ch 29 |
| **cron expression** | A compact schedule with fields for second, minute, hour, day, month and weekday, such as `0 30 3 * * *`. | Ch 14 |
| **CSRF (cross-site request forgery)** | An attack in which another website makes your browser send a request to a site where you are signed in. | Ch 8 |
| **CSRF token** | A secret value that the app's own page sends back in a header on every change, so that a forged request cannot supply it. | Ch 8 |
| **CSV injection** | An attack in which a spreadsheet cell that starts with `=`, `+`, `-` or `@` runs as a formula when an exported CSV file is opened. | Ch 27 |
| **CVE** | A public identifier, in the Common Vulnerabilities and Exposures list, for one known security flaw in some software. | Ch 31 |
| **data transfer object (DTO)** | A small class, often a record, that carries exactly the data of one request or response. | Ch 38 |
| **database** | A program that stores an application's data permanently and lets you query it. | Ch 1 |
| **decompression bomb** | A small file that expands to a huge amount of data or pixels when processed, exhausting memory. | Ch 13 |
| **deep link** | An address that leads straight to a specific state, such as the viewer at page 3 of a document. | Ch 23 |
| **defense** | Whatever stands in the way of a threat. | Ch 1 |
| **defense in depth** | The principle of layering several independent protections so that one failure does not expose everything. | Ch 23 |
| **denial of service** | An attack, or a side effect, that makes a service unavailable to the people who need it. | Ch 30 |
| **denormalized** | Storing a copy of information in more than one place instead of linking to one copy. | Ch 9 |
| **deny by default** | A stance in which anything not explicitly allowed is refused. | Ch 26 |
| **Dependabot** | A GitHub service that opens pull requests when newer versions of your dependencies exist. | Ch 31 |
| **dependency** | A library that your code uses. | Ch 6 |
| **dependency injection** | Giving an object the other objects it needs instead of letting it create them itself. | Ch 4 |
| **deserialization** | Turning text or bytes, such as JSON, back into an object. | Ch 12 |
| **design pattern** | A named, reusable solution to a problem that recurs in code. | Ch 38 |
| **detached HEAD** | A Git state in which you are looking at a commit that is not on any branch. | Ch 7 |
| **developer tools** | The inspection panel built into browsers that shows requests, code and errors. | Ch 1 |
| **diamond** | The empty angle brackets `<>` in Java, which tell the compiler to work out the type arguments from the left-hand side. | Ch 5 |
| **digest** | A fingerprint of exact contents, such as the SHA-256 hash that identifies one Docker image. | Ch 10 |
| **dirty checking** | Hibernate's habit of noticing that a loaded entity was changed and saving the change when the transaction ends. | Ch 14 |
| **discriminated union** | A TypeScript union of object types that each carry a fixed tag field, so that code can tell them apart. | Ch 19 |
| **DNS (Domain Name System)** | The internet's phone book, which turns a name such as `docs.example.com` into a network address. | Ch 8 |
| **doc comment** | A `/** ... */` comment that documents a class or method for people and for documentation tools. | Ch 3 |
| **Docker** | The tool that builds and runs programs in containers. | Ch 10 |
| **Docker Compose** | A tool that describes several containers in one file, `docker-compose.yml`, and starts them with one command. | Ch 10 |
| **Dockerfile** | The recipe of steps that builds a Docker image. | Ch 10 |
| **double-submit cookie** | A CSRF defense in which the same value arrives twice, once in a cookie and once in a header that only the real page can set. | Ch 16 |
| **DPI (dots per inch)** | How many pixels represent one inch of a page when it is rendered as an image. | Ch 17 |
| **Elastic Load Balancing** | The family of AWS load-balancing services, of which the Application Load Balancer is one. | Ch 40 |
| **encapsulation** | Hiding a class's fields and exposing only the operations you intend. | Ch 4 |
| **encoding** | The rule that maps characters to bytes; the project's files use UTF-8. | Ch 2 |
| **encryption** | Scrambling data with a key so that only someone with the key can read it. | Ch 8 |
| **end-to-end test** | A test that drives the whole running application through a real browser. | Ch 24 |
| **entity** | A Java class whose objects are stored as rows in a database table. | Ch 14 |
| **enum** | A Java type that lists all the allowed values of a small fixed set, such as the roles. | Ch 4 |
| **environment variable** | A named setting, such as `DB_PASSWORD`, that the operating system hands to every program it starts. | Ch 2 |
| **epoch seconds** | A time written as the number of seconds since January 1, 1970, UTC. | Ch 5 |
| **event log** | An ordered record of things that happened, added to and never edited. | Ch 39 |
| **event sourcing** | A design in which the sequence of events is the source of truth and the current state is derived from it. | Ch 38 |
| **exception** | An object that signals that something went wrong and interrupts normal flow until it is handled. | Ch 3 |
| **exception handler** | A method that turns a particular exception into a controlled HTTP response. | Ch 13 |
| **executable JAR** | One file that holds the application, its libraries and the web server, so that a single command starts the whole application. | Ch 6 |
| **exponential backoff** | Waiting longer after each failed attempt, for example 50, 100, 200 and 400 milliseconds. | Ch 27 |
| **facade** | A design pattern in which one small, simple interface stands in front of a larger subsystem. | Ch 38 |
| **factory method** | A static method that builds an object, so that callers need not know how. | Ch 38 |
| **fail fast** | Refusing a bad situation at the first opportunity instead of letting it travel further into the code. | Ch 38 |
| **fast-forward** | A Git merge that simply moves a branch pointer forward because no other changes exist. | Ch 7 |
| **fetch** | To download new commits from a remote without changing your files. | Ch 7 |
| **fixture** | Prepared sample data or state that a test starts from. | Ch 24 |
| **flag** | An option that changes how a command behaves, such as `-l` in `ls -l`. | Ch 2 |
| **flaky test** | A test that sometimes passes and sometimes fails without any change to the code. | Ch 18 |
| **Flexbox** | A CSS layout mode, `display: flex`, that arranges children in a row or column and spaces them. | Ch 21 |
| **fluent interface** | A style in which each method returns the object itself so that calls chain into one readable sentence. | Ch 38 |
| **Flyway** | A tool that applies numbered SQL migration files to a database in order. | Ch 9 |
| **folder** | A container for files and other folders; also called a directory. | Ch 2 |
| **for-each loop** | A loop that visits every item of a collection in turn. | Ch 5 |
| **foreign key** | A column that holds the primary key of a row in another table, linking the two. | Ch 9 |
| **framework** | A large body of ready-made code that supplies the structure of an application and calls your code at the right moments. | Ch 4 |
| **frontend** | The part of an application that runs in the user's browser. | Ch 1 |
| **gateway** | A reverse proxy at the edge that does more than forward requests, for example applying common rules to all of them. | Ch 39 |
| **gauge** | A metric that can go up and down, such as the number of renders running now. | Ch 35 |
| **generic** | A Java or TypeScript type that is written with a placeholder, such as `List<String>`, so that one definition works for many types. | Ch 4 |
| **getter** | A method that reads a private field of an object. | Ch 4 |
| **Git** | The version control tool that records every change to the project's files. | Ch 7 |
| **Git Bash** | The terminal that comes with Git for Windows and understands the bash commands used in this book. | Ch 2 |
| **GitHub Actions** | GitHub's service that runs workflows, such as the project's tests, on every change. | Ch 36 |
| **graceful shutdown** | Stopping a server by refusing new requests and letting the ones in progress finish first. | Ch 41 |
| **guard clause** | A check at the top of a method that returns or throws at once when a precondition is not met. | Ch 38 |
| **hash** | A fixed-length, one-way fingerprint of data. | Ch 15 |
| **health check** | A request that a platform sends regularly to see whether a program is up and ready for traffic. | Ch 10 |
| **heap** | The part of memory where a Java program keeps its objects; the JVM sets its size at start. | Ch 17 |
| **Hibernate** | The library that maps Java objects to database tables; it is the JPA implementation this project uses. | Ch 14 |
| **HMAC** | A keyed hash: a short code that only someone holding the secret key can compute, used to detect tampering. | Ch 15 |
| **home folder** | The folder where your personal files live; `~` is shorthand for it. | Ch 2 |
| **host** | The computer that runs Docker and its containers. | Ch 10 |
| **HSTS (HTTP Strict Transport Security)** | A response header that tells browsers to use only HTTPS for a site from then on. | Ch 30 |
| **HTTP** | The set of rules that browsers and servers use to exchange requests and responses. | Ch 8 |
| **HTTPS** | HTTP carried inside an encrypted channel created by TLS. | Ch 33 |
| **IAM policy** | A document that lists which actions on which resources an IAM role or user may perform. | Ch 41 |
| **IAM role** | A set of permissions that a program assumes temporarily in AWS, with no stored password or key. | Ch 40 |
| **idempotent** | Describes an operation that has the same effect when done twice as when done once. | Ch 8 |
| **idle cost** | What a cloud resource bills while it exists, whether or not anyone uses it. | Ch 40 |
| **idle timeout** | The time a session may sit unused before it ends; the app sets it to 30 minutes. | Ch 16 |
| **image** | A read-only template from which Docker containers are started. | Ch 10 |
| **image tag** | A movable name for a Docker image, such as `24-alpine`, which can later point at a different image. | Ch 36 |
| **immutable** | Describes something that cannot change after it is created. | Ch 4 |
| **import** | A Java statement that lets you use a class from another package by its short name. | Ch 4 |
| **index** | A sorted lookup structure on one or more database columns, so that matching rows are found without reading the whole table. | Ch 9 |
| **infrastructure as code** | Describing servers, networks and other infrastructure in files that can be reviewed, versioned and re-applied. | Ch 39 |
| **inheritance** | Building a new class by taking everything an existing class has and adding to it. | Ch 4 |
| **instant** | A single point on the timeline, independent of time zones; `Instant` is the Java class for it. | Ch 5 |
| **integration test** | A test that starts several pieces together, for example the real security filters and a real controller. | Ch 18 |
| **interceptor** | A function in Angular's HTTP client that sees every request and response, used for the CSRF header and for reacting to `401`. | Ch 22 |
| **interface** | A description of what an object can do (Java) or of its shape (TypeScript), without saying how. | Ch 4 |
| **interpolation** | Inserting the value of an expression into text in an Angular template with `{{ }}`. | Ch 21 |
| **inversion of control** | The principle that a framework, not your code, creates objects and decides when to call them. | Ch 11 |
| **Jackson** | The Java library that converts between objects and JSON. | Ch 12 |
| **JAR** | A Java Archive: a zip file of compiled classes. | Ch 6 |
| **JAVA_HOME** | The environment variable that points to the folder where the JDK is installed. | Ch 2 |
| **JavaScript** | The programming language that browsers run. | Ch 19 |
| **JDK** | The Java Development Kit: the compiler and tools needed to build Java programs. | Ch 3 |
| **job** | A group of steps that run together on one machine in a GitHub Actions workflow. | Ch 36 |
| **join** | A query operation that combines rows from two tables. | Ch 9 |
| **join table** | A table that holds pairs of keys to link two other tables in a many-to-many relationship. | Ch 9 |
| **JPA** | The Jakarta Persistence standard for mapping Java objects to database tables. | Ch 14 |
| **JPQL** | The JPA query language, which talks about classes and fields instead of tables and columns. | Ch 27 |
| **JRE** | The Java Runtime Environment: the part of Java needed only to run programs, without the compiler. | Ch 3 |
| **jsdom** | A pretend browser written in JavaScript, so that tests can create page elements without a real browser. | Ch 24 |
| **JSON** | A text format for structured data made of objects, lists, strings, numbers and booleans. | Ch 8 |
| **JVM** | The Java Virtual Machine: the program that runs compiled Java. | Ch 3 |
| **JWT** | A JSON Web Token: a signed piece of text that carries claims and can be verified without a server-side session. | Ch 15 |
| **keepalive_timeout** | The nginx setting for how long an idle connection stays open; it must exceed the load balancer's idle timeout. | Ch 40 |
| **KMS (Key Management Service)** | The AWS service that creates and holds encryption keys. | Ch 40 |
| **lambda** | A small unnamed method that you can pass around. | Ch 5 |
| **latch** | A counter that makes threads wait until it reaches zero; tests use it to release parallel threads at once. | Ch 18 |
| **layer** | In Docker, one step of an image build, stored once and reused; in architecture, a level of code that may call only the level below it. | Ch 10 |
| **layered architecture** | An arrangement in which code is divided into levels, such as controllers, services and repositories, each calling only the one below. | Ch 12 |
| **lazy loading** | Loading related data only when it is first used. | Ch 14 |
| **least privilege** | The principle of giving a process or person only the access it needs. | Ch 36 |
| **library** | A collection of ready-made code that your program calls when it needs it. | Ch 11 |
| **lifecycle hook** | A method that Angular calls when a component appears or is removed, such as `ngOnInit`. | Ch 21 |
| **lifecycle rule** | An S3 rule that expires old object versions or aborts incomplete uploads after a set time. | Ch 40 |
| **line ending** | The mark that ends a line of text: Windows uses two bytes (carriage return and line feed), macOS and Linux use one (line feed). | Ch 2 |
| **list** | A Java collection that keeps items in order and allows duplicates. | Ch 5 |
| **literal** | A value written directly in code, such as `512` or `"hello"`. | Ch 3 |
| **localhost** | The name a computer uses for itself; requests to it never leave the machine. | Ch 2 |
| **lockfile** | A file, such as `package-lock.json`, that records the exact version of every dependency. | Ch 36 |
| **log** | The stream of text lines that a running program writes to say what it is doing. | Ch 11 |
| **log level** | A label such as ERROR, WARN, INFO or DEBUG that says how important a log line is, so that you can filter them. | Ch 11 |
| **LTS (long-term support)** | A release line that receives fixes for years, which the project prefers for MySQL, Node and Java. | Ch 20 |
| **Lua script (Redis)** | A small script that Redis runs atomically, so that several commands take effect as one. | Ch 40 |
| **magic number** | A short, recognizable byte pattern at the start of a file that shows its type, such as `%PDF-`. | Ch 28 |
| **managed service** | A service in which the provider runs the software for you, including patches and backups, and you give up some control. | Ch 40 |
| **many-to-many** | A relationship in which each row on one side can link to many on the other and the reverse; it needs a join table. | Ch 9 |
| **map** | A Java collection of key and value pairs, looked up by key. | Ch 5 |
| **Maven** | The build tool this project uses to compile, test and package its Java code. | Ch 6 |
| **Maven Central** | The public online repository from which Maven downloads libraries. | Ch 6 |
| **Maven wrapper** | A small script (`mvnw`) that downloads and runs the exact Maven version the project uses. | Ch 30 |
| **merge** | Combining the changes of one Git branch into another. | Ch 7 |
| **merge commit** | A commit that joins two lines of work and has two parents. | Ch 7 |
| **merge conflict** | A stop in a merge because two branches changed the same lines and Git cannot choose. | Ch 7 |
| **method** | A named block of code that does one job and can be called by name. | Ch 3 |
| **method reference** | A short form of a lambda that only calls one method, such as `AppUser::getUsername`. | Ch 5 |
| **metric** | A number that a running program exposes over time, such as how many tiles it has served. | Ch 30 |
| **metric tag** | A label on a metric, such as the outcome of a sign-in, that lets you slice it. | Ch 35 |
| **Micrometer** | The Java library that collects metrics and hands them to a system such as Prometheus. | Ch 35 |
| **microservices** | A design in which each part of a system is its own separately deployed program that talks to the others over the network. | Ch 38 |
| **migration** | A versioned change to a database schema, applied once and in order. | Ch 9 |
| **mock** | A stand-in object that records how it was called, used to test code without its real collaborators. | Ch 18 |
| **modular monolith** | A single deployable program whose inside is divided into modules with clear responsibilities. | Ch 39 |
| **module** | A file or group of code that exports some things and imports others. | Ch 19 |
| **Multi-AZ** | A setup that keeps a synchronous standby copy in a second Availability Zone and fails over automatically. | Ch 40 |
| **multi-stage build** | A Dockerfile that builds in one stage and copies only the result into a small final image. | Ch 10 |
| **multipart** | The request format that carries files and form fields together, used for uploads. | Ch 8 |
| **N+1 problem** | A performance bug in which loading one list triggers one extra query for each item. | Ch 14 |
| **naive viewer** | A viewer that only hides its download button in the browser, so that anyone can still fetch the file. | Ch 1 |
| **NAT gateway** | An AWS service that lets resources in private subnets reach the internet; it bills while it exists. | Ch 41 |
| **Node.js** | A program that runs JavaScript outside a browser, on your own computer. | Ch 20 |
| **non-root** | Running a program as an ordinary user instead of the all-powerful root user, so that a break-in does less harm. | Ch 33 |
| **npm** | Node's package manager, which downloads libraries from a public registry. | Ch 20 |
| **object** | One thing built from a class, with its own values for the fields. | Ch 4 |
| **object storage** | A kind of storage that keeps whole files as objects under keys, instead of in folders on a disk. | Ch 40 |
| **observability** | How well you can tell what a running system is doing from what it reports. | Ch 35 |
| **observer** | A design pattern in which a publisher announces events and any number of subscribers react. | Ch 38 |
| **off-site backup** | A copy of a backup kept somewhere other than the machine that runs the app. | Ch 34 |
| **open redirect** | A flaw in which a site sends the browser to whatever address a link names, so that an attacker can dress a hostile link as a trusted one. | Ch 23 |
| **OpenID Connect (OIDC)** | A way for one service, such as GitHub, to prove its identity to another, such as AWS, and receive short-lived credentials with no stored key. | Ch 40 |
| **operator** | A symbol such as `+` or `==` that combines or compares values. | Ch 3 |
| **Optional** | A Java object that is either empty or holds one value, used instead of `null`. | Ch 5 |
| **origin** | The combination of scheme, host and port that browsers use to decide who may read whose responses. | Ch 8 |
| **ORM (object-relational mapper)** | A library that translates between objects in code and rows in a database. | Ch 14 |
| **OSV** | Open Source Vulnerabilities: a database of published security advisories, used to check the project's dependencies. | Ch 36 |
| **owner** | The account that uploaded a document and may manage it. | Ch 27 |
| **package** | A Java folder-like namespace that groups related classes. | Ch 4 |
| **parameter** | A named input that a method declares; the values passed in are its arguments. | Ch 3 |
| **parent POM** | A Maven project file that other projects inherit defaults from, such as a tested list of library versions. | Ch 11 |
| **passed by value** | How Java passes arguments: the method receives a copy of a primitive value. | Ch 3 |
| **PATH** | The environment variable that lists the folders a shell searches for commands. | Ch 2 |
| **path variable** | A part of a URL path, such as the document id in `/api/documents/{id}`, that a controller receives as a parameter. | Ch 12 |
| **PDF** | Portable Document Format: a file type that describes pages so that they look the same everywhere. | Ch 1 |
| **peer dependency** | A library that another package needs you to install at a compatible version, such as the TypeScript range that Angular supports. | Ch 31 |
| **Permissions-Policy** | A response header that switches browser features such as the camera and location on or off for a page. | Ch 8 |
| **PID (process id)** | The number that the operating system gives to a running process. | Ch 2 |
| **pipes and filters** | An architecture in which data flows through a line of small stages, each doing one job. | Ch 39 |
| **Playwright** | A tool that drives a real browser, used for the end-to-end tests. | Ch 24 |
| **PNG** | An image file format that stores a grid of pixels without losing detail. | Ch 17 |
| **point-in-time recovery** | Restoring a database to any moment within a retention period. | Ch 40 |
| **port** | A numbered door on a computer, from 0 to 65535, at which a server listens for network requests. | Ch 2 |
| **presigned URL** | An S3 URL that grants temporary access to one object to whoever holds it. | Ch 41 |
| **primary key** | A column, or set of columns, whose value identifies each row uniquely. | Ch 9 |
| **private subnet** | A subnet with no route from the internet. | Ch 41 |
| **process** | A running program. | Ch 2 |
| **profile** | A named set of extra Spring settings switched on for a purpose, such as the test profile. | Ch 10 |
| **program** | A list of instructions that a computer follows. | Ch 3 |
| **Prometheus** | A system that collects metrics by fetching a web page from each program at a regular interval. | Ch 35 |
| **promise** | A JavaScript object that stands for one result that will arrive later or fail. | Ch 19 |
| **prompt** | The text a shell shows to say that it is ready for your next command. | Ch 2 |
| **PromQL** | The query language for Prometheus metrics. | Ch 35 |
| **propagation** | The rule for how a transactional method behaves when a transaction is already running, such as joining it or starting its own. | Ch 14 |
| **property placeholder** | A `${NAME:default}` marker in configuration that Spring replaces with an environment or file value. | Ch 11 |
| **protocol** | An agreed format for a conversation between programs, such as HTTP. | Ch 8 |
| **proxy** | A program that receives a request on behalf of another server and forwards it. | Ch 8 |
| **publisher** | The role that may upload documents and manage its own; in code, `PUBLISHER`. | Ch 1 |
| **pull** | To fetch from a remote and merge the new commits into your branch. | Ch 7 |
| **pull request** | A proposal on GitHub to merge one branch into another, with a place for discussion and review. | Ch 7 |
| **push** | To send your commits to a remote repository. | Ch 7 |
| **query method** | A repository method whose name Spring turns into a database query. | Ch 14 |
| **query string** | The part of a URL after the `?`, which carries small pieces of data. | Ch 8 |
| **race condition** | A bug in which the result depends on the timing of things that happen at once. | Ch 16 |
| **rasterize** | To turn a page description, such as a PDF page, into a grid of pixels. | Ch 1 |
| **rate limit** | A cap on how many requests a user may make in a period. | Ch 1 |
| **rate limiter** | Code that refuses requests beyond a set number in a period. | Ch 38 |
| **reader** | The role that may open the documents shared with it; in code, `READER`. | Ch 1 |
| **recognised device** | A network address from which an account has signed in successfully in the last 30 days, which the sign-in throttle treats more leniently. | Ch 16 |
| **record** | A compact Java class for immutable plain data. | Ch 4 |
| **RED** | Three questions for watching a service: the Rate of requests, the Errors and the Duration. | Ch 39 |
| **redirection** | Sending a command's output somewhere other than the screen, such as into a file. | Ch 2 |
| **Referrer-Policy** | A response header that controls how much of the page's address is sent to other sites; the app sends `no-referrer`. | Ch 8 |
| **reflog** | Git's private log of where your branch tips have been, which can rescue lost commits. | Ch 7 |
| **Region** | A geographic area where AWS runs data centers. | Ch 40 |
| **registry** | A server that stores and serves Docker images or npm packages. | Ch 10 |
| **regression test** | A test written so that a fixed bug cannot come back unnoticed. | Ch 24 |
| **relational database** | A database that stores data in tables linked by keys. | Ch 9 |
| **relative path** | A path that starts from the folder you are in. | Ch 2 |
| **relaxed binding** | Spring's rule that `signing-secret`, `SIGNING_SECRET` and `signingSecret` all name the same setting. | Ch 11 |
| **remote** | A copy of a Git repository on another computer, usually on GitHub. | Ch 7 |
| **repository** | A Git project (files plus the hidden `.git` folder that holds history), or in Spring a class that reads and writes one kind of entity. | Ch 7 |
| **reproducible build** | A build that produces the same result from the same source on any machine and on any day. | Ch 36 |
| **request body** | The data a client sends with a request, such as the JSON of a form. | Ch 8 |
| **request line** | The first line of an HTTP request, holding the method, the path and the protocol version. | Ch 8 |
| **reserve then compensate** | A technique that takes a resource before a risky step and hands it back if the step does not go ahead. | Ch 38 |
| **reserved word** | A word that SQL reserves for its own use, so that it cannot be used as a plain column name. | Ch 9 |
| **resource** | A thing that a REST API exposes at a URL, such as a document. | Ch 8 |
| **response header** | A named value, such as `Cache-Control`, that a server sends before the body of a response. | Ch 8 |
| **ResponseEntity** | A Spring class that lets a controller set the status, headers and body of a response. | Ch 12 |
| **REST** | A style of web API built around URLs for things and HTTP methods for actions. | Ch 8 |
| **REST API** | A web interface that follows the REST style, with URLs for things and HTTP methods for actions. | Ch 39 |
| **restore drill** | A rehearsal in which you restore a backup into a scratch environment to prove that it works. | Ch 34 |
| **retention** | How long backups, logs or records are kept before they are deleted. | Ch 34 |
| **reverse proxy** | A server that sits in front of an application, receives requests from browsers and passes them on. | Ch 10 |
| **role** | A named set of permissions attached to an account, such as reader, publisher or administrator. | Ch 1 |
| **roll back** | To undo every change of a transaction because it failed. | Ch 14 |
| **route guard** | A function that Angular runs before it lets a user into a route, used for a better experience and never as security. | Ch 23 |
| **row lock** | A database lock on selected rows, so that two transactions cannot change them at once. | Ch 9 |
| **RPO (recovery point objective)** | How much recent data you can afford to lose, expressed as time. | Ch 34 |
| **RTO (recovery time objective)** | How long a restore may take before the outage is unacceptable. | Ch 34 |
| **runtime error** | An error that happens while the program runs rather than while it compiles. | Ch 3 |
| **S3 Block Public Access** | An S3 setting that stops any bucket or object from being made public. | Ch 40 |
| **S3 Versioning** | A bucket setting that keeps earlier versions of an object so that an overwritten or deleted object can be recovered. | Ch 40 |
| **safe method** | An HTTP method, such as GET, that is meant only to read and never to change anything. | Ch 8 |
| **safety net** | A rule that stops a cleanup job from deleting when its inputs look wrong, such as a missing current version. | Ch 28 |
| **salt** | Random data mixed into a password before hashing, so that equal passwords produce different hashes. | Ch 15 |
| **same-origin policy** | The browser rule that a script from one origin may not freely read responses from another. | Ch 8 |
| **SameSite** | A cookie setting that limits when the browser sends the cookie on requests that start from another site. | Ch 8 |
| **schema** | The set of tables and columns of a database. | Ch 9 |
| **scope** | A Maven setting that says when a dependency is needed, such as only for tests. | Ch 6 |
| **scrape** | Fetching a program's metrics page, which is how Prometheus collects data. | Ch 26 |
| **secret rotation** | Replacing a secret with a new one on a schedule or after a leak. | Ch 26 |
| **secure by default** | Describes software whose safe setting is the one you get if you configure nothing. | Ch 39 |
| **security group** | A firewall attached to an AWS resource that says which traffic may enter and leave. | Ch 40 |
| **semantic versioning** | A numbering scheme, major.minor.patch, in which the parts say how much changed. | Ch 20 |
| **semaphore** | A counter of permits that limits how many threads may do something at once. | Ch 17 |
| **serialization** | Turning an object into text or bytes, such as JSON, so that it can be sent or stored. | Ch 12 |
| **server** | The program that answers requests from clients; here, the Spring Boot app. | Ch 1 |
| **service** | In Spring, a class that holds the rules of the application, such as who may open a document. | Ch 11 |
| **service layer** | A layer of services that keeps the rules in one place, so that two controllers cannot apply them differently. | Ch 38 |
| **service name** | In Docker Compose, the name other containers use to reach a service on the private network, such as `mysql`. | Ch 10 |
| **servlet filter** | Code that every web request passes through before it reaches a controller. | Ch 15 |
| **session** | The server's record that a particular browser has proven who it is, so that the browser need not send the password with every request. | Ch 1 |
| **session binding** | Tying a tile token to the session it was issued to, so that a token copied to another browser is refused. | Ch 17 |
| **session cookie** | The cookie that carries the session's identifier; here `SDV_SESSION`. | Ch 1 |
| **session fixation** | An attack in which someone gives you a session identifier they already know; changing the identifier at sign-in defeats it. | Ch 15 |
| **session-based authentication** | A design in which the server keeps a record of each sign-in and the browser holds only an identifier. | Ch 39 |
| **shared responsibility model** | The AWS rule that AWS secures the cloud itself and you secure what you put in it. | Ch 40 |
| **shell** | The program inside a terminal that reads your commands and runs them, such as Bash or PowerShell. | Ch 2 |
| **sidecar** | A helper container that runs beside the main container in the same task. | Ch 40 |
| **signal** | An Angular value that remembers who has read it and tells them when it changes. | Ch 21 |
| **signature** | A value computed from data and a secret key that proves the data was not changed. | Ch 1 |
| **signed URL** | A URL carrying a signature that the server can verify, so that it cannot be altered or forged. | Ch 1 |
| **SLF4J** | The logging interface that Spring Boot applications use to write log lines. | Ch 11 |
| **sliding window** | A rate-limit method that counts requests in the last N seconds instead of in fixed clock periods. | Ch 26 |
| **sliding window log** | A rate-limit method that keeps the timestamps of recent requests and counts those inside the window. | Ch 38 |
| **source code** | The human-readable text of a program, before it is compiled. | Ch 3 |
| **SPA (single-page application)** | A web app that loads once and then draws its screens in the browser, asking the server only for data. | Ch 16 |
| **SPA fallback** | The web-server rule that answers unknown addresses with `index.html`, so that a bookmarked deep link loads the app. | Ch 33 |
| **spec** | A file of tests, named with `.spec.ts`, run by Vitest. | Ch 24 |
| **Spring Boot** | The framework that starts the server and connects the application's classes with sensible defaults. | Ch 11 |
| **Spring Data JPA** | The Spring library that writes common database queries from repository interfaces. | Ch 14 |
| **Spring Security** | The Spring library that handles sign-in, sessions, roles and request protection. | Ch 15 |
| **SQL** | The language used to query and change a relational database. | Ch 9 |
| **SQL injection** | An attack that smuggles SQL commands into an input so that the database runs them. | Ch 9 |
| **stack trace** | The list of method calls that were active when an exception occurred, printed to help find the cause. | Ch 3 |
| **stacked pull requests** | Pull requests whose branches start from one another, so that each one includes the changes of the one before. | Ch 7 |
| **staging area** | In Git, the place where changes wait until you commit them. | Ch 7 |
| **staging directory** | A temporary folder where a render is built and checked before it is moved into place. | Ch 28 |
| **starter** | A single dependency that pulls in a matched set of libraries for one purpose, such as `spring-boot-starter-security`. | Ch 6 |
| **state machine** | A design that names the possible states and the allowed moves between them. | Ch 38 |
| **static factory method** | A static method, such as `Viewer.of(...)`, that returns a new object. | Ch 4 |
| **status code** | The three-digit number in a response that says what happened, such as `404`. | Ch 8 |
| **status line** | The first line of an HTTP response, holding the protocol version and the status code. | Ch 8 |
| **stopTimeout** | The ECS setting for how long a container gets to shut down after it is told to stop. | Ch 41 |
| **strategy** | A design pattern in which interchangeable pieces of behavior are chosen at run time. | Ch 38 |
| **stream** | A Java pipeline that processes a collection step by step, filtering, transforming and collecting. | Ch 5 |
| **string** | A sequence of characters; text. | Ch 3 |
| **stub** | A stand-in with just enough behavior for a test. | Ch 24 |
| **subnet** | A division of a VPC's addresses; a private subnet has no route from the internet. | Ch 40 |
| **supply chain** | The libraries and images your software depends on, which other people wrote. | Ch 6 |
| **synchronized** | A Java keyword that lets only one thread at a time run a block of code. | Ch 5 |
| **system test** | A test that exercises the whole running application, as opposed to one piece. | Ch 18 |
| **table** | A set of rows in a relational database, with named columns. | Ch 9 |
| **tag** | A permanent name for one Git commit, like a bookmark. | Ch 7 |
| **task** | One running copy of your containers, started by ECS from a task definition. | Ch 40 |
| **task definition** | The description that ECS runs: the image, CPU, memory, environment and health check. | Ch 40 |
| **task execution role** | The IAM role that ECS itself uses to pull the container image and read secrets for a task. | Ch 40 |
| **task role** | The IAM role that your own code uses inside an ECS task to call AWS services such as S3. | Ch 40 |
| **template** | In Angular, the HTML that shows a component's data. | Ch 21 |
| **template method** | A design pattern in which a fixed skeleton is written once and the caller supplies the variable step. | Ch 38 |
| **terminal** | A window in which you type commands to the computer instead of clicking. | Ch 2 |
| **Terraform** | A tool that creates cloud resources from files that describe them. | Ch 41 |
| **test** | Code that checks other code. | Ch 3 |
| **test isolation** | The rule that one test's outcome must never depend on another test having run. | Ch 24 |
| **TestBed** | Angular's helper that builds a component or service inside a test, with its dependencies. | Ch 24 |
| **Testcontainers** | A library that starts a real service, such as MySQL, in a Docker container for the length of a test. | Ch 18 |
| **text file** | A file that holds characters rather than, say, an image. | Ch 2 |
| **thread** | A path of execution inside a program; a web server handles many requests at once on many threads. | Ch 5 |
| **thread-safe** | Describes code that stays correct when several threads use it at once. | Ch 5 |
| **threat** | A specific way someone might misuse the system. | Ch 1 |
| **threat model** | A structured description of a system's assets, actors, entry points and threats. | Ch 32 |
| **throttling** | Limiting how many attempts are allowed in a period. | Ch 16 |
| **tile** | One small square piece of a rendered page image. | Ch 1 |
| **timer** | A metric that records how long something took. | Ch 35 |
| **TLS** | The protocol that encrypts traffic between a browser and a server; it is the S in HTTPS. | Ch 8 |
| **token** | A string that stands for a right to do something; here, a signed tile request. | Ch 3 |
| **token bucket** | A rate-limit method in which requests spend tokens that refill at a steady rate. | Ch 38 |
| **token-based authentication** | A design in which the browser holds a signed token that carries the sign-in itself, with no server-side record. | Ch 39 |
| **trace code** | The short code on every watermark that leads to the sign-in in the audit log. | Ch 29 |
| **transaction** | A group of database statements that all succeed or all fail. | Ch 9 |
| **transitive dependency** | A library that you did not list but that arrives because something you listed needs it. | Ch 6 |
| **Trivy** | A scanner that checks built container images for known vulnerabilities. | Ch 36 |
| **trust boundary** | The line between what a system controls, such as the server, and what it does not, such as the browser. | Ch 23 |
| **twelve-factor app** | A published method for building services that deploy cleanly, including keeping configuration in the environment. | Ch 39 |
| **type** | A label that says what kind of value something is. | Ch 3 |
| **TypeScript** | JavaScript with a type system that a compiler checks before the code runs. | Ch 19 |
| **unchecked exception** | A Java exception that the compiler does not force you to catch. | Ch 5 |
| **union type** | A TypeScript type that allows one of several listed alternatives. | Ch 19 |
| **unit test** | A small test of one piece of code on its own, with no framework or database running. | Ch 18 |
| **untracked** | Describes a file that Git can see but is not recording. | Ch 7 |
| **untrusted input** | Any data from outside your program, such as an upload, a form value or a header. | Ch 28 |
| **upsert** | A database write that inserts a row or, if one exists, updates it. | Ch 30 |
| **URL** | Uniform Resource Locator: a web address such as `https://example.com/page`. | Ch 1 |
| **user enumeration** | Discovering which usernames exist from differences in how a service answers. | Ch 15 |
| **UTC** | Coordinated Universal Time, the world's reference time, with no daylight-saving changes. | Ch 5 |
| **UTF-8** | The common encoding that stores each character in one to four bytes. | Ch 2 |
| **UUID** | A universally unique identifier: a long random-looking value, such as a document's id. | Ch 13 |
| **Valkey** | An open-source, Redis-compatible in-memory store. | Ch 40 |
| **value object** | A small immutable object defined by its values, with no identity of its own. | Ch 38 |
| **variable** | A named place that holds a value. | Ch 3 |
| **version control** | A tool that records every change to a project's files so that you can return to any earlier state. | Ch 2 |
| **virtual machine** | A whole computer simulated by software, which is heavier than a container. | Ch 10 |
| **visibility** | Whether a document is private (owner, administrators and shared users) or open to everyone. | Ch 1 |
| **Vitest** | The test runner used for the frontend's unit tests. | Ch 24 |
| **volume** | A place Docker keeps data outside a container so that it survives the container. | Ch 10 |
| **VPC (virtual private cloud)** | Your own private network inside AWS, divided into subnets. | Ch 40 |
| **VPC endpoint** | A connection that keeps traffic to an AWS service on AWS's own network. | Ch 41 |
| **watermark** | Text or a mark drawn onto content to identify who received it. | Ch 1 |
| **web ACL** | The set of AWS WAF rules that you attach to a load balancer or a CloudFront distribution. | Ch 41 |
| **workflow** | A YAML file in `.github/workflows/` that tells GitHub Actions what to run. | Ch 36 |
| **working directory** | The folder a shell or program is currently in; relative paths start here. | Ch 2 |
| **wrapper** | A small script that downloads and runs the right version of a tool, such as `mvnw` for Maven. | Ch 6 |
| **X-Forwarded-For** | A header in which proxies list the client addresses a request has passed through. | Ch 10 |
| **XML** | A text format in which information sits between named tags, used in `pom.xml`. | Ch 6 |
| **XSS (cross-site scripting)** | An attack that gets a hostile script to run inside a trusted page. | Ch 8 |
| **YAML** | A text format for configuration that uses indentation, used in `application.yml` and Compose files. | Ch 2 |
