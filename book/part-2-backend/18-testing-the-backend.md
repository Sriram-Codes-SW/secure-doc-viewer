<!-- chapter: 18 | part: II | owner: writer-backend | tag: book-m6-final | status: draft -->
# Chapter 18: Testing the backend

The Secure Document Viewer's rules about who may see what are only trustworthy if something checks them every time the code changes. This chapter shows the layers of backend tests the project uses, from a single method to the real filter chain to a real MySQL, and how to keep concurrent tests reliable.

## Learning objectives

By the end of this chapter, you will be able to:

- Explain the test pyramid and why the project has tests at several levels.
- Write and read a JUnit 5 test with `@Test` and assertions.
- Test pure logic without starting Spring.
- Write a `MockMvc` test that runs a request through the real security filters.
- Explain what Testcontainers adds beyond H2.
- Explain how a concurrency test is made deterministic.

## Prerequisites

- Chapters 4, 5 and 6: classes, exceptions, Maven
- Chapters 11 to 16: the backend features under test

## Beginner tier: Tests are questions the code must keep answering

### 18.1 Why we test; the test pyramid

A **test** is code that runs other code and checks the result. Think of a pilot's checklist: nobody trusts memory before takeoff, and a checklist run every time catches the step someone forgot. Tests are the checklist for the code.

The **test pyramid** describes a healthy mix: many small, fast tests of single methods at the base; fewer tests that start part of the application in the middle; and very few slow tests of the whole system at the top. Small tests point straight at the broken line; big ones prove the pieces work together. The project has all three layers on the backend (this chapter) plus browser tests in Part III.

**Where the analogy breaks down.** A checklist confirms steps happened; a test can only show the cases you thought to write. Passing tests mean "no known problems", not "no problems".

### 18.2 JUnit 5: your first test

**JUnit 5** is the Java testing framework. A test is a method marked `@Test`; an assertion such as `assertEquals(expected, actual)` fails the test if the values differ. Maven runs them with `mvn test`.

### 18.3 Testing pure logic (`TileGridTest`)

The simplest tests need no Spring at all. `TileGrid` (Chapter 17) is plain arithmetic, so its test is plain Java.

**Listing 18.1 — `TileGridTest.java` (`book-m6-final`, simplified: the image-building helper and the last tests are omitted)**

*`src/test/java/com/example/securedocviewer/service/TileGridTest.java`*

```java
class TileGridTest {

    @Test
    void tileCountRoundsUpForPartialTiles() {
        assertEquals(1, TileGrid.tileCount(1, 256));
        assertEquals(1, TileGrid.tileCount(256, 256));
        assertEquals(2, TileGrid.tileCount(257, 256));
        assertEquals(13, TileGrid.tileCount(612, 50));
        assertEquals(16, TileGrid.tileCount(792, 50));
    }

    @Test
    void tileCountRejectsNonPositiveInputs() {
        assertThrows(IllegalArgumentException.class, () -> TileGrid.tileCount(0, 256));
        assertThrows(IllegalArgumentException.class, () -> TileGrid.tileCount(100, 0));
    }
```

The method names read as sentences describing behavior. The first test picks the edges of the rule "round up": 1, exactly one tile, one pixel over, and two real page sizes. The second uses `assertThrows` to check that bad input fails the right way. `assertThrows` takes a lambda (Chapter 5) so it can run the code and catch the exception. Tests like these run in milliseconds, so you run them constantly.

## Intermediate tier: Tests with the application running

### 18.4 Spring integration tests and `MockMvc`

Most rules live in how classes work together, so many tests start the real application inside the test. `@SpringBootTest` builds the full context; `@ActiveProfiles("test")` loads `application-test.yml` (H2 in MySQL mode, a test signing secret and a known bootstrap admin); and `@AutoConfigureMockMvc` provides **MockMvc**, a tool that sends requests to the controllers without opening a network port. `ErrorContractTest` shows the setup:

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(ErrorContractTest.FailingController.class)
class ErrorContractTest {
```

(`book-m6-final`, `ErrorContractTest.java`, first lines.) The `@Import` adds a small controller that exists only in the test and throws an exception on purpose, so the test can check that Chapter 13's generic 500 hides the message. Because the whole application is real, the test exercises the exception handler, JSON conversion and security together, which a method-level test could not.

### 18.5 Security tests through the real filter chain

`SecurityIntegrationTest` is the class of "one test per property the security review called out" (its own comment). Its names read as the promises the app makes: `apiRequiresSignIn`, `wrongPasswordAndUnknownUserGetTheSameAnswer`, `stateChangingRequestsNeedACsrfToken`, `readersCannotReachAdminOrUpload`, `revokedSessionIsRejectedOnItsNextRequest`, `tileLinksOnlyWorkForTheSessionTheyWereIssuedTo`. A typical one:

```java
@Test
void apiRequiresSignIn() throws Exception {
    mvc.perform(get("/api/documents"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Sign-in required."));
}
```

(`book-m6-final`, `SecurityIntegrationTest.java`.) `mvc.perform(get(...))` sends the request through every filter; `andExpect` checks the status and, with `jsonPath`, a field of the JSON body. Because the request passes the real filter chain, these tests fail if someone changes a rule in `SecurityConfig` by accident. One caution the project learned: the test helper `csrf()` replaces the CSRF repository, hiding real cookie behavior, so `CsrfCookieFlowTest` drives the cookie by hand in its own fresh context marked `@DirtiesContext` (Chapter 16).

## Advanced tier: Real databases and concurrency

### 18.6 Testcontainers and a real MySQL

H2 is fast but is not MySQL: SQL details, locking and time zone handling can differ. **Testcontainers** starts a real MySQL in Docker for the test and discards it afterward.

**Listing 18.2 — `MySqlIntegrationTest.java` (`book-m6-final`, simplified: imports, the JVM time zone setup and most tests are omitted)**

*`src/test/java/com/example/securedocviewer/MySqlIntegrationTest.java`*

```java
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class MySqlIntegrationTest {

    @Container
    @ServiceConnection
    static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4")
            .withCommand("--default-time-zone=-03:00")
            .withUrlParam("connectionTimeZone", "UTC")
            .withUrlParam("forceConnectionTimeZoneToSession", "true");

    // ...

    @Test
    void everyMigrationAppliesToMySql() {
        List<String> applied = jdbc.queryForList(
                "select version from flyway_schema_history where success = 1 and version is not null order by installed_rank",
                String.class);
        assertEquals(List.of("1", "2", "3"), applied);
```

`@Container` starts the MySQL 8.4 image (the same version as production). `@ServiceConnection` points the application's datasource at it automatically, overriding the H2 URL. `disabledWithoutDocker = true` skips the class when Docker isn't available, and the class comment says "CI always has it". The chosen server time zone, `-03:00`, is intentionally odd, so the timezone test proves timestamps stay UTC (Chapter 14).

### 18.7 Concurrency tests; making flaky tests deterministic

Some bugs only appear when requests overlap. `aBurstOfParallelWrongPasswordsGetsNoMoreThanTheLimit` fires 12 wrong-password sign-ins at once and asserts that exactly `MAX_FAILURES_PER_ACCOUNT` (5) were actually checked, the rest refused with `429`. The key device is a `CountDownLatch`:

```java
java.util.concurrent.CountDownLatch start = new java.util.concurrent.CountDownLatch(1);
// ...
start.countDown();
```

(`book-m6-final`, `SecurityIntegrationTest.java`, simplified: two lines of the test, with the loop that submits the threads left out. Each submitted task calls `start.await()` before it sends its request.) Every thread is created and waits at `start.await()`; one `countDown()` releases them together, so they collide in the way the test intends instead of trickling in one at a time. That is what makes the outcome (exactly 5) something the throttle's atomic counting (Chapter 16) must guarantee rather than something luck decides. The assertion is on exact numbers, and on the allowed status codes only (`401` or `429`).

A test can also be flaky, passing or failing without any code change. The repository's history records a fix for one: "Fix flaky render-slot assertion in `TileGenerationServiceTest`" (commit `ec6c1c5`, PR #9). **The problem:** CI failed once because the test `aRenderThatTakesTooLongIsAbandonedAndFreesItsSlot` asserted that every render slot was free at the same instant the second render returned. **How it was found:** the failed CI run on `main` after an earlier pull request merged. **The cause:** the render thread frees its slot in a `finally` block that runs just after the caller receives its result, so on a fast machine the assertion could land in that gap and read 0 instead of 1. **The fix:** the test now waits up to 5 seconds for the counters; production behavior was unchanged. **The lesson:** a test that checks a state at the exact moment a result returns is racing the code it tests; wait for the condition instead. <!-- source: PR #9 description; commit ec6c1c5 --> Where the timing itself can be controlled, do that instead of sleeping and hoping: latches, and fixed clocks such as the `Clock` that `LoginThrottle` and `KnownDevices` accept in a package-private constructor.

## In this project

**Table 18.1 — Backend tests at `book-m6-final`**

| Level | Example | What it proves |
|---|---|---|
| Pure logic | `TileGridTest`, `SignedUrlServiceTest`, `LoginThrottleTest` | Rules without Spring |
| Application + MockMvc | `ErrorContractTest`, `SecurityIntegrationTest`, `CsrfCookieFlowTest`, `SecurityHeadersTest` | Endpoints, filters, error shape |
| Real database | `MySqlIntegrationTest` | Migrations, row locks, UTC |

All live in `src/test/java/com/example/securedocviewer/`.

## Try it

1. (★) Run `mvn test`. Which test classes ran, and which was skipped if Docker is off?
2. (★) Add a case to `tileCountRoundsUpForPartialTiles` for 512 pixels with 512-pixel tiles. Predict the answer first.
3. (★★) Write a `MockMvc` test that `GET /api/documents` without signing in returns `401`. Compare it with `apiRequiresSignIn`.
4. (★★) Why does `CsrfCookieFlowTest` need its own fresh context?
5. (★★★) In the burst test, replace `start.await()` with nothing. Explain why the result might still pass, and why that makes the test weaker.

## Summary

- Tests form a pyramid: many fast method-level tests, fewer application tests, few whole-system tests.
- JUnit 5 tests are `@Test` methods with assertions.
- `@SpringBootTest` with `MockMvc` runs requests through the real filters, controllers and handlers.
- H2 in MySQL mode gives speed; Testcontainers gives a real MySQL for what H2 can't vouch for.
- Concurrency tests use latches to make threads collide on purpose; flaky tests are fixed by controlling time and ordering.

## Further reading

- *JUnit 5 User Guide*. https://docs.junit.org/current/user-guide/
- *Spring Framework Reference Documentation*, "Testing." https://docs.spring.io/spring-framework/reference/testing.html
- *Spring Boot Reference Documentation*, "Testing." https://docs.spring.io/spring-boot/reference/testing/index.html
- *Testcontainers for Java*. https://java.testcontainers.org/
