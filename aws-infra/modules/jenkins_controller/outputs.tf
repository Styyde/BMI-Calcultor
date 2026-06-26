output "jenkins_private_ip" {
  description = "L'IP privée du contrôleur Jenkins (Utile pour la configuration du webhook GitHub via VPN/Proxy)"
  value       = aws_instance.jenkins_controller.private_ip
}

output "jenkins_instance_id" {
  description = "L'ID de l'instance Jenkins"
  value       = aws_instance.jenkins_controller.id
}

output "jenkins_sg_id" {
  description = "L'ID du Security Group de Jenkins"
  value       = aws_security_group.jenkins.id
}
output "jenkins_iam_role_arn" {
  description = "ARN du rôle IAM Jenkins (à utiliser dans les Access Entries)"
  value       = aws_iam_role.jenkins_role.arn   
}