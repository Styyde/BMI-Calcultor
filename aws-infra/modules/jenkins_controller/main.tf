# ==============================================================================
# 1. RÔLE IAM ET PROFIL D'INSTANCE POUR JENKINS (S3 & EKS Access)
# ==============================================================================

# Récupération de l'ID du compte AWS (utile pour les ARN)
data "aws_caller_identity" "current" {}

resource "aws_iam_role" "jenkins_role" {
  name = "${var.project_name}-jenkins-controller-role"

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

resource "aws_iam_role_policy_attachment" "jenkins_ssm" {
  role       = aws_iam_role.jenkins_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

# Droits d'accès : Synchronisation S3 + Introspection EKS + CloudWatch Logs
resource "aws_iam_role_policy" "jenkins_policy" {
  name = "${var.project_name}-jenkins-controller-policy"
  role = aws_iam_role.jenkins_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      # 1. Accès S3 pour les backups
      {
        Effect = "Allow"
        Action = [
          "s3:GetObject",
          "s3:PutObject",
          "s3:ListBucket",
          "s3:DeleteObject"
        ]
        Resource = [
          aws_s3_bucket.jenkins_backup.arn,
          "${aws_s3_bucket.jenkins_backup.arn}/*"
        ]
      },
      # 2. Introspection EKS
      {
        Effect = "Allow"
        Action = [
          "eks:DescribeCluster",
          "eks:ListClusters"
        ]
        Resource = "*"
      },
      # 3. Montage EFS sécurisé (option ,iam)
      {
        Effect = "Allow"
        Action = [
          "elasticfilesystem:DescribeMountTargets",
          "elasticfilesystem:DescribeFileSystems",
          "elasticfilesystem:ClientMount",
          "elasticfilesystem:ClientWrite"
        ]
        Resource = aws_efs_file_system.jenkins_home.arn
      },
      # 4. ⬇️ NOUVEAU : CloudWatch Logs (pour l'agent CloudWatch)
      {
        Effect = "Allow"
        Action = [
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:PutLogEvents",
          "logs:DescribeLogStreams"
        ]
        # Pour une sécurité renforcée, vous pouvez restreindre à un groupe spécifique :
        # Resource = "arn:aws:logs:${var.aws_region}:${data.aws_caller_identity.current.account_id}:log-group:*"
        # Sinon, "*" est plus simple mais moins restrictif
        Resource = "*"
      }
    ]
  })
}

resource "aws_iam_instance_profile" "jenkins_profile" {
  name = "${var.project_name}-jenkins-instance-profile"
  role = aws_iam_role.jenkins_role.name
}

# ==============================================================================
# 2. STOCKAGE PERSISTANT CENTRALISÉ (S3 State Bucket & EFS)
# ==============================================================================
resource "aws_s3_bucket" "jenkins_backup" {
  bucket        = "${var.project_name}-jenkins-state-backup-prod"
  force_destroy = false
}

resource "aws_efs_file_system" "jenkins_home" {
  creation_token   = "${var.project_name}-jenkins-efs"
  encrypted        = true
  performance_mode = "generalPurpose"

  tags = {
    Name = "${var.project_name}-jenkins-efs"
  }
}

resource "aws_efs_mount_target" "jenkins_efs_targets" {
  count           = length(var.private_subnet_ids)
  
  file_system_id  = aws_efs_file_system.jenkins_home.id
  subnet_id       = var.private_subnet_ids[count.index]
  security_groups = [aws_security_group.efs_sg.id]
}

# ==============================================================================
# 3. APPLICATION LOAD BALANCER (ALB)
# ==============================================================================
resource "aws_security_group" "alb_sg" {
  name   = "jenkins-alb-sg"
  vpc_id = var.vpc_id

  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_lb" "jenkins_alb" {
  name               = "jenkins-alb"
  internal           = false 
  load_balancer_type = "application"
  security_groups    = [aws_security_group.alb_sg.id]
  subnets            = var.public_subnet_ids 
}

resource "aws_lb_target_group" "jenkins_tg" {
  name     = "jenkins-tg"
  port     = 8080
  protocol = "HTTP"
  vpc_id   = var.vpc_id

  health_check {
    path                = "/login"
    healthy_threshold   = 3
    unhealthy_threshold = 5
    timeout             = 5
    interval            = 30
    matcher             = "200-399"
  }
}

resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.jenkins_alb.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type = "redirect"

    redirect {
      port        = "443"
      protocol    = "HTTPS"
      status_code = "HTTP_301"
    }
  }
}

resource "aws_lb_listener" "https" {
  load_balancer_arn = aws_lb.jenkins_alb.arn
  port              = 443
  protocol          = "HTTPS"
  ssl_policy        = "ELBSecurityPolicy-TLS13-1-2-2021-06"
  certificate_arn   = var.jenkins_certificate_arn

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.jenkins_tg.arn
  }
}

# ==============================================================================
# LAUNCH TEMPLATE & BOOTSTRAP IMMUABLE (User Data)
# ==============================================================================
resource "aws_launch_template" "jenkins" {
  name_prefix   = "${var.project_name}-jenkins-template-"
  image_id      = var.ami_id
  instance_type = "t3.medium"

  iam_instance_profile {
    arn = aws_iam_instance_profile.jenkins_profile.arn
  }

  network_interfaces {
    associate_public_ip_address = false
    security_groups             = [aws_security_group.jenkins.id]
  }

  user_data = base64encode(templatefile("${path.module}/jenkins-userdata.sh.tpl", {
    efs_id         = aws_efs_file_system.jenkins_home.id
    s3_bucket      = aws_s3_bucket.jenkins_backup.id
    aws_region     = "eu-west-3"
  }))

  depends_on = [
    aws_iam_role_policy.jenkins_policy,
    aws_efs_file_system.jenkins_home,
    aws_efs_mount_target.jenkins_efs_targets
  ]

  tag_specifications {
    resource_type = "instance"
    tags = {
      Name = "${var.project_name}-jenkins-controller"
    }
  }

  lifecycle {
    create_before_destroy = true
  }
}

# ==============================================================================
# 5. AUTO SCALING GROUP (Haute Disponibilité Multi-AZ)
# ==============================================================================
resource "aws_autoscaling_group" "jenkins" {
  name_prefix         = "${var.project_name}-jenkins-asg-"
  desired_capacity    = 1
  min_size            = 1
  max_size            = 1 
  vpc_zone_identifier = var.private_subnet_ids

  launch_template {
    id      = aws_launch_template.jenkins.id
    version = "$Latest"
  }

  target_group_arns = [aws_lb_target_group.jenkins_tg.arn]

  health_check_type         = "EC2"
  health_check_grace_period = 300

  lifecycle {
    create_before_destroy = true
  }
}

# ==============================================================================
# 6. FILTRAGE RÉSEAU (Security Groups)
# ==============================================================================
resource "aws_security_group" "jenkins" {
  name        = "${var.project_name}-jenkins-sg"
  description = "Security group pour le controller Jenkins"
  vpc_id      = var.vpc_id

  ingress {
    description     = "Allow UI access ONLY from ALB"
    from_port       = 8080
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [aws_security_group.alb_sg.id] 
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-jenkins-sg"
  }
}

resource "aws_security_group" "efs_sg" {
  name        = "${var.project_name}-efs-sg"
  description = "Autorise le trafic NFS depuis le controller Jenkins"
  vpc_id      = var.vpc_id

  ingress {
    from_port       = 2049
    to_port         = 2049
    protocol        = "tcp"
    security_groups = [aws_security_group.jenkins.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}