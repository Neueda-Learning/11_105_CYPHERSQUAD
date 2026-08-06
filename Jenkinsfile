pipeline {
  agent any

  options {
    timestamps()
    disableConcurrentBuilds()
  }

  environment {
    GIT_URL = 'https://github.com/Neueda-Learning/11_105_CYPHERSQUAD.git'
    BRANCH = 'main'
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
            docker run --rm \
              -v "$PWD:/app" \
              -w /app \
              node:22-alpine \
              sh -c 'node -v && npm -v && if [ -f package-lock.json ]; then npm ci; else npm install; fi && npm run build'
          '''
        }
      }
    }

    stage('Docker Build') {
      steps {
        sh 'docker-compose build'
      }
    }

    stage('Deploy From Main Branch') {
      when {
        branch 'main'
      }
      steps {
        sh 'docker-compose up -d --build --remove-orphans'
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