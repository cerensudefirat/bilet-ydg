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

import java.net.URL;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class Senaryo3BiletAlIT {

    @LocalServerPort private int port;
    private WebDriver driver;
    private WebDriverWait wait;

    // Docker ağı için dinamik URL (app:8080)
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

        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--remote-allow-origins=*");

        if (remoteUrl != null && !remoteUrl.isBlank()) {
            // Docker ortamında RemoteWebDriver kullan
            driver = new RemoteWebDriver(new URL(remoteUrl), options);
        } else {
            // Yerel ortamda standart ChromeDriver kullan
            driver = new org.openqa.selenium.chrome.ChromeDriver(options);
        }

        wait = new WebDriverWait(driver, Duration.ofSeconds(25));
    }

    @AfterEach
    void tearDown() { if (driver != null) driver.quit(); }

    @Test
    void sc3_userBuysTicket_shouldPass() {
        // baseUrl() metodunu kullanarak dinamik adrese gidiyoruz
        driver.get(baseUrl() + "/ui/senaryo3.html");

        WebElement baseUrlInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("baseUrl")));
        baseUrlInput.clear();
        baseUrlInput.sendKeys(baseUrl());

        driver.findElement(By.id("btnSc3")).click();

        WebElement badge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("resultBadge")));
        wait.until(d -> badge.getText().contains("PASS") || badge.getText().contains("FAIL"));

        if (badge.getText().contains("FAIL")) {
            System.out.println("❌ HATA DETAYI: " + driver.findElement(By.id("log")).getText());
        }

        assertEquals("RESULT: PASS", badge.getText().trim());
    }
}