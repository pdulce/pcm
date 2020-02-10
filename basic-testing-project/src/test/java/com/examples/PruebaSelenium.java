package com.examples;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.examples.junit5.WebdriverObject;

public class PruebaSelenium {

	
	public static void main(String[] args){
				
    	WebDriver driver = WebdriverObject.getWebDriverInstance("Data.xlsx");
		try {
			
			WebElement ele2Rearch =  driver.findElement(By.name("entryForm.user"));
			
			System.out.println("Texto: " + ele2Rearch.getText());
			
		} catch (Exception exc) {
			System.out.println("Error in testLoginErrUser: " + exc.getMessage());
			exc.printStackTrace();
		}finally {
			WebdriverObject.reinitializeDriver();
		}
	}
	
}
