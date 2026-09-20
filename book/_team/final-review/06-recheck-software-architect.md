# Recheck: Software Architect (SA-01..19 and PA-01..18, plus new Part VII content)

Method: current sources in `book/` re-read or grepped; code claims re-checked with `git show`/`git grep` at `book-m6-final`; PDF matters checked in the rebuilt `book/build/out/secure-doc-viewer-guide.pdf` (750 pages, built 14:45). PDF pages viewed as images: 564, 565 (Fig 37.1 order), 567 (Table 37.1), 471 (Fig 30.1), 591 (Fig 39.2), 601 (Table 39.3), 652 (Appendix A glossary); a scripted scan of all 750 pages found no word within 30 pt of the right edge. Nothing edited except this file; nothing committed; no secrets.

One correction to my earlier reports: SA-12 and PA-12 said there are five `@Scheduled` methods. That was wrong. `git grep` finds a sixth, `TileRateLimiter.java:80`, which uses the fully qualified `@org.springframework.scheduling.annotation.Scheduled`. The book's "six, one fully qualified" is correct.

## Result summary

| | Fixed | Partly | Not fixed |
|---|---|---|---|
| SA-01..19 | 16 | 3 (SA-10, SA-14, SA-17) | 1 (SA-02 blueprints; Fig 39.2 and 37.1 are fixed) |
| PA-01..18 | 18 | 0 | 0 (PA-06 is fixed in text but has a new ordering flaw, see NEW-1) |

Open after recheck: Blocker 0, Major 2 (SA-02, NEW-1), Minor 7.

## Status per finding

### First review (SA)
- **SA-01 Fixed (PDF).** Table 37.1 now prints all four columns including "Enterprise alternative" (PDF p.567); Table 39.3 shows the cost column with all five options (p.601); the Appendix A glossary shows definitions plus a "First defined" column (p.652); no text lies past the right margin on any page.
- **SA-02 Partly (PDF).** Fig 39.2 and Fig 37.1 are now top-to-bottom and legible (p.591, p.565). The wide left-to-right blueprint figures are unchanged: Fig 30.1 (p.471) is still a 1568 px image squeezed to 157 pt high with labels of roughly 4 pt. Same shape on PDF p.436, 485, 538, 677, 679 (blueprints and their repeats). A text description was added, which helps screen readers, not print readers. Fix: render blueprints `flowchart TB`, or split into two figures, or shorten node labels.
- **SA-03 Fixed.** Ch 32.11 adds unprotected copies at rest, username discovery, tokens in the URL; Ch 34.11 adds "Protect the copies" (restrict, encrypt before leaving the machine, delete on a schedule).
- **SA-04 Fixed.** Ch 39.5 names the two package cycles, `AuthController`'s 13 collaborators and the session revocation in `UserAdminController`; Fig 39.2 now includes the `security` package and both cycles (verified against my earlier import scan).
- **SA-05 Fixed.** Ch 29 and Ch 30 now say "The AI product-owner reviewer" (lines 645 and 1026).
- **SA-06 Fixed.** Ch 39.9 says the URL is "shaped like" a capability URL but "not a capability in the strict sense"; Fig 39.4 caption now says the server still re-checks access on every tile.
- **SA-07 Fixed.** Ch 30 now says six times more pixels a minute and about 2.4 hours to 33 minutes (about 4.4 times overall), in both places.
- **SA-08 Fixed.** In Ch 25 to 31 the order is now Blueprint, Decisions, In this project, Try it.
- **SA-09 Fixed.** `v5-platform.md` and `v6-final.md` now state that the drawing is a deployment view and name the omitted classes.
- **SA-10 Partly.** Two `*Path:*` lines now name `DocumentService.java` and `AuditLogService.java`, but the caption still reads "`DocumentService.java` (..., two excerpts, in file order)". Fix: name both files in the caption.
- **SA-11 Fixed.** Ch 38.9 and 39.6 now say the compartments are separate permit pools that share CPU, memory and Tomcat threads.
- **SA-12 Fixed.** 37.17 Step 0 adds `SessionMetadata` and the per-instance limits; Step 4 lists all six scheduled methods correctly (see the correction above).
- **SA-13 Fixed.** The Ch 39 "First appears" column is now accurate (controllers at m0, Angular at m1, Compose MySQL-only from m1, `audit/` from m2, and so on).
- **SA-14 Partly.** Ch 21, 26 and 31 mention PR #13 and the stale README canvas line. The Part IV introduction still says "In total the repository has 38 commits and pull requests numbered 1 to 12" with no mention of #13; Ch 7 line 226 says the last box is the merges of 9 to 12 (true at the tag) but I could not confirm it mentions #13. Fix: add "(at `book-m6-final`; PR #13, a documentation fix, came later)" to the Part IV intro sentence.
- **SA-15 Fixed.** Table 36.1 now says "an advisory known to the OSV database on the day the job ran".
- **SA-16 Fixed.** Fig 37.1 now shows "Today" first (PDF p.565). Layout remark: PDF p.560 is half blank because the figure moved to the next page; the layout artist may want to allow the figure to float.
- **SA-17 Partly.** The epilogue now says "took the app toward production", adds "Part VII pointed past the single server, on paper", and states that the cloud design was never deployed. The phrase "the first version of nearly every protection had a gap" (line 35) is unchanged. Fix: "many protections".
- **SA-18 Fixed.** Ch 39.17 adds options (d) do nothing and (e) MFA or identity provider, and says "retold with the framework"; Table 39.3 is complete in the PDF.
- **SA-19 Fixed.** Ch 37.16 now says "Flyway (11 in the pull request text ...)" and Ch 30 says Flyway 12.

### Part VII (PA)
- **PA-01 Fixed.** 40.5 now sets nginx `keepalive_timeout` above the ALB idle timeout (330 s for 300 s), explains that 75 s fits only the default 60 s, adds Table 40.3 row, Example 40.1 line, Common mistakes and Summary. Technically correct.
- **PA-02 Fixed.** 41.5 "Never point the drill at the production bucket with write access": scratch bucket or read-only role, janitor off (with the honest note that no flag exists today), Fig 41.3 shows the boundary.
- **PA-03 Fixed.** 41.5 "A restore rewinds the version counter" plus 40.8 forward pointer. See NEW-2 for a scope refinement.
- **PA-04 Fixed.** New "What still takes you down" and "Recovery objectives" in 41.5, cache as hard dependency in 40.9, readiness blind spot in 41.4 (Fig 41.2), AZ not Region stated. Recovery time is honestly left to the drill.
- **PA-05 Fixed.** Table 40.2 (ElastiCache vs Spring Session JDBC and MySQL counters; S3 vs EFS; RDS vs Aurora; Fargate vs EC2/EKS; scheduler vs leader lock) with "when the alternative wins" and a stated pick for 80 readers. DynamoDB is explicitly left unevaluated, which is honest.
- **PA-06 Fixed in text; see NEW-1.** 40.4 "Where does the single copy run?" and Fig 41.5 "move 0" now exist, but the order 0, A, B, C is not executable as drawn.
- **PA-07 Fixed.** 41.9 "Deleted documents that are not gone".
- **PA-08 Fixed.** Example 40.3 uses `redis.call('TIME')`; Table 40.2 row "One clock" agrees.
- **PA-09 Fixed.** Serverless cache ruled out (cluster mode), sourced.
- **PA-10 Fixed.** 40.8: `409 Conflict`, audit event, semantic change stated; Ch 39.11 now defers to Ch 40.
- **PA-11 Fixed.** 40.8 upload budget: bounded parallelism, retry throttled requests, count check before the lock, timeouts.
- **PA-12 Fixed.** 40.10: overlapping runs, lease or lock, leader-lock pause; six methods listed.
- **PA-13 Fixed.** 41.2: SSE-KMS needs `kms:GenerateDataKey` and `kms:Decrypt` for the role and the key policy.
- **PA-14 Fixed.** Table 41.1 "Direct access" row (managed prefix list).
- **PA-15 Fixed.** `prevent_destroy`, RDS `deletion_protection` and final snapshot, plain-HTTP ALB hop and port-80 redirect (41.3, 41.7).
- **PA-16 Fixed.** Ch 37.13 and Step 6 point to Ch 40.5 readiness; Figure location corrected; Table 40.1 plan-step column matches 37.17; Ch 39.11 reconciled.
- **PA-17 Fixed with pointers.** Network stays in 41.3, with Fig 40.1 stating where it is defined; graceful shutdown stays in 41.6 with cross-references. Acceptable.
- **PA-18 Fixed.** Duplicate sentence removed; session-attribute backward compatibility added in 40.9 and referenced in 41.6; cross-AZ transfer in 41.10.

## Judgement of the new content

**Table 40.2 (alternatives): good.** It is short, fair, and ends with the book's own pick for the case at hand. Two small additions: say in the JDBC row that Spring Session JDBC also removes the `getAllPrincipals()` problem of 40.9 (JDBC supports lookup by principal name), which is a real point in its favor; and in the EFS row name the trade that matters most for this app (a network file system on the tile read path of every request).

**"What still takes you down" and "Recovery objectives": accurate and appropriately modest.** They correctly say series dependency, AZ not Region, and that recovery time is only what the drill measures. Suggest one more sentence: RDS Multi-AZ failover and ElastiCache failover both drop connections, and the app's reconnect behavior is untested, so measure it in the drill (the text says measure failover, not reconnect).

**Restore drill and counter rewind (41.5, Fig 41.3): mostly right, one conflation (NEW-2).** The janitor hazard and the boundary note are exactly right.

**Five Chapter 41 figures.**
- Fig 41.1 network: accurate (ALB public, tasks/RDS/cache private, security-group arrows, NAT optional, port 80 redirect). It shows the cache and RDS as single boxes; a note that they span both zones would match the Multi-AZ text.
- Fig 41.2 observability: accurate; the point that only readiness makes the platform act is the right one. Fine.
- Fig 41.3 restore drill: accurate for the drill; see NEW-2 on when the counter step applies.
- Fig 41.4 pipeline: accurate. Add that the circuit breaker rolls back the service but not the migration, which is why Ch 40.7 insists on backward-compatible migrations.
- Fig 41.5 migration order: flawed (NEW-1).

## New findings

### NEW-1 (Major) The migration order cannot be followed one move at a time
- **Where:** Ch 41, 41.11, Fig 41.5 and "The smallest useful first step"; Exercise 41.2.
- **Quote:** "Move 0 is the base: the one copy runs as a single Fargate task in a VPC ... Moves A to C then improve things with *one* instance still running"
- **Problem:** a Fargate task has only ephemeral storage (the chapter says 20 to 200 GiB, gone when the task is replaced) and cannot run the MySQL container's data volume durably. If move 0 (compute) comes before B (RDS) and C (S3), the database and tiles are on storage that vanishes at every deploy or task replacement. So 0, B and C are one cutover (or 0 must be done with an EFS volume as an interim, which Table 40.2 already names). The figure, the text and Exercise 41.2 present them as sequential improvements.
- **Suggested fix:** in 41.11 write: "Move 0 cannot stand alone: a Fargate task has only ephemeral storage, so do moves 0, B and C together (or mount an EFS volume for the interim)." Redraw Fig 41.5 with 0, B, C grouped, and adjust Exercise 41.2's solution.

### NEW-2 (Minor) The counter step belongs to real recovery more than to the drill as drawn
- **Where:** Ch 41, 41.5 and Fig 41.3.
- **Problem:** in the drill both stores are restored to a scratch bucket at the same point, so no prefix newer than the restored database exists and the counter collision does not occur; it occurs in real recovery, where the database is rewound and the live bucket keeps the newer prefixes. The runbook is right to include the step but does not say which case needs it. It also does not say what to do with the production janitor after a real restore (run the counter reconciliation before re-enabling scheduling; the janitor would otherwise treat the newer prefixes as orphans, which is correct for lost uploads but should be a decision).
- **Fix:** add "In a real recovery (database restored, live bucket kept) this step is mandatory; in the drill it is needed only if you restore the database to an earlier point than the bucket."

### NEW-3 (Minor) Two-container shutdown and the migration rollback
- **Where:** Ch 41, 41.6.
- **Problem:** the graceful-shutdown discussion covers Spring and ECS `stopTimeout` well but not nginx in the same task: on `SIGTERM` nginx may exit quickly and cut proxied uploads that Spring would have finished (the official image's graceful signal is `SIGQUIT`; unverified for `nginx-unprivileged`), and container stop order is unspecified. Separately, the circuit-breaker rollback does not undo the Flyway migration.
- **Fix:** one sentence each: set nginx's stop signal or container dependency so it outlives the app during drain (verify for your image), and "a rollback restores old code onto the new schema, which is why migrations must be backward compatible".

### NEW-4 (Minor) Blueprint figures and the `Fig` sizes: see SA-02
Listed as open under SA-02.

### NEW-5 (Minor) Part IV introduction PR count: see SA-14.

## Length: does it hurt?

Ch 40 is now about 10,600 words including code (about 9,500 prose) and Ch 41 about 8,000 including code (about 6,500 prose), against a Part IV ceiling of 10,000 and a general 4,000 to 8,000. The added material is the right material: every addition answers a review finding, and the design is honest and safer for it. The cost is reader fatigue in the two Advanced-tier sections that matter most (40.8, 40.9: about 1,300 and 1,400 words) and in a 1,000-word 40.5 and 40.6. Nothing needs deleting for correctness. What I would cut or move, in order of value:
1. **40.9 sessions paragraph (about 600 words):** keyspace notifications, `ConfigureRedisAction`, cookie checks, expiry events and serialization detail belong in Table 40.3-style bullets or a "Reference" box; keep the `getAllPrincipals()` finding and the atomicity discussion in the text.
2. **40.6 (about 1,000 words):** keep the ALB header behavior, the last-address rule and Example 40.1; move the `map`-key mechanics and cookie-attribute checks into a table.
3. **41.8 CloudFront:** the optional-edge section plus Table 41.1 (eight rows) is long for a service the chapter recommends not using for tiles; shorten to the one-origin rule and three rows.
4. **41.7 Terraform:** the health-check default mismatch note and provider trivia can go; keep the bucket and lifecycle excerpt.
5. **Sources lists (35 and 20 entries) and the inline "(source N)" tags** are correct fixes for the stripped-comment problem, but make the prose harder to scan; consider putting the tags only on figures/limits a reader might act on.
A 12 to 15 percent trim (about 1,300 words in Ch 40, 800 in Ch 41) would put both chapters at the ceiling without losing a finding.

## Open list (after recheck)

| Severity | Count | Items |
|---|---|---|
| Blocker | 0 | |
| Major | 2 | SA-02 (blueprint diagrams too small in the PDF), NEW-1 (migration order not executable; move 0 needs B and C) |
| Minor | 7 | SA-10 (caption), SA-14 (Part IV intro PR count), SA-17 ("nearly every"), NEW-2, NEW-3, Fig 41.1/41.4 small notes, half-blank page 560 |

## Not checked

- AWS behavior claims (stopTimeout, keep-alive, EFS/JDBC statements, S3 PITR windows): not re-read against AWS documentation; I judged only internal consistency. nginx stop signal is from memory (unverified).
- Whether Ch 7 mentions PR #13 (line 273 is one very long line I did not read).
- Solutions files and Appendix C regeneration (the progress note says Appendix C still had Figure 41.1 in Exercise 40.1's solution until regenerated; not verified in the PDF).
- EPUB and HTML outputs.
