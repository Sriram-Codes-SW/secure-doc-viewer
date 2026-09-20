<!-- chapter: 38 | part: VI | owner: writer-backend | tag: book-m6-final | status: expanded -->
# Solutions: Chapter 38

### Exercise 38.1 ★ Name the pattern

- (a) A **factory method** (and the class it builds is a **value object**): `Viewer.of(authentication)` in `document/Viewer.java`.
- (b) A **template method with a callback**: `TransactionTemplate.execute(...)` runs the fixed steps (begin, commit, or roll back) and calls your lambda for the variable step; used in `document/DocumentService.java`.
- (c) A **composite** of **strategies**: it groups two `SessionAuthenticationStrategy` objects and is itself used as one; in `security/SecurityConfig.java`.
- (d) An **observer** (publish-subscribe): the method is called when the framework publishes a session event; in `security/SessionMetadata.java`.

### Exercise 38.2 ★ Where does the chain stop?

The line `chain.doFilter(request, response);` at the end of `doFilterInternal` continues the chain, and it is reached only when the `if` condition is false. When the session carries the pending-change flag and the path is under `/api/` but not `/api/auth/`, the `if` body runs: it sets status `403`, writes the JSON error with `passwordChangeRequired: true`, and executes `return;`. The chain is never called, so the request stops in this filter and no controller sees it.

### Exercise 38.3 ★★ Find the strategies

The beans in `SecurityConfig` whose types are Spring Security interfaces, with a possible second implementation for each:

- `PasswordEncoder`: the delegating encoder could add a stronger algorithm's label (for example, a newer hashing function); no reason now, but the design allows it without a migration.
- `AuthenticationManager` (a `ProviderManager` around a `DaoAuthenticationProvider`): a second provider could authenticate against an identity provider, which Section 37.6 discusses; not needed today.
- `CsrfTokenRepository`: a session-based repository would keep the token on the server instead of in a cookie, but the single-page app's double-submit design needs the cookie.
- `SecurityContextRepository`: the current one stores the security context in the HTTP session; a token-based one would make authentication stateless, the design Section 37.12 weighs and rejects.
- `SessionRegistry`: a shared registry (for example, one backed by a shared store) would be needed to run several instances (Section 37.5).
- `SessionAuthenticationStrategy`: the composite already combines two; another could be added, for example to limit sessions per user.

`HttpSessionEventPublisher` is also a bean but is not a strategy: it is a publisher of events (an observer's other half).

### Exercise 38.4 ★★ Trace the state machine

In `TileGenerationService.render`: the state is created as `RUNNING`; the render job tries `compareAndSet(RUNNING, DONE)` when it finishes, and if that fails (the request had already abandoned it) it discards its staging folder and throws; on a `TimeoutException` the request tries `compareAndSet(RUNNING, ABANDONED)`, and if that fails it uses the finished result; on an `InterruptedException` it does the same `compareAndSet(RUNNING, ABANDONED)`; and the wrapper that frees the render slot reads `state.get() == RenderState.ABANDONED` to decrement the count of abandoned renders still stopping (a read, not a move). Figure 38.1 shows all three moves: start in `RUNNING`, then `DONE` or `ABANDONED`, both final. No move is missing: nothing ever leaves `DONE` or `ABANDONED`.

### Exercise 38.5 ★★★ Apply the fifth question

A worked outline; any three good answers will do. Examples. *Builder:* a builder for `TileAccess` (three fields), where the record's constructor is already clear and checked by the compiler. *Strategy:* a `TitleValidator` interface with one implementation for `validTitle`, which is a few lines in `DocumentService` and has no second candidate. *Observer:* publishing an event whenever a document is renamed so that a class can update the `updatedAt` time, when the entity's setter already calls `touch()` in one line. *Bulkhead:* a separate limiter for the audit search endpoint, which is used only by administrators and is bounded by its `size` limit of 500. In each case the simpler design is the direct one: a constructor, a private method, a call inside the setter, an existing validation. The test for over-engineering is to say what goes wrong without the pattern; if the answer is "nothing," leave it out.

### Exercise 38.6 ★★★ A decision in pattern words

One good answer, using Section 37.5 (in-memory sessions and counters versus a shared store). *Pattern in use:* a **sliding window log** rate limiter and a **reserve and compensate** rule in `TileRateLimiter` and `LoginThrottle`, plus a `SessionRegistry` that keeps sessions in memory; all are ordinary objects in one process. *Cost as the chapter states it:* per-instance state; the limiter's memory grows with the limit and needs a sweep; and a restart clears counters and sessions. *What would change:* the counters and the session registry would move behind an interface that a shared store implements (a **strategy**), the reserve step would have to be atomic in the store (for example, an atomic increment or a script), and the sweeps would be replaced by expiry in the store. *Team of five:* keep the in-memory version until the need for a second instance is real, because the shared store is another service to run, secure, and back up, which a small team pays for every day; but write the counters behind a small interface now if a move is likely, since that costs a few lines and makes the later change local. Either answer earns full credit if it names the problem, the pattern, the cost, and the trigger for changing.
