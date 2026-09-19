# Glossary

Owner: `editor`. Writers request additions in `book/_team/requests.md`:
`- [writer → editor] glossary: **term** — definition. (Ch N, Section N.M)`.
Entries are alphabetical. "First defined" is the chapter where the term is introduced (per the outline; confirmed as chapters land).

| Term | Definition | First defined |
|---|---|---|
| **annotation (Java)** | A marker such as `@Service` placed on code that tells a framework or the compiler something about it. | Ch 4, used in Ch 11 |
| **application context** | The box that holds all of the app's beans. | Ch 11 |
| **arrow function** | A short way to write a function in JavaScript and TypeScript, using `=>`. | Ch 19 |
| **audit trail** | An append-only record, kept in the database, of important actions such as sign-ins, uploads, views and denied requests. | Ch 1 |
| **authentication** | Proving who you are, for example with a username and password. | Ch 8 |
| **authorization** | Deciding what an authenticated person is allowed to do. | Ch 15  |
| **auto-configuration** | Spring Boot's habit of setting up sensible defaults for the libraries it finds on the classpath. | Ch 11 |
| **backend** | The part of an application that runs on a server and holds the data and rules. | Ch 1  |
| **Bean Validation** | The Java standard for declaring input rules with annotations such as `@NotBlank`. | Ch 13 |
| **bean** | An object Spring creates and manages, from a class labeled with annotations such as `@Component`, `@Service` or `@RestController`. | Ch 11 |
| **browser** | The program (Chrome, Firefox, Safari, Edge) that requests web pages and runs their code. | Ch 19 |
| **build tool** | A program that compiles code, runs tests and packages the result; Maven for this project's Java. | Ch 6  |
| **bytecode** | The compact instructions the Java compiler produces, which the JVM runs. | Ch 3 |
| **cache** | A place where a copy of something is kept so it can be served again faster. | Ch 1 |
| **client** | A program that asks for things over a network; your browser is a client. | Ch 1 |
| **compiler** | A program that checks source code and translates it into a form a machine can run. | Ch 3 |
| **component scanning** | Spring searching your packages for annotated classes and registering them as beans. | Ch 11 |
| **constructor injection** | Dependency injection where an object receives its dependencies as constructor parameters. | Ch 11 |
| **container** | A running, isolated instance of an image: a program packaged with everything it needs. | Ch 10  |
| **cookie** | A small piece of data a server asks the browser to store and send back with later requests. | Ch 8  |
| **CSRF (Cross-Site Request Forgery)** | An attack in which another website makes your browser send a request to a site you are signed in to. | Ch 26 |
| **decompression bomb** | A small file that expands to an enormous amount of data (or pixels) when processed, exhausting memory. | Ch 13 |
| **dependency injection** | Giving an object the other objects it needs instead of letting it create them itself. | Ch 11  |
| **deserialization** | Turning text or bytes, such as JSON, back into an object. | Ch 12 |
| **developer tools** | The inspection panel built into browsers that shows requests, code and errors. | Ch 1 |
| **discriminated union** | A TypeScript union of object types that each carry a fixed tag field, so code can tell them apart. | Ch 19 |
| **DPI** | Dots per inch: how many pixels represent one inch of a page when it is rendered as an image. | Ch 1 |
| **encoding** | The rule that maps characters to bytes; the project's files use UTF-8. | Ch 2 |
| **entity** | A Java class whose objects are stored as rows in a database table. | Ch 14  |
| **environment variable** | A named setting, such as `DB_PASSWORD`, that the operating system hands to every program it starts. | Ch 2 |
| **exception handler** | A method that turns a particular exception into a controlled HTTP response. | Ch 13 |
| **exception** | An object that signals something went wrong and interrupts normal flow until it is handled. | Ch 3 |
| **Flyway** | A tool that applies numbered SQL migration files to a database in order. | Ch 9 |
| **framework** | A library that supplies the structure of an application and calls your code, rather than the reverse. | Ch 6 |
| **frontend** | The part of an application that runs in the user's browser. | Ch 1  |
| **HMAC** | A keyed hash: a short code only someone holding the secret key can compute, used to detect tampering. | Ch 17  |
| **HTTP** | The rules browsers and servers use to exchange requests and responses. | Ch 8  |
| **image (Docker)** | A read-only template from which containers are started. | Ch 10  |
| **interface (TypeScript)** | A description of the shape an object must have. | Ch 4 |
| **inversion of control** | The principle that a framework, not your code, creates objects and decides when to call them. | Ch 11 |
| **Jackson** | The Java library that converts between objects and JSON. | Ch 12 |
| **JavaScript** | The programming language that browsers run. | Ch 19 |
| **JDK** | The Java Development Kit: the compiler and tools needed to build Java programs. | Ch 3  |
| **JSON** | A text format for structured data made of objects, lists, strings, numbers and booleans. | Ch 8  |
| **JVM** | The Java Virtual Machine: the program that runs compiled Java. | Ch 3  |
| **line ending** | The mark for where a line stops: Windows uses two bytes (carriage return plus line feed), macOS and Linux use one (line feed). | Ch 2 |
| **localhost** | The name a computer uses for itself; requests to it never leave the machine. | Ch 2 |
| **method** | A named block of code that does one job and can be called by name. | Ch 3 |
| **migration** | A versioned change to a database schema, applied once and in order. | Ch 9  |
| **module (TypeScript)** | A file that exports some values and imports others. | Ch 19 |
| **multipart** | The request format that carries files and form fields together, used for uploads. | Ch 12 |
| **naive viewer** | A viewer that only hides its download button in the browser, so anyone can still fetch the file. | Ch 1 |
| **operator** | A symbol such as `+` or `==` that combines or compares values. | Ch 3 |
| **parameter** | A named input a method declares; the values passed in are its arguments. | Ch 3 |
| **path** | The address of a file or folder, written with slashes between the folder names. | Ch 2 |
| **PDF** | Portable Document Format: a file type that describes pages so they look the same everywhere. | Ch 1 |
| **port** | A numbered door on your computer, from 0 to 65535, at which a server process listens for network requests addressed to that number. | Ch 2 |
| **process** | A running program; each one gets its own number. | Ch 2 |
| **profile (Spring)** | A named set of extra settings switched on for a purpose, such as the test profile that points the datasource at an in-memory H2 database. | Ch 11 |
| **program** | A list of instructions a computer follows. | Ch 3 |
| **prompt** | The text a shell shows to say it is ready for your next command. | Ch 2 |
| **property placeholder** | A `${NAME:default}` marker in configuration that Spring replaces with an environment or file value. | Ch 11 |
| **rasterize** | To turn a page description (such as a PDF page) into a grid of pixels. | Ch 1 |
| **rate limit** | A cap on how many requests a user may make in a period. | Ch 1 |
| **record (Java)** | A one-line way to declare a class that only carries data; Java generates the constructor and read methods. | Ch 4  |
| **relaxed binding** | Spring's rule that `signing-secret`, `SIGNING_SECRET` and `signingSecret` all name the same setting. | Ch 11 |
| **resource** | A thing a REST API exposes at a URL, such as a document. | Ch 12 |
| **REST** | A style of web API built around URLs for things and HTTP methods for actions. | Ch 12  |
| **return value** | The result a method hands back to the code that called it. | Ch 3 |
| **serialization** | Turning an object into text or bytes, such as JSON, so it can be sent or stored. | Ch 12 |
| **server** | The program that answers requests from clients; here, the Spring Boot app. | Ch 1 |
| **session** | The server's record that a particular browser has proven who it is, so the browser does not have to send the password again with every request. | Ch 1 (in depth: Ch 8) |
| **shell** | The program inside a terminal that reads your commands and runs them (for example Bash or PowerShell). | Ch 2 |
| **signature** | A value computed from data and a secret key that proves the data was not changed. | Ch 1 |
| **signed URL** | A URL carrying a signature the server can verify, so it cannot be altered or forged. | Ch 1 |
| **source code** | The human-readable text of a program, before it is compiled. | Ch 3 |
| **SQL** | The language used to query and change a relational database. | Ch 9  |
| **stack trace** | The list of method calls that were active when an exception occurred, printed to help find the cause. | Ch 3 |
| **starter (Spring Boot)** | A single dependency that pulls in a matched set of libraries for one job, for example `spring-boot-starter-security`. | Ch 6 |
| **string** | A sequence of characters; text. | Ch 3 |
| **text file** | A file that holds characters (rather than, say, an image). | Ch 2 |
| **terminal** | A window in which you type commands to the computer instead of clicking. | Ch 2 |
| **tile** | One small square piece of a rendered page image. | Ch 1  |
| **TLS** | The protocol that encrypts traffic between browser and server (the "S" in HTTPS). | Ch 8  |
| **token** | A string that stands for a right to do something; here, a signed tile request. | Ch 3 |
| **type** | A label saying what kind of value something is (text, number, list, a custom shape). | Ch 3 |
| **TypeScript** | JavaScript plus a type system, checked by a compiler before the code runs. | Ch 19 |
| **union type** | A type that allows one of several listed alternatives. | Ch 19 |
| **variable** | A named place that holds a value. | Ch 3 |
| **watermark** | Text or a mark drawn onto content to identify who received it. | Ch 1 |
| **working directory** | The folder a shell (or a program) is currently "in"; relative paths are read from here. | Ch 2 |

Definitions for requested terms were drafted by the editor; the chapter text is authoritative and the editor reconciles differences.
