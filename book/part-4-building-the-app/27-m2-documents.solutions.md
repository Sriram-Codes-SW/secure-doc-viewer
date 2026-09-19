<!-- chapter: 27 | part: IV | owner: writer-app | tag: see chapter | status: draft -->
# Solutions for Chapter 27

### Exercise 27.1 ★ Why 404

Because a 403 would confirm the document exists. With 404 an outsider can't tell a hidden document from one that was never there. The response is the only thing they ever see, so it is the whole disclosure (Section 27.2).

### Exercise 27.2 ★★ Three ways to see a document

(1) `d.visibility = :everyone`, (2) `d.owner.username = :username`, or (3) the username is among the users in `d.sharedWith`. Admins skip the query's visibility test in `DocumentService`.

### Exercise 27.3 ★★★ Unshare while reading

One good answer: the tile endpoint re-checks access on every request, so the request is answered 404 even though the signed URL hasn't expired. Signing proves the server issued the URL; only the access check knows the document is no longer shared. The pull request for this milestone records a test for exactly this case.

