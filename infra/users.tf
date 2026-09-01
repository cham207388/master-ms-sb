resource "keycloak_user" "happy_camper" {
  realm_id = keycloak_realm.main.id
  username = "happy@example.com"
  enabled  = true

  email            = "happy@example.com"
  email_verified   = true
  first_name       = "Happy"
  last_name        = "Camper"
  required_actions = ["CONFIGURE_TOTP"]

  initial_password {
    value     = var.user_password
    temporary = false
  }

  # Keycloak removes CONFIGURE_TOTP after enrollment; do not add it back.
  lifecycle {
    ignore_changes = [required_actions]
  }

  depends_on = [keycloak_required_action.configure_totp]
}

resource "keycloak_user" "john_doe" {
  realm_id = keycloak_realm.main.id
  username = "johndoe@example.com"
  enabled  = true

  email            = "johndoe@example.com"
  email_verified   = true
  first_name       = "John"
  last_name        = "Doe"
  required_actions = ["CONFIGURE_TOTP"]

  initial_password {
    value     = var.user_password
    temporary = false
  }

  # Keycloak removes CONFIGURE_TOTP after enrollment; do not add it back.
  lifecycle {
    ignore_changes = [required_actions]
  }

  depends_on = [keycloak_required_action.configure_totp]
}

resource "keycloak_user_roles" "happy_camper" {
  realm_id = keycloak_realm.main.id
  user_id  = keycloak_user.happy_camper.id

  role_ids = [
    keycloak_role.service_account["ACCOUNTS"].id,
    keycloak_role.service_account["CARDS"].id,
    keycloak_role.service_account["LOANS"].id,
    data.keycloak_role.account_view_profile.id,
  ]
}

resource "keycloak_user_roles" "john_doe" {
  realm_id = keycloak_realm.main.id
  user_id  = keycloak_user.john_doe.id

  role_ids = [
    keycloak_role.service_account["ACCOUNTS"].id,
    keycloak_role.service_account["CARDS"].id,
    keycloak_role.service_account["LOANS"].id,
    data.keycloak_role.account_view_profile.id,
  ]
}
