pipeline {
  agent any

  options {
    timestamps()
    skipDefaultCheckout(true)
  }

  environment {
    // Aynı makinede başka job'lar da koşarsa çakışmayı azaltır
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
          # Docker Compose komutunu belirle
          COMPOSE_CMD=$(docker compose version >/dev/null 2>&1 && echo "docker compose" || echo "docker-compose")

          echo "=== CI Ön Temizlik: İsim çakışmalarını engelle ==="
          # docker-compose.yml içinde container_name varsa bunlar şart (Conflict hatasını keser)
          docker rm -f bilet-db || true
          docker rm -f bilet-selenium || true
          docker rm -f bilet-app || true

          echo "=== Eski compose kalıntıları temizleniyor ==="
          $COMPOSE_CMD down -v --remove-orphans || true

          echo "=== Yeni servisler ayağa kaldırılıyor ==="
          $COMPOSE_CMD up -d --build

          echo "Servislerin (App, DB, Selenium) hazır olması bekleniyor..."
          sleep 20

          echo "=== Çalışan containerlar ==="
          docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" || true
        '''
      }
    }

    stage('Uçtan Uca Testler (Selenium E2E)') {
      steps {
        sh '''
          set -e
          COMPOSE_CMD=$(docker compose version >/dev/null 2>&1 && echo "docker compose" || echo "docker-compose")

          # Tüm IT (Integration Test) sınıflarını (Senaryo 1-7) Docker ağında koşturur
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
