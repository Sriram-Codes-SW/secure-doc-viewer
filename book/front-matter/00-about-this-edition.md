# About this edition

**Building a Secure Document Viewer: From First Line of Java to Production**

| Item | Detail |
|---|---|
| Author | Claude (Anthropic) |
| Edition | First edition, version 1.0 |
| Copyright and license | Text and figures: copyright 2026, all rights reserved. The code listings are excerpts of the Secure Document Viewer source code, which is released under the MIT License (the `LICENSE` file in the repository). |
| Code version | The repository tag `book-m6-final`; earlier milestones use the tags `book-m0-mvp` to `book-m5-platform` |
| Software versions | Java 25, Spring Boot 4.1.1, Spring Security 7, MySQL 8.4, Node 24, Angular 22, TypeScript 6.0, Docker Compose, Maven 3.9.16 (the first five milestones used Spring Boot 3.3.4 and Java 21) |
| Language | American English |

## What this edition is

This edition teaches you to build the Secure Document Viewer, a web application that shows PDF
documents without handing out the PDF, and to understand every decision behind it. Every listing
that is presented as the project's code is copied from the repository at the tag named in its
caption; code written only to teach is labeled as an example, and sketches of things the project
never built are labeled illustrative.

Part VII is a design study. The project never built or ran the application on a cloud platform, and
the chapters say so.

## How the book was made

The author is Claude, an AI model made by Anthropic. The book and the application were written
together with the project owner, who directed the work and reviewed it, and with further AI agents
that helped research, write, review, and check the chapters. The Acknowledgments in the preface say
more.

## Accessibility

The book is published as a PDF, an EPUB, and a single web page. All three contain real text, and every
diagram has alternative text and a text description in the body. In the EPUB and web editions the
difficulty stars on the exercises are labeled in words, and, in the web edition, code listings can be
reached and scrolled with the keyboard. In the PDF the stars are shown only as symbols, and the
meaning of one, two, and three stars is explained in How to use this book.

What was checked, on September 20, 2026: the PDF is a tagged PDF and passes the PDF/UA-2 check of
veraPDF 1.30.2. The EPUB passes epubcheck, and Ace by DAISY 1.4.6 reports no failures for it. The web
page passes the Nu HTML Checker 26.9.16 without errors. axe-core 4.13.0 reported no violations on 26
chapters of the EPUB and on a sample of the web page, Lighthouse 13.5.0 scored the sample 100 out of
100, and pa11y 10.0.0, run on 11 chapters and the sample, found no missing alternative text, heading,
language, or table problems. Some color-contrast checks on code listings and star symbols could not be
decided by these tools and were measured by hand instead; the checked code colors are at least 4.5 to
1 against their background. These tools were run on part of the book, not the whole book. The PDF
does not pass PDF/UA-1.

What was not checked: nobody has tested these editions with a screen reader or other assistive
technology, the PDF has not been checked with PAC or with the accessibility checker in Acrobat, and no
third-party audit has been made. This book does not claim conformance with WCAG, PDF/UA, or EPUB
Accessibility.

If you find a barrier, please report it as you would an error (see Corrections and updates below), and
say which edition and which page you were using.

## Corrections and updates

Software changes. Versions, screens, and documentation pages described here were checked on
September 20, 2026. Where a chapter cites documentation, check the current page before you rely on
a detail. If you find an error, report it to the author or the publisher of your copy.
