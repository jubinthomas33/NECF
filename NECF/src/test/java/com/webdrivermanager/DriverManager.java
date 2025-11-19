package com.webdrivermanager;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.utility.LoggerHelper;
import io.github.bonigarcia.wdm.WebDriverManager;

public class DriverManager {
	
	private static final Logger log = LoggerHelper.getLogger(DriverManager.class);
	

	//private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();

	// setting the driver
	// In case of any unwanted browser , Chrome will be running automatically

	private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();
	
	public static void setDriver(String browser) {
		if (driver.get() == null) {
			if (browser != null && browser.equalsIgnoreCase("chrome")) {
				WebDriverManager.chromedriver().setup();
				driver.set(new ChromeDriver());

				log.info("Setting Chrome as a browser");
			}
			else if (browser != null && browser.equalsIgnoreCase("firefox")) {
				WebDriverManager.firefoxdriver().setup();
				driver.set(new FirefoxDriver());
				log.info("Launching Firefox Browser");
			}
			else if (browser != null && browser.equalsIgnoreCase("edge")) {
				WebDriverManager.edgedriver().setup();
				driver.set(new EdgeDriver());
				log.info("Launching Edge Browser");
			} else {
				log.warn("Browser not supported: " + browser + " - launching Chrome instead");
				WebDriverManager.chromedriver().setup();
				driver.set(new ChromeDriver());
			}
		}
	}

	public static WebDriver getDriver() {
		return driver.get();
	}
	public static void quitDriver() {
		if(driver.get()!=null) {
			driver.get().quit();
			driver.remove();
			log.info("Driver quit successfully");
		}
	}

	
}
