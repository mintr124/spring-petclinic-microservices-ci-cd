import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.util.HashMap
import java.io.FileNotFoundException

pipeline {
    agent any

    environment {
        DOCKER_HUB_CREDENTIALS_ID = 'dockerhub-credentials'
        IMAGE_PREFIX = 'mintr124'
        K8S_NAMESPACE = 'default'
    }

    stages {
        stage('Checkout Code') {
            steps {
                script {
                    echo "Checking out branch '${env.GIT_BRANCH}' for SCM"
                    checkout([
                        $class: 'GitSCM',
                        branches: [[name: "*/${env.GIT_BRANCH}"]],
                        doGenerateSubmoduleConfigurations: false,
                        extensions: [[$class: 'CleanBeforeCheckout']], // quan trọng!
                        userRemoteConfigs: [[url: "https://github.com/${env.IMAGE_PREFIX}/spring-petclinic-microservices-ci-cd.git"]]
                    ])
                }
            }
        }

        stage('Determine Build Logic and Tags') {
            steps {
                script {
                    echo "${env.GIT_BRANCH}"
                    def appServices = ['vets-service', 'customers-service', 'visits-service', 'genai-service']
                    def servicesToBuildAndTagsMap = [:]

                    if (env.GIT_BRANCH == 'main') {
                        echo "Main branch detected. Building all application services with tag 'main'."
                        appServices.each { svc ->
                            servicesToBuildAndTagsMap[svc] = 'main'
                        }
                    } else if (env.GIT_BRANCH.startsWith('dev-')) {
                        def changedService = env.GIT_BRANCH - "dev-"

                        if (!appServices.contains(changedService)) {
                            error "The 'dev-' branch '${env.GIT_BRANCH}' does not correspond to a known application service."
                        }

                        def commitId = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                        echo "Detected branch '${env.GIT_BRANCH}'. Building only ${changedService} with tag '${commitId}'."
                        servicesToBuildAndTagsMap[changedService] = commitId
                    } else {
                        error "Invalid branch name: '${env.GIT_BRANCH}'. Please use 'main' or a 'dev-*' branch."
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

                                sh "docker tag community/spring-petclinic-${serviceName}:latest ${fullImageName}"
                                echo "Pushing Docker image ${fullImageName} to Docker Hub..."
                                sh "docker push ${fullImageName}"
                            }
                            echo "${serviceName} built and pushed with tag: ${buildTag}"
                        }
                        sh "docker logout"
                        echo "All relevant service images built and pushed."
                    }
                    def jsonString = JsonOutput.toJson(servicesToBuildAndTagsMap)
                    writeFile file: 'servicesToBuildAndTags.json', text: jsonString
                    stash name: 'servicesToBuildAndTags', includes: 'servicesToBuildAndTags.json'
                }
            }
        }

        stage('Deploy to Kubernetes with Helm') {
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
                        echo "No deployment information found. Skipping deployment."
                        return
                    }

                    def foundational = ['config-server', 'discovery-server', 'api-gateway']
                    foundational.each { svc ->
                        servicesToBuildAndTags[svc] = "main" // Luôn gán tag là 'main' cho các dịch vụ này
                        echo "Deployment: Using 'main' tag for foundational service '${svc}'."
                    }

                    // Triển khai các dịch vụ nền tảng trước
                    def foundationalOrder = ['config-server', 'discovery-server', 'api-gateway']
                    foundationalOrder.each { svc ->
                        if (servicesToBuildAndTags.containsKey(svc)) {
                            def tag = servicesToBuildAndTags[svc] // Sẽ là 'main' do logic ở stage trước
                            def chartPath = "charts/${svc}"
                            echo "Deploying foundational service ${svc} with tag ${tag}..."
                            sh """
                                helm upgrade --install ${svc} ${chartPath} \\
                                    --namespace ${env.K8S_NAMESPACE} \\
                                    --set image.repository=${env.IMAGE_PREFIX}/${svc} \\
                                    --set image.tag=${tag} \\
                                    --set image.pullPolicy=Always \\
                                    --wait
                            """
                        }
                    }

                    def appServices = ['vets-service', 'customers-service', 'visits-service', 'genai-service']
                    appServices.each { svc ->
                        if (!servicesToBuildAndTags.containsKey(svc)) {
                            servicesToBuildAndTags[svc] = "main" 
                            echo "Deployment: Using 'main' tag for foundational service '${svc}'."
                        }
                    }

                    echo "Waiting 15 seconds for foundational services to stabilize..."
                    sh "sleep 15"

                    // Triển khai các dịch vụ ứng dụng
                    
                    appServices.each { svc ->
                        if (servicesToBuildAndTags.containsKey(svc)) {
                            def tag = servicesToBuildAndTags[svc]
                            if (tag != "main") {
                                tag = tag.replaceAll(/[\r\n]+/, '').trim()
                            }
                            def chartPath = "charts/${svc}"
                            echo "Deploying foundational service ${svc} with tag ${tag}..."
                            def helmCommand = "helm upgrade --install ${svc} ${chartPath} " +
                                              "--namespace ${env.K8S_NAMESPACE} " +
                                              "--set image.repository=${env.IMAGE_PREFIX}/${svc} " +
                                              "--set image.tag=${tag} " +
                                              "--set image.pullPolicy=Always " +
                                              "--wait"
                            echo "Deploying application service ${svc} with tag ${tag}..."
                            echo "Executing Helm command: ${helmCommand}"
                            sh helmCommand
                        }
                    }
                    echo "All services deployed successfully."
                }
            }
        }
    }
}
