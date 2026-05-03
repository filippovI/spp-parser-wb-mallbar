# Multi-stage Dockerfile for Maven + Java 17 (builds JAR inside the image)

# ===== Stage 1: Build with Maven =====
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /src

# Copy only pom.xml first to leverage dependency cache
COPY pom.xml ./
# Pre-fetch dependencies into cache
RUN --mount=type=cache,target=/root/.m2 mvn -B -q -DskipTests=true dependency:go-offline

# Copy sources and build
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests=true package && \
    JAR=$(ls target/*.jar | grep -v -E '(sources|javadoc|original)' | head -n1) && \
    echo "Artifact: $JAR" && cp "$JAR" /tmp/app.jar

# ===== Stage 2: Runtime (JRE only) =====
FROM eclipse-temurin:17-jre-alpine
ENV TZ=Europe/Moscow \
    JAVA_OPTS="-Xms128m -Xmx512m -XX:+UseG1GC -XX:MaxRAMPercentage=75" \
    GOOGLE_APPLICATION_CREDENTIALS="/app/credentials.json"

RUN addgroup -S app && adduser -S app -G app \
  && apk add --no-cache tzdata \
  && mkdir -p /app/build/downloads /app/build/reports \
  && chown -R app:app /app \
  && ln -snf /usr/share/zoneinfo/$TZ /etc/localtime \
  && echo $TZ > /etc/timezone

WORKDIR /app

COPY --from=builder /tmp/app.jar /app/app.jar

USER app
ENTRYPOINT ["/bin/sh","-c","java $JAVA_OPTS -jar /app/app.jar"]

WORKDIR /app
COPY --from=builder /tmp/app.jar /app/app.jar
# credentials.json будет монтироваться с хоста в /app/credentials.json

USER app
ENTRYPOINT ["/bin/sh","-c","java $JAVA_OPTS -jar /app/app.jar"]