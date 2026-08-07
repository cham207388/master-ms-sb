# Spring Cloud Config Server

![Java 25](https://img.shields.io/badge/Java-25-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen.svg)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.1.2-blue.svg)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Bus-ff6600.svg)
![Docker](https://img.shields.io/badge/Docker%20Compose-Enabled-blue.svg)

The **Spring Cloud Config Server** provides centralized, externalized configuration management for all EazyBank
microservices (`accounts`, `cards`, `loans`) backed by a Git repository and integrated with **RabbitMQ** for dynamic
event-driven configuration refreshes (`Spring Cloud Bus`).

---

## 🏛 Architecture Overview

- **Server Port**: `8071`
- **Active Profile**: `git`
- **Config Storage Repository**: [
  `https://github.com/cham207388/config-server-sb-sc-ms.git`](https://github.com/cham207388/config-server-sb-sc-ms.git)
- **Message Broker Connection**: RabbitMQ on port `5672`

---

## 🚀 Endpoints & Served Configurations

### Exposed Configuration Profiles

| Microservice | Profile   | Config Server Endpoint                                                           |
|:-------------|:----------|:---------------------------------------------------------------------------------|
| **Accounts** | `default` | [http://localhost:8071/accounts/default](http://localhost:8071/accounts/default) |
| **Cards**    | `default` | [http://localhost:8071/cards/default](http://localhost:8071/cards/default)       |
| **Loans**    | `default` | [http://localhost:8071/loans/default](http://localhost:8071/loans/default)       |
| **Eureka**   | `default` | [http://localhost:8071/eureka-server/default](http://localhost:8071/eureka-server/default) |

### Management & Bus Endpoints

- **Health Check**: [http://localhost:8071/actuator/health](http://localhost:8071/actuator/health)
- **Readiness
  Probe**: [http://localhost:8071/actuator/health/readiness](http://localhost:8071/actuator/health/readiness)
- **Bus Refresh**: `POST http://localhost:8071/actuator/busrefresh` (Triggers dynamic config reload across RabbitMQ to
  all connected microservices)

---

## 🛠 Local Setup & Execution

### Prerequisites

- JDK 25
- Docker & Docker Compose
- RabbitMQ running on port `5672`

### Commands

```bash
# 1. Build Config Server
./gradlew clean build

# 2. Run standalone Docker Compose (RabbitMQ + Config Server)
docker compose up -d

# 3. Or launch via Gradle locally
./gradlew bootRun
```