pipeline {
  agent any

  options {
    timestamps()
    skipDefaultCheckout(true)
    disableConcurrentBuilds()
  }

  environment {
    COMPOSE_PROJECT_NAME = "bilet-ydg-ci"
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
        sh '''#!/usr/bin/env bash
set -e
COMPOSE_CMD=$(docker compose version >/dev/null 2>&1 && echo "docker compose" || echo "docker-compose")

echo "=== Eski compose kalıntıları temizleniyor ==="
$COMPOSE_CMD down -v --remove-orphans || true

echo "=== Yeni servisler ayağa kaldırılıyor ==="
$COMPOSE_CMD up -d --build

echo "=== Containerlar ==="
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" || true
'''
      }
    }

    stage('Uçtan Uca Testler (Selenium E2E)') {
      steps {
        sh '''#!/usr/bin/env bash
set -e
COMPOSE_CMD=$(docker compose version >/dev/null 2>&1 && echo "docker compose" || echo "docker-compose")

echo "=== E2E container içinde workspace kontrol ==="
$COMPOSE_CMD run --rm e2e bash -lc "pwd && ls -la"

echo "=== E2E testleri çalıştırılıyor (mvn) ==="
$COMPOSE_CMD run --rm \
  -e E2E_BASE_URL=http://app:8080 \
  -e SELENIUM_REMOTE_URL=http://selenium:4444/wd/hub \
  e2e bash -lc "mvn -B failsafe:integration-test failsafe:verify \
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
      sh '''#!/usr/bin/env bash
COMPOSE_CMD=$(docker compose version >/dev/null 2>&1 && echo "docker compose" || echo "docker-compose")

echo "=== APP LOGS ==="
docker logs --tail=200 bilet-ydg-ci-app-1 || true

echo "=== DB LOGS ==="
docker logs --tail=200 bilet-ydg-ci-db-1 || true

echo "=== SELENIUM LOGS ==="
docker logs --tail=200 bilet-ydg-ci-selenium-1 || true

echo "=== COMPOSE LOGS (GENEL) ==="
$COMPOSE_CMD logs --tail=200 || true
'''
    }

    always {
      sh '''#!/usr/bin/env bash
COMPOSE_CMD=$(docker compose version >/dev/null 2>&1 && echo "docker compose" || echo "docker-compose")
echo "=== Ortam Temizliği ==="
$COMPOSE_CMD down -v --remove-orphans || true
'''
    }
  }
}
