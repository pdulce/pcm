package com.examples;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class WebdriverObject {
	
	static {
		System.setProperty("webdriver.gecko.driver", "/home/pedro/gecko/geckodriver");
	}
	
	private static WebDriver driver;
	private static MemoryData memoryData;
	
	public static WebDriver getWebDriverInstance() {
				
        try {
        	
        	if (driver == null) {
       			driver= new FirefoxDriver();
        		driver.get(getMemoryData().getURL());
        	}
            return driver;
        }catch (Throwable exc) {
        	exc.printStackTrace();
        	return null;
        }        
	}
	
	public static MemoryData getMemoryData() {
		try {
			if (memoryData == null){
	    		String excelFile = "Data.xlsx";
	    		memoryData = new MemoryData(excelFile);
	    	}
	    	return memoryData;
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
