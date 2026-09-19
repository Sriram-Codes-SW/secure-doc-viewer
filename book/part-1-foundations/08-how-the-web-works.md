<!-- chapter: 8 | part: I | owner: writer-foundations | tag: book-m6-final | status: draft -->
# Chapter 8: How the web works

The browser and the backend of the Secure Document Viewer talk in a language called HTTP. Every protection in the app, from sign-in to signed tile URLs, is expressed in that language, so you need to read it fluently. This chapter teaches requests and responses, methods and status codes, headers, JSON, cookies, the same-origin rule, and HTTPS, and shows you how to watch real traffic.

## Learning objectives

By the end of this chapter, you will be able to:

- Describe an HTTP request and response and name their parts.
- Choose the right method for an action and explain the common status codes.
- Read and write a small JSON document.
- Explain what a cookie is and how the app uses one for sign-in.
- Explain the same-origin rule and why browsers enforce it.
- Explain, in one paragraph, what HTTPS adds.
- Inspect traffic with the browser's developer tools and `curl`.

## Prerequisites

- Chapter 1: The big picture
- Chapter 2: The command line and your files

## Beginner tier: Requests and responses

### 8.1 Browsers, servers, requests and responses

Chapter 1 introduced clients and servers. **HTTP** (HyperText Transfer Protocol) is the set of rules they follow to talk. A **protocol** is an agreed format for a conversation. The conversation is always the same shape: the client sends a **request**, and the server sends back one **response**. The server never speaks first.

A request names *what* the client wants and *how*; the response says whether that worked and carries the result. Everything on a web page, from the text to each image, arrives through separate exchanges like this. When a reader looks at one page of a document in the app, the browser makes about a dozen tile requests, one for each tile.

Figure 8.1 shows one exchange, written out the way it travels.

```text
GET /api/documents HTTP/1.1
Host: localhost:8080
Cookie: SDV_SESSION=<session-id>
Accept: application/json

HTTP/1.1 200 OK
Content-Type: application/json

[{"documentId":"...","title":"Quarterly report"}]
```

*Figure 8.1 — One request and its response (teaching example, abbreviated)*

The top block is the request: a **request line** (method, path, protocol version), then **headers**, one per line. The bottom is the response: a **status line**, headers, an empty line, and the **body**. The id and title values are placeholders for illustration.

### 8.2 URLs, methods and status codes

A URL has parts. Take `https://docs.example.com:8080/api/tiles?token=<signed-token>`:

- `https` is the scheme (the protocol, with encryption, Section 8.6);
- `docs.example.com` is the host, the computer to contact;
- `8080` is the port (Chapter 2);
- `/api/tiles` is the path, chosen by the app;
- `?token=<signed-token>` is the query string: extra named values after a question mark.

The **method** says what the client wants to do. Table 8.1 lists the ones the app uses, with real examples from `DocumentController`.

| Method | Meaning | Example in the app |
|---|---|---|
| `GET` | Read something; changes nothing | `GET /api/documents` lists your documents |
| `POST` | Create something or submit an action | `POST /api/documents` uploads a PDF |
| `PUT` | Replace something | `PUT /api/documents/{id}/file` replaces a PDF |
| `PATCH` | Change part of something | `PATCH /api/documents/{id}` renames it |
| `DELETE` | Remove something | `DELETE /api/documents/{id}` |

*Table 8.1 — HTTP methods in the app*

<!-- source: DocumentController.java at book-m6-final -->

The response's **status code** is a three-digit number telling the client what happened. They come in families: 2xx success, 3xx redirect, 4xx the client's request was wrong, 5xx the server failed. Table 8.2 lists the codes this app produces, each taken from its error handler.

| Code | Name | When the app sends it |
|---|---|---|
| `200` | OK | The request worked |
| `204` | No Content | A delete worked; nothing to return |
| `400` | Bad Request | Missing or malformed input |
| `401` | Unauthorized | Not signed in, or an invalid tile token |
| `403` | Forbidden | You may see it but not change it |
| `404` | Not Found | Doesn't exist, or you may not know it exists |
| `409` | Conflict | The username is already taken |
| `410` | Gone | A tile URL for a page that has since been replaced |
| `413` | Content Too Large | The upload is over 50 MB |
| `429` | Too Many Requests | Rate limit or sign-in lockout, with a `Retry-After` header |
| `500` | Internal Server Error | An unexpected failure on the server |
| `503` | Service Unavailable | The server is busy rendering; retry |

*Table 8.2 — Status codes the app uses*

<!-- source: GlobalExceptionHandler.java at book-m6-final -->

One row deserves comment. A document you may not see gets `404`, not `403`, so that nobody can probe which documents exist. The app's own exception comments say this explicitly.

## Intermediate tier: Headers, JSON and cookies

### 8.3 Headers, bodies and JSON

**Headers** are name-and-value lines that add information about the message. A few matter in this app:

- `Content-Type` says what the body is (`application/json`, `image/png`);
- `Cache-Control` tells caches whether they may keep a copy;
- `Retry-After` tells a client how many seconds to wait before trying again;
- `Cookie` and `Set-Cookie` carry cookies (Section 8.4).

The app's tile endpoint sets `Cache-Control: no-store`, which forbids any cache from keeping the tile. The reason, given in its source: a shared cache holding a tile watermarked for one person could hand it to someone else. <!-- source: TileController.java at book-m6-final -->

The **body** carries the data. For the app's API, the body is **JSON** (JavaScript Object Notation): a text format for structured data. It has objects in braces with named fields, arrays in brackets, and values that are text, numbers, `true`, `false` or `null`.

**Example 8.1 — A JSON object**

```json
{
  "title": "Quarterly report",
  "pageCount": 12,
  "visibility": "PRIVATE",
  "sharedWith": ["reader.one"]
}
```

The similarity to Chapter 4's records is deliberate. The app's record `DocumentSummary` becomes a JSON object with the same field names, automatically. Every error the app sends is also JSON with one field, `error`, and the handler sets the `Content-Type` explicitly.

**Listing 8.1 — `GlobalExceptionHandler.java` (book-m6-final, excerpt: method `error`)**

```java
    private static ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("error", message));
    }
```

*Path: `src/main/java/com/example/securedocviewer/controller/GlobalExceptionHandler.java`*

A `ResponseEntity` is Spring's object for a whole response: a status, headers, and a body. This helper builds one with the given status, marks it JSON, and puts the message in a map (Chapter 5), which becomes `{"error": "..."}`. Chapter 13 shows the full handler.

### 8.4 Cookies and sessions

HTTP has no memory: each request stands alone. Yet after you sign in, the server must recognize you on the next request. The mechanism is a **cookie**: a small piece of text that the server asks the browser to store (with a `Set-Cookie` header) and that the browser then attaches to every later request to that server (with a `Cookie` header).

At sign-in, the server creates a **session** (Chapter 1) and sends its identifier as a cookie. The app's configuration names and protects it.

**Listing 8.2 — `application.yml` (book-m6-final, excerpt: session cookie)**

```yaml
  servlet:
    session:
      # Idle timeout: each request extends it, so an active reader is never
      # cut off mid-document, while an abandoned session still dies.
      timeout: 30m
      cookie:
        name: SDV_SESSION
        http-only: true
        same-site: strict
        # Must be true anywhere the app is served over HTTPS.
        secure: ${SESSION_COOKIE_SECURE:false}
```

*Path: `src/main/resources/application.yml`*

YAML is a settings format where indentation shows nesting. The cookie is named `SDV_SESSION`, and three settings protect it:

- `http-only: true` hides the cookie from JavaScript (the programming language that browsers run inside web pages; Part III covers its typed sibling, TypeScript) running in the page, so a script injected into the page can't steal it;
- `same-site: strict` tells the browser to send it only for requests that start on the app's own site, which blocks a class of forged-request attacks (Chapter 16);
- `secure` makes the browser send it only over HTTPS. It is `false` by default so that local development over plain HTTP works, and the file's comment says it must be true wherever the app is served over HTTPS.

The `timeout: 30m` line means a session ends after 30 minutes without a request. **The cookie is the reader's key card:** whoever holds it is that user, which is why it needs these protections. The analogy breaks down because a key card is checked at a door once, while the app checks the session again on every single tile request.

## Advanced tier: Trust and traffic

### 8.5 Same origin, and why browsers are suspicious

An **origin** is the combination of scheme, host and port. `http://localhost:8080` and `http://localhost:4200` are different origins because the ports differ. Browsers apply the **same-origin policy**: a script loaded from one origin may not freely read responses from another. The rule exists because otherwise any web page you visit could silently read your email or your documents using your cookies.

The consequence for this project: the Angular frontend (the pages you see, built in Part III) and the backend must appear to the browser as one origin. A **proxy** is a program that receives a request on behalf of another server and forwards it. In development a small proxy forwards `/api` requests to the backend, and in the Docker stack a web server does the same (Chapters 20 and 33). The project avoids opening cross-origin access rather than granting it.

### 8.6 HTTPS and TLS in one page

Plain HTTP travels as readable text, so anyone on the network path can read or change it, including the session cookie. **HTTPS** is HTTP inside an encrypted channel created by **TLS** (Transport Layer Security). TLS provides three things: **encryption** (eavesdroppers see noise), **integrity** (changes are detected) and **authentication** (a certificate proves you reached the real host). A **certificate** is a signed statement, issued by an authority the browser trusts, that a public key belongs to a given domain.

The project's optional TLS front end is a program called Caddy, and the setting `SESSION_COOKIE_SECURE=true` is what you turn on when you use it. Chapter 33 covers deployment.

### 8.7 Looking at real traffic

You can watch everything in this chapter yourself, against your own local copy of the app.

**Developer tools.** In a browser, press <kbd>F12</kbd>, open the *Network* tab, and load a page. Each row is one request; click it to see the method, status code, headers and body. Look for a `/api/tiles?token=...` row when a document page loads.

**curl.** `curl` is a command-line program that sends requests. The `-i` flag prints the response headers as well:

```bash
curl -i http://localhost:8080/actuator/health
```

You should see something like:

```text
HTTP/1.1 200
Content-Type: application/json
...
{"status":"UP"}
```

Requests to the API without a session cookie get `401`, which is a useful first experiment: `curl -i http://localhost:8080/api/documents`. Only ever run such experiments against your own copy, not against someone else's deployment.

## In this project

- `controller/DocumentController.java`: methods and paths.
- `controller/GlobalExceptionHandler.java`: status codes and the JSON error contract.
- `controller/TileController.java`: the tile endpoint, `Cache-Control: no-store`.
- `src/main/resources/application.yml`: the session cookie.

## Try it

### Exercise 8.1 ★ Match the code

Which status code would you expect for: reading a document you don't have access to; uploading a 60 MB file; sending too many tile requests in a minute?

*Solution:* Appendix C, Exercise 8.1.

### Exercise 8.2 ★ Read a URL

Take `https://docs.example.com/api/tiles?token=abc` and name its scheme, host, path and query string.

*Solution:* Appendix C, Exercise 8.2.

### Exercise 8.3 ★★ Watch the cookie

With the app running locally, sign in through the browser, open the developer tools, and find the `SDV_SESSION` cookie under *Application* (or *Storage*). Which flags are set? Compare with Listing 8.2.

*Solution:* Appendix C, Exercise 8.3.

## Summary

- HTTP is a request and response conversation; the server never speaks first.
- Methods say what to do; status codes say what happened, and the app uses `404` to hide existence.
- Headers describe messages; the API's bodies are JSON.
- A cookie carries the session identifier, protected by `http-only`, `same-site` and `secure`.
- Browsers enforce the same-origin rule; HTTPS adds encryption, integrity and authentication.

## Further reading

- *MDN Web Docs*, "An overview of HTTP." https://developer.mozilla.org/en-US/docs/Web/HTTP/Guides/Overview
- *MDN Web Docs*, "HTTP response status codes." https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Status
- *MDN Web Docs*, "Same-origin policy." https://developer.mozilla.org/en-US/docs/Web/Security/Same-origin_policy
- RFC 9110, "HTTP Semantics." https://www.rfc-editor.org/rfc/rfc9110
