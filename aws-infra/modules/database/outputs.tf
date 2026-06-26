output "db_endpoint" {
  description = "L'adresse de connexion (Endpoint) à la base de données PostgreSQL"
  value       = aws_db_instance.postgres.endpoint
}
output "rds_db_name" {
  description = "Nom de la base de données par défaut"
  value       = aws_db_instance.postgres.db_name
}