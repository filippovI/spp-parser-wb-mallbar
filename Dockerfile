# Multi-stage Dockerfile for Maven + Java 17 + Chromium on Debian (no Snap)
# Stage 1: build JAR inside the image
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /src

# Cache dependencies first
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 mvn -B -q -DskipTests=true dependency:go-offline

# Copy sources and build
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests=true package && \
    JAR=$(ls target/*.jar | grep -v -E '(sources|javadoc|original)' | head -n1) && \
    echo "Artifact: $JAR" && cp "$JAR" /tmp/app.jar

# Stage 2: runtime — Debian + OpenJDK 17 + Chromium/Chromedriver via APT (no snap)
FROM debian:bookworm-slim

ENV TZ=Europe/Moscow \
    GOOGLE_APPLICATION_CREDENTIALS="/app/credentials.json" \
    JAVA_OPTS="-Djava.net.preferIPv4Stack=true -Dwebdriver.chrome.driver=/usr/bin/chromedriver -Dselenide.browser=chrome -Dselenide.browserBinary=/usr/bin/chromium -Dselenide.headless=true -Dselenide.downloadsFolder=/app/build/downloads -Dselenide.reportsFolder=/app/build/reports -Xms128m -Xmx512m -XX:+UseG1GC -XX:MaxRAMPercentage=75"

# Create user, install OpenJDK + Chromium + Chromedriver and required libs
RUN useradd -m -s /bin/sh app \
    && apt-get update \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
         openjdk-17-jre-headless \
         chromium chromium-driver \
         libnss3 libx11-xcb1 libxcomposite1 libxdamage1 libxrandr2 libgbm1 \
         libgtk-3-0 libatk-bridge2.0-0 libasound2 libxshmfence1 xdg-utils \
         fonts-liberation fonts-noto-color-emoji tzdata ca-certificates \
    && ln -snf /usr/share/zoneinfo/$TZ /etc/localtime \
    && echo $TZ > /etc/timezone \
    && mkdir -p /app/build/downloads /app/build/reports \
    && chown -R app:app /app \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy artifact from builder
COPY --from=builder /tmp/app.jar /app/app.jar

USER app
ENTRYPOINT ["/bin/sh","-c","java $JAVA_OPTS -jar /app/app.jar"]