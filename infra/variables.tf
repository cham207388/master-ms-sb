variable "keycloak_url" {
  description = "Base URL of the Keycloak server"
  type        = string
}

variable "keycloak_admin_username" {
  description = "Keycloak administrator username"
  type        = string
  default     = "admin"
}

variable "keycloak_admin_password" {
  description = "Keycloak administrator password"
  type        = string
  sensitive   = true
}

variable "client_type_client_id" {
  description = "Client ID of the Secure DBank API"
  type        = string
}

variable "client_secret" {
  description = "Client secret of the Secure DBank API"
  type        = string
  sensitive   = true
}

variable "client_secret_version" {
  description = "Increment this number whenever client_secret is rotated"
  type        = number
  default     = 1
}

variable "realm" {
  description = "Realm name"
  type        = string
}

variable "auth_code_client_id" {
  description = "Client ID for the authorization-code OAuth client"
  type        = string
  default     = "securedbank-ac"
}

variable "auth_code_client_secret" {
  description = "Client secret for the authorization-code OAuth client"
  type        = string
  sensitive   = true
}

variable "auth_code_client_secret_version" {
  description = "Increment this number whenever auth_code_client_secret is rotated"
  type        = number
  default     = 1
}

variable "pkce_client_id" {
  description = "Client ID for the public authorization-code OAuth client with PKCE S256"
  type        = string
  default     = "securedbankpublicclient"
}

variable "pkce_ui_client_id" {
  description = "Client ID for the Angular UI public authorization-code OAuth client with PKCE S256"
  type        = string
  default     = "securedbankpublicclientui"
}

variable "user_password" {
  description = "Permanent initial password for Terraform-managed realm users"
  type        = string
  sensitive   = true
}