pipeline {
    agent any

    environment {
        PATH = "/usr/bin:/usr/local/bin:${env.PATH}"
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/SimoAcharouaou777/Spring-boot-3-Keycloak-integration.git'
            }
        }

        stage('Build and Test') {
            steps {
                sh 'chmod +x mvnw'
                sh './mvnw clean install' // Removed -DskipTests to run tests during build
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                    jacoco execPattern: 'target/jacoco.exec', classPattern: 'target/classes', sourcePattern: 'src/main/java'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQubeDevops') {
                    withCredentials([string(credentialsId: 'sonar-token2', variable: 'SONAR_TOKEN')]) {
                        sh '''
                            ./mvnw sonar:sonar \
                            -Dsonar.projectKey=com.keyloack:integrationkeyloack \
                            -Dsonar.host.url=http://172.21.224.1:9000 \
                            -Dsonar.login=$SONAR_TOKEN
                        '''
                    }
                }
            }
        }

        stage('Quality Gate') {
            steps {
                script {
                    timeout(time: 10, unit: 'MINUTES') {
                        def qualityGate = waitForQualityGate()
                        if (qualityGate.status != 'OK') {
                            error "Quality Gate failed: ${qualityGate.status}"
                        }
                    }
                }
            }
        }

        stage('Manual Approval') {
            steps {
                script {
                    input message: "Approve deployment?", submitter: "admin"
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh 'docker build -t myapp:latest .'
            }
        }

        stage('Run Container') {
            steps {
                sh 'docker run -d -p 8083:8081 --name myapp-container myapp:latest'
            }
        }
    }

    post {
        success {
            mail to: 'acharouaoumohamed@gmail.com',
                 subject: "Pipeline Success - eBankify",
                 body: "Le pipeline Jenkins s'est terminé avec succès !"
        }
        failure {
            mail to: 'acharouaoumohamed@gmail.com',
                 subject: "Pipeline Failure - eBankify",
                 body: "Le pipeline Jenkins a échoué. Veuillez vérifier les logs."
        }
    }
}
