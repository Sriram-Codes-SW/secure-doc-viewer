# Review: part-5-production/00-part-introduction.md (window 9, full read)

Verified: the five chapters and their order (32 to 36); Chapter 37 is outside Part V (it now lives in `book/tradeoffs/`) and the intro says why; Part V quotes `book-m5-platform` and `book-m6-final`, and the two tags share the same compose file, nginx configuration and Caddyfile (checked: the files differ only where Chapter 36 says they do); the incident summaries (a proxy that appended to a forged header, the janitor refusing to delete when the current version is missing, three automatic Dependabot proposals declined) match Chapters 32, 34 and 36 and the dossier (TM2-1, F1, PRs 6 to 8). The AI-reviewer note is correct and complete. No secrets. No blockers, no majors.

## Findings

1. **minor** - "the hardening from five rounds of review arrived" at `book-m5-platform`: Chapter 32 tells Round 1, Round 2, Round 3 and "later rounds" (four headings), and Chapter 30 says "review rounds" without a count. The dossier does not give a number I could confirm. Say "several rounds" or add the count where Chapter 32 counts them.
2. **minor** - "Chapters 32 to 34 describe findings from two review roles" is true; Chapter 30 and 31 also do (Part IV). Say "Chapters 30 to 34".
3. **minor** - "Try the commands. Chapters 33, 34 and 35 are written so that you can run the stack": the run instructions for the full stack are in Chapter 33 and Table IV.3 in the Part IV introduction; add a pointer to Table IV.3 (`--profile full`) and a one-line reminder that these chapters need Docker and a `.env` file.
4. **minor** - Table caption format (`*Table 1 — ...*` below the table): see the Part II introduction review.
5. **minor** - "It sits outside Part V on purpose" for Chapter 37: the reader also meets Part VI (Chapters 38 and 39) after it, which this intro does not mention; add "and Part VI gives the names of the patterns behind these choices".
