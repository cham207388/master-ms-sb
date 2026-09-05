# SecuredBank Helm chart

Umbrella chart that renders ConfigMap, Kafka, Keycloak (via upstream subchart), and all Spring services from [`values.yaml`](values.yaml). Shared Spring/Postgres/NetworkPolicy templates live in the **library** chart [`charts/securedbank-lib`](charts/securedbank-lib) (`type: library`).

**No Bitnami** (or other commercial) chart dependencies. Kafka and domain Postgres use first-party images (`apache/kafka`, `postgres:18-alpine`). Keycloak uses official `quay.io/keycloak/keycloak` via **[codecentric/keycloakx](https://artifacthub.io/packages/helm/codecentric/keycloakx)**.

## Keycloak

| Piece | Owner | Notes |
|-------|--------|--------|
| Postgres (`keycloak-db` Secret / Service / StatefulSet) | Parent template [`templates/keycloak-db.yaml`](templates/keycloak-db.yaml) | `postgres:18-alpine`, PVC mount `/var/lib/postgresql` |
| Keycloak StatefulSet | Subchart **keycloakx** (`alias: keycloak`) | Image `quay.io/keycloak/keycloak:26.7.0` |
| LoadBalancer (host admin) | Subchart Service `keycloak-http` | Port **7080** → container `http` (kind: `http://localhost:7080`) |
| In-cluster DNS alias | Parent Service `keycloak` | ClusterIP port **7080** for JWKS (`http://keycloak:7080/...`) |

### Contracts

| Contract | Value |
|----------|--------|
| In-cluster JWKS | `http://keycloak:7080/realms/securedbankdev/protocol/openid-connect/certs` |
| Host admin / OpenTofu | `http://localhost:7080` (`admin` / `admin`) |
| Realm / clients / users | OpenTofu after Ready (`make infra`) — **not** Helm realm import |

Toggle with `keycloak.enabled`. Important `keycloak.*` values (see [`values.yaml`](values.yaml)):

- `fullnameOverride: keycloak`
- `http.relativePath: "/"` (chart default `/auth` would break JWKS URLs)
- `proxy.enabled: false` (direct LoadBalancer HTTP, no Ingress)
- `service.type: LoadBalancer`, `service.httpPort: 7080`
- `database.*` → `keycloak-db` + Secret `keycloak-db-secret` / `KC_DB_PASSWORD`
- `args: [start]`, `extraEnv` for bootstrap admin + `KC_HOSTNAME` (`KC_HTTP_ENABLED` comes from the subchart)

## Package dependencies (`.tgz`)

`charts/*.tgz` archives are **gitignored**. Helm needs them before `lint` / `template` / `install`.

- Local library: source tree [`charts/securedbank-lib/`](charts/securedbank-lib/)
- Keycloak: downloaded **keycloakx** from `https://codecentric.github.io/helm-charts` (pinned in [`Chart.yaml`](Chart.yaml) / [`Chart.lock`](Chart.lock))

From repo root (preferred):

```bash
make helm-deps
# → helm dependency update helm/securedbank
# → writes charts/securedbank-lib-0.1.0.tgz and charts/keycloakx-*.tgz
```

Or from this directory:

```bash
cd helm/securedbank
helm dependency update
```

After editing templates under `charts/securedbank-lib/`, run `make helm-deps` again so the library `.tgz` matches the source.

Verify:

```bash
ls helm/securedbank/charts/*.tgz
make helm-lint
```

## Usage

From repo root (`helm-deps` runs automatically before lint/template/up):

```bash
make helm-lint
make helm-template
make helm-up          # helm upgrade --install securedbank ./helm/securedbank
# wait for Keycloak Ready, then:
make infra            # OpenTofu realm securedbankdev
make helm-down
```

Change the global app image tag:

```bash
helm upgrade --install securedbank ./helm/securedbank --set global.imageTag=s15
```

## Relationship to raw YAML

[`kubernetes/`](../kubernetes/) and [`*/k8s/`](../accounts/k8s/) remain the learning / granular apply path (`make k8s-*`), including [`kubernetes/1_keycloak.yml`](../kubernetes/1_keycloak.yml). Prefer **one** apply method per cluster (Helm **or** kubectl) to avoid ownership conflicts.
