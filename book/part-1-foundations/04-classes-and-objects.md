<!-- chapter: 4 | part: I | owner: writer-foundations | tag: book-m6-final | status: draft -->
# Chapter 4: Classes, objects, records and interfaces

Chapter 3 gave you values, decisions, loops and methods. Real programs group these into larger pieces that model things: an account, a document, a tile. This chapter teaches classes and objects, records, enums, interfaces, packages and annotations, using the app's own account and document types as the examples.

## Learning objectives

By the end of this chapter, you will be able to:

- Explain the difference between a class and an object.
- Write a class with private fields, a constructor and getters.
- Write a record and say when a record is a better choice than a class.
- Write an enum for a fixed set of choices.
- Explain what an interface is and why the app depends on interfaces.
- Read a `package` line, an `import` line and a source-folder layout.
- Recognize an annotation and say what it is for.

## Prerequisites

- Chapter 3: Your first Java program

## Beginner tier: Modeling things

### 4.1 Classes and objects; fields, constructors

Suppose the app must keep track of accounts. Each account has a username, a role and a creation time. You could use three separate variables, but you would need a new set for every account. A **class** is a blueprint that groups such values together with the code that works on them. An **object** is one thing built from that blueprint. A class is "account"; the account for `pub.one` is an object of that class.

**Analogy.** A class is a form with blank fields, and each object is a filled-in copy. The analogy breaks down because in Java the form can also carry behavior: a class holds methods, not just blanks.

The values an object holds are its **fields**. A **constructor** is special code that runs when you create an object with `new`, and sets up its fields. Example 4.1 is a tiny teaching class, not the project's.

**Example 4.1 — A minimal class**

```java
public class Account {
    String username;
    int failedAttempts;

    Account(String username) {
        this.username = username;
        this.failedAttempts = 0;
    }
}
```

The constructor has the class's name and no return type. `this` means "this object", so `this.username = username` copies the parameter into the field. Creating and using an object:

```java
Account a = new Account("pub.one");
System.out.println(a.username);   // pub.one
```

The dot reaches into an object: `a.username` is the field, and `a.someMethod()` would call a method.

### 4.2 Encapsulation: private, getters, immutability

If any code can change `a.failedAttempts`, then any bug anywhere can corrupt it. **Encapsulation** means hiding a class's fields and exposing only the operations you intend. You mark fields `private` (only this class can touch them) and offer **getter** methods to read them and, where change is allowed, **setter** methods to write them.

The app's real account class follows this pattern. Here it is, shortened to the parts we need.

**Listing 4.1 — `AppUser.java` (book-m6-final, simplified: imports, the fields `enabled`, `createdAt`, `mustChangePassword` and `lastSignInAt`, and the accessors of `role`, `enabled` and those fields omitted; the `// ...` lines mark the cuts)**

```java
@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    // ...

    protected AppUser() {
    }

    public AppUser(String username, String passwordHash, Role role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    // ...
}
```

*Path: `src/main/java/com/example/securedocviewer/account/AppUser.java`*

Notice four things:

- Every field is `private`. Other code must go through methods.
- There is a getter for `username` but no setter. Once created, an account's username can't be changed through this class; that is a deliberate rule, written into the code's shape.
- There is a setter for `passwordHash`, because passwords change.
- The public constructor takes the three values a new account needs and sets the creation time itself, so no caller can forget or fake it. The `protected` constructor with no parameters exists for the database library, which Chapter 14 explains.

A field with no setter and set only in the constructor is **immutable**: it never changes after creation. Immutable data is easier to reason about, because nothing can alter it behind your back.

The `Role` type of the `role` field is an enum, which we meet in Section 4.4. The `@` lines are annotations, covered in Section 4.7.

### 4.3 Records for plain data

Many types are just bundles of values that never change: a page's dimensions, a row of a table. Writing a private field, constructor and getter for each is repetitive. A **record** gives you all of that in one line. When you declare `record Point(int x, int y) { }`, Java generates the constructor, the read methods `x()` and `y()`, and sensible `equals`, `hashCode` and `toString`. Record fields are always immutable.

The app uses records everywhere data moves around. Here is one describing a rendered page.

**Listing 4.2 — `PageInfo.java` (book-m6-final)**

```java
package com.example.securedocviewer.model;

/**
 * Describes the tile grid for a single rendered page, plus the pixel
 * dimensions the frontend needs to lay tiles out on a &lt;canvas&gt;.
 */
public record PageInfo(
        int page,
        int rows,
        int cols,
        int tileSize,
        int pageWidthPx,
        int pageHeightPx
) {
}
```

*Path: `src/main/java/com/example/securedocviewer/model/PageInfo.java`*

A note on the comment: it mentions a `<canvas>`, a drawing surface in the browser. That comment dates from the first milestone, whose static page drew tiles on a canvas. The Angular viewer in the finished app instead positions plain `div` elements and paints each tile as a CSS background image (Chapter 21), so the comment is stale in the repository; the record itself is unaffected. <!-- source: editor's note on OUTLINE 21.6; requests.md -->

You create one with `new PageInfo(1, 4, 3, 512, 1275, 1650)` and read it with `info.rows()`. Note that the read method is `rows()`, not `getRows()`. Listing 3.2 in Chapter 3 used exactly this: `pageInfo.rows()` and `pageInfo.cols()`.

A record can also contain methods. The signed-URL payload adds one that joins its fields into the text that gets signed.

**Listing 4.3 — `SignedTilePayload.java` (book-m6-final, simplified: comments omitted)**

```java
public record SignedTilePayload(
        String documentId,
        int page,
        int row,
        int col,
        int tileVersion,
        String sessionBinding,
        long expiresAtEpochSeconds
) {
    public String canonicalString() {
        return documentId + "|" + page + "|" + row + "|" + col + "|" + tileVersion + "|" + sessionBinding + "|" + expiresAtEpochSeconds;
    }
}
```

*Path: `src/main/java/com/example/securedocviewer/model/SignedTilePayload.java`*

Why a record here? A signed payload must not change between the moment it is signed and the moment it is checked, and a record can't be altered after creation. Chapter 17 explains what the signature covers.

**When to choose which.** Use a record for plain data that doesn't change. Use a class when the object has changing state or must hide its fields, as `AppUser` does because the database library fills it in and updates it.

### 4.4 Enums for fixed choices

Some values come from a short, fixed list. An account's role is `READER`, `PUBLISHER` or `ADMIN`, and nothing else. If you stored roles as free text, a typo such as `"ADMlN"` would compile and fail silently later. An **enum** declares the complete list, and the compiler rejects anything else.

**Listing 4.4 — `Role.java` (book-m6-final)**

```java
package com.example.securedocviewer.account;

/**
 * What an account may do. Roles are cumulative in practice: every signed-in
 * user can read documents they have access to, publishers can also upload,
 * and admins can additionally manage accounts, sessions and the audit log.
 */
public enum Role {
    READER,
    PUBLISHER,
    ADMIN
}
```

*Path: `src/main/java/com/example/securedocviewer/account/Role.java`*

You write `Role.ADMIN` in code, and compare with `==`: `user.getRole() == Role.ADMIN`. The project has a second enum, `Visibility`, with the values `PRIVATE` and `EVERYONE`. It is the answer to "besides the owner and admins, who may open this document?". <!-- source: Visibility.java at book-m6-final -->

## Intermediate tier: Depending on promises

### 4.5 Interfaces and why we depend on them

An **interface** lists methods that a type promises to offer, without saying how. Code that needs those methods depends on the interface and doesn't care which class supplies them. Example 4.2 is a teaching sketch.

**Example 4.2 — An interface and two implementations**

```java
interface TileStore {
    byte[] load(String tileName);
}

class DiskTileStore implements TileStore {
    public byte[] load(String tileName) { /* read from disk */ return new byte[0]; }
}

class FakeTileStore implements TileStore {
    public byte[] load(String tileName) { return new byte[] {1, 2, 3}; }
}
```

Code that takes a `TileStore` works with either. A test can hand it the fake, so the test needs no real disk. That is the main reason interfaces matter in practice: they let you swap parts, and they make code testable.

The project uses interfaces heavily, often without writing the implementation itself. Look at how the app declares its access to documents.

**Listing 4.5 — `DocumentRepository.java` (book-m6-final, simplified: imports, Javadoc comments and six of the eight query methods omitted; four are cut at the `// ...` marker, one (`findVisibleTo`) before the first shown method and one (`findAllIds`) after the last)**

```java
public interface DocumentRepository extends JpaRepository<Document, String> {

    @Query("select d from Document d join fetch d.owner order by d.createdAt desc")
    List<Document> findAllWithOwner();

    // ...

    long countByOwner_Username(String username);
}
```

*Path: `src/main/java/com/example/securedocviewer/document/DocumentRepository.java`*

There is no class that implements this interface anywhere in the project. Spring, the framework the backend uses (a **framework** is a large library that calls your code, rather than the other way round; Chapter 11 explains it), builds one at startup from the method declarations. You state what you need and the framework supplies the how. Chapter 14 explains this in detail. The `<Document, String>` part is a **generic**: it says this repository handles `Document` objects whose identifier is a `String`. Chapter 5 covers generics.

**We simplify here.** Interfaces have more features, such as default methods. The app doesn't need them, so this book doesn't teach them.

### 4.6 Packages and imports

A large program has hundreds of classes. **Packages** group them, like folders. The first line of each file declares its package, and the folder layout matches it. `PageInfo` is in `com.example.securedocviewer.model`, so its file sits in `src/main/java/com/example/securedocviewer/model/`.

Table 4.1 shows the app's packages under `com.example.securedocviewer`.

| Package | Holds |
|---|---|
| `account` | Users, roles, the account service |
| `audit` | The audit trail |
| `config` | Settings read from `application.yml` |
| `controller` | The web endpoints |
| `document` | Documents, sharing, visibility |
| `exception` | The app's own error types |
| `model` | Plain data records such as `PageInfo` |
| `security` | Sign-in, sessions, throttling |
| `service` | Tile rendering, signing, watermarking |

*Table 4.1 — The app's packages (book-m6-final)*

<!-- source: git ls-tree of src/main/java at book-m6-final -->

To use a class from another package, you **import** it: `import java.util.List;` at the top of a file lets you write `List` instead of `java.util.List`. Classes in `java.lang`, such as `String`, need no import. Packages also let two classes share a name without clashing.

The reverse-domain name `com.example` is a convention that keeps package names unique across the world. The project uses the placeholder `example.com`.

## Advanced tier: Annotations

### 4.7 Annotations: a first look

Look again at Listing 4.1. Lines that start with `@` are **annotations**: labels attached to code that tools read. `@Entity` tells the database library "this class maps to a table". `@Column(nullable = false, length = 64)` says the field maps to a column that can't be empty and holds at most 64 characters. `@SpringBootApplication` in Listing 3.1 tells Spring where to start.

An annotation doesn't do anything by itself. A framework finds it, reads it, and acts. That is why the app's code can be so short: much behavior is declared with labels and carried out by libraries.

The cost is that behavior becomes less visible. If something happens that you can't find in the code, look for an annotation. Parts II and III return to this often.

## In this project

- `account/AppUser.java`: a class with private fields.
- `model/PageInfo.java`, `model/SignedTilePayload.java`, `document/DocumentSummary.java`, `document/Viewer.java`: records.
- `account/Role.java`, `document/Visibility.java`, `audit/AuditEventType.java`: enums.
- `document/DocumentRepository.java`, `account/AppUserRepository.java`: interfaces implemented by Spring.

## Try it

### Exercise 4.1 ★ Class or record?

For each of these say whether you would write a class or a record, and why: a tile's `row` and `col`; an account whose password can change; the result of a search (a title and a page count).

*Solution:* Appendix C, Exercise 4.1.

### Exercise 4.2 ★ Write a record

Write a record `TileRef(int row, int col)`, create one for row 2, column 1, and print it. What does `toString` show?

*Solution:* Appendix C, Exercise 4.2.

### Exercise 4.3 ★★ Add an enum

Open `Visibility.java`, then write your own enum `TileStatus` with three values of your choice for a tile that is queued, ready or failed. Use it in a `main` and compare a value with `==`. Work in a scratch folder, not in the repository.

*Solution:* Appendix C, Exercise 4.3.

## Summary

- A class is a blueprint and an object is one instance; fields hold state and constructors set it up.
- Private fields and getters hide the details; immutable data is easier to trust.
- Records are one-line immutable data classes; enums are a closed list of values.
- Interfaces are promises; depending on them lets you swap and test parts.
- Packages organize code into folders; annotations are labels that frameworks act on.

## Further reading

- *The Java Tutorials*, "Classes and Objects." https://docs.oracle.com/javase/tutorial/java/javaOO/index.html
- *The Java Language Specification*, Java SE 25 edition, "Records" and "Enum Classes." https://docs.oracle.com/javase/specs/
