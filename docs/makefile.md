# Makefile targets

Root [`Makefile`](../Makefile). Compose invocations use `docker/compose*.yml` with `--project-directory .`.

## Stack lifecycle

| Target | Purpose |
| :--- | :--- |
| `all-up` / `all-down` | Start or tear down the default compose stack |
| `dbs-up` / `dbs-down` | All databases (+ Redis via dbs compose) |
| `kafka-up` / `kafka-down` | Apache Kafka broker |
| `keycloak-up` / `keycloak-down` | Keycloak + its Postgres |
| `config-eureka` / `config-eureka-down` | Config Server + Eureka |
| `config-server-up` / `config-server-down` | Config Server only |
| `eureka-server-up` / `eureka-server-down` | Eureka only |

## Domain services

| Target | Purpose |
| :--- | :--- |
| `accounts` / `cards` / `loans` | Start DB + API for that service |
| `accounts-restart` / `cards-restart` / `loans-restart` | Rebuild and recreate that API |
| `accounts-build` / `cards-build` / `loans-build` / `message-build` | Gradle `clean build` |
| `message-up` / `message-restart` / `message-down` | Message worker |
| `gateway-up` / `gateway-restart` / `gateway-down` | Gateway |
| `apis-up` / `apis-down` | Accounts + Cards + Loans APIs |
| `services-up` / `services-down` | Same APIs (alias-style targets) |

## Images (Docker Hub)

| Target | Purpose |
| :--- | :--- |
| `images-build` / `images-push` / `images-build-push` | Build/push Hub images |
| `images-pull` | Pull domain API images |
| `accounts-image-up` / `cards-image-up` / `loans-image-up` | Run DB + API from Hub images |
| `all-image-up` | Full image-based stack (`docker/compose.image.yml`) |
| `all-compose-up` / `all-compose-down` | Monolithic `docker/compose.all.yml` |

## Kubernetes (kind)

| Target | Purpose |
| :--- | :--- |
| `k8s-keycloak` | Apply `kubernetes/1_keycloak.yml` |
| `k8s-configmap` | Apply `kubernetes/2_configmap.yml` |
| `k8s-calico` | Install Calico CNI (`CALICO_VERSION`, default `v3.29.2`) for NetworkPolicy |
| `k8s-accounts` / `k8s-cards` / `k8s-loans` | Apply `<service>/k8s/` (DB, Deployment, ClusterIP Service, NetworkPolicy) |
| `k8s-config-server` / `k8s-eureka-server` / `k8s-gateway-server` | Apply platform service `k8s/` folders |
| `k8s-platform` | Keycloak + ConfigMap |
| `k8s-services` | Config, Eureka, Accounts, Cards, Loans, Gateway |
| `k8s-up` | Platform + all service manifests |

See [kubernetes.md](kubernetes.md) for layout, ClusterIP + NetworkPolicy architecture, Calico-on-kind, `cloud-provider-kind`, and access notes.

## Watch

| Target | Purpose |
| :--- | :--- |
| `watch` / `watch-accounts` / `watch-cards` / `watch-loans` / `watch-gateway` / `watch-message` | Compose watch |
| `watch-all-up` | `all-up` then watch |

## OpenTofu (Keycloak realm)

| Target | Purpose |
| :--- | :--- |
| `infra` / `infra-apply` | Apply realm/clients/users |
| `infra-plan` / `infra-output` / `infra-down` | Plan, show outputs, destroy |
| `infra-init` / `infra-fmt` / `infra-validate` | Init / format / validate |
| `infra-tfvars` | Copy example tfvars if missing |
