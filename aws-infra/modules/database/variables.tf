variable "vpc_id" {
  type        = string
  description = "L'ID du VPC"
}

variable "database_subnets" {
  type        = list(string)
  description = "La liste des sous-réseaux isolés réservés à la base de données"
}

variable "eks_nodes_sg_id" {
  type        = string
  description = "L'ID du Security Group des workers EKS pour restreindre l'accès à PostgreSQL"
}

variable "db_password" { 
  type      = string 
  sensitive = true # Masque le mot de passe dans les logs d'exécution Terraform
}