# Multi-stage Dockerfile for Maven + Java 17 + Chromium (Debian base)
# Stage 1: build JAR inside the image
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /src

# Cache dependencies
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 mvn -B -q -DskipTests=true dependency:go-offline

# Build
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests=true package && \
    JAR=$(ls target/*.jar | grep -v -E '(sources|javadoc|original)' | head -n1) && \
    echo "Artifact: $JAR" && cp "$JAR" /tmp/app.jar

# Stage 2: runtime (Debian + JRE 17 + Chromium + Chromedriver)
FROM eclipse-temurin:17-jre

ENV TZ=Europe/Moscow \
    JAVA_OPTS="-Djava.net.preferIPv4Stack=true -Dwebdriver.chrome.driver=/usr/bin/chromedriver -Dselenide.headless=true -Dselenide.downloadsFolder=/app/build/downloads -Dselenide.reportsFolder=/app/build/reports -Xms128m -Xmx512m -XX:+UseG1GC -XX:MaxRAMPercentage=75" \
    GOOGLE_APPLICATION_CREDENTIALS="/app/credentials.json"

# Создаём пользователя и ставим браузер с драйвером (совместимые бинарники)
RUN useradd -ms /bin/bash app \
    && apt-get update \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
         chromium chromium-driver fonts-liberation libnss3 ca-certificates tzdata \
    && ln -snf /usr/share/zoneinfo/$TZ /etc/localtime \
    && echo $TZ > /etc/timezone \
    && mkdir -p /app/build/downloads /app/build/reports \
    && chown -R app:app /app \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Копируем собранный артефакт
COPY --from=builder /tmp/app.jar /app/app.jar

# Запускаемся под непривилегированным пользователем
USER app

ENTRYPOINT ["/bin/sh","-c","java $JAVA_OPTS -jar /app/app.jar"]