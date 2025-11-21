package com.webdrivermanager;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import com.utility.LoggerHelper;
import io.github.bonigarcia.wdm.WebDriverManager;

public class DriverManager {

	private static final Logger log = LoggerHelper.getLogger(DriverManager.class);

	private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();

	public static void setDriver(String browser) {

		if (driver.get() == null) {

			if (browser != null && browser.equalsIgnoreCase("chrome")) {

				WebDriverManager.chromedriver().setup();
				ChromeOptions options = new ChromeOptions();
				options.addArguments("--headless=new"); // ✅ HEADLESS
				options.addArguments("--remote-allow-origins=*");

				driver.set(new ChromeDriver(options));
				log.info("Launching Chrome in HEADLESS mode");

			} else if (browser != null && browser.equalsIgnoreCase("firefox")) {

				WebDriverManager.firefoxdriver().setup();
				FirefoxOptions options = new FirefoxOptions();
				options.addArguments("--headless"); // ✅ HEADLESS

				driver.set(new FirefoxDriver(options));
				log.info("Launching Firefox in HEADLESS mode");

			} else if (browser != null && browser.equalsIgnoreCase("edge")) {

				WebDriverManager.edgedriver().setup();
				EdgeOptions options = new EdgeOptions();
				options.addArguments("--headless=new"); // ✅ HEADLESS
				options.addArguments("--window-size=1920,1080");

				driver.set(new EdgeDriver(options));
				log.info("Launching Edge in HEADLESS mode");

			} else {

				log.warn("Browser not supported: " + browser + " - launching Chrome Headless instead");
				WebDriverManager.chromedriver().setup();

				ChromeOptions options = new ChromeOptions();
				options.addArguments("--headless=new");
				options.addArguments("--window-size=1920,1080");

				driver.set(new ChromeDriver(options));
			}
		}
	}

	public static WebDriver getDriver() {
		return driver.get();
	}

	public static void quitDriver() {
		if (driver.get() != null) {
			driver.get().quit();
			driver.remove();
			log.info("Driver quit successfully");
		}
	}
}
