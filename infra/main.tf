resource "keycloak_realm" "main" {
  realm        = var.realm
  display_name = var.realm
  enabled      = true

  # Suitable for the local HTTP Keycloak environment.
  # Use "external" or "all" when Keycloak is deployed with HTTPS.
  ssl_required = "none"

  login_with_email_allowed = true
  duplicate_emails_allowed = false
  reset_password_allowed   = true

  otp_policy {
    type              = "totp"
    algorithm         = "HmacSHA1"
    digits            = 6
    initial_counter   = 0
    look_ahead_window = 1
    period            = 15
    code_reusable     = false
  }

  # Prevent accidental realm deletion by terraform destroy.
  terraform_deletion_protection = false
}

# Require newly provisioned users to enroll a TOTP authenticator.
resource "keycloak_required_action" "configure_totp" {
  realm_id       = keycloak_realm.main.id
  alias          = "CONFIGURE_TOTP"
  name           = "Configure OTP"
  enabled        = true
  default_action = true
}

locals {
  # Shared realm roles used by the M2M service account and human users.
  realm_roles = toset([
    "ACCOUNTS",
    "CARDS",
    "LOANS",
  ])
}

# Create ACCOUNTS, CARDS, and LOANS as realm roles.
resource "keycloak_role" "service_account" {
  for_each = local.realm_roles

  realm_id    = keycloak_realm.main.id
  name        = each.value
  description = "Realm ${each.value} role"
}
