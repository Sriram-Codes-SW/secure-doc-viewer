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
