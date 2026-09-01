// authorization code grant type
resource "keycloak_openid_client" "auth_code_type" {
  realm_id  = keycloak_realm.main.id
  client_id = var.auth_code_client_id

  name        = var.auth_code_client_id
  description = "Authorization code OAuth client for ${var.auth_code_client_id}"
  enabled     = true

  access_type = "CONFIDENTIAL"

  # Authorization Code grant for interactive browser login.
  standard_flow_enabled        = true
  implicit_flow_enabled        = false
  direct_access_grants_enabled = false
  service_accounts_enabled     = false

  # PKCE intentionally unset so authorization-code can be tested without it.
  valid_redirect_uris = ["*"]
  web_origins         = ["*"]

  # Only explicitly mapped roles are included in access tokens.
  full_scope_allowed = false

  # Write-only: the secret is sent to Keycloak but not stored in state.
  client_secret_wo         = var.auth_code_client_secret
  client_secret_wo_version = var.auth_code_client_secret_version
}

# Permit realm roles to appear in tokens issued by the authorization-code client.
resource "keycloak_generic_role_mapper" "auth_code" {
  for_each = keycloak_role.service_account

  realm_id  = keycloak_realm.main.id
  client_id = keycloak_openid_client.auth_code_type.id
  role_id   = each.value.id
}
