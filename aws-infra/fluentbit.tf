# ==========================================
# IRSA pour FluentBit (Logging EKS)
# ==========================================

# 1. Politique IAM pour CloudWatch Logs
resource "aws_iam_policy" "fluentbit_policy" {
  name        = "${var.project_name}-fluentbit-policy"
  description = "Permet à FluentBit d'écrire dans CloudWatch Logs"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:PutLogEvents",
          "logs:DescribeLogStreams"
        ]
        Resource = "*"
      }
    ]
  })
}

# 2. Rôle IAM avec OIDC Provider (IRSA)
module "fluentbit_irsa" {
  source  = "terraform-aws-modules/iam/aws//modules/iam-role-for-service-accounts-eks"
  version = "~> 5.0"

  role_name = "${var.project_name}-fluentbit-role"

  oidc_providers = {
    main = {
      provider_arn               = module.eks.oidc_provider_arn
      namespace_service_accounts = ["kube-system:fluent-bit"]  # ← ServiceAccount créé par FluentBit
    }
  }

  role_policy_arns = {
    fluentbit = aws_iam_policy.fluentbit_policy.arn
  }
}
resource "aws_ssm_parameter" "fluentbit_role_arn" {
  name  = "/cicd/fluentbit/role_arn"
  type  = "String"
  value = module.fluentbit_irsa.iam_role_arn   # Récupéré du module IRSA
}