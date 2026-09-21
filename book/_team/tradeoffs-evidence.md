# Trade-off decisions: evidence list (working file for Chapter 37)

Sources: README.md (R), PR bodies (PR#), commits. Tags: m0..m6 = book-m0-mvp..book-m6-final.
Outline sections 37.1-37.12 are fixed; extra decisions are listed as candidates (request to editor if added).

| # | Decision | Evidence | Tag |
|---|---|---|---|
| 37.1 | Tiles + signed URLs vs. streaming the PDF | R "Why this design": PDF stops existing after ingest; PNG tiles only; PR3 deletes source PDF before commit | m0 |
| 37.2 | Watermark at request time vs. at ingest | R "Watermarking happens on the way out": one stored tile serves all; cost CPU, `Cache-Control: no-store`; R Limitations (coarser granularity / per-(tile,viewer) short-TTL cache) | m0, m4 (PR4 4c) |
| 37.3 | Watermark strength: comfort vs deterrence | R: red, 0.2 opacity (was 0.28, PR4), 1.5x gaps; `watermark-opacity` up to 0.6, `watermark-spacing` down to 0.5; trace code (Crockford Base32 after PR4 misread of I/l); commit f7beda2 per-tile layout | m4 |
| 37.4 | App-issued HMAC tokens vs cloud-signed URLs | R: HMAC-SHA256 over doc+page+row+col+render version+session binding+expiry; TTL 120s; R Limitations: mirrors presigned URL pattern -> CloudFront | m0, m1 (PR1 TM-4), m5 (versioned tokens 7484f4f) |
| 37.5 | In-memory sessions/counters vs shared store | R Limitations, "failure counters live in memory in each instance"; Next steps Spring Session + Redis | m1, m6 |
| 37.6 | Built-in auth (BCrypt, admin-created, no MFA) vs IdP | PR1 (TM-2, PO-3); R: no self-signup, no MFA, 72-byte limit (708fd8c), lockout rules, forced pw change (672907d) | m1 |
| 37.7 | Local disk vs S3 + CDN | R Limitations; StorageJanitor (PR2); atomic replace via versioned tiles (00e0619); backup must capture DB+volume together | m2, m5 |
| 37.8 | Per-user rate limit vs per-document sensitivity | R: 180/60s, ~15 pages/min, product-owner decision 2026-09-19 (607182e); follow-up sensitivity levels | m0, m1 (PR1 per-user), m6 |
| 37.9 | MySQL + Flyway (JPA) vs alternatives | PR1/PR2 Flyway V2; MySQL 8.4 LTS pinned (PR10 ignores 26.7); MySqlIntegrationTest UTC + row lock replace | m2, m5 |
| 37.10 | Client-side right-click blocking as friction | R "The client also blocks right-click - on purpose" | m4/m6 |
| 37.11 | One instance vs scale-out | R Go-live "One app instance"; Limitations | m6 |
| cand | HTTP session cookie vs JWT | PR1: httpOnly SameSite=Strict cookie + CSRF, 30-min idle; server-side revocation (admin revoke), role change ends sessions; tile binding to session | m1 |
| cand | Angular vs React | Needs dossier/conversation evidence; NOT yet verified | - |
| cand | nginx + Caddy vs cloud LB | R HTTPS/Trust boundary; nginx unprivileged non-root, Caddy TLS+HSTS profile (c40375a) | m5 |
| cand | Spring Boot 3.3.4 -> 4.1.1 / Java 21 -> 25 | PR5 (08f3879) | m5 |
| cand | Stop app for consistent backup vs online snapshot | R Backup; commit bbca423 | m5 |
| cand | Sync upload rendering vs background jobs | PR3 "Deferred: background processing" ; render pool/timeout (ultrareview) | m3, m5 |
| cand | Dependabot LTS-only | PR10, closed PR6/7/8 | m6 |
| cand | Audit in own transaction (REQUIRES_NEW) | PR2 | m2 |

Open: verify Angular-vs-React and JWT rationale in dossier/conversation before writing; PR5, PR8, PR11, PR12 bodies not yet read.
