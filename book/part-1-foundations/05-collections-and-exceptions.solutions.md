# Chapter 5 solutions

### Exercise 5.1 ★ Pick the collection

Pages in order: a list. Usernames allowed to open a document: a set (no duplicates). Each username with its role: a map from username to role.

### Exercise 5.2 ★ Stream it

```java
List.of(1275, 1650, 512, 300).stream()
        .filter(n -> n > 512)
        .map(n -> (n + 512 - 1) / 512)
        .forEach(System.out::println);
```

It prints 3 and 4.

### Exercise 5.3 ★★ Throw and catch

Throw with `throw new IllegalArgumentException("...")` inside `if (n <= 0)`, and wrap the call in `try { ... } catch (IllegalArgumentException e) { System.out.println(e.getMessage()); }`. For the custom type, write `class PageOutOfRange extends RuntimeException { PageOutOfRange(String m) { super(m); } }` and catch `PageOutOfRange` instead.
