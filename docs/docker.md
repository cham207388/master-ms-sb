# Docker Compose

Platform orchestration lives under [`docker/`](../docker/). Service Dockerfiles and per-service `compose.yml` stay next to each service.

## Files

| File | Role |
| :--- | :--- |
| [`docker/compose.yml`](../docker/compose.yml) | Default full stack (`include:` of DBs, Kafka, observability, APIs, message, gateway) |
| [`docker/compose.image.yml`](../docker/compose.image.yml) | Hub images for domain APIs + message (+ Kafka via `compose.event.yml`) |
| [`docker/compose.dbs.yml`](../docker/compose.dbs.yml) | PostgreSQL + Redis |
| [`docker/compose.event.yml`](../docker/compose.event.yml) | Apache Kafka |
| [`docker/compose.observability.yml`](../docker/compose.observability.yml) | Loki, Alloy, Grafana, Tempo, Prometheus, MinIO |
| [`docker/compose.keycloak.yml`](../docker/compose.keycloak.yml) | Keycloak + its Postgres (not in default `include`) |
| [`docker/compose.all.yml`](../docker/compose.all.yml) | Monolithic alternate stack (CI / one-file); prefer `compose.yml` for day-to-day |
| [`docker/common.yml`](../docker/common.yml) | Shared Compose anchors (network, OTEL env, healthcheck defaults) |

## Always use project directory = repo root

Compose files sit under `docker/`, but the project name and many paths must stay anchored at the **repo root**. The Makefile sets this for you:

```bash
docker compose -f docker/compose.yml --project-directory . up -d
# equivalent: make all-up
```

Prefer `make` targets over running fragment files (`compose.dbs.yml`, `compose.event.yml`) alone: those fragments `extends` [`docker/common.yml`](../docker/common.yml) relative to `docker/`, which breaks if they are the primary `-f` file with `--project-directory .`. Use `make dbs-up` / `make kafka-up` (they go through `docker/compose.yml`).

Volume mounts in [`docker/compose.observability.yml`](../docker/compose.observability.yml) use `../observability/...` (relative to the file under `docker/`). The monolithic [`docker/compose.all.yml`](../docker/compose.all.yml) uses `./observability/...` because it is run as a primary compose file with `--project-directory .`.

## Networks

- `securedbank` — APIs, DBs, Kafka, Redis, gateway, config, Eureka
- `loki` — observability stack (Alloy, Loki targets, MinIO, Grafana, Tempo)

## Kafka dual listeners

| Listener | Bind | Advertised | Used by |
| :--- | :--- | :--- | :--- |
| `PLAINTEXT_HOST` | `:9092` | `localhost:9092` | Host (`./gradlew bootRun`) |
| `PLAINTEXT` | `:19092` | `kafka:19092` | Containers on `securedbank` |

Compose services set `KAFKA_BROKER=kafka:19092`. Bootstrapping containers on `kafka:9092` returns `localhost` metadata and fails inside the client container.

## Common commands

```bash
make all-up                          # full stack
make kafka-up                        # Kafka only (via main compose)
make dbs-up                          # Postgres + Redis
make keycloak-up && make infra       # Keycloak + OpenTofu realm
make accounts-restart                # rebuild/recreate accounts-api
make all-down                        # tear down default project

# Observability-only (uses main stack includes; or start via all-up)
make all-up                          # includes observability
```

See [makefile.md](makefile.md) for the full target list.
