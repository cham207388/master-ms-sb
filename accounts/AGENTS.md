# Accounts Microservice - AI Agent Guidelines & Context

Welcome to the **Accounts Microservice** codebase (Domain: Customer Onboarding & Account Management). This document provides standalone context, technical conventions, and operational rules for AI agents working within this service.

---

## 🏛 Domain Boundaries & Architecture

The **Accounts Microservice** manages customer registration, profile metadata, and bank account lifecycles.

- **Service Port**: `8091`
- **Database**: PostgreSQL 18 on host port `5423` (Database name: `accounts`)
- **Package Base**: `com.abcham.accounts`
- **Entities**:
  - `Customer`: `customer_id` (PK), `name`, `email`, `mobile_number`, audit fields.
  - `Accounts`: `account_number` (PK), `customer_id` (FK), `account_type`, `branch_address`, `communication_sw`, audit fields.
- **Central Infrastructure Dependencies**:
  - **Spring Cloud Config Server**: Port `8071` (`/accounts/default`)
  - **Spring Cloud Netflix Eureka**: Port `8070` (`EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://localhost:8070/eureka/`)
  - **Apache Kafka**: Host `localhost:9092` / Compose `kafka:19092` (Spring Cloud Stream: `send-communication` out, `communication-sent` in)

---

## ⚙️ Tech Stack & Toolchain Standards

- **Java Standard**: Java 25 (`JavaLanguageVersion.of(25)` in `build.gradle`).
- **Framework**: Spring Boot `4.1.0` (Spring Web MVC, Data JPA, Actuator, Flyway).
- **Spring Cloud**: Spring Cloud `2025.1.2` (`spring-cloud-starter-config`, `spring-cloud-starter-netflix-eureka-client`, `spring-cloud-stream-binder-kafka`).
- **Database**: PostgreSQL 18 Alpine (`postgres:18-alpine`).
- **Database Migration**: Flyway (`org.flywaydb:flyway-database-postgresql`), migrations located at `src/main/resources/db/migration/V1__init.sql`.
- **API Documentation**: SpringDoc OpenAPI 3.0 (`springdoc-openapi-starter-webmvc-ui:3.0.2`).
- **Testing**: Spring Boot Testcontainers (`postgresql:1.20.4`, `@ServiceConnection`) and JUnit 5.
- **Containerization**: Multi-stage Dockerfile (`eclipse-temurin:25-jdk-alpine` -> `eclipse-temurin:25-jre-alpine`) executing under user `producer:producer`.

---

## 🛠 Command Conventions for AI Agents

Run all build and execution commands within the `accounts` directory:

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
   - Manifests: [`accounts/k8s/`](k8s/) — `db.yml`, `deployment.yml`, `service.yml`
   - Apply: `make k8s-accounts` (from repo root) or `kubectl apply -f accounts/k8s/`
   - Shared ConfigMap: `securedbank-configmap` (`make k8s-configmap` first)
   - Guide: [`docs/kubernetes.md`](../docs/kubernetes.md)

4. **Environment Configuration**:
   - `SPRING_DATASOURCE_URL` (default: `jdbc:postgresql://localhost:5423/accounts` / `jdbc:postgresql://accounts-db:5432/accounts`)
   - `SPRING_DATASOURCE_USERNAME` (default: `postgres`)
   - `SPRING_DATASOURCE_PASSWORD` (default: `postgres`)
   - `SPRING_CONFIG_IMPORT` (default: `optional:configserver:http://localhost:8071/` or `optional:configserver:http://config-server:8071/`)
   - `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` (default: `http://localhost:8070/eureka/` or `http://eureka-server:8070/eureka/`)
   - `KAFKA_BROKER` (default: `localhost:9092` / Compose: `kafka:19092`)

---

## 📐 REST API & Code Conventions

1. **Routing**: Base path `@RequestMapping(path = "/api/accounts", produces = {MediaType.APPLICATION_JSON_VALUE})`.
2. **Endpoints**:
   - `POST /api/accounts/create` - Onboard new customer and open account
   - `GET /api/accounts/fetch` - Fetch customer & account details by `mobileNumber`
   - `PUT /api/accounts/update` - Update customer & account details
   - `DELETE /api/accounts/delete` - Delete customer & account by `mobileNumber`
3. **DTO & Validation**:
   - Request payloads must use `@Valid`.
   - Mobile numbers validated via `@Pattern(regexp = "(^$|[0-9]{10})", message = "Mobile number must be 10 digits")`.
   - Standard responses: `ResponseDto` (status/message) and `ErrorResponseDto` (error metadata).
4. **Flyway Migrations**:
   - Never modify existing migration scripts (`V1__init.sql`). Create new versioned files (`V2__description.sql`).
