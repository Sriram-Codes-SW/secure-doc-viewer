<!-- chapter: 28 | part: IV | owner: writer-app | tag: book-m3-hardening | status: expanded -->
# Solutions for Chapter 28

### Exercise 28.1 ★ A 60 MB upload

HTTP 413 with a JSON body `{"error": "The file is too large (limit 50 MB)."}`. The multipart limit (`max-file-size: 50MB`) makes the framework throw `MaxUploadSizeExceededException`, and `GlobalExceptionHandler.handleUploadTooLarge` turns it into a 413. (A browser using the project's frontend would have refused the file earlier, with a message.)

### Exercise 28.2 ★ Reference, not message

The exception message can contain SQL, file paths, class names or other internals that help an attacker and confuse users. The catch-all logs the full exception under an eight-character reference and returns only the reference. A user reports the reference and an operator finds the exact failure in the log, without the client ever seeing the internals.

### Exercise 28.3 ★★ Is this page allowed?

Scale = 150 / 72 ≈ 2.0833. Width ≈ 1,000 × 2.0833 ≈ 2,083 pixels; height ≈ 1,500 × 2.0833 ≈ 3,125 pixels. The area is about 6.5 million pixels, which rounds to 7 million. That is well below 40,000,000, so the page passes (assuming the document is also at most 500 pages and under 50 MB).

### Exercise 28.4 ★★ Order of checks

The signature check is cheap: it reads at most 1,024 bytes and needs no parser, so obviously wrong files are refused before any parsing cost or exposure to parser bugs. The page count and page sizes need the document to be opened and parsed. The pixel check comes before rendering because rendering is what allocates the image: the arithmetic on the declared size must run first, or a hostile page size would exhaust memory before the check could refuse it.

### Exercise 28.5 ★★ Read the headers

You should see `Content-Security-Policy` (containing `default-src 'none'` and `frame-ancestors 'none'`), `Referrer-Policy: no-referrer`, `Permissions-Policy`, `X-Content-Type-Options: nosniff` and `X-Frame-Options: DENY`. `SecurityHeadersTest.apiResponsesCarryHardeningHeaders` asserts them.

### Exercise 28.6 ★★★ Health and env

One good answer. With MySQL running, health returns 200 with `{"status":"UP"}` and no details. With MySQL stopped, it returns 503 with status `DOWN`. `/actuator/env` stays closed because only `health` is exposed (`management.endpoints.web.exposure.include: health`), the security rules permit only the health paths, and every other route falls under `anyRequest().denyAll()`, so an anonymous caller gets 401 (403 or 404 would also satisfy `SecurityHeadersTest`).
