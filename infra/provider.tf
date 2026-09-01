terraform {
  required_version = "~> 1.14"

  required_providers {
    keycloak = {
      source  = "keycloak/keycloak"
      version = "= 5.8.0"
    }
  }
}

provider "keycloak" {
  url = var.keycloak_url

  # Terraform authenticates against the master realm through admin-cli.
  realm     = "master"
  client_id = "admin-cli"
  username  = var.keycloak_admin_username
  password  = var.keycloak_admin_password
}