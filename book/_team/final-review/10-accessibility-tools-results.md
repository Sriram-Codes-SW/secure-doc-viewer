# Accessibility tools: results and interpretation

Date of runs: 2026-09-20 (evening, IST). Files tested: `book/build/out/secure-doc-viewer-guide.pdf` (825 pages, written 22:24), `.epub` (written 21:57, 12.1 MB), `.html` (written 21:58, 19.4 MB). One heavy process at a time. Scratch: `book/build/out/a11y-scratch/` (`tools/`, `ace/`, `epub5/` = unzipped EPUB, `html-sample.html`, raw JSON outputs).

Companion file: `10-screen-reader-test-script.md` (manual test with free screen readers). No screen reader was run for this document.

## Summary

| Tool | Version | Ran? | Target | Result |
|---|---|---|---|---|
| veraPDF (docker `verapdf/cli`) | 1.30.2 | Yes | PDF | PDF/UA-2: **PASS**. PDF/UA-1: 102 rules passed, 4 failed (see below; expected for a PDF 2.0 file). |
| epubcheck via `npx epubchecker` | wrapper 5.2.1 (bundled epubcheck version not confirmed) | Yes | EPUB | "Everything is fine" (0 errors, 0 warnings reported). |
| Nu HTML checker (docker `ghcr.io/validator/validator`) | 26.9.16 | Yes | HTML edition and 61 EPUB XHTML files | HTML edition: 0 errors, 0 warnings, 168 info messages. EPUB XHTML: 67 errors, all the same false positive (`epub:type`). |
| axe-core 4.13.0 (puppeteer-core 25.11.0, host Chrome) | 4.13.0 | Yes | 24 EPUB XHTML files, one at a time, plus a sample of the HTML edition | 1 real WCAG failure type (keyboard access to scrollable code blocks); best-practice landmark rules; contrast "needs review" items are false positives. |
| pa11y | 10.0.0 (runners htmlcs and axe, standard WCAG2AA) | Yes | 5 EPUB XHTML files and the HTML sample | HTML CodeSniffer: 0 real issues; axe runner: the same scrollable-region and contrast items as above. |
| Lighthouse (accessibility category) | 13.5.0 | Yes, on a split-out page | HTML sample (About page, Chapters 13 and 41, Appendix A, the Index, with the page header and contents list) | Score 100; 0 failed audits. |
| Ace by DAISY | 1.4.6 (`ace-puppeteer` runner) | Yes, after two attempts in total (first with the Electron runner failed to install; second with the Puppeteer runner and host Chrome worked) | EPUB | Outcome "fail": 185 findings of 2 kinds (see below). Metadata check: no `dcterms:conformsTo` or `certifiedBy`. |
| PAC 2024 | n/a | **No, cannot be run by an automated agent** | PDF | A person must run it; steps are in the test script, section 3. |
| Screen readers (NVDA, VoiceOver, TalkBack), Acrobat | n/a | **No** | all | Not run. Test script written. |

## veraPDF

- `--flavour ua2 --format text`: PASS.
- `--flavour ua1 --format json`: 102 passed, 4 failed rules: clause 5 (1 check, `pdfuaid:part` must be 1), 6.1 (1 check, file header must be `%PDF-1.n`), 7.18.1 and 7.18.5 (436 checks each: link annotations without `/Contents`). The first two are because the file is PDF 2.0 declaring PDF/UA-2; the last two are UA-1 requirements that UA-2 handles through Link structure elements. Interpretation: UA-1 does not apply to this file; do not claim UA-1.
- Own scan (pymupdf): `StructTreeRoot`, `Lang en-US`, 1,663 bookmarks. Not re-measured in this pass: figure `/Alt` and table/TH counts (81 and 130/379 in the previous pass at 747 pages; the PDF is now 825 pages, so numbers may have changed).
- Cannot cover: whether the tags give a sensible reading order on every page, whether every figure `/Alt` is good, and how real assistive technology behaves.

## epubcheck

"Everything is fine". Covers EPUB 3 packaging, XHTML validity, navigation document, links between files and fragment identifiers. It does not judge accessibility beyond the package metadata syntax.

## Nu HTML checker

- HTML edition (19 MB, run with 1.5 GB heap): no errors and no warnings. 168 info messages: 86 "trailing slash on void elements has no effect", 81 "the `img` role is unnecessary for element `img`" (redundant `role="img"` on figure images: harmless but can be removed), 1 "type attribute for style is not needed". Also verified by own script: 6,059 internal links, 0 broken targets, 0 duplicate ids.
- EPUB XHTML (61 files): 67 errors, all "Attribute `type` not allowed on element `body`/`nav`/`section`/`a`". These are **false positives**: `epub:type` is valid in EPUB but the Nu checker validates as plain HTML5 without the EPUB namespace. No other messages.

## axe-core (24 EPUB XHTML files, one at a time)

Files: title page, nav document, About page, Preface, How to use this book, Setting up your machine, the Part I, II, III, IV, V, VI and VII openers, Chapters 1, 13, 24, 37 (ch046), 30 (ch038), 41, the Epilogue, Appendix A, Appendix B, Appendix C, and the Index. Rules: WCAG 2.0/2.1/2.2 A and AA plus best practice.

| Rule | Impact | WCAG | Nodes | Files | Verdict |
|---|---|---|---|---|---|
| `scrollable-region-focusable` | serious | 2.1.1 (Keyboard) | 30 | 7 of 24 | **Real.** Long code lines make `div.sourceCode` scroll horizontally and the region cannot be reached by keyboard. See A11Y-T1. |
| `region` | moderate | best practice | 1,903 | 23 | Not a WCAG failure. XHTML chapter files have no landmarks by design; the reading system supplies the structure. Ignore for EPUB. |
| `landmark-one-main` | moderate | best practice | 24 | 24 | Same: EPUB content documents do not carry `<main>`; the HTML edition does. Ignore for EPUB. |

No violations for: image alternative text, table headers, heading order, page language, link names, ARIA roles, duplicate ids, colour contrast (see next).

**Incomplete ("needs review") colour contrast** (10 files): axe could not decide because "the element's background color could not be determined because it is partially obscured by another element" (code line-number overlays in code blocks) and "element content contains only non-text characters" (the star glyphs inside `role="img" aria-label="..."` spans). False positives for this book: I measured the theme and PDF colours directly (all at least 4.99:1, see 06-recheck-accessibility.md). axe reports 0 contrast violations.

HTML edition, sample page (About page, Chapter 13, Chapter 41, Appendix A, Index; the full 19 MB page is too heavy for one browser run): the same single violation (`scrollable-region-focusable`, 6 nodes) and 17 contrast items in "needs review" (same causes). The skip link, `<main>`, `<nav role="doc-toc">`, page language and title were accepted.

## pa11y (htmlcs and axe runners)

- EPUB files ch001 (About), nav, ch019 (Chapter 13), ch031 (Chapter 24), ch052 (Chapter 41): 0 to 25 items each, all `scrollable-region-focusable` and the contrast "needs review" items (pa11y reports axe's incomplete items as errors); HTML CodeSniffer reported nothing.
- HTML sample: 610 items: 587 `NoSuchID` from HTML CodeSniffer, which are **false positives caused by the sample** (the contents list links to chapters I cut out; in the full HTML file 6,059 internal links have 0 missing targets), plus the same 17 contrast and 6 scrollable items.

## Lighthouse

Lighthouse 13.5.0, accessibility category only, on `html-sample.html` served from a local Python HTTP server (Lighthouse cannot audit `file://` URLs): score 100 of 100, no failed audits. Lighthouse's accessibility audits are a subset of axe's; a perfect score is not a conformance result. Not run on the full page (19 MB, too heavy for this machine).

## Ace by DAISY (EPUB)

Ace 1.4.6, runner `ace-puppeteer` with the host Chrome (first attempt earlier in the day with the Electron runner failed because Electron would not install; second attempt in this pass succeeded). Outcome: **fail**, 185 assertions in 2 kinds:

| Rule | Impact | Count | Files | Verdict |
|---|---|---|---|---|
| `scrollable-region-focusable` | serious | 184 | 42 of 61 | **Real**, same as axe (Ace runs all files, so the count is complete). |
| `empty-table-header` | minor | 1 | ch009 (Chapter 4, Table 4.1 "Class or record") | **Real.** The first header cell is empty: `<th scope="col"></th>`. Source: `part-1-foundations/04-classes-and-objects.md` line 280, `\| \| Class \| Record \|`. |

Metadata check (Ace): present: accessMode, accessModeSufficient, accessibilityFeature, accessibilityHazard, accessibilitySummary. Missing: `dcterms:conformsTo`, `a11y:certifiedBy`, `a11y:certifierCredential`, `a11y:certifierReport` (expected: no conformance is claimed and no certifier exists).

## Findings from the tools

**A11Y-T1 (Major, build) Code blocks are not keyboard-reachable when they scroll.** WCAG 2.1.1. 184 code blocks in 42 EPUB files (Ace), 6 in the HTML sample, and the same construct in the full HTML edition. Suggested fix (coordinator, CSS): either make code wrap (`pre, div.sourceCode { overflow: visible } pre code { white-space: pre-wrap; overflow-wrap: anywhere }`), or add `tabindex="0" role="region" aria-label="Code"` to each `div.sourceCode` with a Lua filter (this makes the region focusable and gives it a name; the focus outline is already defined). Wrapping is simpler and also helps the 400 percent zoom case.

**A11Y-T2 (Minor, content) Empty header cell in Table 4.1.** Write `| Aspect | Class | Record |` (or `| Property | ...`) in `part-1-foundations/04-classes-and-objects.md` line 280.

**A11Y-T3 (Minor, build) 81 redundant `role="img"` on `img` elements** (Nu info). Optional clean-up.

No tool found a missing alt text, heading-order, language, table-header (other than T2) or link-name failure. Tools not run (screen readers, PAC) may find more.

## False positives, real findings

- False positives: Nu `epub:type` (67), pa11y `NoSuchID` on the sample (587), axe/pa11y contrast "needs review" (obscured code overlay, star glyph), axe `region` and `landmark-one-main` on EPUB files, veraPDF UA-1 header rules on a PDF 2.0 file.
- Real: keyboard access to scrollable code blocks (T1), the empty header cell (T2), UA-1 link `/Contents` (only if UA-1 is required).

## What the tools cannot cover

Whether alt text and text descriptions are correct and helpful (77 figure descriptions were only sampled); whether the reading order is right on every PDF page; whether tables are announced correctly by real screen readers; usability for people with cognitive, motor or visual disabilities; behaviour in reading systems (Thorium, Apple Books, Kindle, Kobo); PDF/UA conformance beyond the machine-checkable rules (PAC 2024 and Acrobat's checker were not run); plain-language quality (the sentence-length statistics say 462 sentences are over 35 words); content that only a person can judge (the analogies, the pacing). Automated tools are usually said to find only a minority of the possible barriers.

## What can and cannot be claimed

Can be said (each verified on 2026-09-20): the PDF is tagged and passes veraPDF's PDF/UA-2 rules; the EPUB passes epubcheck; the HTML edition passes the Nu HTML checker without errors; axe, pa11y and Lighthouse found no failure of alternative text, headings, language, table structure or contrast on the sampled pages; every figure has alternative text and a text description; exercise stars are labeled in words in the EPUB and HTML.

Cannot be said: that the book "is accessible", "conforms to WCAG 2.2 AA", "is PDF/UA compliant", "is EPUB Accessibility 1.1 conformant" (the EPUB has no `conformsTo` and Ace's outcome is fail until T1 is fixed), or that it was tested with assistive technology. Do not add `dcterms:conformsTo` or a certification statement without a person's evaluation.

## Proposed replacement text for the About page (`front-matter/00-about-this-edition.md`, section "## Accessibility", replaces lines 34 to 41)

Use this version if T1 is **not** fixed in the final build:

```
The book is published as a PDF, an EPUB, and a single web page. All three contain real text, and every
diagram has alternative text and a text description in the body. In the EPUB and web editions the
difficulty stars on the exercises are labeled in words; in the PDF they are shown only as symbols, and
the meaning of one, two, and three stars is explained in How to use this book.

What was checked, on September 20, 2026: the PDF is a tagged PDF and passes the PDF/UA-2 check of
veraPDF 1.30.2. The EPUB passes epubcheck. The web page passes the Nu HTML Checker 26.9.16 without
errors. axe-core 4.13.0, pa11y 10.0.0, and Lighthouse 13.5.0 found no missing alternative text,
heading, language, table, or color-contrast failures on the pages they were run on (chapters of the
EPUB and a sample of the web page, not the whole book). Ace by DAISY 1.4.6 reports one known problem in
the EPUB and web editions: long lines in code listings scroll sideways, and that scrolling can't be
reached with the keyboard alone. The PDF does not pass PDF/UA-1.

What was not checked: nobody has tested these editions with a screen reader or other assistive
technology, the PDF has not been checked with PAC or Acrobat's accessibility checker, and no
third-party audit has been made. This book does not claim conformance with WCAG, PDF/UA, or EPUB Accessibility.

If you find a barrier, please report it as you would an error, using the address given under
Corrections and updates below, and say which edition and which page you were using.
```

Use this shorter version if T1 **is** fixed and Ace is rerun with no failures (edit the sentence about Ace to say "Ace by DAISY 1.4.6 reports no failures for the EPUB", and rerun before publishing to be sure):

```
What was checked, on September 20, 2026: ... Ace by DAISY 1.4.6 reports no failures for the EPUB.
```

Notes on wording: the phrase "does not claim conformance" replaces any "accessible" wording; the numbers of versions and the date are needed, because the results are only true for these files and tool versions. "Please report ... using the address given under Corrections and updates" assumes that section names an address; if it does not, name one there (the current section says "report it to the author or the publisher of your copy").

## Not done

PAC 2024; Acrobat Accessibility Checker; screen reader sessions; full-page Lighthouse and axe on the 19 MB HTML; axe on all 61 XHTML files (24 sampled; Ace covered all 61); re-count of PDF structure elements at 825 pages.

---

# Final files (built 2026-09-20: EPUB 23:20, HTML 23:21, PDF 23:43; PDF 825 pages)

Runs made after the T1, T2 and T3 fixes, one tool at a time, on exactly these files. Scratch: `book/build/out/a11y-scratch/` (`epub6/` = this EPUB unzipped, `html-sample.html` rebuilt from this HTML with the About page, Chapters 4, 13 and 41, Appendix A and the Index).

| Tool | Version | Target | Result |
|---|---|---|---|
| veraPDF | 1.30.2 | PDF | PDF/UA-2: **PASS**. PDF/UA-1: 102 passed, 4 failed (header and `pdfuaid:part`, 1 check each; 436 link annotations without `/Contents`, clauses 7.18.1 and 7.18.5). Own scan: `StructTreeRoot`, `Lang en-US`, 1,663 bookmarks; 81 of 81 Figure elements have `/Alt`. |
| epubcheck | npm `epubchecker` wrapper 5.2.1 | EPUB | "Everything is fine". |
| Ace by DAISY | 1.4.6 (`ace-puppeteer`, host Chrome) | EPUB, 64 documents | Outcome **pass**, 0 findings (was fail with 185). Metadata present: accessMode, accessModeSufficient, accessibilityFeature, accessibilityHazard, accessibilitySummary. Absent (expected): `conformsTo`, `certifiedBy`, `certifierCredential`, `certifierReport`. |
| Nu HTML checker | 26.9.16 | HTML edition (19.4 MB) | 0 errors, 0 warnings; 87 info messages (86 void-element slashes, 1 style `type`). The 81 redundant `role="img"` messages are gone. |
| Nu HTML checker | 26.9.16 | 61 EPUB XHTML files | 67 messages, all `epub:type` "not allowed" false positives (the checker knows no EPUB namespace); nothing else. |
| axe-core | 4.13.0 (puppeteer-core 25.11.0, host Chrome) | 26 EPUB files, one at a time: title page, nav, About, Preface, How to use, Setup, all seven part openers, Chapters 1, 3, 4, 13, 24, 30, 37, 41, Epilogue, Appendices A, B, C, Index | **0 WCAG violations.** Only the best-practice rules `landmark-one-main` (26) and `region` (2,604 nodes in 25 files), which do not apply to EPUB content documents. `scrollable-region-focusable`: gone (was 30 nodes in 7 files). 12 files list colour-contrast as "needs review". |
| axe-core | 4.13.0 | HTML sample | **0 violations**, 30 colour-contrast items "needs review". |
| pa11y | 10.0.0 (htmlcs and axe runners, WCAG2AA) | 11 EPUB files (About, How to use, Chapters 4, 13, 24, 30, 41, Appendices A and C, Index, nav) and the HTML sample | HTML CodeSniffer: nothing real. The axe runner lists the same "needs review" contrast items as errors (up to 248 in Appendix C, 30 in the sample). The sample also gives 577 `NoSuchID` (artifact of cutting the page; see below). |
| Lighthouse | 13.5.0 | HTML sample (over local HTTP) | Accessibility **100**; 0 failed audits. |

The colour-contrast "needs review" items were classified (axe `messageKey`) on Appendix C, Chapter 30, Chapter 24 and the sample: all are `nonBmp` (star glyphs inside labelled spans: no text to measure), `elmPartiallyObscured` or `elmPartiallyObscuring` (line-number overlays in code blocks). No contrast violation was reported. Own measurements: PDF text colours at least 4.99:1 on the code background, HTML syntax theme at least 5.05:1.

`NoSuchID` on the HTML sample is caused by cutting the page (the contents list points at chapters not in the sample). The whole HTML was checked in the previous pass: 6,059 internal links, 0 broken; not re-run on this build.

Verified counts on these files: EPUB and HTML `<pre>` blocks 522 of 522 with `tabindex="0"`; 0 `role="img"` on `img`; 81 of 81 images with alt; PDF 81 of 81 Figure elements with `/Alt`; 84 "Text description" lines; stars 167 "one star", 199 "two stars", 126 "three stars", 0 stray; Table 4.1 has no empty header cell (Ace reports no `empty-table-header`).

## Sentence-by-sentence check of the printed Accessibility statement (`front-matter/00-about-this-edition.md`)

| # | Sentence (start) | True as written? | Evidence or fix |
|---|---|---|---|
| 1 | "The book is published as a PDF, an EPUB, and a single web page." | True. | Three files. |
| 2 | "All three contain real text, and every diagram has alternative text and a text description in the body." | True. | 81 of 81 images with alt in EPUB and HTML, 81 of 81 PDF Figure elements with `/Alt`, 84 description lines. Quality of the alt text was sampled, not fully reviewed; the sentence does not claim quality. |
| 3 | "In the EPUB and web editions the difficulty stars on the exercises are labeled in words," | True. | 167 / 199 / 126, none unlabelled. |
| 4 | "and code listings can be reached and scrolled with the keyboard." | **True for the web edition in a browser; not shown for EPUB reading systems.** | 522 of 522 `pre` have `tabindex="0"`; axe and Ace report no `scrollable-region-focusable`. Reading systems (Thorium, Apple Books, Kindle) may handle `tabindex` differently, and no reader was run. Replacement below. |
| 5 | "In the PDF the stars are shown only as symbols, and the meaning of one, two, and three stars is explained in How to use this book." | True. | PDF headings extract as star glyphs. |
| 6 | "the PDF is a tagged PDF and passes the PDF/UA-2 check of veraPDF 1.30.2." | True. | PASS. |
| 7 | "The EPUB passes epubcheck, and Ace by DAISY 1.4.6 reports no failures for it." | True. | Both clean. |
| 8 | "The web page passes the Nu HTML Checker 26.9.16 without errors." | True. | 0 errors, 0 warnings. |
| 9 | "axe-core 4.13.0, pa11y 10.0.0, and Lighthouse 13.5.0 found no missing alternative text, heading, language, table, or color-contrast failures on the pages they were run on, which were chapters of the EPUB and a sample of the web page, not the whole book." | **Partly.** True for axe and Lighthouse. Not literally true for pa11y: pa11y prints color-contrast "errors" on 7 of its 12 targets (they are axe "needs review" items, not confirmed failures) and 577 `NoSuchID` errors on the sample (an artifact). Also some code-block and star contrast checks could not be decided automatically. | Replacement below. |
| 10 | "The PDF does not pass PDF/UA-1." | True. | 4 rules fail. |
| 11 | "nobody has tested these editions with a screen reader ..., the PDF has not been checked with PAC or with the accessibility checker in Acrobat, and no third-party audit has been made." | True as far as this review knows. | None run here; the author should confirm nobody else has. |
| 12 | "This book does not claim conformance with WCAG, PDF/UA, or EPUB Accessibility." | True (and prudent). | No `conformsTo` in the OPF. |
| 13 | "If you find a barrier, please report it as you would an error (see Corrections and updates below), and say which edition and which page you were using." | True as written. | The section it points to says "report it to the author or the publisher of your copy" and gives no address; consider adding one. |

### Exact replacement wording for sentences 4 and 9

Sentence 4 (replace "and code listings can be reached and scrolled with the keyboard"):

```
and, in the web edition, code listings can be reached and scrolled with the keyboard.
```

Sentence 9 (replace from "axe-core 4.13.0" to "not the whole book."):

```
axe-core 4.13.0 reported no violations on 26 chapters of the EPUB and on a sample of the web page,
Lighthouse 13.5.0 scored the sample 100 out of 100, and pa11y 10.0.0, run on 11 chapters and the
sample, found no missing alternative text, heading, language, or table problems. Some color-contrast
checks on code listings and star symbols could not be decided by these tools and were measured by hand
instead; the checked code colors are at least 4.5 to 1 against their background. These tools were run
on part of the book, not the whole book.
```

("At least 4.5 to 1" is true: measured 4.99:1 or better in the PDF and 5.05:1 or better in the HTML theme; the EPUB has no syntax colors. If you also want to mention that pa11y lists the undecided checks as errors, add: "pa11y lists those undecided checks as errors.")

## Outcome

All tools ran on the final files. The three fixes worked: Ace changed from fail (185 findings) to pass (0), axe found no violations on 26 EPUB files and the HTML sample, and Lighthouse scored 100. Every sentence of the printed statement is true except two that are stronger than the evidence: sentence 4 (keyboard access shown only for the web edition in a browser) and sentence 9 (pa11y's output contains contrast errors that are only "needs review" items). With the two replacements above the statement matches what was verified.

Still not covered: screen readers, PAC, the Acrobat checker, full-page Lighthouse and axe on the 19 MB HTML, axe on 35 of 61 EPUB files (Ace covered all 61), figure description quality beyond a sample, and any reading-system testing of the EPUB.

Note on coverage: the request was at least 20 EPUB files for axe and pa11y. axe covered 26 files; pa11y covered 11 files (it is much slower) plus the HTML sample, so pa11y's coverage is below the requested 20. The replacement wording for sentence 9 states the real numbers (26 for axe, 11 for pa11y).
