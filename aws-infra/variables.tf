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
variable "ami_id" {
  type        = string
  description = "AMI ID pour l'instance Jenkins"
  default     = "ami-0f54908a1f0d2a5b9"
}
variable "route53_zone_id" {
  description = "L'ID de la zone Route53 pour la validation des certificats"
  type        = string
}
variable "project_name" {
  description = "Nom du projet pour le nommage des ressources"
  type        = string
  default     = "ci-cd-project"  
}

