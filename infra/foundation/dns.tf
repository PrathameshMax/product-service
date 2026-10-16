# Delegated subdomain zone. After the first targeted apply, copy the four
# name_servers output values into Namecheap as NS records for host "dev".
resource "aws_route53_zone" "dev" {
  name          = var.dns_zone_name
  comment       = "Delegated from Namecheap for ${var.project}"
  force_destroy = false
}

resource "aws_acm_certificate" "api" {
  domain_name               = local.api_fqdn
  subject_alternative_names = ["*.${var.dns_zone_name}"]
  validation_method         = "DNS"
  key_algorithm             = "RSA_2048"

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_route53_record" "acm_validation" {
  for_each = {
    for dvo in aws_acm_certificate.api.domain_validation_options : dvo.domain_name => {
      name   = dvo.resource_record_name
      type   = dvo.resource_record_type
      record = dvo.resource_record_value
    }
  }

  zone_id         = aws_route53_zone.dev.zone_id
  name            = each.value.name
  type            = each.value.type
  records         = [each.value.record]
  ttl             = 300
  allow_overwrite = true
}

resource "aws_acm_certificate_validation" "api" {
  certificate_arn         = aws_acm_certificate.api.arn
  validation_record_fqdns = [for r in aws_route53_record.acm_validation : r.fqdn]

  timeouts {
    create = "45m"
  }
}
