pipeline {
  agent any

  options {
    timestamps()
    disableConcurrentBuilds()
    ansiColor('xterm')
  }

  environment {
    BACKEND_IMAGE = 'cyphersquad-backend'
    FRONTEND_IMAGE = 'cyphersquad-frontend'
    IMAGE_TAG = "${env.BUILD_NUMBER}"
    // Set this in Jenkins if you want Docker push, for example: yourdockerhubuser
    DOCKER_REGISTRY_NAMESPACE = "${env.DOCKER_REGISTRY_NAMESPACE}"
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Backend Tests') {
      steps {
        dir('backend') {
          sh 'chmod +x mvnw || true'
          sh './mvnw -B clean test'
        }
      }
      post {
        always {
          junit allowEmptyResults: true, testResults: 'backend/target/surefire-reports/*.xml'
        }
      }
    }

    stage('Frontend Build') {
      steps {
        dir('frontend/transaction-monitoring-ui') {
          sh '''
            if [ -f package-lock.json ]; then
              npm ci
            else
              npm install
            fi
          '''
          sh 'npm run build'
        }
      }
    }

    stage('Docker Build') {
      steps {
        sh 'docker build -t ${BACKEND_IMAGE}:${IMAGE_TAG} backend'
        sh 'docker build -t ${FRONTEND_IMAGE}:${IMAGE_TAG} frontend/transaction-monitoring-ui'
      }
    }

    stage('Docker Push (main only)') {
      when {
        branch 'main'
        expression { return env.DOCKER_REGISTRY_NAMESPACE?.trim() }
      }
      steps {
        withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
          sh 'echo "${DOCKER_PASS}" | docker login -u "${DOCKER_USER}" --password-stdin'
          sh 'docker tag ${BACKEND_IMAGE}:${IMAGE_TAG} ${DOCKER_REGISTRY_NAMESPACE}/${BACKEND_IMAGE}:${IMAGE_TAG}'
          sh 'docker tag ${FRONTEND_IMAGE}:${IMAGE_TAG} ${DOCKER_REGISTRY_NAMESPACE}/${FRONTEND_IMAGE}:${IMAGE_TAG}'
          sh 'docker push ${DOCKER_REGISTRY_NAMESPACE}/${BACKEND_IMAGE}:${IMAGE_TAG}'
          sh 'docker push ${DOCKER_REGISTRY_NAMESPACE}/${FRONTEND_IMAGE}:${IMAGE_TAG}'
          sh 'docker logout'
        }
      }
    }
  }

  post {
    success {
      echo 'Pipeline completed successfully.'
    }
    failure {
      echo 'Pipeline failed. Check stage logs for details.'
    }
    always {
      cleanWs(deleteDirs: true, notFailBuild: true)
    }
  }
}
