# ==========================================
# 1. Fondations Réseau (VPC Multi-AZ & VLSM)
# ==========================================
module "networking" {
  source   = "./modules/networking"
  vpc_cidr = var.vpc_cidr
}

# ==========================================
# 2. Cluster EKS (Orchestration Élastique)
# ==========================================
module "eks" {
  source          = "./modules/eks"
  vpc_id          = module.networking.vpc_id
  private_subnets = module.networking.private_subnets
  cluster_name    = "ci-cd-project-eks"
}

# ==========================================
# 3. Contrôleur Jenkins (EC2 Privée Dédiée) + HA + multiaz
# ==========================================
module "jenkins_controller" {
  source             = "./modules/jenkins_controller"
  vpc_id             = module.networking.vpc_id
  vpc_cidr           = var.vpc_cidr
  private_subnet_ids = module.networking.private_subnets  
  public_subnet_ids = module.networking.public_subnets
  ami_id             = var.ami_id 
  jenkins_certificate_arn = aws_acm_certificate.jenkins_cert.arn                          
}
resource "aws_route53_record" "jenkins" {
  zone_id = var.route53_zone_id
  name    = "jenkins.kolynois.com"   # adaptez à votre domaine
  type    = "A"

  alias {
    name                   = module.jenkins_controller.alb_dns_name
    zone_id                = module.jenkins_controller.alb_zone_id
    evaluate_target_health = true
  }

  depends_on = [module.jenkins_controller]
}

# ==========================================
# 4. Registre Docker Privé (AWS ECR)
# ==========================================
module "registry" {
  source = "./modules/registry"
  
  project_name = var.project_name
}

# ==========================================
# 5. Base de Données (RDS PostgreSQL Multi-AZ)
# ==========================================
module "database" {
  source           = "./modules/database"
  vpc_id           = module.networking.vpc_id
  database_subnets = module.networking.database_subnets # Isolation subnets /24 dédiés
  eks_nodes_sg_id  = module.eks.node_security_group_id  # Filtrage chirurgical port 5432
  db_password      = var.db_password
}
#Module qui contient les endpoints utilisée par notre vpc
module "endpoints" {
  source                     = "./modules/endpoints"
  vpc_id                     = module.networking.vpc_id
  route_table_ids            = module.networking.private_route_table_ids
  private_subnet_ids         = module.networking.private_subnets
  endpoint_security_group_id = module.networking.endpoints_sg_id 
}
resource "aws_security_group_rule" "jenkins_to_eks_api" {
  type                     = "ingress"
  from_port                = 443
  to_port                  = 443
  protocol                 = "tcp"
  security_group_id        = module.eks.cluster_primary_security_group_id
  source_security_group_id = module.jenkins_controller.jenkins_sg_id  # ← Doit exister
  description              = "Autoriser Jenkins a communiquer avec lAPI Kubernetes"
  
}
resource "aws_eks_access_entry" "jenkins_controller" {
  cluster_name  = module.eks.cluster_name
  principal_arn = module.jenkins_controller.iam_role_arn
}
resource "aws_cloudwatch_dashboard" "main" {
  dashboard_name = "ci-cd-project-overview"

  dashboard_body = jsonencode({
    widgets = [
      {
        type = "metric"
        properties = {
          metrics = [
            ["AWS/RDS", "CPUUtilization", "DBInstanceIdentifier", "ci-cd-database"],
            ["AWS/ApplicationELB", "TargetResponseTime", "LoadBalancer", "jenkins-alb"],
            ["AWS/ECS", "CPUUtilization", "ClusterName", "ci-cd-project-eks"]
          ]
          period = 300
          stat   = "Average"
          region = "eu-west-3"
          title  = "Métriques principales du projet"
        }
      }
    ]
  })
}
resource "aws_eks_access_policy_association" "jenkins_controller_admin" {
  cluster_name  = module.eks.cluster_name
  principal_arn = aws_eks_access_entry.jenkins_controller.principal_arn
  policy_arn    = "arn:aws:eks::aws:cluster-access-policy/AmazonEKSClusterAdminPolicy"
  access_scope {
    type = "cluster"
  }
}
resource "aws_eks_access_entry" "jenkins_pod" {
  cluster_name  = module.eks.cluster_name
  principal_arn = module.jenkins_pod_irsa.iam_role_arn
  type          = "STANDARD"
}
resource "aws_eks_access_policy_association" "jenkins_pod_admin" {
  cluster_name  = module.eks.cluster_name
  policy_arn    = "arn:aws:eks::aws:cluster-access-policy/AmazonEKSClusterAdminPolicy"
  principal_arn = module.jenkins_pod_irsa.iam_role_arn
  access_scope {
    type = "cluster"
  }
}