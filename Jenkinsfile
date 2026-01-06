pipeline {
  agent any

  options {
    timestamps()
    skipDefaultCheckout(true)
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build') {
      steps {
        sh 'chmod +x mvnw || true'
        sh './mvnw -B -DskipTests clean package'
      }
    }

    stage('Unit Tests') {
      steps {
        sh 'chmod +x mvnw || true'
        sh './mvnw -B test'
      }
      post {
        always {
          junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
        }
      }
    }

    stage('Integration Tests') {
      steps {
        sh 'chmod +x mvnw || true'
        sh './mvnw -B failsafe:integration-test failsafe:verify'
      }
      post {
        always {
          junit testResults: '**/target/failsafe-reports/*.xml', allowEmptyResults: true
        }
      }
    }

    stage('Docker Up (Compose)') {
      steps {
        sh '''
          docker-compose --version

          docker-compose down -v || true
          docker-compose up -d --build

          echo "Waiting for app health..."
          for i in $(seq 1 30); do
            if curl -fsS http://localhost:8080/actuator/health | grep -q UP; then
              echo "App is UP"
              exit 0
            fi
            sleep 2
          done

          echo "App did not become healthy in time"
          docker-compose ps
          docker-compose logs --tail=200
          exit 1
        '''
      }
    }
  }

  post {
    always {
      sh 'docker-compose down -v || true'
    }
  }
}
