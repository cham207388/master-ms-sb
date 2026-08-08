# Spring Cloud Netflix Eureka Server

![Java 25](https://img.shields.io/badge/Java-25-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen.svg)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.1.2-blue.svg)
![Eureka](https://img.shields.io/badge/Eureka-Service%20Discovery-blue.svg)
![Docker](https://img.shields.io/badge/Docker%20Compose-Enabled-blue.svg)

The **Spring Cloud Netflix Eureka Server** provides centralized service registration and discovery for all SecuredBank microservices (`accounts`, `cards`, `loans`). It exposes a live management dashboard and REST registration endpoints for microservices inside the `securedbank` network.

---

## 🏛 Architecture Overview

- **Server Port**: `8070`
- **Dashboard UI**: [http://localhost:8070](http://localhost:8070)
- **Service Registration Endpoint**: `http://localhost:8070/eureka/`
- **Config Server Integration**: Connects to Spring Cloud Config Server on port `8071` (`CONFIG_SERVER_URL`) for centralized property resolution.

---

## 🚀 Registered Microservices & Endpoints

| Microservice | Default Port | Default Eureka Client Zone          |
| :----------- | :----------- | :---------------------------------- |
| **Accounts** | `8091`       | `http://eureka-server:8070/eureka/` |
| **Cards**    | `8092`       | `http://eureka-server:8070/eureka/` |
| **Loans**    | `8093`       | `http://eureka-server:8070/eureka/` |

---

## 🌐 Docker Networking & Dashboard Status Page Routing

When microservices run containerized inside Docker Desktop (macOS/Windows), their internal bridge network IP addresses (`172.19.x.x`) are not routable from host web browsers.

To ensure clicking service status links on the Eureka Dashboard ([http://localhost:8070](http://localhost:8070)) opens successfully in your host browser, registered client services configure explicit status and health page URLs:

```yaml
eureka:
  instance:
    prefer-ip-address: true
    status-page-url: ${EUREKA_INSTANCE_STATUS_PAGE_URL:http://localhost:${server.port}/actuator/info}
    health-check-url: ${EUREKA_INSTANCE_HEALTH_CHECK_URL:http://localhost:${server.port}/actuator/health}
```

This allows:
1. **Container Inter-Communication**: Microservices discover and call each other using internal container IPs (`172.19.x.x`) or container hostnames.
2. **Host Browser Dashboard Links**: Host developers clicking status links on the Eureka Dashboard navigate via published host ports (`localhost:8091/actuator/info`, `localhost:8092/actuator/info`, `localhost:8093/actuator/info`).

---

## 🛠 Management & Health Check Endpoints

- **Eureka Dashboard**: [http://localhost:8070](http://localhost:8070)
- **Actuator Health**: [http://localhost:8070/actuator/health](http://localhost:8070/actuator/health)
- **Readiness Probe**: [http://localhost:8070/actuator/health/readiness](http://localhost:8070/actuator/health/readiness)
- **Liveness Probe**: [http://localhost:8070/actuator/health/liveness](http://localhost:8070/actuator/health/liveness)

---

## 🛠 Local Setup & Running

### Prerequisites
- JDK 25
- Docker & Docker Compose
- Config Server running on port `8071`

### Commands

```bash
# 1. Build Eureka Server
./gradlew clean build

# 2. Run unit & integration tests
./gradlew test

# 3. Launch via Docker Compose
docker compose up -d

# 4. Or run locally via Gradle
./gradlew bootRun
```
