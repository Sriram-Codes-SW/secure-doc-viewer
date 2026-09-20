# Manual accessibility test script (free tools, 30 to 60 minutes)

Book: *Building a Secure Document Viewer*. Files under test: `secure-doc-viewer-guide.html`, `.epub`, `.pdf` (folder `book/build/out/`). Goal: find out whether a person who uses a screen reader, keyboard only, zoom or high contrast can read and navigate the book. This script does not prove conformance; it produces evidence. Record everything in the results table at the end.

Do not skip the recording. A "pass" you did not write down cannot be cited later.

## 0. Before you start

Pick a tester, a date, and write down: operating system and version, browser and version, screen reader and version. Have this script open on a second device or printed, because a screen reader will read your screen aloud.

Test locations (the same in every format, so results compare):

| Code | Location | Why |
|---|---|---|
| L1 | Front matter: "About this edition" (its Accessibility section) | Statement, key-value table with header row |
| L2 | The table of contents / navigation | Landmarks, depth, jump to chapter |
| L3 | Chapter 1 opener ("The big picture") | Heading levels, learning objectives list |
| L4 | Table 13.2 (Chapter 13, "A sample of the mapping (`GlobalExceptionHandler`)") | Table with a caption and header cells |
| L5 | Figure 41.5 (Chapter 41, "A migration order (moves A to E)...") | Figure alt text and the visible "Text description" |
| L6 | Listing 13.1 (Chapter 13, sign-in request record) | Code block, caption above, `Path:` line below |
| L7 | Exercise 13.1 (one star), 13.3 (two stars), and any three-star exercise | Star labels |
| L8 | A callout box such as a `Note:` or `Warning:` in the "Setting up your machine" chapter | Blockquote announced as a quote or note |
| L9 | Appendix A (glossary) | Definition list or table structure |
| L10 | The Index (last part of the book) | Long list, letter headings |
| L11 | A part opener, for example "Part II: The backend" | Navigation between parts |

What a pass looks like is given for each tool. If any item fails, record the exact words the screen reader spoke and the location.

## 1. NVDA with Firefox or Chrome on the HTML edition (Windows)

**Install and launch**
1. NVDA is a free, open-source screen reader from NV Access (project site: nvaccess.org). Download the installer from the official site, or run the portable copy. Start it with Ctrl+Alt+N. Stop it with NVDA key+Q (NVDA key is Insert or Caps Lock).
2. Use Firefox (mozilla.org) or Chrome (google.com/chrome). Use one; repeat with the other if time allows.
3. Open the HTML edition from disk (`File > Open File`). The page is very large (about 10 MB); allow it to load fully before testing. If it is too slow on your machine, say so and test the EPUB chapters instead.
4. Speech rate: set NVDA to a comfortable rate; turn on the Speech Viewer (NVDA menu > Tools > Speech Viewer) so you can copy what was said.

**Steps and what should be announced**
- **Page load**: title "Building a Secure Document Viewer"; language English. Press Tab once: the first stop is a link "Skip to main content". Press Enter: focus moves to the main content (pass: next Down Arrow reads the first content, not the table of contents).
- **Landmarks (D key)**: expect "navigation" (table of contents) and "main". Press D repeatedly; note any landmark without a name.
- **Headings (H key, 1 to 6)**: press 1 to jump through chapter titles. Pass: chapter names read in order, "heading level 1 Chapter 1: The big picture". Then press 2 in Chapter 1: "Learning objectives", "Prerequisites", "Beginner tier..." as level 2. Record any jump from level 1 to level 3.
- **L1**: go to the About page. Press T to move to the table. Pass: "table with 2 columns and N rows"; column headers "Item" and "Detail" announced when you move with Ctrl+Alt+Arrow keys.
- **L4**: press T until you reach Table 13.2. Pass: the caption "Table 13.2 - A sample of the mapping" is announced when the table is entered (as the table name, or just before it); header cells are announced when moving across columns (Ctrl+Alt+Right/Left/Up/Down). Record whether every cell reads with its column name.
- **L5**: press G (graphics) to reach Figure 41.5. Pass: the alt text is read (it is the text description: it describes the order of moves A to E, not just "diagram"). Then read the caption and the "Text description" paragraph below. Record whether the alt text and the visible description duplicate each other in a way that is confusing.
- **L6**: press Down Arrow through Listing 13.1. Pass: the caption is read before the code; code reads line by line; symbols are read as you expect when punctuation level is set to "all" (Insert+P). The `Path:` line follows the code and names the file.
- **L7**: press H to reach "Exercise 13.1", "Exercise 13.3". Pass: "one star", "two stars", "three stars" are read (not "black star" repeated).
- **L8**: press Q (quote) to jump to callouts. Pass: the announcement gives "block quote" and the reader hears the word "Note" or "Warning" at the start.
- **L9 and L10**: in the glossary and index, use K (links) or the elements list (NVDA+F7). Pass: letters (A, B, C...) are headings, the terms are reachable without reading hundreds of entries.
- **Links**: open the Elements List (NVDA+F7), Links tab. Pass: link texts make sense out of context. Record any "here", "link", or a bare file name.
- **Cross-references** ("see Chapter 5"): they are plain text in this edition and are not links. Record whether that blocks a task (you cannot jump to the chapter).

**Record**: Speech Viewer text for L4, L5, L6, L7 and the first 5 headings; any failure; time taken to reach Chapter 13 from the start using headings only.

## 2. NVDA and Narrator on the EPUB (Windows)

**Install and launch**
- Thorium Reader is a free EPUB reader with accessibility features (project site: edrlab.org/software/thorium-reader; releases on the project's GitHub page). Install it and open `secure-doc-viewer-guide.epub`.
- Narrator is built into Windows: Ctrl+Windows+Enter to start or stop. Use NVDA (section 1) as the main reader and Narrator as a second opinion.
- The EPUB is also readable in Adobe Digital Editions (free) or Calibre's viewer (free); use one of them if Thorium fails.

**Steps**
- **Metadata**: in Thorium, open the book information panel. Pass: the title, language English, and (in the publication details or "accessibility" panel) the accessibility metadata: access mode textual and visual, features (alternative text, reading order, structural navigation, table of contents), hazard none, and the summary. Record what Thorium shows; record if the summary is missing.
- **Contents (L2)**: open the table of contents panel. Pass: nested entries to chapter and section level; selecting one moves to that place.
- **Reading (L1, L3)**: turn on NVDA, and read the first pages. Pass: language is English, headings are announced with levels.
- **L4, L5, L6, L7, L8, L9, L10**: navigate as in section 1 (NVDA Browse mode keys: H, T, G, Q). Record every difference from the HTML result.
- **Page navigation**: the book is long (chapters are separate files in the EPUB); test moving to the next and previous chapter.
- **Text settings**: increase the font size several steps, change the font to a dyslexia-friendly font, switch to high-contrast, night and sepia themes in Thorium's display settings. Pass: text reflows, the code blocks scroll or wrap, tables remain readable, no text is cut off.

**Record**: NVDA output for L4, L5, L7; the metadata panel content; any missing images; reflow at the largest font size.

## 3. Adobe Acrobat Reader (free), Read Aloud, and NVDA on the PDF

**Install and launch**
- Adobe Acrobat Reader is free from Adobe (adobe.com). Open `secure-doc-viewer-guide.pdf`. The PDF is about 750 pages and 12 MB; opening takes some seconds.
- NVDA (section 1) reads PDFs in Acrobat Reader in browse mode when the PDF is tagged.

**Steps**
- **Document properties**: File > Properties. Pass: title "Building a Secure Document Viewer", language English (Advanced tab: Language), "Tagged PDF: Yes", initial view shows the document title in the title bar.
- **Bookmarks (L2, L11)**: open the Bookmarks pane. Pass: nested chapters; a click moves to the chapter.
- **Read Aloud**: View > Read Out Loud > Activate Read Out Loud, then Ctrl+Shift+V (page) or Ctrl+Shift+B (to end of document). Read page 1 of Chapter 1 and a page with a table. Pass: text is read in order, headers and footers are not interrupting each paragraph, and the table is read row by row.
- **NVDA in Acrobat**: press H to jump to headings; T for tables; G for graphics. Repeat L4 to L10. Pass: as in section 1: alt text on figures, table header announcement, code readable. Record where NVDA says "unlabelled graphic", "blank" or repeats page headers.
- **Reading order tool** (Acrobat Reader has the Reading Order in Accessibility panel only in Pro; skip if unavailable; note it).
- **Zoom**: View > Zoom to 400 percent and use Reflow (View > Zoom > Reflow) when tagged. Pass: the text reflows; tables and code lines are still readable, no text is cut off at the right margin.
- **High contrast**: Edit > Preferences > Accessibility > "Replace Document Colors", choose "Use High-Contrast colors". Pass: text stays readable, diagrams are visible.

**PAC 2024 (free, Windows; a person must run it)**
1. Download PAC 2024 (PDF Accessibility Checker) from the official site of its publisher, the Swiss foundation "Access for All" (accessibility.org), free of charge.
2. Open the PDF (drag it in). It runs a machine check for PDF/UA and WCAG, and shows a report per category (document, page content, forms, alternative text, tables, lists, headings, fonts).
3. Use the "Screen reader preview" to see the reading order, and "Logical structure" to check every heading, figure and table.
4. Save the report (PDF) and record the number of errors, warnings and each error text. Note that PAC checks PDF/UA-1 rules as well as the newer standard; the 421 link annotations without `/Contents` in this PDF are expected to be reported by UA-1 checkers.

## 4. VoiceOver (macOS and iOS): Books, Preview, Safari

**Launch**
- macOS: Cmd+F5 starts VoiceOver; VoiceOver Utility and the built-in tutorial are in System Settings > Accessibility. The VoiceOver key ("VO") is Control+Option.
- iOS: Settings > Accessibility > VoiceOver (or triple-click the side button after enabling the shortcut).

**HTML in Safari (macOS)**: open the HTML file. Use the Rotor (VO+U) to list Headings, Landmarks, Links, Tables, Form controls (none expected). Pass: the Rotor lists chapters and sections in order, and the "Skip to main content" link works. Move to L4 with VO+Right Arrow: the table is announced with row and column numbers, and header names as you move across cells (VO+Command+Arrow keys in table navigation). L5: images are announced with their alt text.

**EPUB in Books (macOS and iOS)**: import the EPUB. Pass: the book opens, the contents list works, VoiceOver reads text and images with alt text; the accessibility metadata may appear in the book's information. Record whether Books shows the "Accessibility" information for the book (recent versions show accessibility details from the metadata).

**PDF in Preview (macOS)**: open the PDF. Pass: VoiceOver reads text in order, bookmarks (sidebar) work, figures are announced with their alt text. Preview's support for tagged PDF structure is limited: record what happens with tables. This is a known weaker environment than Acrobat.

**iOS**: open the EPUB in Books with VoiceOver on; use the Rotor set to Headings. Swipe through L3 and L7. Record if "one star", "two stars" are read.

## 5. TalkBack (Android)

- Enable TalkBack in Settings > Accessibility > TalkBack (Google's screen reader, built into Android). Use "Reading controls" (swipe up then right) to move by headings, links, controls.
- Open the EPUB in a reader that supports it, for example Thorium Reader for Android or Google Play Books (free). Open the HTML file in Chrome for Android.
- Pass: headings are reachable with the reading control "Headings"; figure alt text is read; the table is announced with row and column; code lines read.
- Record device, Android version, reader app and version.

## 6. Keyboard-only navigation (no mouse, no screen reader)

Do this for the HTML edition in a normal browser.
- Press Tab from the top of the page. Pass: the first focus is the skip link, visible when focused (a visible label and outline of at least 3 px).
- Tab through the table of contents: each link shows a visible focus outline. Press Enter on a link: the page jumps to the section. Record if focus is lost.
- Use the browser's find (Ctrl+F) to reach L4 and press Tab: the focus ring is visible and never hidden behind another element.
- Pass criteria: no keyboard trap, every link is reachable and shows a focus indicator, and no action needs a mouse. The book has no forms, buttons or scripts; report any that exist.

## 7. Zoom and reflow (400 percent)

HTML and EPUB, in the browser or in Thorium. Set the browser window to 1280 pixels wide, then zoom to 400 percent (Ctrl and +, or Ctrl and scroll). This is equal to a width of 320 CSS pixels.
- Pass: content reflows to a single column; no horizontal scrolling for text; code blocks and wide tables may scroll inside their own box (allowed), but the rest of the page does not scroll sideways; nothing overlaps.
- Look at L4 (table), L5 (figure), L6 (code), L10 (index).
- Record which content requires horizontal scrolling and how much.
- For the PDF, zoom in Acrobat to 400 percent and try Reflow; PDFs are not required to reflow the way web pages do, but a tagged PDF should reflow (record what you see).
- Text spacing: use a browser extension that sets the line height to 1.5 times the font size, paragraph spacing to 2 times, letter spacing 0.12 em and word spacing 0.16 em (for example the free "Text Spacing Editor" bookmarklet). Pass: no text is cut off or overlaps.

## 8. High contrast and dark mode

- **Windows contrast themes**: Settings > Accessibility > Contrast themes > "Night sky" or "Desert". Open the HTML edition in Firefox or Edge and the EPUB in Thorium. Pass: all text remains visible; code blocks keep readable text; the diagrams' text is visible (the diagrams are images with a light background; a transparent background would fail: record it); links are still distinguishable.
- **Dark mode**: turn on the operating system dark mode and the browser's forced dark mode ("Auto Dark Mode" flag in Chrome, or the Dark Reader extension in Firefox). Pass: no text disappears into its background, especially code and the callout boxes.
- **Colour-blind check**: use the browser's built-in vision-deficiency emulation (Chrome DevTools > Rendering > Emulate vision deficiencies). Pass: no information is carried only by colour (syntax colours in code are decoration only).
- **Print**: not an accessibility requirement here.

## 9. Results table template

Copy this table into a results file and fill one row per tester per check. Use Pass, Partial, Fail, or N/A, and add the exact words or a screenshot reference for anything that is not a Pass.

| Date | Tester | OS / device | Tool and version | Format | Location | Check | Result | What was announced or seen | Severity (Blocker, Major, Minor) |
|---|---|---|---|---|---|---|---|---|---|
| | | | NVDA x.y + Firefox z | HTML | Load | Skip link, title, language | | | |
| | | | | HTML | L2 | Landmarks and headings | | | |
| | | | | HTML | L4 | Table caption and header announcement | | | |
| | | | | HTML | L5 | Figure alt text, text description | | | |
| | | | | HTML | L6 | Code and Path line | | | |
| | | | | HTML | L7 | Star labels | | | |
| | | | | HTML | L8 | Callouts | | | |
| | | | | HTML | L9, L10 | Glossary and index | | | |
| | | | NVDA + Thorium | EPUB | Metadata | Accessibility metadata visible | | | |
| | | | | EPUB | L2 to L10 | As HTML | | | |
| | | | Acrobat + NVDA | PDF | Properties | Tagged, language, title | | | |
| | | | | PDF | L4 to L10 | Tables, alt text, code | | | |
| | | | PAC | PDF | Whole file | Errors and warnings | | | |
| | | | VoiceOver + Safari | HTML | L4, L5 | Table, image | | | |
| | | | VoiceOver + Books | EPUB | L3, L7 | Headings, stars | | | |
| | | | TalkBack | EPUB/HTML | L3, L7 | Headings, stars | | | |
| | | | Browser | HTML | All | Keyboard only | | | |
| | | | Browser | HTML/EPUB | L4 to L6 | 400 percent zoom | | | |
| | | | Windows contrast theme | HTML/EPUB | All | High contrast and dark | | | |

## 10. How to report

After the run, list each failure with the location code (L1 to L11), the format, the tool, and the words heard. A failure that appears in every screen reader is probably in the source or the build; one that appears in one reader only may be that tool's limitation: say which.

## 11. What a full pass does and does not show

A run of this script by one person with these tools gives evidence of usability in those tools. It does not replace testing by disabled readers, a third-party audit, or a formal conformance evaluation, and it must not be described as one.
