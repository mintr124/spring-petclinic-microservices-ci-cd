pipeline {
    agent any
    environment {
        REGISTRY = "docker.io/mintr124" // Docker Registry
        COMMIT_ID = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim() // Get current commit ID
    }
    stages {
        stage('Build & Push Images') {
            steps {
                script {
                    // Get the current branch name
                    def branchName = sh(script: 'git rev-parse --abbrev-ref HEAD', returnStdout: true).trim()

                    // List of services
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

                    // Loop through each service to build and push the image
                    for (s in services) {
                        dir("${s}") {
                            // Check if Dockerfile exists in the service directory
                            def dockerfileExists = fileExists 'Dockerfile'
                            if (dockerfileExists) {
                                // Build and push Docker image with the commit ID tag
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
                            } else {
                                echo "Dockerfile does not exist in the ${s} directory, skipping this service."
                            }
                        }
                    }
                }
            }
        }
    }
}
