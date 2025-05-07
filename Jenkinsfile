pipeline {
    agent any
    environment {
        DOCKER_CREDENTIALS_ID = 'dockerhub-credentials' // ID của Docker Hub credentials trong Jenkins
        DOCKERHUB_NAMESPACE = 'mintr124'
    }
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/mintr124/spring-petclinic-microservices-ci-cd.git'
            }
        }
        stage('Build & Push Images') {
            steps {
                script {
                    def services = ['vets-service', 'visits-service', 'customers-service']
                    def commitId = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                    services.each { service ->
                        dir(service) {
                            def imageTag = "${DOCKERHUB_NAMESPACE}/${service}:${commitId}"
                            docker.withRegistry('', DOCKER_CREDENTIALS_ID) {
                                def customImage = docker.build(imageTag)
                                customImage.push()
                            }
                        }
                    }
                }
            }
        }
    }
}
