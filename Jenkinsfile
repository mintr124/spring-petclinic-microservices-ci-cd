pipeline {
    agent any
    environment {
        REGISTRY = "docker.io/mintr124"
        COMMIT_ID = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
    }
    stages {
        stage('Build & Push Images') {
            steps {
                script {
                    def services = [
                        'spring-petclinic-vets-service', 
                        'spring-petclinic-visits-service', 
                        'spring-petclinic-customers-service',
                        'spring-petclinic-admin-server', 
                        'spring-petclinic-api-gateway',
                        'spring-petclinic-config-server', 
                        'spring-petclinic-discovery-server', 
                        'spring-petclinic-genai-service'
                    ]
                    for (s in services) {
                        dir("${s}") {
                            withCredentials([usernamePassword(
                                credentialsId: 'dockerhub-credentials',
                                usernameVariable: 'DOCKER_USERNAME',
                                passwordVariable: 'DOCKER_PASSWORD'
                            )]) {
                                sh """
                                    docker build -t $REGISTRY/${s}:${COMMIT_ID} .
                                    echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
                                    docker push $REGISTRY/${s}:${COMMIT_ID}
                                """
                            }
                        }
                    }
                }
            }
        }
    }
}
