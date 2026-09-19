# Architecture blueprints v0 to v6

One blueprint per milestone tag, each derived from the code at that tag (file lists, endpoints
and class dependencies read with `git show <tag>:<path>`). Convention: solid arrows are calls or
requests; dotted arrows are "reads or writes data"; a node named after a class is that class.
No custom colors (STYLE.md section 13.1). Chapters copy the Mermaid block into their
"Architecture blueprint vN" section unchanged.

| Blueprint | Tag | Chapter | File |
|---|---|---|---|
| v0 | `book-m0-mvp` | 25 | `v0-mvp.md` |
| v1 | `book-m1-accounts` | 26 | `v1-accounts.md` |
| v2 | `book-m2-documents` | 27 | `v2-documents.md` |
| v3 | `book-m3-hardening` | 28 | `v3-hardening.md` |
| v4 | `book-m4-reading` | 29 | `v4-reading.md` |
| v5 | `book-m5-platform` | 30 | `v5-platform.md` |
| v6 | `book-m6-final` | 31 | `v6-final.md` |

Status: first pass (editor). v3 and v4 are derived from file-level diffs and are shallower than
the others. The writer-app checks each blueprint against the code when writing the chapter and
reports corrections through `requests.md`.
