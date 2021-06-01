package com.examples;

import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class WebdriverObject {
	
	static {
		System.setProperty("webdriver.gecko.driver", "C:\\Users\\pedro.dulce\\git\\pcm\\basic-testing-project\\tools\\geckodriver.exe");
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
	
	public static void main(String[] args) {
		String excelFile = "Data.xlsx";
		MemoryData chargerDataSet = new MemoryData(excelFile);
		
		Map<String,Map<String,String>> mapaAll = chargerDataSet.getDatosEscenariosTest();
		System.out.println("Datos de todo el mapa para pruebas: " + mapaAll);
		
		System.out.println("URL para pruebas: " + chargerDataSet.getURL());
	}
	
}
