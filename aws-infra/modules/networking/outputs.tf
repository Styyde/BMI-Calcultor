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
