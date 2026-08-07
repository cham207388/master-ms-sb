# Eureka Server - AI Agent Guidelines & Context

Welcome to the **Eureka Server** codebase (Domain: Service Discovery & Registration). This document provides standalone context, technical conventions, and operational rules for AI agents working within this service.

---

## 🏛 Domain Boundaries & Architecture

The **Eureka Server** operates as a centralized Netflix Eureka service registry where all banking platform microservices (`accounts`, `cards`, `loans`) dynamically register themselves to enable service-to-service discovery, load balancing, and dynamic routing.

- **Server Port**: `8070`
- **Dashboard UI**: [http://localhost:8070](http://localhost:8070)
- **REST Base Path**: `/eureka/` (Registration POST endpoint: `http://eureka-server:8070/eureka/apps/{APP_NAME}`)
- **Package Base**: `com.abcham.eurekaserver`
- **Annotations**: `@EnableEurekaServer` on `EurekaServerApplication.java`.
- **Central Infrastructure Dependencies**:
  - **Spring Cloud Config Server**: Port `8071` (Imports configuration from Config Server)

---

## ⚙️ Tech Stack & Toolchain Standards

- **Java Standard**: Java 25 (`JavaLanguageVersion.of(25)` in `build.gradle`).
- **Framework**: Spring Boot `4.1.0` (Spring Actuator).
- **Spring Cloud**: Spring Cloud `2025.1.2` (`spring-cloud-starter-netflix-eureka-server`, `spring-cloud-starter-config`).
- **Containerization**: Multi-stage Dockerfile (`eclipse-temurin:25-jdk-alpine` -> `eclipse-temurin:25-jre-alpine`) executing under user `producer:producer`.

---

## 🛠 Command Conventions for AI Agents

Run all build and execution commands within the `eureka-server` directory:

1. **Gradle Build & Test**:
   ```bash
   ./gradlew clean build
   ./gradlew test
   ./gradlew bootRun
   ```

2. **Docker Compose**:
   ```bash
   docker compose up -d
   docker compose down -v
   ```

3. **Environment Configuration**:
   - `CONFIG_SERVER_URL` (default: `http://localhost:8071/` or `http://config-server:8071/`)
   - `EUREKA_HOSTNAME` (default: `localhost`)
   - `EUREKA_DEFAULT_ZONE` (default: `http://localhost:8070/eureka/`)

---

## 📐 Operations & Endpoint Conventions

1. **Eureka Server Dashboard**:
   - Available at `http://localhost:8070` displaying active instances, instance IDs, status (UP/DOWN), and host metadata.
2. **Eureka REST Registration Endpoint**:
   - Clients send heartbeats and registration POST payloads to `/eureka/apps/{APP_NAME}`.
   - Successful registration returns **HTTP 204 No Content**.
3. **Actuator & Health Checks**:
   - `GET /actuator/health` - Health status
   - `GET /actuator/health/readiness` - Readiness probe (Wget healthcheck used by Docker Compose)
