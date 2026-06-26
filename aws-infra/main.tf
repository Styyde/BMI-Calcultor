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
# 3. Contrôleur Jenkins (EC2 Privée Dédiée)
# ==========================================
module "jenkins_controller" {
  source            = "./modules/jenkins_controller"
  vpc_id            = module.networking.vpc_id
  vpc_cidr          = var.vpc_cidr
  private_subnet_id = module.networking.private_subnets[0] # Déploiement dans l'AZ-a privée
  eks_nodes_sg_id   = module.eks.node_security_group_id    # Restriction JNLP au SG EKS
  eks_cluster_sg_id = module.eks.cluster_primary_security_group_id
}

# ==========================================
# 4. Registre Docker Privé (AWS ECR)
# ==========================================
module "registry" {
  source = "./modules/registry"
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
resource "aws_security_group_rule" "jenkins_to_eks_api" {
  type                     = "ingress"
  from_port                = 443
  to_port                  = 443
  protocol                 = "tcp"
  security_group_id        = module.eks.cluster_primary_security_group_id # Le SG de l'API EKS
  source_security_group_id = module.jenkins_controller.jenkins_sg_id     # Le SG de Jenkins
  description              = "Autoriser Jenkins a communiquer avec lAPI Kubernetes"
}