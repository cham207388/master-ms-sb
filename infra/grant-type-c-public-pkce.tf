// Public SPA client: authorization code + PKCE (S256) for the future Vite UI on :5173.
resource "keycloak_openid_client" "spa_pkce" {
  realm_id  = keycloak_realm.main.id
  client_id = var.spa_client_id

  name        = var.spa_client_id
  description = "Public PKCE SPA client for ${var.spa_client_id}"
  enabled     = true

  access_type = "PUBLIC"

  standard_flow_enabled        = true
  implicit_flow_enabled        = false
  direct_access_grants_enabled = false
  service_accounts_enabled     = false

  pkce_code_challenge_method = "S256"

  valid_redirect_uris = [
    "http://localhost:5173/*",
  ]
  valid_post_logout_redirect_uris = [
    "http://localhost:5173/",
  ]
  web_origins = [
    "http://localhost:5173",
  ]

  # Only explicitly mapped roles are included in access tokens.
  full_scope_allowed = false
}

# Same realm role mappers as other API clients so Gateway JWT roles work.
resource "keycloak_generic_role_mapper" "spa_pkce" {
  for_each = keycloak_role.service_account

  realm_id  = keycloak_realm.main.id
  client_id = keycloak_openid_client.spa_pkce.id
  role_id   = each.value.id
}
