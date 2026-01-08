pipeline {
  agent any

  options {
    timestamps()
    skipDefaultCheckout(true)
    disableConcurrentBuilds()
  }

  environment {
    COMPOSE_PROJECT_NAME = "bilet-ydg-ci"
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
          set -e
          echo "=== DEBUG: workspace ==="
          pwd
          ls -la
          echo "=== DEBUG: compose dosyası var mı? ==="
          ls -la docker-compose.yml

          echo "=== DEBUG: docker/compose version ==="
          docker --version
          docker compose version

          echo "=== Eski compose kalıntıları temizleniyor ==="
          docker compose -p "$COMPOSE_PROJECT_NAME" down -v --remove-orphans || true

          echo "=== Yeni servisler ayağa kaldırılıyor ==="
          docker compose -p "$COMPOSE_PROJECT_NAME" up -d --build

          echo "=== Containerlar ==="
          docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" || true

          echo "=== Compose ps ==="
          docker compose -p "$COMPOSE_PROJECT_NAME" ps || true
        '''
      }
    }

    stage('Uçtan Uca Testler (Selenium E2E)') {
      steps {
        sh '''
          set -e
          echo "=== Jenkins workspace ==="
          pwd
          ls -la

          echo "=== E2E: workspace mount kontrol ==="
          docker compose -p "$COMPOSE_PROJECT_NAME" run --rm \
            -v "$PWD:/workspace" -w /workspace \
            e2e bash -lc "pwd && ls -la && test -f pom.xml"

          echo "=== E2E testleri (mvn) ==="
          docker compose -p "$COMPOSE_PROJECT_NAME" run --rm \
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
        echo "=== Docker ps (failure) ==="
        docker ps -a --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" || true

        echo "=== COMPOSE LOGS (failure) ==="
        docker compose -p "$COMPOSE_PROJECT_NAME" logs --tail=200 || true
      '''
    }

    always {
      sh '''
        set +e
        echo "=== Ortam Temizliği ==="
        docker compose -p "$COMPOSE_PROJECT_NAME" down -v --remove-orphans || true
      '''
    }
  }
}
