package com.utility;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ISuite;
import org.testng.ISuiteListener;

import com.stepDefinitions.Hooks;
import com.utility.EmailReportSender;

public class SuiteListener implements ISuiteListener {

    private static final Logger log = LogManager.getLogger(SuiteListener.class);
    private static final String EXTENT_REPORTS_DIR = "target/ExtentReports";

    @Override
    public void onStart(ISuite suite) {
        log.info("Test Suite started: " + suite.getName());
        cleanupExtentReportsDirectory();
    }

    /**
     * Flush ExtentReports instance managed by ExtentCucumberAdapter
     */
    private void flushExtentReports() {
        try {
            // Use reflection to access the ExtentReports instance from ExtentCucumberAdapter
            Class<?> adapterClass = Class.forName("com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter");
            java.lang.reflect.Method getExtentMethod = adapterClass.getMethod("getExtent");
            Object extentReports = getExtentMethod.invoke(null);
            
            if (extentReports != null) {
                java.lang.reflect.Method flushMethod = extentReports.getClass().getMethod("flush");
                flushMethod.invoke(extentReports);
                log.info("✔ ExtentReports flushed successfully via ExtentCucumberAdapter");
            } else {
                log.warn("ExtentReports instance is null in ExtentCucumberAdapter");
            }
        } catch (ClassNotFoundException e) {
            log.warn("ExtentCucumberAdapter class not found: " + e.getMessage());
        } catch (NoSuchMethodException e) {
            log.warn("Method not found in ExtentCucumberAdapter: " + e.getMessage());
        } catch (Exception e) {
            log.warn("Could not flush ExtentReports via reflection: " + e.getMessage());
            log.debug("ExtentCucumberAdapter should auto-flush, waiting for it to complete...");
        }
    }

    /**
     * Clean up old ExtentReports files and directories before starting new test run
     */
    private void cleanupExtentReportsDirectory() {
        try {
            Path extentReportsPath = Paths.get(EXTENT_REPORTS_DIR);
            
            if (Files.exists(extentReportsPath)) {
                log.info("Cleaning up old ExtentReports directory: " + extentReportsPath.toAbsolutePath());
                
                // Delete all files and subdirectories
                Files.walk(extentReportsPath)
                    .sorted((a, b) -> b.compareTo(a)) // Delete files before directories
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            log.debug("Deleted: " + path);
                        } catch (IOException e) {
                            log.warn("Failed to delete: " + path + " - " + e.getMessage());
                        }
                    });
                
                log.info("✔ ExtentReports directory cleaned successfully");
            } else {
                log.info("ExtentReports directory does not exist, creating it...");
                Files.createDirectories(extentReportsPath);
                log.info("✔ ExtentReports directory created");
            }
        } catch (Exception e) {
            log.error("Error cleaning up ExtentReports directory: " + e.getMessage(), e);
        }
    }

    @Override
    public void onFinish(ISuite suite) {
        try {
            log.info("Test Suite finished: " + suite.getName());
            
            // Flush ExtentReports managed by ExtentCucumberAdapter
            log.info("Flushing ExtentReports...");
            flushExtentReports();
            
            // Wait a bit to ensure the report is written to disk
            Thread.sleep(2000);

            // Wait and verify HTML report is fully generated before sending email
            File htmlReport = new File("target/ExtentReports/SparkReport.html");
            int maxWaitAttempts = 10;
            int waitInterval = 500; // milliseconds
            boolean reportReady = false;
            long reportSize = 0;
            
            for (int i = 0; i < maxWaitAttempts; i++) {
                if (htmlReport.exists()) {
                    long sizeBefore = htmlReport.length();
                    Thread.sleep(waitInterval);
                    long sizeAfter = htmlReport.length();
                    // If file size is stable and greater than minimum size (not empty), report is ready
                    if (sizeBefore == sizeAfter && sizeBefore > 1000) { // Minimum 1KB to ensure it's not empty
                        reportReady = true;
                        reportSize = sizeAfter;
                        log.info("HTML report is ready: " + htmlReport.getAbsolutePath() + " (Size: " + reportSize + " bytes)");
                        break;
                    }
                } else {
                    Thread.sleep(waitInterval);
                }
            }
            
            if (!reportReady) {
                log.warn("HTML report may not be fully generated or is empty. Report exists: " + 
                        htmlReport.exists() + ", Size: " + (htmlReport.exists() ? htmlReport.length() : 0) + " bytes");
            }

            // Get failed scenarios and screenshots from Hooks
            java.util.List<String> failedScenarios = Hooks.getFailedScenarios();
            java.util.List<String> failedScreenshots = Hooks.getFailedScreenshots();
            
            // Only send email if there are failures AND report is ready and valid
            if (!failedScenarios.isEmpty()) {
                if (reportReady && reportSize > 1000) {
                    log.info("Sending email report for " + failedScenarios.size() + " failed scenario(s)...");
                    EmailReportSender.sendFailureReportWithScreenshots(failedScenarios, failedScreenshots);
                } else {
                    log.warn("⚠️ Email not sent: Report is not ready or is empty. Report ready: " + 
                            reportReady + ", Report size: " + reportSize + " bytes");
                }
            } else {
                log.info("No failed scenarios detected. Email not sent.");
            }

        } catch(Exception e) {
            log.error("Error in SuiteListener.onFinish: " + e.getMessage(), e);
        }
    }
}
