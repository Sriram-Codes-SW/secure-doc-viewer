# QA review checklist

Owner: qa. Based on CHARTER.md, STYLE.md (esp. sections 6-17) and OUTLINE.md.
Severity: **blocker** (wrong, invented, insecure or unteachable; must fix before draft is accepted),
**major** (misleads or breaks a style rule that affects learning), **minor** (polish).

## A. Technical accuracy
1. Every Listing is byte-identical to `git show <tag>:<path>`, except where labeled "simplified" with what was cut.
2. The Listing caption names the file and tag (`Listing N.M — File.java (book-mX-...)`); the tag exists and is the right milestone.
3. Examples (not repo code) carry no tag and are not presented as project code.
4. Versions match `pom.xml` / `frontend/package.json` / Dockerfile / compose at that tag. Remember: m0-m4 = Spring Boot 3.3.4, Java 21, PDFBox 3.0.3; m5-m6 = Boot 4.1.1, Java 25, PDFBox 3.0.8, Tomcat 11.0.26, Maven 3.9.16; Vitest 4.x until m6 (5.0.1); Playwright 1.63 from m5; MySQL 8.4; Flyway/MySQL driver arrive at m1.
5. Parts I-III quote `book-m6-final` by default; Part IV chapters on m0-m4 state the older versions once and note differences.
6. Every historical claim (decision, bug, number, date, PR) is in the dossier or verifiable in git/PRs, and has a `<!-- source: ... -->` comment. Nothing invented.
7. Commands shown actually work on the stated OS/tag (paths, flags, ports, wrapper `mvnw`).
8. Security statements are correct (cookies, CSRF, signed URLs, hashing, TLS); no overclaiming ("unhackable").
9. No secrets: no passwords, tokens, session ids, personal emails, contents of off-limits files. Placeholders in angle brackets; example users only pub.one / reader.one / outsider.one.
10. Attacks are shown only against the reader's local copy.

## B. Pedagogy (complete-beginner view)
11. Every term is defined before first use (or in the same paragraph); bolded terms are new and have a glossary request in requests.md.
12. No concept appears before the chapter that teaches it (check OUTLINE "assumes"); if unavoidable, minimal teach-in plus "Chapter N goes deeper".
13. Every analogy has an explicit "where the analogy breaks down"; established analogies (STYLE 8.1) are reused consistently.
14. Explains why before how; code is explained line by line for beginner-tier listings.
15. Simplifications are announced with a pointer to the full picture.
16. Nothing the app does not use (scope creep).
17. Exercises: graded star/2/3, appropriate to what the chapter taught, need no credentials, name the tag; star and 2-star have solutions; solutions correct and runnable.
18. Objectives (3-6) are testable by the exercises and covered in the text; Summary matches objectives.
19. Difficulty ramps across the three tiers; tiers are not labeled per paragraph.

## C. Structure and publishing standards
20. Metadata comment and `# Chapter N: Title` match OUTLINE; file name matches `NN-slug.md`; correct folder/owner.
21. Template sections present and in order: Objectives, Prerequisites, Beginner, Intermediate, Advanced, In this project, Try it, Summary, Further reading (+ blueprint vN and Decisions and challenges in Part IV).
22. Heading levels and numbering (N.M) follow STYLE 5; no skipped levels.
23. Listings, Examples, Figures, Tables numbered per chapter, captioned (figures: caption below in italics), and referred to in the text before they appear.
24. Cross-references use "Chapter N / Section N.M / Listing N.M" with working relative links; no "above/below".
25. Callouts (Note/Warning/In this project) are sparing, 1-4 sentences, correctly typed.
26. Terminology consistent with the STYLE 3.1 word list and across chapters; abbreviations expanded on first use.
27. American English; no emojis; second person; no "we" for project history; lines in Examples <= 90 chars.
28. Further reading cites official documentation only.
29. Glossary coverage: each bolded term appears in appendix-a or has a pending request.
30. Diagrams (Mermaid) render, match the code at that tag, and the blueprint vN describes real changes since vN-1.
