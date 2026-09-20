<!-- chapter: 18 | part: II | owner: writer-backend | tag: book-m6-final | status: expanded -->
# Solutions: Chapter 18

### Exercise 18.1 ★ Run the tests and read the summary

`./mvnw test` runs every test class under `src/test/java`. Near the end of the output, Maven prints a line of the form `Tests run: N, Failures: 0, Errors: 0, Skipped: S`. If Docker isn't running, `MySqlIntegrationTest` is skipped: the class is annotated `@Testcontainers(disabledWithoutDocker = true)`, so JUnit reports its tests as skipped instead of failed. With Docker running, nothing is skipped and the class starts a `mysql:8.4` container.

### Exercise 18.2 ★ Add a boundary case

```java
@Test
void aPageExactlyOneTileWideNeedsOneTileAndOnePixelMoreNeedsTwo() {
    assertEquals(1, TileGrid.tileCount(512, 512));
    assertEquals(2, TileGrid.tileCount(513, 512));
}
```

Prediction: `(512 + 511) / 512` is `1023 / 512`, which is 1 with integer division; `(513 + 511) / 512` is `1024 / 512`, which is 2. Both values sit on either side of the point where the answer changes, which is why they are the right cases.

### Exercise 18.3 ★★ Write an unauthenticated test

The anonymous test:

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminAuditAccessTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void anonymousCallersAreToldToSignIn() throws Exception {
        mvc.perform(get("/api/admin/audit"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Sign-in required."));
    }
}
```

For the reader case, create a reader with `UserAccountService.create(...)`, sign in as in `SecurityIntegrationTest.login`, pass the session with `.session(reader)`, and expect `status().isForbidden()`. The `401` comes from the authentication entry point (nobody is signed in), and the `403` comes from the access-denied handler (someone is signed in but lacks the `ADMIN` role); both are written by `SecurityErrorResponses` in the same JSON shape, but the framework calls them at different points in the filter chain.

### Exercise 18.4 ★★ Break a rule and watch the test

**Experiment 1 (5 to 6).** Nothing fails. The tests in `LoginThrottleTest` and `SecurityIntegrationTest` loop `LoginThrottle.MAX_FAILURES_PER_ACCOUNT` times rather than writing the literal 5, so they follow the constant to its new value. The same is true of the burst test, which asserts that exactly `MAX_FAILURES_PER_ACCOUNT` guesses were checked. Your prediction may have been "some tests fail"; the surprise is the lesson. Those tests check that the throttle *enforces whatever limit is configured*, not that the limit is 5.

**Experiment 2 (ignore the account-and-address rule).** Tests such as `locksOneAddressOutOfAnAccountAfterFiveFailures` and `repeatedFailuresLockTheAccountWithRetryAfter` fail, because they expect a `LoginLockedException` (or a `429`) after the limit and no longer get one. Read each message: JUnit reports which assertion failed and what was expected.

What the two experiments show: the tests are tied to the *behavior* (locking happens at the limit), not to the number. That is usually what you want. The cost is that nothing in the tests pins the value 5 itself; if the product decision "five tries" matters, add one test that says so.

### Exercise 18.5 ★★★ Is the burst test strong enough?

A worked outline, since results depend on your machine.

1. **Without `start.await()`**, each task begins as soon as its thread is scheduled. On a quick machine the twelve requests may run almost one after another. Against the correct, atomic `reserve`, the test still passes, because the throttle is correct. That is fine, but it shows the test stopped forcing an overlap.
2. **With a non-atomic throttle** (check first, count afterward) **and the latch in place**, the twelve requests all pass the check before any is counted, so far more than 5 passwords are actually verified, and the assertion on the count of `401` responses fails. That is the test doing its job.
3. **With a non-atomic throttle and no latch**, the test may pass on some runs, because the overlap that exposes the bug is not guaranteed. That is a test that cannot be trusted.

A paragraph that earns full credit says: a concurrency test is trustworthy when (a) it forces the overlap rather than hoping for it, (b) it asserts exact outcomes that the protection guarantees, and (c) you have seen it fail against the bug it is meant to catch.

### Exercise 18.6 ★★★ Design a test for a promise

One good answer, for "a document unshared from a user stops serving new tiles to them":

- **Name:** `unsharingADocumentStopsNewTileRequestsFromThatUser`.
- **Level:** an application test with `MockMvc` (Section 18.6), because the promise spans the sharing endpoints, the signed-URL issuing endpoint, the tile controller and the access check; a unit test of one class could not prove it.
- **Arrange:** create a publisher, a reader and a PDF; the publisher uploads it and shares it with the reader; the reader signs in and requests a tile URL and one tile, which succeeds.
- **Act:** the publisher unshares the document (`DELETE /api/documents/{id}/shares/{username}`), then the reader requests the same tile again with the still-valid token.
- **Assert:** the response is `404` with the JSON error shape, not `200`. This is the "re-check on every tile" behavior described in the `TileController` class comment.

The key decisions: use the real filter chain so sessions and roles are real, use a token that has not expired so that only the access check can be the reason for refusal, and give each user a unique name, as in Listing 18.7.
