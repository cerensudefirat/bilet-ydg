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
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class Senaryo7StokIT {




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
            options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
            driver = new RemoteWebDriver(new URL(remoteUrl), options);
        } else {
            driver = new org.openqa.selenium.chrome.ChromeDriver(options);
        }
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    @AfterEach
    void tearDown() { if (driver != null) driver.quit(); }

    @Test
    void sc7_adminInventoryControl() {
        driver.get(baseUrl() + "/ui/senaryo7.html");
        WebElement baseUrlInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("baseUrl")));
        baseUrlInput.clear();
        baseUrlInput.sendKeys(baseUrl());

        driver.findElement(By.id("btnSc7")).click();
        WebElement badge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("resultBadge")));
        wait.until(d -> badge.getText().contains("PASS") || badge.getText().contains("FAIL"));
        assertEquals("RESULT: PASS", badge.getText().trim());
    }
}