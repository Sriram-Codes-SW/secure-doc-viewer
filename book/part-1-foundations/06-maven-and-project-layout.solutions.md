# Chapter 6 solutions

### Exercise 6.1 ★ Read the coordinates

`groupId` `com.example`, `artifactId` `secure-doc-viewer`, version `0.1.0`. It compiles for Java 25 (`<java.version>25</java.version>`).

### Exercise 6.2 ★ Build it

The JAR is `target/secure-doc-viewer.jar`, named by `<finalName>` in the build section of `pom.xml`. `target/classes` holds the compiled `.class` files, in folders that mirror the packages (`com/example/securedocviewer/...`), plus copies of the files from `src/main/resources` such as `application.yml`.

### Exercise 6.3 ★★ Why pin Tomcat?

The project overrides the web server (Tomcat) version to 11.0.26 because the version Spring Boot 4.1.1 manages (11.0.24) had three critical security advisories. The override should be removed once Spring Boot manages Tomcat 11.0.25 or later, so the parent's tested versions apply again.

### Exercise 6.4 ★★ Follow the tree

Your exact output depends on the resolved versions, so the names below are examples of what to look for: PDFBox brings in its own supporting libraries (for instance a logging library and font or image helpers), which appear indented beneath `org.apache.pdfbox:pdfbox`. You did not list them because they are transitive dependencies: PDFBox's own POM declares them, and Maven downloads everything a dependency needs.

### Exercise 6.5 ★★★ Compare two milestones

One good answer, from `git show book-m2-documents:pom.xml` compared with `book-m0-mvp`: `spring-boot-starter-security` (accounts, from milestone 1), `spring-boot-starter-validation` (input checks), `spring-boot-starter-data-jpa` (storing objects in the database), `flyway-core` and `flyway-mysql` (schema migrations), `mysql-connector-j` (the MySQL driver), `spring-security-test` and `h2` (test tools). Scopes other than the default: `mysql-connector-j` is `runtime`; `spring-boot-starter-test`, `spring-security-test` and `h2` are `test`. The guess for each feature is the point of the exercise; check it against Chapters 14 to 16.
