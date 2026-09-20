# Final technical review (after the proofreading pass)

Reviewer: final technical reviewer. Branch `book/draft`, diff base `bb46933..HEAD`. Read-only review; no chapter edited, no build run.

## Verdict

PUBLISH on the technical content. No Blocker and no Major finding. Five Minor findings, all optional polish. The proofreading pass (about 800 non-trivial word-level changes across 82 files) did not damage any listing, code token, number, cross-reference or attribution that I could find.

| Severity | Count |
|---|---|
| Blocker | 0 |
| Major | 0 |
| Minor | 5 |

## How this was checked

1. Structural drift scan (own script, `scratchpad/codedrift.py`): compared every changed hunk between `bb46933` and HEAD against fenced blocks and inline code. Result: no change inside any non-diagram code listing; the only fence-level changes are the restructured Mermaid diagrams (top-to-bottom layouts, shorter node labels, new Prometheus and Actuator nodes) and one Markdown re-indent in Ch 9 Section 9.3 (the two `bash` blocks are now indented under list items 2 and 3; valid). The only inline-code changes are added tokens (tag names in "In this project", `401`, `.env`, `stopTimeout`, `denyAll`, and so on), none altered.
2. Word-level scan of all 1,995 change pairs; the 803 non-punctuation ones were read (glossary and index by sampling). Number-changing pairs were all checked. Word-list replacements ("login" to "sign-in", "logout", "front end") appear only in prose; the two prose spots (`25:557` "logging" to "signing" out, `30:82` "front end" to "proxy") are correct. "recognised" to "recognized" touched no code or quoted repository text (the KnownDevices Javadoc, which uses the British spelling, is not quoted in a listing).
3. Checkers: `listingcheck.py` (292 listings verified, 13 flagged, exactly the 13 known: 10.6, 10.7, 13.2, 15.1, 18.2, 18.4, 26.13, 28.4, 28.6, 31.1, 34.1, 34.2, 36.4; nothing new). `refcheck.py`: 0 broken references (the 2 hits are in STYLE.md). `xref.py`: 0. `figcheck3.py`: 78 captioned figures, every one has a text description; sequence problem only for the Part II opener figure label "II.1" (a part-level figure, harmless). `gen_appendix_c.py` regenerated to the scratchpad and is byte-identical (ignoring CR) to the committed Appendix C; all 242 exercises exist, every non-three-star exercise has a solution heading in Appendix C, and the Ch 39 exercises now carry "Solution:" pointers that match their headings.
4. Three new listings, compared line by line with the tag:
   - 28.9 (`upload.component.ts` at `book-m3-hardening`): matches lines 70-78 exactly apart from indentation; caption says excerpt and adds the Path line. OK.
   - 29.8 (`TileController.java` at `book-m4-reading`): the two lines match source lines 125-126. OK.
   - 30.11 (`SignedTilePayload.canonicalString` at `book-m5-platform`): the return line matches; path `model/SignedTilePayload.java` is correct. OK.
   Listing renumbering (28.10, 28.11, 29.9, 29.10, 30.12) is consistent with all in-text references (28.9, 28.10, 29.8, 30.11, 30.12).
5. Technical claims spot-checked against the tag: idle spec rows in Table 29.1 (10, 26, 30 minutes plus the scaled test) match `idle.spec.ts`; six `@Scheduled` methods in Ch 37 Step 4 match `book-m6-final` (`TileRateLimiter` uses the fully qualified annotation); "TileController has eleven collaborators" (Appendix C 39.2) is exactly 11 `private final` fields; `DocumentController` depends on `DocumentService` and `RequestActors` only, `UserDirectoryController` on `AppUserRepository` only; recognized-device glossary entry (30 days, keyed hash) matches `KnownDevices`; `Sign-in required.` message exists at m6; the 33-minute versus 2.4-hour copy estimate in Ch 30 is consistent with the arithmetic (6,000 tiles at 180 per minute; 17,500 at 120 per minute) and the "six times more pixels" ratio; grid example in Ch 19 (2 by 3 tiles, two full-size) is right; pull request 13 exists and was merged (Ch 7, Part IV opener consistent); the Ch 39 "twelve patterns in Sections 39.4 to 39.15" is right; Section 36.5/36.7 references were correctly moved to 36.6/36.8; Ch 30 `.env` rule now points to Section 26.13 which contains it.
6. Reviewer renaming: every "AI product-owner reviewer / PO reviewer / TM reviewer / project owner" sentence I checked attributes decisions correctly. Human decisions (built-in accounts, MySQL over H2, sign-off on the rate limit and tile size, platform versions, Dependabot LTS rule, the ultrareview drop) are all attributed to the project owner; findings are attributed to AI reviewers. Ch 32.4 and the Part V opener still state plainly that the reviewers were AI agents and the project owner is a person.
7. PDF sample (825 pages, tagged): rendered 20 pages and read 16 of them (pp. 15 not viewed, others viewed): 120 (Ch 7), 235 (Ch 15), 257 (Table 16.2), 300 (Ch 18 exercises), 400 (Ch 24), 481 (Ch 28), 495 (Ch 29), 520 (Ch 30), 560 (Ch 32 exercises), 620 (Ch 37), 675 (Ch 39), 706 (Ch 41), 740 (glossary), 808 (Appendix C), 815 (Appendix D), 822 (index). Listing captions, Path lines, Example labels ("teaching example, not repository code"), continuation marks on wrapped code, star labels, "Solution:" pointers, "Where the analogy breaks down:" paragraphs and the glossary/index entries printed as in the sources. Numbering matched the sources on every page viewed. I did not extract text from the PDF beyond page location searches.
8. Part VII: Ch 40 and Ch 41 open with "this is a design, not a deployment" and say the project never ran on AWS; no sentence claims otherwise. Source lists are complete and contiguous (35 in Ch 40, 20 in Ch 41), every cited number has an entry, and first-citation order holds (the two apparent inversions are the "such as (source N)" examples in the reading note, which is fine). Spot checks of claims against general AWS documentation knowledge: ECS `stopTimeout` default 30 s and maximum 120 s on Fargate; Spring Boot graceful shutdown 30 s default; S3 returning 403 instead of 404 without `s3:ListBucket`; gateway endpoints for S3 carrying no extra charge; AWS Backup continuous PITR for RDS (5 minutes) and S3 (15 minutes). All consistent; I did not re-fetch the documentation (no network review requested).
9. Old open items: Table 14.2 now says "three of the six" and lists six sweeps (fixed); no hits for "five gates later", "Section 38.12", "Listing 21.6a"; chapter headers show `status: expanded` (Ch 32 checked).

## Minor findings

### FT-01 (Minor) Ch 40 pitfalls list: a wording change reversed the reading
- File: `book/part-7-cloud/40-aws-production.md`, line 485.
- Before: "limits that multiply with the number of tasks; unsharing stops working on open pages, or watermarks that vanish."
- After: "limits that multiply with the number of tasks; revoked shares that stop working on open pages, or watermarks that vanish."
- Problem: the symptom is that revocation does not take effect (a cached tile keeps working). "Revoked shares that stop working" reads as the opposite (the shares stop working, which is the desired outcome).
- Fix: "revocation that no longer takes effect on pages already open, or watermarks that vanish."

### FT-02 (Minor) Epilogue overclaims slightly about the interceptor
- File: `book/appendices/epilogue.md`, line 25-26.
- After: "the interceptor is where a `401` from any call is handled in one place".
- Problem: at `book-m6-final` the interceptor skips the two auth probes (`/api/auth/me`, `/api/auth/login`), and tile fetches use plain `fetch()` that handle their own 401s (see the file's own comment). The prior wording (cookies and tokens) was worse; this is now nearly right.
- Fix: "where a `401` from the API's ordinary calls is handled in one place".

### FT-03 (Minor) Ch 32 Section 32.4: ambiguous sentence structure
- File: `book/part-5-production/32-security-review.md`, lines 84-85.
- After: "They read the code and wrote numbered findings, and the project owner, a human, made the product calls. They were not human colleagues."
- Problem: "They" in the second sentence can be misread as covering the project owner. The meaning is right but the join is fragile after the rename.
- Fix: "The two AI reviewers read the code and wrote numbered findings; the project owner, a human, made the product calls. The reviewers were not human colleagues."

### FT-04 (Minor) Reviewer naming is inconsistent before Part IV
- Files: `part-1-foundations/01-the-big-picture.md:233`, `02-command-line-and-files.md:318`, `04-classes-and-objects.md:525`, `05-collections-and-exceptions.md:462`, `06-maven-and-project-layout.md:303`, `08-how-the-web-works.md:493`, `10-docker-and-compose.md:393`, `22-http-client-and-services.md:410`.
- Problem: these use older phrasings ("technical-manager review (an AI review agent)", "threat-modeling reviewer", "a 'product owner' reviewer"), while the Preface, Part IV and later chapters use "AI technical-manager reviewer / TM reviewer". Nothing is false (each says AI, none names a person), but the "threat-modeling reviewer" label in Ch 6 and Ch 10 is the same role as the TM reviewer and could be taken as a third reviewer.
- Fix: in Ch 6:303 and Ch 10:393 replace "threat-modeling reviewer/review" with "the AI technical-manager reviewer".

### FT-03b (Minor, layout note) Table 16.2 first column
- PDF p. 257 (printed page 238): the narrow first column hyphenates "ac-count+ip" and "account-wide" awkwardly. Not a technical error; suggest widening the first column or writing the rule names as `account+ip` in code style so they do not hyphenate.

(Count above treats this as the fifth Minor.)

## Checked and found correct (brief)

Listings 28.9, 29.8, 30.11 and all 292 verified listings; Appendix C generation; cross-references and renumbered listing/section targets; reviewer-role attribution; idle-timeout table; scheduled-jobs count; controller dependency claims; the rate-limit and tile-size arithmetic; PR 13 mention; Ch 40/41 "design, not deployment"; Part VII sources numbering; new "Solution:" pointers in Ch 39.

## Not checked

Full re-audit of every Part VII claim against the current AWS documentation (no network); exhaustive read of all 803 word changes in the glossary and index (sampled); text extraction of the whole PDF (only 16 pages viewed, page locations searched for the new listings, figures and exercises); EPUB and HTML outputs; running any exercise; accessibility (other reviewers).
