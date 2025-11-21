package com.stepDefinitions;

import java.io.File;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter;
import com.base.BaseClass;
import com.utility.EmailReportSender;
import com.utility.ExtentLogger;
import com.utility.ExtentManager;
import com.utility.LoggerHelper;
import com.webdrivermanager.DriverManager;

import io.cucumber.java.*;

public class Hooks {

    private static final Logger log = LoggerHelper.getLogger(Hooks.class);
    private static final List<String> failedScenarios = new ArrayList<>();
    private static final List<String> failedScreenshots = new ArrayList<>();
    private static final String SCREENSHOT_DIR = "target/ExtentReports/screenshots/";

    @Before
    public void setUp(Scenario scenario) {
        for(String tag : scenario.getSourceTagNames()) {
            if(tag.startsWith("@browser=")) {
                String browser = tag.split("=")[1];
                DriverManager.setDriver(browser);
            }
        }
        WebDriver driver = DriverManager.getDriver();
        if(driver != null) {
            log.info("Driver initialized for scenario: " + scenario.getName());
            driver.manage().window().maximize();
            driver.manage().timeouts().implicitlyWait(java.time.Duration.ofSeconds(10));
            BaseClass.setWait(new WebDriverWait(driver, java.time.Duration.ofSeconds(10)));
        }
    }

    @After
    public void tearDown(Scenario scenario) {
        WebDriver driver = DriverManager.getDriver();
        if(driver == null) return;

        try {
            String scenarioName = scenario.getName().replaceAll("[^a-zA-Z0-9]", "_");
            Path screenshotPath = Paths.get(SCREENSHOT_DIR, scenarioName + ".png");
            Files.createDirectories(screenshotPath.getParent());

            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(screenshot.toPath(), screenshotPath, StandardCopyOption.REPLACE_EXISTING);

            if(scenario.isFailed()) {
                failedScenarios.add(scenario.getName());
                failedScreenshots.add(screenshotPath.toAbsolutePath().toString());
                ExtentLogger.screenshot("screenshots/" + scenarioName + ".png");
            }
        } catch(Exception e) {
            log.error("Error capturing screenshot: " + e.getMessage(), e);
        } finally {
            DriverManager.quitDriver();
            log.info("Driver quit after scenario: " + scenario.getName());
        }
    }

    @AfterAll
    public static void flushReportsAndSendEmail() {
        try {
            log.info("Flushing ExtentReports...");
            ExtentManager.getExtentReports().flush();

            // Wait to ensure HTML report is written
            Thread.sleep(3000);

            // Generate PDF from HTML
            EmailReportSender.generatePdfFromHtml(
                "target/ExtentReports/SparkReport.html",
                "target/ExtentReports/ExtentReport.pdf"
            );

            log.info("Sending email report...");
            EmailReportSender.sendFailureReportWithScreenshots(failedScenarios, failedScreenshots);

        } catch(Exception e) {
            log.error("Error in AfterAll: " + e.getMessage(), e);
        }
    }
}
