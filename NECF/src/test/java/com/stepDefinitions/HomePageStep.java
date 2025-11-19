package com.stepDefinitions;

import org.openqa.selenium.WebDriver;

import com.pages.HomePage;
import com.utility.ConfigReader;
import com.utility.Elements;
import com.webdrivermanager.DriverManager;
import io.cucumber.java.en.*;

public class HomePageStep {
	WebDriver driver;
	HomePage homePage;
	Elements e;

	public HomePageStep() {

		if (this.driver == null) {
			this.driver = DriverManager.getDriver();
		}
		if (this.homePage == null) {
			this.homePage = new HomePage();
		}
		if (this.e == null) {
			this.e = new Elements();
		}
	}
	
	
	@Given("the website should load successfully")
	public void the_website_should_load_successfully() {
		homePage.isSiteUp(ConfigReader.getProperty("url"));
		homePage.goToUrl();
		homePage.verifyingTitle();
		homePage.verfiyingHomePage();
	    
	}
	
}
