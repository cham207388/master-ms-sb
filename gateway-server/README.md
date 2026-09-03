# Spring Cloud Gateway Server

![Java 25](https://img.shields.io/badge/Java-25-orange.svg)
![Spring Cloud Gateway](https://img.shields.io/badge/Spring%20Cloud-Gateway%20WebFlux-green.svg)
![OAuth2](https://img.shields.io/badge/OAuth2-Resource%20Server-purple.svg)
![Keycloak](https://img.shields.io/badge/Keycloak-JWT-blue.svg)

Edge API for `accounts`, `cards`, and `loans`. Routes through Eureka (`lb://…`), rewrites `/accounts|cards|loans/**`, and enforces Keycloak JWT roles on mutating requests.

---

## Specifications

- **Port**: `8072`
- **JWKS**: `http://localhost:7080/realms/securedbankdev/protocol/openid-connect/certs`
- **Routes**: [http://localhost:8072/actuator/gateway/routes](http://localhost:8072/actuator/gateway/routes)
- **Health**: [http://localhost:8072/actuator/health](http://localhost:8072/actuator/health)

---

## Security

OAuth2 resource server. JWT is validated against Keycloak JWKS. `KeycloakRoleConverter` maps `realm_access.roles` to Spring authorities (`ROLE_ACCOUNTS`, `ROLE_CARDS`, `ROLE_LOANS`). CSRF is off.

```mermaid
flowchart LR
  Client -->|Authorization: Bearer JWT| GW[Gateway :8072]
  GW -->|GET JWKS| KC[Keycloak<br/>securedbankdev]
  GW -->|GET permitAll| Svc[lb://ACCOUNTS / CARDS / LOANS]
  GW -->|POST/PUT/DELETE + role| Svc
```

| Path | Rule |
| :--- | :--- |
| `GET /**` | Permit all |
| `/accounts/**`, `/ACCOUNTS/**` | `ROLE_ACCOUNTS` |
| `/cards/**`, `/CARDS/**` | `ROLE_CARDS` |
| `/loans/**`, `/LOANS/**` | `ROLE_LOANS` |

Realm roles and clients are provisioned in [`infra/`](../infra/README.md) (`ACCOUNTS`, `CARDS`, `LOANS`). Example: `POST http://localhost:8072/accounts/api/accounts/create` with a token that includes `ACCOUNTS`.

---

## Routing & resilience

Path rewrite `(?i)/accounts|cards|loans/(.*)` → `/$1`, then `lb://` the matching service.

- **Accounts**: circuit breaker `accountsCircuitBreaker` → `/accounts-fallback`; GET retry ×3
- **Cards**: GET retry ×3
- **Loans**: GET retry ×3; Redis rate limiter (`user` header, else `anonymous`)

---

## Local run

Requires Config Server (`8071`), Eureka (`8070`), and Keycloak (`7080`).

```bash
./gradlew clean build
make gateway-restart
```
