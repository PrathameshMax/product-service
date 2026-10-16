terraform {
  required_version = ">= 1.10.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
  }

  backend "s3" {}
}

provider "aws" {
  region = var.region

  default_tags {
    tags = {
      Project     = var.project
      Stack       = "foundation"
      Environment = var.environment
      ManagedBy   = "terraform"
      Repository  = "${var.github_owner}/${var.github_repo}"
    }
  }
}
