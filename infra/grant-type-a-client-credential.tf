// client credentials grant type
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

# Permit the explicitly assigned realm roles to appear in the M2M client's token.
resource "keycloak_generic_role_mapper" "service_account" {
  for_each = keycloak_role.service_account

  realm_id  = keycloak_realm.main.id
  client_id = keycloak_openid_client.client_type.id
  role_id   = each.value.id
}
