package com.examples.junit5;

import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.examples.MemoryData;

public class WebdriverObject {
	
	static {
		System.setProperty("webdriver.gecko.driver", "/home/pedro/gecko/geckodriver");
	}
	
	private static WebDriver driver;
	private static MemoryData memoryData;
	
	public static WebDriver getWebDriverInstance(final String dataExcelFile) {
		
        try {
        	
        	if (driver == null) {
       			driver= new FirefoxDriver();
       			memoryData = new MemoryData(dataExcelFile);
        		driver.get(memoryData.getURL());
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
	    		throw new RuntimeException("First, you must charge Excel file with data tests");
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
	
	public static void main(String[] args) {
		
		String excelFile = "Data.xlsx";
		
		WebdriverObject.getWebDriverInstance(excelFile);		
		MemoryData chargerDataSet = WebdriverObject.getMemoryData();
		
		Map<String,Map<String,String>> mapaAll = chargerDataSet.getDatosEscenariosTest();
		System.out.println("Datos de todo el mapa para pruebas: " + mapaAll);
		
		System.out.println("URL para pruebas: " + chargerDataSet.getURL());
	}
	
}
