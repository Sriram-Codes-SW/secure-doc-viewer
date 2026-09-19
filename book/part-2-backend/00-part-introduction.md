<!-- chapter: 0 | part: II | owner: writer-backend | tag: book-m6-final | status: draft -->
# Part II: The backend

In Part I you learned the language (Java), the tools (Maven, Git, Docker), and the web and database basics the rest of the book stands on. Part II builds the server side of the Secure Document Viewer on top of them.

The backend is the part of the app users never see and everything depends on. It decides who you are, what you may open, and whether a request is honest. The browser is easy to bypass, so every rule that matters lives here.

## What you will build and learn

**Table II.1 — Chapters in Part II**

| Chapter | You learn | The app uses it for |
|---|---|---|
| 11. Spring Boot foundations | Frameworks, beans, configuration | Starting the server and wiring its classes |
| 12. REST controllers and JSON | Mapping requests to methods | The document, sign-in and tile endpoints |
| 13. Validation and errors | Checking input, one error shape | Refusing bad input without leaking internals |
| 14. Storing data | JPA, Flyway, transactions | Accounts, documents, shares, the audit trail |
| 15. Spring Security I | Sessions, passwords, roles | Signing users in |
| 16. Spring Security II | CSRF, headers, throttling | Defending the sign-in and the API |
| 17. Files, images, PDFs, signatures | Rendering, HMAC, bounded work | Tiles and signed tile URLs |
| 18. Testing the backend | JUnit, MockMvc, Testcontainers | Proving all of the above keeps working |

## How Part II is organized

Each chapter has three tiers: a beginner tier with the core idea and setup, an intermediate tier on how the pieces interact, and an advanced tier on security, performance and real incidents from this project. Code listings are copied from the repository at a named tag, such as `book-m6-final`. Part IV then tells the story of how the app grew milestone by milestone, so Part II deliberately teaches the concepts using the final code and leaves the history to Part IV.

Java 25, Spring Boot 4.1.1, Spring Security 7, Jackson 3, Hibernate 7, Flyway 11 and MySQL 8.4 are the versions used throughout.
