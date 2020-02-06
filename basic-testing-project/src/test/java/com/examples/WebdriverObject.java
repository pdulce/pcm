package test.java.com.examples;

import org.openqa.selenium.WebDriver;

import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class WebdriverObject {
	
	static {
		System.setProperty("webdriver.ie.driver", "C:\\workspaceEclipse\\IEDriverServer.exe");
		//System.setProperty("webdriver.gecko.driver", "/home/pedro/gecko/geckodriver");
	}
	
	private static WebDriver driver;
	private static MemoryData memoryData;
	
	public static WebDriver getWebDriverInstance() {
				
        try {
        	
        	if (driver == null) {
        		if (System.getProperty("webdriver.gecko.driver") != null){
        			//driver= new FirefoxDriver();
        		}else if (System.getProperty("webdriver.ie.driver") != null){
        			driver= new InternetExplorerDriver();
        		}else{
        			throw new RuntimeException("Error: no se ha indicado el driver a usar" );
        		}
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
	    		String excelFile = "resources/Data.xlsx";
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
