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
        sh './mvnw -B -DskipTests=false failsafe:integration-test failsafe:verify'
      }
      post {
        always {
          junit testResults: '**/target/failsafe-reports/*.xml', allowEmptyResults: true
        }
      }
    }
  }
}
