output "cluster_name" {
  description = "Le nom du cluster EKS"
  value       = module.eks.cluster_name
}

output "cluster_endpoint" {
  description = "L'URL de l'API Kubernetes (Endpoint de contrôle)"
  value       = module.eks.cluster_endpoint
}

output "cluster_certificate_authority_data" {
  description = "Les données du certificat du cluster (Nécessaire pour l'authentification Helm/Kubernetes)"
  value       = module.eks.cluster_certificate_authority_data
}

output "oidc_provider_arn" {
  description = "L'ARN de l'OIDC Provider pour associer des rôles IAM aux ServiceAccounts (IRSA)"
  value       = module.eks.oidc_provider_arn
}

output "oidc_provider" {
  description = "L'URL de l'OIDC Provider du cluster"
  value       = module.eks.oidc_provider
}
output "cluster_primary_security_group_id" {
  description = "Le Security Group principal genere automatiquement par EKS pour le Cluster Control Plane"
  value       = module.eks.cluster_primary_security_group_id
}

output "node_security_group_id" {
  description = "Le Security Group des noeuds (workers) EKS"
  value       = module.eks.node_security_group_id
}
output "eks_cluster_arn" {
  description = "ARN du cluster EKS (utile pour les politiques IAM)"
  value       = module.eks.cluster_arn
}