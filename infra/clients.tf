data "keycloak_openid_client" "account" {
  realm_id  = keycloak_realm.main.id
  client_id = "account"
}

data "keycloak_role" "account_view_profile" {
  realm_id  = keycloak_realm.main.id
  client_id = data.keycloak_openid_client.account.id
  name      = "view-profile"
}

# Assign ACCOUNTS, CARDS, and LOANS to the securedbank-cc service-account user.
resource "keycloak_openid_client_service_account_realm_role" "service_account" {
  for_each = keycloak_role.service_account

  realm_id                = keycloak_realm.main.id
  service_account_user_id = keycloak_openid_client.client_type.service_account_user_id
  role                    = each.value.name
}
