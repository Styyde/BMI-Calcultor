# 1. Le Security Group pour RDS (Sécurité maximale : restriction par SG, pas par IP)
resource "aws_security_group" "rds_sg" {
  name        = "rds-private-sg"
  description = "Autorise le trafic entrant uniquement depuis EKS"
  vpc_id      = var.vpc_id

  ingress {
    description     = "PostgreSQL depuis les workers EKS"
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [var.eks_nodes_sg_id] # Seuls tes pods applicatifs sur EKS peuvent s'y connecter
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "rds-private-sg"
  }
}

# 2. Le Groupe de Sous-réseaux (Isolé dans les sous-réseaux Database dédiés)
resource "aws_db_subnet_group" "rds_subnet_group" {
  name       = "rds-private-subnet-group"
  subnet_ids = var.database_subnets # Utilisation des subnets isolés /24 créés dans le VPC

  tags = {
    Name = "RDS Private Subnet Group"
  }
}

# 3. L'instance RDS PostgreSQL Multi-AZ
resource "aws_db_instance" "postgres" {
  identifier           = "ci-cd-database"
  engine               = "postgres"
  engine_version       = "17.5"
  instance_class       = "db.t3.micro"
  allocated_storage    = 20
  storage_type         = "gp3" # Standard moderne plus performant que le gp2 par défaut
  
  # HAUTE DISPONIBILITÉ : Déploie un master dans l'AZ-a et un standby synchrone dans l'AZ-b
  multi_az             = true 

  db_name              = "applicationdb"
  username             = "dbadmin"
  password             = var.db_password
  
  db_subnet_group_name   = aws_db_subnet_group.rds_subnet_group.name
  vpc_security_group_ids = [aws_security_group.rds_sg.id]

  skip_final_snapshot = true 
  publicly_accessible = false # Aucune IP publique, totalement invisible d'Internet
}