# Spring Cloud Gateway Server

![Java 25](https://img.shields.io/badge/Java-25-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen.svg)
![Spring Cloud Gateway](https://img.shields.io/badge/Spring%20Cloud-Gateway%20WebFlux-green.svg)
![Resilience4j](https://img.shields.io/badge/Resilience4j-Circuit%20Breaker-red.svg)
![Eureka](https://img.shields.io/badge/Eureka-Client-blue.svg)
![Docker](https://img.shields.io/badge/Docker-Enabled-blue.svg)

The **Spring Cloud Gateway Server** serves as the single edge API gateway for all SecuredBank microservices (`accounts`, `cards`, `loans`). Built on Spring Cloud Gateway WebFlux, it provides dynamic routing, path rewriting, client-side load balancing, correlation ID request tracing, and resilience mechanisms.

---

## 🏛 Architecture & Features

- **Server Port**: `8072`
- **Engine**: Spring Cloud Gateway WebFlux (Reactive & Non-blocking)
- **Service Discovery**: Integrates with Netflix Eureka Server on port `8070` for dynamic service resolution (`lb://ACCOUNTS`, `lb://CARDS`, `lb://LOANS`)
- **Central Config**: Fetches configuration properties from Spring Cloud Config Server on port `8071`
- **Fault Tolerance**: Integrated Resilience4j Circuit Breakers & Retries with fallback endpoints

---

## 🌐 Configured Gateway Routes & Rewrite Rules

| Route ID | Matching Path Pattern | Rewrite Filter | Circuit Breaker & Fallback | Target Service URI | Sample API Gateway Request |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `accounts-upper` | `/ACCOUNTS/**` | `/ACCOUNTS/(.*)` -> `/$1` | `accountsCircuitBreaker` (`forward:/accounts-fallback`) | `lb://ACCOUNTS` | `POST http://localhost:8072/ACCOUNTS/api/accounts/create` |
| `accounts-lower` | `/accounts/**` | `/accounts/(.*)` -> `/$1` | `accountsCircuitBreaker` (`forward:/accounts-fallback`) | `lb://ACCOUNTS` | `POST http://localhost:8072/accounts/api/accounts/create` |
| `cards-upper` | `/CARDS/**` | `/CARDS/(.*)` -> `/$1` | `cardsCircuitBreaker` (`forward:/cards-fallback`) | `lb://CARDS` | `POST http://localhost:8072/CARDS/api/cards/create` |
| `cards-lower` | `/cards/**` | `/cards/(.*)` -> `/$1` | `cardsCircuitBreaker` (`forward:/cards-fallback`) | `lb://CARDS` | `POST http://localhost:8072/cards/api/cards/create` |
| `loans-upper` | `/LOANS/**` | `/LOANS/(.*)` -> `/$1` | `loansCircuitBreaker` (`forward:/loans-fallback`) | `lb://LOANS` | `POST http://localhost:8072/LOANS/api/loans/create` |
| `loans-lower` | `/loans/**` | `/loans/(.*)` -> `/$1` | `loansCircuitBreaker` (`forward:/loans-fallback`) | `lb://LOANS` | `POST http://localhost:8072/loans/api/loans/create` |

---

## 🛡️ Resilience & Fallback Endpoints

When downstream services are unreachable, experiencing high latency, or returning error thresholds, the gateway circuit breaker redirects requests to local fallback endpoints handled by `FallbackController`:

- **`/accounts-fallback`**: Returns `"Accounts service is currently unavailable. Please try again later."`
- **`/cards-fallback`**: Returns `"Cards service is currently unavailable. Please try again later."`
- **`/loans-fallback`**: Returns `"Loans service is currently unavailable. Please try again later."`

---

## 🔍 Global Filters & Correlation Tracing

1. **`RequestTraceFilter`** (`@Order(1)`):
   - Checks incoming HTTP headers for `securedbank-correlation-id`.
   - If missing, generates a unique UUID `securedbank-correlation-id` and attaches it to the request exchange.
2. **`ResponseTraceFilter`**:
   - Captures the correlation ID and adds `securedbank-correlation-id` to the outbound HTTP response headers for end-to-end distributed tracing.

---

## 🛠 Management & Actuator Monitoring Endpoints

- **Gateway Routes**: [http://localhost:8072/actuator/gateway/routes](http://localhost:8072/actuator/gateway/routes)
- **Circuit Breakers Status**: [http://localhost:8072/actuator/circuitbreakers](http://localhost:8072/actuator/circuitbreakers)
- **Circuit Breaker Events**: [http://localhost:8072/actuator/circuitbreakerevents](http://localhost:8072/actuator/circuitbreakerevents)
- **Health Indicator**: [http://localhost:8072/actuator/health](http://localhost:8072/actuator/health)
- **Info Endpoint**: [http://localhost:8072/actuator/info](http://localhost:8072/actuator/info)

---

## 🛠 Local Setup & Running

### Prerequisites
- **JDK 25**
- **Docker & Docker Compose**
- Config Server running on port `8071`
- Eureka Server running on port `8070`

### Execution Commands

```bash
# 1. Build Gateway Server
./gradlew clean build

# 2. Execute Unit Tests
./gradlew test

# 3. Launch via Docker Compose
docker compose up -d

# 4. Or run locally via Gradle
./gradlew bootRun
```
