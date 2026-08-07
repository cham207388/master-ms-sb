FROM ghcr.io/graalvm/native-image-community:23-ol9 AS build

# Set working directory in the container
WORKDIR /app

# Copy Gradle wrapper files and other dependencies
COPY gradlew settings.gradle build.gradle /app/
COPY gradle /app/gradle/

# Download dependencies to speed up builds
RUN ./gradlew dependencies --no-daemon

# Copy the project source code
COPY src /app/src

# Build the GraalVM Native Executable
RUN ./gradlew nativeCompile --no-daemon -x test

# Second stage: runtime image
FROM debian:bookworm-slim

RUN apt-get update && apt-get install -y --no-install-recommends curl wget && \
    rm -rf /var/lib/apt/lists/* && \
    groupadd -g 1000 eureka && \
    useradd -u 1000 -g eureka -m eureka && \
    mkdir -p /app && \
    chown eureka:eureka /app

USER eureka

# Set working directory
WORKDIR /app

# Copy the native executable from the build stage
COPY --from=build /app/build/native/nativeCompile/eureka-server eureka-server

# Expose the application port
EXPOSE 8070

# Run the native binary executable
ENTRYPOINT ["/app/eureka-server"]