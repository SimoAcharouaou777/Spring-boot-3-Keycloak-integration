pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/SimoAcharouaou777/Spring-boot-3-Keycloak-integration.git'
            }
        }

        stage('Build') {
            steps {
                sh 'chmod +x mvnw'
                sh './mvnw clean install -DskipTests'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQubeDevops') {
                    withCredentials([string(credentialsId: 'sonar-token2', variable: 'SONAR_TOKEN')]) {
                        sh './mvnw sonar:sonar \
                            -Dsonar.projectKey=com.keyloack:integrationkeyloack \
                            -Dsonar.host.url=http://172.21.224.1:9000 \
                            -Dsonar.login=$SONAR_TOKEN'
                    }
                }
            }
        }

        stage('Unit Tests & Coverage') {
            steps {
                sh './mvnw test'
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
                sh "docker build -t myapp:latest ."
            }
        }

        stage('Run Container') {
            steps {
                sh "docker run -d -p 8083:8083 --name myapp-container myapp:latest"
            }
        }
    }

    post {
        always {
            echo 'Pipeline finished'
        }
    }
}
