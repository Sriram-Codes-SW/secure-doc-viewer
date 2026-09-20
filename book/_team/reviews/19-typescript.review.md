# Review: 19-typescript.md (window 8, expanded text; replaces the window-2 partial-draft review, whose majors are all RESOLVED)

Scope and method: I read the header, objectives, prerequisites and Sections 19.1 to 19.3 in full, and checked the rest by search and by targeted reading (every "Section 19.x" cross-reference in the book, all listing captions, analogies, exercises, and the code claims against `git show book-m6-final`). I did not re-read Sections 19.5 to 19.11 word by word this window; my earlier passes covered them and found them strong.

Earlier majors: the `IDLE_WARNING_SECONDS` gap, the unwritten sections, and the prerequisite ordering of Angular templates are RESOLVED (all sections now exist; the `idle.ts` example is explained inline).

Verified: TypeScript `~6.0.2` in `package.json` and 6.0.3 in the lock file; `Role` union in `session.service.ts`; `firstValueFrom` in `session.service.ts`; `MAX_CONCURRENT_TILE_FETCHES = 6` and `Array.from({ length: MAX_CONCURRENT_TILE_FETCHES }, () => worker())`; `loadGeneration` guards; `admin.models.ts` event list; the Chapter 20 pointers (`~` in Section 20.8, `tsconfig` in Section 20.7) are correct. All 14 listings follow the house caption format (`**Listing N.M — file (tag)**`, `*Path: ...*` AFTER the block). The two analogies (doctor's-office form, the buzzer) each have a "Where the analogy breaks down" paragraph. Terms are bold at first definition. No dossier or agent-id citations, no secrets, header status is `expanded`. Renumbering check: all Section 19.x references in Chapters 19, 21, 22, 23 and 24 point to the right sections (19.1, 19.3, 19.4, 19.6, 19.9, 19.11, 19.12 verified against the headings). No blockers, no majors.

## Findings

1. **minor** - 19.7 says "Chapter 22 explains why a small pool", yet Chapter 22 never explains the pool size (it explains why `fetch` is used, 22.7, and the throttle, 22.14). Either add two sentences in 22.7 or change the pointer in 19.7 to Section 19.13.
2. **minor** - 19.1: "Data that arrives later ... is not checked at all when the program runs" is right for this app (`HttpClient.get<T>`), but a beginner may read it as "TypeScript never checks at run time". One clause, "unless you write the check yourself", would make the limit precise.
3. **minor** - Beginner-tier pacing: six sections and four listings before the reader sees why Angular needs the `*.models.ts` and `idle.ts` shapes. A one-sentence forward pointer to Chapter 22 at Listing 19.1 ("these shapes describe what the API sends back") would help.
4. **minor** - Terms used lightly before being defined: "spread operator" (learning objective; first explained in 19.6), "narrowing" (19.3: define in one clause where `state.kind === 'warning'` is explained), "module" (19.4: the Java package comparison would help).
5. **minor** - The objective "Handle missing values with `null`, `?.`, `??` and the spread operator": the spread operator is not about missing values. Say "and copy-with-change (`{ ...user, ... }`)".
6. **minor** - 19.14 shows the output of the floating-point experiment ("Run in Node (Chapter 20)") but the code that produced it is not in the same block. Show the two-line snippet so the reader can reproduce it.
7. **minor** - Listing 19.8 is Java (`DocumentSummary.java`) inside a TypeScript chapter; add "Java" to the caption so a reader scanning listings sees the language change.
8. **minor** - "The lock file records 6.0.3" (19.1) is a detail a beginner does not need there; move it to Chapter 20, where lock files are explained.
9. **minor** - Exercises 19.1 to 19.6 are in the house format with a solutions file. 19.6 (tile arithmetic, ★★) and 19.5 (generic `firstWhere`, ★★★) are solvable from the text. I did not re-derive the solutions this window.
