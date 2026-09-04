# Config Server - AI Agent Guidelines & Context

Welcome to the **Spring Cloud Config Server** codebase (Domain: Centralized Configuration). This document provides standalone context, technical conventions, and operational rules for AI agents working within this service.

---

## 🏛 Domain Boundaries & Architecture

The **Config Server** serves centralized, version-controlled environment configuration to all microservices in the banking platform (`accounts`, `cards`, `loans`, `eureka-server`, `gateway-server`).

- **Service Port**: `8071`
- **Package Base**: `com.abcham.configserver`
- **Config Storage Backend**: Git (`https://github.com/cham207388/config-server-sb-sc-ms.git`, default branch: `main`).
- **Exposed Configurations**:
  - `http://localhost:8071/accounts/default`
  - `http://localhost:8071/cards/default`
  - `http://localhost:8071/loans/default`
  - `http://localhost:8071/eureka-server/default`

---

## ⚙️ Tech Stack & Toolchain Standards

- **Java Standard**: Java 25 (`JavaLanguageVersion.of(25)` in `build.gradle`).
- **Framework**: Spring Boot `4.1.0` (Spring Actuator).
- **Spring Cloud**: Spring Cloud `2025.1.2` (`spring-cloud-config-server`).
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

3. **Kubernetes (kind)**:
   - Manifests: [`config-server/k8s/`](k8s/) — `deployment.yml`, `service.yml`
   - Apply: `make k8s-config-server` or `kubectl apply -f config-server/k8s/`
   - Guide: [`docs/kubernetes.md`](../docs/kubernetes.md)

---

## 📐 REST API & Operational Conventions

1. **Config Endpoints**:
   - `GET /{application}/{profile}` (e.g. `/accounts/default`, `/cards/default`, `/loans/default`)
2. **Actuator Endpoints**:
   - `GET /actuator/health` - Health status
   - `GET /actuator/health/readiness` - Container readiness probe
3. **Healthcheck Command**:
   - Readiness probe uses `wget -q --spider http://localhost:8071/actuator/health/readiness || exit 1`.
