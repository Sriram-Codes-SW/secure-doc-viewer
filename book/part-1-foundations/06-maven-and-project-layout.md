<!-- chapter: 6 | part: I | owner: writer-foundations | tag: book-m6-final | status: draft -->
# Chapter 6: Maven and the shape of a project

Chapter 3 compiled one file by hand. The Secure Document Viewer has 84 Java files (tests included) and depends on many libraries. Nobody compiles that by hand. This chapter teaches Maven, the tool that downloads the libraries, compiles the code, runs the tests and packages the result, and shows where every file in the project belongs.

## Learning objectives

By the end of this chapter, you will be able to:

- Explain what a build tool does and why the project needs one.
- Read `pom.xml` and say what each section is for.
- Explain what a dependency is and where Maven finds it.
- Explain why the project runs Maven through `mvnw`.
- Run the compile, test, package and verify steps.
- Say where source code, tests and settings live in the project.

## Prerequisites

- Chapter 3: Your first Java program
- Chapter 4: Classes, objects, records and interfaces

## Beginner tier: What a build tool does

### 6.1 What a build tool does

To turn source code into a running application you must do several things in the right order: fetch the libraries the code uses, compile every file, run the tests, and bundle the result into one runnable file. Doing this by hand is slow and easy to get wrong, and two people would do it slightly differently.

A **build tool** does it from a written description, the same way every time, on every machine. **Maven** is the build tool for this project. You describe the project once in a file called `pom.xml` (POM stands for Project Object Model), and Maven does the rest.

**Analogy.** Maven is a recipe-following kitchen assistant: you hand over a recipe and it fetches the ingredients and cooks the dish in a fixed order. The analogy breaks down because ingredients here are other people's code, which can carry security flaws; Chapter 36 covers how the project watches for that.

### 6.2 `pom.xml` line by line

A `pom.xml` is written in **XML**, a format where information sits between named tags: `<name>secure-doc-viewer</name>` means "the name is secure-doc-viewer". Tags nest. Here is the top of the project's file.

**Listing 6.1 — `pom.xml` (book-m6-final, excerpt: the project's identity)**

```xml
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>4.1.1</version>
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>secure-doc-viewer</artifactId>
    <version>0.1.0</version>
    <name>secure-doc-viewer</name>
```

*Path: `pom.xml`*

Every Maven project or library is identified by three values, its **coordinates**: a `groupId` (who made it, in the reverse-domain style from Chapter 4), an `artifactId` (its name), and a `version`. This project is `com.example:secure-doc-viewer:0.1.0`.

The `<parent>` block says "start from the settings of `spring-boot-starter-parent` version 4.1.1". A parent supplies sensible defaults: compiler settings, and, importantly, a tested list of matching library versions. That is why most dependencies below need no version number. Spring Boot is the framework Part II teaches.

Next come the properties.

**Listing 6.2 — `pom.xml` (book-m6-final, excerpt: properties)**

```xml
    <properties>
        <java.version>25</java.version>
        <pdfbox.version>3.0.8</pdfbox.version>
        <!-- Boot 4.1.1 ships Tomcat 11.0.24 (GHSA-9xv2-5v5q-p794, GHSA-gcx9-497g-6cp6,
             GHSA-h3x4-894j-xpx5, all critical). Drop this once Boot manages 11.0.25+. -->
        <tomcat.version>11.0.26</tomcat.version>
    </properties>
```

*Path: `pom.xml`*

**Properties** are named values used elsewhere. `java.version` tells Maven to compile for Java 25. `pdfbox.version` names the version of the library that reads PDFs. `tomcat.version` is a real decision: the project overrides the web server version that Spring Boot chose because the chosen one had critical security advisories, and the comment says when to remove the override. This is normal practice: state the reason next to the pin. <!-- source: pom.xml comment at book-m6-final -->

### 6.3 Dependencies and where they come from

A **dependency** is a library your code uses. Instead of copying its files into the project, you name it, and Maven downloads it (and the libraries it needs in turn) from **Maven Central**, a public repository of Java libraries, into a cache on your computer.

**Listing 6.3 — `pom.xml` (book-m6-final, excerpt: three dependencies)**

```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webmvc</artifactId>
        </dependency>
        <dependency>
            <groupId>org.apache.pdfbox</groupId>
            <artifactId>pdfbox</artifactId>
            <version>${pdfbox.version}</version>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
```

*Path: `pom.xml`*

Reading them:

- `spring-boot-starter-webmvc` is a **starter**, a bundle that pulls in everything needed for web endpoints. Its version comes from the parent. The file's own comment says Spring Boot 4 splits the old all-in-one starters into focused ones.
- `pdfbox` has an explicit version, written as `${pdfbox.version}`: the value of the property from Listing 6.2.
- `mysql-connector-j` is the driver that lets Java talk to MySQL. Its **scope** is `runtime`: needed when the program runs, not when compiling.

Test-only libraries carry `<scope>test</scope>`, so they never ship in the packaged application. <!-- source: pom.xml at book-m6-final -->

**Why so many dependencies?** Each does one job the project would not write itself: PDF rendering (`pdfbox`), the database (`spring-boot-starter-data-jpa`, Flyway, the MySQL driver), security, validation, metrics. Every dependency is also code you trust, so the project keeps the list short and scans it for known vulnerabilities in CI (Chapter 36).

**Versions at older tags.** Milestones `book-m0-mvp` to `book-m4-reading` used Spring Boot 3.3.4 and Java 21, and `book-m0-mvp` had only three dependencies (`spring-boot-starter-web`, `pdfbox` and `spring-boot-starter-test`). The upgrade to Spring Boot 4.1.1 and Java 25 happened at `book-m5-platform`. <!-- source: pom.xml at book-m0-mvp and book-m5-platform -->

## Intermediate tier: Running Maven

### 6.4 The Maven wrapper (`mvnw`) and why it pins the version

Different Maven versions can behave differently. If you had 3.8 and a teammate had 3.9, the same command could give different results. The project therefore ships a **wrapper**: small scripts, `mvnw` (macOS, Linux, Git Bash) and `mvnw.cmd` (Windows), that download and run one exact Maven version. You don't need Maven installed at all.

**Listing 6.4 — `maven-wrapper.properties` (book-m6-final)**

```properties
wrapperVersion=3.3.4
distributionType=only-script
distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.16/apache-maven-3.9.16-bin.zip
```

*Path: `.mvn/wrapper/maven-wrapper.properties`*

The `distributionUrl` names Maven 3.9.16. Run Maven by typing `./mvnw` instead of `mvn`; the first run downloads that version and later runs reuse it. This also helps the Docker image build and the automated checks, which use the same command. The wrapper was added when the project moved to Java 25 (`book-m5-platform`). <!-- source: git log for mvnw, commit 2d10e07 -->

### 6.5 Lifecycle: compile, test, package, verify

Maven runs a fixed sequence of **phases**. Asking for a phase runs it and every phase before it. Table 6.1 lists the ones you'll use.

**Table 6.1 — Maven phases you'll use**

| Command | What happens |
|---|---|
| `./mvnw compile` | Compiles the source code |
| `./mvnw test` | Compiles, then runs the unit tests |
| `./mvnw package` | Also bundles the app into `target/secure-doc-viewer.jar` |
| `./mvnw verify` | Also runs the integration checks; this is what continuous integration runs |

A **JAR** file is a zip of compiled classes. Because the project uses the Spring Boot plugin, the JAR it builds is **executable**: it contains the web server too, so you start the whole app with `java -jar target/secure-doc-viewer.jar`. The output name comes from `<finalName>secure-doc-viewer</finalName>` in the `pom.xml`. <!-- source: pom.xml and Dockerfile at book-m6-final -->

Everything Maven produces lands in a folder called `target/`, which the project's `.gitignore` excludes; you can always delete it and rebuild. The project's continuous integration runs `./mvnw -B verify` (the `-B` flag means batch mode, no interactive prompts). <!-- source: .github/workflows/ci.yml at book-m6-final -->

## Advanced tier: The shape of the project

### 6.6 Directory layout

Maven relies on **convention over configuration**: if you put files where it expects them, it needs no instructions. Table 6.2 shows the layout at `book-m6-final`.

**Table 6.2 — Where things live**

| Path | Holds |
|---|---|
| `pom.xml` | The build description |
| `src/main/java/` | The application's Java code, in folders matching packages |
| `src/main/resources/` | Non-code files the app reads: `application.yml`, the database migrations in `db/migration/` |
| `src/test/java/` | Tests |
| `src/test/resources/` | Settings for tests (`application-test.yml`) |
| `target/` | Build output (ignored by Git) |
| `frontend/` | The Angular application (Part III) |
| `Dockerfile`, `docker-compose.yml` | Packaging (Chapter 10) |

Splitting `src/main` from `src/test` matters: test code and test libraries never enter the packaged application. Chapter 18 covers the tests themselves.

## In this project

- `pom.xml`, `mvnw`, `mvnw.cmd` and `.mvn/wrapper/maven-wrapper.properties`: the build.
- `Dockerfile`: runs `./mvnw -B -q -DskipTests package` to build the JAR inside a container, and copies the dependency list first so that downloads stay cached until `pom.xml` changes. <!-- source: Dockerfile at book-m6-final -->
- `.github/workflows/ci.yml`: runs `./mvnw -B verify` on every pull request.

## Try it

### Exercise 6.1 ★ Read the coordinates

Open `pom.xml` at `book-m6-final`. What are the project's `groupId`, `artifactId` and version? Which Java version does it compile for?

*Solution:* Appendix C, Exercise 6.1.

### Exercise 6.2 ★ Build it

With Java 25 installed and the project cloned, run `./mvnw compile`, then `./mvnw package -DskipTests`. Where does the JAR appear?

*Solution:* Appendix C, Exercise 6.2.

### Exercise 6.3 ★★ Why pin Tomcat?

Read the comment above `tomcat.version`. In three sentences, explain what the project did, why, and when the override should be removed.

*Solution:* Appendix C, Exercise 6.3.

## Summary

- A build tool fetches libraries, compiles, tests and packages the same way everywhere.
- `pom.xml` gives the project's coordinates, parent, properties and dependencies.
- Dependencies come from Maven Central; scopes control where they are used.
- `mvnw` pins Maven 3.9.16 so every machine builds alike.
- Phases run in order; the layout follows Maven's conventions.

## Further reading

- *Apache Maven Project*, "Maven in 5 Minutes." https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html
- *Apache Maven Project*, "Introduction to the Build Lifecycle." https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html
- *Apache Maven Project*, "Maven Wrapper." https://maven.apache.org/tools/wrapper/
