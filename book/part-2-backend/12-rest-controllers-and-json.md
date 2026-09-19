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

**Listing 12.1 — `DocumentController.java` (`book-m6-final`, simplified: the nested records, fields and constructor are omitted, see Listing 11.2; the other methods and the closing brace are left out)**

*`src/main/java/com/example/securedocviewer/controller/DocumentController.java`*

```java
@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    // ...

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

Listing 12.2 shows the path variable and the request body; the query parameter follows in the text after it. Two terms first: the **query string** is the part of a URL after the `?`, and a **JSON array** is a list written in square brackets. `Authentication` and `HttpServletRequest` are objects Spring supplies describing the signed-in user and the raw request; you can ignore them for now, because [Chapter 15](15-spring-security-authentication.md) explains them.

**Listing 12.2 — Excerpts from `DocumentController.java` (`book-m6-final`, simplified: three declarations from different places in the class; everything else omitted)**

*`src/main/java/com/example/securedocviewer/controller/DocumentController.java`*

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

## Intermediate tier: JSON, bytes and files

### 12.4 Jackson: turning objects into JSON

You return a Java object; the client receives text. The library that converts between them is **Jackson**, and Spring Boot 4 uses Jackson 3, whose packages start with `tools.jackson` (`SecurityErrorResponses` imports `tools.jackson.databind.ObjectMapper`, for example). The conversion is called **serialization** (object to JSON) and **deserialization** (JSON to object).

For a record, the rule is simple: each component becomes a JSON key with the same name. Listing 12.3 is a record the project returns from the document list.

**Listing 12.3 — `DocumentSummary.java` (`book-m6-final`)**

*`src/main/java/com/example/securedocviewer/document/DocumentSummary.java`*

```java
public record DocumentSummary(
        String documentId,
        String title,
        int pageCount,
        String owner,
        Visibility visibility,
        long createdAtEpochSeconds,
        long updatedAtEpochSeconds,
        boolean canManage,
        Integer sharedWithCount
) {
}
```

A response for one document therefore looks like `{"documentId": "...", "title": "...", "pageCount": 12, ...}`. Two details are worth noticing. The timestamps are plain numbers (seconds since 1970) rather than date strings, which avoids time zone confusion on the client. And `sharedWithCount` is an `Integer`, not an `int`, so it can be `null`; the source comment says it is "only filled in for users who can manage it".

### 12.5 Returning bytes: how `TileController` sends PNGs

Not every response is JSON. The tile endpoint sends an image. Listing 12.4 shows the parts that matter.

**Listing 12.4 — `TileController.java` (`book-m6-final`, simplified: the checks and rendering in the middle of the method are omitted)**

*`src/main/java/com/example/securedocviewer/controller/TileController.java`*

```java
@GetMapping(value = "/api/tiles", produces = MediaType.IMAGE_PNG_VALUE)
public ResponseEntity<byte[]> getTile(@RequestParam String token,
                                      Authentication authentication,
                                      HttpServletRequest request) throws Exception {
    // ... verify the token, session, rate limit and access; render the tile ...

    return ResponseEntity.ok()
            // Deliberately not cacheable beyond a moment — a shared cache
            // holding onto a watermarked-for-someone-else tile would leak it.
            .cacheControl(CacheControl.noStore())
            .body(png);
}
```

`produces = MediaType.IMAGE_PNG_VALUE` declares that this method answers with `image/png`. The return type `ResponseEntity<byte[]>` lets the method control the whole response: status (`ok()` is 200), headers (`cacheControl(CacheControl.noStore())` sets `Cache-Control: no-store`) and body (the PNG bytes, unchanged). Returning `byte[]` bypasses Jackson because the bytes are already in their final format. The `no-store` header matters for security: each tile has the viewer's name drawn into it, so no shared cache may keep it for someone else.

### 12.6 File uploads (multipart)

A PDF upload can't be JSON. Browsers send files as **multipart** form data: one request with several named parts, each with its own content type. Listing 12.5 is the upload method.

**Listing 12.5 — `DocumentController.upload` (`book-m6-final`)**

*`src/main/java/com/example/securedocviewer/controller/DocumentController.java`*

```java
@PostMapping
public DocumentDetail upload(@RequestParam(value = "title", required = false) String title,
                             @RequestParam(value = "visibility", required = false) Visibility visibility,
                             @RequestParam("file") MultipartFile file,
                             Authentication authentication,
                             HttpServletRequest request) throws IOException {
    try (var in = file.getInputStream()) {
        return documents.upload(title, file.getOriginalFilename(), in, visibility,
                Viewer.of(authentication), actors.of(request, authentication));
    }
}
```

The text fields `title` and `visibility` arrive as ordinary parameters; `required = false` makes them optional, and Spring converts the text `PRIVATE` into the `Visibility` enum. The file arrives as a `MultipartFile`. The method passes the file's stream, not its whole contents, to the service, and `try (var in = ...)` closes the stream afterward. Streaming keeps a 50 MB PDF from being copied into memory just to hand it on. The size limit is set in `application.yml` (`spring.servlet.multipart.max-file-size: 50MB`), and [Chapter 13](13-validation-and-errors.md) shows how an oversized upload becomes a clean error.

## Advanced tier: The contract is part of the design

### 12.7 Status codes and headers as part of the contract

Clients, including the Angular app in Part III, decide what to do by looking at the status code before they read the body. So a controller's choice of status is a promise. The project follows a few rules:

- Success with nothing to return is `204 No Content`: `DocumentController.delete` returns `ResponseEntity.noContent().build()`.
- Creating something returns `201 Created`: `UserAdminController.create` returns `ResponseEntity.status(HttpStatus.CREATED).body(created)`.
- "Try again later" carries a `Retry-After` header, on both `429` and `503`. `GlobalExceptionHandler` builds these responses (Chapter 13).
- A document a user may not see is reported as `404`, never `403`, so its existence isn't revealed (the `DocumentService` class comment; Chapter 16).

The project returns plain objects when 200 is right and `ResponseEntity` whenever it needs a different status or a header.

## In this project

**Table 12.2 — Controllers in `book-m6-final`**

| Controller | Path prefix | Purpose |
|---|---|---|
| `AuthController` | `/api/auth` | Sign in, sign out, current user, password change |
| `DocumentController` | `/api/documents` | Library, upload, replace, share |
| `TileController` | `/api/tiles` | Watermarked PNG tiles |
| `AdminController` | `/api/admin` | Sessions, audit log, rate limits |
| `UserAdminController` | `/api/admin/users` | Account management |

All live in `src/main/java/com/example/securedocviewer/controller/`.

## Try it

1. (★) Which annotation and value make `DocumentController.get` respond to `GET /api/documents/{id}`? What fills the `documentId` parameter?
2. (★) Find the methods in `DocumentController` that return `ResponseEntity`. Why do they need it?
3. (★★) The JSON keys of `DocumentSummary` come from its record components. If you renamed `pageCount` to `pages`, what would break on the client?
4. (★★★) `TileController.getTile` returns `byte[]`. Explain why Jackson isn't involved, and what could go wrong if `Cache-Control: no-store` were missing.

## Summary

- REST-style APIs map an HTTP method and a path to an action on a resource; this project bends statelessness for its session cookie.
- `@RestController`, `@GetMapping` and friends connect requests to methods; `@PathVariable`, `@RequestParam` and `@RequestBody` read the path, query string and body.
- Jackson 3 serializes records to JSON with the component names as keys.
- `ResponseEntity` gives full control of status, headers and body, which is how PNG tiles and `204`, `201` and `Retry-After` responses are produced.
- Multipart requests carry files; the service receives a stream.
- Status codes and headers are part of the contract with the client.

## Further reading

- *Spring Framework Reference Documentation*, "Annotated Controllers." https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller.html
- *Spring Framework Reference Documentation*, "Multipart Resolver." https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-servlet/multipart.html
- *RFC 9110*, "HTTP Semantics." https://www.rfc-editor.org/rfc/rfc9110
