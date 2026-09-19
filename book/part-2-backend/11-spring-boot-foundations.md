<!-- chapter: 11 | part: II | owner: writer-backend | tag: book-m6-final | status: draft -->
# Chapter 11: Spring Boot foundations

Everything the Secure Document Viewer does on the server (signing you in, listing documents, cutting a PDF page into tiles) runs inside one Java program built on Spring Boot. This chapter explains what a framework is, shows the smallest Spring Boot application in the repository, and teaches the two ideas the rest of Part II depends on: beans and dependency injection.

## Learning objectives

By the end of this chapter, you will be able to:

- Explain what a framework is and how it differs from a library.
- Describe what `@SpringBootApplication` does when the program starts.
- Explain what a bean is and how dependency injection supplies one to another class.
- Read a constructor-injected class such as `DocumentController` and name each of its dependencies.
- Configure the application through `application.yml` and environment variables.
- Explain what a starter dependency and auto-configuration are.

## Prerequisites

- Chapter 3: your first Java program (`main`, imports, `final`)
- Chapter 4: classes, objects, records and interfaces
- Chapter 6: Maven and the shape of a project
- Chapter 8: how the web works (HTTP requests and responses)

## Beginner tier: Someone else runs the show

### 11.1 What a framework is

Imagine you want to open a restaurant. You could build the building, wire the electricity, install plumbing, and only then start cooking. Or you could rent a fitted-out kitchen where the ovens, the extraction fans and the fire alarms already exist, and you bring your recipes. A **framework** is the fitted-out kitchen: a large body of ready-made code that already knows how to start a program, listen for web requests and connect to a database. You supply the recipes, which are the pieces specific to your application.

A **library** is a single tool you pick up when you need it, such as a PDF reader. You call the library. With a framework, the direction reverses: the framework calls you. Your code sits in classes that the framework finds, creates and invokes at the right moment. This reversal has a name, **inversion of control**, and it explains most of what looks strange about Spring at first.

**Where the analogy breaks down.** A rented kitchen has walls you can see. A framework's structure is mostly invisible: it's made of rules such as "any class with this label gets created at startup". When something goes wrong, you often need to know the rule to understand the behavior. Much of Part II is these rules.

Spring is the framework the project uses, and **Spring Boot** is a layer on top of it that chooses sensible defaults so a program can start with almost no setup. The project uses Spring Boot 4.1.1 on Java 25 (`pom.xml`, tag `book-m6-final`).

### 11.2 The first Spring Boot application

Listing 11.1 is the entire entry point of the server. It is short on purpose: nearly everything else is discovered by the framework.

**Listing 11.1 — `SecureDocViewerApplication.java` (`book-m6-final`)**

```java
package com.example.securedocviewer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SecureDocViewerApplication {
    public static void main(String[] args) {
        SpringApplication.run(SecureDocViewerApplication.class, args);
    }
}
```

Read it from the bottom up.

- `main` is the method Java runs first (Chapter 3). Its only job is to hand control to `SpringApplication.run`, which starts the framework. From this line on, Spring is in charge.
- Words that start with `@` are **annotations**: labels attached to a class or method that tell a tool how to treat it. They don't change what the code does by themselves; the framework reads them.
- `@SpringBootApplication` marks this class as the starting point. It switches on three behaviors: it tells Spring to search this package and every package beneath it (`com.example.securedocviewer...`) for classes to manage, which is called **component scanning**; it allows this class to declare extra configuration; and it turns on **auto-configuration**, which Section 11.5 explains.
- `@EnableScheduling` turns on the feature that runs methods on a timer. The project uses it for cleanup jobs that [Chapter 14](14-jpa-and-flyway.md) describes; for now, note that one annotation is enough to activate a whole capability.

### 11.3 Beans and dependency injection

When Spring starts, it creates one object of every class it finds that is labeled as a managed component (with annotations such as `@Component`, `@Service`, `@RestController` or `@Configuration`). Each such object is a **bean**, and the box that holds them all is the **application context**. By default there is one instance of each bean, shared by the whole program.

Why not just write `new DocumentService(...)` wherever you need one? Because a `DocumentService` needs a `DocumentRepository`, which needs a database connection, which needs configuration. If every class built its own helpers, you'd repeat that wiring everywhere and couldn't swap a helper for a fake in a test. Instead, a class declares what it needs, and Spring hands it over. That is **dependency injection**.

Listing 11.2 shows it in the smallest useful example in the repository.

**Listing 11.2 — Constructor injection in `DocumentController.java` (`book-m6-final`, simplified: only the first lines of the class are shown)**

```java
@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    // ... two nested records omitted ...

    private final DocumentService documents;
    private final RequestActors actors;

    public DocumentController(DocumentService documents, RequestActors actors) {
        this.documents = documents;
        this.actors = actors;
    }

    // ... request-handling methods omitted ...
}
```

The constructor lists two parameters. When Spring builds the controller, it looks in the application context for a bean of type `DocumentService` and one of type `RequestActors`, and passes them in. Nothing in the class says where they come from, which is the point: `DocumentController` only says what it needs. The fields are `final`, so once the object exists its dependencies can't change or be missing. This style is called **constructor injection**, and every controller and service in the project uses it.

Dependencies form a chain. `DocumentService` in turn asks for a `DocumentRepository`, an `AppUserRepository`, a `TileGenerationService`, an `AuditLogService` and a `PlatformTransactionManager`. Spring works out the order and builds them from the bottom up. If one can't be found, the program refuses to start and names the missing type, so a wiring mistake shows up at startup and never in front of a user.

**A note on how Spring chooses.** Spring picks a dependency by type, not by name. If two beans of the same type exist, Spring can't choose without more information, and startup fails. `KnownDevices` shows a related detail: it has two constructors (one public for Spring, and one visible only inside its own package, which the unit tests use to supply a fixed clock), so the public one carries an explicit `@Autowired` label to say "use this one".

## Intermediate tier: Configuring the program from outside

### 11.4 Configuration files: `application.yml`, profiles, environment variables

Some values must change between your laptop and a real server: the database address, a secret key, whether cookies require HTTPS. Hard-coding them would mean rebuilding the program for every machine. Spring Boot reads them from outside the code, mainly from `src/main/resources/application.yml`, a file in the YAML format (indented `key: value` lines, where indentation means nesting).

Listing 11.3 shows two excerpts.

**Listing 11.3 — `application.yml` (`book-m6-final`, simplified: two excerpts, comments trimmed)**

```yaml
server:
  port: 8080
  servlet:
    session:
      timeout: 30m
      cookie:
        name: SDV_SESSION
        http-only: true
        same-site: strict
        secure: ${SESSION_COOKIE_SECURE:false}

spring:
  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: none
```

Two things to notice. First, `${SESSION_COOKIE_SECURE:false}` is a **property placeholder**: use the environment variable `SESSION_COOKIE_SECURE` if it exists, otherwise `false`. The same file therefore works on a laptop (plain HTTP, the default) and in production (the variable set to `true`). Second, keys like `server.servlet.session.timeout` are ones Spring Boot defines itself; you set them and the framework obeys. The project's own settings live under the `secure-doc-viewer:` block, which [Chapter 13](13-validation-and-errors.md) turns into a typed Java class (`ViewerProperties`).

A **profile** is a named set of extra settings. The tests use one: `@ActiveProfiles("test")` makes Spring also read `application-test.yml`, which points the datasource at an in-memory H2 database instead of MySQL ([Chapter 14](14-jpa-and-flyway.md) and [Chapter 18](18-testing-the-backend.md) cover why). Real environment variables win over the file, and a git-ignored `.env` file is imported for local development with `spring.config.import: optional:file:.env[.properties]`. That is why secrets such as `SIGNING_SECRET` never appear in the repository.

### 11.5 Starters and auto-configuration

Look at the dependencies in `pom.xml` and you'll see names like `spring-boot-starter-webmvc`, `spring-boot-starter-security`, `spring-boot-starter-data-jpa` and `spring-boot-starter-flyway`. A **starter** is a single dependency that pulls in a matched set of libraries for one job. Adding `spring-boot-starter-security` brings Spring Security and what it needs, at versions known to work together. A comment in the project's `pom.xml` notes that Spring Boot 4 splits the old all-in-one starters into focused ones, which is why the web starter is named `webmvc`.

**Auto-configuration** is the second half of the trick. When Spring Boot starts, it looks at what is on the classpath and at your settings, and creates sensible beans for you. With the JPA starter and a MySQL driver present and `spring.datasource.url` set, it builds the database connection pool and the transaction manager without you writing a line. If you define your own bean of the same kind, yours takes priority. `SecurityConfig` does this when it declares its own `PasswordEncoder` and `SecurityFilterChain` ([Chapter 15](15-spring-security-authentication.md)).

We simplify here: the full list of what is auto-configured is long and changes between versions. You don't need to memorize it; you need to know it exists, so that when a bean appears that you never wrote, you know where it came from.

### 11.6 Logging and startup output

A running server has no screen to show what it's doing, so it writes **log** lines to the console. Spring Boot configures logging by default, and the project writes to it through SLF4J, a common logging interface. Two real examples from the code: `BootstrapAdmin` logs a message at `info` level when it creates the first administrator, and `GlobalExceptionHandler` logs unexpected failures at `error` level with a short reference code, so a support report can be matched to a log line ([Chapter 13](13-validation-and-errors.md)).

This book doesn't reproduce a startup log, because its exact text depends on your machine and versions. When you run the app, read the output from the top: the framework reports the port it listens on and any bean that failed to build. If startup fails, the last error message near the bottom usually names the missing bean or setting.

## Advanced tier: Failing early, and where the seams are

### 11.7 Fail at startup, not at the first request

The most useful property of this wiring is that mistakes surface when the program starts. A missing dependency stops startup with an error naming the type. `ViewerProperties` goes further: its signing secret must be present and at least 32 characters, or the application refuses to start (Chapter 13 shows how). The comment in the source states the reason: startup should fail "rather than failing on the first tile". A server that started with a broken secret would only fail later, in front of a user.

### 11.8 Annotations are read by the framework, so calls matter

Annotations such as `@Transactional` work because Spring wraps the bean in a proxy that adds behavior around your methods. The wrapper only runs when the call comes from outside the bean, a consequence Chapter 14 returns to. The project's `DocumentService` doesn't use the annotation; it manages transactions explicitly, because rendering a PDF is slow and must run outside any database transaction (Chapter 14).

## In this project

**Table 11.1 — Where Chapter 11's ideas live (`book-m6-final`)**

| Idea | File |
|---|---|
| Entry point, scheduling switch | `src/main/java/com/example/securedocviewer/SecureDocViewerApplication.java` |
| Constructor injection | `controller/DocumentController.java` and the other controllers |
| Settings, placeholders | `src/main/resources/application.yml` |
| Test profile | `src/test/resources/application-test.yml` |
| Starters | `pom.xml` |

## Try it

1. (★) Open `application.yml` and find the session cookie name. Which line makes the cookie unreadable to JavaScript?
2. (★) In `DocumentController`, list every dependency injected by the constructor.
3. (★★) `BootstrapAdmin` reads two settings with `@Value` rather than `ViewerProperties`. Find them and their default values.
4. (★★★) Explain why `KnownDevices` needs `@Autowired` on one constructor while the project's other classes don't.

## Summary

- A framework calls your code; a library is called by it.
- `@SpringBootApplication` starts component scanning and auto-configuration; `SpringApplication.run` starts everything.
- Managed objects are beans, held in the application context and supplied to each other by constructor injection, matched by type.
- `application.yml`, environment variables and profiles let one build run in many places; `${NAME:default}` placeholders bridge them.
- Starters bundle dependencies; auto-configuration builds beans from what it finds, and your own beans take priority.
- Mistakes in wiring or required settings stop the program at startup, which is where you want them.

## Further reading

- *Spring Boot Reference Documentation*, "Externalized Configuration." https://docs.spring.io/spring-boot/reference/features/external-config.html
- *Spring Framework Reference Documentation*, "The IoC Container." https://docs.spring.io/spring-framework/reference/core/beans.html
- *Spring Boot Reference Documentation*, "Auto-configuration." https://docs.spring.io/spring-boot/reference/using/auto-configuration.html
