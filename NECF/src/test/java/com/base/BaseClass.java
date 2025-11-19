package com.base;

import org.openqa.selenium.support.ui.WebDriverWait;

public class BaseClass {

	private static ThreadLocal<WebDriverWait> wait = new ThreadLocal<>();

	public static void setWait(WebDriverWait webDriverWait) {
		wait.set(webDriverWait);
	}

	public static WebDriverWait getWait() {
		return wait.get();
	}

}
