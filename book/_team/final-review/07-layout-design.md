# 07 Layout design (final interior)

Author: layout artist agent, 2026-09-20. Changed files: `book/build/header.tex`, `book.lua`, `book.css`, `templates/book.html`, `build.py` (geometry constants and the cover flag only), new `make_cover.py` and `cover.png`. No chapter text, front matter or appendix was edited. Nothing was committed. `out/secure-doc-viewer-guide.*` was not touched (preview builds write to `out/preview`).

## Decisions and rationale

| Topic | Decision | Why |
|---|---|---|
| Trim | US Letter (8.5 x 11 in), two-sided, `openany`; margins inner 1.0 in, outer 1.2 in, top and bottom 1.05 in | American English book. Letter keeps a 453.6 pt text width (A4 was 453 pt), so diagrams (scaled to text width) print no smaller than before, and the page count stays near the +15 percent budget. A 7 x 9 in trim would cut the text width to about 375 pt: diagrams 17 percent smaller, heavy code wrapping, pages +30 percent or more. Inner margin larger than outer for binding. |
| Body | TeX Gyre Pagella 10.5/13.8 pt, justified, microtype, widow/club/broken penalties 10000, `\raggedbottom`, paragraph spacing 0.55 em, no indent | Legible at 10.5 pt; ragged bottom avoids stretched pages around unbreakable code, figures and boxes. |
| Headings | TeX Gyre Heros bold in deep blue `#1F3D5C` (about 11:1 on white). Chapter: label "Chapter N" letter-spaced, title 25 pt, rule. Part opener: a page of its own, "Part N" 20 pt, title 38 pt, thick rule, list of the part's chapters with page numbers (built by the Lua filter from the headings). Tier headings (H2 "Beginner/Intermediate/Advanced tier"): 15.5 pt with a rule above. "In this project", "Try it", "Summary", "Further reading": 15 pt with a thin rule. "Learning objectives", "Prerequisites": 12 pt. H3 11.5 pt, H4 bold italic. | Clear hierarchy that separates tiers from sections. |
| Code | DejaVu Sans Mono at about 8.3 pt effective (was 7.65 pt), left rule 1.6 pt in the accent colour, no background, contrast theme unchanged. Wrap constant 96 to 86 columns for highlighted code (80 unhighlighted kept). | Larger code; 86 columns is 427 pt, inside the 443 pt available. |
| Callouts | Blockquotes (notes, "This is a design, not a deployment") and the sections "Learning objectives", "Summary", "Common mistakes" (H3), "A real incident" (H4) are set in a tinted breakable `tcolorbox` with a thick left rule. Sections holding a table are not boxed. | Meaning is carried by the words ("Note:", etc.), never by colour alone; tint `#EEF2F7` with black text is above 15:1. |
| Captions | Table and listing captions: bold sans blue, kept with their table or code (`\Needspace` and no page break). Figure: image, caption and "Text description" are kept together. "Path:" lines pulled up under the code and set small. Table header cells bold, table text 9.5 pt, zebra rows `#F1F4F8`. | All by Lua raw LaTeX around existing blocks; text unchanged (the Emph or Strong wrapper of a caption paragraph is dropped because the group font is already italic or bold). |
| Front matter | Half-title, blank verso, title page (tile motif drawn with rules, title, subtitle, author, edition), edition page (edition, licence, listing tag, fonts), contents with dot leaders and part titles as divider entries, roman numbers; arabic numbering restarts at 1 on the first page of Part I. | Edition, licence and tag come from `metadata.yaml` through the Lua filter (no duplicated values). |
| Contents | Chapter entries with dot leaders; a `\sdvnextpart` marker written to the .toc turns each Part entry into a bold divider line. | Works inside the tagged TOC. |
| Running heads | Verso: folio and chapter; recto: section and folio; first page of a chapter: folio at the foot; part openers and title pages: none. | |
| Glossary | Same table, 25/62/9 percent columns, small type, zebra rows, repeating header. | Table markup kept (best for tags). |
| Index | Two columns (`multicols`), entries as hanging lines without bullets, letter headings in blue. | veraPDF PDF/UA-2 passes with it. |
| HTML/EPUB | Same identity in `book.css`: serif and sans stacks, blue accent, tinted callouts (block quotes), zebra tables, code on a light background in both colour schemes (syntax colours stay at 5:1 or better), focus outline kept, `prefers-color-scheme: dark`, print stylesheet, nested contents indent. Title block with the tile motif (inline SVG, `aria-hidden`), edition line and licence footer in `book.html`. Cover: `cover.png` (1600 x 2560, navy, tile motif, white text, about 11:1) made by `make_cover.py`, passed with `--epub-cover-image` in `build.py`. | No stock art, logos or claims. |

## What was tested (preview builds only, never a full build)

Preview set: about, preface, all part introductions, Ch 1, 13, 24, 41, Appendix A, index (131 pages; the same set in the old design was 118 pages on A4 one-sided: +11 percent, including 5 new front-matter pages and 7 part openers).

- veraPDF `--flavour ua2` (docker `verapdf/cli`): PASS (0 failed rules, 1727 passed) on every iteration including the final one. Bookmarks: 203 in the preview set.
- Overflow scan (pymupdf, text beyond the recto or verso text block plus 2 pt): 0 pages.
- Pages inspected: title and front matter, part opener, Ch 1 opener and pages, Ch 13 (listings, tables, callouts), Ch 41 (figure, honesty box), glossary, index.
- HTML and EPUB were built from the preview manuscript with the same Pandoc flags; the HTML was opened in headless Chrome (light mode). Dark mode was not viewed (a forced-dark capture came out blank); its contrast is calculated only. epubcheck was not run by me.

## Needs a full build to verify

- Page count (estimate 850 to 870 pages against 747 today; the 15 percent budget is about 860).
- Long tables and all code blocks at the new width (the wrap constants were checked only on the preview set; run the right-edge scan on the full PDF).
- Part openers with many chapters (Parts IV to VII), the full contents (several pages), running heads on very long titles, the bookmark count (1,663 expected).
- EPUB with the cover (epubcheck) and the HTML in a browser, light and dark.

## Risks and notes

- Tagging: TeX Live 2026 builds chapter headings from kernel heading templates, not from `\@makechapterhead`, so the opener is set with `\EditInstance` (heading and headformat, chapter). The H1 stays one heading paragraph with its full text; the colon after "Chapter 2" is not printed in the opener (bookmarks and contents keep it). If the template keys change in a later TeX Live, the headings fall back to plain and the build still works.
- `tcolorbox`, `multicol`, colortbl row colours and rule drawings pass veraPDF, but veraPDF does not judge reading order; a human check of boxed sections with a screen reader is advisable.
- Include-in-header is added by Pandoc after the Lua filters, so the metadata values are emitted through `include-before` (typeset right after the emptied `\maketitle`).
- "Chap-ter" wraps in narrow table headers (automatic column width; text not changed).

## Decided not to do

- A 7 x 9 in trim, a two-column body, or `fvextra breaklines` (see above and the tagging constraint).
- "In this chapter" boxes: no existing markup gives a summary, and a generated list would repeat Learning objectives. Part openers do list their chapters (from the headings).
- Small caps (no true small caps in the fonts); boxes around Try it or Further reading (exercise text with code is better unboxed).
- Editing `metadata.yaml` (the author is "Claude (Anthropic)" as set by the owner).
