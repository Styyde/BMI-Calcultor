module "eks" {
  source  = "terraform-aws-modules/eks/aws"
  version = "~> 20.0"

  cluster_name    = var.cluster_name
  cluster_version = var.cluster_version

  # Permet au PC ainsi que jenkins controller de parler à l'API Kubernetes (kubectl)
  cluster_endpoint_public_access = true
  cluster_endpoint_private_access = true
  # On déploie le cluster dans le VPC et les instances dans les subnets privés
  vpc_id     = var.vpc_id
  subnet_ids = var.private_subnets

  # Activation de l'OIDC (Indispensable pour l'ALB Controller et Jenkins)
  enable_irsa = true

  # Nouveau standard de sécurité EKS v20+ 
  enable_cluster_creator_admin_permissions = true

  # Configuration du Managed Node Group optimisé pour la CI/CD
  eks_managed_node_groups = {
    jenkins_workers = {
      ami_type       = "AL2023_x86_64_STANDARD"
      
      # Diversification des instances pour éviter le manque de capacité AWS (Spot ou On-Demand)
      instance_types = ["t3.medium", "t3.large"]
      capacity_type = "SPOT"
      # Élasticté poussée à 10 nœuds pour absorber les vagues de builds des agents Jenkins
      min_size     = 1
      max_size     = 5
      desired_size = 2

      # Extension du stockage pour le cache des builds Docker et dépendances (Maven/NPM)
      disk_size = 15

      # Labels utiles pour isoler vos agents Jenkins via nodeSelector si nécessaire
      labels = {
        Role = "jenkins-agents-pool"
      }

      tags = {
        "k8s.io/cluster-autoscaler/enabled" = "true"
        "k8s.io/cluster-autoscaler/${var.cluster_name}" = "owned"
      }
    }
  }

  tags = {
    Environment = "dev"
    Project     = "ci-cd-project"
  }
}