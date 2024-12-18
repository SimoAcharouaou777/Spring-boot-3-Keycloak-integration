pipeline {
    agent any

    environment {
        PATH = "C:\\\\Program Files\\\\Docker\\\\Docker\\\\resources\\\\bin;${env.PATH}"
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/SimoAcharouaou777/Spring-boot-3-Keycloak-integration.git'
            }
        }

        stage('Build') {
            steps {
                bat 'chmod +x mvnw'
                bat './mvnw clean install -DskipTests'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQubeDevops') {
                    withCredentials([string(credentialsId: 'sonar-token2', variable: 'SONAR_TOKEN')]) {
                        bat '''mvnw.cmd sonar:sonar ^
                            -Dsonar.projectKey=com.keyloack:integrationkeyloack ^
                            -Dsonar.host.url=http://172.21.224.1:9000 ^
                            -Dsonar.login=%SONAR_TOKEN%'''
                    }
                }
            }
        }

        stage('Unit Tests & Coverage') {
            steps {
                bat './mvnw test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                    jacoco execPattern: 'target/jacoco.exec', classPattern: 'target/classes', sourcePattern: 'src/main/java'
                }
            }
        }

        stage('Quality Gate') {
            steps {
                script {
                    timeout(time: 2, unit: 'MINUTES') {
                        withSonarQubeEnv('SonarQubeDevops') {
                            def qg = waitForQualityGate()
                            if (qg.status != 'OK') {
                                error "Quality Gate failed: ${qg.status}"
                            }
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
                bat "docker --version" // Verify Docker works
                bat "docker build -t myapp:latest ."
            }
        }

        stage('Run Container') {
            steps {
                bat "docker run -d -p 8083:8083 --name myapp-container myapp:latest"
            }
        }
    }

    post {
        always {
            echo 'Pipeline finished'
        }
    }
}
