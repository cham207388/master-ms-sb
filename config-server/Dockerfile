FROM eclipse-temurin:25-jdk-alpine AS build

# Set working directory in the container
WORKDIR /app

# Copy Gradle wrapper files and other dependencies
COPY gradlew settings.gradle build.gradle /app/
COPY gradle /app/gradle/

# Download dependencies to speed up builds
RUN ./gradlew dependencies --no-daemon

# Copy the project source code
COPY src /app/src

# Build the Spring Boot application
RUN ./gradlew build --no-daemon -x test

# Second stage: runtime image
FROM eclipse-temurin:25-jre-alpine

RUN addgroup -S -g 1000 producer && \
    adduser -S -u 1000 -G producer producer && \
    mkdir -p /app/libs && \
    chown -R producer:producer /app

ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.30.0/opentelemetry-javaagent.jar /app/libs/opentelemetry-javaagent-2.30.0.jar

USER producer

# Set working directory
WORKDIR /app

# Copy the Spring Boot JAR file from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose the application port
EXPOSE 8071

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]