variable "region" {
  description = "Primary AWS region"
  type        = string
  default     = "ap-south-1"
}

variable "project" {
  description = "Project name, used as a prefix for every resource"
  type        = string
  default     = "product-service"
}

variable "environment" {
  description = "Environment name"
  type        = string
  default     = "dev"
}

variable "github_owner" {
  description = "GitHub user or organisation that owns the repository (case-sensitive)"
  type        = string
}

variable "github_repo" {
  description = "GitHub repository name"
  type        = string
}

variable "github_owner_id" {
  description = "Immutable numeric ID of the GitHub owner (appears in the OIDC sub claim as owner@id)"
  type        = string
}

variable "github_repo_id" {
  description = "Immutable numeric ID of the GitHub repository (appears in the OIDC sub claim as repo@id)"
  type        = string
}

variable "dns_zone_name" {
  description = "Subdomain delegated from the registrar to Route 53"
  type        = string
}

variable "api_subdomain" {
  description = "Host label for the API inside dns_zone_name"
  type        = string
  default     = "api"
}

variable "alert_email" {
  description = "Email for budget and CloudWatch alarm notifications"
  type        = string
}

variable "monthly_budget_limit" {
  description = "Monthly cost budget ceiling"
  type        = number
  default     = 18
}

variable "budget_currency" {
  description = "Budget currency unit. USD for standard accounts; switch to INR only if AWS rejects USD for this account"
  type        = string
  default     = "USD"
}

variable "ecr_keep_image_count" {
  description = "Number of tagged images ECR keeps before expiring older ones"
  type        = number
  default     = 10
}
