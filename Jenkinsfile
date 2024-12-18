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
                sh './mvnw clean install -DskipTests'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('MySonarQubeServer') {
                    sh './mvnw sonar:sonar -Dsonar.projectKey=com.keyloack:integrationkeyloack'
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
                    // waitForQualityGate requires SonarQube integration
                    timeout(time: 2, unit: 'MINUTES') {
                        def qg = waitForQualityGate()
                        if (qg.status != 'OK') {
                            error "Quality gate failed: ${qg.status}"
                        }
                    }
                }
            }
        }

        stage('Manual Approval') {
            steps {
                script {
                    input(message: "Approve deployment?", submitter: "admin")
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
        success {
            // Configure mail or slack plugin first, then:
            mail to: 'devteam@example.com',
                 subject: "Build Successful",
                 body: "The pipeline completed successfully!"
        }
        failure {
            mail to: 'devteam@example.com',
                 subject: "Build Failed",
                 body: "The pipeline has failed. Check the logs for details."
        }
    }
}
