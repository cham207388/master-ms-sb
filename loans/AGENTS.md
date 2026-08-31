# Loans Microservice - AI Agent Guidelines & Context

Welcome to the **Loans Microservice** codebase (Domain: Loan Origination & Repayment Tracking). This document provides standalone context, technical conventions, and operational rules for AI agents working within this service.

---

## 🏛 Domain Boundaries & Architecture

The **Loans Microservice** manages customer loans (home, personal, vehicle), repayment tracking, total borrowed limits, and outstanding balances.

- **Service Port**: `8093`
- **Database**: Shared PostgreSQL 18 on host port `5423` (Database name: `bank`, Schema: `loans`)
- **Package Base**: `com.abcham.loans`
- **Entities**:
  - `Loans`: `loan_id` (PK, Identity), `mobile_number`, `loan_number`, `loan_type`, `total_loan`, `amount_paid`, `outstanding_amount`, audit fields.
- **Central Infrastructure Dependencies**:
  - **Spring Cloud Config Server**: Port `8071` (`/loans/default`)
  - **Spring Cloud Netflix Eureka**: Port `8070` (`EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://localhost:8070/eureka/`)
  - **RabbitMQ Bus Broker**: Port `5672` (Event bus for dynamic refresh)

---

## ⚙️ Tech Stack & Toolchain Standards

- **Java Standard**: Java 25 (`JavaLanguageVersion.of(25)` in `build.gradle`).
- **Framework**: Spring Boot `4.1.0` (Spring Web MVC, Data JPA, Actuator, Flyway).
- **Spring Cloud**: Spring Cloud `2025.1.2` (`spring-cloud-starter-config`, `spring-cloud-starter-netflix-eureka-client`, `spring-cloud-starter-bus-amqp`).
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
   - `SPRING_DATASOURCE_URL` (default: `jdbc:postgresql://localhost:5423/bank?currentSchema=loans` / `jdbc:postgresql://bank-db:5432/bank?currentSchema=loans`)
   - `SPRING_DATASOURCE_USERNAME` (default: `postgres`)
   - `SPRING_DATASOURCE_PASSWORD` (default: `postgres`)
   - `SPRING_CONFIG_IMPORT` (default: `optional:configserver:http://localhost:8071/` or `optional:configserver:http://config-server:8071/`)
   - `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` (default: `http://localhost:8070/eureka/` or `http://eureka-server:8070/eureka/`)
   - `SPRING_RABBITMQ_HOST` (default: `localhost` or `rabbit-mq`)
   - `SPRING_RABBITMQ_PORT` (default: `5672`)

---

## 📐 REST API & Code Conventions

1. **Routing**: Base path `@RequestMapping(path = "/api/loans", produces = {MediaType.APPLICATION_JSON_VALUE})`.
2. **Endpoints**:
   - `POST /api/loans/create` - Create a new loan for a customer by `mobileNumber`
   - `GET /api/loans/fetch` - Fetch loan details by `mobileNumber`
   - `PUT /api/loans/update` - Update loan details and repayments
   - `DELETE /api/loans/delete` - Delete loan details by `mobileNumber`
3. **DTO & Validation**:
   - Request payloads must use `@Valid`.
   - Mobile numbers validated via `@Pattern(regexp = "(^$|[0-9]{10})", message = "Mobile number must be 10 digits")`.
   - Standard responses: `ResponseDto` (status/message) and `ErrorResponseDto` (error metadata).
4. **Flyway Migrations**:
   - Never modify existing migration scripts (`V1__init.sql`). Create new versioned files (`V2__description.sql`).
