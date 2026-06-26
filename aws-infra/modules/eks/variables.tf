variable "cluster_name" {
  description = "Le nom unique du cluster EKS"
  type        = string
  default     = "ci-cd-project-eks"
}

variable "cluster_version" {
  description = "La version de Kubernetes (Choix d'une version stable de production)"
  type        = string
  default     = "1.30"
}

variable "vpc_id" {
  description = "L'ID du VPC cible pour le déploiement du cluster"
  type        = string
}

variable "private_subnets" {
  description = "La liste des sous-réseaux privés réservés aux nœuds de calcul EKS"
  type        = list(string)
}