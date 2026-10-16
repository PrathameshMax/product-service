variable "region" {
  description = "AWS region"
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

variable "image_tag" {
  description = "ECR image tag (git SHA) to deploy. Resolved by the lab-up workflow"
  type        = string
  default     = "unset"
}

variable "vpc_cidr" {
  description = "CIDR block for the lab VPC"
  type        = string
  default     = "10.20.0.0/16"
}

variable "container_port" {
  description = "Port the Spring Boot container listens on"
  type        = number
  default     = 8080
}

variable "task_cpu" {
  description = "Fargate task CPU units (256 = 0.25 vCPU)"
  type        = number
  default     = 512
}

variable "task_memory" {
  description = "Fargate task memory in MiB"
  type        = number
  default     = 1024
}

variable "desired_count" {
  description = "Initial number of running tasks"
  type        = number
  default     = 1
}

variable "min_capacity" {
  description = "Auto scaling minimum task count"
  type        = number
  default     = 1
}

variable "max_capacity" {
  description = "Auto scaling maximum task count"
  type        = number
  default     = 3
}

variable "use_fargate_spot" {
  description = "Run tasks on Fargate Spot (about 70% cheaper, can be interrupted with 2 minutes notice)"
  type        = bool
  default     = true
}

variable "enable_container_insights" {
  description = "Enable ECS Container Insights (adds CloudWatch custom metric charges)"
  type        = bool
  default     = false
}

variable "log_retention_days" {
  description = "CloudWatch Logs retention for application logs"
  type        = number
  default     = 7
}

variable "cpu_target_percent" {
  description = "Target average CPU for target-tracking auto scaling"
  type        = number
  default     = 60
}

variable "requests_per_target" {
  description = "Target ALB requests per task per minute for request-count auto scaling"
  type        = number
  default     = 300
}
