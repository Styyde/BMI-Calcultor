variable "repo_name" {
  description = "Nom du repository ECR"
  type        = string
  default     = "ci-cd-backend-repo"
}

resource "aws_ecr_repository" "app_repo" {
  name                 = var.repo_name
  image_tag_mutability = "MUTABLE" # Permet d'écraser le tag 'latest' pendant le développement

  # Touche de sécurité très appréciée en entretien :
  image_scanning_configuration {
    scan_on_push = true
  }

  tags = {
    Environment = "dev"
    Project     = "ci-cd-project"
  }
}

output "repository_url" {
  value = aws_ecr_repository.app_repo.repository_url
}