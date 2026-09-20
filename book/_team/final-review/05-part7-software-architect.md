# Part VII review: Software Architect

Scope: `book/part-7-cloud/00-part-introduction.md`, `40-aws-production.md`, `41-aws-operations.md` (solutions files skimmed only). Lens: is the target design sound, are trade-offs and alternatives fairly treated, does it overclaim, what failure modes are missing, consistency with Chapters 32 to 39, honesty of "design, not deployment". Code claims checked with `git show`/`git grep` at `book-m6-final`. AWS documentation was not re-checked (no network reading of AWS pages); AWS statements are judged for internal logic only, and marked "unverified" where I lean on memory. My own view was formed before reading `book/_team/reviews/40-*.md`; afterwards I only looked for overlap (noted where relevant). Nothing edited except this file; nothing committed; no secrets read.

## Verdict

The design is sound at the level of building blocks and the hardest part is done well: the S3 "database pointer is the atomic switch" argument, the reserve-then-upload-then-switch ordering with a forward-only counter, the Lua-script atomicity requirement, the single-hash-slot constraint, the `SessionRegistry.getAllPrincipals()` trap, the ALB `X-Forwarded-For` trust chain and the CloudFront one-origin table are all correct against the code and reasonable engineering. The "design, not a deployment" honesty is exemplary (repeated, dated, no prices, "illustrative" listings). The weaknesses are (1) one technically wrong piece of advice that produces intermittent `502`s, (2) a restore-drill instruction that can destroy live tiles, (3) a forward-only counter that point-in-time restore silently breaks, (4) availability is claimed as a benefit while the new hard dependencies (Redis, S3, per-request) and the Region boundary are not analysed, (5) alternatives to ElastiCache and S3 (JDBC sessions, counters in MySQL, EFS) are never weighed although the chapters otherwise stress "do you need this at all", and (6) the "smallest useful first move" does not say where the single instance runs.

| Severity | Count |
|---|---|
| Blocker | 0 |
| Major | 7 |
| Minor | 12 |

## Blockers

None.

## Majors

### PA-01 ALB idle timeout advice contradicts the nginx keep-alive advice
- **Where:** Ch 40, Section 40.5, "Idle timeout" paragraph.
- **Quote:** "Raise it above `render-timeout` plus that queue wait ... and keep the server's own keep-alive timeout above the ALB's ... In the sidecar layout that server is nginx, whose 75-second default already fits"
- **Problem:** the chapter tells the reader to raise the ALB idle timeout above 3 minutes + 30 s (at least about 210 s, realistically 240 to 300 s). The rule it then quotes requires nginx's keep-alive to be *above* that value. nginx's default `keepalive_timeout` is 75 s (`frontend/nginx.conf` at the tag sets none), so it is far below a raised ALB timeout. That is exactly the situation that makes the ALB reuse a connection nginx has just closed and answer `502`. "Already fits" is true only if the ALB timeout is left at 60 s, which the same paragraph forbids. The earlier backend review (`40-backend.md` m1) checked 75 against 60 and did not see the combination. Also `proxy_read_timeout 300s` (nginx to app) is a different timer and does not help.
- **Suggested fix:** "Set nginx `keepalive_timeout` above the ALB idle timeout (for example 310 s if the ALB is at 300 s). The 75-second default fits only the ALB's default of 60 s, and you are raising it." Add the forged-header-style check: a test that idles a connection for longer than 75 s then sends a request through the ALB, expecting no `502`.

### PA-02 The restore drill can delete live tiles
- **Where:** Ch 41, Section 41.5, third paragraph; Ch 34 drill by extension.
- **Quote:** "restore the database to a point in time as a new instance, point a scratch service at it and at the versioned bucket"
- **Problem:** the scratch service runs the same image, which runs `StorageJanitor` (first run 2 minutes after start, then every 6 h; `@EnableScheduling` on the application class, verified). The janitor deletes tile version folders that no document row points at. A database restored to an earlier moment does not know documents or versions created since, so pointed at the production bucket, the scratch janitor sees newer live prefixes as orphans and can delete them (subject only to the age test the chapter says S3 lacks natively, 40.8). Even without the janitor a scratch service with the production task role has write and delete rights on the live bucket. Ch 40.10 itself says the janitor cannot be switched off per task without a code change.
- **Suggested fix:** "Never point the drill at the production bucket with write access. Restore the bucket to a scratch bucket with AWS Backup, or give the scratch task a read-only role, and start it with the janitor disabled (a flag the app does not have today; see 40.10)."

### PA-03 Point-in-time restore breaks the "counter must never reuse a number" invariant
- **Where:** Ch 40, Section 40.8 (reserve counter, forward-only rule) with Ch 41, Section 41.5.
- **Quote:** "The counter must never reuse a number, so a retry after a failed attempt uses a new prefix instead of meeting a leftover one (a `412`)."
- **Problem:** the counter lives in a database column. Restoring the database to a past point (the chapter's own recovery path) rewinds the counter. S3 still holds prefixes for versions reserved after that point (the chapter deliberately keeps them for the backup window). The next replace then reserves a number whose prefix exists: with `If-None-Match` it fails with `412` on every tile and the replace can never succeed until the counter passes the highest existing prefix; without the condition, live-looking objects would be overwritten. The chapter proves forward-only within one database timeline and does not address restore. (The same class of problem exists in Ch 34 today, but there tiles restore with the database.)
- **Suggested fix:** add to 41.5: "After any restore, set every document's version counter above the highest `v{n}` prefix that exists in the bucket before accepting uploads (a reconciliation step in the runbook and in the drill)." Or use a non-reusable version id (UUID/ULID) as the prefix and keep the counter only for ordering.

### PA-04 Availability is claimed as the gain, but the new hard dependencies and the Region boundary are not analysed
- **Where:** Ch 40 intro and 40.4, 40.5 (health check), 40.9 (failure policy row), Ch 41.5, 41.9, 41.11.
- **Quote:** "The cloud improves availability and recoverability, not the limits in the README." and "the code has no answer to 'the store is unreachable'" (Table 40.2, with no recommendation).
- **Problem:** after the move every request depends on ALB, at least one healthy task, RDS, ElastiCache (session lookup, counters) and, for tiles, S3, in series. Redis holds the sessions, so a Redis outage is a full outage of everything authenticated; the "fail open or fail closed" question the chapter poses for the tile counters (Exercise 40.5) is moot while sessions share the same store, and the chapter never says so. The recommended health check (`/actuator/health/readiness`, chosen to exclude external systems) will keep reporting healthy while Redis or S3 is down, so ECS/ALB never act and only an alarm tells you (the chapter says "watch the database with an alarm" but not the cache and S3 in that sentence). Figure 40.1 draws one ElastiCache box: a single node loses sessions and counters on an AZ failure; Multi-AZ needs a replica and automatic failover, which the text mentions only in the sentence about asynchronous replication. RDS Multi-AZ failover drops connections for tens of seconds (unverified duration; the app has no stated reconnect behavior). Nothing states RTO or RPO for the design, and "disaster recovery" (41.5 title) covers only restore drills: the design survives an AZ failure, not a Region failure (no cross-Region copy of backups or bucket).
- **Suggested fix:** add a short "What still takes you down" list: Redis (sessions, so authentication), RDS failover window, S3 errors on tile reads, a bad deploy; state "Multi-AZ covers an Availability Zone, not a Region"; recommend a policy: sign-in throttle fails closed for the account being throttled but sign-in itself needs the store, so treat the cache as a hard dependency, run a replica in a second AZ, and alarm on it; state expected RPO from PITR and that RTO must be measured with the drill. Include the cache and S3 in the readiness discussion ("readiness does not see them; alarm on them").

### PA-05 Alternatives to ElastiCache and S3 are not weighed
- **Where:** Ch 40, Sections 40.3, 40.8, 40.9.
- **Quote:** Table 40.1 row "In-memory sessions, throttle counters | Amazon ElastiCache"; "Amazon S3 stores objects in buckets and is the piece that makes a second instance possible"
- **Problem:** the chapters treat EKS and EC2 briefly (fair) but give no alternatives at all for the two most expensive parts of the redesign. (a) Sessions and counters: MySQL is already there, and Spring Session has a JDBC store; at "tens or hundreds of readers" (Table 41.2) and 180 tile requests per minute per user, a database-backed session and counter table (with the same row-lock discipline the app already uses) removes a service, its parameter group, TLS, auth token, failover and Lua scripts, at the cost of database load on every tile request (which already does a read, as 40.7 notes). Ch 37.5 named Redis as "the enterprise alternative" but not as the only one. (b) Tiles: Amazon EFS is a shared file system that Fargate can mount and that keeps directory rename semantics, so `FileOperations`/`commit`/`StorageJanitor` would change little; the trade (latency, throughput mode, per-GB cost, cross-AZ mount targets) is exactly the kind of Choice/Pros/Cons/Alternative comparison Chapter 37 teaches. RDS versus Aurora, and DynamoDB for counters, are also unmentioned (Aurora is a fair one-line "not needed at this size"). The "stay on one server" alternative is treated fairly (Table 41.2, 40.4), but the middle path (one bigger server, off-site backups to S3, a tested restore) is only implied.
- **Suggested fix:** add a compact Table 40.3 "Alternatives considered" with one row each: ElastiCache vs JDBC session/counters in MySQL; S3 vs EFS; RDS vs Aurora; Fargate vs EC2/EKS; state which the book would pick at 80 readers and why (likely: RDS + S3 first, and a second instance only if needed).

### PA-06 "Smallest useful first step" does not say where the single instance runs
- **Where:** Ch 40, Section 40.4; Ch 41, Section 41.11, Figure 41.1 and Exercise 40.1/41.2.
- **Quote:** "Moves A to C improve things with *one* instance still running: Secrets Manager and a task role remove the `.env` file and stored keys; RDS ...; S3 ..."
- **Problem:** a task role and a Secrets Manager injection exist only for a container running on ECS (or another AWS compute). If the one instance stays on the existing server, moves B and C put the database and tile reads across the internet or a VPN, a publicly reachable RDS or bucket, extra latency on every tile, and internet egress cost, and the "no stored AWS key" benefit of A is not available. If the one instance runs on Fargate, then compute and part of the network (moves in F: task definition, ALB, ACM) are already done before A. Figure 41.1 puts the front door last, so the order cannot be followed literally with one instance. The text never states the intermediate topology.
- **Suggested fix:** define "move 0: one Fargate task behind an ALB (or reachable directly) in a VPC" as the base of A to C, say A to C assume it, and mark the hybrid (server outside AWS talking to RDS/S3) as an option with its risks (never a public RDS endpoint). Adjust Figure 41.1 accordingly or add a note that F splits into "one task" and "two tasks".

### PA-07 Versioned bucket plus delayed delete means "deleted" documents survive; not stated
- **Where:** Ch 40, Section 40.8 (janitor); Ch 41, Sections 41.5, 41.7 (`noncurrent_days = 30`), 41.9.
- **Quote:** "set the noncurrent-version expiry ... to at least the longest RDS backup retention you configure (RDS keeps automated backups for up to 35 days)"
- **Problem:** to keep restores consistent the design keeps every superseded and every deleted document's tiles for up to 35 days in the bucket, and the database backups keep the rows for as long. The app's user-visible "delete" and "replace" therefore do not remove content for 30 to 35 days, and the audit/retention story (180-day audit purge) does not mention it. For an app whose purpose is controlled access to sensitive documents this is a security and compliance fact (who can read noncurrent versions: the same task role only if `s3:GetObject` with version id is granted; an administrator with bucket access can read all). It is also a cost driver (every replaced 500-page document stays about 6,000 objects). 41.9 lists new risks but not this one.
- **Suggested fix:** add to 41.9: "Deleted and replaced documents remain recoverable in the bucket and in database backups until the retention windows end; decide the windows against your data-retention rules, restrict who can read noncurrent versions, and say so in the privacy notice." (The gap I reported for Ch 32/34 in the first review, SA-03, applies here too: tiles and backups are unwatermarked copies; SSE protects the disk, not the reader with IAM access.)

## Minors

### PA-08 Redis scripts: the sketch uses the caller's clock, the table says one clock
- **Where:** Ch 40, Listing 40.9 vs Table 40.2 row "One clock".
- **Quote:** "ARGV: now_ms, window_ms, limit, unique_id" vs "Use the store's clock".
- **Problem:** the illustrative script takes `now_ms` from the caller, i.e. from each task's clock; the table says use the store's clock. Also `Retry-After` is computed by the caller "from the oldest score" in a second call, so the value is not atomic with the decision (harmless, but say so). 
- **Fix:** read the time inside the script (`redis.call('TIME')`) or say "the sketch takes the time from the caller for brevity; production code should use the store's time".

### PA-09 ElastiCache Serverless is excluded by the hash-slot rule but the text does not say so
- **Where:** Ch 40, Section 40.9 and Table 40.2.
- **Quote:** "the newer `FUNCTION` commands are not available on serverless caches, so use plain scripts" vs "cluster mode *disabled*".
- **Problem:** the first sentence suggests a serverless cache is a candidate; the table's cluster-mode-disabled requirement rules it out (serverless caches run in cluster mode; unverified against AWS docs, from memory) unless the three sign-in keys are redesigned to share a slot with hash tags, which the table says they cannot (different key values).
- **Fix:** "This rules out a serverless cache unless you redesign the keys; use a node-based replication group."

### PA-10 Losing-replace behavior changes and is not described for the user
- **Where:** Ch 40, Section 40.8.
- **Quote:** "a losing replace discards its own upload."
- **Problem:** today two concurrent replaces serialize and the last one committed wins; in the new shape the one that reserved the higher number wins even if it finished first, and the other publisher's replace silently fails after a long upload. The API and page behavior (which status? `409`? what message?) and the audit event are not stated, and Ch 39.11 / Fig 39.5 still describe the current shape.
- **Fix:** one sentence: return `409 Conflict` with a message to retry, and record an audit event; note the semantic change (last reserved wins).

### PA-11 Upload verification and S3 partial failure detail
- **Where:** Ch 40, Section 40.8.
- **Problem:** the design uploads about 6,000 objects inside the request that already renders for up to 3 minutes; the extra time (parallel PUTs, SDK retries and back-off on throttling `503 SlowDown`), and its effect on the ALB idle timeout, `proxy_read_timeout 300s`, and the 30 s queue, is not budgeted. The rule "switch only after every object is acknowledged and the count matches the manifest" is implied but not stated. Because the janitor is the only cleanup, a crash between upload and switch leaves up to 6,000 orphan objects for the janitor (fine, but say so).
- **Fix:** add "verify all N objects (use the page and tile counts from the render) before locking the row; include upload time in the timeout budget; parallelize uploads with bounded concurrency".

### PA-12 Single runner: overlapping and duplicate runs are not addressed
- **Where:** Ch 40, Section 40.10.
- **Problem:** EventBridge Scheduler starts a task; a run that takes longer than the interval, a retry, or an at-least-once invocation can overlap another run, and a janitor pass overlaps normal uploads. The leader-lock alternative has the classic lock-expiry split-brain (a paused holder continues after its lease ends). The text calls the scheduler "simpler to reason about" without these caveats. It also says "six scheduled methods in all"; at `book-m6-final` `git grep @Scheduled` finds five (`AuditLogService` x2, `KnownDevices`, `LoginThrottle`, `StorageJanitor`). (Ch 14 also says six; my first review did not find a sixth.)
- **Fix:** "make the job idempotent and guard it with a short lease or a database lock so an overlapping run exits at once"; correct the count to five or name the sixth.

### PA-13 KMS mentioned in 40.8 but the task role policy in 41.2 says "no key management"
- **Where:** Ch 40.8 (KMS option, Bucket Keys) vs Ch 41, Listing 41.2.
- **Problem:** with SSE-KMS the task role also needs `kms:GenerateDataKey` and `kms:Decrypt` on the key, and the key policy must allow it; the listing and sentence ("no key management") would give a working design that fails with `AccessDenied` on the first tile.
- **Fix:** "with SSE-KMS add the key's `Decrypt`/`GenerateDataKey` for this role only".

### PA-14 CloudFront/WAF in front of the ALB: bypass not mentioned
- **Where:** Ch 41, Section 41.8, Table 41.1.
- **Problem:** if CloudFront or WAF is added for protection, the ALB is still reachable directly unless its security group accepts only CloudFront (managed prefix list) and/or a secret origin header; otherwise the "layer in front of the app's rules" is optional for an attacker. Table 41.1's client-address row also implies trusting a CloudFront-added header, which is safe only with that restriction.
- **Fix:** add a Table 41.1 row "Direct access: restrict the ALB to CloudFront".

### PA-15 Infrastructure-as-code data-loss traps and encryption in transit at the ALB hop
- **Where:** Ch 41, Section 41.7 and 41.3.
- **Problem:** Listing 41.4 creates the tiles bucket without `prevent_destroy`/deletion protection notes; a `terraform destroy` or resource replacement of the bucket or RDS is the classic data-loss incident, and the text says only "review every plan". RDS deletion protection and final snapshot are not mentioned. The ALB to nginx hop is plain HTTP inside the VPC; acceptable, but 41.9's "Unencrypted new hops" list does not name it, and there is no HTTP to HTTPS redirect listener (port 80) mentioned; HSTS is only advised in nginx.
- **Fix:** add `prevent_destroy` and RDS deletion protection to the notes; add one line on the ALB-to-task hop and the redirect listener.

### PA-16 Cross-chapter inconsistencies
- **Where:** several.
  - Ch 40, Section 40.3, prose: "Chapter 41, Figure 41.1, at the end of this chapter" (Figure 41.1 is in Ch 41).
  - Table 40.1 "plan step" column: "(5) secrets" and "(7) the second and third instance"; Ch 37.17 Step 5 is "the same secret everywhere" and Step 7 "deploy without downtime". The mapping is close but not identical.
  - Ch 37.13 and 37.17 Step 6 say to point the load balancer's health check at `/actuator/health`; Ch 40.5 says to use `/actuator/health/readiness` and explains why. Add a forward pointer in 37.13/37.17 or a sentence in 40.5 that this refines Chapter 37.
  - Ch 39.11 / Fig 39.5 caption "only the switch happens under the lock" versus Ch 40.8 "`tiles.commit(...)` runs inside the transaction" (Listing 39.2 shows the commit inside the `tx.execute` lambda, so Ch 40 is the more accurate).
- **Fix:** align the four sentences.

### PA-17 Chapter boundary between 40 and 41 splits topics that belong together
- **Where:** Ch 40 vs Ch 41.
- **Problem:** the network layout (private subnets, security groups, endpoints) is in 41.3 while Ch 40's Figure 40.1 and text already depend on it; TLS to RDS and S3 encryption are in 40.7/40.8 while the security review of new hops is 41.9; graceful shutdown and connection draining (41.6) are compute concerns tied to the ALB idle timeout in 40.5; the S3 versioning/lifecycle rule spans 40.8, 41.5 and 41.7 (Listing 41.4). The decision "when not to go" is at the very end of Ch 41 (Ch 40.4 gives a short early version, good). Chapter 40 is 8,485 words, above the 8,000 target.
- **Fix:** either move 41.3 (network) into Ch 40 next to Figure 40.1, or add explicit "continued in" pointers at each split; consider moving 41.6's graceful-shutdown paragraph next to the idle-timeout paragraph. Keep the early "do you need this at all" and repeat it as a one-line pointer at the start of Ch 41.

### PA-18 Smaller accuracy and honesty items
- Ch 40, blockquote at the top: the sentence "Where a claim could not be verified, the text says so." appears twice in a row.
- Ch 40.9: sessions serialized with Java serialization "test that every attribute round-trips" is right, but a rolling deploy with a changed serialized class or attribute can make old and new tasks unable to read each other's sessions; add "keep session attribute classes backward compatible for one release, or use JSON".
- Ch 41.6: "With shared sessions, a rolling deploy signs nobody out" holds only with the previous point; state it conditionally.
- Ch 41.10: cost list omits cross-AZ data transfer (ALB to tasks, tasks to RDS/Redis) and S3 list/request costs of the janitor's full-bucket listing (40.8 does mention the latter).
- Part introduction: "would that make the app's actual services better?" is a good frame; add one sentence naming the new dependencies as a cost to the "services" (see PA-04).

## Checked and found correct (architecture lens)

- Target architecture (Fig 40.1): ECS on Fargate with an nginx sidecar, ALB in two AZs, RDS Multi-AZ, ElastiCache, S3 through a gateway endpoint is a conventional, defensible shape for this app; the sidecar keeps the frontend image and the trust model intact.
- Code claims verified at `book-m6-final`: `TileWorkLimiter` default 0 = twice the CPU count; `max-concurrent-renders`; Dockerfile `-XX:MaxRAMPercentage=75`; Compose `mem_limit` 1536m (app) and 128m (nginx); `proxy_read_timeout 300s`, `listen 8080`, `/actuator/health` already proxied; `@EnableScheduling`, janitor timings hard-coded (`PT2M`, `PT6H`); audit purge and device purge have cron properties; `SessionAdministration` calls `registry.getAllPrincipals()` in three places; `SecurityConfig` builds `SessionRegistryImpl`; `SessionLifetimeFilter.SIGNED_IN_AT`; `LoginThrottle.reserve` is `synchronized`; `TileRateLimiter` window and `synchronized (window)`; `Files.exists(target)` check in `commit`; `commit` runs inside the transaction in `replaceFile`; `TileController` does a database read per tile.
- The atomic-switch argument (pointer, immutable versions, forward-only counter within one timeline, `If-None-Match`), the split of what must stay local (`TileWorkLimiter`, sweeps) versus shared, the "`SessionAdministration` must change" finding and its security consequence (role-change session revocation), and the trusted-proxy chain (`real_ip_recursive off` taking the last address, `map` key as exact string, `XSRF-TOKEN` Secure attribute follows the proxied protocol) are sound and consistent with Chapter 32/33.
- CloudFront section: the one-origin rule, the loss of session binding, access re-check and watermark with signed URLs, `no-store` and never caching `/api/tiles`, CSP blocking cross-origin tile bytes, WAF "knows addresses, not accounts" are honest and consistent with Chapter 37.4 and 39.9.
- Honesty about "design, not deployment", dated documentation checks, "illustrative" labels and "no prices" is consistent in the part introduction and both chapters; Table 41.2 "when not to move" is a fair statement of staying on one server. Consistent with Ch 37.17 and Figure 37.2 on the need to share sessions, counters and tiles first, and with Ch 39 (twelve-factor, immutable versions).

## Not checked

- AWS documentation itself (service limits, defaults, ALB/ElastiCache/S3/CloudFront/WAF behavior, AWS Backup PITR windows, RDS failover time, Fargate ephemeral storage figures): not re-read; those items are the AWS-research reviewer's remit (`40-research-aws.md`). Items where I lean on memory are marked "unverified".
- Whether Spring Boot 4.1.1 defaults to graceful shutdown (the chapter says it does not; `application.yml` has no `shutdown` setting): unverified.
- Solutions files (`40-*.solutions.md`, `41-*.solutions.md`): skimmed only; exercise difficulty ratings left to the beginner reviewer.
- Listings 40.1 to 40.3, 40.4, 40.6, 40.7, 40.8 were compared only in substance (key lines), not line by line; the reviews in `book/_team/reviews/40-*.md` (backend, QA) cover listing exactness.
- PDF rendering of Part VII was not inspected. In my first review the pipe tables were clipped in the PDF; Table 40.1 (four columns) and Table 40.2 (three columns) and Table 41.1 are likely affected unless the build was fixed; please re-check after the table fix.
- Terraform, IAM and OIDC snippets were read for consistency with the prose, not validated against provider schemas.
