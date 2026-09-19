<!-- chapter: 5 | part: I | owner: writer-foundations | tag: book-m6-final | status: draft -->
# Chapter 5: Collections, generics, lambdas and exceptions

Programs rarely handle one value at a time. The app lists many documents, tracks many failed sign-ins and stores many tiles. This chapter teaches how Java holds groups of values, how to process them concisely, and how to signal and handle failure. It finishes with time, which the app treats with unusual care.

## Learning objectives

By the end of this chapter, you will be able to:

- Choose between a list, a set and a map.
- Read a generic type such as `Map<String, Deque<Instant>>`.
- Read a lambda and a short stream pipeline.
- Explain why `Optional` exists and use `orElseThrow`.
- Throw, catch and define an exception.
- Explain what try-with-resources does.
- Explain why the app stores times in UTC.

## Prerequisites

- Chapter 4: Classes, objects, records and interfaces

## Beginner tier: Groups of values

### 5.1 Lists, sets and maps

A **collection** is an object that holds many values. Three kinds cover almost everything in this app.

- A **list** keeps items in order and allows duplicates. Example: the documents on a library page, newest first.
- A **set** keeps each item at most once. Example: the users a document is shared with; sharing twice with the same user changes nothing.
- A **map** connects **keys** to **values**, like a dictionary. Example: a username mapped to that user's recent failed sign-ins.

Example 5.1 shows all three. (`var` lets the compiler work out the type from the right-hand side.)

**Example 5.1 — The three collections**

```java
import java.util.*;

public class Collections101 {
    public static void main(String[] args) {
        var pages = new ArrayList<String>();      // list
        pages.add("cover");
        pages.add("contents");
        System.out.println(pages.get(0));         // cover (counting starts at 0)

        var sharedWith = new HashSet<String>();   // set
        sharedWith.add("reader.one");
        sharedWith.add("reader.one");             // ignored: already there
        System.out.println(sharedWith.size());    // 1

        var failures = new HashMap<String, Integer>();   // map
        failures.put("pub.one", 2);
        System.out.println(failures.get("pub.one"));     // 2
    }
}
```

Save it as `Collections101.java` and run it with `java Collections101.java` (Chapter 3).

The real app uses each. A document keeps its shares as `Set<AppUser> sharedWith = new HashSet<>()`, and the sign-in throttle keeps a map from a key to a queue of timestamps. <!-- source: Document.java line 68 and LoginThrottle.java line 61 at book-m6-final -->

### 5.2 Generics in plain words

What are the angle brackets in `ArrayList<String>`? They're a **generic** parameter: they say what type of thing the collection holds. A `List<String>` holds only text, and the compiler refuses to let you add a number. Without generics you'd find out at runtime, with a crash.

Read a generic type from the outside in. The throttle's field is `Map<String, Deque<Instant>>`:

- a map,
- whose keys are `String`,
- and whose values are `Deque<Instant>`, a **deque** (double-ended queue: you can add and remove at both ends) of `Instant` values, which are moments in time (Section 5.7).

That is: for each key, a queue of when the failures happened. Reading types like this is a skill you'll use all through Parts II and III.

## Intermediate tier: Processing and failing

### 5.3 Lambdas and streams

A **lambda** is a small unnamed method you can pass around. `u -> u.getUsername()` means "given `u`, produce `u.getUsername()`". A **stream** is a pipeline that processes a collection step by step: filter some items, transform others, collect a result.

**Example 5.2 — Filter and transform**

```java
import java.util.List;

public class StreamDemo {
    public static void main(String[] args) {
        List<String> names = List.of("pub.one", "reader.one", "outsider.one");
        List<String> shortNames = names.stream()
                .filter(n -> n.length() < 10)
                .map(n -> n.toUpperCase())
                .toList();
        System.out.println(shortNames);   // [PUB.ONE]
    }
}
```

`filter` keeps items for which the lambda is true, `map` transforms each item, and `toList()` ends the pipeline. The app uses the same idea to turn stored documents into rows for the library page.

**Listing 5.1 — `DocumentService.java` (book-m6-final, excerpt: method `list`)**

```java
    public List<DocumentSummary> list(Viewer viewer) {
        return tx.execute(status -> {
            List<Document> visible = viewer.admin()
                    ? documents.findAllWithOwner()
                    : documents.findVisibleTo(viewer.username(), Visibility.EVERYONE);
            return visible.stream().map(d -> summary(d, viewer)).toList();
        });
    }
```

*Path: `src/main/java/com/example/securedocviewer/document/DocumentService.java`*

Three things to notice. The `? :` is a compact `if`: administrators get every document, everyone else only those visible to them. `visible.stream().map(d -> summary(d, viewer)).toList()` converts each `Document` to a `DocumentSummary`. And `status -> { ... }` is a lambda handed to `tx.execute`, which runs it inside a database transaction (Chapter 9 and Chapter 14). We simplify here: streams have many more operations, but the app needs mostly `map`, `filter`, `anyMatch` and `toList`.

### 5.4 Optional and the trouble with null

Java has a special value, `null`, meaning "no object here". Calling a method on `null` crashes with a `NullPointerException`, so forgetting to check is a common bug. **Optional** is a box that is either empty or holds one value; a method that returns `Optional<AppUser>` tells the caller "there may be no user, decide what to do".

The user repository declares exactly that, and callers decide:

**Listing 5.2 — `AppUserRepository.java` (book-m6-final, excerpt)**

```java
    Optional<AppUser> findByUsername(String username);
```

*Path: `src/main/java/com/example/securedocviewer/account/AppUserRepository.java`*

**Listing 5.3 — `UserAccountService.java` (book-m6-final, excerpt: line 155)**

```java
                .orElseThrow(() -> new ResourceNotFoundException("No such user: " + username));
```

*Path: `src/main/java/com/example/securedocviewer/account/UserAccountService.java`*

`orElseThrow` means: give me the value, or if the box is empty, throw this exception. The `() -> new ...` lambda builds the exception only when needed.

### 5.5 Exceptions: throwing, catching, custom types

An **exception** is an object that represents a failure. When code throws one, normal execution stops and the exception travels up the chain of callers until something **catches** it.

**Example 5.3 — Catching**

```java
public class CatchDemo {
    static int tileCount(int lengthPx, int tileSize) {
        if (lengthPx <= 0 || tileSize <= 0) {
            throw new IllegalArgumentException("lengthPx and tileSize must both be positive");
        }
        return (lengthPx + tileSize - 1) / tileSize;
    }

    public static void main(String[] args) {
        try {
            int cols = tileCount(0, 512);
            System.out.println(cols);
        } catch (IllegalArgumentException e) {
            System.out.println("Bad input: " + e.getMessage());
        }
    }
}
```

The method is a copy of the app's `TileGrid.tileCount` from Chapter 3, so the example stands alone. The `println(cols)` line never runs: the exception jumps straight to the `catch` block.

The app defines its own exception types so that each failure has a meaning. The smallest is one line of substance.

**Listing 5.4 — `ResourceNotFoundException.java` (book-m6-final)**

```java
package com.example.securedocviewer.exception;

/** Mapped to 404. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

*Path: `src/main/java/com/example/securedocviewer/exception/ResourceNotFoundException.java`*

`extends RuntimeException` means "is a kind of `RuntimeException`" (inheritance: the new class gets everything the old one has). `super(message)` passes the message to it. The comment says where it ends up: a single class, `GlobalExceptionHandler`, catches these and turns each into an HTTP status such as `404 Not Found`. Chapter 8 teaches status codes and Chapter 13 shows the handler. The design choice is that business code throws a meaningful exception and one central place decides what the user sees. <!-- source: GlobalExceptionHandler.java at book-m6-final -->

A related security rule shows in `ForbiddenException`'s comment: resources a user may not see at all are reported as 404, not 403, "so their existence isn't revealed". <!-- source: ForbiddenException.java at book-m6-final -->

### 5.6 try-with-resources and files

Files, network connections and database connections must be **closed** after use, or the program leaks them. **Try-with-resources** closes them automatically, even if an error happens.

**Listing 5.5 — `DocumentController.java` (book-m6-final, excerpt: method `upload`)**

```java
        try (var in = file.getInputStream()) {
            return documents.upload(title, file.getOriginalFilename(), in, visibility,
                    Viewer.of(authentication), actors.of(request, authentication));
        }
```

*Path: `src/main/java/com/example/securedocviewer/controller/DocumentController.java`*

The resource in parentheses, the uploaded file's stream of bytes, is closed when the block ends. For files on disk Java offers `Path` (a file address) and `Files` (operations on it). `FileOperations.java` uses `Files.move` to rename a folder of tiles in one step. Chapter 17 covers files in the app.

## Advanced tier: Time

### 5.7 Time: Instant, UTC and why the app uses it

An **Instant** is a single point on the timeline, independent of time zones. **UTC** (Coordinated Universal Time) is the world's reference time, with no daylight-saving changes. The app stores every timestamp as an instant, and the database columns hold UTC. Why?

A server in a Docker container (Chapter 10) runs in UTC, while a developer's laptop may run in local time. If they disagreed about what "9:00" means, the same stored value would be read as different moments. Two settings in `application.yml` fix that: `connectionTimeZone=UTC` in the database connection address, and `hibernate.jdbc.time_zone: UTC` for Hibernate, the library that moves Java objects in and out of the database (Chapter 14). Watermarks also show a UTC timestamp for the same reason. <!-- source: application.yml comment at book-m6-final; commit 2d82253 "Fix review findings: ... UTC" -->

You saw `Instant.now()` in `AppUser`'s constructor (Listing 4.1). Expiry checks compare instants, and expiry values are stored as whole seconds since 1970 (`expiresAtEpochSeconds` in Listing 4.3), which is a plain number and therefore unambiguous.

## In this project

- `document/Document.java`, `security/LoginThrottle.java`: sets, maps and deques.
- `document/DocumentService.java`: streams and lambdas.
- `exception/`: eleven custom exception types.
- `controller/GlobalExceptionHandler.java`: one place that turns exceptions into responses.
- `service/FileOperations.java`: file moves with retries.

## Try it

### Exercise 5.1 ★ Pick the collection

For each, choose list, set or map: the pages of a document in order; the usernames allowed to open a document; each username with its role.

*Solution:* Appendix C, Exercise 5.1.

### Exercise 5.2 ★ Stream it

Given `List.of(1275, 1650, 512, 300)` of pixel lengths, write a stream that keeps the values above 512 and prints how many tiles each needs, using the ceiling formula from Chapter 3.

*Solution:* Appendix C, Exercise 5.2.

### Exercise 5.3 ★★ Throw and catch

Write a method `tileCount` that throws `IllegalArgumentException` on a non-positive input, and a `main` that catches it and prints the message. Then define your own exception `PageOutOfRange` and throw it instead.

*Solution:* Appendix C, Exercise 5.3.

## Summary

- Lists keep order, sets keep uniqueness, maps connect keys to values; generics say what they hold.
- Lambdas and streams process collections in short pipelines.
- `Optional` makes "maybe nothing" explicit; `orElseThrow` turns nothing into an error.
- Custom exceptions give failures meaning, and one central handler decides what users see.
- Try-with-resources closes what you open; the app stores times as UTC instants.

## Further reading

- *The Java Tutorials*, "Collections." https://docs.oracle.com/javase/tutorial/collections/index.html
- *The Java Tutorials*, "Exceptions." https://docs.oracle.com/javase/tutorial/essential/exceptions/index.html
- *Java Platform SE 25 API*, `java.time.Instant`. https://docs.oracle.com/en/java/javase/25/docs/api/
