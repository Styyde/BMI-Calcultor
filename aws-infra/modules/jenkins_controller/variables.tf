variable "project_name" {
  type    = string
  default = "ci-cd-project"
}

variable "vpc_id" {
  type        = string
  description = "ID du VPC pour y associer le Security Group"
}

variable "vpc_cidr" {
  type        = string
  description = "Le bloc CIDR du VPC pour restreindre l'accès au port 8080"
}

variable "private_subnet_ids" {
  type        = list(string)
  description = "Liste des sous-réseaux privés (Multi-AZ) pour déployer l'ASG Jenkins"
}
variable "jenkins_certificate_arn" {
  description = "ARN du certificat ACM pour jenkins.mondomaine.com"
  type        = string
}
variable "ami_id" {
  type        = string
  description = "AMI ID (Amazon Linux ou RHEL compatible yum) pour l'instance"
  default     = "ami-0f54908a1f0d2a5b9" 
}
variable "public_subnet_ids" {
  description = "Liste des IDs des sous-réseaux publics pour l'ALB"
  type        = list(string)
}