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
public class Senaryo6ProfilIT {

    @LocalServerPort private int port;
    private WebDriver driver;
    private WebDriverWait wait;

    private String baseUrl() {
        String prop = System.getProperty("e2e.baseUrl");
        if (prop != null && !prop.isBlank()) return prop.replaceAll("/$", "");
        String env = System.getenv("E2E_BASE_URL");
        if (env != null && !env.isBlank()) return env.replaceAll("/$", "");
        return "http://localhost:" + port;
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
        wait = new WebDriverWait(driver, Duration.ofSeconds(25));
    }

    @AfterEach
    void tearDown() { if (driver != null) driver.quit(); }

    @Test
    void sc6_userShouldSeePurchasedTicketsInProfile() {
        driver.get(baseUrl() + "/ui/senaryo6.html");

        WebElement baseUrlInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("baseUrl")));
        baseUrlInput.clear();
        baseUrlInput.sendKeys(baseUrl());

        driver.findElement(By.id("btnSc6")).click();

        WebElement badge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("resultBadge")));
        wait.until(d -> badge.getText().contains("PASS") || badge.getText().contains("FAIL"));

        assertEquals("RESULT: PASS", badge.getText().trim());
    }
}