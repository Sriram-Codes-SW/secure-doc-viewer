# Glossary

Owner: `editor`. Writers request additions in `book/_team/requests.md`:
`- [writer → editor] glossary: **term** — definition. (Ch N, Section N.M)`.
Entries are alphabetical. "First defined" is the chapter where the term is introduced (per the outline; confirmed as chapters land).

| Term | Definition | First defined |
|---|---|---|
| **annotation (Java)** | A marker such as `@Service` placed on code that tells a framework or the compiler something about it. | Ch 4, used in Ch 11 |
| **application context** | Spring's container: the object that creates, holds and wires together all of the app's beans. | Ch 11 |
| **arrow function** | A short way to write a function in JavaScript and TypeScript, using `=>`. | Ch 19 |
| **audit trail** | A permanent record of who did what and when, kept so events can be investigated later. | Ch 27  |
| **authentication** | Proving who you are, for example with a username and password. | Ch 15  |
| **authorization** | Deciding what an authenticated person is allowed to do. | Ch 15  |
| **auto-configuration** | Spring Boot's habit of setting up sensible defaults for the libraries it finds on the classpath. | Ch 11 |
| **backend** | The part of an application that runs on a server and holds the data and rules. | Ch 1  |
| **bean** | An object created and managed by Spring. | Ch 11 |
| **browser** | The program (Chrome, Firefox, Safari, Edge) that requests web pages and runs their code. | Ch 19 |
| **build tool** | A program that compiles code, runs tests and packages the result; Maven for this project's Java. | Ch 6  |
| **client** | The program that asks for something over a network; for this app, the browser. | Ch 1 |
| **compiler** | A program that checks source code and translates it into a form a machine can run. | Ch 19 |
| **component scanning** | Spring searching your packages for annotated classes and registering them as beans. | Ch 11 |
| **constructor injection** | Dependency injection where an object receives its dependencies as constructor parameters. | Ch 11 |
| **container** | A running, isolated instance of an image: a program packaged with everything it needs. | Ch 10  |
| **cookie** | A small piece of data a server asks the browser to store and send back with later requests. | Ch 8  |
| **CSRF (Cross-Site Request Forgery)** | An attack in which another website makes your browser send a request to a site you are signed in to. | Ch 16  |
| **dependency injection** | Giving an object the other objects it needs instead of letting it create them itself. | Ch 11  |
| **discriminated union** | A TypeScript union of object types that each carry a fixed tag field, so code can tell them apart. | Ch 19 |
| **DPI** | Dots per inch: how many pixels represent one inch of a page when it is rendered as an image. | Ch 1 |
| **entity** | A Java class whose objects are stored as rows in a database table. | Ch 14  |
| **Flyway** | A tool that applies numbered SQL migration files to a database in order. | Ch 14  |
| **framework** | A library that supplies the structure of an application and calls your code, rather than the reverse. | Ch 11 |
| **frontend** | The part of an application that runs in the user's browser. | Ch 1  |
| **HMAC** | A keyed hash: a short code only someone holding the secret key can compute, used to detect tampering. | Ch 17  |
| **HTTP** | The rules browsers and servers use to exchange requests and responses. | Ch 8  |
| **image (Docker)** | A read-only template from which containers are started. | Ch 10  |
| **interface (TypeScript)** | A description of the shape an object must have. | Ch 19 |
| **inversion of control** | The principle that a framework, not your code, creates objects and decides when to call them. | Ch 11 |
| **JavaScript** | The programming language that browsers run. | Ch 19 |
| **JDK** | The Java Development Kit: the compiler and tools needed to build Java programs. | Ch 3  |
| **JSON** | A text format for structured data made of objects, lists, strings, numbers and booleans. | Ch 8  |
| **JVM** | The Java Virtual Machine: the program that runs compiled Java. | Ch 3  |
| **migration** | A versioned change to a database schema, applied once and in order. | Ch 9  |
| **module (TypeScript)** | A file that exports some values and imports others. | Ch 19 |
| **naive viewer** | A viewer that only hides its download button in the browser, so anyone can still fetch the file. | Ch 1 |
| **profile (Spring)** | A named set of configuration that is switched on for a particular environment. | Ch 11 |
| **property placeholder** | A `${NAME:default}` marker in configuration that Spring replaces with an environment or file value. | Ch 11 |
| **rasterize** | To turn a page description (such as a PDF page) into a grid of pixels. | Ch 1 |
| **rate limit** | A cap on how many requests a user may make in a period. | Ch 26  |
| **record (Java)** | A compact class for immutable plain data. | Ch 4  |
| **relaxed binding** | Spring's rule that `signing-secret`, `SIGNING_SECRET` and `signingSecret` all name the same setting. | Ch 11 |
| **REST** | A style of web API built around URLs for things and HTTP methods for actions. | Ch 12  |
| **server** | The program that answers requests from clients; here, the Spring Boot app. | Ch 1 |
| **session** | The server's memory of a signed-in visitor across several requests. | Ch 8  |
| **signed URL** | A URL carrying a signature the server can verify, so it cannot be altered or forged. | Ch 25  |
| **SQL** | The language used to query and change a relational database. | Ch 9  |
| **starter (Spring Boot)** | A single dependency that pulls in a matched set of libraries for one purpose. | Ch 11 |
| **tile** | One small square piece of a rendered page image. | Ch 1  |
| **TLS** | The protocol that encrypts traffic between browser and server (the "S" in HTTPS). | Ch 8  |
| **token** | A string that stands for a right to do something; here, a signed tile request. | Ch 25  |
| **type** | A label saying what kind of value something is (text, number, list, a custom shape). | Ch 19 |
| **TypeScript** | JavaScript plus a type system, checked by a compiler before the code runs. | Ch 19 |
| **union type** | A type that allows one of several listed alternatives. | Ch 19 |
| **watermark** | Text or a mark drawn onto content to identify who received it. | Ch 25  |

Definitions for terms requested through requests.md were drafted by the editor from the request lists; the chapter text is authoritative and the editor will reconcile any differences.
