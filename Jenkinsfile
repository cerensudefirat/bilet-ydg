pipeline {
  agent any

  options {
    timestamps()
    skipDefaultCheckout(true)
    disableConcurrentBuilds()
  }

  environment {
    COMPOSE_PROJECT_NAME = "bilet-ydg"
    COMPOSE_CMD = "docker compose"
    APP_HOST = "bilet-app"
    SELENIUM_HOST = "bilet-selenium"
  }

  stages {

    stage('Çalışma Alanı Temizliği') {
      steps { deleteDir() }
    }

    stage('Kodların Çekilmesi') {
      steps { checkout scm }
    }

    stage('Build (Testsiz Paketleme)') {
      steps {
        sh '''
          set -e
          chmod +x mvnw
          ./mvnw -B clean package -DskipTests
        '''
      }
    }

    stage('Birim Testler (Unit / Surefire)') {
      steps {
        sh '''
          set -e
          ./mvnw -B test
        '''
      }
      post {
        always { junit '**/target/surefire-reports/*.xml' }
      }
    }

    stage('Entegrasyon Testler (IT / Failsafe)') {
      steps {
        sh '''
          set -e
          # Eğer integration testlerin ayrı profile/flag istiyorsa burada ekle
          ./mvnw -B failsafe:integration-test failsafe:verify -DskipUTs=true
        '''
      }
      post {
        always { junit '**/target/failsafe-reports/*.xml' }
      }
    }

    stage('Docker Ortamının Başlatılması') {
      steps {
        sh '''
          set +e
          $COMPOSE_CMD -p "$COMPOSE_PROJECT_NAME" down -v --remove-orphans || true
          docker rm -f bilet-app bilet-db bilet-selenium bilet-e2e || true
          set -e

          $COMPOSE_CMD -p "$COMPOSE_PROJECT_NAME" up -d --build

          # Jenkins container'ı network'e bağla (container isimleriyle erişim için)
          docker network connect "${COMPOSE_PROJECT_NAME}_bilet-network" jenkins-server || true
        '''
      }
    }

    // -----------------------------
    // Selenium E2E - Daha çok stage
    // -----------------------------
    stage('E2E - Ön Kontroller') {
      steps {
        sh '''
          set -e
          echo "=== Docker ps ==="
          docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

          echo "=== App health check (container içi) ==="
          # App içinde actuator varsa burayı /actuator/health yapabilirsin
          docker exec -i ${APP_HOST} sh -lc "wget -qO- http://localhost:8080/actuator/health || true"

          echo "=== Selenium status ==="
          docker exec -i ${SELENIUM_HOST} sh -lc "wget -qO- http://localhost:4444/status || true"
        '''
      }
    }

    stage('E2E - Testleri Çalıştır (Selenium)') {
      steps {
        sh '''
          set -e
          echo "=== E2E Testleri Başlıyor ==="

          ./mvnw -B failsafe:integration-test failsafe:verify \
            -Dselenium.remoteUrl=http://bilet-selenium:4444/wd/hub \
            -De2e.baseUrl=http://bilet-app:8080 \
            -Dotel.sdk.disabled=true
        '''
      }
      post {
        always { junit '**/target/failsafe-reports/*.xml' }
      }
    }

    stage('E2E - Log / Artefact Toplama') {
      steps {
        sh '''
          set +e
          echo "=== Son durum (ps) ==="
          docker ps -a --format "table {{.Names}}\t{{.Status}}"

          echo "=== bilet-app logs (tail) ==="
          docker logs --tail=200 ${APP_HOST} || true

          echo "=== bilet-selenium logs (tail) ==="
          docker logs --tail=200 ${SELENIUM_HOST} || true
        '''
      }
    }
  }

  post {
    failure {
      sh '''
        set +e
        echo "=== HATA ANALİZİ ==="
        docker ps -a --format "table {{.Names}}\t{{.Status}}"
        docker logs bilet-app || true
      '''
    }
    always {
      echo "Analiz için konteynerlar açık bırakıldı."
    }
  }
}
