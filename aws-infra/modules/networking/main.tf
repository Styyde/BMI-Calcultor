module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "~> 5.0"

  name = "ci-cd-project-vpc"
  cidr = var.vpc_cidr
  # Répartition stricte sur 2 zones de disponibilité
  azs = ["${var.aws_region}a", "${var.aws_region}b"]

  # 1. Sous-réseaux Publics (~32 IPs par subnet)
  public_subnets = ["10.0.0.0/27", "10.0.0.32/27"]

  # 2. Sous-réseaux Privés pour EKS (1024 IPs par subnet -> Élasticité des agents)
  private_subnets = ["10.0.4.0/22", "10.0.8.0/22"]

  # 3. Sous-réseaux Privés pour la Database (256 IPs par subnet)
  database_subnets = ["10.0.12.0/24", "10.0.13.0/24"]

  # Configuration de la Haute Disponibilité Réseau (1 NAT par AZ)
  enable_nat_gateway     = true
  single_nat_gateway     = false
  one_nat_gateway_per_az = true

  enable_vpn_gateway = false

  # Configuration automatique des tables de routage et groupes de sous-réseaux pour RDS
  # Note : Le module associe automatiquement les sous-réseaux à cette table,
  # l'argument "enable_database_subnet_route_table_association" a été supprimé d'ici.
  create_database_subnet_route_table = true
  create_database_subnet_group       = true

  # Tags requis pour EKS et l'AWS Load Balancer Controller
  public_subnet_tags = {
    "kubernetes.io/role/elb"                  = "1"
    "kubernetes.io/cluster/ci-cd-project-eks" = "shared"
  }

  private_subnet_tags = {
    "kubernetes.io/role/internal-elb"         = "1"
    "kubernetes.io/cluster/ci-cd-project-eks" = "shared"
  }

  tags = {
    Environment = "dev"
    Project     = "ci-cd-project"
  }
}