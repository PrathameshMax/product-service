data "aws_caller_identity" "current" {}

data "aws_partition" "current" {}

data "aws_availability_zones" "available" {
  state = "available"

  filter {
    name   = "opt-in-status"
    values = ["opt-in-not-required"]
  }
}

# Reads outputs of the permanent foundation stack (ECR, DNS zone, cert, SNS)
data "terraform_remote_state" "foundation" {
  backend = "s3"

  config = {
    bucket = "${var.project}-tfstate-${data.aws_caller_identity.current.account_id}"
    key    = "foundation/terraform.tfstate"
    region = var.region
  }
}

locals {
  name       = "${var.project}-${var.environment}"
  account_id = data.aws_caller_identity.current.account_id
  partition  = data.aws_partition.current.partition
  azs        = slice(data.aws_availability_zones.available.names, 0, 2)
  foundation = data.terraform_remote_state.foundation.outputs

  container_name = var.project
  image          = "${local.foundation.ecr_repository_url}:${var.image_tag}"
  api_fqdn       = local.foundation.api_fqdn
}
