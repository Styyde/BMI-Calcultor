variable "project_name" {
  type        = string
  default     = "ci-cd-project"
}

variable "vpc_id" {
  type        = string
  description = "ID du VPC pour y associer le Security Group"
}

variable "vpc_cidr" {
  type        = string
  description = "Le bloc CIDR du VPC pour restreindre l'accès au port 8080"
}

variable "eks_cluster_sg_id" {
  type        = string
  description = "L'ID du Security Group du cluster EKS pour autoriser le flux JNLP"
}

variable "private_subnet_id" {
  type        = string
  description = "Le subnet privé (ex: AZ-a) où sera déployé l'instance Jenkins"
}

variable "eks_nodes_sg_id" {
  type        = string
  description = "L'ID du Security Group des Nodes EKS pour ouvrir le port JNLP 50000"
}

variable "ami_id" {
  type        = string
  description = "AMI ID (ex: Amazon Linux 2023 ou Ubuntu) pour l'instance"
  default     = "ami-045a8ab02132515f4" # Exemple d'AMI Amazon Linux 2 dans eu-west-3
}