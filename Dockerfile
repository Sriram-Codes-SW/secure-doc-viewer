# Backend image: build with the Maven wrapper on a JDK, run on a slim JRE.
# Build:  docker build -t secure-doc-viewer-api .
# Usually run via `docker compose --profile full up -d --build` (see docker-compose.yml).

FROM eclipse-temurin:25-jdk AS build
WORKDIR /src
# Dependencies first, so they stay cached until pom.xml changes.
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline
COPY src src
RUN ./mvnw -B -q -DskipTests package

FROM eclipse-temurin:25-jre
# The watermark and PDF rendering use Java2D text, which needs real fonts in a headless container.
RUN apt-get update \
    && apt-get install -y --no-install-recommends fontconfig fonts-dejavu-core \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system app && useradd --system --gid app --home /app app \
    && mkdir -p /app /data/storage && chown -R app:app /app /data
WORKDIR /app
COPY --from=build /src/target/secure-doc-viewer.jar app.jar
USER app
ENV STORAGE_ROOT=/data/storage \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -Djava.awt.headless=true"
VOLUME /data/storage
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
