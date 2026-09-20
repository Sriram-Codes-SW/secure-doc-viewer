# Part VII review: Senior Full-Stack Engineer (hands-on correctness)

Scope: `book/part-7-cloud/00-part-introduction.md`, `40-aws-production.md`, `41-aws-operations.md` and both solutions files. Source review only. I formed my view before reading `book/_team/reviews/40-*.md` and did not read them. No chapter edited, nothing committed, no secret files opened. App-side claims were checked with `git show book-m6-final:<path>`; AWS claims were spot-checked from my own knowledge of the AWS documentation (I had no web access), so they are marked "unverified" where I am not sure.

## Verdict

The chapters are unusually careful. The seven real listings (40.1 to 40.4, 40.6 to 40.8) match `book-m6-final` (listing checker: Ch 40 "7 ok"; the illustrative listings 40.5, 40.9 and 41.1 to 41.4 are all captioned "illustrative", "not in the repository" or "not run", so a beginner will not mistake them for project code). The app-side facts I checked are all true at the tag: `application.yml` trusted-proxy default and `FORWARD_HEADERS_STRATEGY` default `none`; nginx `proxy_read_timeout 300s`, per-location `add_header`, `X-Forwarded-Proto $forwarded_proto`; `TileWorkLimiter` default `2 * availableProcessors()`; `MaxRAMPercentage=75`; `mem_limit 1536m`; no graceful shutdown property; `SessionAdministration` calling `registry.getAllPrincipals()` in `list`, `revoke` and `revokeAllFor`; `SessionMetadata` on `SessionDestroyedEvent`; `LoginThrottle.reserve` `synchronized` and `unlock`/`isLocked` prefix scans; the six scheduled methods (janitor timings literal, the two purges property-driven); `StorageJanitor` mtime test and the missing-current-version guard; `replaceFile` committing tiles inside the lock while a first upload commits before its transaction; `AuditLogService.record` `REQUIRES_NEW` (called from inside the replace transaction via `recordDenied`, so two connections are held); `SESSION_COOKIE_SECURE`, `CookieCsrfTokenRepository` without an explicit secure flag (so the `XSRF-TOKEN` remark in 40.6 is right); `<base href="/">`; "Step 2 of 2: preparing pages"; "Upload failed."; viewer retries only 429 and 503; the CSP with `img-src 'self' blob: data:`; probes enabled and `/actuator/health/**` permitted. The Terraform, IAM, JSON, YAML and Lua excerpts are syntactically valid. The main problems are two configuration recommendations that would not work as written, one IAM/S3 behavior that could turn a designed `410` into a `500`, and a few precision items.

## Counts

| Severity | Count |
|---|---|
| Blocker | 0 |
| Major | 3 |
| Minor | 9 |

## Major

### P7-01 Raising the ALB idle timeout while relying on nginx's 75-second keep-alive default would cause 502s
- **Where:** Ch 40, section 40.5, "Idle timeout" paragraph (`part-7-cloud/40-aws-production.md`).
- **Quote:** "Raise it above `render-timeout` plus that queue wait ... and keep the server's own keep-alive timeout above the ALB's ... In the sidecar layout that server is nginx, whose 75-second default already fits (check it for your build)".
- **Problem:** the paragraph contradicts itself. `render-timeout` is 3 minutes plus up to 30 seconds queue wait, so the ALB idle timeout must be set to roughly 210 seconds or more (say 240). nginx's default `keepalive_timeout` is 75 seconds, which is *below* that, so it does not "fit": nginx would close idle upstream connections that the ALB still considers open, which is exactly the `502` race the paragraph warns about. `nginx.conf` at the tag sets no `keepalive_timeout` (grep of `frontend/nginx.conf`), so the default applies.
- **Suggested fix:** "nginx's default `keepalive_timeout` of 75 seconds is shorter than the raised ALB timeout, so set `keepalive_timeout` in `nginx.conf` above the ALB value (for example 300s, matching `proxy_read_timeout`)". Add `keepalive_timeout` to the nginx change list in section 40.4 and the "In this project" table, and to Common mistakes.

### P7-02 A graceful shutdown as long as the longest render cannot be configured on Fargate
- **Where:** Ch 41, section 41.6.
- **Quote:** "Set `server.shutdown: graceful` with a shutdown timeout at least as long as the longest render, and set the ECS stop timeout and the target group's deregistration delay to match (check the current limits)".
- **Problem:** the longest render is 3 minutes (`render-timeout: 3m`), but as far as I know the ECS container `stopTimeout` is capped at 120 seconds on Fargate (unverified against the current guide; I could not fetch it). If so, the recommended design cannot be met and the "check the current limits" hedge hides that the answer is probably "you cannot". The deregistration delay (up to 3600 s) is not the limit; `stopTimeout` is.
- **Suggested fix:** state the Fargate `stopTimeout` maximum explicitly after checking it, and offer the honest consequence: either cap `render-timeout` and the queue wait below it, or accept that an in-flight render can be cut and lean on the janitor (the chapter already says staging is cleaned by the janitor), or run uploads as a separate service with a longer drain. Change "at least as long as the longest render" to "as long as the platform allows, and no longer than its maximum".

### P7-03 The task-role policy may turn a missing tile into a 403, and therefore a 500 instead of the designed 410
- **Where:** Ch 41, Listing 41.2; Ch 40, section 40.8 ("An S3 'no such key' must map to the same two outcomes as a missing file today"); solutions 41.3 ("plus `s3:ListBucket` ... if the app lists").
- **Quote:** `"Action": "s3:ListBucket", "Resource": "arn:aws:s3:::BUCKET", "Condition": { "StringLike": { "s3:prefix": ["tiles/*"] } }`.
- **Problem (unverified, but a known S3 behavior):** S3 returns `404 NoSuchKey` for a missing key only if the caller has `s3:ListBucket` on the bucket; otherwise it returns `403 AccessDenied`. A `GetObject` request carries no `s3:prefix` context key, so a policy that grants `ListBucket` only under an `s3:prefix` condition may not satisfy that check for a plain `GetObject` on a missing key. The app relies on the missing-file signal: `TileGenerationService.loadRawTile` throws `TileGoneException` (410) when the tile is absent, which is the mechanism behind the replaced-render reload. With a 403 the S3 implementation would surface a server error instead, and the solution text treats `ListBucket` as optional ("if the app lists").
- **Suggested fix:** in section 40.8 add "S3 answers a missing key with 403 unless the caller may list the bucket; either grant `s3:ListBucket` without the prefix condition for the service role or treat 403 on `GetObject` as 'not found' and test it." Make the same point next to Listing 41.2 and fix solution 41.3. Add a test to the migration checklist: request a tile of a replaced version and expect 410.

## Minor

### P7-04 Listing 40.5 claims to mirror the real methods but omits `discard`
- **Where:** Ch 40, Listing 40.5 and the sentence "The names mirror the real methods."
- **Problem:** `DocumentService.replaceFile` calls `tiles.render(...)`, `tiles.deleteVersion`, `tiles.commit` and, on failure, `tiles.discard(rendered)` (public in `TileGenerationService`). The interface has no `discard`, though the following paragraph does say an S3 commit "must also discard the staging folder". Note also that today's `replaceFile` calls `deleteVersion(nextVersion)` before `commit` (debris cleanup under the lock), which the reserve-a-counter design in 40.8 replaces; the chapter does not say that this existing step disappears.
- **Fix:** add `void discard(RenderedDocument rendered) throws IOException;` and say "the four storage methods plus `discard`"; one sentence that the "delete next version before commit" step is replaced by never reusing numbers.

### P7-05 The Lua sketch and Table 40.2 disagree
- **Where:** Ch 40, Listing 40.9 versus Table 40.2 and Solution 40.5.
- **Quote:** listing: `ARGV: now_ms, ...`, `return 1`; table: "Use the store's clock"; "Return an id from the script".
- **Problem:** the sketch takes the time from the calling task (`ARGV[1]`, each task with its own clock, which is exactly what row "One clock" says to avoid) and returns 0/1, not an id. It is labeled "shape only", but a reader copying it gets both defects. (Also `ZREMRANGEBYSCORE ... 0, now-window` prunes inclusively while the Java prunes strictly `isBefore(cutoff)`; harmless.)
- **Fix:** use `redis.call('TIME')` inside the script (allowed in scripts on modern Redis and Valkey; check the engine version for your cache) and return the member id or `{status, oldestScore}`, or add a comment "the real script would use TIME and return an id".

### P7-06 Terraform lifecycle rule will let delete markers pile up
- **Where:** Ch 41, Listing 41.4; Ch 40, "The janitor, honestly".
- **Problem:** with versioning on, the app's `deleteVersion` only adds delete markers (roughly 6,000 per superseded large document). A rule with only `noncurrent_version_expiration` removes the old versions but leaves markers with no remaining versions ("expired object delete markers"); cleaning them needs `expiration { expired_object_delete_marker = true }` (it cannot be combined with `days`). Chapter 40 says "check that markers don't pile up" but the listing that follows has no fix, so the sketch would do the wrong thing on a large bucket, and listing the bucket would slow down.
- **Fix:** add to the rule:
  ```hcl
  expiration {
    expired_object_delete_marker = true
  }
  ```
  and mention it in the "Three notes" paragraph.

### P7-07 nginx port move touches more places than the chapter lists
- **Where:** Ch 40, section 40.6 (sidecar paragraph) and the "In this project" table.
- **Quote:** "`proxy_pass` changes from `http://app:8080` to `http://127.0.0.1:8080`, and the ALB targets 8081."
- **Problem:** `frontend/nginx.conf` has two `proxy_pass http://app:8080` lines (the `/api/` location and the exact `/actuator/health` location) plus `listen 8080;`; `frontend/Dockerfile` has `EXPOSE 8080`; the Compose `web` health check probes 8080. The chapter names "the container's port declaration" but not the second `proxy_pass`. A beginner changing only the first would leave the health location pointing at `app`, which does not resolve in a task.
- **Fix:** "both `proxy_pass` lines". Also note that in the `map`, a concrete regex example helps: `~^10\.0\.(1|2)\.` for two ALB subnets (illustrative), with a warning to escape the dots.

### P7-08 Duplicate sentence in the opening note
- **Where:** Ch 40, first callout.
- **Quote:** "Where a claim could not be verified, the text says so. Code marked "illustrative" is a sketch that was never run. Where a claim could not be verified, the text says so."
- **Fix:** delete the second copy.

### P7-09 Figure 40.1 draws the ALB outside the VPC
- **Where:** Ch 40, Figure 40.1 and the sentence after it ("The box labeled VPC is the private network; only the load balancer is reachable from the internet").
- **Problem:** the ALB node is declared before the subgraph, so Mermaid puts it outside the VPC box, but the ALB lives in public subnets of the VPC (Ch 41, section 41.3 says so). The figure also shows only the S3 gateway endpoint, while private tasks pulling from ECR, reading Secrets Manager and sending logs need a NAT or interface endpoints (Ch 41.3 covers this in prose).
- **Fix:** put the ALB inside the VPC box as "public subnets", and add a note or a NAT/endpoint node; or say in the text description that the figure omits ECR, Secrets Manager and log endpoints (Section 41.3).

### P7-10 Exercise 40.1 and its solution ask for and answer different things
- **Where:** Ch 40, Exercise 40.1 ("which of the four (or which two) the project could adopt first"); Solution 40.1.
- **Problem:** the solution names "the first three moves" (Secrets Manager, RDS, S3), but the four items in the exercise are the `mysql` container, the `app-storage` volume, the `.env` file and Caddy, so the answer is three of the four, not "two", and Caddy is correctly not among them. The "strongest first pair" wording adds a fourth answer.
- **Fix:** ask "which of the four could you adopt first with one instance still running?" and answer "all but Caddy: `.env`, `mysql`, `app-storage`; if you can only do two, Secrets Manager plus RDS".

### P7-11 CloudFront origin timeout stated too absolutely; WAF console name unverified
- **Where:** Ch 41, Table 41.1 row "Timeouts"; section 41.8 ("the console now calls a web ACL a 'protection pack'").
- **Problem (unverified):** as I recall, the CloudFront origin response timeout is adjustable per origin up to 60 seconds without a quota request, and only beyond that needs one; "raising it needs a quota request" is true only above 60 seconds, which matters because the render can take 180 seconds. The "protection pack" console naming I could not verify.
- **Fix:** "CloudFront's origin response timeout defaults to 30 seconds; values above the standard maximum need a quota request (check the current limits)"; keep "protection pack" only if checked, otherwise drop the parenthesis.

### P7-12 "Egx" and the Spring Session property name are hedged but the Boot 4 name is likely different
- **Where:** Ch 40, section 40.9.
- **Quote:** "`spring.session.redis.configure-action` ... (check the name for your version)".
- **Problem:** at Boot 4 the Spring Session Redis properties are, to my knowledge, under a different prefix than at Boot 3 (Spring Session was split into modules). The hedge is honest; just be aware that the concrete name shown may not exist on Boot 4.1.1. Unverified.
- **Fix:** say "the name has moved between Spring Boot versions; use the `ConfigureRedisAction.NO_OP` bean, which does not depend on the property".

## Exercises and solutions

- Exercises 40.3 (`198.51.100.7, 203.0.113.9` at nginx, `203.0.113.9` at the app and in the audit log, `real_ip_recursive off`), 40.4, 40.5, 40.6, 41.1, 41.2, 41.3 and 41.4 have correct, internally consistent solutions. Solution 40.6 matches the code: `SessionAdministration` has `list`, `revoke`, `revokeAllFor`; `LoginThrottle` has `reserve`, `succeeded`, `unlock`, `isLocked`; `TileController` holds the `Instant` for `refund`.
- Solution 41.3's janitor role (list plus delete, no get/put) is consistent with the janitor described in 40.8 (it uses the listing's last-modified time). The service-role note "`ListBucket` if the app lists" should become a requirement (P7-03).
- Exercise 40.2 and Solution 40.2 teach the right habit (caption or source comment marks code versus documentation) and match how the chapter is written.
- Exercise 40.1: see P7-10.

## Checked and found correct (code and config)

- Listing 40.1 to 40.4, 40.6 to 40.8 exactly as in the repository at `book-m6-final`.
- Sidecar reasoning: with awsvpc, nginx and the app share `127.0.0.1`; the app default `127\.0\.0\.1|0:0:0:0:0:0:0:1` fits; keeping `FORWARD_HEADERS_STRATEGY=native` is required because the default is `none`. Correct and well explained, including the failure mode.
- nginx `map` keys are exact strings (regex `~` or `geo` needed for CIDR); `set_real_ip_from` accepts CIDR; `real_ip_recursive off` takes the last entry; `add_header` not inherited by locations that define their own.
- The Spring Session limitation (`SpringSessionBackedSessionRegistry` does not implement `getAllPrincipals()`) and its effect on the admin sessions page, revoke-by-handle and revoke-on-role-change; the `SessionMetadata` events issue.
- Redis Lua atomicity, single hash slot for multi-key scripts, sorted-set sliding window: sound designs. Illustrative Lua is valid syntax.
- Listing 41.1 (`secrets`/`valueFrom` ARN form), Listing 41.2 (valid IAM JSON; prefix condition syntax), Listing 41.3 (valid workflow fragment: `id-token: write`, `contents: read`, SHA-pinned placeholder), Listing 41.4 (all resource and argument names are valid; the `depends_on` on versioning is right; the health-check default mismatch 5/2 (ALB) versus 3/3 (Terraform provider) is right).
- ALB defaults (30 s interval, 5 s timeout, 5/2 thresholds, `200`), idle timeout 60 s (1 to 4000), `routing.http.xff_header_processing.mode` default `append`, fail-open behavior, Fargate CPU/memory pairs and 20 to 200 GiB ephemeral storage, S3 default encryption since January 5, 2023, `If-None-Match: *` with `412`/`409`, S3 no atomic cross-key updates: all match my knowledge of the AWS docs.
- Cross-references inside the part: sections 37.2, 37.4, 37.7, 37.17, Figure 37.2, 39.11, 39.13, 39.14, Exercise 33.3 all exist and say what is claimed.

## Not checked

- I could not open the AWS documentation, so every AWS number and behavior above is from memory; those I am unsure of are labeled unverified (P7-02, P7-03, P7-11, P7-12).
- Nothing was run: no Terraform validation, no `nginx -t`, no `redis-cli`; syntax was judged by reading.
- I did not check `part-7-cloud/00-part-introduction.md` beyond its structure and the honesty frame, nor Appendix C text beyond Exercises 40.1 and 41.3, nor the reviews in `book/_team/reviews/40-*.md` (deliberately read last, and not read at all).
- Source comments (`<!-- source: ... -->`) were not audited one by one against the AWS pages.
