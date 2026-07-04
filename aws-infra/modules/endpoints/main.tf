module "vpc_endpoints" {
  source  = "terraform-aws-modules/vpc/aws//modules/vpc-endpoints"
  version = "5.21.0"

  vpc_id             = var.vpc_id
  security_group_ids = [var.endpoint_security_group_id]

  endpoints = {
    s3 = {
      service         = "s3"
      service_type    = "Gateway"
      route_table_ids = var.route_table_ids # <--- On utilise la variable ici
    },
    ssm = {
      service             = "ssm"
      service_type        = "Interface"
      subnet_ids          = var.private_subnet_ids
      private_dns_enabled = true
    },
    ssmmessages = {
      service             = "ssmmessages"
      service_type        = "Interface"
      subnet_ids          = var.private_subnet_ids
      private_dns_enabled = true
    },
    ec2messages = {
      service             = "ec2messages"
      service_type        = "Interface"
      subnet_ids          = var.private_subnet_ids
      private_dns_enabled = true
    },
    ecr_api = {
      service             = "ecr.api"
      service_type        = "Interface"
      subnet_ids          = var.private_subnet_ids
      private_dns_enabled = true
    },
    ecr_dkr = {
      service             = "ecr.dkr"
      service_type        = "Interface"
      subnet_ids          = var.private_subnet_ids
      private_dns_enabled = true
    }
  }
}