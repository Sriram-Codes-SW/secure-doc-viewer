# Chapter 4 solutions

### Exercise 4.1 ★ Class or record?

A tile's row and column: a record, because it is plain data that never changes. An account whose password can change: a class, because it has changing state and hides its fields. A search result: a record, because it is a fixed bundle of values.

### Exercise 4.2 ★ Write a record

```java
public class TileRefDemo {
    record TileRef(int row, int col) { }

    public static void main(String[] args) {
        System.out.println(new TileRef(2, 1));
    }
}
```

`toString` prints `TileRef[row=2, col=1]`.

### Exercise 4.3 ★★ Add an enum

One good answer: `enum TileStatus { QUEUED, READY, FAILED }`, then `TileStatus s = TileStatus.READY; if (s == TileStatus.READY) { ... }`. Enum values are compared with `==` because each value exists once.

### Exercise 4.4 ★★ A class that keeps a rule

```java
public class Counter {
    private int count;
    private final int max;

    public Counter(int max) {
        this.max = max;
    }

    public void increment() {
        if (count < max) {
            count++;
        }
    }

    public int getCount() {
        return count;
    }

    public static void main(String[] args) {
        Counter c = new Counter(2);
        c.increment();
        c.increment();
        c.increment();
        System.out.println(c.getCount());   // 2
    }
}
```

Because `count` is `private` and only `increment` changes it, no caller can push it past `max`. If `count` were a public field, `c.count = 99` would break the rule, just as a public `updatedAt` would break `Document`'s.

### Exercise 4.5 ★★ Swap a part

```java
public class Swap {
    interface TileStore { byte[] load(String name); }
    static class DiskTileStore implements TileStore {
        public byte[] load(String name) { return new byte[0]; }
    }
    static class FakeTileStore implements TileStore {
        public byte[] load(String name) { return new byte[] {1, 2, 3}; }
    }

    static void describe(TileStore store) {
        System.out.println(store.load("tile-0_0.png").length);
    }

    public static void main(String[] args) {
        describe(new DiskTileStore());   // 0
        describe(new FakeTileStore());   // 3
    }
}
```

Nothing in `describe` has to change for a third store: it depends only on the `TileStore` interface, so any new class that `implements TileStore` works.

### Exercise 4.6 ★★★ Model a request

One good answer follows the project's own design: `enum EventType { SIGN_IN, PAGE_VIEWED, ACCESS_DENIED }`; `record Actor(String username, String clientIp)` with `static Actor anonymous(String ip)`; `record Subject(String documentId, Integer page)` with `static Subject none()`; and `record Event(EventType type, Actor actor, Subject subject)`. Records fit because each is plain data that must not change after being recorded, which is exactly what an audit trail promises; the enum fits because the kinds of event are a fixed list the compiler can check. Fields that may be `null`: the actor's `username` (an anonymous request has none) and every field of `Subject`, since a sign-in is about no document. Use `Integer` rather than `int` for `page`, so that "no page" can be `null`, as `AuditEvent` does.
