# SecuredBank Microservices Architecture

![Java 25](https://img.shields.io/badge/Java-25-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen.svg)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.1.2-blue.svg)
![Spring Cloud Gateway](https://img.shields.io/badge/Spring%20Cloud-Gateway%20WebFlux-green.svg)
![Eureka](https://img.shields.io/badge/Eureka-Service%20Discovery-blue.svg)
![Grafana](https://img.shields.io/badge/Grafana-11.5.2-orange.svg)
![Loki](https://img.shields.io/badge/Loki-3.4.2-blue.svg)
![Alloy](https://img.shields.io/badge/Grafana%20Alloy-1.7.1-red.svg)
![MinIO](https://img.shields.io/badge/MinIO-S3%20Store-pink.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18--alpine-blue.svg)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Bus-ff6600.svg)
![Flyway](https://img.shields.io/badge/Flyway-Migration-red.svg)
![Testcontainers](https://img.shields.io/badge/Testcontainers-1.20.4-black.svg)
![Docker](https://img.shields.io/badge/Docker%20Compose-Enabled-blue.svg)
![OpenAPI](https://img.shields.io/badge/OpenAPI-3.0.2-green.svg)

An enterprise-grade, domain-driven banking microservices platform built with **Spring Boot 4.1.0**, **Spring Cloud 2025.1.2**, and **Java 25**. The platform decouples core banking domains into standalone microservices (**Accounts**, **Cards**, **Loans**) accessible via an edge API Gateway ([**Gateway Server**](file:///Users/baicham/develop/java-projects/master-ms-sb/gateway-server)), backed by centralized configuration management ([**Config Server**](file:///Users/baicham/develop/java-projects/master-ms-sb/config-server)), service registration & discovery ([**Eureka Server**](file:///Users/baicham/develop/java-projects/master-ms-sb/eureka-server)), dynamic event-driven refresh (**RabbitMQ** & **Spring Cloud Bus**), and a full observability stack (**Grafana**, **Loki**, **Grafana Alloy**, and **MinIO**).

---

## 🏛 Architecture Overview

```mermaid
graph TD
    Client[HTTP Client / API Consumer]

    subgraph Infrastructure [Shared Network: securedbank]
        ConfigServer[Spring Cloud Config Server<br/>Port: 8071]
        EurekaServer[Spring Cloud Eureka Server<br/>Port: 8070]
        RabbitMQ[RabbitMQ Event Bus<br/>Port: 5672]
        GatewayServer[Spring Cloud Gateway Server<br/>Port: 8072]
    end

    subgraph Accounts Microservice [Port 8091]
        AccountsApp[Accounts API]
        AccountsDB[(PostgreSQL 18<br/>DB: accounts<br/>Port: 5423)]
        AccountsApp --> AccountsDB
    end

    subgraph Cards Microservice [Port 8092]
        CardsApp[Cards API]
        CardsDB[(PostgreSQL 18<br/>DB: cards<br/>Port: 5424)]
        CardsApp --> CardsDB
    end

    subgraph Loans Microservice [Port 8093]
        LoansApp[Loans API]
        LoansDB[(PostgreSQL 18<br/>DB: loans<br/>Port: 5425)]
        LoansApp --> LoansDB
    end

    subgraph Telemetry & Observability [Network: loki / securedbank]
        Alloy[Grafana Alloy Log Collector<br/>Port: 12345]
        DockerSock[("/var/run/docker.sock")]
        LokiGateway[Loki Nginx Gateway<br/>Port: 3100]
        LokiWrite[Loki Write Target<br/>Port: 3102]
        LokiRead[Loki Read Target<br/>Port: 3101]
        LokiBackend[Loki Backend Target]
        MinIO[(MinIO S3 Storage<br/>Buckets: loki-data, loki-ruler<br/>Ports: 9000 / 9001)]
        GrafanaUI[Grafana Dashboards & Explore<br/>Port: 3000]
    end

    AccountsApp -.->|Fetch Config| ConfigServer
    CardsApp -.->|Fetch Config| ConfigServer
    LoansApp -.->|Fetch Config| ConfigServer
    GatewayServer -.->|Fetch Config| ConfigServer

    AccountsApp -.->|Register & Discover| EurekaServer
    CardsApp -.->|Register & Discover| EurekaServer
    LoansApp -.->|Register & Discover| EurekaServer
    GatewayServer -.->|Register & Discover| EurekaServer

    GatewayServer ==>|lb://ACCOUNTS| AccountsApp
    GatewayServer ==>|lb://CARDS| CardsApp
    GatewayServer ==>|lb://LOANS| LoansApp

    AccountsApp <==>|Spring Cloud Bus| RabbitMQ
    CardsApp <==>|Spring Cloud Bus| RabbitMQ
    LoansApp <==>|Spring Cloud Bus| RabbitMQ
    ConfigServer <==>|Spring Cloud Bus| RabbitMQ

    Client -->|REST / HTTP| GatewayServer

    %% Observability Connections
    DockerSock ==>|Harvest Stdout/Stderr| Alloy
    Alloy ==>|Push Logs / tenant1| LokiGateway
    LokiGateway -->|Write Stream| LokiWrite
    LokiGateway -->|Read Queries| LokiRead
    LokiWrite -->|Store Chunks| MinIO
    LokiRead -->|Query Chunks| MinIO
    LokiBackend -->|Compact & Retention| MinIO
    GrafanaUI ==>|Query Loki Datasource| LokiGateway
```

---

<details open>
<summary><strong>🚀 Tech Stack</strong></summary>

| Component              | Technology                                         | Description                                                                                                                                     |
| :--------------------- | :------------------------------------------------- | :---------------------------------------------------------------------------------------------------------------------------------------------- |
| **Language**           | Java 25                                            | Latest Java Toolchain standard (`JavaLanguageVersion.of(25)`)                                                                                   |
| **Framework**          | Spring Boot 4.1.0                                  | Core microservice framework (Spring Web MVC, Data JPA, Actuator)                                                                                |
| **API Gateway**        | Spring Cloud Gateway WebFlux 2025.1.2              | Reactive edge routing & path rewriting ([`/gateway-server`](file:///Users/baicham/develop/java-projects/master-ms-sb/gateway-server))           |
| **Load Balancer**      | Spring Cloud LoadBalancer                          | Reactive client-side load balancing (`lb://ACCOUNTS`, `lb://CARDS`, `lb://LOANS`)                                                               |
| **Central Config**     | Spring Cloud Config 2025.1.2                       | Centralized configuration management ([`/config-server`](file:///Users/baicham/develop/java-projects/master-ms-sb/config-server))               |
| **Service Discovery**  | Spring Cloud Netflix Eureka                        | Service registration server & management dashboard ([`/eureka-server`](file:///Users/baicham/develop/java-projects/master-ms-sb/eureka-server)) |
| **Event Bus**          | Spring Cloud Bus AMQP                              | Event-driven dynamic configuration refresh via RabbitMQ (`/actuator/busrefresh`)                                                                |
| **Observability UI**   | Grafana 11.5.2                                     | Telemetry dashboard & log analytics UI with pre-configured Loki datasource (`tenant1`)                                                          |
| **Log Storage Engine** | Grafana Loki 3.4.2                                 | Microservices decoupled log engine (`read`, `write`, `backend` targets)                                                                         |
| **Log Collector**     | Grafana Alloy v1.7.1                               | Next-gen OpenTelemetry & Prometheus telemetry collector harvesting Docker logs via `/var/run/docker.sock`                                      |
| **Object Storage**     | MinIO (RELEASE.2024-12-18)                         | High-performance S3-compatible object storage backing Loki index & chunk storage                                                               |
| **Loki Edge Proxy**    | Nginx 1.27.4-alpine                                | Reverse proxy routing push requests to `write` target and query requests to `read` target                                                        |
| **Build Tool**         | Gradle                                             | Independent wrapper scripts (`./gradlew`) for each service                                                                                      |
| **Database**           | PostgreSQL 18 Alpine                               | Containerized relational database per microservice (`postgres:18-alpine`)                                                                       |
| **Database Migration** | Flyway (`org.flywaydb:flyway-database-postgresql`) | Versioned SQL database migrations (`db/migration/V1__init.sql`)                                                                                 |
| **API Documentation**  | SpringDoc OpenAPI 3.0 (`3.0.2`)                    | Automated Swagger UI (`/swagger-ui/index.html`) & OpenAPI specs                                                                                 |
| **Testing**            | Spring Boot Testcontainers                         | Ephemeral PostgreSQL containers (`@ServiceConnection`) for integration tests                                                                    |
| **Containerization**   | Docker & Docker Compose                            | Multi-stage container builds & unified modular orchestration (`compose.yml` with `include:`)                                                   |

</details>

---

<details open>
<summary><strong>⚙️ Infrastructure, Ports & API Allocation</strong></summary>

### Service Port & Infrastructure Allocation

| Microservice / Component | Path | Server Port | Database Name | Host DB Port | Dashboard / UI Endpoint | Actuator Health | Circuit Breaker / Status Endpoint |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Gateway Server** | [`/gateway-server`](file:///Users/baicham/develop/java-projects/master-ms-sb/gateway-server) | `8072` | N/A | N/A | [http://localhost:8072/actuator/gateway/routes](http://localhost:8072/actuator/gateway/routes) | [http://localhost:8072/actuator/health](http://localhost:8072/actuator/health) | [http://localhost:8072/actuator/circuitbreakers](http://localhost:8072/actuator/circuitbreakers) |
| **Accounts** | [`/accounts`](file:///Users/baicham/develop/java-projects/master-ms-sb/accounts) | `8091` | `accounts` | `5423` | [http://localhost:8091/swagger-ui/index.html](http://localhost:8091/swagger-ui/index.html) | [http://localhost:8091/actuator/health](http://localhost:8091/actuator/health) | [http://localhost:8091/actuator/circuitbreakers](http://localhost:8091/actuator/circuitbreakers) |
| **Cards** | [`/cards`](file:///Users/baicham/develop/java-projects/master-ms-sb/cards) | `8092` | `cards` | `5424` | [http://localhost:8092/swagger-ui/index.html](http://localhost:8092/swagger-ui/index.html) | [http://localhost:8092/actuator/health](http://localhost:8092/actuator/health) | N/A |
| **Loans** | [`/loans`](file:///Users/baicham/develop/java-projects/master-ms-sb/loans) | `8093` | `loans` | `5425` | [http://localhost:8093/swagger-ui/index.html](http://localhost:8093/swagger-ui/index.html) | [http://localhost:8093/actuator/health](http://localhost:8093/actuator/health) | N/A |
| **Config Server** | [`/config-server`](file:///Users/baicham/develop/java-projects/master-ms-sb/config-server) | `8071` | N/A | N/A | N/A | [http://localhost:8071/actuator/health](http://localhost:8071/actuator/health) | N/A |
| **Eureka Server** | [`/eureka-server`](file:///Users/baicham/develop/java-projects/master-ms-sb/eureka-server) | `8070` | N/A | N/A | [http://localhost:8070](http://localhost:8070) | [http://localhost:8070/actuator/health](http://localhost:8070/actuator/health) | N/A |
| **RabbitMQ** | N/A | `5672` (Mgmt: `15672`) | N/A | N/A | [http://localhost:15672](http://localhost:15672) | N/A | N/A |
| **Grafana UI** | [`/observability/grafana`](file:///Users/baicham/develop/java-projects/master-ms-sb/observability/grafana) | `3000` | N/A | N/A | [http://localhost:3000](http://localhost:3000) | [http://localhost:3000/api/health](http://localhost:3000/api/health) | N/A |
| **Loki Gateway** | [`/observability/loki`](file:///Users/baicham/develop/java-projects/master-ms-sb/observability/loki) | `3100` | N/A | N/A | [http://localhost:3100](http://localhost:3100) | N/A | N/A |
| **Loki Read Target** | N/A | `3101` | N/A | N/A | N/A | [http://localhost:3101/ready](http://localhost:3101/ready) | N/A |
| **Loki Write Target** | N/A | `3102` | N/A | N/A | N/A | [http://localhost:3102/ready](http://localhost:3102/ready) | N/A |
| **MinIO Console** | N/A | `9001` (API: `9000`) | N/A | N/A | [http://localhost:9001](http://localhost:9001) | [http://localhost:9000/minio/health/live](http://localhost:9000/minio/health/live) | N/A |
| **Grafana Alloy** | [`/observability/alloy`](file:///Users/baicham/develop/java-projects/master-ms-sb/observability/alloy) | `12345` | N/A | N/A | [http://localhost:12345](http://localhost:12345) | N/A | N/A |

</details>

---

## 🔭 Observability & Log Telemetry Architecture

The platform features an automated, zero-code-change log telemetry pipeline powered by **Grafana Alloy**, **Grafana Loki**, and **Grafana**:

```
[ Docker Socket /var/run/docker.sock ] 
            │
            ▼ (Harvest stdout/stderr logs)
[ Grafana Alloy Collector (Port 12345) ]
            │
            ▼ (HTTP Push / tenant1)
[ Loki Nginx Gateway (Port 3100) ]
       ├───► Write Target (Port 3102) ───► MinIO S3 (Buckets: loki-data, loki-ruler)
       └───► Read Target (Port 3101)  ◄─── MinIO S3
            ▲
            │ (LogQL Query)
[ Grafana UI (Port 3000) ]
```

### 1. How Log Collection Works (Grafana Alloy)
- **Zero Instrument overhead**: Microservices don't need dedicated log appenders. Alloy mounts `/var/run/docker.sock` to discover every running container.
- **Relabeling Rules**: Alloy extracts the raw Docker container name (e.g., `accounts-api`, `cards-api`, `gateway-server`) and attaches it as a searchable stream label `container`.
- **Multi-Tenant Push**: Alloy pushes collected log streams to `http://gateway:3100/loki/api/v1/push` tagged with header `X-Scope-OrgID: tenant1`.

### 2. Loki Decoupled Microservices Architecture
- **Read Target (`grafana/loki:3.4.2`)**: Dedicated query processing engine listening on port `3101`.
- **Write Target (`grafana/loki:3.4.2`)**: High-throughput log ingestion engine listening on port `3102`.
- **Backend Target (`grafana/loki:3.4.2`)**: Compactor, retention enforcer, and ruler manager.
- **MinIO Object Store**: S3-compatible backend hosting index files (`index_*`) and compressed log chunks (`loki-data`).

### 3. LogQL Learning & Query Guide
When accessing Grafana at **http://localhost:3000**, navigate to **Explore** and select the **Loki** datasource.

#### Essential LogQL Examples:
- **Stream all logs for a specific microservice**:
  ```logql
  {container="accounts-api"}
  ```
- **Filter logs containing explicit errors**:
  ```logql
  {container="accounts-api"} |= "ERROR"
  ```
- **Filter and parse JSON log payloads**:
  ```logql
  {container="gateway-server"} | json | level="error"
  ```
- **Exclude noise (e.g., actuator health checks)**:
  ```logql
  {container="accounts-api"} != "/actuator/health"
  ```
- **Calculate log entry rate per minute across services**:
  ```logql
  sum by (container) (rate({container=~".+"}[1m]))
  ```

---

## 🛠 Local Development & Execution Guide

### Prerequisites
- **JDK 25** installed & configured in environment path.
- **Docker & Docker Compose** installed and running.
- **Gradle** (or use bundled `./gradlew` wrapper in each directory).

---

### 1. Provisioning Platform via Docker Compose

To launch the full architecture (Config Server, Eureka Server, Gateway Server, RabbitMQ, Databases, Microservice APIs, Loki, MinIO, Alloy, Grafana) on the network stack:

```bash
# Launch entire platform via Makefile (Includes Observability)
make all-up

# Or via Docker Compose directly
docker compose up -d

# Provision standalone Observability stack only
docker compose -f docker-compose-observability.yml up -d

# Stop platform and clean volumes
make all-down
```

---

### 2. Building the Services

Compile and package services using the [`Makefile`](file:///Users/baicham/develop/java-projects/master-ms-sb/Makefile) or Gradle directly:

```bash
# Clean & build microservices via Makefile
make accounts-build
make cards-build
make loans-build
make eureka-server-build
make gateway-server-build

# Or build directly using the Gradle wrapper
cd accounts && ./gradlew clean build
cd ../cards && ./gradlew clean build
cd ../loans && ./gradlew clean build
cd ../config-server && ./gradlew clean build
cd ../eureka-server && ./gradlew clean build
cd ../gateway-server && ./gradlew clean build
```

---

### 3. Running Applications Locally (IDE / CLI)

Navigate into the respective service folder and launch via the Gradle wrapper:

```bash
# Launch Config Server (Port 8071)
cd config-server && ./gradlew bootRun

# Launch Eureka Server (Port 8070)
cd eureka-server && ./gradlew bootRun

# Launch Gateway Server (Port 8072)
cd gateway-server && ./gradlew bootRun

# Launch Accounts Service (Port 8091)
cd accounts && ./gradlew bootRun

# Launch Cards Service (Port 8092)
cd cards && ./gradlew bootRun

# Launch Loans Service (Port 8093)
cd loans && ./gradlew bootRun
```

---

## 📊 Environment Configuration & Overrides

Each microservice relies on configurable environment variables in its `application.yaml` file:

| Environment Variable  | Description             | Accounts Default                | Cards Default                   | Loans Default                   | Config Server Default | Eureka Server Default           | Gateway Server Default          |
| :-------------------- | :---------------------- | :------------------------------ | :------------------------------ | :------------------------------ | :-------------------- | :------------------------------ | :------------------------------ |
| `DB_HOST`             | Database Hostname       | `localhost`                     | `localhost`                     | `localhost`                     | N/A                   | N/A                             | N/A                             |
| `DB_PORT`             | PostgreSQL Host Port    | `5423`                          | `5424`                          | `5425`                          | N/A                   | N/A                             | N/A                             |
| `DB_NAME`             | PostgreSQL DB Name      | `accounts`                      | `cards`                         | `loans`                         | N/A                   | N/A                             | N/A                             |
| `DB_USERNAME`         | Database User           | `postgres`                      | `postgres`                      | `postgres`                      | N/A                   | N/A                             | N/A                             |
| `DB_PASSWORD`         | Database Password       | `postgres`                      | `postgres`                      | `postgres`                      | N/A                   | N/A                             | N/A                             |
| `CONFIG_SERVER_URL`   | Config Server Endpoint  | `http://localhost:8071/`        | `http://localhost:8071/`        | `http://localhost:8071/`        | N/A                   | `http://localhost:8071/`        | `http://localhost:8071/`        |
| `EUREKA_DEFAULT_ZONE` | Eureka Default Zone URL | `http://localhost:8070/eureka/` | `http://localhost:8070/eureka/` | `http://localhost:8070/eureka/` | N/A                   | `http://localhost:8070/eureka/` | `http://localhost:8070/eureka/` |
| `RABBITMQ_HOST`       | RabbitMQ Broker Host    | `localhost`                     | `localhost`                     | `localhost`                     | `localhost`           | N/A                             | N/A                             |
| `RABBITMQ_PORT`       | RabbitMQ AMQP Port      | `5672`                          | `5672`                          | `5672`                          | `5672`                | N/A                             | N/A                             |

---

## 📄 Build & Management Commands ([`Makefile`](file:///Users/baicham/develop/java-projects/master-ms-sb/Makefile))

| Makefile Target        | Command Executed                                                                    | Purpose                                   |
| :--------------------- | :---------------------------------------------------------------------------------- | :---------------------------------------- |
| `all-up`               | `docker compose up -d`                                                              | Launches entire platform stack            |
| `all-down`             | `docker compose down -v`                                                            | Stops platform stack & cleans volumes     |
| `gateway-server-build` | `cd gateway-server && ./gradlew clean build`                                        | Cleans & compiles Gateway Server          |
| `gateway-up`           | `docker compose up gateway-server -d --build --no-deps`                             | Starts Gateway Server container           |
| `gateway-down`         | `docker compose stop gateway-server`                                                | Stops Gateway Server container            |
| `accounts-build`       | `cd accounts && ./gradlew clean build`                                              | Cleans & compiles Accounts service        |
| `accounts-db-up`       | `cd accounts && docker compose up accounts-db -d`                                   | Starts Accounts PostgreSQL database       |
| `accounts-db-down`     | `docker compose -f accounts/compose.yml down accounts-db -v`                        | Stops Accounts DB & removes volumes       |
| `accounts-api-run`     | `docker compose -f accounts/compose.yml up accounts-api -d --build`                 | Rebuilds & starts Accounts API container  |
| `accounts`             | `docker compose -f accounts/compose.yml up -d`                                      | Starts Accounts DB & API stack            |
| `accounts-down`        | `docker compose -f accounts/compose.yml down -d`                                    | Stops Accounts DB & API stack             |
| `cards-build`          | `cd cards && ./gradlew clean build`                                                 | Cleans & compiles Cards service           |
| `cards-db-up`          | `cd cards && docker compose up cards-db -d`                                         | Starts Cards PostgreSQL database          |
| `cards-db-down`        | `docker compose -f cards/compose.yml down cards-db -v`                              | Stops Cards DB & removes volumes          |
| `cards-api-run`        | `docker compose -f cards/compose.yml up cards-api -d`                               | Starts Cards API container                |
| `cards`                | `docker compose -f cards/compose.yml up -d`                                         | Starts Cards DB & API stack               |
| `cards-down`           | `docker compose -f cards/compose.yml down -d`                                       | Stops Cards DB & API stack                |
| `loans-build`          | `cd loans && ./gradlew clean build`                                                 | Cleans & compiles Loans service           |
| `loans-db-up`          | `cd loans && docker compose up loans-db -d`                                         | Starts Loans PostgreSQL database          |
| `loans-db-down`        | `docker compose -f loans/compose.yml down loans-db -v`                              | Stops Loans DB & removes volumes          |
| `loans-api`            | `docker compose -f loans/compose.yml up loans-api -d`                               | Starts Loans API container                |
| `loans`                | `docker compose -f loans/compose.yml up -d`                                         | Starts Loans DB & API stack               |
| `loans-down`           | `docker compose -f loans/compose.yml down -d`                                       | Stops Loans DB & API stack                |
| `rabbit-mq-up`         | `docker compose -f ../master-ms-sb-config-server/compose.yml up rabbit-mq -d`       | Starts standalone RabbitMQ container      |
| `rabbit-mq-down`       | `docker compose -f ../master-ms-sb-config-server/compose.yml down rabbit-mq -v`     | Stops RabbitMQ container & cleans volumes |
| `config-server-up`     | `docker compose -f ../master-ms-sb-config-server/compose.yml up config-server -d`   | Starts Config Server container            |
| `config-server-down`   | `docker compose -f ../master-ms-sb-config-server/compose.yml down config-server -v` | Stops Config Server container             |
| `config-all-up`        | `docker compose -f ../master-ms-sb-config-server/compose.yml up -d`                 | Starts Config Server & RabbitMQ stack     |
| `config-all-down`      | `docker compose -f ../master-ms-sb-config-server/compose.yml down -v`               | Stops Config Server & RabbitMQ stack      |
| `dbs-down`             | `accounts-db-down cards-db-down loans-db-down`                                      | Stops all databases and cleans volumes    |
