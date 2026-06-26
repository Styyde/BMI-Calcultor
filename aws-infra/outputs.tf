output "project_region" {
  description = "La région AWS utilisée pour le déploiement"
  value       = var.aws_region
}

output "vpc_id" {
  description = "L'ID du VPC généré"
  value       = module.networking.vpc_id
}

# --- OUTPUTS EKS ---
output "eks_cluster_name" {
  description = "Le nom du cluster EKS"
  value       = module.eks.cluster_name
}

output "eks_cluster_endpoint" {
  description = "Le point d'accès (URL) de l'API Kubernetes"
  value       = module.eks.cluster_endpoint
}

output "eks_oidc_provider_arn" {
  description = "L'ARN de l'OIDC Provider (Nécessaire pour IRSA / Jenkins ServiceAccount)"
  value       = module.eks.oidc_provider_arn
}

output "commande_connexion_kubectl" {
  description = "Commande de bascule pour configurer kubectl localement"
  value       = "aws eks update-kubeconfig --region ${var.aws_region} --name ${module.eks.cluster_name}"
}

# --- OUTPUT JENKINS ---
output "jenkins_private_ip" {
  description = "L'IP privée du contrôleur Jenkins au sein du VPC"
  value       = module.jenkins_controller.jenkins_private_ip
}

# --- OUTPUTS DEPENDANCES ---
output "ecr_repository_url" {
  description = "L'URL de l'ECR pour pousser les images Docker de votre pipeline"
  value       = module.registry.repository_url
}

output "rds_endpoint" {
  description = "L'endpoint de connexion de la base de données PostgreSQL"
  value       = module.database.db_endpoint
}