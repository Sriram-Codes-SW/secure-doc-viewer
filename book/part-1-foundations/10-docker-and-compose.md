<!-- chapter: 10 | part: I | owner: writer-foundations | tag: book-m6-final | status: draft -->
# Chapter 10: Containers and Docker

The app needs MySQL, and later a web server and more. Installing each by hand on every machine would be slow and error-prone. Docker lets you run each program in a sealed, repeatable package. This chapter teaches images, containers, volumes and Docker Compose, using the project's real `docker-compose.yml` and `Dockerfile`, and finishes Part I.

## Learning objectives

By the end of this chapter, you will be able to:

- Explain why containers exist and how they differ from virtual machines.
- Distinguish an image, a container, a volume and a network.
- Start the project's MySQL database with Docker Compose.
- Read a Compose file, including ports, volumes, environment, profiles and health checks.
- Explain how secrets reach a container without being committed.
- Read a two-stage `Dockerfile`.

## Prerequisites

- Chapter 2: The command line and your files
- Chapter 8: How the web works
- Chapter 9: SQL and MySQL

## Beginner tier: Packages that run anywhere

### 10.1 Why containers exist

"It works on my machine" is a classic failure. The database version differs, a library is missing, a setting is different. **Docker** solves this by running programs in **containers**: isolated processes that bring their own files and settings. A container sees its own small filesystem and its own network, and can't see your other files unless you allow it.

A container isn't a full virtual computer. It shares your computer's operating system core and starts in seconds, where a virtual machine boots a whole operating system. The cost is weaker isolation than a virtual machine offers, which is one reason the project also hardens its images (Chapter 33).

**Analogy.** A container is a shipping container: it holds anything, looks the same to every ship and crane, and needs no knowledge of what's inside. The analogy breaks down because a shipping container is inert cargo, while a Docker container is a running program.

### 10.2 Images, containers, volumes and networks

Four words carry the whole idea.

- An **image** is a read-only template containing a program and everything it needs. `mysql:8.4` is an image.
- A **container** is a running instance of an image. You can start many containers from one image.
- A **volume** is storage that lives outside a container. A container's own files vanish when it is removed; a volume survives. The database's data must live in a volume, or every restart would erase the accounts.
- A **network** connects containers so they can reach each other by name.

Docker fetches images from a **registry**, a public store; Docker Hub is the default. Images have a **tag** after the colon (`mysql:8.4`), the same idea as Chapter 7's Git tags but for images.

Check Docker works:

```bash
docker --version
docker run --rm hello-world
```

## Intermediate tier: Docker Compose

### 10.3 Running MySQL 8.4 with Docker

You could start MySQL with one long `docker run` command, but you'd have to retype it exactly. **Docker Compose** describes one or more containers in a file, `docker-compose.yml`, and starts them with one command. At `book-m1-accounts`, where the database first appears, the file held only MySQL. From the repository folder, with your `.env` in place (Chapter 2):

```bash
docker compose up -d
```

`up` starts what the file describes, and `-d` (detached) runs it in the background. Then check on it, and stop it when you're done:

```bash
docker compose ps
docker compose down
```

`down` removes the containers but keeps the volume, so your data survives. Adding `-v` would delete the volumes too, and with them all the data, so use it only when you mean to.

### 10.4 Compose files, profiles and health checks

Here is the MySQL service from the final version of the file.

**Listing 10.1 — `docker-compose.yml` (book-m6-final, excerpt: service `mysql`)**

```yaml
services:
  mysql:
    image: mysql:8.4@sha256:85b9bf2e29cf836ecb8c2a15a935d4ba0c606631dff1dd79531a11983c638f2a
    container_name: securedocs-mysql
    restart: unless-stopped
    environment:
      MYSQL_DATABASE: ${DB_NAME:-securedocs}
      MYSQL_USER: ${DB_USERNAME:-securedocs}
      MYSQL_PASSWORD: ${DB_PASSWORD:?Set DB_PASSWORD in .env}
      MYSQL_ROOT_PASSWORD: ${DB_ROOT_PASSWORD:?Set DB_ROOT_PASSWORD in .env}
    ports:
      # Bound to localhost only; the database is never exposed to the network.
      - "127.0.0.1:${DB_PORT:-3306}:3306"
    volumes:
      - mysql-data:/var/lib/mysql
    healthcheck:
      # $$ defers expansion to the container, so the password never appears in
      # the stored command (docker inspect); MYSQL_PWD keeps it off the argv.
      test: ["CMD-SHELL", "MYSQL_PWD=\"$$MYSQL_ROOT_PASSWORD\" mysqladmin ping -h localhost -u root --silent"]
      interval: 5s
      timeout: 5s
      retries: 20
```

*Path: `docker-compose.yml`*

Line by line:

- `image: mysql:8.4@sha256:...` names the image, its tag, and a **digest**: a fingerprint of the exact image contents. A tag can be re-pointed to a newer image; a digest can't. Pinning by digest means a rebuild gets exactly the reviewed image, as the project's own comment in the file explains.
- `restart: unless-stopped` restarts the container after a crash or reboot, unless you stopped it on purpose.
- `environment:` sets environment variables inside the container (Chapter 2). The MySQL image reads them on first start to create the database and user. `${DB_NAME:-securedocs}` means "the value of `DB_NAME` from `.env`, or `securedocs` if unset". `${DB_PASSWORD:?Set DB_PASSWORD in .env}` means "required": Compose stops with that message if the variable is missing.
- `ports: "127.0.0.1:3306:3306"` publishes container port 3306 to your computer's port 3306, but only on `127.0.0.1`. That means no other machine can connect (Chapter 2).
- `volumes: mysql-data:/var/lib/mysql` attaches a named volume at the folder where MySQL keeps its data. The volume is declared at the bottom of the file.
- `healthcheck` runs a command every 5 seconds to test whether MySQL is really answering. A container can be running but not yet ready; the health check tells other services when they can begin. The `$$` and `MYSQL_PWD` details keep the root password out of the stored command, which `docker inspect` would otherwise reveal.

A Compose file can hold several services. The final file adds `app` (the backend), `web` (the frontend server) and `tls` (an optional HTTPS front end). It uses **profiles** so that you start only what you need:

**Listing 10.2 — `docker-compose.yml` (book-m6-final, excerpt: header comment)**

```yaml
# Local development:  `docker compose up -d`                    -> MySQL only (run the app with mvnw / ng serve)
# Full stack:         `docker compose --profile full up -d --build` -> MySQL + API + web at http://localhost:8081
# With HTTPS:         `docker compose --profile full --profile tls up -d --build` -> also https://localhost:8443
```

*Path: `docker-compose.yml`*

A service with `profiles: ["full"]` starts only when you ask for that profile, so plain `docker compose up -d` gives you just MySQL, which is what you want while developing. The `app` service also says `depends_on: mysql: condition: service_healthy`, so the backend waits until MySQL's health check passes. <!-- source: docker-compose.yml at book-m6-final -->

### 10.5 Environment variables and secrets in containers

The Compose file contains no passwords. It reads `${DB_PASSWORD}` from `.env`, the git-ignored file from Chapter 2, and passes it into the container. The `app` service uses `env_file: .env` for the same purpose. The result: secrets live in one local file, and the repository holds only the template.

**We simplify here.** An environment variable is visible to anyone who can inspect the container. For a small deployment that's acceptable; larger systems use a dedicated secrets store. Chapter 37 compares the options.

## Advanced tier: Building your own image

### 10.6 Building your own image (a first Dockerfile)

For MySQL you used a ready-made image. For the backend, the project builds its own from a **Dockerfile**: a recipe of steps. Each step adds a layer to the image, and Docker reuses layers that haven't changed, which makes rebuilds fast. The project's file has two stages.

**Listing 10.3 — `Dockerfile` (book-m6-final, simplified: image digests replaced by `<digest>`; comments, the font and user setup, and the `JAVA_TOOL_OPTIONS` value of `ENV` omitted)**

```dockerfile
FROM eclipse-temurin:25-jdk@sha256:<digest> AS build
WORKDIR /src
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline
COPY src src
RUN ./mvnw -B -q -DskipTests package

FROM eclipse-temurin:25-jre@sha256:<digest>
# ...
WORKDIR /app
COPY --from=build /src/target/secure-doc-viewer.jar app.jar
USER app
ENV STORAGE_ROOT=/data/storage
VOLUME /data/storage
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

*Path: `Dockerfile`*

Reading it:

- `FROM` picks a base image. The first stage uses a full JDK (`25-jdk`) because building needs the compiler. `AS build` names it.
- `WORKDIR` sets the working folder. `COPY` brings files in. `RUN` executes a command at build time: here, the Maven wrapper from Chapter 6.
- The order is deliberate. Copying `pom.xml` and downloading dependencies first means that layer is cached until `pom.xml` changes; copying `src` later means a code change doesn't repeat the downloads. The project's own comment says: "Dependencies first, so they stay cached until pom.xml changes."
- The second `FROM` starts a fresh image with only a JRE (`25-jre`, Java without the compiler). `COPY --from=build` takes just the finished JAR from the first stage. This is a **multi-stage build**: the final image is smaller and contains no build tools, so there is less to attack.
- `USER app` runs the program as an unprivileged user rather than as the all-powerful `root`. If an attacker takes over the program, they gain less.
- `ENV` sets a variable, `VOLUME` marks where data lives, `EXPOSE` documents the port, and `ENTRYPOINT` is the command that starts the app.

<!-- source: Dockerfile at book-m6-final -->

## In this project

- `docker-compose.yml`: MySQL, and behind profiles the backend, web server and TLS front end. Milestone `book-m1-accounts` has the MySQL-only version; `book-m5-platform` added the full stack.
- `Dockerfile` and `frontend/Dockerfile`: the backend and frontend images.
- `.dockerignore`: keeps `.env`, `storage/` and `target/` out of the build context, so secrets never enter an image. <!-- source: .dockerignore at book-m6-final -->
- `.env.example`: the template for the variables Compose reads.

## Try it

### Exercise 10.1 ★ Image or container?

For each, say image, container, volume or network: `mysql:8.4`; the running `securedocs-mysql`; `mysql-data`.

*Solution:* Appendix C, Exercise 10.1.

### Exercise 10.2 ★ Start the database

Copy `.env.example` to `.env`, choose your own values for the two password variables (never reuse a real password), and run `docker compose up -d`. Use `docker compose ps` to see the health status. Then run `docker compose down`, start again, and explain why your data would survive.

*Solution:* Appendix C, Exercise 10.2.

### Exercise 10.3 ★★ Read the ports

In Listing 10.1, what would change if the ports line were `"3306:3306"`? Why did the project add the `127.0.0.1` prefix?

*Solution:* Appendix C, Exercise 10.3.

## Summary

- Docker runs programs in isolated, repeatable containers created from images.
- Volumes keep data across container removal; networks connect containers.
- Compose describes services in one file; profiles select which start; health checks say when a service is ready.
- Secrets come from a git-ignored `.env`, never from the repository.
- A multi-stage Dockerfile builds in one image and ships a smaller one.

## Further reading

- *Docker Docs*, "Get started." https://docs.docker.com/get-started/
- *Docker Docs*, "Compose file reference." https://docs.docker.com/reference/compose-file/
- *Docker Docs*, "Dockerfile reference." https://docs.docker.com/reference/dockerfile/
- *MySQL 8.4 Reference Manual*, "Deploying MySQL on Linux with Docker." https://dev.mysql.com/doc/refman/8.4/en/linux-installation-docker.html
