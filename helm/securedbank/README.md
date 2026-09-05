# SecuredBank Helm chart

Umbrella chart that renders Keycloak, ConfigMap, Kafka, and all Spring services from [`values.yaml`](values.yaml). Shared templates live in the **library** chart [`charts/securedbank-lib`](charts/securedbank-lib) (`type: library`).

No Bitnami (or other commercial) chart dependencies — images match the raw manifests (`postgres:18-alpine`, `apache/kafka`, `quay.io/keycloak/keycloak`, `baicham/securedbank-*`).

## Package the local library chart (`.tgz`)

`charts/securedbank-lib-0.1.0.tgz` is **gitignored**. Helm still needs that archive (or an equivalent dependency build) before `lint` / `template` / `install`. Source of truth is the unpacked tree [`charts/securedbank-lib/`](charts/securedbank-lib/).

From repo root (preferred):

```bash
make helm-deps
# → helm dependency update helm/securedbank
# → writes helm/securedbank/charts/securedbank-lib-0.1.0.tgz
```

Or from this directory:

```bash
cd helm/securedbank
helm dependency update
# packages file://charts/securedbank-lib into charts/securedbank-lib-0.1.0.tgz
# refreshes Chart.lock
```

After editing templates under `charts/securedbank-lib/`, run `make helm-deps` again so the `.tgz` matches the source.

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
make helm-down
```

Change the global app image tag:

```bash
helm upgrade --install securedbank ./helm/securedbank --set global.imageTag=s15
```

## Relationship to raw YAML

[`kubernetes/`](../kubernetes/) and [`*/k8s/`](../accounts/k8s/) remain the learning / granular apply path (`make k8s-*`). Prefer **one** apply method per cluster (Helm **or** kubectl) to avoid ownership conflicts.
