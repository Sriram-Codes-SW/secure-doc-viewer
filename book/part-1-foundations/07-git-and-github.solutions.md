# Chapter 7 solutions

### Exercise 7.1 ★ Read the first commit

The oldest commit is `b6aef4e`, "Add secure document viewer: tiled rendering with signed URLs and per-viewer watermarking". It changed 31 files, all additions; most are under `src/`, chiefly `src/main/java/com/example/securedocviewer/`.

### Exercise 7.2 ★ Time travel without moving

`git show book-m0-mvp:pom.xml` and `git show book-m6-final:pom.xml`. At `book-m0-mvp` the property is `<java.version>21</java.version>`; at `book-m6-final` it is `25`. The Spring Boot parent also changes from 3.3.4 to 4.1.1.

### Exercise 7.3 ★★ Your own branch

`git switch -c exercise-7-3 book-m2-documents`, then `echo "hello" > notes.txt`, `git add notes.txt`, `git commit -m "Add notes"`. `git switch main` removes the file from the folder; `git switch exercise-7-3` brings it back, because the commit lives on that branch.

### Exercise 7.4 ★ Two commits in a scratch repository

```bash
mkdir scratch && cd scratch
git init
echo "one" > a.txt
git add a.txt
git commit -m "Add a.txt"
echo "two" >> a.txt
git diff
git add a.txt
git commit -m "Append a line"
git log --oneline
```

`git diff` shows `+two`; `git log --oneline` lists two commits.

### Exercise 7.5 ★★ Find when a file appeared

`git log --diff-filter=A --oneline -- Dockerfile` gives `2d10e07`, "Phase 5: Spring Boot 4 / Java 25, Docker stack, CI, and e2e tests", which is part of milestone 5 (`book-m5-platform`). `git log --diff-filter=A --oneline -- src/main/resources/db/migration/V2__documents_shares_audit.sql` gives `ba00693`, "Phase 2: document ownership, sharing, persistence, and audit", part of milestone 2 (`book-m2-documents`).

### Exercise 7.6 ★★★ Cause and resolve a conflict

One good answer: create `notes.txt` with a line, commit it on `main`, branch `a` and `b` from there, change that line differently on each, and commit. Merge `a` (fast-forward), then `git merge b`, which stops with a conflict. The file then contains `<<<<<<< HEAD` (start of the version from the branch you are on), `=======` (the divider) and `>>>>>>> b` (end of the incoming version from the other branch). Keep the correct text, delete all three marker lines, `git add notes.txt`, and `git commit`.
