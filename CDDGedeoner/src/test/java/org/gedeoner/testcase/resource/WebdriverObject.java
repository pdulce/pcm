package org.gedeoner.testcase.resource;

import org.openqa.selenium.WebDriver;
//import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class WebdriverObject {

	static {
		System.setProperty("webdriver.gecko.driver", "C:\\webtools\\geckodriver.exe");
		
		/**ANOTHER WEB DRIVERS**/
		//System.setProperty("webdriver.chrome.driver", "C:\\webtools\\chromedriver.exe");    
	}

	private static WebDriver driver;

	public static WebDriver getWebDriverInstance() {
		try {
			if (driver == null) {
				driver = new FirefoxDriver();
				//driver = new ChromeDriver();
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

	public static void killDriverInstance() {
		disposeDriver();
		driver = null;
	}

}
