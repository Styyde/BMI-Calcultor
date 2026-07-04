resource "aws_acm_certificate" "jenkins_cert" {
  domain_name       = "jenkins.kolynois.com"
  validation_method = "DNS"
}

resource "aws_route53_record" "jenkins_cert_validation" {
  for_each = {
    for dvo in aws_acm_certificate.jenkins_cert.domain_validation_options : dvo.domain_name => {
      name   = dvo.resource_record_name
      record = dvo.resource_record_value
      type   = dvo.resource_record_type
    }
  }

  allow_overwrite = true
  name            = each.value.name
  records         = [each.value.record]
  ttl             = 60
  type            = each.value.type
  zone_id         = var.route53_zone_id  # Utilisez une variable pour l'ID de zone
}

resource "aws_acm_certificate_validation" "jenkins_cert" {
  certificate_arn         = aws_acm_certificate.jenkins_cert.arn
  validation_record_fqdns = [for record in aws_route53_record.jenkins_cert_validation : record.fqdn]
}

# ==========================================
# Certificat pour l'application (app.mondomaine.com)
# ==========================================
resource "aws_acm_certificate" "app_cert" {
  domain_name       = "app.kolynois.com"
  validation_method = "DNS"
}

resource "aws_route53_record" "app_cert_validation" {
  for_each = {
    for dvo in aws_acm_certificate.app_cert.domain_validation_options : dvo.domain_name => {
      name   = dvo.resource_record_name
      record = dvo.resource_record_value
      type   = dvo.resource_record_type
    }
  }

  allow_overwrite = true
  name            = each.value.name
  records         = [each.value.record]
  ttl             = 60
  type            = each.value.type
  zone_id         = var.route53_zone_id
}

resource "aws_acm_certificate_validation" "app_cert" {
  certificate_arn         = aws_acm_certificate.app_cert.arn
  validation_record_fqdns = [for record in aws_route53_record.app_cert_validation : record.fqdn]
}