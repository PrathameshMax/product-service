output "account_id" {
  value = local.account_id
}

output "region" {
  value = var.region
}

output "state_bucket" {
  value = local.state_bucket
}

output "ecr_repository_url" {
  value = aws_ecr_repository.app.repository_url
}

output "ecr_repository_name" {
  value = aws_ecr_repository.app.name
}

output "dns_zone_id" {
  value = aws_route53_zone.dev.zone_id
}

output "dns_zone_name" {
  value = aws_route53_zone.dev.name
}

output "dns_name_servers" {
  description = "Add each of these as an NS record for host 'dev' in Namecheap Advanced DNS"
  value       = aws_route53_zone.dev.name_servers
}

output "api_fqdn" {
  value = local.api_fqdn
}

output "acm_certificate_arn" {
  value = aws_acm_certificate_validation.api.certificate_arn
}

output "alerts_topic_arn" {
  value = aws_sns_topic.alerts.arn
}

output "gha_plan_role_arn" {
  value = aws_iam_role.gha_plan.arn
}

output "gha_deploy_role_arn" {
  value = aws_iam_role.gha_deploy.arn
}

output "github_variables" {
  description = "Values to set as GitHub repository variables (Settings > Secrets and variables > Actions > Variables)"
  value = {
    AWS_REGION          = var.region
    AWS_ACCOUNT_ID      = local.account_id
    AWS_PLAN_ROLE_ARN   = aws_iam_role.gha_plan.arn
    AWS_DEPLOY_ROLE_ARN = aws_iam_role.gha_deploy.arn
    ECR_REPOSITORY      = aws_ecr_repository.app.name
    TF_STATE_BUCKET     = local.state_bucket
  }
}
