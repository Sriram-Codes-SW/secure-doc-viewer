# Accessibility review (final copy)

Reviewer: Accessibility. Built files tested: `book/build/out/secure-doc-viewer-guide.pdf` (652 pages), `.epub`, `.html`, plus `manuscript.md` (files dated 2026-09-20 06:57-07:00). Scratch: `book/build/out/a11y-scratch/`.

## Verdict

The EPUB and HTML are a reasonable base (language, title, nav, alt on all 73 images, real `<th>` header cells, semantic code blocks). The PDF, which the coordinator says is the final copy, is **not accessible**: it is untagged, tables are clipped at the page edge on many pages, and no figure has alt text. The figure alt text is only the caption, and 63 of 69 captioned figures have no long description. The EPUB has accessibility metadata but lacks the summary and conformance statement, has 13 epubcheck errors (one fatal XML error), and 19 cross-reference links point at `.md` files that do not exist in any output.

| Severity | Count |
|---|---|
| Blocker | 3 |
| Major | 9 |
| Minor | 8 |

PDF pages inspected: 1, 5, 30, 40, 45 (text extraction), 96 (image), 535 (text), 560 (image), plus a full-book scan of text positions on all 652 pages and of link annotations. Markers: **[PDF-only]** = not visible in markdown/EPUB/HTML.

Tools: **veraPDF** (docker `verapdf/cli`, flavour ua1) ran: result FAIL (see A11Y-01). **epubcheck** ran via `npx epubchecker`: 13 errors (A11Y-04, A11Y-05). **Ace by DAISY** could not be run (`npx @daisy/ace`: "Electron failed to install correctly"); its checks were done by hand below. The official epubcheck docker image (ghcr.io/w3c/epubcheck) was denied, so the npm wrapper was used.

## Blockers

**A11Y-01 [PDF-only] Untagged PDF (fails PDF/UA-1 and Section 508)**
- Where: whole PDF; `book/build/out/secure-doc-viewer-guide.pdf`; `book/build/build.py`, `book/build/header.tex`.
- Quote: catalog has `/Lang (en-US)`, `/Outlines` (599 bookmarks) and title metadata, but `MarkInfo` and `StructTreeRoot` are absent (pymupdf `xref_get_key`). veraPDF ua1: FAIL.
- Problem: no structure tree, so screen readers get no headings, lists, table cells, figures or reading order beyond raw text order. Producer is xdvipdfmx (xelatex), which does not tag by default. Author is empty; no `DisplayDocTitle` viewer preference.
- Suggested fix (build): enable LaTeX tagging with `\DocumentMetadata{lang=en-US, pdfversion=2.0, pdfstandard=ua-2, tagging=on}` as the very first line of the template (requires a recent TeX Live 2025+ and lualatex; xelatex tagging support is still experimental, so test; if xelatex cannot tag, switch the PDF engine to lualatex with the same fonts). Pandoc: `-V`/`--include-in-header` cannot place it before `\documentclass`; use a custom template or `--pdf-engine-opt`. Verify with veraPDF `--flavour ua1` or `ua2`. Also set `--metadata author=...` and `-V colorlinks` stays; add `\hypersetup{pdfdisplaydoctitle=true}`. Fallback if tagging cannot be done: publish the EPUB and HTML as the accessible editions and state that the PDF is a print-fidelity copy.

**A11Y-02 [PDF-only] Tables run off the page and are clipped; text is lost to a reader**
- Where: 96 of 652 pages have text spans ending at or beyond the page edge (595 pt), for example pages 26, 31, 35, 43, 96, 97, 151, 157-164, 560, 573-578, 630, 641, 649, 650 (full list from scan). Seen visually on p. 96 (Table 6.2, right column cut mid-sentence: "the project configures no sepa") and p. 560 (Table 39.1, "Where" column cut: `SecurityConfig, appl`).
- Problem: pipe tables with long cells become unwrapped longtable columns; content beyond the edge is invisible in print and on screen (WCAG 1.4.10 Reflow, 1.3.1). Extracted text still has it, which hides the bug from a text-only check.
- Suggested fix (build): give Pandoc pipe tables relative column widths (dash counts in the separator line make Pandoc wrap; content, writers: use `|---|------|----------|` proportional separators for every table whose lines exceed 78 columns), or add a Lua filter that sets `ColSpec` widths equally/heuristically for all tables, and use `p{}` columns via `\usepackage{array}`. Recheck with the same scan.

**A11Y-03 Figures: alt text is the caption only; most complex diagrams have no long description; the PDF has no alt at all**
- Where: all 73 figures; `build.py` lines 39-53 (alt from caption). 63 of 69 captioned figures have no `*Text description:*` (only the 6 blueprints in Appendix B carry one; count in `manuscript.md`: 6 vs 69 captions). The contexts brief says a Text description line is required.
- Quote: alt is `Diagram. Figure 32.2 — The chain of checks a tile request passes, and the answer each one gives`. The six Appendix B images have alt exactly `Diagram` (ch051.xhtml), with the description in a separate paragraph.
- Problem: sampled 15 figures (1.1, 4.1, 6.2, 8.3, 12.1, 16.2, 18.2, 22.1, 25.1, 28.1, 32.2, 35.1, 37.2, 39.3, plus the sign-in throttle flow, rendered-24.png). Captions name the topic, not the content: for example Figure 1.1 (a six-step chain), the sign-in flow (four decisions, five outcomes and the numbers 5, 20, 20), the sequence diagrams (message order) and Figure 32.2 (a chain of checks and answers). A blind reader gets none of the thresholds, order or branches. "Diagram." prefix is redundant with "Figure". PDF: 73 images but no `/Alt` (untagged).
- Suggested fix (content, writers): add a `*Text description: ...*` (1-4 sentences, or a numbered list for flows and sequences, with the numbers and outcomes) under all 63 figures. (Build, coordinator): make the alt text the caption without the "Diagram." prefix, and make the build fail when a figure lacks a description; for Appendix B use the description as the alt. Consider wrapping in `<figure>`/`<figcaption>` with a Lua filter (Pandoc `implicit_figures` does this if the alt equals the caption on its own paragraph; the current blank-line layout defeats it), and mark a description as `aria-describedby` in HTML.

## Major

**A11Y-04 EPUB: fatal XML error and invalid content**
- Where: `EPUB/text/ch034.xhtml(338,106)` (Ch 27, sharing rules). epubcheck: `RSC-016 fatal` and `RSC-005: element "username" not allowed here`.
- Quote: manuscript line 15203: `'<username>'."` (a bare tag in prose, message "No user named '<username>'.").
- Problem: raw HTML passes through Pandoc; the chapter file is not well-formed XHTML. Some readers refuse or truncate the chapter. Same construct is also live HTML in the single HTML page (the text vanishes).
- Suggested fix (content): write it as `` `No user named '<username>'.` `` or `&lt;username&gt;`. Search the sources for other bare `<word>` in prose (build check: run epubcheck in CI).

**A11Y-05 19 cross-reference links point at `.md` files (dead in EPUB, HTML and PDF)**
- Where: manuscript lines 419, 548, 553, 568(?), 643, 1132, 3542, 5456, 5636, 13039, 13896, 14892, 15728, 16435 and others; epubcheck `RSC-007` x9, and `RSC-012` (8 unresolved fragments in `nav.xhtml` and `toc.ncx`). PDF has 20 link annotations that are file/launch links (e.g. p. 27 `../front-matter/c-setting-up-your-machine.md`, p. 31, 34, 35).
- Quote: `[Chapter 8](08-how-the-web-works.md)`, `[Setting up your machine](../front-matter/c-setting-up-your-machine.md)`, `[Part IV introduction](00-part-introduction.md)`.
- Problem: links promise navigation but fail; screen-reader users hear a link that leads nowhere (WCAG 2.4.4, 3.2.x).
- Suggested fix (build): Lua filter that rewrites relative `*.md` link targets to the heading identifier of that chapter's H1 (build a map from the joined order file), or strip the link and keep the text. Content: prefer `Chapter 8` plain text (STYLE says cross-reference by number) and drop these markdown links.

**A11Y-06 EPUB accessibility metadata incomplete**
- Where: `EPUB/content.opf` has accessMode textual, accessModeSufficient textual, features alternativeText/readingOrder/structuralNavigation/tableOfContents, hazard none. Missing: `schema:accessibilitySummary` (required by EPUB Accessibility 1.1), `dcterms:conformsTo` (EPUB Accessibility 1.1 - WCAG 2.x AA), `schema:accessMode visual` (the diagrams are images), `a11y:certifiedBy`, publisher/`dc:creator` (empty), `dc:publisher`. `alternativeText` is claimed but 63 figures only have caption-level alt text (A11Y-03), and `tableOfContents` claim is fine; `index` is not claimed (there is an index-terms appendix; check whether an index exists).
- Suggested fix (build): add a metadata file passed with `--epub-metadata` containing `<meta property="schema:accessMode">visual</meta>`, `<meta property="schema:accessibilitySummary">...</meta>`, `<meta property="dcterms:conformsTo">EPUB Accessibility 1.1 - WCAG 2.2 Level AA</meta>` only after the fixes verify; until then claim nothing higher than what tests show. Also `--metadata author=...`.

**A11Y-07 Heading hierarchy skips levels**
- Where: EPUB chapters ch032-ch038 (Ch 25-31, Part IV): H2 (Beginner tier) then H4 at some points (29 skips in total by script); appendix C solutions (ch053+): H1 "Solutions: Chapter 11" jumps to H3 "Exercise 11.1 ★ ...". Chapter openers are `# Chapter N` with H2 tiers and H3 sections, which is otherwise consistent.
- Problem: WCAG 1.3.1/2.4.6; heading navigation shows gaps.
- Suggested fix (content, Appendix C): make exercise solutions H2. (Content, Part IV) check the `####` inside Part IV chapters. Build: lint heading jumps in `build.py`.

**A11Y-08 Tables: no scope, no real captions, caption position inconsistent**
- Where: 118 tables in EPUB; 344 `<th>` and 0 `scope`; 0 `<caption>`; 65 tables have the caption as a separate bold paragraph. In the PDF the caption sits below Table 6.2 (p. 96) but above Table 39.1 (p. 560), against STYLE ("caption above").
- Problem: header cells are recognised (good), but the caption is not programmatically tied to the table (WCAG 1.3.1); some readers do not announce the table name. HTML CSS sets `table{display:block}` (Pandoc default), which can strip table semantics in some browser and screen reader combinations.
- Suggested fix (build): Lua filter that moves a preceding paragraph matching `^Table [0-9.]+ —` into the table `caption`, adds `scope="col"` to `th` (or use a Pandoc HTML post-process); override the CSS with `table{display:table}` plus a wrapper `div{overflow-x:auto}`.

**A11Y-09 HTML has no skip link, no landmarks except nav, and default focus styling**
- Where: `secure-doc-viewer-guide.html`: 9.4 MB single page, `<main>` count 0, one `<nav>` (TOC), no skip link (the "skip" hits are prose).
- Problem: WCAG 2.4.1: a 600-page document with a long TOC first; keyboard users must tab through hundreds of links (the TOC has 599 entries).
- Suggested fix (build): custom Pandoc HTML template or `--include-before-body` with `<a class="skip" href="#chapter-1">Skip to content</a>`, wrap in `<main>`, and CSS `.skip:not(:focus){position:absolute;left:-9999px}`. Add explicit `:focus-visible{outline:3px solid #005fcc;outline-offset:2px}`. Add `--toc-depth=2` for the HTML page and offer per-chapter navigation, or `--split` output.

**A11Y-10 Difficulty markers (★) have no text equivalent at first use in headings and in the HTML/EPUB**
- Where: 901 uses of ★; headings like `### Exercise 1.1 ★ Count the tiles`, 464 exercise headings. The preface explains "graded ★, ★★ and ★★★" and STYLE defines "One star".
- Problem: screen readers announce "black star" (varies by reader: "black star black star"), the meaning "difficulty 1" is not conveyed in the heading itself, and the navigation list of headings reads "black star". In the PDF the star comes from DejaVu via `newunicodechar`, so extraction gives U+2605 (which reads fine as text, but the fonts have no `ActualText`).
- Suggested fix (content, writers): write `Exercise 1.1 (★ one star: guided)` or `Exercise 1.1 ★ (one star)` so the text carries it. Or (build) a Lua filter that wraps runs of ★ in `<span role="img" aria-label="one star">★</span>` in HTML/EPUB and, in LaTeX, uses `\texorpdfstring` and, once tagged, `ActualText`.

**A11Y-11 [PDF-only] Figures print small; wide diagrams are hard to read**
- Where: p. 30 (Figure 1.1, a 6-box horizontal flow scaled to the text width; text about 5 pt effective); p. 140 (1512x440 px image printed 454 x 132 pt), p. 546 (effective 129 dpi). Text is legible at zoom but below 8 pt printed. Diagram colours themselves are fine (default Mermaid lavender fill `#ECECFF`, dark text on light, 0 custom colours, arrows and labels carry meaning, not colour), and the PNGs I viewed (rendered-24, rendered-8) are crisp at `-s 2`.
- Suggested fix (content): use `flowchart TB` (top-to-bottom) for chains longer than 4 nodes; (build) render wide diagrams at higher scale or put them in landscape; provide the Mermaid text in the description.

**A11Y-12 Code blocks: reading order and wrapping in the PDF; syntax highlighting contrast unverified**
- Where: 501 `<pre>` blocks. HTML/EPUB are fine for screen readers (`<pre><code>`), listing captions are plain bold paragraphs above, not tied to the block. In the PDF, `breakanywhere` breaks long lines mid-token (e.g. p. 66 text ending beyond margin) which a screen reader announces as separate words; identifiers such as `SignedUrlService` are split across lines.
- Problem/fix: (build) set `breakafter` on punctuation only (`breaksymbolleft`, `breakanywhere=false`) and reduce `fontsize` if needed; check the Pandoc highlight theme (default `pygments`) for contrast (comment colour `#60a0b0` on white is 2.9:1, below 4.5:1). Use `--highlight-style=tango` or a custom `.theme` with `#586e75`-class contrast, or `--highlight-style=monochrome` for print. I did not measure every colour; comments and some keyword colours in the default theme fail AA (unverified numbers, computed from the Pygments palette).

## Minor

**A11Y-13 HTML link contrast/identity:** `a{color:#1a1a1a}` with underline (Pandoc default) keeps links distinguishable by underline, so OK; but `a:visited` is identical and there is no focus style (see A11Y-09). PDF links use `colorlinks` (blue, red, cyan); cyan URL links (`urlcolor`) on white fail contrast (about 1.5:1 for pure cyan). Fix (build): `-V urlcolor=NavyBlue -V linkcolor=NavyBlue -V citecolor=NavyBlue`.

**A11Y-14 Language metadata for non-English fragments:** none needed (all English); PDF `/Lang` and EPUB `xml:lang` present. OK; no finding beyond adding `lang` on the quoted British text is unnecessary.

**A11Y-15 Ambiguous link text:** none found among 5,000-plus link targets ("here", "click here": 0). Most links are "Chapter N". Some raw URLs appear as link text (not counted); acceptable.

**A11Y-16 Jargon at first use:** bold first-use terms with glossary (Appendix A) are consistent; sampled Ch 1, 2, 6, 8, 12. Fine. Minor: acronyms such as HMAC, CSRF, CSP may appear before their definition in Part I (a technical lead should confirm the order).

**A11Y-17 Plain language:** 10,263 sentences in prose, mean 16.1 words, but 494 exceed 35 words and 93 exceed 50 (scripted estimate including list fragments). Worst clusters are in the Advanced tiers and Ch 37. Suggested fix (content): split sentences over 40 words in Beginner tiers first; target reading grade about 9-10 for Beginner tier.

**A11Y-18 Colour:** no colour words used for meaning in prose (0 matches for "the red/green/blue box/line"). The watermark is described as red; a text description exists. The checkmark symbols (✓ ✗, 41 uses) come with adjacent words in the tables I sampled; unverified elsewhere.

**A11Y-19 PDF outline and page labels:** 599 bookmarks with correct hierarchy, roman-to-arabic page labels are present (good). Bookmark titles carry no chapter numbers for some parts (unverified). Minor: the printed TOC page 5 extracts with dot leaders; fine.

**A11Y-20 EPUB/HTML stylesheet:** EPUB CSS sets `line-height:1.2`, `font-family: Georgia`, text colour `#1a1a1a` (contrast fine, 17:1); `h1{page-break-before:always}` fine; body font size is user-controllable (relative units, good). HTML `max-width:36em` reflows, `viewport` meta present (good).

## Concrete build recipe (coordinator)

1. `header.tex`/template: `\DocumentMetadata{...tagging=on...}` (A11Y-01), table widths (A11Y-02), link colours (A11Y-13), code wrap and highlight style (A11Y-12).
2. Lua filters (`--lua-filter`): `.md` link rewrite (A11Y-05), table caption/scope (A11Y-08), figure alt from description (A11Y-03), ★ labels (A11Y-10), heading-jump check (A11Y-07).
3. `--epub-metadata` file and `--metadata author` (A11Y-06); HTML template with skip link, `<main>`, focus CSS (A11Y-09).
4. CI: run `epubchecker` and veraPDF; fail on errors.

Writers: text descriptions for 63 figures (A11Y-03), `<username>` (A11Y-04), Appendix C heading level (A11Y-07), star wording (A11Y-10), long sentences (A11Y-17), table separator widths (A11Y-02).

## Checked and found correct

EPUB `lang=en-US` on every chapter and OPF; `dc:title`; nav document with `epub:type="toc"` and `role="doc-toc"` (599 entries); all 73 EPUB images and all 73 HTML images have non-empty alt; HTML `lang`, `title`, viewport; code in semantic `<pre>`; tables have `<thead>` and `<th>`; PDF has language, title, 599 bookmarks, page labels, 951 links (599 internal-goto, 332 URI); no custom diagram colours; diagram PNGs legible at their native size; no placeholder link texts.

## Not checked

Full Ace run (tool failed); screen-reader playback (NVDA/VoiceOver); measured contrast of every highlight colour; every one of 73 figures (15 sampled); hazard flash/motion (none expected); the layout artist's final PDF (layout not final); Section 508 checklist items outside these.
