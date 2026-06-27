# Security Group pour le Jenkins Controller
resource "aws_security_group" "jenkins" {
  name        = "${var.project_name}-jenkins-sg"
  description = "Controle des flux pour le controlleur Jenkins"
  vpc_id      = var.vpc_id

  # Flux Entrant : Port 8080 pour l'interface Web (accessible uniquement depuis le VPC / ALB)
  ingress {
    description = "Acces a l interface web Jenkins depuis le VPC"
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = [var.vpc_cidr] 
  }

  # Flux Entrant : Port 50000 requis pour la communication JNLP des Agents EKS
  ingress {
    description     = "Flux JNLP provenant des agents EKS"
    from_port       = 50000
    to_port         = 50000
    protocol        = "tcp"
    security_groups = [var.eks_nodes_sg_id] # Autorise uniquement le SG des workers EKS
  }

  egress {
    description     = "Autoriser Jenkins a contacter lAPI EKS sur le port 443"
    from_port       = 443
    to_port         = 443
    protocol        = "tcp"
    security_groups = [var.eks_cluster_sg_id]
  }
  egress {
    description = "Autoriser le trafic sortant vers Internet pour les mises a jour, GitHub et ECR"
    from_port   = 0
    to_port     = 0
    protocol    = "-1" # Équivalent à "tous les protocoles"
    cidr_blocks = ["0.0.0.0/0"]
  }
  tags = {
    Name = "${var.project_name}-jenkins-sg"
  }
}

# Instance EC2 Jenkins
resource "aws_instance" "jenkins_controller" {
  ami           = var.ami_id
  instance_type = "t3.medium" # Recommandé pour Jenkins (2 vCPU, 4 Go RAM)
  subnet_id     = var.private_subnet_id # Isolé dans un subnet privé

  vpc_security_group_ids = [aws_security_group.jenkins.id]
  iam_instance_profile   = aws_iam_instance_profile.jenkins_profile.name

  # User data 
  user_data = <<-EOF
            #!/bin/bash
            yum update -y
            # Indispensable pour Git et Java
            yum install java-21-amazon-corretto git -y

            # Installation de Jenkins
            wget -O /etc/yum.repos.d/jenkins.repo https://pkg.jenkins.io/redhat-stable/jenkins.repo
            rpm --import https://pkg.jenkins.io/jenkins-stable.key
            yum install jenkins -y

            # =========================================================================
            # CORRECTION : GESTION DYNAMIQUE ET SÉCURISÉE DU VOLUME EBS
            # =========================================================================
            
            # 1. Identifier dynamiquement le périphérique NVMe lié à "/dev/sdh"
            # (Utilisation de l'outil 'ebsnvme-id' natif sur Amazon Linux)
            TARGET_DEVICE=""
            for dev in /dev/nvme*n1; do
                if [ -b "$dev" ] && /sbin/ebsnvme-id "$dev" | grep -q "sdh"; then
                    TARGET_DEVICE="$dev"
                    break
                fi
            done

            # Tolérance aux pannes : Si la détection dynamique échoue, on se rabat sur nvme1n1
            if [ -z "$TARGET_DEVICE" ] && [ -b "/dev/nvme1n1" ]; then
                TARGET_DEVICE="/dev/nvme1n1"
            fi

            if [ -n "$TARGET_DEVICE" ]; then
                mkdir -p /var/jenkins_home

                # 2. Sécurité Idempotence : Formater en ext4 UNIQUEMENT s'il n'y a pas déjà un système de fichiers
                # (Évite d'écraser les données si l'infrastructure est recréée mais que le volume EBS persiste)
                if ! blkid "$TARGET_DEVICE" > /dev/null 2>&1; then
                    mkfs.ext4 "$TARGET_DEVICE"
                fi

                # 3. Montage du volume
                mount "$TARGET_DEVICE" /var/jenkins_home
                chown -R jenkins:jenkins /var/jenkins_home

                # 4. Persistance au redémarrage : Ajouter l'entrée dans le fichier /etc/fstab via son UUID
                UUID=$(blkid -o value -s UUID "$TARGET_DEVICE")
                if [ -n "$UUID" ] && ! grep -q "$UUID" /etc/fstab; then
                    echo "UUID=$UUID /var/jenkins_home ext4 defaults,nofail 0 2" >> /etc/fstab
                fi
            fi
            # =========================================================================

            # Configurer Jenkins pour utiliser ce dossier dès le départ
            # Rétrocompatibilité (pour les anciennes versions de Jenkins / Amazon Linux 2)
            if [ -f /etc/sysconfig/jenkins ]; then
                sed -i 's|JENKINS_HOME="/var/lib/jenkins"|JENKINS_HOME="/var/jenkins_home"|g' /etc/sysconfig/jenkins
            fi

            # Standard moderne (Amazon Linux 2023 / systemd) : 
            # Les versions récentes de Jenkins ignorent /etc/sysconfig/jenkins, il faut surcharger systemd
            mkdir -p /etc/systemd/system/jenkins.service.d/
            echo -e "[Service]\nEnvironment=\"JENKINS_HOME=/var/jenkins_home\"" > /etc/systemd/system/jenkins.service.d/override.conf
            systemctl daemon-reload

            systemctl enable jenkins
            systemctl start jenkins
            EOF

  tags = {
    Name = "${var.project_name}-jenkins-controller"
  }
}

# Volume EBS persistant pour le répertoire /var/jenkins_home
resource "aws_ebs_volume" "jenkins_home" {
  availability_zone = aws_instance.jenkins_controller.availability_zone
  size              = 10
  type              = "gp3" # Performance moderne et économique

  tags = {
    Name = "${var.project_name}-jenkins-home-volume"
  }
}

# Attachement du Volume à l'EC2
resource "aws_volume_attachment" "jenkins_attachment" {
  device_name = "/dev/sdh"
  volume_id   = aws_ebs_volume.jenkins_home.id
  instance_id = aws_instance.jenkins_controller.id
}

# Instance Profile pour l'EC2 (Lie le rôle IAM à la machine)
resource "aws_iam_instance_profile" "jenkins_profile" {
  name = "${var.project_name}-jenkins-instance-profile"
  role = aws_iam_role.jenkins_role.name
}
resource "aws_iam_role_policy_attachment" "ssm_policy" {
  role       = aws_iam_role.jenkins_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

# Rôle IAM de base pour l'EC2 Jenkins
resource "aws_iam_role" "jenkins_role" {
  name = "${var.project_name}-jenkins-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
      }
    ]
  })
}

# Politique IAM complète pour EKS et ECR
# -------------------------------------------------------------------------
resource "aws_iam_role_policy" "jenkins_eks_ecr_policy" {
  name = "${var.project_name}-jenkins-eks-ecr-policy"
  role = aws_iam_role.jenkins_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      # 1. Permissions pour l'API EKS
      {
        Effect = "Allow"
        Action = [
          "eks:DescribeCluster",
        ]
        Resource = "*"
      },
      # 2. Permissions pour Pousser/Tirer des images ECR
      {
        Effect = "Allow"
        Action = [
          "ecr:CreateRepository",
          "ecr:GetAuthorizationToken",
          "ecr:BatchCheckLayerAvailability",
          "ecr:GetDownloadUrlForLayer",
          "ecr:GetRepositoryPolicy",
          "ecr:DescribeRepositories",
          "ecr:ListImages",
          "ecr:DescribeImages",
          "ecr:BatchGetImage",
          "ecr:InitiateLayerUpload",
          "ecr:UploadLayerPart",
          "ecr:CompleteLayerUpload",
          "ecr:PutImage"
        ]
        # Conseil prod : Remplacez "*" par l'ARN spécifique de vos dépôts ECR si possible
        Resource = "*" 
      }
    ]
  })
}