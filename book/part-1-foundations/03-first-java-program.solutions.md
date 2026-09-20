# Chapter 3 solutions

### Exercise 3.1 ★ Hello, tiles

```java
public class Tiles {
    public static void main(String[] args) {
        int tileSize = 512;
        int cols = (1275 + tileSize - 1) / tileSize;
        int rows = (1650 + tileSize - 1) / tileSize;
        System.out.println(cols + " columns, " + rows + " rows");
    }
}
```

Output: `3 columns, 4 rows`.

### Exercise 3.2 ★ Break it on purpose

A missing semicolon gives a compiler error (`';' expected`) naming the line. Renaming `main` to `mian` compiles, but `java` fails at launch with an error that the class has no `main` method, because the JVM looks for `main` by name.

### Exercise 3.3 ★★ Print the grid

Put a `for (int col = 0; col < cols; col++)` loop inside a `for (int row = 0; row < rows; row++)` loop and print `row + "," + col`. Expect 3 x 4 = 12 lines.

### Exercise 3.4 ★ Characters or bytes

For `"café"` the expected numbers are 4 characters and 5 bytes, because `é` takes two bytes in UTF-8. Every extra accented letter adds one byte. If your prediction was wrong, recount which letters are outside plain English.

### Exercise 3.5 ★★ Write a rule

```java
public class Titles {
    static boolean validTitle(String title) {
        return title != null && !title.isBlank() && title.length() <= 200;
    }

    public static void main(String[] args) {
        System.out.println(validTitle(""));                    // false
        System.out.println(validTitle("   "));                 // false
        System.out.println(validTitle("Quarterly report"));    // true
        System.out.println(validTitle("x".repeat(201)));       // false
    }
}
```

`"x".repeat(201)` builds a 201-character string. The `null` check comes first so that `isBlank()` is never called on `null`; `&&` stops at the first false part.

### Exercise 3.6 ★★★ Test your own method

```java
public class Check {
    static int tilesNeeded(int lengthPx, int tileSize) {
        if (lengthPx <= 0 || tileSize <= 0) {
            throw new IllegalArgumentException("lengthPx and tileSize must both be positive");
        }
        return (lengthPx + tileSize - 1) / tileSize;
    }

    static void expect(int expected, int actual) {
        System.out.println(expected == actual ? "PASS" : "FAIL");
    }

    public static void main(String[] args) {
        expect(1, tilesNeeded(1, 256));
        expect(1, tilesNeeded(256, 256));
        expect(2, tilesNeeded(257, 256));
        try {
            tilesNeeded(0, 256);
            System.out.println("FAIL");
        } catch (IllegalArgumentException e) {
            System.out.println("PASS");
        }
    }
}
```

It prints four `PASS` lines. Choosing the boundaries (1, 256, 257) is the point: rounding errors live at the edges. Chapter 18 shows the same checks written with the real testing library.
