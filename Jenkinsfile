pipeline {
    agent any
    environment {
        REGISTRY = "docker.io/mintr124"
        COMMIT_ID = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
        BRANCH_NAME = sh(script: 'git rev-parse --abbrev-ref HEAD', returnStdout: true).trim()
    }
    stages {
        stage('Build & Push Image') {
            steps {
                script {
                    // Chỉ cần build một image cho toàn bộ repo
                    def imageName = "${COMMIT_ID}"
                    withCredentials([usernamePassword(
                        credentialsId: 'dockerhub-credentials',
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )]) {
                        sh """
                            docker build -t $REGISTRY/$imageName .
                            echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
                            docker push $REGISTRY/$imageName
                        """
                    }
                }
            }
        }
    }
}
