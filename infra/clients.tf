data "keycloak_openid_client" "account" {
  realm_id  = keycloak_realm.main.id
  client_id = "account"
}

data "keycloak_role" "account_view_profile" {
  realm_id  = keycloak_realm.main.id
  client_id = data.keycloak_openid_client.account.id
  name      = "view-profile"
}

resource "keycloak_openid_client" "client_type" {
  realm_id  = keycloak_realm.main.id
  client_id = var.client_type_client_id

  name        = var.client_type_client_id
  description = "Machine-to-machine OAuth client for ${var.client_type_client_id}"
  enabled     = true

  access_type = "CONFIDENTIAL"

  # Enables the OAuth 2.0 Client Credentials grant.
  service_accounts_enabled = true

  # Disable grants that are not needed by this machine client.
  standard_flow_enabled        = false
  implicit_flow_enabled        = false
  direct_access_grants_enabled = false

  # Only explicitly mapped roles are included in access tokens.
  full_scope_allowed = false

  # Write-only: the secret is sent to Keycloak but not stored in state.
  client_secret_wo         = var.client_secret
  client_secret_wo_version = var.client_secret_version
}

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

resource "keycloak_openid_client" "pkce_type" {
  realm_id  = keycloak_realm.main.id
  client_id = var.pkce_client_id

  name        = var.pkce_client_id
  description = "Public authorization-code OAuth client with PKCE for ${var.pkce_client_id}"
  enabled     = true

  # Public client: Client authentication off (no client secret).
  access_type = "PUBLIC"

  # Authorization Code grant for interactive browser / SPA login.
  standard_flow_enabled        = true
  implicit_flow_enabled        = false
  direct_access_grants_enabled = false
  service_accounts_enabled     = false

  pkce_code_challenge_method = "S256"

  valid_redirect_uris = ["*"]
  web_origins         = ["*"]

  # Only explicitly mapped roles are included in access tokens.
  full_scope_allowed = false
}

resource "keycloak_openid_client" "pkce_ui_type" {
  realm_id  = keycloak_realm.main.id
  client_id = var.pkce_ui_client_id

  name        = var.pkce_ui_client_id
  description = "Public authorization-code OAuth client with PKCE for the Angular UI (${var.pkce_ui_client_id})"
  enabled     = true

  # Public client: Client authentication off (no client secret).
  access_type = "PUBLIC"

  # Authorization Code grant for interactive Angular SPA login.
  standard_flow_enabled        = true
  implicit_flow_enabled        = false
  direct_access_grants_enabled = false
  service_accounts_enabled     = false

  pkce_code_challenge_method = "S256"

  valid_redirect_uris             = ["http://localhost:4200/dashboard"]
  valid_post_logout_redirect_uris = ["http://localhost:4200/home"]
  web_origins                     = ["http://localhost:4200"]

  # Only explicitly mapped roles are included in access tokens.
  full_scope_allowed = false
}

# Assign USER and ADMIN to the securedbank-cc service-account user.
resource "keycloak_openid_client_service_account_realm_role" "service_account" {
  for_each = keycloak_role.service_account

  realm_id                = keycloak_realm.main.id
  service_account_user_id = keycloak_openid_client.client_type.service_account_user_id
  role                    = each.value.name
}

# Permit the explicitly assigned realm roles to appear in the M2M client's token.
resource "keycloak_generic_role_mapper" "service_account" {
  for_each = keycloak_role.service_account

  realm_id  = keycloak_realm.main.id
  client_id = keycloak_openid_client.client_type.id
  role_id   = each.value.id
}

# Permit realm roles to appear in tokens issued by the authorization-code client.
resource "keycloak_generic_role_mapper" "auth_code" {
  for_each = keycloak_role.service_account

  realm_id  = keycloak_realm.main.id
  client_id = keycloak_openid_client.auth_code_type.id
  role_id   = each.value.id
}

# Permit realm roles to appear in tokens issued by the PKCE public client.
resource "keycloak_generic_role_mapper" "pkce" {
  for_each = keycloak_role.service_account

  realm_id  = keycloak_realm.main.id
  client_id = keycloak_openid_client.pkce_type.id
  role_id   = each.value.id
}

# Permit realm roles to appear in tokens issued by the Angular UI PKCE client.
resource "keycloak_generic_role_mapper" "pkce_ui" {
  for_each = keycloak_role.service_account

  realm_id  = keycloak_realm.main.id
  client_id = keycloak_openid_client.pkce_ui_type.id
  role_id   = each.value.id
}

# Allow the Angular UI token to read the signed-in user's Keycloak profile.
resource "keycloak_generic_role_mapper" "pkce_ui_account_view_profile" {
  realm_id  = keycloak_realm.main.id
  client_id = keycloak_openid_client.pkce_ui_type.id
  role_id   = data.keycloak_role.account_view_profile.id
}
