# Project Context & AI Agent Guidelines: Securedbank Microservices

Welcome to the **Securedbank Microservices Platform** codebase. This file provides authoritative context, architectural rules, and technical conventions for AI agents operating within this workspace.

---

## 🏛 Architecture & Domain Boundaries

The application is structured as a domain-driven microservices architecture composed of independent, decoupled Spring Boot services alongside centralized Spring Cloud infrastructure and telemetry systems located within subdirectories:

1. **Accounts Microservice** ([`/accounts`](file:///Users/baicham/develop/java-projects/master-ms-sb/accounts))
   - **Server Port**: `8091`
   - **Database**: Shared PostgreSQL 18 on host port `5423` (DB: `bank`, Schema: `accounts`)
   - **Domain**: Customer onboarding, account lifecycle management, and profile metadata.

2. **Cards Microservice** ([`/cards`](file:///Users/baicham/develop/java-projects/master-ms-sb/cards))
   - **Server Port**: `8092`
   - **Database**: Shared PostgreSQL 18 on host port `5423` (DB: `bank`, Schema: `cards`)
   - **Domain**: Credit and debit card issuance, limit tracking, and usage metrics.

3. **Loans Microservice** ([`/loans`](file:///Users/baicham/develop/java-projects/master-ms-sb/loans))
   - **Server Port**: `8093`
   - **Database**: Shared PostgreSQL 18 on host port `5423` (DB: `bank`, Schema: `loans`)
   - **Domain**: Customer loan creation, repayment tracking, and outstanding balance management.

4. **Spring Cloud Config Server** ([`/config-server`](file:///Users/baicham/develop/java-projects/master-ms-sb/config-server))
   - **Server Port**: `8071`
   - **Domain**: Centralized configuration management backed by Git (`https://github.com/cham207388/config-server-sb-sc-ms.git`).

5. **Spring Cloud Netflix Eureka Server** ([`/eureka-server`](file:///Users/baicham/develop/java-projects/master-ms-sb/eureka-server))
   - **Server Port**: `8070`
   - **Domain**: Service registration & discovery server (Dashboard: `http://localhost:8070`, endpoint: `http://eureka-server:8070/eureka/`).

6. **Spring Cloud Gateway Server** ([`/gateway-server`](file:///Users/baicham/develop/java-projects/master-ms-sb/gateway-server))
   - **Server Port**: `8072`
   - **Domain**: Edge API routing, reactive load balancing (`lb://ACCOUNTS`, `lb://CARDS`, `lb://LOANS`), dynamic path rewriting (`/ACCOUNTS/**` -> `/api/accounts/**`), and Gateway Actuator metrics (`/actuator/gateway/routes`).

7. **RabbitMQ Event Bus**
   - **AMQP Port**: `5672` (Management UI: `15672`)
   - **Domain**: Provides Spring Cloud Bus AMQP for dynamic configuration refresh (`/actuator/busrefresh`).

8. **Observability Telemetry Stack** ([`docker-compose-observability.yml`](file:///Users/baicham/develop/java-projects/master-ms-sb/docker-compose-observability.yml), [`/observability`](file:///Users/baicham/develop/java-projects/master-ms-sb/observability))
   - **Grafana UI Port**: `3000`
   - **Loki Gateway Port**: `3100` (Read target: `3101`, Write target: `3102`)
   - **MinIO S3 Store Ports**: `9000` (API) / `9001` (Console)
   - **Grafana Alloy Port**: `12345`
   - **Domain**: Centralized log harvesting and visualization. Grafana Alloy collects stdout/stderr logs from all Docker containers via `/var/run/docker.sock` and pushes them to Loki gateway (`tenant1`), backed by MinIO object storage.

---

## ⚙️ Tech Stack & Toolchain Standards

- **Java Standard**: Java 25 (`JavaLanguageVersion.of(25)` configured in `build.gradle`).
- **Framework**: Spring Boot `4.1.0` (Spring Web MVC, Spring Data JPA, Actuator, Flyway).
- **Spring Cloud**: Spring Cloud `2025.1.2` (`spring-cloud-starter-config`, `spring-cloud-starter-gateway-server-webflux`, `spring-cloud-starter-loadbalancer`, `spring-cloud-starter-netflix-eureka-server`, `spring-cloud-starter-netflix-eureka-client`, `spring-cloud-starter-bus-amqp`).
- **Observability & Telemetry**: Grafana `11.5.2`, Grafana Loki `3.4.2` (Microservices target architecture: Read, Write, Backend), Grafana Alloy `v1.7.1` log collector, MinIO `RELEASE.2024-12-18T13-15-44Z` S3 storage, Nginx `1.27.4-alpine` Loki edge proxy gateway.
- **Dependency Management**: Spring Dependency Management `1.1.7`.
- **Database**: PostgreSQL 18 Alpine (`postgres:18-alpine`).
- **Database Migration**: Flyway (`org.flywaydb:flyway-database-postgresql`), with scripts located at `src/main/resources/db/migration/V1__init.sql`.
- **API Documentation**: SpringDoc OpenAPI 3.0 (`springdoc-openapi-starter-webmvc-ui:3.0.2`).
- **Testing & Testcontainers**: Spring Boot Testcontainers (`org.springframework.boot:spring-boot-testcontainers`), Testcontainers PostgreSQL (`org.testcontainers:postgresql:1.20.4`), and JUnit 5 integration (`org.testcontainers:junit-jupiter:1.20.4`) with `@ServiceConnection` for isolated containerized integration testing.
- **Build System**: Independent Gradle wrapper scripts (`./gradlew`) inside each microservice directory, managed globally via the root [`Makefile`](file:///Users/baicham/develop/java-projects/master-ms-sb/Makefile).
- **Containerization & Orchestration**:
  - Multi-stage Docker builds (`eclipse-temurin:25-jdk-alpine` -> `eclipse-temurin:25-jre-alpine`) executing under non-root users (`producer:producer`, `gateway:gateway`).
  - Consolidated root [`compose.yml`](file:///Users/baicham/develop/java-projects/master-ms-sb/compose.yml) with `include:` directives pulling in infrastructure, observability (`docker-compose-observability.yml`), config-server, eureka-server, and microservices via shared bridge networks (`securedbank`, `loki`).

---

## 🛠 Command Conventions for AI Agents

When building, testing, or executing commands in this workspace, always adhere to these rules:

1. **Gradle Build Commands**:
   - Always run Gradle commands within the specific service directory:
     - Accounts: `cd accounts && ./gradlew clean build`
     - Cards: `cd cards && ./gradlew clean build`
     - Loans: `cd loans && ./gradlew clean build`
     - Config Server: `cd config-server && ./gradlew clean build`
     - Eureka Server: `cd eureka-server && ./gradlew clean build`
     - Gateway Server: `cd gateway-server && ./gradlew clean build`

2. **Makefile Commands**:
   - Use the root [`Makefile`](file:///Users/baicham/develop/java-projects/master-ms-sb/Makefile) targets for multi-service operations:
     - Build: `make accounts-build`, `make cards-build`, `make loans-build`, `make eureka-server-build`, `make gateway-server-build`
     - Databases: `make bank-db-up`, `make accounts-db-up`, `make cards-db-up`, `make loans-db-up`, `make dbs-down`
     - Service Stacks: `make accounts`, `make cards`, `make loans`, `make gateway-up`, `make gateway-down`, `make all-up`, `make all-down`
     - Stack Teardown: `make accounts-down`, `make cards-down`, `make loans-down`
     - Config Server & RabbitMQ: `make rabbit-mq-up`, `make config-server-up`, `make config-all-up`, `make config-all-down`
     - Eureka Server: `make eureka-server-up`, `make eureka-server-down`

3. **Orchestration with Root Compose**:
   - To bring up the entire platform (shared bank DB, Config Server, Eureka Server, Gateway Server, RabbitMQ, APIs, Loki, Alloy, MinIO, Grafana) on the network stack:
     - `make all-up` or `docker compose up -d`
     - Standalone Observability: `docker compose -f docker-compose-observability.yml up -d`
     - Teardown: `docker compose down -v`

4. **Environment Configuration**:
   - Settings in `application.yaml` use standard Spring Boot relaxed binding environment variables:
     - Database: `SPRING_DATASOURCE_URL` (e.g. `jdbc:postgresql://bank-db:5432/bank?currentSchema=accounts`), `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
     - Config Server: `SPRING_CONFIG_IMPORT` (e.g. `optional:configserver:http://config-server:8071/`)
     - Eureka Server: `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` (e.g. `http://eureka-server:8070/eureka/`)
     - Event Bus / RabbitMQ: `SPRING_RABBITMQ_HOST` (default: `localhost` or `rabbit-mq`), `SPRING_RABBITMQ_PORT` (`5672`), `SPRING_RABBITMQ_USERNAME`, `SPRING_RABBITMQ_PASSWORD`
     - Redis Rate Limiter: `SPRING_DATA_REDIS_HOST` (default: `localhost` or `redis`), `SPRING_DATA_REDIS_PORT` (`6379`)

5. **Docker Container Networking & Eureka Dashboard Status Links**:
   - **Bridge IP Isolation**: Inside Docker Desktop (macOS/Windows), container IP addresses (e.g., `172.19.x.x`) run in an isolated Linux VM and are not directly routable from host web browsers.
   - **Status & Health Page URLs**: Microservices explicitly define `eureka.instance.status-page-url: http://localhost:${server.port}/actuator/info` and `eureka.instance.health-check-url: http://localhost:${server.port}/actuator/health` so clicking status links on the Eureka Dashboard (`http://localhost:8070`) routes through published host ports (`8091`, `8092`, `8093`, `8072`) while inter-service communication remains containerized.

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

---

## 🛡️ Resilience & Monitoring (Circuit Breaker & Telemetry)

When configuring circuit breakers, retries, or monitoring endpoints:

1. **Spring Cloud Gateway Resilience**:
   - Gateway routes employ Resilience4j CircuitBreaker filters (`accountsCircuitBreaker`, `cardsCircuitBreaker`, `loansCircuitBreaker`).
   - Fallback endpoints are handled in `FallbackController.java`:
     - `/accounts-fallback`: Handles Accounts service circuit breaker fallbacks.
     - `/cards-fallback`: Handles Cards service circuit breaker fallbacks.
     - `/loans-fallback`: Handles Loans service circuit breaker fallbacks.
   - Gateway Actuator Monitoring Paths:
     - Routes: `http://localhost:8072/actuator/gateway/routes`
     - Circuit Breakers: `http://localhost:8072/actuator/circuitbreakers`
     - Circuit Breaker Events: `http://localhost:8072/actuator/circuitbreakerevents`
     - Health Status: `http://localhost:8072/actuator/health`

2. **Accounts Microservice Resilience**:
   - OpenFeign circuit breaker is enabled via `spring.cloud.openfeign.circuitbreaker.enabled: true`.
   - `CardsFeignClient` uses `fallback = CardsFallback.class`.
   - `LoansFeignClient` uses `fallback = LoansFallback.class`.
   - Accounts Actuator Monitoring Paths:
     - Health & Circuit Breaker Status: `http://localhost:8091/actuator/health`
     - Circuit Breakers: `http://localhost:8091/actuator/circuitbreakers`
     - Circuit Breaker Events: `http://localhost:8091/actuator/circuitbreakerevents`

3. **Observability & Log Telemetry (Grafana, Loki & Alloy)**:
   - Grafana Explorer UI: `http://localhost:3000` (Pre-configured Loki Datasource `tenant1`).
   - Loki Edge Nginx Gateway: `http://localhost:3100` (Push endpoint: `/loki/api/v1/push`).
   - MinIO Storage Console: `http://localhost:9001` (Credentials: `loki` / `supersecret`).
   - Grafana Alloy Monitoring: `http://localhost:12345`.
