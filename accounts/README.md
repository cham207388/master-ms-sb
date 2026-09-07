# Accounts Microservice

![Java 25](https://img.shields.io/badge/Java-25-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen.svg)
![Spring Cloud Stream](https://img.shields.io/badge/Spring%20Cloud%20Stream-Kafka-blue.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18--alpine-blue.svg)

Customer onboarding, account lifecycle, and USD money movement for SecuredBank. Create and money events publish typed notifications; Message sends email via Resend. Only account-open completes the SMS → `communication_sw` path.

---

## Specifications

- **Port**: `8091` · **DB**: PostgreSQL 18 on `5423` (`accounts`)
- **Swagger**: [http://localhost:8091/swagger-ui/index.html](http://localhost:8091/swagger-ui/index.html)
- **Health**: [http://localhost:8091/actuator/health](http://localhost:8091/actuator/health)

**Schema**
- `customer`: `customer_id`, `name`, `email`, `mobile_number`, audit fields
- `accounts`: `account_number`, `customer_id`, `account_type`, `branch_address`, `balance`, `communication_sw`, audit fields
- `transactions`: history rows (`DEPOSIT` / `WITHDRAWAL` / `TRANSFER_OUT` / `TRANSFER_IN`)

> Rewriting Flyway `V1` invalidates checksums — wipe the Accounts Postgres volume/PVC before first boot after a schema rewrite (`make accounts-down` / delete PVC).

---

## Event-driven communication

Accounts publishes `NotificationMsgDto` (`type`, `accountNumber`, `name`, `email`, `mobileNumber`, optional `amount` / `balance` / `counterpartyAccount`) to `send-communication`.

| Type | When | `communication_sw` |
| :--- | :--- | :--- |
| `ACCOUNT_OPENED` | After create | Yes (via `communication-sent`) |
| `TRANSFER_COMPLETED` | After transfer (sender + destination) | No |
| `LOW_BALANCE` | When balance crosses below `$100` | No |

```mermaid
flowchart LR
  Client -->|POST /api/accounts/create| Accounts
  Accounts -->|send-communication<br/>NotificationMsgDto| KFK[(Kafka)]
  KFK -->|email then sms| Message
  Message -->|communication-sent<br/>accountNumber| KFK
  KFK -->|updateCommunication| Accounts
  Accounts -->|communication_sw = true| DB[(accounts)]
```

| Binding | Direction | Destination | Function |
| :--- | :--- | :--- | :--- |
| `sendCommunication-out-0` | out | `send-communication` | `StreamBridge` |
| `updateCommunication-in-0` | in (`group: accounts`) | `communication-sent` | `Consumer<Long>` |

---

## REST API

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/accounts/create` | Onboard customer, open account (balance `0`), publish `ACCOUNT_OPENED` |
| `GET` | `/api/accounts/fetch` | Fetch by `mobileNumber` (includes `balance`) |
| `PUT` | `/api/accounts/update` | Update customer and account |
| `DELETE` | `/api/accounts/delete` | Delete by `mobileNumber` |
| `POST` | `/api/accounts/deposit` | Credit balance |
| `POST` | `/api/accounts/withdraw` | Debit if funds allow |
| `POST` | `/api/accounts/transfer` | Atomic transfer between accounts |
| `GET` | `/api/accounts/transactions` | History by `accountNumber` or `mobileNumber` |
| `GET` | `/api/accounts/customers/fetchCustomerDetails` | Aggregate customer, cards, and loans |

Money moves use `@Transactional` and pessimistic locks. Self-transfers and non-positive amounts are rejected.

---

## Local run

Requires Kafka (`make kafka-up` or full stack). Compose sets `KAFKA_BROKER=kafka:19092`; local `bootRun` defaults to `localhost:9092`.

```bash
./gradlew clean build
make accounts-restart   # rebuild and recreate accounts-api
```

### Kubernetes (kind)

Manifests: [`k8s/`](k8s/) (`db.yml`, `deployment.yml`, `service.yml`, `networkpolicy.yml`). From repo root: `make k8s-accounts`. See [docs/kubernetes.md](../docs/kubernetes.md).
