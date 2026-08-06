pipeline {
  agent any

  options {
    timestamps()
    disableConcurrentBuilds()
    ansiColor('xterm')
  }

  environment {
        GIT_URL = 'https://github.com/SumeetWajpe/frauddetectionapp.git'
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
        sh 'docker compose build'
      }
    }

    stage('Deploy From Main Branch') {
      when {
        branch 'main'
      }
      steps {
        sh 'docker compose up -d --build --remove-orphans'
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
