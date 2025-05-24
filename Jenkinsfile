import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.util.HashMap
import java.io.FileNotFoundException

pipeline {
    agent any

    parameters {
        string(name: 'GIT_BRANCH', defaultValue: 'main', description: 'Branch to build (e.g., main or dev-vets-service)')
    }

    environment {
        DOCKER_HUB_CREDENTIALS_ID = 'dockerhub-credentials'
        IMAGE_PREFIX = 'mintr124'
    }

    stages {
        stage('Checkout Code') {
            steps {
                script {
                    echo "Checking out branch '${params.GIT_BRANCH}' for SCM"
                    checkout([
                        $class: 'GitSCM',
                        branches: [[name: "refs/heads/${params.GIT_BRANCH}"]],
                        userRemoteConfigs: [[url: "https://github.com/${env.IMAGE_PREFIX}/spring-petclinic-microservices-ci-cd.git"]]
                    ])
                }
            }
        }

        stage('Determine Build Logic and Tags') {
            steps {
                script {
                    def appServices = ['vets-service', 'customers-service', 'visits-service', 'genai-service']
                    def servicesToBuildAndTagsMap = [:]

                    if (params.GIT_BRANCH == 'main') {
                        echo "Main branch detected. Building all application services with tag 'main'."
                        appServices.each { svc ->
                            servicesToBuildAndTagsMap[svc] = 'main'
                        }
                    } else if (params.GIT_BRANCH.startsWith('dev-')) {
                        def changedService = params.GIT_BRANCH - "dev-"

                        if (!appServices.contains(changedService)) {
                            error "The 'dev-' branch '${params.GIT_BRANCH}' does not correspond to a known application service."
                        }

                        def commitId = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                        echo "Detected branch '${params.GIT_BRANCH}'. Building only ${changedService} with tag '${commitId}'."
                        servicesToBuildAndTagsMap[changedService] = commitId
                    } else {
                        error "Invalid branch name: '${params.GIT_BRANCH}'. Please use 'main' or a 'dev-*' branch."
                    }

                    echo "Services to build and tags determined: ${servicesToBuildAndTagsMap}"

                    def jsonString = JsonOutput.toJson(servicesToBuildAndTagsMap)
                    writeFile file: 'servicesToBuildAndTags.json', text: jsonString
                    stash name: 'servicesToBuildAndTags', includes: 'servicesToBuildAndTags.json'
                }
            }
        }

        stage('Build & Push All Service Images') {
            steps {
                script {
                    def servicesToBuildAndTags = [:]
                    try {
                        unstash 'servicesToBuildAndTags'
                        def jsonString = readFile('servicesToBuildAndTags.json')
                        servicesToBuildAndTags = new HashMap(new JsonSlurper().parseText(jsonString))
                    } catch (FileNotFoundException e) {
                        echo "Error: servicesToBuildAndTags.json not found. This indicates an issue in the previous stage or pipeline state."
                        error "Failed to retrieve service build info."
                    }

                    if (servicesToBuildAndTags.isEmpty()) {
                        echo "No services to build. Skipping build stage."
                        return
                    }

                    withCredentials([usernamePassword(credentialsId: env.DOCKER_HUB_CREDENTIALS_ID, usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                        sh "echo ${DOCKER_PASS} | docker login -u ${DOCKER_USER} --password-stdin"

                        servicesToBuildAndTags.each { serviceName, buildTag ->
                            def repoDir = "spring-petclinic-${serviceName}"
                            def fullImageName = "${env.IMAGE_PREFIX}/${serviceName}:${buildTag}"

                            echo "--- Building ${serviceName} with Maven profile 'buildDocker' ---"
                            dir(repoDir) {
                                echo "Executing: mvn clean install -DskipTests -P buildDocker for ${serviceName}..."
                                sh "mvn clean install -DskipTests -P buildDocker"

                                echo "Pushing Docker image ${fullImageName} to Docker Hub..."
                                sh "docker push ${fullImageName}"
                            }
                            echo "${serviceName} built and pushed with tag: ${buildTag}"
                        }
                        sh "docker logout"
                        echo "All relevant service images built and pushed."
                    }
                }
            }
        }
    }
}
