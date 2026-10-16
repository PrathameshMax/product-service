# syntax=docker/dockerfile:1.7

# ---------- build ----------
FROM maven:3.9-eclipse-temurin-26 AS build
WORKDIR /workspace

COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B -q dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -q package -DskipTests \
 && java -Djarmode=tools -jar target/product-service.jar extract --layers --launcher --destination target/extracted

# ---------- runtime ----------
FROM eclipse-temurin:21-jre-alpine AS runtime

ARG APP_VERSION=local
ENV APP_VERSION=${APP_VERSION} \
    APP_ENV=local \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"

LABEL org.opencontainers.image.title="product-service" \
      org.opencontainers.image.source="https://github.com/PrathameshMax/product-service" \
      org.opencontainers.image.revision="${APP_VERSION}"

RUN addgroup -S app && adduser -S -G app -u 10001 app
WORKDIR /app

COPY --from=build --chown=app:app /workspace/target/extracted/dependencies/ ./
COPY --from=build --chown=app:app /workspace/target/extracted/spring-boot-loader/ ./
COPY --from=build --chown=app:app /workspace/target/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=app:app /workspace/target/extracted/application/ ./

USER 10001
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget -qO- http://127.0.0.1:8080/actuator/health/liveness >/dev/null || exit 1

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
