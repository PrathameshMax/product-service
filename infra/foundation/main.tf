data "aws_caller_identity" "current" {}

data "aws_partition" "current" {}

locals {
  account_id      = data.aws_caller_identity.current.account_id
  partition       = data.aws_partition.current.partition
  state_bucket    = "${var.project}-tfstate-${local.account_id}"
  repo_full_name  = "${var.github_owner}/${var.github_repo}"
  api_fqdn        = "${var.api_subdomain}.${var.dns_zone_name}"
  iam_name_prefix = var.project
}
