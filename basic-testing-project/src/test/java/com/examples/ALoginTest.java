package com.examples;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import org.openqa.selenium.By;
//import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import junit.framework.TestCase;

/***
 * Tests login feature for SeleniumHQ WebDriver
 */
public class ALoginTest extends TestCase{
	
	//static {
	//	System.setProperty("webdriver.gecko.driver", "/home/pedro/gecko/geckodriver");
	//}
	
	 /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public ALoginTest( )
    {
        super( "loginTest" );
	
    }

	
	@Test
	public void testLoginExists() {
		
		WebDriver driver = WebdriverObject.getWebDriverInstance();
        try {
            
            WebElement entryUserForm = driver.findElement(By.name("entryForm.user"));
            WebElement entryPaswdForm = driver.findElement(By.name("entryForm.password"));
                        
            Assert.assertTrue(entryUserForm.isDisplayed());
            Assert.assertTrue(entryPaswdForm.isDisplayed());
            
            entryUserForm.sendKeys("admin");
            entryPaswdForm.sendKeys("admin");
            
            WebElement submitFormElement = driver.findElement(By.id("submitForm"));
            submitFormElement.click();
            
            WebDriverWait wait = new WebDriverWait(driver, Long.valueOf(20));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("principal")));
            WebElement divPral = wait.until(presenceOfElementLocated(By.xpath("//div[@id='principal']")));

            Assert.assertTrue(divPral.isDisplayed());
                   
            Assert.assertTrue(divPral.getText().contains("Bienvenid@ Administrador, comience a navegar por el árbol lateral de servicios"));
                        
        }catch (Exception exc) {
        	System.out.println("Error " + exc.getMessage());
        	exc.printStackTrace();
        } finally {
        	WebdriverObject.reinitializeDriver();
        }
		
	}

}