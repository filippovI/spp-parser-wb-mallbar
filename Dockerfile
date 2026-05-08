# --- build stage ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Кэш зависимостей
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests dependency:go-offline

# Сборка
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests clean package

# Найти собранный JAR (предпочтительно *-shaded.jar) и скопировать под единым именем
RUN set -eux; \
    JAR_PATH=$(ls target/*-shaded.jar 2>/dev/null || ls target/*.jar | head -n 1); \
    echo "Using JAR: $JAR_PATH"; \
    test -n "$JAR_PATH"; \
    mkdir -p /opt/app; \
    cp "$JAR_PATH" /opt/app/app.jar

# --- runtime stage ---
FROM eclipse-temurin:17-jre-jammy
ENV LANG=C.UTF-8

# Системные библиотеки и Google Chrome
RUN apt-get update && apt-get install -y --no-install-recommends \
    wget gnupg ca-certificates fonts-liberation \
    libasound2 libatk-bridge2.0-0 libnss3 libxcomposite1 libxrandr2 \
    libxss1 libxtst6 libgtk-3-0 libxdamage1 libgbm1 libu2f-udev xdg-utils \
  && rm -rf /var/lib/apt/lists/*

RUN wget -q -O - https://dl.google.com/linux/linux_signing_key.pub | gpg --dearmor -o /usr/share/keyrings/google-linux-signing-keyring.gpg && \
    echo "deb [arch=amd64 signed-by=/usr/share/keyrings/google-linux-signing-keyring.gpg] http://dl.google.com/linux/chrome/deb/ stable main" \
      > /etc/apt/sources.list.d/google-chrome.list && \
    apt-get update && apt-get install -y --no-install-recommends google-chrome-stable && \
    rm -rf /var/lib/apt/lists/*

# Непривилегированный пользователь
RUN useradd -m appuser
USER appuser

WORKDIR /app

# Копируем единый JAR
COPY --from=build /opt/app/app.jar /app/app.jar

# Директории для ключей и профиля браузера
RUN mkdir -p /app/creds /app/browser_data

# Переменные окружения по умолчанию
ENV GOOGLE_APPLICATION_CREDENTIALS=/app/creds/credentials.json \
    BROWSER_DATA_DIR=/app/browser_data \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -Dfile.encoding=UTF-8 -Dselenide.headless=true" \
    TELEGRAM_TOKEN=8513691300:AAEGP1RhZBK-p0To4ctdVtyYgZ07qWAJtdE

ENTRYPOINT ["java","-jar","/app/app.jar"]