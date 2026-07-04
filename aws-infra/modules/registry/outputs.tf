# ==========================================
# OUTPUTS POUR LE BACKEND
# ==========================================
output "backend_repository_url" {
  description = "URL du repository ECR pour le backend"
  value       = aws_ecr_repository.backend_repo.repository_url
}

output "backend_repository_name" {
  description = "Nom du repository ECR pour le backend"
  value       = aws_ecr_repository.backend_repo.name
}

output "backend_repository_arn" {
  description = "ARN du repository ECR pour le backend"
  value       = aws_ecr_repository.backend_repo.arn
}

# ==========================================
# OUTPUTS POUR LE FRONTEND
# ==========================================
output "frontend_repository_url" {
  description = "URL du repository ECR pour le frontend"
  value       = aws_ecr_repository.frontend_repo.repository_url
}

output "frontend_repository_name" {
  description = "Nom du repository ECR pour le frontend"
  value       = aws_ecr_repository.frontend_repo.name
}

output "frontend_repository_arn" {
  description = "ARN du repository ECR pour le frontend"
  value       = aws_ecr_repository.frontend_repo.arn
}