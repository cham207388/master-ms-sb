# Keycloak infra

OpenTofu provisions a local [Keycloak](https://www.keycloak.org/) tenant for Securedbank: realm, OAuth clients, realm roles, and two demo users. The server itself is **Keycloak 26.7.0**. Local Compose: [`docker/compose.keycloak.yml`](../docker/compose.keycloak.yml). Kind: [`kubernetes/1_keycloak.yml`](../kubernetes/1_keycloak.yml) (Postgres StatefulSet + Deployment). Host URL: `http://localhost:7080`. This stack uses OpenTofu with provider `keycloak/keycloak` `5.8.0`.

```bash
# Compose
make keycloak-up      # start Keycloak + Postgres
make keycloak-down    # stop Keycloak and wipe its volume

# kind
make k8s-keycloak     # apply kubernetes/1_keycloak.yml

make infra            # tofu init + apply (alias: make infra-apply)
make infra-plan       # preview changes
make infra-output     # realm, client IDs, OIDC URLs
make infra-down       # destroy OpenTofu resources
```

On first run, `make infra-init` copies `terraform.tfvars.example` → `terraform.tfvars` if needed. Edit secrets there (`terraform.tfvars` is gitignored). OpenTofu logs into the built-in **master** realm via `admin-cli`, then creates the application realm.

Discovery document (after apply): `http://localhost:7080/realms/securedbankdev/.well-known/openid-configuration`

Admin console: `http://localhost:7080` (`admin` / `admin` by default).

---

## Realm

A **realm** is Keycloak’s security tenant. It owns users, clients, roles, authentication flows, and signing keys. Realms do not share users or keys.

This project creates `securedbankdev` (`var.realm`). Local HTTP is allowed (`ssl_required = none`). Login-by-email is on; duplicate emails are off.

The **master** realm is only for Keycloak administration. Do not put application users or API clients there.

---

## Users

**Users** authenticate in a realm. They have credentials, roles, and optional required actions (for example TOTP enrollment).

| Username | Roles | Notes |
| :--- | :--- | :--- |
| `happy@example.com` | `USER` | Standard user |
| `johndoe@example.com` | `USER`, `ADMIN` | Admin user |

Initial password comes from `user_password`. Both users also get the built-in `account` client’s `view-profile` role so UserInfo / account APIs can read their profile.

A confidential client with **service accounts** also gets a hidden service-account user. Tokens from `client_credentials` are issued for that user, not a human.

---

## Roles

**Realm roles** are permissions defined on the realm (`USER`, `ADMIN` here). **Client roles** belong to one client (this stack uses `account` / `view-profile`).

Role mappings decide who gets which role. **Role mappers** decide whether those roles appear in the access token. `full_scope_allowed = false` on every client, so only explicitly mapped roles are included.

---

## Clients

A **client** is an application that talks to Keycloak. OIDC “Client authentication” maps to:

| Access type | Client authentication | Secret | Typical use |
| :--- | :--- | :--- | :--- |
| **Confidential** | On | Yes | Servers, M2M |
| **Public** | Off | No | SPAs, mobile |

| Client ID | Type | Flow | Audience |
| :--- | :--- | :--- | :--- |
| `securedbank-cc` | Confidential | Client credentials | Accounts / cards / loans APIs (M2M) |
| `securedbank-ac` | Confidential | Authorization code (no PKCE) | Interactive login tests |

The UI client redirects to `http://localhost:4200/dashboard` after login and `http://localhost:4200/home` after logout. The other browser clients allow `*` redirects for local testing only.

Client secrets are write-only (`client_secret_wo`). Rotate by changing the secret **and** bumping `client_secret_version` / `auth_code_client_secret_version`.

---

## Grant types

Keycloak exposes these OAuth 2.0 / OIDC grants on `/.well-known/openid-configuration`. This repo enables only what each client needs.

| Grant | Keycloak switch | This stack |
| :--- | :--- | :--- |
| **Authorization code** | Standard flow | Browser clients. Browser hits `/auth`, gets a code, exchanges it at `/token`. |
| **Client credentials** | Service accounts | `securedbank-cc` only. POST `/token` with `grant_type=client_credentials` and the client secret. |
| **Refresh token** | Issued with code flow | Used to get a new access token without logging in again. |
| **Implicit** | Implicit flow | Off on every client. Do not use for new apps. |
| **Direct access grants** (resource owner password) | Direct access grants | Off on every client. Disabled by default on new Keycloak 26 clients. |

---

## PKCE

**PKCE** (Proof Key for Code Exchange) binds the authorization code to the client that started the login. The public clients require **S256**. They send a `code_challenge` to `/auth` and the matching `code_verifier` to `/token`.

`securedbank-ac` leaves PKCE unset so the confidential authorization-code path can be tested with a client secret only.

---

## TOTP and required actions

A **required action** runs on next login until the user completes it. `CONFIGURE_TOTP` is enabled as a default action, and both demo users start with it.

Realm OTP policy: TOTP, HMAC-SHA1, 6 digits, **15-second** period, codes not reusable. After enrollment Keycloak clears the action; OpenTofu ignores later `required_actions` drift so it is not re-applied.

---

## OIDC endpoints

Base: `http://localhost:7080/realms/securedbankdev`

| Endpoint | Path | Use |
| :--- | :--- | :--- |
| Issuer (`iss`) | `/realms/{realm}` | Resource servers check this claim. |
| Authorization | `.../protocol/openid-connect/auth` | Browser login (code flow). |
| Token | `.../protocol/openid-connect/token` | Code exchange, client credentials, refresh. |
| UserInfo | `.../protocol/openid-connect/userinfo` | Profile for a **user** access token (not M2M). |
| JWKS | `.../protocol/openid-connect/certs` | Public signing keys (`jwk-set-uri` in Spring Security). |
| Logout | `.../protocol/openid-connect/logout` | End the SSO session. |

`make infra-output` prints the same URLs for the configured `keycloak_url` and realm.
