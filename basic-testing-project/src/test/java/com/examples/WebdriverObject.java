package com.examples;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class WebdriverObject {

	static {
		System.setProperty("webdriver.gecko.driver",
				"C:\\Users\\pedro.dulce\\git\\pcm\\basic-testing-project\\tools\\geckodriver.exe");
	}

	private static WebDriver driver;

	public static WebDriver getWebDriverInstance() {
		try {
			if (driver == null) {
				driver = new FirefoxDriver();
			}
			return driver;
		} catch (Throwable exc) {
			exc.printStackTrace();
			return null;
		}
	}

	public static void disposeDriver() {
		driver.quit();
	}

	public static void reinitializeDriver() {
		driver.quit();
		driver = null;
	}

}
