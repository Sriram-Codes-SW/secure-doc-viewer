# Chapter 6 solutions

### Exercise 6.1 ★ Read the coordinates

`groupId` `com.example`, `artifactId` `secure-doc-viewer`, version `0.1.0`. It compiles for Java 25 (`<java.version>25</java.version>`).

### Exercise 6.2 ★ Build it

The JAR is `target/secure-doc-viewer.jar`, named by `<finalName>` in the build section of `pom.xml`.

### Exercise 6.3 ★★ Why pin Tomcat?

The project overrides the web server (Tomcat) version to 11.0.26 because the version Spring Boot 4.1.1 manages (11.0.24) had three critical security advisories. The override should be removed once Spring Boot manages Tomcat 11.0.25 or later, so the parent's tested versions apply again.
