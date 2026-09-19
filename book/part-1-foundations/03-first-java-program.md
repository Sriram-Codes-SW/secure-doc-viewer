<!-- chapter: 3 | part: I | owner: writer-foundations | tag: book-m6-final | status: draft -->
# Chapter 3: Your first Java program

The backend of the Secure Document Viewer is written in Java. This chapter teaches the smallest useful core of the language: how to write, compile and run a program, how to store values, how to make decisions and repeat work, and how to package logic into methods. It ends by reading a real method from the app that decides how many tiles a page needs.

## Learning objectives

By the end of this chapter, you will be able to:

- Explain what a program, source code, the JDK and the JVM are.
- Write, compile and run a small Java program from the terminal.
- Declare variables of the basic types and combine them with operators.
- Write `if` statements and loops.
- Write and call a method with parameters and a return value.
- Read a compiler error and a stack trace to find the line at fault.
- Read `TileGrid.tileCount` and explain each line.

## Prerequisites

- Chapter 2: The command line and your files

## Beginner tier: Writing and running a program

### 3.1 What Java, the JDK and the JVM are

A **program** is a list of instructions a computer follows. People write those instructions as **source code**, text in a **programming language**. A computer's processor can't read source code directly, so the code must be translated first.

Java works in two steps. A tool called the **compiler** (`javac`) translates your source files (which end in `.java`) into **bytecode**, a compact set of instructions stored in `.class` files. Then the **JVM** (Java Virtual Machine) runs the bytecode. The JVM is a program that behaves like a computer inside your computer, which is why the same bytecode runs on Windows, macOS and Linux.

You install the **JDK** (Java Development Kit), which contains the compiler, the JVM and the standard library, a large collection of ready-made code. This book uses Java 25. The project sets `<java.version>25</java.version>` in its `pom.xml`, which Chapter 6 explains. <!-- source: pom.xml at book-m6-final -->

**Analogy.** Source code is a recipe in English, bytecode is the same recipe translated into a simple universal shorthand, and the JVM is a cook in each kitchen who knows the shorthand. The analogy breaks down because a cook works from the shorthand at human speed, while the JVM also optimizes the bytecode as it runs, so long-running programs get faster after a warm-up.

### 3.2 Hello, world: compile and run

Check that Java is installed:

```bash
java -version
```

You should see something like:

```text
openjdk version "25" ...
```

Create a file named `Hello.java` with the code in Example 3.1. The file name must match the class name.

**Example 3.1 — `Hello.java`**

```java
public class Hello {
    public static void main(String[] args) {
        System.out.println("Hello, world");
    }
}
```

Every token here is new, so we go through it.

- `public class Hello { ... }` declares a **class**, a named container for code. Chapter 4 explains classes fully. For now, every Java program lives inside one, and the name matches the file.
- `public static void main(String[] args)` declares the **main method**, the entry point: where the JVM starts. The words `public` and `static` control who can call it and whether it needs an object; `void` means it returns nothing; `String[] args` receives any words typed after the program name.
- `System.out.println("Hello, world");` prints a line of text. `"Hello, world"` is a **string**, a piece of text in double quotes. The semicolon ends the statement.
- Braces `{ }` group code into blocks.

Compile and run:

```bash
javac Hello.java
java Hello
```

`javac` creates `Hello.class`; `java Hello` runs it. You should see:

```text
Hello, world
```

Since Java 11 you can also run a single file in one step with `java Hello.java`.

The real application has the same shape. Its entry point is short.

**Listing 3.1 — `SecureDocViewerApplication.java` (book-m6-final)**

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

*Path: `src/main/java/com/example/securedocviewer/SecureDocViewerApplication.java`*

You recognize the class and `main`. The `package` and `import` lines and the `@` lines are explained in Chapter 4. The single line inside `main` starts the whole web server; Chapter 11 explains how.

### 3.3 Variables, types and operators

A **variable** is a named place to keep a value. Java is **statically typed**: every variable has a **type** that says what kind of value it holds, and the compiler checks that you use it correctly. Table 3.1 lists the types you'll meet most.

| Type | Holds | Example |
|---|---|---|
| `int` | Whole numbers (about -2 billion to 2 billion) | `512` |
| `long` | Larger whole numbers | `120L` |
| `double` | Decimal numbers | `1.5` |
| `boolean` | `true` or `false` | `true` |
| `String` | Text | `"admin"` |

*Table 3.1 — Basic types*

Example 3.2 declares some variables using names from the app's configuration.

**Example 3.2 — Variables and operators**

```java
int tileSize = 512;
int pageWidthPx = 1275;
String owner = "pub.one";
boolean isPrivate = true;

int leftover = pageWidthPx - 2 * tileSize;   // 1275 - 1024 = 251
boolean fits = pageWidthPx <= tileSize;      // false
String label = owner + " owns this";         // text joining
```

Each line has the shape `type name = value;`. The `=` **assigns**: it stores the value on the right in the variable on the left. **Operators** combine values: `+ - * /` for arithmetic, `<=`, `<`, `==` (equal), `!=` (not equal) for comparison, and `&&` (and), `||` (or), `!` (not) for booleans. With `+`, text and other values are joined.

Two details trip up beginners. Dividing two `int` values gives an `int` with the remainder dropped: `7 / 2` is `3`, not `3.5`. And `==` compares numbers; to compare text, use `.equals`: `owner.equals("pub.one")`.

### 3.4 Decisions and loops

An **if statement** runs code only when a condition is true.

**Example 3.3 — Deciding**

```java
if (pageCount > 500) {
    System.out.println("Too many pages");
} else {
    System.out.println("OK");
}
```

A **loop** repeats code. A `for` loop counts:

**Example 3.4 — Counting**

```java
for (int row = 0; row < 4; row++) {
    System.out.println("Row " + row);
}
```

Read the parentheses as three parts: start (`int row = 0`), keep going while (`row < 4`), and step (`row++`, which adds one). This prints rows 0 to 3. Counting starts at zero throughout Java, and the app follows that: the first tile of a page is row 0, column 0.

The app uses exactly this pattern to hand out one URL per tile, with one loop inside another.

**Listing 3.2 — `PageTileUrlController.java` (book-m6-final, excerpt: method `tileUrls`)**

```java
        String[][] urls = new String[pageInfo.rows()][pageInfo.cols()];
        for (int row = 0; row < pageInfo.rows(); row++) {
            for (int col = 0; col < pageInfo.cols(); col++) {
                String token = signedUrlService.issueToken(documentId, page, row, col, issuable.tileVersion(), sessionBinding);
                urls[row][col] = "/api/tiles?token=" + token;
            }
        }
```

*Path: `src/main/java/com/example/securedocviewer/controller/PageTileUrlController.java`*

The outer loop walks the rows, the inner loop walks the columns of each row, and each pass creates a **token** (the signed part of a URL) and stores a URL in `urls`, a table of strings (`String[][]`). Names like `pageInfo.rows()` and `signedUrlService.issueToken(...)` are calls to code defined elsewhere; you'll learn to read them in Chapter 4.

### 3.5 Methods

A **method** is named, reusable code that takes inputs (**parameters**) and can give back a result (a **return value**). Methods stop you from writing the same logic twice and let you test it in isolation.

**Example 3.5 — A method**

```java
static int tilesNeeded(int lengthPx, int tileSize) {
    return (lengthPx + tileSize - 1) / tileSize;
}
```

`static int tilesNeeded(...)` says: this method returns an `int`. Inside the parentheses are two parameters, each with a type. `return` sends a value back. You call it like this: `int cols = tilesNeeded(1275, 512);`, and `cols` becomes 3.

Why that formula? Whole-number division drops the remainder, so `1275 / 512` is 2, one tile short. Adding `tileSize - 1` first makes any remainder push the result up to the next whole number. This is **ceiling division**, "divide and round up". The app needs it because a page rarely divides evenly into tiles.

## Intermediate tier: When things go wrong

### 3.6 Reading errors

Errors are normal. You'll read many. Learn to read them calmly, from the first line.

A **compiler error** stops the build before anything runs. Suppose you forget a semicolon:

```text
Hello.java:3: error: ';' expected
        System.out.println("Hello, world")
                                          ^
1 error
```

The message gives the file, the line number (3), what is wrong, and a caret under the place. Fix the first error first; later ones often follow from it.

A **runtime error** happens while the program runs. Java reports it as an **exception** with a **stack trace**, the chain of method calls that led to the failure, newest first:

```text
Exception in thread "main" java.lang.IllegalArgumentException: lengthPx and tileSize must both be positive
        at TileMath.tileCount(TileMath.java:6)
        at TileMath.main(TileMath.java:12)
```

Read it top to bottom: the first line says what went wrong, and the first `at` line says where. Here that is line 6 of `TileMath.java`, called from line 12. (The trace above is teaching output, not from the project.) Chapter 5 teaches how to raise and handle exceptions.

## Advanced tier: A real method from the app

### 3.7 A small taste of the app: tile-grid math

You now know enough to read a real class. `TileGrid` holds the math that turns a page's pixel size into a number of tiles. Listing 3.3 is its counting method.

**Listing 3.3 — `TileGrid.java` (book-m6-final, excerpt: method `tileCount`)**

```java
    /** Number of tiles needed to cover {@code lengthPx} pixels: ceil(lengthPx / tileSize). */
    public static int tileCount(int lengthPx, int tileSize) {
        if (lengthPx <= 0 || tileSize <= 0) {
            throw new IllegalArgumentException("lengthPx and tileSize must both be positive");
        }
        return (lengthPx + tileSize - 1) / tileSize;
    }
```

*Path: `src/main/java/com/example/securedocviewer/service/TileGrid.java`*

Line by line:

- The `/** ... */` block is a **doc comment**: text for people, ignored by the compiler.
- `public static int tileCount(int lengthPx, int tileSize)` is a method that returns a whole number. `public` lets any code call it.
- The `if` uses `||` ("or"): if either input is zero or negative, the method refuses to continue.
- `throw new IllegalArgumentException(...)` raises an exception with a message. Refusing bad input early stops a nonsense answer from spreading.
- The final line is the ceiling division from Section 3.5.

The project tests this method with a synthetic image so it never needs a real PDF. That is why the class is kept separate from PDF rendering, as its own comment says. <!-- source: TileGrid.java class comment at book-m6-final -->

## In this project

- `src/main/java/com/example/securedocviewer/SecureDocViewerApplication.java`: the entry point.
- `src/main/java/com/example/securedocviewer/service/TileGrid.java`: the tile math, with a test in `src/test/java/com/example/securedocviewer/service/TileGridTest.java`.
- `src/main/java/com/example/securedocviewer/controller/PageTileUrlController.java`: the nested loop.

## Try it

### Exercise 3.1 ★ Hello, tiles

Write `Tiles.java` with a `main` that prints how many columns and rows a 1275 by 1650 page needs at 512 pixels, using the formula from Section 3.5. Compile and run it.

*Solution:* Appendix C, Exercise 3.1.

### Exercise 3.2 ★ Break it on purpose

Remove the semicolon from your `println` line and compile. Then restore it and change `main` to `mian`, compile, and run. Describe each message.

*Hint:* one fails at compile time, one at launch.

*Solution:* Appendix C, Exercise 3.2.

### Exercise 3.3 ★★ Print the grid

Extend `Tiles.java` with two nested `for` loops that print each tile as `row,col`, one per line. How many lines do you expect for a 1275 by 1650 page?

*Solution:* Appendix C, Exercise 3.3.

## Summary

- Java source is compiled by `javac` to bytecode, which the JVM runs; the JDK contains both.
- Variables have types; `int` division drops the remainder.
- `if` decides, `for` repeats, and methods package logic behind a name.
- Errors are read from the first line: the message, then the location.
- `TileGrid.tileCount` is ceiling division guarded by a check that throws an exception.

## Further reading

- *The Java Tutorials*, "Getting Started." https://docs.oracle.com/javase/tutorial/getStarted/index.html
- *The Java Tutorials*, "Language Basics." https://docs.oracle.com/javase/tutorial/java/nutsandbolts/index.html
