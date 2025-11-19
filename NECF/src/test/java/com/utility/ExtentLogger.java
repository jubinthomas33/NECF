package com.utility;

import com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter;

public class ExtentLogger {

    public static void extentInfo(String message) {
        ExtentCucumberAdapter.addTestStepLog("[INFO] " + message + "\n");
    }

    public static void extentPass(String message) {
        ExtentCucumberAdapter.addTestStepLog("[PASS] " + message + "\n");
    }

    public static void extentFail(String message) {
        ExtentCucumberAdapter.addTestStepLog("[FAIL] " + message + "\n");
    }

    public static void extentError(String message) {
        ExtentCucumberAdapter.addTestStepLog("[ERROR] " + message + "\n");
    }

    public static void extentWarning(String message) {
        ExtentCucumberAdapter.addTestStepLog("[WARNING] " + message + "\n");
    }

    /**
     * Attach screenshot to Extent report.
     * Only pass **file name**, not folder. It will resolve using screenshot.rel.path.
     */
    public static void screenshot(String screenshotFileName) {
        try {
            ExtentCucumberAdapter.addTestStepScreenCaptureFromPath(screenshotFileName);
        } catch (Exception e) {
            ExtentCucumberAdapter.addTestStepLog("[ERROR] Screenshot attach failed: " + e.getMessage());
        }
    }
}
