package com.pages;

import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;


import com.utility.ConfigReader;
import com.utility.Elements;
import com.utility.LoggerHelper;
import com.utility.ExtentLogger;
import com.webdrivermanager.DriverManager;



public class HomePage {
	WebDriver driver;
	Elements util;
	JavascriptExecutor js;
	private static final Logger log = LoggerHelper.getLogger(HomePage.class);

	public HomePage() {
		this.driver = DriverManager.getDriver();
		util = new Elements();
		PageFactory.initElements(driver, this);
	}

	@FindBy(xpath = "(//a[@href='https://mediaxbook.com'])[1]")
	WebElement titleLink;
	@FindBy(xpath = "//div[.='size']")
	WebElement size;
	@FindBy(xpath = "//h1[contains(text(),'About NECF')]")
	WebElement AboutNECF;
	@FindBy(xpath = "//p[contains(@class,'elementor-heading') and contains(text(),'NECF Corporation. All Rights Reserved')]")
	WebElement endLine;

	// -----------------------------------------
	// CHECK SITE UP
	// -----------------------------------------
	public boolean isSiteUp(String urlString) {
		try {

			log.info("Checking site availability for URL: " + urlString);
			ExtentLogger.extentInfo("Checking site availability for URL: " + urlString);

			URL url = new URL(urlString);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(5000);
			connection.connect();

			int code = connection.getResponseCode();

			log.info("Server responded with status code: " + code);
			ExtentLogger.extentInfo("Server responded with status code: " + code);

			boolean isUp = (code >= 200 && code < 400);

			if (isUp) {
				log.info("Website is UP");
				ExtentLogger.extentPass("Website is UP");
			} else {
				log.error("Website is DOWN! HTTP Status: " + code);
				ExtentLogger.extentError("Website is DOWN! HTTP Status: " + code);
			}

			return isUp;

		} catch (Exception e) {
			log.error("Website is DOWN! Error: " + e.getMessage());
			ExtentLogger.extentError("Website is DOWN! Error: " + e.getMessage());
			return false;
		}
	}

	// -----------------------------------------
	// NAVIGATE TO URL + PAGE LOAD TIME
	// -----------------------------------------
	public void goToUrl() {
		try {
			driver.get(ConfigReader.getProperty("url"));
			log.info("Navigated to URL: " + ConfigReader.getProperty("url"));
			ExtentLogger.extentInfo("Navigated to URL: " + ConfigReader.getProperty("url"));
		} catch (Exception e) {
			log.error("Page failed to load. Site may be DOWN!");
			ExtentLogger.extentError("Page failed to load. Site may be DOWN!");

			log.error("Error message: " + e.getMessage());
			ExtentLogger.extentError("Error message: " + e.getMessage());

			Assert.fail("Page load failed. Website is down.");
		}

		try {
			js = (JavascriptExecutor) driver;
			long loadTime = (Long) js.executeScript(
					"return window.performance.timing.loadEventEnd - window.performance.timing.navigationStart;");

			log.info("Page Load Time: " + loadTime + " ms");
			ExtentLogger.extentInfo("Page Load Time: " + loadTime + " ms");

		} catch (Exception e) {
			log.error("Unable to calculate performance timing");
			ExtentLogger.extentError("Unable to calculate performance timing");
		}
	}

	public void verifyingTitle() {
		try {
			log.info("Verifying URL...");
			ExtentLogger.extentInfo("Verifying URL...");

			String url = util.getCurrentUrl();
			log.info("Current URL: " + url);
			ExtentLogger.extentInfo("Current URL: " + url);

			if (!ConfigReader.getProperty("url").equals(url)) {
				ExtentLogger.extentFail(
						"URL verification failed! Expected: " + ConfigReader.getProperty("url") + ", Found: " + url);
				Assert.fail(
						"URL verification failed! Expected: " + ConfigReader.getProperty("url") + ", Found: " + url);
			} else {
				ExtentLogger.extentPass("URL verified successfully");
			}

			String title = util.getTitle();
			log.info("Page Title: " + title);
			ExtentLogger.extentInfo("Page Title: " + title);

			if (!ConfigReader.getProperty("title").equals(title)) {
				ExtentLogger.extentFail("Title verification failed! Expected: " + ConfigReader.getProperty("title")
						+ ", Found: " + title);
				Assert.fail("Title verification failed! Expected: " + ConfigReader.getProperty("title") + ", Found: "
						+ title);
			} else {
				ExtentLogger.extentPass("Title verified successfully");
			}

		} catch (Exception e) {
			log.error("Exception during title verification: " + e.getMessage());
			ExtentLogger.extentError("Exception during title verification: " + e.getMessage());
			Assert.fail("Exception during title verification: " + e.getMessage());
		}
	}

	public void verfiyingHomePage() {
		try {
			log.info("Verifying the page elements...");
			ExtentLogger.extentInfo("Verifying the page elements...");

			util.getVisible(titleLink);
			if (titleLink.isDisplayed()) {
				ExtentLogger.extentPass("Title link verified...");
			} else {
				ExtentLogger.extentFail("Title link not displayed!");
				Assert.fail("Title link not displayed!");
			}

			util.scrollToView(AboutNECF);
			util.getVisible(AboutNECF);
			if (AboutNECF.isDisplayed()) {
				ExtentLogger.extentPass("About NECF section verified...");
			} else {
				ExtentLogger.extentFail("About NECF section not displayed!");
				Assert.fail("About NECF section not displayed!");
			}

			util.scrollToView(endLine);
			util.getVisible(endLine);
			if (endLine.isDisplayed()) {
				ExtentLogger.extentPass("Last element displayed...");
			} else {
				ExtentLogger.extentFail("Last element not displayed!");
				Assert.fail("Last element not displayed!");
			}

		} catch (Exception e) {
			log.error("Exception while verifying home page: " + e.getMessage());
			ExtentLogger.extentError("Exception while verifying home page: " + e.getMessage());
			Assert.fail("Exception while verifying home page: " + e.getMessage());
		}
	}

	// -----------------------------------------
	// VERIFY URL + TITLE
	// -----------------------------------------
}
