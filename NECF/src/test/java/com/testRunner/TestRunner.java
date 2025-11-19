package com.testRunner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter;

@CucumberOptions(
		features="src/test/resources/NECF.feature", 
		glue="com.stepDefinitions", 
		plugin = {
			    "pretty",
			    "html:target/htmlReports/htmlReport.html",
			    "json:target/jsonReports/jsonReport.json",
			    "com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:"
			}

) 
public class TestRunner extends AbstractTestNGCucumberTests {
	
	@BeforeSuite
	public void beforeSuite() {
		System.out.println("Test Suite started - ExtentReports will be initialized");
	}
	
	
}