# Project Context & AI Agent Guidelines: EazyBank Microservices

Welcome to the **EazyBank Microservices Platform** codebase. This file provides authoritative context, architectural rules, and technical conventions for AI agents operating within this workspace.

---

## 🏛 Architecture & Domain Boundaries

The application is structured as a domain-driven microservices architecture composed of independent, decoupled Spring Boot services alongside centralized Spring Cloud infrastructure located within subdirectories:

1. **Accounts Microservice** ([`/accounts`](file:///Users/baicham/develop/java-projects/master-ms-sb/accounts))
   - **Server Port**: `8091`
   - **Database**: PostgreSQL 18 on host port `5423` (DB: `accounts`)
   - **Domain**: Customer onboarding, account lifecycle management, and profile metadata.

2. **Cards Microservice** ([`/cards`](file:///Users/baicham/develop/java-projects/master-ms-sb/cards))
   - **Server Port**: `8092`
   - **Database**: PostgreSQL 18 on host port `5424` (DB: `cards`)
   - **Domain**: Credit and debit card issuance, limit tracking, and usage metrics.

3. **Loans Microservice** ([`/loans`](file:///Users/baicham/develop/java-projects/master-ms-sb/loans))
   - **Server Port**: `8093`
   - **Database**: PostgreSQL 18 on host port `5425` (DB: `loans`)
   - **Domain**: Customer loan creation, repayment tracking, and outstanding balance management.

4. **Spring Cloud Config Server** ([`/config-server`](file:///Users/baicham/develop/java-projects/master-ms-sb/config-server))
   - **Server Port**: `8071`
   - **Domain**: Centralized configuration management backed by Git (`https://github.com/cham207388/config-server-sb-sc-ms.git`).

5. **RabbitMQ Event Bus**
   - **AMQP Port**: `5672` (Management UI: `15672`)
   - **Domain**: Provides Spring Cloud Bus AMQP for dynamic configuration refresh (`/actuator/busrefresh`).

---

## ⚙️ Tech Stack & Toolchain Standards

- **Java Standard**: Java 25 (`JavaLanguageVersion.of(25)` configured in `build.gradle`).
- **Framework**: Spring Boot `4.1.0` (Spring Web MVC, Spring Data JPA, Actuator, Flyway).
- **Spring Cloud**: Spring Cloud `2025.1.2` (`spring-cloud-starter-config`, `spring-cloud-starter-bus-amqp`).
- **Dependency Management**: Spring Dependency Management `1.1.7`.
- **Database**: PostgreSQL 18 Alpine (`postgres:18-alpine`).
- **Database Migration**: Flyway (`org.flywaydb:flyway-database-postgresql`), with scripts located at `src/main/resources/db/migration/V1__init.sql`.
- **API Documentation**: SpringDoc OpenAPI 3.0 (`springdoc-openapi-starter-webmvc-ui:3.0.2`).
- **Testing & Testcontainers**: Spring Boot Testcontainers (`org.springframework.boot:spring-boot-testcontainers`), Testcontainers PostgreSQL (`org.testcontainers:postgresql:1.20.4`), and JUnit 5 integration (`org.testcontainers:junit-jupiter:1.20.4`) with `@ServiceConnection` for isolated containerized integration testing.
- **Build System**: Independent Gradle wrapper scripts (`./gradlew`) inside each microservice directory, managed globally via the root [`Makefile`](file:///Users/baicham/develop/java-projects/master-ms-sb/Makefile).
- **Containerization & Orchestration**:
  - Multi-stage Docker builds (`eclipse-temurin:25-jdk-alpine` -> `eclipse-temurin:25-jre-alpine`) executing under non-root user `producer:producer`.
  - Consolidated root [`compose.yml`](file:///Users/baicham/develop/java-projects/master-ms-sb/compose.yml) connecting all microservices (`accounts`, `cards`, `loans`, `config-server`, `rabbit-mq`, and DBs) via a shared bridge network (`securedbank`) with 700MB memory deployment limits.

---

## 🛠 Command Conventions for AI Agents

When building, testing, or executing commands in this workspace, always adhere to these rules:

1. **Gradle Build Commands**:
   - Always run Gradle commands within the specific service directory:
     - Accounts: `cd accounts && ./gradlew clean build`
     - Cards: `cd cards && ./gradlew clean build`
     - Loans: `cd loans && ./gradlew clean build`
     - Config Server: `cd config-server && ./gradlew clean build`

2. **Makefile Commands**:
   - Use the root [`Makefile`](file:///Users/baicham/develop/java-projects/master-ms-sb/Makefile) targets for multi-service operations:
     - Build: `make accounts-build`, `make cards-build`, `make loans-build`
     - Databases: `make accounts-db-up`, `make cards-db-up`, `make loans-db-up`, `make dbs-down`
     - Service Stacks: `make accounts`, `make cards`, `make loans`
     - Stack Teardown: `make accounts-down`, `make cards-down`, `make loans-down`
     - Config Server & RabbitMQ: `make rabbit-mq-up`, `make config-server-up`, `make config-all-up`, `make config-all-down`

3. **Orchestration with Root Compose**:
   - To bring up the entire platform (all DBs, Config Server, RabbitMQ, and APIs) on the `securedbank` network:
     - `docker compose up -d`
     - `docker compose down -v`

4. **Environment Configuration**:
   - Settings in `application.yaml` use the environment variables:
     - Database: `DB_HOST` (`localhost` or container name), `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`
     - Config Server: `CONFIG_SERVER_URL` (default: `http://localhost:8071/` or `http://config-server:8071/`)
     - Event Bus / RabbitMQ: `RABBITMQ_HOST` (default: `localhost` or `rabbit-mq`), `RABBITMQ_PORT` (`5672`), `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD`

---

## 📐 REST API & Code Conventions

When adding or updating endpoints, models, or database schemas:

1. **API Endpoints & Routing**:
   - Base mapping must be `@RequestMapping(path = "/api", produces = {MediaType.APPLICATION_JSON_VALUE})`.
   - Keep CRUD paths uniform across services: `/create`, `/fetch`, `/update`, `/delete`.

2. **Validation & DTOs**:
   - Request bodies must use `@Valid` bean validation.
   - Mobile numbers must be validated using `@Pattern(regexp = "(^$|[0-9]{10})", message = "Mobile number must be 10 digits")`.
   - Standard response DTOs: `ResponseDto` (status & message) and `ErrorResponseDto` (error metadata).

3. **OpenAPI / Swagger Annotations**:
   - Annotate controllers with `@Tag(name = "...", description = "...")`.
   - Annotate methods with `@Operation(summary = "...", description = "...")` and `@ApiResponses`.
   - UI is available at `/swagger-ui/index.html` for each microservice:
     - Accounts: http://localhost:8091/swagger-ui/index.html
     - Cards: http://localhost:8092/swagger-ui/index.html
     - Loans: http://localhost:8093/swagger-ui/index.html

4. **Flyway Migrations**:
   - Never modify existing, already-applied Flyway SQL scripts.
   - Add versioned migration files following the naming pattern: `V<Version>__<Description>.sql` (e.g., `V2__add_column.sql`).
