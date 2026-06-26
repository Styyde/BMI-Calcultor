variable "aws_region" {
  description = "La région AWS où déployer l'infrastructure"
  type        = string
  default     = "eu-west-3" # Paris
}

variable "vpc_cidr" {
  description = "Le bloc CIDR principal pour l'ensemble du VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "db_password" {
  description = "Mot de passe maître pour l'instance RDS PostgreSQL"
  type        = string
  sensitive   = true # Masquage automatique dans les logs Terraform CLI
}