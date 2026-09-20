<!-- chapter: 12 | part: II | owner: writer-backend | tag: book-m6-final | status: expanded -->
# Solutions: Chapter 12

### Exercise 12.1 ★ Match a request to a method

`@GetMapping("/{documentId}")` on the method, combined with the class-level `@RequestMapping("/api/documents")`, gives the full path `/api/documents/{documentId}` for `GET`. Spring fills the `documentId` parameter from the matching part of the URL because of `@PathVariable`.

### Exercise 12.2 ★ Why `ResponseEntity`?

In `DocumentController`, `delete` returns `ResponseEntity<Void>` built with `ResponseEntity.noContent().build()`: the answer is status `204` with *no body*, which a plain return value can't express (returning `void` would give `200`). The other methods return plain objects (`DocumentDetail`, `List<DocumentSummary>`, `List<String>`) because `200 OK` with a JSON body is exactly right, so they don't need `ResponseEntity`. Elsewhere, `TileController.getTile` and `PageTileUrlController.tileUrls` return `ResponseEntity` because they set headers (`Cache-Control: no-store`) or want explicit control of the response.

### Exercise 12.3 ★★ Read the JSON of your own copy

Every key of an element in the array matches a component of `DocumentSummary`: `documentId`, `title`, `pageCount`, `owner`, `visibility`, `createdAtEpochSeconds`, `updatedAtEpochSeconds`, `canManage`, and `sharedWithCount`. For a reader (or anyone who can't manage the document), `canManage` is `false` and `sharedWithCount` is `null`, because the count is only filled in for users who can manage the document. That way a reader can't learn how widely a document is shared.

### Exercise 12.4 ★★ Rename a component

On the server: the record `DocumentSummary` (the component name), the code that builds it (`DocumentService.summary`, which passes `d.getPageCount()` to the constructor by position, so the compiler doesn't force a change there), and any test that reads the field, for example a `jsonPath("$[0].pageCount")` assertion. On the client: the TypeScript interface that models a document summary in the Angular app, and every template or component that reads `pageCount`. The JSON key is a contract: the server and the browser app are separate programs, built separately, and the only thing that connects them is the name in the JSON. A rename that compiles on the server can silently break the client, where the missing key appears as `undefined`. That's why such changes are made deliberately, in both places, and covered by an end-to-end test (Chapter 24).

### Exercise 12.5 ★★★ Design an endpoint

One good answer.

- **HTTP:** `GET /api/documents?limit=10&sort=updated`, or a dedicated `GET /api/documents/recent`. Prefer extending the existing collection with a query parameter when the result is the same kind of thing (a list of `DocumentSummary`), so clients reuse the same model. Validate `limit` with `@Min(1) @Max(50)`, as `AdminController.audit` does for its paging, so a caller can't ask for a million rows.
- **Response:** `List<DocumentSummary>`, newest first, using the same `updatedAtEpochSeconds` field the summary already has.
- **Status codes:** `200` on success, `400` for a bad `limit` (through the shared handler), `401` when not signed in.
- **Where the rule lives:** the controller method only reads the parameter and calls `DocumentService`. Which documents a user may see is already decided in `DocumentService.list` (using `DocumentRepository.findVisibleTo` or `findAllWithOwner`), so the new service method must reuse it and then sort and limit; it must not build its own visibility rule.
- **Tests:** a unit test of the sort-and-limit logic; a `MockMvc` test that a reader sees only documents visible to them, that `limit=0` returns `400`, and that the anonymous call returns `401` (Chapter 18).

### Exercise 12.6 ★★★ Follow the tile chain

The requests you should see for a page, in order: the document detail (`GET /api/documents/{id}`), the tile-URL grid for the page (`GET /api/documents/{id}/pages/{n}/tile-urls`), then one `GET /api/tiles?token=...` per tile, several in parallel. Decoding the part of the token before the dot with a base64url decoder gives a pipe-separated string with seven fields: the document id, page, row, column, tile version, a session binding, and an expiry time in epoch seconds (the canonical string of `SignedTilePayload`). You would not want the *session binding* logged if it were the session id, but it is not the session id: it is a keyed value derived from it, so the design makes the token safe to leak in a log or a shared screenshot for its short lifetime. What a leaked token still allows is fetching that one tile, from the session it was issued to, until it expires; the signature (the part after the dot) stops anyone changing any of the fields.
