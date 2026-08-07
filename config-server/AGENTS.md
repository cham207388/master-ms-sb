# Config Server - AI Agent Guidelines & Context

Welcome to the **Spring Cloud Config Server** codebase (Domain: Centralized Configuration & Spring Cloud Bus Broadcasting). This document provides standalone context, technical conventions, and operational rules for AI agents working within this service.

---

## 🏛 Domain Boundaries & Architecture

The **Config Server** serves centralized, version-controlled environment configuration to all microservices in the banking platform (`accounts`, `cards`, `loans`) and manages Spring Cloud Bus AMQP configuration refresh events via RabbitMQ.

- **Service Port**: `8071`
- **Package Base**: `com.abcham.configserver`
- **Config Storage Backend**: Git (`https://github.com/cham207388/config-server-sb-sc-ms.git`, default branch: `main`).
- **Exposed Configurations**:
  - `http://localhost:8071/accounts/default`
  - `http://localhost:8071/cards/default`
  - `http://localhost:8071/loans/default`
- **Event Bus Integration**:
  - Connected to RabbitMQ (`5672`) for broadcasting `/actuator/busrefresh` events to refresh application properties without restarting services.

---

## ⚙️ Tech Stack & Toolchain Standards

- **Java Standard**: Java 25 (`JavaLanguageVersion.of(25)` in `build.gradle`).
- **Framework**: Spring Boot `4.1.0` (Spring Actuator).
- **Spring Cloud**: Spring Cloud `2025.1.2` (`spring-cloud-config-server`, `spring-cloud-starter-bus-amqp`).
- **Message Broker**: RabbitMQ 3.12 Alpine (`rabbitmq:3.12-management`).
- **Containerization**: Multi-stage Dockerfile (`eclipse-temurin:25-jdk-alpine` -> `eclipse-temurin:25-jre-alpine`) executing under user `producer:producer`.

---

## 🛠 Command Conventions for AI Agents

Run all build and execution commands within the `config-server` directory:

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
   - `RABBITMQ_HOST` (default: `localhost` / `rabbit-mq`)
   - `RABBITMQ_PORT` (default: `5672`)
   - `RABBITMQ_USERNAME` (default: `guest`)
   - `RABBITMQ_PASSWORD` (default: `guest`)

---

## 📐 REST API & Operational Conventions

1. **Config Endpoints**:
   - `GET /{application}/{profile}` (e.g. `/accounts/default`, `/cards/default`, `/loans/default`)
2. **Actuator & Bus Endpoints**:
   - `POST /actuator/busrefresh` - Broadcasts refresh event across RabbitMQ to reload configurations dynamically in downstream microservices.
   - `GET /actuator/health` - Health status
   - `GET /actuator/health/readiness` - Container readiness probe
3. **Healthcheck Command**:
   - Readiness probe uses `wget -q --spider http://localhost:8071/actuator/health/readiness || exit 1`.
