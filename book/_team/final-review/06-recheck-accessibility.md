# Accessibility recheck (after the fix batch)

Reviewer: Accessibility. Files tested: `book/build/out/secure-doc-viewer-guide.pdf` (750 pages, PDF 2.0, written 14:45), `.epub` and `.html` (written 14:24-14:25), sources in `book/` (order.txt, 59 files). Scratch: `book/build/out/a11y-scratch/`.

## Verdict

Large improvement. The PDF is now tagged and passes veraPDF PDF/UA-2; tables no longer run off the page; every figure has a text description and the PDF carries it as `/Alt`; the EPUB passes epubcheck. I found one **new defect introduced by the fix** (two- and three-star exercises are announced as "one star") and one build mismatch (the built EPUB does not contain the accessibility summary that `epub-metadata.xml` now specifies). I cannot support a claim of full conformance yet (see "What still stops the accessible claim").

Counts for A11Y-01..20: Fixed 12, Partly 5, Not fixed 1, unchanged or no action 2 (A11Y-14 was a non-finding, A11Y-16 not re-verified). New findings: 1 Major, 4 Minor.

## Tool results

- **veraPDF (docker `verapdf/cli`)**: `--flavour ua2`: **PASS**. `--flavour ua1`: 102 rules passed, 4 failed. The UA-1 failures are the wrong-flavour header/`pdfuaid:part` rules (the file is PDF 2.0 declaring part 2) and 418 link annotations without a `/Contents` key (UA-1 7.18.1 and 7.18.5). UA-2 handles links through structure, and it passes. Someone who insists on UA-1 would not accept this file, but UA-1 does not apply to PDF 2.0.
- **epubcheck (`npx epubchecker`)**: "Everything is fine" (0 errors; it was 13 before).
- **Ace by DAISY**: still could not be run (Electron does not install here). Its checks were done by hand.
- **PDF structure (own scan, pymupdf)**: `/Lang en-US`, `MarkInfo /Marked true`, `StructTreeRoot` present, `DisplayDocTitle true`, XMP with `pdfuaid:part 2 rev 2024`, title/author/subject/keywords set, 1,662 bookmarks. Structure elements: 129 Table, 376 TH (each with class `TH-col`), 3,654 TD, 1,376 TR, 81 Figure (81 of 81 with `/Alt`, taken from the text descriptions), 419 lists with LI/Lbl/LBody, 519 code (`verbatim`), 274 Link, 1 TOC and 59 TOCI. RoleMap maps `chapter` to H1, `section` to H2, `subsection` to H3, `subsubsection` to H4 (headings are real). Text extraction of p. 91 and p. 594 shows a sensible reading order (paragraph, table caption, table cells row by row, figure caption, Text description, code, path line).
- **PDF pages inspected**: 20 (rendered), 27 (text), 91 (rendered and text), 500, 594 (rendered and text), plus a scan of all 750 pages for clipped text (0 pages), links (359 URI, 59 internal named; 0 launch/file links), font sizes (8 pt is the minimum: 195,100 characters, code) and image widths.

## Source-level re-verification

| Check | Result |
|---|---|
| Figures with `*Text description:*` | 77 of 77 (was 6 of 69). Sampled 1.1, 6.2, 39.3: accurate, state order and outcome. |
| Heading jumps in the sources (per file), EPUB, HTML | 0 (was 29 in EPUB). |
| Bare `<word>` tags in prose | Only `<kbd>` (valid HTML). `<username>` is gone. |
| Markdown links to `.md` files | 21 remain in the sources; in the built EPUB, HTML and PDF they are unwrapped (0 `href` to `.md`, 0 launch links). Cross-references are plain text now, so they cannot be clicked. |
| Positional references ("above", "below", "page N") | 17 hits, all false positives (page numbers of a document in the app, not of the book). None found. |
| Table captions | 94 bold "Table N.M" captions in the sources; the build folds them into `<caption>` (105 of 129 tables in EPUB and HTML). The 24 without a caption are unnumbered "In this project" and appendix tables, which STYLE exempts. |
| Sentences over 35 words / over 50 | 462 / 62 (was 494 / 93); mean 15.1 words. Improved, not solved. |

## Re-verification of each earlier finding

| ID | Status | Evidence |
|---|---|---|
| A11Y-01 Untagged PDF | **Fixed** | StructTreeRoot, MarkInfo, Lang, XMP `pdfuaid`, veraPDF UA-2 PASS. Caveat: UA-1 flavour fails on the header/`pdfuaid` rules and on 418 link annotations without `/Contents`; automatic checks cannot show that the tags are useful. |
| A11Y-02 Tables clipped | **Fixed** | 0 of 750 pages with text at the page edge (was 96). Tables 6.2 (p. 91) and 39.1 (p. 594) wrap fully. Residual: rows have no rules, so multi-line cells blur (Minor, layout). |
| A11Y-03 Figure alt text | **Fixed** (with a caveat) | 77 of 77 text descriptions; alt is the description (average 439 characters in EPUB, no "Diagram." prefix); all 81 PDF Figure elements have `/Alt`; 81 of 81 HTML and EPUB images have alt. Caveat: I sampled 3 descriptions for accuracy after the fix, not all 77. |
| A11Y-04 Invalid XML in EPUB | **Fixed** | epubcheck clean; no bare tags but `<kbd>`. |
| A11Y-05 Dead `.md` links | **Fixed** | 0 `.md` hrefs in EPUB and HTML, 0 launch links in the PDF, epubcheck clean. Trade-off: cross-references are no longer links. |
| A11Y-06 EPUB accessibility metadata | **Not fixed in the built file** | The built `content.opf` still has only textual, textual (sufficient), the four features and hazard none. It lacks `accessibilitySummary`, `accessMode visual` and `dcterms:conformsTo`. `book/build/epub-metadata.xml` in the repo has the summary and `visual`, but the OPF in the EPUB (written 14:24) does not, and its feature order matches the old file; the shipped EPUB appears to have been built from an older metadata file. Rebuild the EPUB and recheck. Do not add `conformsTo` until every check below is passed. `dc:creator` is now set ("Claude (Anthropic)"). |
| A11Y-07 Heading jumps | **Fixed** | 0 jumps in EPUB, HTML and sources. |
| A11Y-08 Tables: scope, caption | **Partly** | EPUB and HTML: 374 of 374 `th` have `scope="col"`; PDF: 376 TH with column class; captions are in `<caption>` for 105 of 129 tables. Remaining: the 24 uncaptioned tables; the "About this edition" table (ch001) is a key-value table with no header cells at all (row headers are not marked `scope="row"`). |
| A11Y-09 HTML skip link, landmarks, focus | **Fixed** | `<a class="skip-link" href="#main">`, one `<main>`, `<nav id="TOC" role="doc-toc">`, `a:focus, button:focus, [tabindex]:focus { outline: 3px solid #0b57d0; outline-offset: 2px }`. Not tested in a browser. |
| A11Y-10 Stars | **Partly, with a new defect** | See A11Y-N1: HTML and EPUB label only the first star ("one star") and leave the rest as bare text. In the PDF the heading is the glyphs "★★" with no wording. |
| A11Y-11 Small figures | **Partly** | The text description now carries the content, so nothing is lost for a reader who cannot see the figure. Printed diagrams are still small: the flow diagrams on p. 91 and p. 594 print with a text height of about 5-6 pt (wide left-to-right charts scaled to the text width). Low-vision readers must zoom. |
| A11Y-12 Code blocks | **Partly** | Fixed: lines wrap with a "↪" continuation mark, so identifiers no longer split at random (p. 91 to p. 594 samples); code is real text. Not fixed: colours. See A11Y-N3. Code is 8 pt in the PDF (small). |
| A11Y-13 Link colours | **Fixed** | PDF internal links `#214a87` on white, 8.8:1; URL links `#000099`, 14.4:1. HTML `#0b4f9c`, visited `#5b2a86`, underlined. |
| A11Y-14 Language | **Fixed / no action** | `lang="en-US"` on HTML and EPUB, `/Lang en-US` in the PDF. |
| A11Y-15 Link text | **Fixed / unchanged** | No "here" type links. |
| A11Y-16 Jargon first use | **Not re-verified** | Unchanged from the first review (no defect then). |
| A11Y-17 Plain language | **Partly** | 462 sentences over 35 words, 62 over 50, all in prose (was 494 and 93). Still worth a writer pass on Advanced tiers and Ch 37. |
| A11Y-18 Colour alone | **Fixed / unchanged** | No colour-only meaning; diagrams are default Mermaid (no custom colours); syntax colours are decoration. |
| A11Y-19 Bookmarks, metadata | **Fixed** | 1,662 bookmarks (the index letters A-Y are bookmarks too), `DisplayDocTitle`, title, subject, keywords, author set. Note for the owner: the author field reads "Claude (Anthropic)" in the PDF and EPUB; decide whether that is intended. |
| A11Y-20 EPUB/HTML stylesheet | **Fixed / unchanged** | Text colour `#1a1a1a`, relative units, viewport meta present, reflows. |

## New findings

**A11Y-N1 (Major) Two- and three-star exercises are announced as "one star" (HTML and EPUB)**
- Where: every exercise heading with more than one star. HTML: `Exercise 1.3 <span role="img" aria-label="one star">★</span>★ Read the limits`. In EPUB, 322 chapter-file lines have the form `aria-label="one star">★</span>★`. `h.count('">★★</span>')` and `'">★★★</span>'` are both 0: the filter wraps each star separately, and only the first star gets a label. The only label used anywhere is "one star" (492 times).
- Problem: a screen reader says "one star, black star" for a two-star exercise and "one star, black star, black star" for a three-star one; the difficulty is misstated (WCAG 1.1.1, 1.3.1). The label is worse than none.
- Suggested fix (build, `book.lua`): match a whole run of ★ and emit one span: `role="img" aria-label="two stars"` around `★★` and `"three stars"` around `★★★`. For the PDF, the tagged LaTeX layer supports `\texorpdfstring`/ActualText, or just add wording in the heading (content): `Exercise 1.3 (two stars)`.

**A11Y-N2 (Minor) PDF stars carry no wording**
- Where: PDF headings extract as `Exercise 1.3 ★★ Read the limits`. Screen readers say "black star" per glyph. Fix: same wording, or ActualText in the tagged PDF.

**A11Y-N3 (Minor) Syntax highlighting colours fail WCAG AA in code blocks**
- Where: PDF code, measured on the actual span colours against white and against the `#f8f8f8` code background: strings `#4f9905` 3.6:1 and 3.4:1; operators `#cf5c00` 4.05:1 and 3.8:1; HTML and EPUB theme: comment `#60a0b0` (about 2.7:1), number `#40a070` (about 3.3:1); keywords `#007020` (about 7:1, fine) and `#8f5903` 5.8:1 (fine).
- Problem: text below 4.5:1 (WCAG 1.4.3). Colour is decorative, so meaning is not lost, but the text is faint for low-vision readers.
- Suggested fix (build): pass a custom Pandoc `--highlight-style` JSON with darker colours (for example strings `#2e6b00`, operators `#8a3d00`, comments `#4a6e78`, numbers `#1f6b4a`), or `--highlight-style=monochrome`/`tango` tuned to at least 4.5:1, and set the same in `header.tex` for LaTeX.

**A11Y-N4 (Minor) Key-value and uncaptioned tables**
- Where: `About this edition` table (ch001), a two-column table without header cells; 24 tables without `<caption>` (unnumbered "In this project" and appendix tables).
- Fix (content): give the key-value table a header row (`Item | Value`) or turn it into a definition list; give appendix tables a caption or an introducing sentence.

**A11Y-N5 (Minor) Cross-references are no longer links**
- Where: all "Chapter N", "Section N.M", "Figure N.M" references, since the `.md` links were unwrapped. The PDF has 59 internal links (the table of contents) and 359 external links.
- Consequence: no defect, but keyboard and screen reader users cannot jump to a referenced chapter or figure. Fix (build): a Lua filter that maps "Chapter N" and "Figure N.M" to anchors would restore navigation; optional.

## What still stops the accessible claim

1. **The built EPUB does not carry the accessibility summary or the `visual` access mode** (A11Y-06), so it cannot be said to meet EPUB Accessibility 1.1 discovery metadata. Rebuild and verify the OPF. Do not add `dcterms:conformsTo` until items 2 to 6 are done.
2. **Stars are announced wrongly** in HTML and EPUB (A11Y-N1). This is a correctness defect in an accessibility feature.
3. **No assistive-technology test has been done.** Neither Ace, Acrobat's Accessibility Checker, PAC 2024, NVDA, JAWS nor VoiceOver was run. veraPDF passing is necessary, not sufficient: it does not judge whether Alt text is meaningful, whether the reading order is right for every page, or whether tables are logically tagged. I read two pages' order only.
4. **Code colours fail AA contrast** (A11Y-N3) and the printed code is 8 pt; diagrams in the PDF print small (A11Y-11).
5. **Only 3 of the 77 descriptions were checked for accuracy after the fix**, and none against a blind reader's needs (the writers must confirm that each description states the numbers and outcomes shown).
6. PDF/UA-1 cannot pass (link annotations lack `/Contents`) and PDF/UA-2 is a newer standard with less checker support; state which one is claimed and cite the tool. Say "tagged PDF that passes veraPDF PDF/UA-2", not "fully accessible".
7. Layout is not final; a new PDF build after the layout artist's changes needs the same checks (veraPDF, clipped-text scan, tag counts).

Suggested wording for the book's accessibility statement, if one is added: "This edition provides real text, a heading structure, table headers, text descriptions for all figures, and a tagged PDF that passes veraPDF PDF/UA-2 and an EPUB that passes epubcheck; it has not been reviewed with assistive technology."

## Top issues

1. A11Y-N1: "one star" label on two- and three-star exercises (Major, build `book.lua`).
2. A11Y-06: built EPUB lacks the summary, `visual` mode; stale metadata (Major until rebuilt).
3. No assistive-technology or Ace test (process gap).
4. A11Y-N3: code colours below 4.5:1 (Minor to Major depending on policy).
5. A11Y-11: small diagrams in the PDF.
6. A11Y-17: 462 long sentences remain.
7. A11Y-08 and A11Y-N4: key-value table without headers, 24 tables without captions.
8. A11Y-N2: PDF stars without words.
9. A11Y-N5: cross-references are not links.
10. Author metadata says "Claude (Anthropic)" (owner decision).

## Not checked

Screen-reader playback; Acrobat and PAC checkers; every one of the 77 descriptions; PDF tag order on every page (two pages read); the EPUB in a reading system; browser behaviour of the HTML focus and skip link; whether the EPUB and HTML are older than the last source change (they are 20 minutes older than the PDF).

---

# Final pass (files written 2026-09-20: EPUB 17:26, HTML 17:27, PDF 17:49; PDF 747 pages)

## Results

| Check | Result |
|---|---|
| veraPDF PDF/UA-2 | **PASS**. |
| veraPDF PDF/UA-1 | 102 rules passed, 4 failed: header/`pdfuaid:part` (1 each, wrong flavour for a PDF 2.0 file) and link annotations without `/Contents` (421 each, clauses 7.18.1 and 7.18.5). Same as before; UA-1 does not apply to PDF 2.0, so I read this as expected, but a UA-1 audit would fail the file. |
| epubcheck (`npx epubchecker`) | "Everything is fine". |
| PDF structure | `StructTreeRoot`, `Lang en-US`, 1,663 bookmarks; 130 Table, 379 TH, 81 Figure of which 81 have `/Alt`; 0 of 747 pages with clipped text. |
| Star labels (EPUB and HTML) | Fixed. 167 "one star" on `★`, 199 "two stars" on `★★`, 126 "three stars" on `★★★`; 0 stray unlabelled stars after a span. PDF headings still show only glyphs (see the statement check). |
| EPUB OPF | Now has `accessMode` textual and visual, `accessModeSufficient textual`, the 4 features, hazard none, `accessibilitySummary`, creator. No `conformsTo`, correctly (do not add it). Minor formatting: the `visual` line is indented differently (harmless). |
| Tables and headings (EPUB) | 379 of 379 `th` have `scope`; 106 captions for 130 tables (the rest are exempt or unnumbered); 0 heading jumps; 81 of 81 images have alt of 5 or more characters. |
| Contrast, PDF code colours | All text colours in the PDF measured: `#8f5903` 5.5:1, `#3d7805` 5.09:1, `#b04f00` 4.99:1 on `#f8f8f8` (5.3:1 on white), `#214a87` 8.26:1, `#0000cf` 10.4:1, `#a30000` 7.73:1. Note `#b04f00` on `#f8f8f8` is 4.99, a hair under 5.05 stated, but above the 4.5:1 AA limit. Old failures (`#4f9905`, `#cf5c00`) are gone. |
| Contrast, HTML syntax theme | All seven colours 5.05:1 to 19.8:1 on `#f8f8f8` (`#d51010` 5.06, `#af4e00` 5.05, `#3d7805` 5.09). Pass. |
| Contrast, EPUB CSS | Colours `#1a1a1a` text; the EPUB has no syntax colours. Links use the reading system's colours (not set). Pass by inspection. |
| Contrast, HTML links and focus | `#0b4f9c` links, `#5b2a86` visited, `outline: 3px solid #0b57d0` on focus; all above 4.5:1 on white (not re-measured individually). |
| Diagram size in the PDF | 81 images, narrowest 264 pt wide (tall diagrams). Nominal label size computed from the image scale: 9.3 pt for the smallest (mermaid 16 px labels at `-s 2`), matching the coordinator's 9.2 pt. On the rendered page 92 the wide flow diagram is still visibly smaller than body text (body 11 pt); acceptable for AA (text alternative exists, zooming works) but the print size is my calculation, not a measured glyph height. |
| Code size | Code prints at 8 pt for about 195,000 characters (was the same). Not an AA failure for a PDF (the user can zoom) but small; consider 9 pt. |
| Reading order, sample | Text extraction of p. 92 (heading, paragraph, table caption, table cells row by row, figure caption, text description, code, path line) is in a sensible order. Only a few pages read (20, 92, 594 in earlier passes); not every page. |
| Alt text | 81 of 81 PDF figures with `/Alt`; 81 of 81 EPUB and HTML images with alt. The 77 captioned figures all have a visible text description. |

## Reading the About-page accessibility statement (`front-matter/00-about-this-edition.md`, lines 32-40)

Sentence by sentence against what I verified:

1. "The book is published as a PDF, an EPUB and a single web page." True.
2. "All three contain real text in reading order" Mostly true. Reading order is by structure in EPUB and HTML; in the PDF I read only a few pages. Slight overclaim on "in reading order" for the PDF.
3. "and every diagram has alternative text and a text description in the body." True (81 of 81 alt; 77 of 77 captioned figures described; the six blueprints in Appendix B carry theirs in the appendix text).
4. "The PDF is a tagged PDF that passes the automated PDF/UA-2 check of the veraPDF validator" True and correctly limited to "automated" and "PDF/UA-2".
5. "and the EPUB passes the EPUB validator epubcheck." True. Correctly says nothing about EPUB Accessibility conformance.
6. "In the EPUB and web editions the difficulty stars on the exercises are labelled in words;" True (167/199/126).
7. "in the PDF they are shown only as symbols, and the exercise text says what each level asks." **Overclaim.** The exercise text does not say what its level asks; the levels are explained once, in "How to use this book" ("One star checks your understanding ..."). Replacement: "in the PDF they are shown only as symbols; the meaning of one, two and three stars is explained in How to use this book."
8. "These editions were checked with automated tools and a manual review, not tested with screen readers or other assistive technology." Honest on the last point. "a manual review" is broader than what I did (sampling of pages, tags and files). Replacement: "These editions were checked with automated tools (veraPDF and epubcheck) and by manual sampling, and have not been tested with screen readers or other assistive technology."

Optional tightening of sentence 2: "All three contain real text; the EPUB and web page follow the reading order of the source, and the PDF is tagged so that reading order can be followed." I would keep that softer version; do not write "conforms to WCAG", "PDF/UA compliant" or "accessible" without qualification. The OPF summary sentence "All text is real text in reading order with a navigable table of contents and heading structure" has the same slight overreach for the PDF; it is an EPUB summary, so it is acceptable for the EPUB (which it describes).

## What still blocks a strict accessibility claim

1. **No assistive-technology test** (NVDA/JAWS/VoiceOver on the EPUB and HTML; Acrobat, NVDA on the PDF), and no Ace by DAISY or PAC run. Ace still cannot start here (Electron); run it on a machine where it does. Without this, claim only what the About page now says.
2. **PDF/UA-1 fails** on 421 link annotations without `/Contents` (a PDF/UA-1 requirement). Claim PDF/UA-2 only, and say "automated check". The tagging of links is otherwise done through Link structure elements.
3. **The EPUB has no `dcterms:conformsTo`** and no `a11y:certifiedBy`; this is right until a checker or a person confirms conformance. Do not add it before item 1.
4. **Reading order was verified on a handful of PDF pages**, not on all 747; tables were checked by tag counts and two pages, not every table.
5. **Figure descriptions were sampled, not all read by a person who cannot see them** (77 descriptions).
6. Small print: code at 8 pt; the widest diagrams at about 9 pt nominal; cross-references are plain text, not links; `#b04f00` code text is 4.99:1 on the code background (passes AA, misses the 5.05 the build notes state).
7. PDF stars still carry no words (fix by the wording in the About sentence 7 or by ActualText).

Nothing found that would make the current About text false apart from the two sentences above.

## Final counts

Earlier findings A11Y-01..20: Fixed 17, Partly 1 (A11Y-17 sentence length: 462 over 35 words), no action 2. New findings from the previous pass: A11Y-N1 and A11Y-06 now Fixed; N3 Fixed; N4 Fixed (About table has a header row); N2 open only as wording in the PDF (Minor); N5 open (Minor, by design). Open now: 1 Major (About-page sentence 7 overclaim: wording only), 4 Minor (PDF stars, code 8 pt, sentence length, cross-references not links).
