pipeline {
    agent {
        kubernetes {
            yaml '''
apiVersion: v1
kind: Pod
spec:
  serviceAccountName: jenkins-sa
  containers:
  - name: maven
    image: maven:3.8.5-eclipse-temurin-17
    command: ['cat']
    tty: true
  - name: node
    image: node:18-alpine
    command: ['cat']
    tty: true
  - name: kaniko
    image: gcr.io/kaniko-project/executor:debug
    command: ['cat']
    tty: true
    volumeMounts:
      - name: docker-config
        mountPath: /kaniko/.docker
  - name: aws-helm
    image: amazon/aws-cli:latest
    command: ['cat']
    tty: true
    resources:
      limits:
        memory: "512Mi"
        cpu: "500m"
      requests:
        memory: "256Mi"
        cpu: "250m"
    volumeMounts:
      - name: docker-config
        mountPath: /kaniko/.docker
    # SUPPRESSION du lifecycle/postStart
  volumes:
  - name: docker-config
    emptyDir: {}
'''
        }
    }
   
    environment {
        AWS_REGION = 'eu-west-3'
        IMAGE_TAG = "${env.BUILD_NUMBER}"
        K8S_NAMESPACE = 'production'
    }
   
    stages {
        // ⬇️ NOUVEAU STAGE : Installation de Helm
        stage('Install Helm') {
            steps {
                container('aws-helm') {
                    script {
                        sh '''
                        if ! command -v helm &> /dev/null; then
                            echo "Helm non trouvé, installation en cours..."
                            curl -fsSL -o get_helm.sh https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3
                            chmod 700 get_helm.sh
                            ./get_helm.sh
                            rm -f get_helm.sh
                        else
                            echo "Helm déjà installé"
                        fi
                        helm version
                        '''
                    }
                }
            }
        }

        stage('Fetch Infra State (SSM)') {
            steps {
                container('aws-helm') {
                    script {
                        echo "--- Récupération des variables depuis AWS SSM Parameter Store ---"
                       
                        env.DB_PASSWORD = sh(script: "aws ssm get-parameter --name '/cicd/rds/password' --with-decryption --query 'Parameter.Value' --output text", returnStdout: true).trim()
                        env.ECR_BACKEND = sh(script: "aws ssm get-parameter --name '/cicd/ecr/backend_url' --query 'Parameter.Value' --output text", returnStdout: true).trim()
                        env.ECR_FRONTEND = sh(script: "aws ssm get-parameter --name '/cicd/ecr/frontend_url' --query 'Parameter.Value' --output text", returnStdout: true).trim()
                        env.RDS_ENDPOINT = sh(script: "aws ssm get-parameter --name '/cicd/rds/endpoint' --query 'Parameter.Value' --output text", returnStdout: true).trim()
                        env.APP_CERT_ARN = sh(script: "aws ssm get-parameter --name '/cicd/app/cert_arn' --query 'Parameter.Value' --output text", returnStdout: true).trim()
                       
                        def rds_host = env.RDS_ENDPOINT.split(':')[0]
                        env.RDS_HOST = rds_host
                       
                        echo "✅ Backend ECR : ${env.ECR_BACKEND}"
                        echo "✅ Frontend ECR: ${env.ECR_FRONTEND}"
                        echo "✅ RDS Host: ${env.RDS_HOST}"
                        echo "✅ App Cert ARN: ${env.APP_CERT_ARN}"
                    }
                }
            }
        }
        stage('Backend: Test & Compile') {
            steps {
                container('maven') {
                    dir('backend') {
                        sh 'mvn clean package -DskipTests'
                    }
                }
            }
        }
        stage('Frontend: Test & Compile') {
            steps {
                container('node') {
                    dir('frontend') {
                        sh 'npm ci'
                        sh 'npm run build'
                    }
                }
            }
        }
        stage('Build & Push to ECR (Kaniko)') {
            steps {
                container('aws-helm') {
                    script {
                        sh """
                        REGISTRY_URL=\$(echo ${env.ECR_BACKEND} | cut -d'/' -f1)
                        PASSWORD=\$(aws ecr get-login-password --region ${AWS_REGION})
                        echo "{\\"auths\\":{\\"\${REGISTRY_URL}\\":{\\"username\\":\\"AWS\\",\\"password\\":\\"\${PASSWORD}\\"}}}" > /kaniko/.docker/config.json
                        """
                    }
                }
                container('kaniko') {
                    script {
                        dir('backend') {
                            sh "/kaniko/executor --context `pwd` --dockerfile `pwd`/Dockerfile --destination ${env.ECR_BACKEND}:${IMAGE_TAG}"
                        }
                        dir('frontend') {
                            sh "/kaniko/executor --context `pwd` --dockerfile `pwd`/Dockerfile --destination ${env.ECR_FRONTEND}:${IMAGE_TAG}"
                        }
                    }
                }
            }
        }
        stage('Deploy via Helm') {
            steps {
                container('aws-helm') {
                    script {
                        // Créer le namespace s'il n'existe pas
                        sh "kubectl create namespace ${K8S_NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -"
                       
                        // Mettre à jour kubeconfig
                        sh "aws eks update-kubeconfig --region ${AWS_REGION} --name ci-cd-project-eks"
                       
                        // Créer un fichier values temporaire
                        sh """
                        cat > /tmp/values-${BUILD_NUMBER}.yaml << EOF
backend:
  image:
    repository: ${env.ECR_BACKEND}
    tag: ${IMAGE_TAG}
    pullPolicy: Always
  env:
    database:
      host: ${env.RDS_HOST}
      password: ${env.DB_PASSWORD}
      username: dbadmin
      name: bmicalculator
      port: 5432
    springProfile: prod
frontend:
  image:
    repository: ${env.ECR_FRONTEND}
    tag: ${IMAGE_TAG}
    pullPolicy: Always
  ingress:
    enabled: true
    hostname: app.kolynois.com
    annotations:
      alb.ingress.kubernetes.io/scheme: internet-facing
      alb.ingress.kubernetes.io/target-type: ip
      alb.ingress.kubernetes.io/listen-ports: '[{"HTTPS":443}]'
      alb.ingress.kubernetes.io/certificate-arn: ${env.APP_CERT_ARN}
EOF
                        """
                       
                        echo "--- Déploiement Backend ---"
                        sh """
                        helm upgrade --install backend-release ./helm/helm-charts/backend \
                          --namespace ${K8S_NAMESPACE} \
                          --values /tmp/values-${BUILD_NUMBER}.yaml \
                          --timeout 10m
                        """
                       
                        echo "--- Déploiement Frontend ---"
                        sh """
                        helm upgrade --install frontend-release ./helm/helm-charts/frontend \
                          --namespace ${K8S_NAMESPACE} \
                          --values /tmp/values-${BUILD_NUMBER}.yaml \
                          --timeout 10m
                        """
                       
                        // Nettoyer le fichier temporaire
                        sh "rm -f /tmp/values-${BUILD_NUMBER}.yaml"
                    }
                }
            }
        }

        // ====================== STAGE FLUENTBIT CORRIGÉ ======================
        stage('Install FluentBit (Logging)') {
            steps {
                container('aws-helm') {
                    script {
                        echo "--- Installation de AWS for Fluent Bit avec IRSA ---"
                        
                        sh "helm repo add eks https://aws.github.io/eks-charts || true"
                        sh "helm repo update"
                        
                        def FLUENTBIT_ROLE_ARN = sh(
                            script: "aws ssm get-parameter --name '/cicd/fluentbit/role_arn' --query 'Parameter.Value' --output text",
                            returnStdout: true
                        ).trim()
                        
                        echo "✅ FluentBit Role ARN récupéré : ${FLUENTBIT_ROLE_ARN}"
                        
                        sh """
                        helm upgrade --install aws-for-fluent-bit eks/aws-for-fluent-bit \
                          --namespace kube-system \
                          --set cloudWatch.region=${AWS_REGION} \
                          --set cloudWatch.logGroupName=/eks/ci-cd-project-eks/applications \
                          --set cloudWatch.logStreamPrefix=container-logs- \
                          --set cloudWatch.autoCreateGroup=true \
                          --set serviceAccount.create=true \
                          --set serviceAccount.name=fluent-bit \
                          --set serviceAccount.annotations."eks\\.amazonaws\\.com/role-arn"=${FLUENTBIT_ROLE_ARN} \
                          --set rbac.create=true \
                          --set tolerations[0].key=CriticalAddonsOnly \
                          --set tolerations[0].operator=Exists \
                          --timeout 10m0s
                        """
                        
                        echo "✅ AWS for Fluent Bit déployé / mis à jour avec succès"
                    }
                }
            }
        }
    } // Fin des stages

    post {
        success {
            script {
                echo "✅ Pipeline réussi pour le build ${IMAGE_TAG}"
                echo "🔗 Jenkins: https://jenkins.kolynois.com"
                echo "🔗 Application: https://app.kolynois.com"
            }
        }
        failure {
            script {
                echo "❌ Pipeline échoué pour le build ${IMAGE_TAG}"
            }
        }
        always {
            script {
                echo "✅ Fin d'exécution du pipeline. Le pod Kubernetes éphémère et ses fichiers temporaires seront automatiquement détruits."
            }
        }
    }
}