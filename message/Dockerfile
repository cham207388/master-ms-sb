FROM eclipse-temurin:25-jdk-alpine AS build

WORKDIR /app

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle/
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

COPY src ./src/
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:25-jre-alpine

RUN addgroup -S -g 1000 message && \
    adduser -S -u 1000 -G message message && \
    mkdir -p /app/libs && \
    wget -q https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.30.0/opentelemetry-javaagent.jar -O /app/libs/opentelemetry-javaagent.jar && \
    chown -R message:message /app

WORKDIR /app
COPY --from=build --chown=message:message /app/build/libs/*.jar app.jar

USER message

ENTRYPOINT ["java", "-jar", "app.jar"]
