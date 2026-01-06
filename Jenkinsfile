pipeline {
  agent any

  options {
    timestamps()
    skipDefaultCheckout(true)
  }

  environment {
    // docker compose v2 varsa bunu kullanacağız, yoksa docker-compose'a düşeceğiz
    COMPOSE = 'docker compose'
  }

  stages {

    stage('Clean Workspace') {
      steps {
        deleteDir()
      }
    }

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
        sh './mvnw -B test'
      }
      post {
        always {
          junit '**/target/surefire-reports/*.xml'
        }
      }
    }

    stage('Integration Tests') {
      steps {
        sh './mvnw -B failsafe:integration-test failsafe:verify'
      }
      post {
        always {
          junit '**/target/failsafe-reports/*.xml'
        }
      }
    }

   stage('Docker Up (Compose)') {
     steps {
       sh '''
         set -e

         if docker compose version >/dev/null 2>&1; then
           COMPOSE="docker compose"
         else
           COMPOSE="docker-compose"
         fi
         echo "Using compose command: $COMPOSE"

         echo "Stopping any previous compose stack..."
         $COMPOSE down -v --remove-orphans || true

         echo "Starting compose..."
         $COMPOSE up -d --build

         sleep 2

         echo "Waiting for Docker HEALTHY (or RUNNING if no healthcheck)..."
         for i in $(seq 1 60); do
           health="$(docker inspect -f '{{if .State.Health}}{{.State.Health.Status}}{{else}}no-healthcheck{{end}}' bilet-app 2>/dev/null || true)"
           state="$(docker inspect -f '{{.State.Status}}' bilet-app 2>/dev/null || true)"

           health="${health:-missing}"
           state="${state:-missing}"

           echo "bilet-app state: $state | health: $health"

           if [ "$health" = "healthy" ]; then
             echo "App is healthy ✅"
             $COMPOSE ps || true
             exit 0
           fi

           if [ "$health" = "no-healthcheck" ] && [ "$state" = "running" ]; then
             echo "App is running (no healthcheck) ✅"
             $COMPOSE ps || true
             exit 0
           fi

           if [ "$state" = "exited" ] || [ "$state" = "dead" ]; then
             echo "App container is not running ❌"
             break
           fi

           sleep 2
         done

         echo "App did not become ready ❌"
         $COMPOSE ps || true
         $COMPOSE logs --tail=200 || true
         exit 1
       '''
     }
   }

  }

  post {
    failure {
      sh '''
        if docker compose version >/dev/null 2>&1; then
          COMPOSE="docker compose"
        else
          COMPOSE="docker-compose"
        fi
        echo "=== compose ps ==="
        $COMPOSE ps || true
        echo "=== compose logs (tail) ==="
        $COMPOSE logs --tail=200 || true
      '''
    }
    always {
      sh '''
        if docker compose version >/dev/null 2>&1; then
          COMPOSE="docker compose"
        else
          COMPOSE="docker-compose"
        fi
        $COMPOSE down -v --remove-orphans || true
      '''
    }
  }
}
