pipeline {
  agent any

  options {
    timestamps()
    skipDefaultCheckout(true)
    disableConcurrentBuilds() // aynı job aynı anda 2 kez çalışmasın
  }

  environment {
    COMPOSE_PROJECT_NAME = "bilet-ydg-ci"
  }

  stages {
    stage('Çalışma Alanı Temizliği') {
      steps { deleteDir() }
    }

    stage('Kodların Çekilmesi') {
      steps { checkout scm }
    }

    stage('Paketleme (Build)') {
      steps {
        sh 'chmod +x mvnw || true'
        sh './mvnw -B -DskipTests clean package'
      }
    }

    stage('Birim Testler (Unit Tests)') {
      steps {
        sh './mvnw -B test'
      }
      post {
        always {
          junit '**/target/surefire-reports/*.xml'
        }
      }
    }

    stage('Docker Ortamının Başlatılması') {
      steps {
        sh '''
          set -e
          COMPOSE_CMD=$(docker compose version >/dev/null 2>&1 && echo "docker compose" || echo "docker-compose")

          echo "=== Eski compose kalıntıları temizleniyor ==="
          $COMPOSE_CMD down -v --remove-orphans || true

          echo "=== Yeni servisler ayağa kaldırılıyor ==="
          $COMPOSE_CMD up -d --build

          echo "=== Containerlar ==="
          docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" || true

          echo "=== Sağlık kontrolleri (app/selenium) ==="
          # app health
          for i in $(seq 1 60); do
            if $COMPOSE_CMD exec -T app sh -lc "curl -fsS http://localhost:8080/actuator/health >/dev/null"; then
              echo "APP OK"
              break
            fi
            echo "APP bekleniyor... ($i)"
            sleep 2
          done

          # selenium health
          for i in $(seq 1 60); do
            if $COMPOSE_CMD exec -T selenium sh -lc "curl -fsS http://localhost:4444/wd/hub/status >/dev/null || curl -fsS http://localhost:4444/status >/dev/null"; then
              echo "SELENIUM OK"
              break
            fi
            echo "SELENIUM bekleniyor... ($i)"
            sleep 2
          done
        '''
      }
    }

    stage('Uçtan Uca Testler (Selenium E2E)') {
      steps {
        sh '''
          set -e
          COMPOSE_CMD=$(docker compose version >/dev/null 2>&1 && echo "docker compose" || echo "docker-compose")

          $COMPOSE_CMD run --rm \
            -e E2E_BASE_URL=http://app:8080 \
            -e SELENIUM_REMOTE_URL=http://selenium:4444/wd/hub \
            e2e bash -lc "./mvnw -B failsafe:integration-test failsafe:verify \
            -Dselenium.remoteUrl=http://selenium:4444/wd/hub \
            -De2e.baseUrl=http://app:8080"
        '''
      }
      post {
        always {
          junit '**/target/failsafe-reports/*.xml'
        }
      }
    }
  }

  post {
    failure {
      sh '''
        COMPOSE_CMD=$(docker compose version >/dev/null 2>&1 && echo "docker compose" || echo "docker-compose")
        echo "=== Hata Analizi: Konteyner Logları ==="
        $COMPOSE_CMD logs --tail=200 || true
      '''
    }
    always {
      sh '''
        COMPOSE_CMD=$(docker compose version >/dev/null 2>&1 && echo "docker compose" || echo "docker-compose")
        echo "=== Ortam Temizliği ==="
        $COMPOSE_CMD down -v --remove-orphans || true
      '''
    }
  }
}
