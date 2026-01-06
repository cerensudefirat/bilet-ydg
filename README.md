Bilet Satış Projesi — Kısa Kullanım ve Test Rehberi

Özet
- Basit etkinlik/bilet satış uygulaması (Spring Boot, JPA, H2/Postgres).
- Özellikler: kullanıcı kayıt/giriş, etkinlik listeleme/filtreleme, bilet satın alma, kullanıcıya ait biletler.
- Testler: birim testleri (JUnit/Mockito), entegrasyon testleri (SpringBootTest + H2), Selenium örnekleri (Java testi disabled; Python e2e script mevcut).
- CI: `Jenkinsfile` örneği eklendi. Docker + docker-compose yapılandırması var.

Projeyi Çalıştırma (Local JVM)
1) Java 17 yüklü olduğundan emin olun.
2) Proje kökünde aşağıdaki komutları çalıştırın (Windows):

```cmd
:: Birim testleri çalıştır (IT atla)
.\mvnw.cmd -DskipITs test

:: Entegrasyon testlerini çalıştır (birim testleri de çalışır)
.\mvnw.cmd verify

:: Uygulamayı çalıştır
.\mvnw.cmd spring-boot:run
```

Test Profili
- Entegrasyon testlerinde `src/test/resources/application-test.properties` kullanılır; H2 bellek veritabanı kullanılır.

Önemli Dosyalar (kısa)
- `src/main/java/com/ydg/bilet/controller` — `AuthController`, `EtkinlikController`, `BiletController`
- `src/main/java/com/ydg/bilet/service` — `AuthService`, `EtkinlikService`, `BiletService`, `QrGenerator`
- `src/main/java/com/ydg/bilet/entity` — domain sınıfları (Etkinlik, Bilet, Kategori, Mekan, Kullanici, vs.)
- `src/test/java/com/ydg/bilet` — birim testleri (`AuthServiceTest`), entegrasyon testi (`integration/EtkinlikIT`), Selenium (Java) test örneği `selenium/SeleniumE2ETest.java` (devre dışı), Python e2e script `tests/selenium/test_e2e.py`.
- `pom.xml` — test bağımlılıkları (H2, Mockito, Selenium, WebDriverManager)
- `Jenkinsfile` — örnek pipeline (checkout, build, unit, integration, docker, selenium)
- `Dockerfile`, `docker-compose.yml` — docker ile çalıştırma

Docker ile çalıştırma (örnek)
```cmd
:: Docker ve docker-compose yüklü olmalı
docker compose up -d --build
```
Bu `docker-compose.yml` Postgres + uygulama servisini ayağa kaldırır.

Selenium / E2E
- Jenkins ortamında Selenium/Chrome driver sağlanabiliyorsa `selenium` testleri pipeline'da çalıştırılabilir.
- Local olarak örnek Python E2E script'i kullanabilirsiniz (Chrome yüklü ve uygun driver varsa):

```cmd
python -m pip install -r tests/selenium/requirements.txt
python tests/selenium/test_e2e.py
```

Notlar ve İlerleme Önerileri
- Şu anda proje halihazırda temel API'leri ve testleri içerir. İsterseniz:
  - Auth için JWT desteği ekleyip endpointleri güvenli hale getirebilirim.
  - Daha fazla birim / entegrasyon testi ekleyebilirim (bilet satın alma happy path + edge cases).
  - Jenkins pipeline'ı sizin Jenkins'e göre özelleştirip, Selenium stage'lerini parça parça ekleyebilirim.

İsterseniz şimdi hangi adımı priorite etmemi istersiniz? (API genişletme, JWT auth, daha çok test, Jenkins özelleştirme vb.)

