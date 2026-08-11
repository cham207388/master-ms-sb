# Gateway Server - AI Agent Guidelines & Context

Welcome to the **Gateway Server** codebase (Domain: Edge API Gateway, Routing & Load Balancing). This document provides standalone context, technical conventions, and operational rules for AI agents working within this service.

---

## 🏛 Domain Boundaries & Architecture

The **Gateway Server** acts as the central reactive edge API gateway for the SecuredBank platform, providing uniform request entry, path rewriting, correlation ID tracing, client-side load balancing, and fault tolerance via Resilience4j circuit breakers.

- **Server Port**: `8072`
- **Package Base**: `com.abcham.gatewayserver`
- **Annotations**: `@SpringBootApplication` on `GatewayServerApplication.java`
- **Route Locator & Path Rewriting**:
  - `/ACCOUNTS/**` & `/accounts/**` -> rewrites path and load balances to `lb://ACCOUNTS`
  - `/CARDS/**` & `/cards/**` -> rewrites path and load balances to `lb://CARDS`
  - `/LOANS/**` & `/loans/**` -> rewrites path and load balances to `lb://LOANS`
- **Distributed Tracing**: Managed automatically via `opentelemetry-javaagent` runtime agent for cross-service W3C `traceparent` context propagation.
- **Fault Tolerance & Fallback** (`controller` package):
  - Resilience4j CircuitBreaker filters (`accountsCircuitBreaker`, `cardsCircuitBreaker`, `loansCircuitBreaker`).
  - `FallbackController`: Handles `/accounts-fallback`, `/cards-fallback`, and `/loans-fallback` when downstream microservices time out or fail.
- **Central Infrastructure Dependencies**:
  - **Spring Cloud Config Server**: Port `8071` (`/gateway-server/default`)
  - **Spring Cloud Netflix Eureka**: Port `8070` (`EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://localhost:8070/eureka/`)

---

## ⚙️ Tech Stack & Toolchain Standards

- **Java Standard**: Java 25 (`JavaLanguageVersion.of(25)` in `build.gradle`).
- **Framework**: Spring Boot `4.1.0` (Spring Cloud Gateway WebFlux, Actuator).
- **Spring Cloud**: Spring Cloud `2025.1.2` (`spring-cloud-starter-gateway-server-webflux`, `spring-cloud-starter-circuitbreaker-reactor-resilience4j`, `spring-cloud-starter-loadbalancer`, `spring-cloud-starter-netflix-eureka-client`, `spring-cloud-starter-config`).
- **Containerization**: Multi-stage Dockerfile (`eclipse-temurin:25-jdk-alpine` -> `eclipse-temurin:25-jre-alpine`) executing under user `gateway:gateway`.

---

## 🛠 Command Conventions for AI Agents

Run all build and execution commands within the `gateway-server` directory:

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
   - `SPRING_CONFIG_IMPORT` (default: `optional:configserver:http://localhost:8071/` or `optional:configserver:http://config-server:8071/`)
   - `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` (default: `http://localhost:8070/eureka/` or `http://eureka-server:8070/eureka/`)
   - `SPRING_DATA_REDIS_HOST` (default: `localhost` or `redis`)
   - `SPRING_DATA_REDIS_PORT` (default: `6379`)

---

## 📐 Operations, Routing & Actuator Conventions

1. **Gateway Routes Actuator**:
   - `GET /actuator/gateway/routes` - Lists active gateway routes, predicates, filters, and target URIs.
2. **Resilience4j Circuit Breakers & Monitoring**:
   - `GET /actuator/circuitbreakers` - Displays state of active circuit breakers (`CLOSED`, `OPEN`, `HALF_OPEN`).
   - `GET /actuator/circuitbreakerevents` - Displays historical event log for circuit breaker state transitions and errors.
3. **Actuator & Health Checks**:
   - `GET /actuator/health` - Comprehensive health status including Resilience4j health indicators.
   - `GET /actuator/info` - Application description and build version metadata.
