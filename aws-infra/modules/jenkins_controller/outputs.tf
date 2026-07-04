output "iam_role_arn" {
  description = "L'ARN du rôle IAM attaché au contrôleur Jenkins pour l'associer à EKS"
  value       = aws_iam_role.jenkins_role.arn
}

output "asg_name" {
  description = "Le nom de l'Auto Scaling Group gérant Jenkins"
  value       = aws_autoscaling_group.jenkins.name
}
output "jenkins_sg_id" {
  description = "L'ID du Security Group Jenkins"
  value       = aws_security_group.jenkins.id
}
# modules/jenkins_controller/outputs.tf

output "alb_dns_name" {
  description = "Le nom DNS de l'ALB Jenkins"
  value       = aws_lb.jenkins_alb.dns_name
}

output "alb_zone_id" {
  description = "L'ID de zone de l'ALB Jenkins (pour les alias Route 53)"
  value       = aws_lb.jenkins_alb.zone_id
}