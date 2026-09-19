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
- `@EnableScheduling` turns on the feature that runs methods on a timer. The project uses it for cleanup jobs that later chapters describe; for now, note that one annotation is enough to activate a whole capability.

### 11.3 Beans and dependency injection

When Spring starts, it creates one object of every class it finds that is labeled as a managed component (with annotations such as `@Component`, `@Service`, `@RestController` or `@Configuration`). Each such object is a **bean**, and the box that holds them all is the **application context**. By default there is one instance of each bean, shared by the whole program.

Why not just write `new DocumentService(...)` wherever you need one? Because a `DocumentService` needs a `DocumentRepository`, which needs a database connection, which needs configuration. If every class built its own helpers, you'd repeat that wiring everywhere and couldn't swap a helper for a fake in a test. Instead, a class declares what it needs, and Spring hands it over. That is **dependency injection**.

Listing 11.2 shows it in the smallest useful example in the repository.

**Listing 11.2 — Constructor injection in `DocumentController.java` (`book-m6-final`, simplified: only the first lines of the class are shown)**

```java
@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documents;
    private final RequestActors actors;

    public DocumentController(DocumentService documents, RequestActors actors) {
        this.documents = documents;
        this.actors = actors;
    }
```

The constructor lists two parameters. When Spring builds the controller, it looks in the application context for a bean of type `DocumentService` and one of type `RequestActors`, and passes them in. Nothing in the class says where they come from, which is the point: `DocumentController` only says what it needs. The fields are `final`, so once the object exists its dependencies can't change or be missing. This style is called **constructor injection**, and every controller and service in the project uses it.

Dependencies form a chain. `DocumentService` in turn asks for a `DocumentRepository`, an `AppUserRepository`, a `TileGenerationService`, an `AuditLogService` and a `PlatformTransactionManager`. Spring works out the order and builds them from the bottom up. If one can't be found, the program refuses to start and names the missing type, so a wiring mistake shows up at startup and never in front of a user.

**Where the analogy breaks down.** Handing over parts like a stock room suggests Spring picks by name. It picks by type. If two beans of the same type exist, Spring can't choose without more information, and startup fails. `KnownDevices` shows one related detail: it has two constructors (one public for Spring, one package-private for tests that supply a fixed clock), so the public one carries an explicit `@Autowired` label to say "use this one".

<!-- Sections 11.4 to 11.6, the Intermediate and Advanced tiers, In this project, Try it, Summary and Further reading are still to be written. -->
