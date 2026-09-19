<!-- chapter: 13 | part: II | owner: writer-backend | tag: book-m3-hardening | status: draft -->
# Chapter 13: Validation, configuration properties and errors

A server that accepts anything a client sends will eventually be hurt by it. This chapter teaches three habits the Secure Document Viewer applies from milestone 3 onward: check every input, refuse to start with a broken configuration, and answer every failure in one predictable JSON shape that never leaks internals.

## Learning objectives

By the end of this chapter, you will be able to:

- Explain why a server must validate input that the user interface has already checked.
- Add Bean Validation annotations to a request record and to request parameters.
- Explain how `ViewerProperties` makes the application fail at startup when a secret is missing.
- Describe how `GlobalExceptionHandler` maps exceptions to status codes and one JSON error body.
- Explain why the catch-all handler returns a reference code rather than the exception message.
- Name three limits the project sets on uploads.

**A note on versions.** Most listings in this chapter are quoted at `book-m3-hardening` (Spring Boot 3.3.4, Java 21), where these features were added. Listing 13.1 and the paging snippet come from `book-m6-final` (Spring Boot 4.1.1, Java 25). At `book-m6-final`, `ViewerProperties` has more settings and different defaults (for example `tileRateLimitPerWindow` is 180, not 120), and `GlobalExceptionHandler` has extra handlers, but the parts quoted here are unchanged.

Terms used here and explained where they appear: **`Accept` header** (the request header naming the content types the caller can receive; Chapter 8), **stack trace** (the list of method calls at the moment of an exception; Chapter 3), **log** (Chapter 11), **`Retry-After`** (a response header telling the client how many seconds to wait; Chapter 12), and **UUID** (a randomly generated identifier, used here only to make a short reference code). CSRF is the subject of [Chapter 16](16-spring-security-defenses.md).

## Prerequisites

- Chapter 5: exceptions
- Chapter 11: Spring Boot foundations (beans, `application.yml`)
- Chapter 12: REST controllers and JSON

## Beginner tier: Never trust input

### 13.1 Never trust input

Picture a bank teller who checks that a deposit slip is filled in, but never looks at the check itself. The slip's form is not the security; the teller's check is. The Angular app also checks what users type, so a mistake gets a friendly message immediately. But anyone can skip the app and send requests directly with a script. So the server must treat every request as untrusted, however it arrived.

**Where the analogy breaks down.** A teller sees the customer. A server never does; it sees only bytes, and it must decide from the bytes alone.

Validation has two jobs: protect the system (a 10 MB username shouldn't reach the database) and help the caller (a clear message saying what to fix).

### 13.2 Bean Validation annotations

**Bean Validation** is a standard set of annotations that state rules directly on the data. Spring runs them for you when a method parameter is marked `@Valid` (for bodies) or when a controller class has validation enabled. Listing 13.1 is the sign-in request.

**Listing 13.1 — The sign-in request record in `AuthController.java` (`book-m6-final`, simplified: only the record and the start of the method)**

*`src/main/java/com/example/securedocviewer/controller/AuthController.java`*

```java
public record LoginRequest(@NotBlank @Size(max = 64) String username,
                           @NotBlank @Size(max = 128) String password) {
}

// ...

@PostMapping("/login")
public ResponseEntity<CurrentUser> login(@Valid @RequestBody LoginRequest body,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
```

`@NotBlank` rejects a missing value, an empty string and one made only of spaces. `@Size(max = 64)` caps the length. `@Valid` on the parameter tells Spring to check these rules before the method body runs; if a rule fails, the method is never called and Spring raises a `MethodArgumentNotValidException`. Section 13.4 shows where that becomes a `400` response: its handler takes the first field error and returns `field: message`. Spring uses three related exception types depending on where the rule sits: `MethodArgumentNotValidException` for a validated request body, `HandlerMethodValidationException` for rules on individual parameters (as below), and Jakarta's `ConstraintViolationException` for rules checked elsewhere in the code. The handler class has one method for each, so all three give the same `400` shape.

Rules also work on individual query parameters. `AdminController.audit` restricts its paging inputs:

```java
@RequestParam(defaultValue = "0") @Min(0) int page,
@RequestParam(defaultValue = "50") @Min(1) @Max(500) int size
```

(`book-m6-final`, `AdminController.java`, excerpt.) A caller can't ask for a page size of a million rows, which would otherwise load the audit table into memory. A failure here raises a different exception, `HandlerMethodValidationException`, and the error handler has a method for it too.

## Intermediate tier: Failing early and speaking one language

### 13.3 Typed configuration (`ViewerProperties`) that fails fast

Chapter 11 showed the `secure-doc-viewer:` block of `application.yml`. `ViewerProperties` is the Java class that receives it. Listing 13.2 shows the parts that matter.

**Listing 13.2 — `ViewerProperties.java` (`book-m3-hardening`, simplified: the other settings and all getters and setters are omitted)**

*`src/main/java/com/example/securedocviewer/config/ViewerProperties.java`*

```java
@Component
@ConfigurationProperties(prefix = "secure-doc-viewer")
@Validated
public class ViewerProperties {

    private String storageRoot = "./storage";
    // ...
    /**
     * HMAC key for tile tokens and session-derived values. Supplied via the
     * SIGNING_SECRET environment variable; startup fails if it is missing or
     * too short to be a real key, rather than failing on the first tile.
     */
    @NotBlank(message = "SIGNING_SECRET must be set")
    @Size(min = 32, message = "SIGNING_SECRET must be at least 32 characters")
    private String signingSecret;
    // ...
}
```

`@ConfigurationProperties(prefix = "secure-doc-viewer")` tells Spring to copy each key under that prefix into the matching field: `storage-root` in the file becomes `storageRoot` in Java (Spring matches the two spellings). Defaults such as `"./storage"` apply when a key is absent. `@Validated` switches on the same Bean Validation rules from Section 13.2, so a missing or short `signingSecret` stops the program during startup with the message you wrote.

Compare with reading each value by hand using `@Value`, which the project still does for a few single settings. A typed class gives you a name, a type, a default and a rule in one place, and misspelled keys are easier to spot. It also fails early: a server that started without a signing secret would only discover the problem when it tried to sign its first tile URL.

### 13.4 One JSON error shape: `GlobalExceptionHandler`

When something goes wrong, a client needs to know two things: the status code, and a message it can show. If every endpoint invented its own format, the Angular app would need many code paths. The project uses one: `{"error": "message"}`.

Java signals a failure by throwing an exception (Chapter 5). Spring lets you write a class of **exception handlers**, methods that catch a given exception type anywhere in the controllers and turn it into a response. The class is marked `@RestControllerAdvice`. Listing 13.3 shows the start of the project's.

**Listing 13.3 — `GlobalExceptionHandler.java` (`book-m3-hardening`, simplified: imports and most handlers omitted)**

*`src/main/java/com/example/securedocviewer/controller/GlobalExceptionHandler.java`*

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException e) {
        return error(HttpStatus.UNAUTHORIZED, "Invalid username or password.");
    }

    @ExceptionHandler({DocumentNotFoundException.class, ResourceNotFoundException.class})
    public ResponseEntity<Map<String, String>> handleNotFound(RuntimeException e) {
        return error(HttpStatus.NOT_FOUND, e.getMessage());
    }

    // ... more handlers ...
}
```

Each `@ExceptionHandler` names the exception types it catches. The project defines its own small exception classes, such as `BadRequestException` ("a request that is well-formed HTTP but breaks a business rule; mapped to 400", in its own comment) and `LoginLockedException` (mapped to `429` with `Retry-After`). A service simply throws the one that fits, and the handler decides the status. This keeps HTTP details out of the business code.

**Table 13.1 — A sample of the mapping (`GlobalExceptionHandler`)**

| Exception | Status |
|---|---|
| `MethodArgumentNotValidException`, `HandlerMethodValidationException`, `ConstraintViolationException` | 400 |
| `HttpMessageNotReadableException` (body missing or malformed) | 400 |
| `BadCredentialsException`, `InvalidTokenException` | 401 |
| `ForbiddenException` | 403 |
| `DocumentNotFoundException`, `ResourceNotFoundException`, unknown route | 404 |
| `UsernameTakenException` | 409 |
| `MaxUploadSizeExceededException` | 413 |
| `RateLimitExceededException`, `LoginLockedException` | 429 with `Retry-After` |
| any other `Exception` | 500 |

A handler that catches a validation failure picks only the first message, so the client gets one clear sentence, for example `username: must not be blank`.

The same shape is used for errors raised before a request even reaches a controller. Failures inside the security filter chain (not signed in, missing CSRF token) can't reach `@RestControllerAdvice`, so `SecurityErrorResponses` writes the same `{"error": ...}` JSON itself (Chapter 16).

## Advanced tier: Not leaking, and setting limits

### 13.5 Not leaking internals (generic 500 with a reference)

A helpful error message can be dangerous. An unhandled exception might say `SELECT * FROM ... WHERE path='C:/internal'`, which tells an attacker the database, table and file layout. Listing 13.4 is the last-resort handler.

**Listing 13.4 — The catch-all handler (`book-m3-hardening`)**

*`src/main/java/com/example/securedocviewer/controller/GlobalExceptionHandler.java`*

```java
/** Last resort: never leak internals, but make the failure traceable in the log. */
@ExceptionHandler(Exception.class)
public ResponseEntity<Map<String, String>> handleUnexpected(Exception e) {
    String reference = UUID.randomUUID().toString().substring(0, 8);
    log.error("Unhandled error, reference {}", reference, e);
    return error(HttpStatus.INTERNAL_SERVER_ERROR,
            "Something went wrong on our side. Reference: " + reference + ".");
}
```

The client gets a generic sentence and an eight-character reference. The full exception, with its stack trace, goes to the server log next to the same reference. When a user reports "reference 3f9a1c22", an operator finds the exact log entry. Nothing sensitive crosses the network, and the failure is still traceable.

The project's `ErrorContractTest` proves this. It adds a controller that throws an exception whose message contains a fake SQL statement and a file path, calls it, and asserts that the response is `500`, starts with `Something went wrong on our side. Reference: `, and contains neither the SQL nor the path (`src/test/java/com/example/securedocviewer/controller/ErrorContractTest.java`; Chapter 18).

#### A real incident: the handler that failed on images

The comment above `GlobalExceptionHandler` records a subtle bug. Without an explicitly set `Content-Type`, Spring negotiates the error body against the request's `Accept` header. The tile endpoint's callers may accept only images, so a JSON error body was "not acceptable", the handler itself failed, and a clean `401` or `429` turned into a `500`. The fix is visible in the `error(...)` helper, which always sets `MediaType.APPLICATION_JSON` explicitly. The lesson: error paths are code too, and they need tests as much as success paths do. <!-- source: dossier/bugs-and-findings.md#A3; dossier/decisions.md; GlobalExceptionHandler class comment at book-m3-hardening; phase 3b commit 335e0b0 -->

### 13.6 Limits: upload size, page count, decompression bombs

Validation isn't only about shape; it's about size. A PDF can be small on disk and enormous once drawn, a **decompression bomb**. The project sets three separate limits, all in configuration:

- **File size.** `spring.servlet.multipart.max-file-size: 50MB`. Exceeding it raises `MaxUploadSizeExceededException`, which the handler turns into `413` with the message "The file is too large (limit 50 MB)." A comment in `application.yml` says to keep this in step with `GlobalExceptionHandler.MAX_UPLOAD_MB` and the frontend check, three places that must agree.
- **Page count.** `max-pages: 500`. The comment on this field in `ViewerProperties.java` says: "Uploads with more pages are rejected before anything is rendered."
- **Page size in pixels.** `max-page-pixels: 40000000`. The comment on this field in `ViewerProperties.java` says: "Largest rendered page allowed (width x height at render DPI); stops decompression-bomb PDFs."

Each limit is checked before the expensive work begins, which is the point: rejecting cheaply protects the CPU and memory that rendering would otherwise consume. Chapter 17 shows the rendering side.

## In this project

**Table 13.2 — Where Chapter 13's ideas live**

| Idea | File | Tag |
|---|---|---|
| Request validation | `controller/AuthController.java`, `controller/AdminController.java` | `book-m6-final` |
| Typed, validated settings | `config/ViewerProperties.java` | `book-m3-hardening` |
| Error mapping | `controller/GlobalExceptionHandler.java` | `book-m3-hardening` |
| Filter-chain errors | `security/SecurityErrorResponses.java` | `book-m6-final` |
| Error contract test | `src/test/.../controller/ErrorContractTest.java` | `book-m3-hardening` |
| Upload limits | `src/main/resources/application.yml` | `book-m3-hardening` |

## Try it

1. (★) Which annotations stop a sign-in request with a 200-character username?
2. (★) What JSON does the server return for an unexpected exception? Which parts are safe to show a user?
3. (★★) Add a new exception class `ConflictException` and map it to `409` in the handler. Where does the message text come from?
4. (★★) Start the application without `SIGNING_SECRET` set. Read the startup error. Which line of `ViewerProperties` produced it?
5. (★★★) Explain why the upload limit appears in three places, and what a user would experience if the frontend limit were 100 MB while the server's stayed 50 MB.

## Summary

- The server validates every input, whatever the UI already checked.
- Bean Validation annotations (`@NotBlank`, `@Size`, `@Min`, `@Max`) plus `@Valid` state the rules next to the data.
- `@ConfigurationProperties` with `@Validated` turns configuration into a typed class and stops startup when a required value is wrong.
- `@RestControllerAdvice` maps exceptions to status codes and one `{"error": ...}` body, always with an explicit JSON content type.
- Unexpected errors return a generic message and a reference; details stay in the log.
- Size limits are checked before expensive work begins.

## Further reading

- *Spring Framework Reference Documentation*, "Validation, Data Binding, and Type Conversion." https://docs.spring.io/spring-framework/reference/core/validation.html
- *Spring Boot Reference Documentation*, "Type-safe Configuration Properties." https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties
- *Spring Framework Reference Documentation*, "Error Responses." https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-rest-exceptions.html
- *OWASP Cheat Sheet Series*, "Input Validation Cheat Sheet." https://cheatsheetseries.owasp.org/cheatsheets/Input_Validation_Cheat_Sheet.html
