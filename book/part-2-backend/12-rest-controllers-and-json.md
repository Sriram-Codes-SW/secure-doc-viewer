<!-- chapter: 12 | part: II | owner: writer-backend | tag: book-m6-final | status: draft -->
# Chapter 12: REST controllers and JSON

A controller is the class that receives one kind of web request and returns a response. In this chapter you learn to read and write controllers, using the real ones from the Secure Document Viewer: the document library, the sign-in endpoint and the tile endpoint that sends PNG images.

## Learning objectives

By the end of this chapter, you will be able to:

- Explain what REST means and how this book uses the word.
- Write a controller method that maps an HTTP method and path to Java code.
- Read data from a path variable, a query parameter and a JSON request body.
- Explain how Jackson turns Java objects and records into JSON.
- Describe how a controller returns binary data and sets status codes and headers.

## Prerequisites

- Chapter 8: how the web works (requests, responses, status codes, JSON)
- Chapter 11: Spring Boot foundations (beans, annotations)

## Beginner tier: A method that answers a request

### 12.1 What REST means, and what this book means by it

Think of a library desk. You don't walk the shelves; you hand the clerk a request slip naming the thing you want and what you want done with it: "fetch book 42", "return book 42". **REST** (representational state transfer) is a style of web API built the same way. Each thing the server holds, called a **resource**, has an address (a path such as `/api/documents/abc`), and the HTTP method says the action: `GET` reads, `POST` creates, `PUT` replaces, `PATCH` changes part, `DELETE` removes.

**Where the analogy breaks down.** A clerk remembers you between visits. A REST server is meant to treat each request as self-contained. This project bends that rule on purpose: sign-in creates a server-side session, and later requests carry a cookie that identifies it (Chapter 15). This book calls the API "REST-style": resources, paths and methods as above, JSON bodies, and honest status codes, but not the full academic definition.

### 12.2 A controller method: `@RestController`, `@GetMapping`

**Listing 12.1 — `DocumentController.java` (`book-m6-final`, simplified: the start of the class and one method)**

```java
@RestController
@RequestMapping("/api/documents")
public class DocumentController {
    // ... constructor as in Listing 11.2 ...

    /** Only the documents the caller is allowed to open. */
    @GetMapping
    public List<DocumentSummary> list(Authentication authentication) {
        return documents.list(Viewer.of(authentication));
    }
```

Reading it line by line:

- `@RestController` tells Spring this class is a bean whose methods answer web requests, and that whatever they return should be written into the response body (as JSON, for objects).
- `@RequestMapping("/api/documents")` puts a path prefix on every method in the class.
- `@GetMapping` on `list` means: when a `GET` request arrives at exactly `/api/documents`, call this method.
- `Authentication authentication` is a parameter Spring fills in with the signed-in user (Chapter 15). You never call this method yourself; the framework calls it, which is the inversion of control from Chapter 11.
- The method hands the real work to `DocumentService`. The comment in the file says it plainly: who may see or change what is decided in the service; the controller "only adapts HTTP". Keeping controllers thin makes the rules easy to test without a web server.

The return type `List<DocumentSummary>` is turned into a JSON array automatically (Section 12.4).

### 12.3 Path variables, query parameters, request bodies

A request carries data in three places, and each has an annotation.

**Table 12.1 — Where request data comes from**

| Where | Example | Annotation |
|---|---|---|
| The path | `/api/documents/abc` | `@PathVariable` |
| The query string | `/api/tiles?token=...` | `@RequestParam` |
| The body (JSON) | `{"title": "Q3 report"}` | `@RequestBody` |

Listing 12.2 shows all of them in one class.

**Listing 12.2 — Excerpts from `DocumentController.java` (`book-m6-final`, simplified: three methods, others omitted)**

```java
public record UpdateDocumentRequest(String title, Visibility visibility) {
}

@GetMapping("/{documentId}")
public DocumentDetail get(@PathVariable String documentId, Authentication authentication,
                          HttpServletRequest request) {
    return documents.get(documentId, Viewer.of(authentication), actors.of(request, authentication));
}

@PatchMapping("/{documentId}")
public DocumentDetail update(@PathVariable String documentId, @RequestBody UpdateDocumentRequest body,
                             Authentication authentication, HttpServletRequest request) {
    return documents.update(documentId, body.title(), body.visibility(),
            Viewer.of(authentication), actors.of(request, authentication));
}
```

`{documentId}` in the path is a placeholder; `@PathVariable` copies the matching part of the real URL into the parameter. For `@RequestBody`, Spring reads the JSON body and builds an `UpdateDocumentRequest` from it. That class is a Java **record** (Chapter 4): a short way to declare a class that only carries data. Its field names, `title` and `visibility`, are the JSON keys the client must send.

Query parameters use `@RequestParam`; `TileController` reads `token` this way with `@RequestParam String token`. If a required one is missing, Spring raises an error, which Chapter 13 turns into a clean JSON `400`.

<!-- Chapter 12 continues: 12.4 Jackson, then Intermediate tier (12.5 bytes/TileController, 12.6 multipart), Advanced (12.7 status codes and headers), In this project, Try it, Summary, Further reading. Verify any book-m1-accounts listing via git show before quoting it. -->
