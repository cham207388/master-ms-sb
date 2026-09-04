# Kubernetes (kind)

Local orchestration for SecuredBank on [kind](https://kind.sigs.k8s.io/). Platform resources live under [`kubernetes/`](../kubernetes/); each microservice keeps split manifests in `<service>/k8s/`.

## Layout

| Path | Role |
| :--- | :--- |
| [`kubernetes/1_keycloak.yml`](../kubernetes/1_keycloak.yml) | Keycloak Deployment + Postgres StatefulSet + Secret + Services |
| [`kubernetes/2_configmap.yml`](../kubernetes/2_configmap.yml) | Shared `securedbank-configmap` (Config, Eureka, Kafka, Redis, Keycloak JWKS) |
| [`kubernetes/3_*.yml` … `8_*.yml`](../kubernetes/) | Monolithic copies (learning / alternate apply path); **do not delete** |
| `accounts/k8s/`, `cards/k8s/`, `loans/k8s/` | `db.yml` (Service + StatefulSet), `deployment.yml`, `service.yml` |
| `config-server/k8s/`, `eureka-server/k8s/`, `gateway-server/k8s/` | `deployment.yml`, `service.yml` |

Postgres 18 volumes mount at **`/var/lib/postgresql`** (same as Compose). Do not mount at `/var/lib/postgresql/data`.

## Prerequisites

1. kind cluster running (`kubectl` context `kind-kind`).
2. [cloud-provider-kind](https://kind.sigs.k8s.io/docs/user/loadbalancer/) on the **host** (not in-cluster):

```bash
brew install cloud-provider-kind
sudo cloud-provider-kind          # foreground
# or: sudo -b cloud-provider-kind # background
# stop: sudo pkill cloud-provider-kind
```

On macOS/Docker Desktop, prefer **`localhost:<Service port>`** (mapped by the `kindccm-...` container). Do not use the Docker bridge `EXTERNAL-IP` from `kubectl get svc` in a browser.

## Apply (Makefile)

```bash
make k8s-keycloak          # kubernetes/1_keycloak.yml
make k8s-configmap         # kubernetes/2_configmap.yml
make k8s-config-server
make k8s-eureka-server
make k8s-accounts          # accounts/k8s/
make k8s-cards
make k8s-loans
make k8s-gateway-server

make k8s-platform          # keycloak + configmap
make k8s-services          # all six service folders
make k8s-up                # platform + services
```

Equivalent raw applies:

```bash
kubectl apply -f kubernetes/1_keycloak.yml
kubectl apply -f kubernetes/2_configmap.yml
kubectl apply -f accounts/k8s/
# …
```

## Keycloak + realm

After Keycloak is Ready:

```bash
# Admin UI (expand left nav; realm dropdown → securedbankdev)
open http://localhost:7080/admin/

make infra                 # OpenTofu creates securedbankdev on the Keycloak Postgres PVC
```

H2/`start-dev` is not used in k8s: realm data survives Keycloak pod restarts while the Postgres PVC remains.

## Shared ConfigMap keys

Consumed via `configMapKeyRef` from `securedbank-configmap` (see [`kubernetes/2_configmap.yml`](../kubernetes/2_configmap.yml)):

- `CONFIG_SERVER_URL`, `SPRING_CONFIG_IMPORT` (`optional:configserver:…`)
- `EUREKA_DEFAULT_ZONE`, `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE`
- `KAFKA_BROKER`, `SPRING_DATA_REDIS_HOST`, `SPRING_DATA_REDIS_PORT`
- `KEYCLOAK_JWK_SET_URI` (gateway; use Service port `7080` → `http://keycloak:7080/...`)

Per-service names and datasource URLs stay on each Deployment (not in the shared map).

## Suggested order

1. `make k8s-platform` (wait for Keycloak; `make infra`)
2. `make k8s-config-server` then `make k8s-eureka-server`
3. `make k8s-accounts` / `k8s-cards` / `k8s-loans`
4. `make k8s-gateway-server` (needs Redis if rate limiting is enabled; ConfigMap points at `redis`)
