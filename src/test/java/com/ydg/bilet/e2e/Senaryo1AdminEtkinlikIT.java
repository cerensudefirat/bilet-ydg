package com.ydg.bilet.e2e;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class Senaryo1AdminEtkinlikIT {

    @LocalServerPort
    private int port;

    private WebDriver driver;
    private WebDriverWait wait;

    private String baseUrl() {
        String prop = System.getProperty("e2e.baseUrl");
        if (prop != null && !prop.isBlank()) return prop.replaceAll("/$", "");
        String env = System.getenv("E2E_BASE_URL");
        if (env != null && !env.isBlank()) return env.replaceAll("/$", "");
        return "http://localhost:" + port;
    }

    private String adminUser() {
        String env = System.getenv("ADMIN_USER");
        if (env != null && !env.isBlank()) return env;
        return "admin@ydg.com";
    }

    private String adminPass() {
        String env = System.getenv("ADMIN_PASS");
        if (env != null && !env.isBlank()) return env;
        return "admin123";
    }

    @BeforeEach
    void setUp() throws Exception {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage", "--remote-allow-origins=*");

        String remoteUrl = System.getProperty("selenium.remoteUrl");
        if (remoteUrl == null) remoteUrl = System.getenv("SELENIUM_REMOTE_URL");

        if (remoteUrl != null && !remoteUrl.isBlank()) {
            driver = new RemoteWebDriver(new URL(remoteUrl), options);
        } else {
            driver = new org.openqa.selenium.chrome.ChromeDriver(options);
        }
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }


    private void waitForServerReachable() {
        String url = baseUrl() + "/ui/senaryo1.html";
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
        long deadline = System.currentTimeMillis() + 30_000;

        while (System.currentTimeMillis() < deadline) {
            try {
                HttpRequest req = HttpRequest.newBuilder(URI.create(url)).GET().build();
                HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
                if (res.statusCode() == 200) return;
            } catch (Exception ignored) {}
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        }
    }

    private void debugCallMekan() {
        try {
            String creds = adminUser() + ":" + adminPass();
            String token = java.util.Base64.getEncoder()
                    .encodeToString(creds.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            var client = java.net.http.HttpClient.newBuilder().build();
            var req = java.net.http.HttpRequest.newBuilder(java.net.URI.create(baseUrl() + "/api/mekan"))
                    .header("Authorization", "Basic " + token).GET().build();
            var res = client.send(req, java.net.http.HttpResponse.BodyHandlers.ofString());
            System.out.println("DEBUG /api/mekan status=" + res.statusCode());
            System.out.println("DEBUG body=" + res.body());
        } catch (Exception e) {
            System.out.println("DEBUG /api/mekan EXCEPTION: " + e.getMessage());
        }
    }

    @AfterEach
    void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    void sc1_adminCreatesEvent_shouldPass() throws InterruptedException {
        driver.get(baseUrl() + "/ui/senaryo1.html");
        Thread.sleep(2000); // Sayfa açılışını izle

        WebElement baseUrlInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("baseUrl")));
        baseUrlInput.clear();
        baseUrlInput.sendKeys(baseUrl());
        Thread.sleep(1000); // Girişi gör

        WebElement adminUserInput = driver.findElement(By.id("adminUser"));
        WebElement adminPassInput = driver.findElement(By.id("adminPass"));

        adminUserInput.clear();
        adminUserInput.sendKeys(adminUser());
        Thread.sleep(500);

        adminPassInput.clear();
        adminPassInput.sendKeys(adminPass());
        Thread.sleep(2000);

        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(By.id("btnSc1")));
        btn.click();

        WebElement status = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("status")));
        WebElement badge = driver.findElement(By.id("resultBadge"));
        WebElement log = driver.findElement(By.id("log"));

        try {
            wait.until(d -> {
                String t = badge.getText().trim();
                return t.equalsIgnoreCase("RESULT: PASS") || t.equalsIgnoreCase("RESULT: FAIL");
            });
            Thread.sleep(5000);
        } catch (TimeoutException te) {
            dumpUiState("timeout_no_result");
            fail("SC1 sonucu gelmedi.");
        }

        String result = badge.getText().trim();
        if (!result.equalsIgnoreCase("RESULT: PASS")) {
            dumpUiState("result_fail");
            fail("SC1 FAIL çıktı. Log: " + log.getText());
        }

        assertEquals("RESULT: PASS", result);
    }

    private void dumpUiState(String tag) {
        try {
            File png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            File out = new File("target/e2e-" + tag + ".png");
            out.getParentFile().mkdirs();
            java.nio.file.Files.copy(png.toPath(), out.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception ignored) {}
    }
}