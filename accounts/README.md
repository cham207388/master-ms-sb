# Accounts Microservice

![Java 25](https://img.shields.io/badge/Java-25-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen.svg)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.1.2-blue.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18--alpine-blue.svg)
![Flyway](https://img.shields.io/badge/Flyway-Migration-red.svg)
![OpenAPI](https://img.shields.io/badge/OpenAPI-3.0.2-green.svg)

The **Accounts Microservice** handles customer onboarding, profile metadata, and core bank account lifecycle operations for the SecuredBank platform.

---

## 🏛 Architecture & Schema

### Service Specifications
- **Server Port**: `8091`
- **Database**: PostgreSQL 18 on port `5423` (`accounts`)
- **Eureka Registration**: `http://localhost:8070/eureka/` (Dashboard: [http://localhost:8070](http://localhost:8070))
- **Swagger UI**: [http://localhost:8091/swagger-ui/index.html](http://localhost:8091/swagger-ui/index.html)
- **Actuator Health**: [http://localhost:8091/actuator/health](http://localhost:8091/actuator/health)

### Database Schema (`customer` & `accounts`)
- `customer`: `customer_id` (PK, Identity), `name`, `email`, `mobile_number`, audit fields (`created_at`, `created_by`, `updated_at`, `updated_by`).
- `accounts`: `account_number` (PK), `customer_id` (FK -> `customer.customer_id`), `account_type`, `branch_address`, audit fields.

---

## 🚀 REST API Endpoints

| Method | Endpoint | Description | Request Body / Params | Status Codes |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/create` | Onboard Customer & Create Account | `CustomerDto` (JSON) | `201 Created`, `500` |
| `GET` | `/api/fetch` | Fetch Customer & Account Details | `mobileNumber` (Query Param, 10 digits) | `200 OK`, `500` |
| `PUT` | `/api/update` | Update Customer & Account Details | `CustomerDto` (JSON) | `200 OK`, `417 Expectation Failed`, `500` |
| `DELETE`| `/api/delete` | Delete Customer & Account | `mobileNumber` (Query Param, 10 digits) | `200 OK`, `417 Expectation Failed`, `500` |

---

## 🛠 Local Setup & Running

### Prerequisites
- JDK 25
- Docker & Docker Compose

### Commands
```bash
# 1. Build service
./gradlew clean build

# 2. Run unit & integration tests
./gradlew test

# 3. Start local database & API stack via Docker Compose
docker compose up -d

# 4. Or run locally via Gradle
./gradlew bootRun
```
