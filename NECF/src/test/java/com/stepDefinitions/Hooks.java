package com.stepDefinitions;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter;
import com.base.BaseClass;
import com.utility.EmailReportSender;
import com.utility.ExtentLogger;
import com.utility.LoggerHelper;
import com.webdrivermanager.DriverManager;

import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

public class Hooks {

    private static final Logger log = LoggerHelper.getLogger(Hooks.class);
    private static final List<String> failedScenarios = new ArrayList<>();
    private static final List<String> failedScreenshots = new ArrayList<>();
    private static final String SCREENSHOT_DIR = "target/ExtentReports/screenshots/";

    @Before
    public void setUp(Scenario scenario) {
        for (String tag : scenario.getSourceTagNames()) {
            if (tag.startsWith("@browser=")) {
                String browser = tag.split("=")[1];
                DriverManager.setDriver(browser);
            }
        }

        WebDriver driver = DriverManager.getDriver();
        if (driver != null) {
            log.info("Driver initialized for scenario: " + scenario.getName());
            ExtentLogger.extentInfo("Driver initialized for scenario: " + scenario.getName());
            driver.manage().window().maximize();
            driver.manage().timeouts().implicitlyWait(java.time.Duration.ofSeconds(10));
            BaseClass.setWait(new WebDriverWait(driver, java.time.Duration.ofSeconds(10)));
        }
    }

    @After
    public void tearDown(Scenario scenario) {

        WebDriver driver = DriverManager.getDriver();
        if (driver == null)
            return;

        try {
            String scenarioName = scenario.getName().replaceAll("[^a-zA-Z0-9]", "_");
            Path screenshotPath = Paths.get(SCREENSHOT_DIR, scenarioName + ".png");
            Files.createDirectories(screenshotPath.getParent());

            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(screenshot.toPath(), screenshotPath, StandardCopyOption.REPLACE_EXISTING);

            if (scenario.isFailed()) {

                String relPath = System.getProperty("screenshot.rel.path", "screenshots/");
                String fullRelPath = relPath + scenarioName + ".png";

                try {
                    ExtentLogger.screenshot(fullRelPath);
                    log.info("Screenshot attached to report: " + fullRelPath);
                } catch (Exception e) {
                    log.warn("Failed to attach screenshot using file path: " + e.getMessage());
                }

                try {
                    String base64Screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
                    if (ExtentCucumberAdapter.getCurrentStep() != null) {
                        ExtentCucumberAdapter.getCurrentStep()
                                .fail(MediaEntityBuilder.createScreenCaptureFromBase64String(base64Screenshot).build());
                        log.info("Screenshot attached to report using Base64.");
                    }
                } catch (Exception e) {
                    log.warn("Failed to attach screenshot using Base64: " + e.getMessage());
                }

                failedScenarios.add(scenario.getName());
                failedScreenshots.add(screenshotPath.toAbsolutePath().toString());
            }
        } catch (Exception e) {
            log.error("Error in after hook: " + e.getMessage());
        } finally {
            DriverManager.quitDriver();
            log.info("Driver quit after scenario: " + scenario.getName());
        }
    }

    @AfterAll
    public static void sendEmailReport() {

        // ⭐ NEW — Ensures reports are written before email is sent
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                log.info("ShutdownHook triggered → Waiting for ExtentReports to finish writing...");

                // Final wait for report generation
                Thread.sleep(5000);

                File htmlReport = new File("target/ExtentReports/SparkReport.html");
                File pdfReport = new File("target/ExtentReports/ExtentReport.pdf");

                log.info("HTML Report Exists: " + htmlReport.exists());
                log.info("PDF Report Exists: " + pdfReport.exists());

                // Send email WITH screenshots
                EmailReportSender.sendFailureReportWithScreenshots(failedScenarios, failedScreenshots);

            } catch (Exception e) {
                log.error("Error inside ShutdownHook: " + e.getMessage());
            }
        }));

        log.info("ShutdownHook registered successfully.");
    }

    private static String attachScreenshot(WebDriver driver) {
        TakesScreenshot ts = (TakesScreenshot) driver;
        String base64 = ts.getScreenshotAs(OutputType.BASE64);
        return "data:image/jpg;base64," + base64;
    }
}
