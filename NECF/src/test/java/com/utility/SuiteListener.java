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
    private static final String REPORT_FILE_NAME = "SparkReport.html";
    
    /**
     * Get the absolute path to the ExtentReports directory
     */
    private static Path getExtentReportsPath() {
        String userDir = System.getProperty("user.dir");
        log.info("Current working directory: " + userDir);
        Path reportsPath = Paths.get(userDir, EXTENT_REPORTS_DIR);
        log.info("ExtentReports directory path: " + reportsPath.toAbsolutePath());
        return reportsPath;
    }
    
    /**
     * Get the absolute path to the HTML report file
     */
    private static File getHtmlReportFile() {
        Path reportsPath = getExtentReportsPath();
        File reportFile = reportsPath.resolve(REPORT_FILE_NAME).toFile();
        log.info("HTML report file path: " + reportFile.getAbsolutePath());
        return reportFile;
    }

    @Override
    public void onStart(ISuite suite) {
        log.info("Test Suite started: " + suite.getName());
        log.info("Working directory: " + System.getProperty("user.dir"));
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
            Path extentReportsPath = getExtentReportsPath();
            
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
                log.info("✔ ExtentReports directory created at: " + extentReportsPath.toAbsolutePath());
            }
        } catch (Exception e) {
            log.error("Error cleaning up ExtentReports directory: " + e.getMessage(), e);
        }
    }

    @Override
    public void onFinish(ISuite suite) {
        try {
            log.info("Test Suite finished: " + suite.getName());
            log.info("Working directory: " + System.getProperty("user.dir"));
            
            // Get failed scenarios and screenshots from Hooks first
            java.util.List<String> failedScenarios = Hooks.getFailedScenarios();
            java.util.List<String> failedScreenshots = Hooks.getFailedScreenshots();
            log.info("Failed scenarios count: " + failedScenarios.size());
            log.info("Failed screenshots count: " + failedScreenshots.size());
            
            // Flush ExtentReports managed by ExtentCucumberAdapter
            log.info("Flushing ExtentReports...");
            flushExtentReports();
            
            // Wait a bit to ensure the report is written to disk
            Thread.sleep(2000);

            // Get the absolute path to the HTML report
            File htmlReport = getHtmlReportFile();
            log.info("Checking for HTML report at: " + htmlReport.getAbsolutePath());
            
            // Ensure parent directory exists
            File parentDir = htmlReport.getParentFile();
            if (!parentDir.exists()) {
                log.warn("Parent directory does not exist, creating: " + parentDir.getAbsolutePath());
                parentDir.mkdirs();
            }
            
            // Wait and verify HTML report is fully generated before sending email
            int maxWaitAttempts = 20; // Increased from 10 to 20
            int waitInterval = 1000; // Increased from 500ms to 1000ms (1 second)
            boolean reportReady = false;
            long reportSize = 0;
            
            log.info("Waiting for HTML report to be generated (max " + (maxWaitAttempts * waitInterval / 1000) + " seconds)...");
            
            for (int i = 0; i < maxWaitAttempts; i++) {
                if (htmlReport.exists()) {
                    long sizeBefore = htmlReport.length();
                    log.debug("Attempt " + (i + 1) + ": Report exists, size: " + sizeBefore + " bytes");
                    Thread.sleep(waitInterval);
                    long sizeAfter = htmlReport.length();
                    // If file size is stable and greater than minimum size (not empty), report is ready
                    if (sizeBefore == sizeAfter && sizeBefore > 1000) { // Minimum 1KB to ensure it's not empty
                        reportReady = true;
                        reportSize = sizeAfter;
                        log.info("✔ HTML report is ready: " + htmlReport.getAbsolutePath() + " (Size: " + reportSize + " bytes)");
                        break;
                    } else if (sizeBefore != sizeAfter) {
                        log.debug("Report still being written (size changed from " + sizeBefore + " to " + sizeAfter + " bytes)");
                    } else if (sizeBefore <= 1000) {
                        log.warn("Report exists but is too small: " + sizeBefore + " bytes (minimum 1KB required)");
                    }
                } else {
                    log.debug("Attempt " + (i + 1) + ": Report does not exist yet, waiting...");
                    Thread.sleep(waitInterval);
                }
            }
            
            if (!reportReady) {
                log.warn("⚠️ HTML report may not be fully generated or is empty.");
                log.warn("Report exists: " + htmlReport.exists());
                log.warn("Report path: " + htmlReport.getAbsolutePath());
                if (htmlReport.exists()) {
                    log.warn("Report size: " + htmlReport.length() + " bytes");
                } else {
                    // Check if directory exists and list its contents
                    if (parentDir.exists()) {
                        log.warn("Parent directory exists. Contents:");
                        File[] files = parentDir.listFiles();
                        if (files != null) {
                            for (File f : files) {
                                log.warn("  - " + f.getName() + " (" + f.length() + " bytes)");
                            }
                        } else {
                            log.warn("  (directory is empty or cannot be read)");
                        }
                    } else {
                        log.warn("Parent directory does not exist: " + parentDir.getAbsolutePath());
                    }
                }
            }

            // Only send email if there are failures AND report is ready and valid
            if (!failedScenarios.isEmpty()) {
                if (reportReady && reportSize > 1000) {
                    log.info("Sending email report for " + failedScenarios.size() + " failed scenario(s)...");
                    EmailReportSender.sendFailureReportWithScreenshots(failedScenarios, failedScreenshots);
                } else {
                    log.warn("⚠️ Email not sent: Report is not ready or is empty. Report ready: " + 
                            reportReady + ", Report size: " + reportSize + " bytes");
                    log.warn("Report path checked: " + htmlReport.getAbsolutePath());
                }
            } else {
                log.info("No failed scenarios detected. Email not sent.");
            }

        } catch(Exception e) {
            log.error("Error in SuiteListener.onFinish: " + e.getMessage(), e);
            e.printStackTrace();
        }
    }
}
