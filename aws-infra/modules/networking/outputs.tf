output "vpc_id" {
  description = "L'ID du VPC"
  value       = module.vpc.vpc_id
}
output "private_subnets" {
  description = "Liste des IDs des subnets prives"
  value       = module.vpc.private_subnets
}
output "database_subnets" {
  description = "Liste des IDs des sous-reseaux dedies a la base de donnees"
  value       = module.vpc.database_subnets
}
output "endpoints_sg_id" {
  description = "L'ID du groupe de sécurité pour les VPC Endpoints"
  value       = aws_security_group.endpoints_sg.id
}
# Fichier : modules/networking/outputs.tf

output "private_route_table_ids" {
  description = "IDs des tables de routage privées"
  value       = module.vpc.private_route_table_ids
}
output "public_subnets" {
  description = "IDs des sous-réseaux publics"
  value       = module.vpc.public_subnets
}
