<!-- chapter: 13 | part: II | owner: writer-backend | tag: book-m3-hardening | status: draft -->
# Solutions: Chapter 13

1. (★) `@Size(max = 64)` on `username` in `LoginRequest`, applied because the controller parameter is marked `@Valid`. A failure becomes a `400`.
2. (★) `{"error": "Something went wrong on our side. Reference: <8 characters>."}` with status `500`. The message and the reference are safe to show; the exception details stay in the server log next to the same reference.
3. (★★) Add `public class ConflictException extends RuntimeException` with a constructor taking a message, then in `GlobalExceptionHandler` add `@ExceptionHandler(ConflictException.class)` returning `error(HttpStatus.CONFLICT, e.getMessage())`. The message text comes from whoever throws the exception.
4. (★★) Unset `SIGNING_SECRET` and start the app from the project folder. If a `.env` file supplies the value, remove it there too, because the project imports `.env` as a fallback. Startup stops with the message `SIGNING_SECRET must be set`, from `@NotBlank` on the `signingSecret` field of `ViewerProperties`.
5. (★★★) The server limit is `spring.servlet.multipart.max-file-size`, the handler message uses `MAX_UPLOAD_MB`, and the frontend checks the size before sending. If the frontend allowed 100 MB while the server allowed 50 MB, users would wait through a long upload only to be refused with `413`; the three should agree so the refusal comes early.
