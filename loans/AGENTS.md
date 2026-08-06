# Loans Microservice - AI Agent Guidelines & Context

Welcome to the **Loans Microservice** codebase (Domain: Loan Origination & Repayment Tracking). This document provides standalone context, technical conventions, and operational rules for AI agents working within this service.

---

## 🏛 Domain Boundaries & Architecture

The **Loans Microservice** manages customer loans (home, personal, vehicle), repayment tracking, total borrowed limits, and outstanding balances.

- **Service Port**: `8093`
- **Database**: PostgreSQL 18 on host port `5425` (Database name: `loans`)
- **Package Base**: `com.abcham.loans`
- **Entities**:
  - `Loans`: `loan_id` (PK, Identity), `mobile_number`, `loan_number`, `loan_type`, `total_loan`, `amount_paid`, `outstanding_amount`, audit fields.
- **Central Infrastructure Dependencies**:
  - **Spring Cloud Config Server**: Port `8071` (`/loans/default`)
  - **RabbitMQ Bus Broker**: Port `5672` (Event bus for dynamic refresh)

---

## ⚙️ Tech Stack & Toolchain Standards

- **Java Standard**: Java 25 (`JavaLanguageVersion.of(25)` in `build.gradle`).
- **Framework**: Spring Boot `4.1.0` (Spring Web MVC, Data JPA, Actuator, Flyway).
- **Spring Cloud**: Spring Cloud `2025.1.2` (`spring-cloud-starter-config`, `spring-cloud-starter-bus-amqp`).
- **Database**: PostgreSQL 18 Alpine (`postgres:18-alpine`).
- **Database Migration**: Flyway (`org.flywaydb:flyway-database-postgresql`), migrations located at `src/main/resources/db/migration/V1__init.sql`.
- **API Documentation**: SpringDoc OpenAPI 3.0 (`springdoc-openapi-starter-webmvc-ui:3.0.2`).
- **Testing**: Spring Boot Testcontainers (`postgresql:1.20.4`, `@ServiceConnection`) and JUnit 5.
- **Containerization**: Multi-stage Dockerfile (`eclipse-temurin:25-jdk-alpine` -> `eclipse-temurin:25-jre-alpine`) executing under user `producer:producer`.

---

## 🛠 Command Conventions for AI Agents

Run all build and execution commands within the `loans` directory:

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
   - `DB_HOST` (default: `localhost` / `loans-db`)
   - `DB_PORT` (default: `5425` / `5432`)
   - `DB_NAME` (default: `loans`)
   - `DB_USERNAME` (default: `postgres`)
   - `DB_PASSWORD` (default: `postgres`)
   - `CONFIG_SERVER_URL` (default: `http://localhost:8071/` or `http://host.docker.internal:8071/`)
   - `RABBITMQ_HOST` (default: `localhost` or `host.docker.internal`)
   - `RABBITMQ_PORT` (default: `5672`)

---

## 📐 REST API & Code Conventions

1. **Routing**: Base path `@RequestMapping(path = "/api", produces = {MediaType.APPLICATION_JSON_VALUE})`.
2. **Endpoints**:
   - `POST /api/create` - Create a new loan for a customer by `mobileNumber`
   - `GET /api/fetch` - Fetch loan details by `mobileNumber`
   - `PUT /api/update` - Update loan details and repayments
   - `DELETE /api/delete` - Delete loan details by `mobileNumber`
3. **DTO & Validation**:
   - Request payloads must use `@Valid`.
   - Mobile numbers validated via `@Pattern(regexp = "(^$|[0-9]{10})", message = "Mobile number must be 10 digits")`.
   - Standard responses: `ResponseDto` (status/message) and `ErrorResponseDto` (error metadata).
4. **Flyway Migrations**:
   - Never modify existing migration scripts (`V1__init.sql`). Create new versioned files (`V2__description.sql`).
