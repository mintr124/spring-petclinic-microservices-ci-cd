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
                    def services = ['vets-service', 'visits-service', 'customers-service']
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
