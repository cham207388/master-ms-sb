# Platform docs

Guides for running and navigating the SecuredBank monorepo.

| Doc | Contents |
| :--- | :--- |
| [docker.md](docker.md) | Compose layout under `docker/`, networks, Kafka listeners, how to start stacks |
| [makefile.md](makefile.md) | Make target cheat sheet |
| [../observability/README.md](../observability/README.md) | Loki, Alloy, Grafana, Tempo, Prometheus |
| [../infra/README.md](../infra/README.md) | Keycloak + OpenTofu realm |

## Layout

```text
master-ms-sb/
  docker/           # Compose orchestration (compose.yml, fragments, common.yml)
  docs/             # Platform guides (this folder)
  infra/            # OpenTofu for Keycloak realm/clients/users
  observability/    # Telemetry config (mounted by docker/compose.observability.yml)
  accounts/ …       # Services: source, Dockerfile, compose.yml
```

Service docs stay next to each service (`accounts/README.md`, `message/README.md`, …). Agent guidelines stay at repo root ([`AGENTS.md`](../AGENTS.md)).
