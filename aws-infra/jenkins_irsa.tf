# . Définition de la politique IAM (Exemple : permissions requises pour Jenkins)
resource "aws_iam_policy" "jenkins_irsa_policy" {
  name        = "${var.project_name}-jenkins-irsa-policy"
  description = "Permissions pour les agents/controleur Jenkins sur EKS"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "ecr:GetAuthorizationToken",
          "ecr:BatchCheckLayerAvailability",
          "ecr:GetDownloadUrlForLayer",
          "ecr:BatchGetImage",
          "ecr:InitiateLayerUpload",
          "ecr:UploadLayerPart",
          "ecr:CompleteLayerUpload",
          "ecr:PutImage",
        ]
        Resource = "*"
      },
      {
        Effect = "Allow"
        Action = [
          "ssm:GetParameter",
          "ssm:GetParameters",
        ]
        Resource = "*"
      },
      # Permissions pour interagir avec EKS
      {
        Effect = "Allow"
        Action = [
          "eks:DescribeCluster",
          "eks:ListClusters"
        ]
        Resource = "*"
      }
    ]
  })
}

# . Utilisation du module officiel IRSA d'AWS pour lier le rôle à l'OIDC d'EKS
module "jenkins_pod_irsa" {
  source  = "terraform-aws-modules/iam/aws//modules/iam-role-for-service-accounts-eks"
  version = "~> 5.0"

  role_name = "${var.project_name}-jenkins-pod-role"

  oidc_providers = {
    main = {
      provider_arn               = module.eks.oidc_provider_arn
      namespace_service_accounts = ["kube-system:jenkins-sa"] # IMPORTANT: Remplacez par votre namespace et nom de SA exacts
    }
  }

  role_policy_arns = {
    additional = aws_iam_policy.jenkins_irsa_policy.arn
  }
}