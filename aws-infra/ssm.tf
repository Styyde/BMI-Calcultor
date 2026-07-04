resource "aws_ssm_parameter" "ecr_backend_url" {
  name  = "/cicd/ecr/backend_url"
  type  = "String"
  value = module.registry.backend_repository_url
}

resource "aws_ssm_parameter" "ecr_frontend_url" {
  name  = "/cicd/ecr/frontend_url"
  type  = "String"
  value = module.registry.frontend_repository_url
}
resource "aws_ssm_parameter" "rds_password" {
  name  = "/cicd/rds/password"
  type  = "SecureString"
  value = var.db_password
}
resource "aws_ssm_parameter" "jenkins_cert_arn" {
  name  = "/cicd/jenkins/cert_arn"
  type  = "String"
  value = aws_acm_certificate.jenkins_cert.arn
}

resource "aws_ssm_parameter" "app_cert_arn" {
  name  = "/cicd/app/cert_arn"
  type  = "String"
  value = aws_acm_certificate.app_cert.arn
}
resource "aws_ssm_parameter" "rds_endpoint" {
  name  = "/cicd/rds/endpoint"
  type  = "String"
  value = module.database.db_endpoint # Correspond à l'output de votre module database principal
}