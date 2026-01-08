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
    // Konteyner isimlerini yml dosyanla eşleştiriyoruz
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

    stage('Paketleme ve Birim Testler') {
      steps {
        sh '''
          set -e
          chmod +x mvnw
          ./mvnw -B clean package
        '''
      }
      post {
        always { junit '**/target/surefire-reports/*.xml' }
      }
    }

    stage('Docker Ortamının Başlatılması') {
      steps {
        sh '''
          set +e
          $COMPOSE_CMD -p "$COMPOSE_PROJECT_NAME" down -v --remove-orphans || true
          docker rm -f bilet-app bilet-db bilet-selenium || true

          set -e
          $COMPOSE_CMD -p "$COMPOSE_PROJECT_NAME" up -d --build

          # KRİTİK ADIM: Jenkins'i Docker ağına bağla (Böylece konteynerlara isimle ulaşabilir)
          docker network connect "${COMPOSE_PROJECT_NAME}_bilet-network" jenkins-server || true
        '''
      }
    }

    stage('Uçtan Uca Testler (Selenium E2E)') {
              steps {
                sh '''
                  set -e
                  echo "=== E2E Testleri Başlıyor ==="

                  # OpenTelemetry hatasını engellemek için -Dotel.sdk.disabled=true eklendi
                  ./mvnw -B failsafe:integration-test failsafe:verify \
                    -Dselenium.remoteUrl=http://bilet-selenium:4444/wd/hub \
                    -De2e.baseUrl=http://bilet-app:8080 \
                    -Dotel.sdk.disabled=true
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
        set +e
        echo "=== HATA ANALİZİ ==="
        docker ps -a --format "table {{.Names}}\t{{.Status}}"
        docker logs bilet-app
      '''
    }
    always {
        echo "Analiz için konteynerlar açık bırakıldı."
        // sh '$COMPOSE_CMD -p "$COMPOSE_PROJECT_NAME" down -v || true'
    }
  }
}