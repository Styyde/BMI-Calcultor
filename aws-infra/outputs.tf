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

output "ecr_backend_repo_url" {
  description = "URL du repository ECR pour le backend"
  value       = module.registry.backend_repository_url
}

output "ecr_frontend_repo_url" {
  description = "URL du repository ECR pour le frontend"
  value       = module.registry.frontend_repository_url
}

output "ecr_backend_repo_name" {
  description = "Nom du repository ECR pour le backend"
  value       = module.registry.backend_repository_name
}

output "ecr_frontend_repo_name" {
  description = "Nom du repository ECR pour le frontend"
  value       = module.registry.frontend_repository_name
}


output "rds_endpoint" {
  description = "L'endpoint de connexion de la base de données PostgreSQL"
  value       = module.database.db_endpoint
}
output "jenkins_asg_name" {
  description = "Le nom de l'Auto Scaling Group Jenkins"
  value       = module.jenkins_controller.asg_name
}
output "jenkins_iam_role_arn" {
  description = "L'ARN du rôle IAM Jenkins"
  value       = module.jenkins_controller.iam_role_arn
}
