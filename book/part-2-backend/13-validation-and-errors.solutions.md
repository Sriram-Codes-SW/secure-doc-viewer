<!-- chapter: 13 | part: II | owner: writer-backend | tag: book-m3-hardening | status: expanded -->
# Solutions: Chapter 13

### Exercise 13.1 ★ Which rule stops a long username?

`@Size(max = 64)` on the `username` component of `LoginRequest`, checked because the controller parameter is marked `@Valid`. Spring throws `MethodArgumentNotValidException` before `login` runs, and `GlobalExceptionHandler.handleInvalidBody` answers `400` with `{"error": "username: size must be between 0 and 64"}` (the standard message of `@Size` states its bounds; the exact wording comes from Hibernate Validator). The same rule stops a 10 MB username: it is refused before any sign-in code runs.

### Exercise 13.2 ★ The reference code

`{"error": "Something went wrong on our side. Reference: <8 characters>."}` with status `500`. The sentence and the reference are safe to show. The exception, with its message and stack trace, is written to the server log by `log.error("Unhandled error, reference {}", reference, e)`, next to the same reference, so an operator can find it from what a user reports.

### Exercise 13.3 ★★ Add an exception and a mapping

```java
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
```

In `GlobalExceptionHandler`:

```java
@ExceptionHandler(ConflictException.class)
public ResponseEntity<Map<String, String>> handleConflict(ConflictException e) {
    return error(HttpStatus.CONFLICT, e.getMessage());
}
```

For the test, copy the shape of `ErrorContractTest`: a `@TestConfiguration @RestController` with a `@GetMapping("/api/test/conflict")` method that throws `new ConflictException("That title is taken.")`, `@Import` it into a `@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")` class, sign in (the path is under `/api/`, so it needs authentication), and assert `status().isConflict()` and `jsonPath("$.error").value("That title is taken.")`. The message text comes from whoever throws the exception, which is why exception messages that reach the client must be written for users.

### Exercise 13.4 ★★ Start without the secret

Unset `SIGNING_SECRET` in your shell, and remove or rename the `.env` file if it defines it (the project imports `.env` with `spring.config.import`, so a value there would satisfy the setting). The application refuses to start, and the error output mentions the property and your message `SIGNING_SECRET must be set`; the annotation is `@NotBlank(message = "SIGNING_SECRET must be set")` on the `signingSecret` field. With a 10-character value, the `@Size(min = 32, ...)` rule fires instead and the message is `SIGNING_SECRET must be at least 32 characters`. Restore your real value afterward.

### Exercise 13.5 ★★★ Design the limits

One good answer, in order: (1) role or sign-in required, `401` or `403`, before the body is read, protecting time and bandwidth; (2) maximum request size 2 MB (`413`), protecting memory and disk; (3) stream to a temporary file instead of holding it in memory; (4) check the file's leading bytes against the expected image format (PNG starts with a fixed 8-byte signature, JPEG with `FF D8`), `400`, because the client-supplied name and content type can't be trusted; (5) decode with a maximum pixel area (width times height), `400`, protecting memory against a small file that expands hugely; (6) re-encode into a standard format and discard the original, so anything hidden in the file is dropped; (7) limit the rate per user, `429`. The PDF-specific limits with no equivalent are the page count and the render-slot and timeout layers, because decoding one small image is cheap and bounded; the size, signature and pixel-area checks are still needed.

### Exercise 13.6 ★★★ Why three places?

The three are the server's multipart limit (what actually stops the upload), the number in the error message (what the user is told) and the frontend's pre-check (what avoids a wasted upload). If the frontend allows 100 MB but the server stops at 50, the user selects a 70 MB file, waits for a long upload, and is then refused with a `413` at the end. If the message says 50 but the setting is different, the user is told something false. To keep them from drifting: make one the source of truth, for example expose the limit through an endpoint or a build-time setting the frontend reads instead of copying it, or at minimum add a test that reads the server setting and asserts the constant and the frontend value match. The project's answer is a comment in each place saying to keep them in step, which is the weakest of the three, but cheap.
