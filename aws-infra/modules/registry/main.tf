# ==========================================
# REPOSITORY BACKEND
# ==========================================
resource "aws_ecr_repository" "backend_repo" {
  name                 = "${var.project_name}-backend-repo"
  image_tag_mutability = "MUTABLE"
  
  image_scanning_configuration {
    scan_on_push = true
  }
  
  tags = {
    Name= "${var.project_name}-backend-repo"
    Project= var.project_name
    Service= "backend"
  }
}

# ==========================================
# REPOSITORY FRONTEND
# ==========================================
resource "aws_ecr_repository" "frontend_repo" {
  name                 = "${var.project_name}-frontend-repo"
  image_tag_mutability = "MUTABLE"
  
  image_scanning_configuration {
    scan_on_push = true
  }
  
  tags = {
    Name="${var.project_name}-frontend-repo"
    Project= var.project_name
    Service= "frontend"
  }
}