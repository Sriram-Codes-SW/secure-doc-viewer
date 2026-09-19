<!-- chapter: 28 | part: IV | owner: writer-app | tag: see chapter | status: draft -->
# Solutions for Chapter 28

### Exercise 28.1 ★ A 60 MB upload

413, with a JSON body in the `{"error": "..."}` shape saying the file is too large (limit 50 MB), from `GlobalExceptionHandler.handleUploadTooLarge`.

### Exercise 28.2 ★★ Reference, not message

The message could hold SQL, file paths or other internals. The reference lets an operator find the full exception, which is logged under the same reference, without exposing it to the client.

### Exercise 28.3 ★★★ Health and env

One good answer: health reports UP, then DOWN with HTTP 503 when the database is unreachable; it reports status only. Only the health paths are `permitAll`; `anyRequest().denyAll()` and the `/api/**` rules leave every other Actuator endpoint closed, so `/actuator/env` returns 401 for an anonymous caller (as the pull request's live check records).

