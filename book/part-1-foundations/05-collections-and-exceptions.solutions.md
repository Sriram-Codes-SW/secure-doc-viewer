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

### Exercise 5.4 ★★ Look up a user

```java
import java.util.Map;
import java.util.Optional;

public class Lookup {
    static Optional<String> findRole(Map<String, String> roles, String username) {
        return Optional.ofNullable(roles.get(username));
    }

    public static void main(String[] args) {
        Map<String, String> roles = Map.of("pub.one", "PUBLISHER");
        System.out.println(findRole(roles, "pub.one").orElse("none"));
        try {
            findRole(roles, "nobody")
                    .orElseThrow(() -> new IllegalStateException("No such user: nobody"));
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        }
    }
}
```

It prints `PUBLISHER`, then `No such user: nobody`.

### Exercise 5.5 ★★★ Build a small throttle

One good answer:

```java
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class MiniThrottle {
    private final Map<String, Deque<Instant>> calls = new HashMap<>();

    boolean allow(String user, Instant now) {
        Deque<Instant> q = calls.computeIfAbsent(user, k -> new ArrayDeque<>());
        Instant cutoff = now.minus(Duration.ofSeconds(60));
        while (!q.isEmpty() && q.peekFirst().isBefore(cutoff)) {
            q.pollFirst();
        }
        if (q.size() >= 3) {
            return false;
        }
        q.addLast(now);
        return true;
    }
}
```

Test with four calls in a row (the fourth returns `false`), then a call 61 seconds later (returns `true`, because the earlier ones have aged out). With many threads, the whole body of `allow` must run as one step: the `computeIfAbsent` and the prune/size-check/`addLast` sequence would need a `ConcurrentHashMap` and a `synchronized (q)` block around the last three steps, or the check and the update could interleave, which is the same gap as in the nine-simultaneous-passwords incident.
