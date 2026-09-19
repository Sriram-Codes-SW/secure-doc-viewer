# Chapter 7 solutions

### Exercise 7.1 ★ Read the first commit

The oldest commit is `b6aef4e`, "Add secure document viewer: tiled rendering with signed URLs and per-viewer watermarking". Most of its files are under `src/`, chiefly `src/main/java/com/example/securedocviewer/`.

### Exercise 7.2 ★ Time travel without moving

`git show book-m0-mvp:pom.xml` and `git show book-m6-final:pom.xml`. At `book-m0-mvp` the property is `<java.version>21</java.version>`; at `book-m6-final` it is `25`. The Spring Boot parent also changes from 3.3.4 to 4.1.1.

### Exercise 7.3 ★★ Your own branch

`git switch -c exercise-7-3 book-m2-documents`, then `echo "hello" > notes.txt`, `git add notes.txt`, `git commit -m "Add notes"`. `git switch main` removes the file from the folder; `git switch exercise-7-3` brings it back, because the commit lives on that branch.
