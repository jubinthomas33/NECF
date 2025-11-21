package com.utility;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import org.apache.logging.log4j.Logger;

import com.base.BaseClass;
import com.github.javafaker.Address;
import com.github.javafaker.Faker;

import com.github.javafaker.Name;

import com.webdrivermanager.DriverManager;

public class Elements {

	private static final Logger log = LoggerHelper.getLogger(Elements.class);

	private WebDriverWait wait;
	public WebDriver driver;
	public JavascriptExecutor js;
	public Actions action;
	public Alert alert;
	public Select select;
	public Faker faker;

	public Elements() {
		this.wait = BaseClass.getWait();
		this.driver = DriverManager.getDriver();
		this.faker = new Faker();
	}

	public WebElement getVisible(WebElement element) {
		return wait.until(ExpectedConditions.visibilityOf(element));
	}

	public WebElement getClickable(WebElement element) {
		return wait.until(ExpectedConditions.elementToBeClickable(element));
	}

	public void click(WebElement element) {
		getClickable(element).click();

	}

	public void sendText(WebElement element, String text) {
		getVisible(element).click();
		getVisible(element).sendKeys(text);
	}

	public void sendNumber(WebElement element, CharSequence[] data) {
		getVisible(element).sendKeys(data);
	}

	public boolean isDisplayed(WebElement element) {
		return getVisible(element).isDisplayed();

	}

	public boolean isEnabled(WebElement element) {
		return getVisible(element).isEnabled();

	}

	public void clear(WebElement element) {
		getVisible(element).clear();
	}


	public void clearAndType(WebElement element, String text) {

		getVisible(element).click();
		element.sendKeys(Keys.CONTROL + "a");
		element.sendKeys(Keys.BACK_SPACE);
		element.sendKeys(text);
	}

	public void clearAll(WebElement element) {

		getVisible(element).click();
		element.sendKeys(Keys.CONTROL + "a");
		element.sendKeys(Keys.BACK_SPACE);

	}

	public void sendTextToCellByHeading(String heading, int rowNum, String text) {

		// 1️⃣ Find the column index of the heading (MAC ID, SERIAL NO, etc.)
		String colIndexXPath = "count((//table//tr)[1]/th[normalize-space()='" + heading
				+ "']/preceding-sibling::th) + 1";

		// 2️⃣ Build final cell XPath: go to desired row, pick matching column
		String cellXPath = "(//table//tr)[" + rowNum + "]/td[" + colIndexXPath + "]";

		// 3️⃣ Locate the cell
		WebElement cell = driver.findElement(By.xpath(cellXPath));

		// 4️⃣ Try sending inside input if cell contains input
		try {
			WebElement input = cell.findElement(By.xpath(".//input"));
			input.clear();
			input.sendKeys(text);
		} catch (Exception e) {
			// If no input field → try sending directly to cell
			cell.click();
			cell.clear();
			cell.sendKeys(text);
		}

		System.out.println("Sent text '" + text + "' under heading '" + heading + "' in row " + rowNum);
	}

	public String getTextBoxValue(WebElement element) {
		return getVisible(element).getAttribute("value");
	}

	public void checkBox(WebElement element) {
		if (!getClickable(element).isSelected()) {
			element.click();
		}
	}

	public String generateMacId(int length) {
		faker = new Faker();
		return faker.regexify("[A-Za-z0-9]{" + length + "}");
	}

	public void UncheckBox(WebElement element) {
		if (getClickable(element).isSelected()) {
			element.click();
		}
	}

	public String getText(WebElement element) {
		return getVisible(element).getText();
	}

	public void scrollToView(WebElement element) {
		js = (JavascriptExecutor) driver;
		js.executeScript("arguments[0].scrollIntoView(true);", element);

	}

	public void javaScrollAndClick(WebElement element) {
		js = (JavascriptExecutor) driver;
		js.executeScript("arguments[0].scrollIntoView(true);", element);
		js.executeScript("arguments[0].click();", element);
	}

	public void javaClick(WebElement element) {
		js = (JavascriptExecutor) driver;
		js.executeScript("arguments[0].scrollIntoView(true);", element);
		js.executeScript("arguments[0].click();", element);
	}

	public void scrollToBottom() {
		js = (JavascriptExecutor) driver;
		js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
	}

	public void scrollByPixels(int x, int y) {
		js = (JavascriptExecutor) driver;
		js.executeScript(("window.scrollBy(arguments[0], arguments[1]);"), x, y);
	}

	public void selectByText(WebElement element, String text) {
		select = new Select(getVisible(element));
		select.selectByVisibleText(text);
	}

	public void selectByValue(WebElement element, int index) {
		select = new Select(getVisible(element));
		select.selectByIndex(index);
	}

	public void selectByValue(WebElement element, String text) {
		select = new Select(getVisible(element));
		select.selectByValue(text);
	}

	public void doubleClick(WebElement element) {

		action = new Actions(driver);
		action.doubleClick(getClickable(element)).perform();

	}

	public void goToUrl(String url) {
		driver.get(url);

	}

	public void rightClick(WebElement element) {
		action = new Actions(driver);
		action.contextClick(getClickable(element)).perform();
	}

	public void moveToElement(WebElement element) {
		action = new Actions(driver);
		action.moveToElement(getClickable(element)).perform();
	}

	public void alertAccept() {

		alert = driver.switchTo().alert();
		alert.accept();

	}

	public void alertDismiss() {
		alert = driver.switchTo().alert();
		alert.dismiss();
	}

	public void alertSendKeys(String text) {
		alert = driver.switchTo().alert();
		alert.sendKeys(text);
	}

	public String getFormattedDateMinusDays(int minusDays, String format) {
		LocalDate today = LocalDate.now();
		LocalDate calculatedDate = today.minusDays(minusDays);

		// LocalDate automatically handles month/year boundaries
		// Example: Jan 1 - 2 days = Dec 30 of previous year
		// Example: Feb 1 - 5 days = Jan 27 of same year

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
		String formattedDate = calculatedDate.format(formatter);

		// Enhanced logging to verify calculation
		log.info("=== Date Calculation ===");
		log.info("Today's date: " + today);
		log.info("Days to subtract: " + minusDays);
		log.info("Calculated date: " + calculatedDate);
		log.info("Formatted date: " + formattedDate);
		log.info("Days difference: " + java.time.temporal.ChronoUnit.DAYS.between(calculatedDate, today) + " days");

		// Verify the calculation is correct
		long daysDifference = java.time.temporal.ChronoUnit.DAYS.between(calculatedDate, today);
		if (daysDifference != minusDays) {
			log.error("DATE CALCULATION ERROR! Expected " + minusDays + " days difference, but got " + daysDifference);
			throw new RuntimeException("Date calculation error: Expected " + minusDays
					+ " days before today, but calculation shows " + daysDifference + " days");
		}

		// Validate the calculated date is reasonable (not too far in past/future)
		if (calculatedDate.isBefore(today.minusYears(1)) || calculatedDate.isAfter(today)) {
			log.warn("Calculated date " + calculatedDate + " seems unusual (more than 1 year in past or in future)");
		}

		log.info("=== End Date Calculation ===");

		return formattedDate;
	}

	public String getTextAlert() {
		alert = driver.switchTo().alert();
		return alert.getText();
	}

	public String getCurrentUrl() {
		return driver.getCurrentUrl();
	}

	public String getTitle() {
		return driver.getTitle();
	}

	public void assertEqualsObject(String expected, Object object) {
		Assert.assertEquals(expected, object);

	}

	public void assertEqualsString(String expected, String actual) {
		Assert.assertEquals(expected, actual);

	}

	public void assertEqualsInt(int expected, int actual) {
		Assert.assertEquals(expected, actual);

	}

	public void keyboardEnter() {
		action = new Actions(driver);
		action.sendKeys(Keys.ENTER).perform();
	}

	public void toggleOn(WebElement element) {
		if (!element.isSelected()) {
			element.click();
		}
	}

	public void refresh() {
		driver.navigate().refresh();
	}

	public void toggleOff(WebElement element) {
		if (element.isSelected()) {
			element.click();

		}
	}

	public void toggleOffConfig(WebElement element) {

		String ariaChecked = element.getAttribute("aria-checked");
		if ("true".equalsIgnoreCase(ariaChecked)) {
			element.click();
		} else {
			log.debug("Switch already off!");
		}
	}

	public void toggleOnConfig(WebElement element) {
		String ariaChecked = element.getAttribute("aria-checked");
		if ("false".equalsIgnoreCase(ariaChecked)) {
			element.click();
		} else {
			log.debug("Switch already On!");
		}
	}

	public void sendDatePlusOneDay(WebElement element, LocalDate baseDate, String format) {
		// Note: Despite method name, this adds 2 days (not 1) to the base date
		// LocalDate automatically handles month/year boundaries correctly:
		// Example: Jan 30 + 2 days = Feb 1 (next month)
		// Example: Dec 30 + 2 days = Jan 1 (next year)
		// Example: Feb 28 + 2 days = Mar 2 (handles leap years correctly)

		LocalDate newDate = baseDate.plusDays(2);

		// Log the date calculation for debugging
		log.debug("Date calculation: Base date = " + baseDate + ", Adding 2 days = " + newDate);

		// Validate date is reasonable (not too far in future)
		LocalDate maxFutureDate = LocalDate.now().plusYears(1);
		if (newDate.isAfter(maxFutureDate)) {
			log.warn("Calculated date " + newDate + " is more than 1 year in the future");
		}

		// Format the date
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
		String finalDate = newDate.format(formatter);

		log.info("Sending date to field: " + finalDate + " (calculated from " + baseDate + " + 2 days)");

		// Send the date (field already cleared)
		element.sendKeys(finalDate);
		element.sendKeys(Keys.ENTER);

		log.debug("Date successfully sent: " + finalDate);
	}

	public Name fakeName() {
		return faker.name();

	}

	public Address fakeAddress() {
		return faker.address();
	}

}
