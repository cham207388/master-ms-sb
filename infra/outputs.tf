output "realm" {
  description = "Keycloak realm name. A realm is an isolated security tenant that owns users, clients, roles, and signing keys."
  value       = keycloak_realm.main.realm
}

output "client_type_client_id" {
  description = "M2M (client_credentials) OAuth client ID. Sent when requesting machine tokens; appears in JWT claims such as azp and client_id."
  value       = keycloak_openid_client.client_type.client_id
}

output "auth_code_client_id" {
  description = "Authorization-code OAuth client ID used for interactive browser login via the authorization endpoint."
  value       = keycloak_openid_client.auth_code_type.client_id
}

output "issuer_uri" {
  description = "OIDC issuer identifier for this realm (iss claim). Resource servers use it to validate that tokens were issued by this Keycloak realm."
  value       = "${trimsuffix(var.keycloak_url, "/")}/realms/${keycloak_realm.main.realm}"
}

output "token_endpoint" {
  description = "OIDC token endpoint. Clients exchange credentials or authorization codes for access tokens (for example grant_type=client_credentials for M2M, or authorization_code for browser login)."
  value       = "${trimsuffix(var.keycloak_url, "/")}/realms/${keycloak_realm.main.realm}/protocol/openid-connect/token"
}

output "authorization_endpoint" {
  description = "OIDC authorization endpoint. Browsers are redirected here to start the authorization code flow for interactive user login."
  value       = "${trimsuffix(var.keycloak_url, "/")}/realms/${keycloak_realm.main.realm}/protocol/openid-connect/auth"
}

output "userinfo_endpoint" {
  description = "OIDC UserInfo endpoint. Returns profile claims for an access token issued to a human user; typically not usable with service-account tokens."
  value       = "${trimsuffix(var.keycloak_url, "/")}/realms/${keycloak_realm.main.realm}/protocol/openid-connect/userinfo"
}

output "jwks_uri" {
  description = "JSON Web Key Set URI. Publishes Keycloak's public signing keys so resource servers (Spring Security jwk-set-uri) can verify JWT signatures."
  value       = "${trimsuffix(var.keycloak_url, "/")}/realms/${keycloak_realm.main.realm}/protocol/openid-connect/certs"
}
