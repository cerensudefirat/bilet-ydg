pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps { checkout scm }
        }
        stage('Build') {
            steps { sh 'mvn -B -DskipTests clean package' }
        }
        stage('Unit Tests') {
            steps { sh 'mvn -B test -DskipITs' }
        }
        stage('Integration Tests') {
            steps { sh 'mvn -B verify -DskipUnitTests' }
        }
        stage('Docker Build & Compose') {
            steps {
                sh 'docker build -t bilet-app .'
                sh 'docker compose up -d --build'
            }
        }
        stage('Selenium E2E') {
            steps {
                // In Jenkins, run the Python selenium package provided in tests/selenium
                sh 'python -m pip install -r tests/selenium/requirements.txt'
                sh 'python tests/selenium/test_e2e.py'
            }
        }
    }
}

