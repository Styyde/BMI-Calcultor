# Image de base officielle AWS CLI (basée sur Amazon Linux 2023)
FROM amazon/aws-cli:latest

# Metadonnées
LABEL maintainer="CI/CD Pipeline Admin"
LABEL description="Image personnalisée contenant AWS CLI, Kubectl, Helm, Git"

# Étape 1 : Installation des paquets système manquants (Git, Tar, Gzip, OpenSSL)
# Note: bash, jq et curl (via curl-minimal) sont déjà pré-installés
RUN yum update -y && \
    yum install -y \
    git \
    tar \
    gzip \
    openssl && \
    yum clean all

# Étape 2 : Installation de kubectl
RUN curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl" && \
    chmod +x ./kubectl && \
    mv ./kubectl /usr/local/bin/kubectl

# Étape 3 : Installation de Helm 3
RUN curl -fsSL -o get_helm.sh https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 && \
    chmod 700 get_helm.sh && \
    ./get_helm.sh && \
    rm get_helm.sh

# Étape 4 : Vérification des installations au moment du build
RUN aws --version && \
    kubectl version --client && \
    helm version && \
    git --version && \
    jq --version

# Remplacement de l'ENTRYPOINT pour la compatibilité Jenkins
ENTRYPOINT []
CMD ["/bin/bash"]