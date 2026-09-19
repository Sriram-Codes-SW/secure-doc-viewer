<!-- chapter: 37 | part: trade-offs | owner: writer-production | tag: book-m6-final | status: draft -->
# Chapter 37: The engineering trade-offs

Tag: `book-m6-final`. Prerequisites: Chapters 15, 16, 25 to 31, and 32 to 36. Terms such as CDN (a network of servers that delivers files from near the reader), Redis (an in-memory data store shared between servers), and presigned URL (a temporary signed link to a stored file) are glossed where they first appear or in the chapters named.

## Learning objectives

By the end of this chapter you can:

- explain, for each major decision in this app, what was chosen, what it bought, and what it cost;
- say which limits you will hit first as the app grows, and in what order;
- name the enterprise alternative to each choice and the trigger that would justify the move;
- separate a decision the project actually made and recorded from an opinion about it.

## How to read this chapter

A trade-off is a decision where getting one good thing means giving up another. Each choice below was
reasonable for a small, single-server app with one team, and
each has a point where it stops being reasonable.

Every decision below uses the same six headings: **The decision**, **What the project chose**,
**Pros**, **Cons**, **The enterprise alternative**, and **When you'd switch**. Statements about
what happened cite the README, a pull request (PR), or a commit. Where the record shows the
outcome but not the reasoning, the text says so and marks the reasoning as the book's reading.

## 37.1 Server-side tiles vs. sending the PDF

<!-- source: README "Why this design"; commit b6aef4e; dossier/decisions.md D4 -->

**The decision.** How do you show a document to someone without handing them the file?

**What the project chose.** The server rasterizes each page, slices it into 512-pixel PNG tiles,
and, in the README's words, the PDF "stops existing as a servable file after ingest". Only disconnected tiles remain, and no endpoint returns a
page or document (README, "Why this design"; the ingest code deletes the staged source PDF, `TileGenerationService`).
The browser paints the tiles as absolutely positioned elements with CSS background images built from `blob:` URLs (Chapter 21). The reasoning at MVP time survives only in the
commit message of `b6aef4e`.

**Pros.**
- The protection lives on the server, where the client cannot reach it. Hiding a download button in the browser is undone with DevTools in seconds.
- There is no file to save, so "Save the PDF" has nothing to save.
- Each tile passes through checks (signature, session, access, rate limit), so every piece of the document is individually controlled.

**Cons.**
- Rendering costs CPU at upload and per tile request; the project needed a render pool, a timeout, and limits on pages and pixels (Chapter 32).
- Pages are images, so there is no text layer: screen readers get nothing, and users cannot search or copy text (README, Limitations).
- It cannot stop screenshots or photographs, and a patient user with a valid session can fetch every tile.

**The enterprise alternative.** The README says this is "the architecture commercial e-magazine and flipbook readers use". Beyond that the project recorded no alternative. As general industry practice, not something the project recorded: a stricter requirement is often met with a digital rights management (DRM) product, and accessibility with a text layer served under policy.

**When you'd switch.** When accessibility is a hard requirement (add a controlled text layer), or when the content's value justifies a DRM vendor's cost.

## 37.2 Watermark at request time vs. at ingest

**The decision.** When do you stamp the viewer's identity on a tile?

**What the project chose.** At request time, when each tile is served (README, "Watermarking happens on the way out"; original design `b6aef4e`).

**Pros.**
- One stored tile serves every viewer. Stamping at ingest would give one identical, un-attributable copy for everyone; pre-stamping per user would store N copies of every tile.
- Every response is individually traceable: it carries the viewer, a UTC timestamp, and a trace code that leads to the exact sign-in in the audit log.

**Cons.**
- A decode, draw, and PNG encode on every tile request. The reviewer recorded this as a documented limitation on a single node.
- Tile responses must be `Cache-Control: no-store`, because a shared cache holding a tile stamped for someone else would leak it. That gives up shared caching.

**The enterprise alternative.** Watermark at a coarser granularity, or cache per (tile, viewer) with a short lifetime (both named in the README's Limitations). Anything beyond those two options is general industry practice, not something the project recorded.

**When you'd switch.** When tile CPU becomes the bottleneck. The metric that shows it first is `sdv_tiles_served_total` next to CPU use and `503` responses from the tile work cap.

## 37.3 Watermark strength: comfort vs. deterrence

**The decision.** How visible should the mark be?

**What the project chose.** Red at 20% opacity in a brick pattern with gaps of 1.5 times the text height. The default opacity was lowered from 0.28 in PR #4, because dense pages (code, tables) were hard to read through a heavier mark. The README calls it "a product decision, not a default nobody chose", and both values are configurable (`watermark-opacity` up to 0.6, `watermark-spacing` down to 0.5).

**Pros.**
- Readers can read the page. The goal is attribution, and a legible trace code achieves that.
- It can't be switched off by the reader, because it is burned into the pixels on the server.
- No code change is needed to turn it up for a sensitive deployment.

**Cons.**
- A light mark deters less than a heavy one, and a determined person can crop or retouch a fragment.
- The pattern is laid out per tile, so copies do not line up across tile boundaries (commit `f468678` states this exactly in the README).

**The enterprise alternative.** The README's own lever is turning the mark up (`watermark-opacity`, `watermark-spacing`). General industry practice, not recorded by the project: adding an invisible forensic mark alongside the visible one.

**When you'd switch.** When leaks happen and the visible trace code is not enough evidence, or when different documents need different strength (see 37.8).

## 37.4 App-issued HMAC tokens vs. cloud-signed URLs

<!-- source: README "Why this design", Limitations; commit f682716; dossier/decisions.md D8 -->

**The decision.** Who signs the URL that lets a browser fetch a tile?

**What the project chose.** The app signs it with HMAC-SHA256 over document, page, row, column, render version, session binding, and expiry, with a 120-second lifetime. `SignedUrlService` deliberately mirrors the presigned-URL pattern (README, Limitations). The render version was added to the signed payload after a probe found old URLs silently serving the new render (`f682716`).

**Pros.**
- No external service: the whole check is one class you can read and test.
- Session binding: a URL pasted into another browser or account gets `401`.
- Checking the session on every tile request means signing out kills every outstanding URL at once.

**Cons.**
- Every tile passes through the application server, so the app's CPU and network carry all the traffic. A cloud signed URL would let a CDN serve the bytes.
- One shared secret (`SIGNING_SECRET`) protects everything; outstanding URLs are signed with it, so changing it invalidates them. The README also warns that restoring a backup under a different secret still works, but every account's recognised devices are forgotten, because their hashes are keyed by it; so keep `.env` with the backup.

**The enterprise alternative.** Object storage plus a CDN, with CloudFront signed URLs or S3 presigned URLs, so the edge serves the bytes and the app only signs. The README lists this as the production shape.

**When you'd switch.** When bandwidth, not logic, is the cost. Note the catch: at the edge, per-request watermarking and per-request session checks become harder, so the switch changes 37.2 as well.

## 37.5 In-memory sessions and counters vs. a shared store

**The decision.** Where do sessions and rate-limit counters live?

**What the project chose.** In the memory of the one app instance. Accounts, documents, shares, and the audit trail are in MySQL; sessions and the throttle counters are not. The README states this and lists Spring Session with Redis as a next step.

**Pros.**
- Nothing extra to run, secure, or back up.
- Fast, and simple to test.
- Sessions are server-side, so an administrator can list and revoke them and a role change ends the user's sessions (PR #1).

**Cons.**
- A restart signs everyone out and resets throttle counters.
- With two instances, each would have its own sessions and its own counters, so limits would multiply and a user could land on an instance that does not know them.

**The enterprise alternative.** A shared session store such as Redis through Spring Session, and rate limiting counters held in the same store, so limits hold across instances.

**When you'd switch.** The moment you need a second instance, for capacity or for zero-downtime deploys. See 37.11.

## 37.6 Built-in authentication vs. an identity provider

<!-- source: PR #1 body; README Limitations; build transcript (the user's choice of built-in accounts) -->
**The decision.** Who owns accounts and passwords?

**What the project chose.** Built-in accounts: BCrypt passwords in MySQL, roles READER, PUBLISHER, and ADMIN, created by an administrator with no self-signup. Both reviewers had suggested an identity provider (OIDC or SSO); the product owner chose built-in accounts when asked (PR #1). The recorded outcome is sourced; the reasoning is the book's reading: a self-contained app with no external service to depend on.

**Pros.**
- Nothing outside the stack to set up, and full control of the rules: three lockout counters, recognised devices, forced first-password change, and an unlock action.
- Sessions live in an httpOnly cookie on the server, so the session id is never visible to JavaScript. The project's record shows this choice was made in PR #1 with no recorded comparison against browser-held tokens, so this book states the outcome only.

**Cons.**
- The app now owns password storage, lockout, and reset. Reviews found real defects here (the lockout that let anyone lock out any user; the 72-byte BCrypt limit).
- No MFA (multi-factor authentication: a second proof of identity beyond a password), including for admins (README, Limitations). Accounts also exist only inside this app, so there is no single sign-on (SSO: one company login that works across many apps).
- Every new user needs an administrator.

**The enterprise alternative.** An identity provider through OIDC (OpenID Connect, a standard for signing in through a separate identity service), which is how the reviewers suggested fixing sign-in and which typically supplies single sign-on (SSO) and multi-factor authentication (MFA). Naming specific vendors is beyond what the project recorded. The app would then take identity from the verified principal and keep only roles and ownership.

**When you'd switch.** When people already have company accounts, when MFA becomes a requirement, or when administrators cannot keep up with accounts.

## 37.7 Local disk tiles vs. object storage

<!-- source: README "Backup and restore", Limitations; commits cd0f5c2, 66f7152; dossier/decisions.md D8 -->

**The decision.** Where do the tiles live?

**What the project chose.** On local disk, in a Docker volume, laid out as `{docId}/v{version}/page-{n}/tile-{row}_{col}.png`. A `StorageJanitor` removes folders no document points to; replacing a PDF writes a new version folder and switches under a row lock (README).

**Pros.**
- No extra service; reading a tile is a file read.
- Versioned folders make replacement atomic for readers (no mixed old and new pages).

**Cons.**
- Backups are coupled: the database and the tile volume must be captured at the same moment, so the runbook stops the app during a backup. A dump taken before a replace plus an archive taken after would point documents at deleted tiles (commit `66f7152`).
- Storage is tied to one machine; a second instance cannot see it.
- The developer setup warns against synced folders (OneDrive, Dropbox) because they lock files (PR #2).

**The enterprise alternative.** Object storage (S3 or equivalent) with versioned object keys, served through a CDN with signed URLs, and storage-level versioning for backups.

**When you'd switch.** When you need more than one instance (37.11), when the volume outgrows one disk, or when backups need to run without stopping the app.

## 37.8 Per-user rate limits vs. per-document sensitivity

<!-- source: README Limitations; commits a51674c, 51ea941; dossier/decisions.md D6 -->

**The decision.** How fast may a viewer pull tiles, and is the same limit right for every document?

**What the project chose.** One per-user limit: 180 tile requests per 60-second window with 512-pixel tiles, about 15 pages a minute. The history: the limit and tile size moved from 256 pixels and 120 a minute to 512 and 180 after readers saw blank pages (raised by the AI product-owner review agent; see Chapter 32). The reviewer noted that a 500-page harvest then takes about 33 minutes instead of about 2.4 hours. The product owner accepted this on September 19, 2026 (commit `51ea941`) and asked how sensitive documents could differ; per-document sensitivity levels are listed as a possible follow-up.

**Pros.**
- Reading feels normal, and bulk harvesting is slow and boundable instead of instant.
- One number to explain, monitor (`sdv_tiles_rate_limited_total`), and tune.

**Cons.**
- The limit bounds speed, not possibility. A scripted harvest still succeeds, only slower.
- It treats a public brochure and a confidential contract the same.
- Counters are in memory (37.5).

**The enterprise alternative.** The README names two follow-ups: "per-document sensitivity levels with tighter limits" (Limitations) and logging or alerting on "every tile-urls page fetched back-to-back" rather than only a flat per-minute cap (Possible next steps).

**When you'd switch.** When one deployment holds documents of very different value, or when the rate-limited counter shows readers are being hurt.

## 37.9 MySQL and Flyway vs. alternatives

**The decision.** Which database, and how does its structure change over time?

**What the project chose.** MySQL 8.4 in Docker, with schema changes as Flyway migrations. The product owner chose MySQL over H2 and Postgres. Unit tests use H2 in MySQL mode; `MySqlIntegrationTest` runs on real MySQL 8.4 through Testcontainers because the technical-manager review agent asked for it (Chapter 32). Dependabot is set to stay on the 8.4 LTS line (PR #10).

**Pros.**
- A real server database from the start, with row locks that the PDF-replace design depends on.
- Migrations are versioned files in the repository, so every environment reaches the same schema.
- The real-database test found the class of problem H2 hides: timestamps are stored as UTC and verified with the server and the JVM each set to a different non-UTC zone.

**Cons.**
- H2 in MySQL mode is only similar to MySQL; two test suites are needed to cover the gap.
- A major upgrade (8.4 to the next LTS) is a deliberate project with its own migration test.
- One database server is a single point of failure until you add replication.

**The enterprise alternative.** A managed database (RDS, Cloud SQL) with automated backups, point-in-time recovery, and replicas. The project considered H2 and Postgres and chose MySQL; the record does not say why Postgres lost.

**When you'd switch.** When you can't afford the restore time of a dump (move to a managed service with point-in-time recovery), or when the team's skills favor another database.

## 37.10 Client-side blocking as friction only

**The decision.** Should the browser block right-click and drag?

**What the project chose.** Yes, on purpose. The Angular viewer blocks the context menu and native drag and paints tiles as CSS backgrounds. The README opens by dismissing exactly this trick, then says it was added anyway as a deliberate, acknowledged trade-off that provides zero extra protection.

**Pros.**
- It stops the casual "right-click, save image" path a non-technical user takes without thinking.
- It costs almost nothing.

**Cons.**
- It stops nothing for anyone who opens DevTools or calls the API directly.
- The risk is psychological: someone may mistake it for a control. The README says it must never be mistaken for one.

**The enterprise alternative.** The same friction in commercial readers, sitting on top of server-side controls, never in place of them.

**When you'd switch.** It was never meant as a control. Remove it if it harms usability, for example for assistive technology users.

## 37.11 One instance vs. scale-out

**The decision.** How many copies of the app run?

**What the project chose.** One. The go-live checklist says so, and the Limitations section explains why: sessions and throttle counters are in memory and tiles are on local disk.

**Pros.**
- The simplest deployment, and every limit, counter, and log lives in one place.
- The compose file (MySQL, app, nginx, optional Caddy) is small enough to read in one sitting.

**Cons.**
- No zero-downtime deploys; the app is a single point of failure.
- The backup runbook stops the app for its duration.
- Capacity means a bigger machine, not more machines.

**The enterprise alternative.** Several stateless instances behind a load balancer, with shared sessions (Redis), shared tile storage (S3 and a CDN), a managed database, The README names shared sessions (Spring Session and Redis) and shared tile storage as the requirements. Container orchestration and a cloud load balancer in place of nginx and Caddy are general industry practice, not something the project recorded.

**When you'd switch.** When you need availability that one machine can't give, or capacity beyond a bigger machine. Do 37.5 and 37.7 first; scaling out before them does not work.

## 37.12 Decision table

**Table 37.1 — The decisions at a glance**

| Decision | Project chose | First limit you'll hit | Enterprise alternative |
|---|---|---|---|
| 37.1 Delivery | Tiles, no PDF | No text layer; CPU for rendering | Controlled text layer, DRM |
| 37.2 Watermark timing | At request time | CPU per tile; no shared cache | Coarser or cached per (tile, viewer) |
| 37.3 Watermark strength | 20% opacity, configurable | Weak deterrence | Visible plus forensic marks |
| 37.4 URL signing | App-issued HMAC | App carries all traffic | CDN signed URLs |
| 37.5 Sessions | In memory | Restarts, one instance | Redis session store |
| 37.6 Authentication | Built-in accounts | No MFA or SSO | OIDC identity provider |
| 37.7 Tile storage | Local disk volume | One machine; coupled backups | S3 and CDN |
| 37.8 Rate limit | One per-user limit | Same rule for all documents | Sensitivity levels, anomaly detection |
| 37.9 Database | MySQL 8.4 and Flyway | Single server | Managed database with replicas |
| 37.10 Client blocking | Right-click blocked | Bypassed with DevTools | Friction only, on top of controls |
| 37.11 Instances | One | Availability and capacity | Load-balanced stateless instances |

## Try it

### Exercise 37.1 ★ Where is the choice?

Pick three rows of Table 37.1. For each, name the file or README section that shows the project's choice.
### Exercise 37.2 ★★ Why S3 forces a rethink

Explain why moving tiles to S3 (37.7) forces you to revisit per-request watermarking (37.2) and session checks (37.4).
### Exercise 37.3 ★★ Order of change

Order the decisions in the sequence you would change them for a first scale-out, and defend the order.
### Exercise 37.4 ★★★ A switch trigger as an alert

Choose one decision and write the "When you'd switch" trigger as a measurable alert, using metrics from Chapter 35.

## Summary

- Each choice in this app bought speed of building and simplicity, and each has a named limit.
- Several limits are linked: sessions, counters, and tiles must all become shared before a second instance is safe.
- The project recorded some reasoning and only outcomes for others; this chapter says which.
- Friction that is not a control is acceptable only when it is labeled honestly.

## Further reading

- Spring Session reference: https://docs.spring.io/spring-session/reference/
- Spring Security reference: https://docs.spring.io/spring-security/reference/
- Amazon S3, presigned URLs: https://docs.aws.amazon.com/AmazonS3/latest/userguide/using-presigned-url.html
- Amazon CloudFront, signed URLs: https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/private-content-signed-urls.html
- Flyway documentation: https://documentation.red-gate.com/flyway
- MySQL 8.4 reference manual: https://dev.mysql.com/doc/refman/8.4/en/
- OpenID Connect Core specification: https://openid.net/specs/openid-connect-core-1_0.html
