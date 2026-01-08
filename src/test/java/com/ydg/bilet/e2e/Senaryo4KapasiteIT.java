package com.ydg.bilet.e2e;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class Senaryo4KapasiteIT {

    @LocalServerPort private int port;
    private WebDriver driver;
    private WebDriverWait wait;

    private String baseUrl() {
        String env = System.getenv("E2E_BASE_URL");
        return (env != null && !env.isBlank()) ? env.replaceAll("/$", "") : "http://localhost:" + port;
    }

    @BeforeEach
    void setUp() throws Exception {
        ChromeOptions options = new ChromeOptions();
        String remoteUrl = System.getenv("SELENIUM_REMOTE_URL");

        if (remoteUrl != null && !remoteUrl.isBlank()) {
            options.addArguments("--headless=new");
            options.addArguments("--no-sandbox", "--disable-dev-shm-usage");
            driver = new RemoteWebDriver(new URL(remoteUrl), options);
        } else {
            driver = new ChromeDriver(options);
        }
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    @Test
    void sc4_capacityOverload_shouldShowError() throws Exception {
        driver.get(baseUrl() + "/ui/senaryo4.html");
        WebElement baseUrlInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("baseUrl")));
        baseUrlInput.clear();
        baseUrlInput.sendKeys(baseUrl());

        driver.findElement(By.id("btnSc4")).click();
        WebElement badge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("resultBadge")));
        wait.until(d -> badge.getText().contains("PASS") || badge.getText().contains("FAIL"));

        assertEquals("RESULT: PASS", badge.getText().trim());
    }
    // ... tearDown ve diagnostic metodları aynen kalabilir
}