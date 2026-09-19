<!-- chapter: 12 | part: II | owner: writer-backend | tag: book-m6-final | status: draft -->
# Solutions: Chapter 12

### Exercise 12.1 ★ Map a URL to a method

`@GetMapping("/{documentId}")`, combined with the class-level `@RequestMapping("/api/documents")`. `@PathVariable` copies the matching part of the URL into `documentId`.

### Exercise 12.2 ★ Why ResponseEntity

`delete` returns `ResponseEntity.noContent().build()` because it must send status `204` with no body. Other methods return plain objects and get `200`.

### Exercise 12.3 ★★ Renaming a JSON key

The JSON key would change from `pageCount` to `pages`, so any client code reading `pageCount` (the Angular app's model of a document) would get `undefined` until it is updated. The record component names are the contract.

### Exercise 12.4 ★★★ Why tiles skip Jackson

`byte[]` is already the final format, so Spring writes the bytes as they are and Jackson, which converts objects to JSON, isn't involved. Without `Cache-Control: no-store`, a shared cache such as a proxy could keep a tile that has one viewer's name drawn into it and serve it to someone else.
