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

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class Senaryo2AdminRequestIT {

    @LocalServerPort
    private int port;
    private WebDriver driver;
    private WebDriverWait wait;

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    // Docker ortamında konteynerlar birbirini isimle (app) tanır
    private String baseUrl() {
        String env = System.getenv("E2E_BASE_URL");
        if (env != null && !env.isBlank()) return env.replaceAll("/$", "");
        return "http://localhost:" + port;
    }





    @BeforeEach
    void setUp() throws Exception {
        ChromeOptions options = new ChromeOptions();

        // Jenkins/Docker ortamında HEADLESS zorunludur
        String remoteUrl = System.getenv("SELENIUM_REMOTE_URL");
        if (remoteUrl != null && !remoteUrl.isBlank()) {
            options.addArguments("--headless=new");
        }

        options.addArguments(
                "--disable-gpu",
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-extensions",
                "--disable-popup-blocking",
                "--remote-allow-origins=*",
                "--disable-blink-features=AutomationControlled"
        );

        if (remoteUrl != null && !remoteUrl.isBlank()) {
            // Jenkins: selenium konteynerine bağlan
            driver = new RemoteWebDriver(new URL(remoteUrl), options);
        } else {
            // Local: Kendi bilgisayarındaki ChromeDriver'ı kullan
            driver = new org.openqa.selenium.chrome.ChromeDriver(options);
        }

        wait = new WebDriverWait(driver, Duration.ofSeconds(35));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    void sc2_userRequestsAdminRole_shouldPass() throws Exception {
        // baseUrl() kullanarak dinamik URL yönetimi sağlıyoruz
        driver.get(baseUrl() + "/ui/senaryo2.html");

        WebElement baseUrlInput = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("baseUrl"))
        );
        baseUrlInput.clear();
        baseUrlInput.sendKeys(baseUrl());

        driver.findElement(By.id("btnSc2")).click();

        WebElement badge = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("resultBadge"))
        );

        wait.until(d ->
                badge.getText().contains("PASS") ||
                        badge.getText().contains("FAIL")
        );

        String result = badge.getText().trim();
        String uiLog = safeGetText(By.id("log"));

        if (result.contains("FAIL")) {
            dumpArtifacts("senaryo2", uiLog);
            System.out.println("\n--- SENARYO 2 UI LOG (FAIL) ---");
            System.out.println(uiLog);
            System.out.println("--------------------------------\n");
        }

        assertEquals("RESULT: PASS", result);
    }

    private String safeGetText(By locator) {
        try {
            return driver.findElement(locator).getText();
        } catch (Exception e) {
            return "(log okunamadı: " + e.getClass().getSimpleName() + " " + e.getMessage() + ")";
        }
    }

    private void dumpArtifacts(String prefix, String uiLog) {
        try {
            Path dir = Paths.get("target", "failsafe-reports");
            Files.createDirectories(dir);
            String stamp = LocalDateTime.now().format(TS);

            Files.writeString(dir.resolve(prefix + "_" + stamp + "_ui.log.txt"), uiLog, StandardCharsets.UTF_8);
            Files.writeString(dir.resolve(prefix + "_" + stamp + "_page.html"), driver.getPageSource(), StandardCharsets.UTF_8);

            if (driver instanceof TakesScreenshot ts) {
                byte[] png = ts.getScreenshotAs(OutputType.BYTES);
                Files.write(dir.resolve(prefix + "_" + stamp + "_screen.png"), png);
            }
        } catch (IOException ioe) {
            System.out.println("Artifact dump failed: " + ioe.getMessage());
        }
    }
}