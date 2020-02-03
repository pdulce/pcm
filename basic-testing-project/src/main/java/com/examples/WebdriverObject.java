package com.examples;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class WebdriverObject {
	static {
		System.setProperty("webdriver.gecko.driver", "/home/pedro/gecko/geckodriver");
	}
	private static WebDriver driver;
	
	public static WebDriver getWebDriverInstance() {

        try {
        	if (driver == null) {
        		driver=new FirefoxDriver();
        	}
            driver.get("http://localhost:8081/sg/prjManager");
            return driver;
        }catch (Throwable exc) {
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
