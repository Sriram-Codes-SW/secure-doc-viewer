# Glossary

Owner: `editor`. Writers request additions in `book/_team/requests.md`:
`- [writer → editor] glossary: **term** — definition. (Ch N, Section N.M)`.
Entries are alphabetical. "First defined" is the chapter where the term is introduced; "planned"
means the outline's intended home, to be confirmed as chapters land.

| Term | Definition | First defined |
|---|---|---|
| **audit trail** | A permanent record of who did what and when, kept so events can be investigated later. | Ch 27 (planned) |
| **authentication** | Proving who you are, for example with a username and password. | Ch 15 (planned) |
| **authorization** | Deciding what an authenticated person is allowed to do. | Ch 15 (planned) |
| **backend** | The part of an application that runs on a server and holds the data and rules. | Ch 1 (planned) |
| **build tool** | A program that compiles code, runs tests and packages the result; Maven for this project's Java. | Ch 6 (planned) |
| **container** | A running, isolated instance of an image: a program packaged with everything it needs. | Ch 10 (planned) |
| **cookie** | A small piece of data a server asks the browser to store and send back with later requests. | Ch 8 (planned) |
| **CSRF (Cross-Site Request Forgery)** | An attack in which another website makes your browser send a request to a site you are signed in to. | Ch 16 (planned) |
| **dependency injection** | Giving an object the other objects it needs instead of letting it create them itself. | Ch 11 (planned) |
| **entity** | A Java class whose objects are stored as rows in a database table. | Ch 14 (planned) |
| **Flyway** | A tool that applies numbered SQL migration files to a database in order. | Ch 14 (planned) |
| **frontend** | The part of an application that runs in the user's browser. | Ch 1 (planned) |
| **HMAC** | A keyed hash: a short code only someone holding the secret key can compute, used to detect tampering. | Ch 17 (planned) |
| **HTTP** | The rules browsers and servers use to exchange requests and responses. | Ch 8 (planned) |
| **image (Docker)** | A read-only template from which containers are started. | Ch 10 (planned) |
| **JDK** | The Java Development Kit: the compiler and tools needed to build Java programs. | Ch 3 (planned) |
| **JSON** | A text format for structured data made of objects, lists, strings, numbers and booleans. | Ch 8 (planned) |
| **JVM** | The Java Virtual Machine: the program that runs compiled Java. | Ch 3 (planned) |
| **migration** | A versioned change to a database schema, applied once and in order. | Ch 9 (planned) |
| **rate limit** | A cap on how many requests a user may make in a period. | Ch 26 (planned) |
| **record (Java)** | A compact class for immutable plain data. | Ch 4 (planned) |
| **REST** | A style of web API built around URLs for things and HTTP methods for actions. | Ch 12 (planned) |
| **session** | The server's memory of a signed-in visitor across several requests. | Ch 8 (planned) |
| **signed URL** | A URL carrying a signature the server can verify, so it cannot be altered or forged. | Ch 25 (planned) |
| **SQL** | The language used to query and change a relational database. | Ch 9 (planned) |
| **tile** | One small square piece of a rendered page image. | Ch 1 (planned) |
| **token** | A string that stands for a right to do something; here, a signed tile request. | Ch 25 (planned) |
| **TLS** | The protocol that encrypts traffic between browser and server (the "S" in HTTPS). | Ch 8 (planned) |
| **watermark** | Text or a mark drawn onto content to identify who received it. | Ch 25 (planned) |

## Requested terms, awaiting definitions (from requests.md, window 2)

- Ch 11 (writer-backend): framework, inversion of control, bean, application context, dependency injection, constructor injection, annotation, component scanning, starter, auto-configuration, profile, property placeholder, relaxed binding. The writer supplies definitions; the editor merges them into the table above.
- Ch 1 (writer-foundations): rasterize, tile, DPI, client, server, naive viewer, watermark, signed URL, rate limit, audit trail.
