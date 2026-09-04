# Cards Microservice - AI Agent Guidelines & Context

Welcome to the **Cards Microservice** codebase (Domain: Credit/Debit Card Issuance & Limit Tracking). This document provides standalone context, technical conventions, and operational rules for AI agents working within this service.

---

## 🏛 Domain Boundaries & Architecture

The **Cards Microservice** manages credit and debit card issuance, limit allocation, available balance tracking, and usage metrics.

- **Service Port**: `8092`
- **Database**: PostgreSQL 18 on host port `5424` (Database name: `cards`)
- **Package Base**: `com.abcham.cards`
- **Entities**:
  - `Cards`: `card_id` (PK, Identity), `mobile_number`, `card_number` (Unique), `card_type`, `total_limit`, `amount_used`, `available_amount`, audit fields.
- **Central Infrastructure Dependencies**:
  - **Spring Cloud Config Server**: Port `8071` (`/cards/default`)
  - **Spring Cloud Netflix Eureka**: Port `8070` (`EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://localhost:8070/eureka/`)

---

## ⚙️ Tech Stack & Toolchain Standards

- **Java Standard**: Java 25 (`JavaLanguageVersion.of(25)` in `build.gradle`).
- **Framework**: Spring Boot `4.1.0` (Spring Web MVC, Data JPA, Actuator, Flyway).
- **Spring Cloud**: Spring Cloud `2025.1.2` (`spring-cloud-starter-config`, `spring-cloud-starter-netflix-eureka-client`).
- **Database**: PostgreSQL 18 Alpine (`postgres:18-alpine`).
- **Database Migration**: Flyway (`org.flywaydb:flyway-database-postgresql`), migrations located at `src/main/resources/db/migration/V1__init.sql`.
- **API Documentation**: SpringDoc OpenAPI 3.0 (`springdoc-openapi-starter-webmvc-ui:3.0.2`).
- **Testing**: Spring Boot Testcontainers (`postgresql:1.20.4`, `@ServiceConnection`) and JUnit 5.
- **Containerization**: Multi-stage Dockerfile (`eclipse-temurin:25-jdk-alpine` -> `eclipse-temurin:25-jre-alpine`) executing under user `producer:producer`.

---

## 🛠 Command Conventions for AI Agents

Run all build and execution commands within the `cards` directory:

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
   - Manifests: [`cards/k8s/`](k8s/) — `db.yml`, `deployment.yml`, `service.yml` (ClusterIP), `networkpolicy.yml`
   - Apply: `make k8s-cards` or `kubectl apply -f cards/k8s/`
   - Shared ConfigMap: `securedbank-configmap` (`make k8s-configmap` first)
   - Guide: [`docs/kubernetes.md`](../docs/kubernetes.md)

4. **Environment Configuration**:
   - `SPRING_DATASOURCE_URL` (default: `jdbc:postgresql://localhost:5424/cards` / `jdbc:postgresql://cards-db:5432/cards`)
   - `SPRING_DATASOURCE_USERNAME` (default: `postgres`)
   - `SPRING_DATASOURCE_PASSWORD` (default: `postgres`)
   - `SPRING_CONFIG_IMPORT` (default: `optional:configserver:http://localhost:8071/` or `optional:configserver:http://config-server:8071/`)
   - `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` (default: `http://localhost:8070/eureka/` or `http://eureka-server:8070/eureka/`)

---

## 📐 REST API & Code Conventions

1. **Routing**: Base path `@RequestMapping(path = "/api/cards", produces = {MediaType.APPLICATION_JSON_VALUE})`.
2. **Endpoints**:
   - `POST /api/cards/create` - Issue a new card for a customer by `mobileNumber`
   - `GET /api/cards/fetch` - Fetch card details by `mobileNumber`
   - `PUT /api/cards/update` - Update card limits and details
   - `DELETE /api/cards/delete` - Delete card details by `mobileNumber`
3. **DTO & Validation**:
   - Request payloads must use `@Valid`.
   - Mobile numbers validated via `@Pattern(regexp = "(^$|[0-9]{10})", message = "Mobile number must be 10 digits")`.
   - Standard responses: `ResponseDto` (status/message) and `ErrorResponseDto` (error metadata).
4. **Flyway Migrations**:
   - Never modify existing migration scripts (`V1__init.sql`). Create new versioned files (`V2__description.sql`).
