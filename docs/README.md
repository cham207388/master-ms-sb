# Platform docs

Guides for running and navigating the SecuredBank monorepo.

| Doc | Contents |
| :--- | :--- |
| [docker.md](docker.md) | Compose layout under `docker/`, networks, Kafka listeners, how to start stacks |
| [makefile.md](makefile.md) | Make target cheat sheet |
| [kubernetes.md](kubernetes.md) | kind manifests, `*/k8s/`, Helm umbrella (`helm/securedbank`), ClusterIP + NetworkPolicy, Calico, `make k8s-*` / `make helm-*` |
| [../observability/README.md](../observability/README.md) | Loki, Alloy, Grafana, Tempo, Prometheus |
| [../infra/README.md](../infra/README.md) | Keycloak + OpenTofu realm |

## Layout

```text
master-ms-sb/
  docker/           # Compose orchestration (compose.yml, fragments, common.yml)
  docs/             # Platform guides (this folder)
  infra/            # OpenTofu for Keycloak realm/clients/users
  kubernetes/       # Platform manifests (Keycloak, ConfigMap) + monolithic copies
  observability/    # Telemetry config (mounted by docker/compose.observability.yml)
  accounts/ …       # Services: source, Dockerfile, compose.yml, k8s/
```

Service docs stay next to each service (`accounts/README.md`, `message/README.md`, …). Agent guidelines stay at repo root ([`AGENTS.md`](../AGENTS.md)).
