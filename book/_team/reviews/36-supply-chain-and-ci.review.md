# Review: 36-supply-chain-and-ci.md (window 4, LIGHT PASS)

Scope: automated listing check (all listings match their tags) plus a targeted check of version and history claims. The Dependabot story (PRs #6, #7, #8 closed and replaced by #10; then PRs #11 and #12) matches dossier decisions D13, bugs C8 and timeline. Versions quoted (Node 24, Boot 4.1.1, MySQL 8.4, Vitest 5 only at m6) are consistent with the repo. Prose, terms and other exercises not reviewed.

1. **major** - Exercise (near line 127) states "Angular 23 requires TypeScript 6.1". Angular 23 does not exist in the repo or dossier; the project is on Angular 22 (peer range `>=6.0 <6.1` per dossier C8). This is a hypothetical presented as fact. Fix: reword as a hypothetical ("Suppose a future Angular release supports TypeScript 6.1 ...") and mirror the fix in `36-supply-chain-and-ci.solutions.md` (line 13 mentions Angular 23 too).
2. **minor** - Line 88 "proposed MySQL 26.7 (PR #6)" is right per the dossier, but a beginner will not know why 8.4 to 26.7 is odd; add "(a different release line, not a patch of 8.4)" only if a source states it; otherwise leave as the dossier words ("mysql 8.4 -> 26.7").
