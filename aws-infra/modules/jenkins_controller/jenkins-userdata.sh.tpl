#!/bin/bash
set -euo pipefail

exec > >(tee /var/log/user-data.log | logger -t user-data -s 2>/dev/console) 2>&1

echo "=== Démarrage bootstrap Jenkins HA (EFS + S3) ==="

# Fonction pour attendre que yum soit libre
wait_for_yum() {
    local max_attempts=30
    local attempt=0
    while pgrep -x "yum" > /dev/null; do
        attempt=$((attempt+1))
        if [ $attempt -ge $max_attempts ]; then
            echo "⚠️ YUM toujours occupé après $max_attempts tentatives. On continue."
            return 0
        fi
        echo "⏳ YUM en cours d'exécution, attente 5s... (tentative $attempt/$max_attempts)"
        sleep 5
    done
    return 0
}

# Attendre que yum soit libre avant de commencer
wait_for_yum

# Mise à jour système
yum update -y

# === Installation AWS CLI v2 ===
echo "=== Installation AWS CLI v2 ==="
curl -s "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "/tmp/awscliv2.zip"
cd /tmp
unzip -q -o awscliv2.zip
./aws/install --bin-dir /usr/local/bin --install-dir /usr/local/aws-cli --update
rm -rf /tmp/awscliv2.zip /tmp/aws
cd -

# Installation des paquets de base (sans dejavu-fonts qui n'existe pas sur AL2)
wait_for_yum
yum install -y git jq wget unzip amazon-efs-utils fontconfig

# === Installation Java 21 ===
echo "=== Installation Java 21 ==="
curl -L -o /tmp/corretto21.rpm https://corretto.aws/downloads/latest/amazon-corretto-21-x64-linux-jdk.rpm
yum localinstall -y /tmp/corretto21.rpm
rm -f /tmp/corretto21.rpm

# === Installation kubectl ===
echo "=== Installation kubectl ==="
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl
rm -f kubectl

# === Installation Jenkins ===
echo "=== Installation Jenkins ==="
wait_for_yum
wget -O /etc/yum.repos.d/jenkins.repo https://pkg.jenkins.io/redhat-stable/jenkins.repo
rpm --import https://pkg.jenkins.io/redhat-stable/jenkins.io-2026.key
yum install -y jenkins

# === Configuration des logs Jenkins ===
echo "=== Configuration des logs Jenkins ==="
mkdir -p /var/log/jenkins
chown -R jenkins:jenkins /var/log/jenkins

# Utiliser systemd override pour JENKINS_OPTS (plus fiable que sysconfig)
mkdir -p /etc/systemd/system/jenkins.service.d/
cat > /etc/systemd/system/jenkins.service.d/override.conf << 'EOF'
[Service]
Environment="JENKINS_OPTS=--logfile=/var/log/jenkins/jenkins.log"
EOF

# === Montage EFS ===
echo "=== Configuration et montage EFS ==="
mkdir -p /var/lib/jenkins
echo "${efs_id}:/ /var/lib/jenkins efs _netdev,tls,iam 0 0" >> /etc/fstab

MOUNT_SUCCESS=false
for i in {1..5}; do
    if mount -a; then
        echo "EFS monté avec succès."
        MOUNT_SUCCESS=true
        break
    fi
    echo "Tentative $i/5 échouée..."
    sleep 10
done

if [ "$MOUNT_SUCCESS" = false ]; then
    echo "ERREUR CRITIQUE : Impossible de monter l'EFS après 5 tentatives."
    exit 1
fi

# === Restauration depuis S3 ===
if [ -z "$(ls -A /var/lib/jenkins 2>/dev/null)" ]; then
    echo "Restauration depuis S3..."
    /usr/local/bin/aws s3 sync "s3://${s3_bucket}/jenkins_home" /var/lib/jenkins --region "${aws_region}" || echo "Pas de backup"
fi

chown -R jenkins:jenkins /var/lib/jenkins

# === Attente du cluster EKS ===
echo "=== Attente du cluster EKS (ci-cd-project-eks) ==="
MAX_RETRIES=30
RETRY=0
EKS_READY=false

while [ $RETRY -lt $MAX_RETRIES ]; do
    STATUS=$(/usr/local/bin/aws eks describe-cluster --name "ci-cd-project-eks" --region "${aws_region}" --query "cluster.status" --output text 2>/dev/null || echo "NOT_FOUND")
    if [ "$STATUS" = "ACTIVE" ]; then
        echo "Cluster EKS actif."
        EKS_READY=true
        break
    fi
    echo "Statut: $STATUS - attente ($((RETRY+1))/$MAX_RETRIES)..."
    sleep 30
    RETRY=$((RETRY+1))
done

if [ "$EKS_READY" = true ]; then
    sudo -u jenkins mkdir -p /var/lib/jenkins/.kube
    sudo -u jenkins /usr/local/bin/aws eks update-kubeconfig \
        --region "${aws_region}" \
        --name "ci-cd-project-eks" \
        --kubeconfig /var/lib/jenkins/.kube/config
    chown jenkins:jenkins /var/lib/jenkins/.kube/config
    echo "kubeconfig Jenkins configuré."
else
    echo "⚠️ Cluster EKS non disponible après $MAX_RETRIES tentatives."
fi

# === Cron de backup quotidien ===
echo "=== Configuration du backup quotidien ==="
cat > /etc/cron.d/jenkins-s3-backup << EOF
0 2 * * * root /usr/local/bin/aws s3 sync /var/lib/jenkins "s3://${s3_bucket}/jenkins_home" --delete --region "${aws_region}"
EOF
chmod 644 /etc/cron.d/jenkins-s3-backup

# === Démarrage de Jenkins ===
echo "=== Démarrage du service Jenkins ==="
systemctl daemon-reload
systemctl enable --now jenkins

# === Installation de l'agent CloudWatch ===
echo "=== Installation de l'agent CloudWatch ==="
wait_for_yum
yum install -y amazon-cloudwatch-agent

# Nettoyer les anciennes configurations
rm -f /opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.d/*

cat > /opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json << 'EOF'
{
  "logs": {
    "logs_collected": {
      "files": {
        "collect_list": [
          {
            "file_path": "/var/log/jenkins/jenkins.log",
            "log_group_name": "/jenkins/controller",
            "log_stream_name": "{instance_id}"
          },
          {
            "file_path": "/var/log/user-data.log",
            "log_group_name": "/jenkins/bootstrap",
            "log_stream_name": "{instance_id}"
          }
        ]
      }
    }
  }
}
EOF

/opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl \
  -a fetch-config \
  -m ec2 \
  -c file:/opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json \
  -s

systemctl enable --now amazon-cloudwatch-agent

echo "=== Bootstrap Jenkins terminé avec succès ! ==="