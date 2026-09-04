# SecuredBank Helm chart

Umbrella chart that renders Keycloak, ConfigMap, Kafka, and all Spring services from [`values.yaml`](values.yaml). Shared templates live in the **library** chart [`charts/securedbank-lib`](charts/securedbank-lib) (`type: library`).

No Bitnami (or other commercial) chart dependencies — images match the raw manifests (`postgres:18-alpine`, `apache/kafka`, `quay.io/keycloak/keycloak`, `baicham/securedbank-*`).

## Usage

From repo root:

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
