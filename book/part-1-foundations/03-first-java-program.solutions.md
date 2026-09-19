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
