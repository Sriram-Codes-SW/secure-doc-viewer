<!-- chapter: 18 | part: II | owner: writer-backend | tag: book-m6-final | status: draft -->
# Solutions: Chapter 18

### Exercise 18.1 ★ Run the backend tests

`mvn test` runs every test class under `src/test/java`. `MySqlIntegrationTest` is skipped when Docker isn't running, because of `@Testcontainers(disabledWithoutDocker = true)`.

### Exercise 18.2 ★ Extend a tile-count test

`assertEquals(1, TileGrid.tileCount(512, 512));` since `(512 + 511) / 512 = 1`.

### Exercise 18.3 ★★ Test the sign-in requirement

`mvc.perform(get("/api/documents")).andExpect(status().isUnauthorized());` in a `@SpringBootTest` class with `@AutoConfigureMockMvc` and `@ActiveProfiles("test")`. `apiRequiresSignIn` also checks the JSON error text.

### Exercise 18.4 ★★ A fresh context for CSRF

The `csrf()` test helper permanently replaces the CSRF filter's repository in the context where it runs, so real `XSRF-TOKEN` cookies would never be written. A fresh context (`@DirtiesContext`) keeps that class unaffected by other tests.

### Exercise 18.5 ★★★ Removing the start latch

Without the latch, threads start as they are created and may finish one by one, so the requests might not overlap and the code could pass without proving anything about parallel attempts. The latch makes them collide.
