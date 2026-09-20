# Solutions: Chapter 39

### Exercise 39.1 ★ Name the pattern

(a) The reverse proxy or gateway at the trust boundary: nginx decides what the app is told about the caller. (b) The single-page-app fallback of client-server with a REST API: any path that isn't a real file serves `index.html`, so the client-side router handles it. (c) Secure by default, part of defense in depth: whatever no rule allows is refused. (d) Immutable versions with an atomic switch (copy-on-write): build the new version beside the old one, then move one pointer.

### Exercise 39.2 ★★ Check the layers

A model answer using two controllers at `book-m6-final`. `DocumentController` depends on `DocumentService` and `RequestActors` only, so it follows the rule: it talks to the service layer, and the service talks to the repository. `UserDirectoryController` depends on `AppUserRepository` directly, so it skips the service layer: a controller reaches straight into data access. Dependencies still point down (no class outside the `controller` package imports a controller), so the rule "never point up" holds even where the rule "one layer at a time" is relaxed. A third example: `TileController` has eleven collaborators, so it also carries coordination logic that a strict layering would put in a service. Any two controllers, with their constructor fields read from the code, earn credit.

### Exercise 39.3 ★★ Cost first

Check your answer against the "What it costs" and "When not to use it" lines of the pattern you chose. For example, for the pipeline: cost is that the whole behavior is hidden in the order of the stages, so you must read the entire chain; not to use it when there are two fixed steps that always run in the same order.

### Exercise 39.4 ★★ The twelve factors

Three pieces of in-memory or local state: (1) sessions, held by the app in memory (37.5); (2) the rate-limit counters, `TileRateLimiter` and the sign-in throttle `LoginThrottle` (37.5, 37.8); (3) the tiles on the local disk volume (37.7). A fourth is the audit throttle, which limits how often events are written and lives in a map in `AuditLogService`. All of them are correct with one instance and wrong with several; that is the connection to decision 37.11 and the plan in 37.17.

### Exercise 39.5 ★★★ Apply the framework

A model answer. Problem and constraints: users want a list of pages they have viewed; the audit log already records `PAGE_VIEWED` once per session, document, and page every 10 minutes; retention is 180 days; privacy matters because the log names users. Options: (a) query the existing audit log filtered by the user (event log pattern, cheapest, but a page opened again within 10 minutes isn't recorded, and the log is purged after 180 days); (b) a new table of reading progress written on each page view (an ordinary table, more accurate, more writes, more personal data to protect and back up); (c) keep it in the browser only, as the app already does for "resume last page" (client-side state, no server cost, lost when the browser data is cleared, and not shared across devices). Costs: as stated in each. Decision record: choose (a) if approximate history is enough and expose only the signed-in user's own events; trigger to revisit: users need exact history or history beyond 180 days, or the count of events makes queries slow. Any answer that gives three options with patterns and costs, and a trigger, earns credit.

### Exercise 39.6 ★★★ Argue against a pattern

Against: rendering is CPU-heavy but happens inside the request today, bounded by the render pool (two at once) and timeout; splitting it adds a network call, a second deployable, shared storage, and new failure modes (what does the upload return if the render service is down?). The project's records show no evaluation, and one team runs one deployable. For: rendering is the resource hog, so a separate service could be scaled and limited independently and a hostile PDF could not starve sign-ins. What would have to be true: rendering load must actually slow other requests despite the bulkhead; the team or scale must justify the operational cost. What to measure first: `sdv_render_seconds`, `sdv_render_rejected_total`, and `sdv_render_abandoned_running` (Chapter 35), plus CPU and response times of unrelated endpoints during renders. If those are healthy, the split is over-engineering. A defensible middle course, noted in section 39.6: keep the monolith and improve the modules' boundaries first.
