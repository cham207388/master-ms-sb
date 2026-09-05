# SecuredBank Microservices Architecture

![Java 25](https://img.shields.io/badge/Java-25-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen.svg)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.1.2-blue.svg)
![Spring Cloud Gateway](https://img.shields.io/badge/Spring%20Cloud-Gateway%20WebFlux-green.svg)
![Keycloak](https://img.shields.io/badge/Keycloak-OAuth2%20JWT-blue.svg)
![Eureka](https://img.shields.io/badge/Eureka-Service%20Discovery-blue.svg)
![Grafana](https://img.shields.io/badge/Grafana-11.5.2-orange.svg)
![Loki](https://img.shields.io/badge/Loki-3.4.2-blue.svg)
![Alloy](https://img.shields.io/badge/Grafana%20Alloy-1.7.1-red.svg)
![MinIO](https://img.shields.io/badge/MinIO-S3%20Store-pink.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18--alpine-blue.svg)
![Kafka](https://img.shields.io/badge/Apache%20Kafka-Stream-231F20.svg)
![Flyway](https://img.shields.io/badge/Flyway-Migration-red.svg)
![Testcontainers](https://img.shields.io/badge/Testcontainers-1.20.4-black.svg)
![Docker](https://img.shields.io/badge/Docker%20Compose-Enabled-blue.svg)
![OpenAPI](https://img.shields.io/badge/OpenAPI-3.0.2-green.svg)

Domain-driven banking platform on **Spring Boot 4.1.0**, **Spring Cloud 2025.1.2**, and **Java 25**. Domain APIs (**Accounts**, **Cards**, **Loans**) sit behind a Keycloak-secured Gateway. Config Server and Eureka handle config and discovery. Accounts publishes communication events over **Apache Kafka**; the **Message** worker sends email/SMS and Accounts marks `communication_sw`. Grafana, Loki, Alloy, Tempo, and MinIO cover telemetry.

Service docs: [accounts](accounts/README.md) · [cards](cards/README.md) · [loans](loans/README.md) · [message](message/README.md) · [gateway](gateway-server/README.md) · [config](config-server/README.md) · [eureka](eureka-server/README.md) · [keycloak](infra/README.md) · [observability](observability/README.md)

Platform guides: [docs/](docs/README.md) · [Docker Compose](docs/docker.md) · [Makefile](docs/makefile.md) · [Kubernetes](docs/kubernetes.md) · Orchestration: [`docker/`](docker/)

---

<details>
<summary><span style="color: cyan;"><strong>Architecture</strong></span></summary>

```mermaid
flowchart TB
  Client[HTTP Client]
  KC[Keycloak :7080<br/>realm securedbankdev]

  subgraph edge [Edge]
    GW[Gateway :8072]
  end

  subgraph platform [Platform]
    CFG[Config Server :8071]
    EU[Eureka :8070]
    KFK[(Kafka :9092/:19092)]
  end

  subgraph domain [Domain]
    ACC[Accounts :8091]
    CRD[Cards :8092]
    LON[Loans :8093]
    MSG[Message worker]
    ACCDB[(accounts :5423)]
    CRDDB[(cards :5424)]
    LONDB[(loans :5425)]
  end

  subgraph telemetry [Observability]
    Alloy[Alloy :12345]
    LokiGW[Loki gateway :3100]
    Tempo[Tempo OTLP]
    Grafana[Grafana :3000]
  end

  Client -->|Bearer JWT| GW
  GW -->|JWKS| KC
  GW -->|lb://ACCOUNTS| ACC
  GW -->|lb://CARDS| CRD
  GW -->|lb://LOANS| LON

  ACC --> ACCDB
  CRD --> CRDDB
  LON --> LONDB

  ACC & CRD & LON & GW -.->|config / register| CFG
  ACC & CRD & LON & GW -.-> EU

  ACC -->|send-communication| KFK
  KFK -->|email then sms| MSG
  MSG -->|communication-sent| KFK
  KFK -->|updateCommunication| ACC

  Alloy -->|push tenant1| LokiGW
  ACC & CRD & LON & GW & MSG -.->|OTLP| Tempo
  Grafana --> LokiGW
  Grafana --> Tempo
```

</details>

---

<details>
<summary><span style="color: cyan;"><strong>Event-driven communication</strong></span></summary>

On `POST /api/accounts/create`, Accounts publishes `AccountsMsgDto` to `send-communication`. Message runs composed function `email|sms` and publishes `accountNumber` to `communication-sent`. Accounts then sets `communication_sw = true`.

```mermaid
flowchart LR
  ACC[Accounts] -->|send-communication| KFK[(Kafka)]
  KFK --> MSG[Message email then sms]
  MSG -->|communication-sent| KFK
  KFK --> ACC
```

| Binding | Destination | Role |
| :--- | :--- | :--- |
| `sendCommunication-out-0` | `send-communication` | Accounts → Message |
| `emailsms-in-0` / `emailsms-out-0` | in / `communication-sent` | Message `email\|sms` |
| `updateCommunication-in-0` | `communication-sent` | Accounts consumer |

</details>

---

<details>
<summary><span style="color: cyan;"><strong>Gateway security</strong></span></summary>

OAuth2 resource server. JWT is validated against Keycloak JWKS (`http://localhost:7080/realms/securedbankdev/protocol/openid-connect/certs`). Realm roles `ACCOUNTS`, `CARDS`, `LOANS` become `ROLE_*`. CSRF is off. Realm and clients: [`infra/`](infra/README.md).

| Path | Rule |
| :--- | :--- |
| `GET /**` | Permit all |
| `/accounts/**`, `/ACCOUNTS/**` | `ROLE_ACCOUNTS` |
| `/cards/**`, `/CARDS/**` | `ROLE_CARDS` |
| `/loans/**`, `/LOANS/**` | `ROLE_LOANS` |

Path rewrite `(?i)/accounts|cards|loans/(.*)` → `/$1`, then `lb://` the matching service. Accounts has a circuit breaker + fallback; loans has a Redis rate limiter.

</details>

---

<details>
<summary><span style="color: cyan;"><strong>Tech Stack</strong></span></summary>

| Component | Technology | Description |
| :--- | :--- | :--- |
| **Language** | Java 25 | Toolchain (`JavaLanguageVersion.of(25)`) |
| **Framework** | Spring Boot 4.1.0 | Web MVC, Data JPA, Actuator |
| **API Gateway** | Spring Cloud Gateway WebFlux | Reactive edge routing ([`gateway-server`](gateway-server)) |
| **Security** | Keycloak + OAuth2 Resource Server | JWT JWKS validation; realm roles `ACCOUNTS` / `CARDS` / `LOANS` |
| **Load Balancer** | Spring Cloud LoadBalancer | `lb://ACCOUNTS`, `lb://CARDS`, `lb://LOANS` |
| **Central Config** | Spring Cloud Config | ([`config-server`](config-server)) |
| **Service Discovery** | Netflix Eureka | ([`eureka-server`](eureka-server)) |
| **Messaging** | Spring Cloud Stream + Apache Kafka | Accounts ↔ Message (`send-communication` / `communication-sent`) |
| **Observability UI** | Grafana 11.5.2 | Dashboards; Loki datasource `tenant1` |
| **Log Engine** | Grafana Loki 3.4.2 | `read`, `write`, `backend` targets |
| **Log Collector** | Grafana Alloy v1.7.1 | Harvests Docker logs via `/var/run/docker.sock` |
| **Tracing** | Tempo 2.9.0 / OpenTelemetry Agent | OTLP `4317` gRPC / `4318` HTTP |
| **Object Storage** | MinIO | S3 backend for Loki chunks |
| **Loki Edge Proxy** | Nginx 1.27.4-alpine | Push → write; query → read |
| **Build** | Gradle | Per-service `./gradlew` |
| **Database** | PostgreSQL 18 Alpine | One DB per domain service |
| **Migrations** | Flyway | `db/migration/V*.sql` |
| **API Docs** | SpringDoc OpenAPI 3.0.2 | `/swagger-ui/index.html` |
| **Testing** | Testcontainers | `@ServiceConnection` PostgreSQL |
| **Containers** | Docker Compose | [`docker/compose.yml`](docker/compose.yml) with `include:` |

</details>

---

<details>
<summary><span style="color: cyan;"><strong>Infrastructure, Ports & Endpoints</strong></span></summary>

| Component | Path | Port | DB / Host Port | UI / Docs | Health / Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Gateway** | [`gateway-server`](gateway-server) | `8072` | — | [routes](http://localhost:8072/actuator/gateway/routes) | [health](http://localhost:8072/actuator/health) · [CBs](http://localhost:8072/actuator/circuitbreakers) |
| **Accounts** | [`accounts`](accounts) | `8091` | `accounts` / `5423` | [swagger](http://localhost:8091/swagger-ui/index.html) | [health](http://localhost:8091/actuator/health) · [CBs](http://localhost:8091/actuator/circuitbreakers) |
| **Cards** | [`cards`](cards) | `8092` | `cards` / `5424` | [swagger](http://localhost:8092/swagger-ui/index.html) | [health](http://localhost:8092/actuator/health) |
| **Loans** | [`loans`](loans) | `8093` | `loans` / `5425` | [swagger](http://localhost:8093/swagger-ui/index.html) | [health](http://localhost:8093/actuator/health) |
| **Message** | [`message`](message) | `9010` internal | — | Worker (no published HTTP) | — |
| **Config Server** | [`config-server`](config-server) | `8071` | — | — | [health](http://localhost:8071/actuator/health) <br> [accounts-config-prod](http://localhost:8071/accounts/prod) <br> [accounts-config-default](http://localhost:8071/accounts/default) <br> [cards-config-prod](http://localhost:8071/cards/prod) <br> [cards-config-default](http://localhost:8071/cards/default) <br> [loans-config-prod](http://localhost:8071/loans/prod) <br> [loans-config-default](http://localhost:8071/loans/default) |
| **Eureka** | [`eureka-server`](eureka-server) | `8070` | — | [dashboard](http://localhost:8070) | [health](http://localhost:8070/actuator/health) |
| **Keycloak** | [`infra`](infra) | `7080` | — | [admin](http://localhost:7080) · realm `securedbankdev` | — |
| **Kafka** | [`docker/compose.event.yml`](docker/compose.event.yml) | `9092` host / `19092` Docker | — | Broker for Accounts ↔ Message | — |
| **Redis** | — | `6379` | — | Gateway rate limiter | — |
| **Grafana** | [`observability/grafana`](observability/grafana) | `3000` | — | [UI](http://localhost:3000) | [api/health](http://localhost:3000/api/health) |
| **Tempo** | [`observability/tempo`](observability/tempo) | `3110` | OTLP `4317`/`4318` | — | — |
| **Loki gateway** | [`observability/loki`](observability/loki) | `3100` | Read `3101` · Write `3102` | [3100](http://localhost:3100) | [read ready](http://localhost:3101/ready) · [write ready](http://localhost:3102/ready) |
| **MinIO** | — | `9000` / `9001` | — | [console](http://localhost:9001) | [live](http://localhost:9000/minio/health/live) |
| **Alloy** | [`observability/alloy`](observability/alloy) | `12345` | — | [UI](http://localhost:12345) | — |

</details>

---

<details>
<summary><span style="color: cyan;"><strong>Observability & log telemetry</strong></span></summary>

```txt
[ Docker Socket /var/run/docker.sock ]
            │
            ▼ harvest stdout/stderr
[ Grafana Alloy :12345 ]
            │
            ▼ HTTP push / tenant1
[ Loki Nginx Gateway :3100 ]
       ├──► Write :3102 ──► MinIO (loki-data, loki-ruler)
       └──► Read  :3101 ◄── MinIO
            ▲
            │ LogQL
[ Grafana :3000 ]
```

**Alloy** mounts `/var/run/docker.sock`, labels streams with `container` (e.g. `accounts-api`), and pushes to Loki with `X-Scope-OrgID: tenant1`.

**Loki** uses separate read / write / backend targets. **Tempo** receives OTLP from the OpenTelemetry Java agent (`JAVA_TOOL_OPTIONS` in [`docker/common.yml`](docker/common.yml), `OTEL_EXPORTER_OTLP_ENDPOINT: http://tempo:4317`). Set `OTEL_SERVICE_NAME` per service (`accounts`, `cards`, `loans`, `message`, `gateway-server`, …).

**LogQL examples**

```logql
{container="accounts-api"}
{container="accounts-api"} |= "ERROR"
{container="gateway-server"} | json | level="error"
{container="accounts-api"} != "/actuator/health"
sum by (container) (rate({container=~".+"}[1m]))
```

Full detail: [observability/README.md](observability/README.md).

</details>

---

<details>
<summary><span style="color: cyan;"><strong>Local development</strong></span></summary>

**Prerequisites:** JDK 25, Docker & Docker Compose.

```bash
# Full stack (includes Message + Kafka + observability)
make all-up
# or: docker compose -f docker/compose.yml --project-directory . up -d

# Keycloak + realm/roles (needed for mutating gateway calls)
make keycloak-up && make infra

# Observability is included in make all-up (see docs/docker.md)

# Rebuild / recreate a service
make accounts-restart
make cards-restart
make loans-restart
make message-restart
make gateway-restart

make all-down
```

Compose layout and Kafka listeners: [docs/docker.md](docs/docker.md).

**Build**

```bash
make accounts-build
make cards-build
make loans-build
make message-build
make eureka-server-build
make gateway-server-build
# or: cd <service> && ./gradlew clean build
```

**Boot locally**

```bash
cd config-server && ./gradlew bootRun   # 8071
cd eureka-server && ./gradlew bootRun   # 8070
cd gateway-server && ./gradlew bootRun  # 8072
cd accounts && ./gradlew bootRun        # 8091
cd cards && ./gradlew bootRun           # 8092
cd loans && ./gradlew bootRun           # 8093
cd message && ./gradlew bootRun         # 9010 (worker)
```

</details>

---

<details>
<summary><span style="color: cyan;"><strong>Environment variables</strong></span></summary>

| Variable | Description | Typical defaults |
| :--- | :--- | :--- |
| `SPRING_DATASOURCE_URL` | JDBC URL | Accounts `…:5423/accounts` · Cards `…:5424/cards` · Loans `…:5425/loans` |
| `SPRING_DATASOURCE_USERNAME` / `PASSWORD` | DB credentials | `postgres` / `postgres` |
| `SPRING_CONFIG_IMPORT` | Config Server | `optional:configserver:http://localhost:8071/` |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | Eureka zone | `http://localhost:8070/eureka/` |
| `KAFKA_BROKER` | Kafka bootstrap | `localhost:9092` (in Compose: `kafka:19092`) |
| `SPRING_DATA_REDIS_HOST` / `PORT` | Gateway rate limiter | `localhost` / `6379` |
| `KEYCLOAK_JWK_SET_URI` | Gateway JWT JWKS | `http://localhost:7080/realms/securedbankdev/protocol/openid-connect/certs` |

</details>

---

<details>
<summary><span style="color: cyan;"><strong>Makefile</strong></span></summary>

Full cheat sheet: [docs/makefile.md](docs/makefile.md).

| Target | Purpose |
| :--- | :--- |
| `all-up` / `all-down` | Start or tear down the compose stack |
| `accounts` / `cards` / `loans` | Start DB + API for that service |
| `accounts-restart` / `cards-restart` / `loans-restart` | Rebuild and recreate that API |
| `accounts-build` / `cards-build` / `loans-build` / `message-build` | Gradle `clean build` |
| `message-up` / `message-restart` / `message-down` | Message worker |
| `gateway-up` / `gateway-restart` / `gateway-down` | Gateway |
| `eureka-server-up` / `eureka-server-down` | Eureka |
| `kafka-up` / `kafka-down` | Apache Kafka broker |
| `dbs-up` / `dbs-down` | All databases |
| `keycloak-up` / `infra` / `infra-down` / `keycloak-down` | Keycloak + OpenTofu realm |
| `images-build` / `images-push` / `images-build-push` | Hub images (APIs, message, gateway, config, eureka) |
| `watch` / `watch-accounts` / `watch-message` / … | Compose watch |
| `k8s-keycloak` / `k8s-configmap` | Apply platform manifests under `kubernetes/` |
| `k8s-calico` | Install Calico on kind (NetworkPolicy; needs `disableDefaultCNI`) |
| `k8s-kafka` | Apply `kubernetes/9_kafka.yml` |
| `k8s-accounts` / `k8s-cards` / `k8s-loans` / `k8s-message` | Apply `<service>/k8s/` (message: Deployment + ClusterIP) |
| `k8s-config-server` / `k8s-eureka-server` / `k8s-gateway-server` | Apply platform service `k8s/` folders |
| `k8s-platform` / `k8s-services` / `k8s-up` | Platform only, services only, or everything |
| `helm-up` / `helm-down` / `helm-lint` / `helm-template` | Umbrella Helm chart (`helm/securedbank`; no Bitnami; Keycloak via codecentric/keycloakx) |

### Docker Hub images

Default tag is `latest` via `IMAGE_TAG`. Override with `IMAGE_TAG=...`, or use `*-tag` targets that require `TAG=` for immutable releases.

| Makefile Target | Image(s) | Purpose |
| :-------------- | :------- | :------ |
| `accounts-image-build` / `accounts-image-push` | `baicham/securedbank-accounts-api:$(IMAGE_TAG)` | Build or push Accounts image |
| `cards-image-build` / `cards-image-push` | `baicham/securedbank-cards-api:$(IMAGE_TAG)` | Build or push Cards image |
| `loans-image-build` / `loans-image-push` | `baicham/securedbank-loans-api:$(IMAGE_TAG)` | Build or push Loans image |
| `message-image-build` / `message-image-push` | `baicham/securedbank-message:$(IMAGE_TAG)` | Build or push Message worker image |
| `message-image-up` | — | Run `kafka` + `message` from Hub (`compose.image.yml`) |
| `config-server-image-build` / `config-server-image-push` | `baicham/securedbank-config-server:$(IMAGE_TAG)` | Build or push Config Server image |
| `eureka-server-image-build` / `eureka-server-image-push` | `baicham/securedbank-eureka-server:$(IMAGE_TAG)` | Build or push Eureka Server image |
| `gateway-server-image-build` / `gateway-server-image-push` | `baicham/securedbank-gateway-server:$(IMAGE_TAG)` | Build or push Gateway Server image |
| `images-build` / `images-push` / `images-build-push` | All seven images above | Build and/or push every service image |
| `*-image-build-tag` / `*-image-push-tag` | Same repos with `TAG` | Same as above; **requires** `TAG=` |
| `images-build-tag` / `images-push-tag` / `images-build-push-tag` | All seven with `TAG` | Aggregate immutable build/push; **requires** `TAG=` |

```bash
# Mutable (defaults to latest)
make images-build-push
make images-build-push IMAGE_TAG=v1.0.0

# Immutable (TAG required)
make images-build-push-tag TAG=v1.0.0
make config-server-image-build-tag TAG=v1.0.0
make eureka-server-image-push-tag TAG=v1.0.0
make gateway-server-image-build-tag TAG=v1.0.0
make gateway-server-image-push-tag TAG=v1.0.0
```

</details>

**Observability resources**

- Metrics: `/actuator/metrics`, `/actuator/prometheus` — [Micrometer](https://micrometer.io/) · [Prometheus](https://prometheus.io/)
- Logs: [Loki](https://grafana.com/docs/loki/latest/) via Alloy
- Traces: [Tempo](https://grafana.com/docs/tempo/latest/) · [OpenTelemetry](https://opentelemetry.io/)
- UI: [Grafana](https://grafana.com/) at [http://localhost:3000](http://localhost:3000)

---

<details>
<summary><span style="color: blue;">Kubernetes</span></summary>

Full guide: [docs/kubernetes.md](docs/kubernetes.md).

**Layout**

- Platform: [`kubernetes/1_keycloak.yml`](kubernetes/1_keycloak.yml), [`kubernetes/2_configmap.yml`](kubernetes/2_configmap.yml)
- Per service: `accounts/k8s/`, `cards/k8s/`, `loans/k8s/` (include `networkpolicy.yml`), `message/k8s/`, `config-server/k8s/`, `eureka-server/k8s/`, `gateway-server/k8s/`
- Numbered files `kubernetes/3_*.yml` … `10_message.yml` are monolithic copies (kept for the learning path; `5`–`7` include NetworkPolicies; `9` is Kafka)
- Optional DRY install: [`helm/securedbank`](helm/securedbank) (`make helm-up`) — values-driven; no Bitnami; Keycloak via codecentric/keycloakx + first-party Postgres

**Isolation**

- Edge (gateway / config / eureka / keycloak): LoadBalancer
- Domain APIs + DBs: ClusterIP; NetworkPolicies allow gateway→APIs, accounts→cards/loans (Feign), and API→own DB only
- kindnet does **not** enforce NetworkPolicy — use `make k8s-calico` on a cluster created with `disableDefaultCNI: true` (see [docs/kubernetes.md](docs/kubernetes.md))

**Prerequisites**

- kind cluster + `brew install cloud-provider-kind`
- Run on the host: `sudo cloud-provider-kind` (or `sudo -b …`); stop with `sudo pkill cloud-provider-kind`
- On macOS use `localhost:<port>` (e.g. Keycloak `http://localhost:7080/admin/`), not the Docker bridge EXTERNAL-IP

**Apply**

```bash
# optional: make k8s-calico   # NetworkPolicy enforcement
make k8s-platform          # Keycloak + ConfigMap
make infra                 # OpenTofu realm on Keycloak Postgres
make k8s-services          # or: make k8s-up for platform + services
# per service: make k8s-kafka | k8s-accounts | k8s-cards | k8s-loans | k8s-message | k8s-config-server | …
```

**Access**

- Keycloak admin: http://localhost:7080/admin/ → realm dropdown → `securedbankdev`
- Call domain APIs through the gateway LoadBalancer (`localhost:<gateway port>`), not accounts/cards/loans Services directly
- `kubectl get svc` then curl `http://localhost:<Service port>/…` for edge LoadBalancers when `cloud-provider-kind` has mapped the port

</details>

---

<details>
<summary><span style="color: cyan;"><strong>Helm chart</strong></span></summary>

Chart: [`helm/securedbank`](helm/securedbank). Full notes: [helm/securedbank/README.md](helm/securedbank/README.md).

**After code changes → new images → cluster**

Pick a tag (example `s16`), then run from the repo root:

```bash
# 1. Build and push all securedbank-* images
make images-build-push IMAGE_TAG=s16

# 2. Package the local library chart (.tgz is gitignored)
make helm-deps

# 3. Upgrade the release to that tag
helm upgrade --install securedbank ./helm/securedbank --set global.imageTag=s16

# 4. Confirm pods pulled the new images
kubectl get pods -l 'app in (accounts,cards,loans,message,config-server,eureka-server,gateway-server)' -o wide
kubectl rollout status deployment/eureka-server-deployment
```

Single service only:

```bash
make accounts-image-build IMAGE_TAG=s16
make accounts-image-push IMAGE_TAG=s16
helm upgrade --install securedbank ./helm/securedbank --set global.imageTag=s16
```

Immutable tag style (requires `TAG=`):

```bash
make images-build-push-tag TAG=s16
make helm-deps
helm upgrade --install securedbank ./helm/securedbank --set global.imageTag=s16
```

</details>

<details>
<summary><span style="color: cyan;"><strong>References</strong></span></summary>

- [Spring Boot Application Properties](https://docs.spring.io/spring-boot/appendix/application-properties/index.html)
- [Apache Kafka](https://kafka.apache.org/)
  - [Getting started with Docker](https://kafka.apache.org/43/getting-started/docker/)
  - [Kafka on Docker (Confluent tutorial)](https://developer.confluent.io/confluent-tutorials/kafka-on-docker/)
- [kubernetes](https://kubernetes.io/)
  - [dashboard](https://kubernetes.io/docs/tasks/access-application-cluster/web-ui-dashboard/)
  - [helm](https://helm.sh/)

</details>
