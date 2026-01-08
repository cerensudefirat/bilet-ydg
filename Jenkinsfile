pipeline {
  agent any

  options {
    timestamps()
    skipDefaultCheckout(true)
    disableConcurrentBuilds()
  }

  environment {
    // Docker Compose proje adını sabitliyoruz
    COMPOSE_PROJECT_NAME = "bilet-ydg"
    COMPOSE_CMD = "docker compose"
  }

  stages {
    stage('Çalışma Alanı Temizliği') {
      steps {
        deleteDir()
      }
    }

    stage('Kodların Çekilmesi') {
      steps {
        checkout scm
      }
    }

    stage('Paketleme (Build)') {
      steps {
        sh '''
          set -e
          chmod +x mvnw || true
          ./mvnw -B -DskipTests clean package
        '''
      }
    }

    stage('Birim Testler (Unit Tests)') {
      steps {
        sh '''
          set -e
          ./mvnw -B test
        '''
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
          set +e
          echo "=== Eski Kalıntılar Temizleniyor ==="
          # Proje ismiyle eşleşen her şeyi durdur ve sil
          $COMPOSE_CMD -p "$COMPOSE_PROJECT_NAME" down -v --remove-orphans || true

          # İsim çakışması riskine karşı manuel temizlik (garantiye almak için)
          docker rm -f bilet-app bilet-db bilet-selenium bilet-e2e || true

          set -e
          echo "=== Servisler Başlatılıyor ==="
          # Uygulamayı 'test' profiliyle başlatmak test verileri için kritiktir
          $COMPOSE_CMD -p "$COMPOSE_PROJECT_NAME" up -d --build

          echo "=== Konteyner Durumları ==="
          docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
        '''
      }
    }

    stage('Uçtan Uca Testler (Selenium E2E)') {
      steps {
        sh '''
          set -e
          echo "=== E2E Testleri Başlıyor ==="
          $COMPOSE_CMD -p "$COMPOSE_PROJECT_NAME" run --rm \
            -v "$PWD:/workspace" -w /workspace \
            -e E2E_BASE_URL=http://app:8080 \
            -e SELENIUM_REMOTE_URL=http://selenium:4444/wd/hub \
            e2e bash -lc "mvn -B failsafe:integration-test failsafe:verify -Dselenium.remoteUrl=http://selenium:4444/wd/hub -De2e.baseUrl=http://app:8080"
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
        echo "********************************************************"
        echo "!!! HATA TESPİT EDİLDİ: LOGLAR DÖKÜLÜYOR !!!"
        echo "********************************************************"

        echo "=== 1) Konteyner Listesi (Neden çöktü?) ==="
        docker ps -a --format "table {{.Names}}\t{{.Status}}\t{{.ExitCode}}"

        echo "=== 2) Uygulama Logları (Spring Boot Hata Mesajı) ==="
        # Docker Compose proje ismiyle konteyner loguna ulaşır
        docker logs ${COMPOSE_PROJECT_NAME}-app-1 || docker logs bilet-app

        echo "=== 3) Veritabanı Logları ==="
        docker logs ${COMPOSE_PROJECT_NAME}-db-1 || docker logs bilet-db

        echo "********************************************************"
      '''
    }

    /* Debug aşamasında olduğumuz için 'always' içindeki silme komutunu
       kapalı tutmaya devam ediyoruz. Bu sayede hata sonrası terminalden
       'docker logs' bakabilirsin. */
    always {
      echo "İşlem tamamlandı. (Otomatik temizlik devre dışı bırakıldı)"
    }
  }
}