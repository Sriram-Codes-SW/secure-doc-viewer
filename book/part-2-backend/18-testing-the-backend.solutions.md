<!-- chapter: 18 | part: II | owner: writer-backend | tag: book-m6-final | status: draft -->
# Solutions: Chapter 18

1. (★) `mvn test` runs every test class under `src/test/java`. `MySqlIntegrationTest` is skipped when Docker isn't running, because of `@Testcontainers(disabledWithoutDocker = true)`.
2. (★) `assertEquals(1, TileGrid.tileCount(512, 512));` since `(512 + 511) / 512 = 1`.
3. (★★) `mvc.perform(get("/api/documents")).andExpect(status().isUnauthorized());` in a `@SpringBootTest` class with `@AutoConfigureMockMvc` and `@ActiveProfiles("test")`. `apiRequiresSignIn` also checks the JSON error text.
4. (★★) The `csrf()` test helper permanently replaces the CSRF filter's repository in the context where it runs, so real `XSRF-TOKEN` cookies would never be written. A fresh context (`@DirtiesContext`) keeps that class unaffected by other tests.
5. (★★★) Without the latch, threads start as they are created and may finish one by one, so the requests might not overlap and the code could pass without proving anything about parallel attempts. The latch makes them collide.
