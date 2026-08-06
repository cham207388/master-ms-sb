# Loans Microservice

![Java 25](https://img.shields.io/badge/Java-25-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen.svg)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.1.2-blue.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18--alpine-blue.svg)
![Flyway](https://img.shields.io/badge/Flyway-Migration-red.svg)
![OpenAPI](https://img.shields.io/badge/OpenAPI-3.0.2-green.svg)

The **Loans Microservice** handles customer loans (home, personal, vehicle), repayment tracking, total borrowed limits, and outstanding balances for the EazyBank platform.

---

## 🏛 Architecture & Schema

### Service Specifications
- **Server Port**: `8093`
- **Database**: PostgreSQL 18 on port `5425` (`loans`)
- **Swagger UI**: [http://localhost:8093/swagger-ui/index.html](http://localhost:8093/swagger-ui/index.html)
- **Actuator Health**: [http://localhost:8093/actuator/health](http://localhost:8093/actuator/health)

### Database Schema (`loans`)
- `loans`: `loan_id` (PK, Identity), `mobile_number`, `loan_number`, `loan_type`, `total_loan`, `amount_paid`, `outstanding_amount`, audit fields (`created_at`, `created_by`, `updated_at`, `updated_by`).

---

## 🚀 REST API Endpoints

| Method | Endpoint | Description | Request Body / Params | Status Codes |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/create` | Create New Loan | `mobileNumber` (Query Param, 10 digits) | `201 Created`, `500` |
| `GET` | `/api/fetch` | Fetch Loan Details | `mobileNumber` (Query Param, 10 digits) | `200 OK`, `500` |
| `PUT` | `/api/update` | Update Loan Details | `LoansDto` (JSON) | `200 OK`, `417 Expectation Failed`, `500` |
| `DELETE`| `/api/delete` | Delete Loan Details | `mobileNumber` (Query Param, 10 digits) | `200 OK`, `417 Expectation Failed`, `500` |

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
