package com.examples.junit5;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

import java.util.Map;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import org.openqa.selenium.By;
//import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.examples.MemoryData;
import com.examples.WebdriverObject;

import junit.framework.TestCase;

/***
 * Tests login feature for SeleniumHQ WebDriver
 */
public class LoginTest extends TestCase{
	
	 /**
     * Create the test case: from PC ISM modified
     *
     * @param testName name of the test case
     */
    public LoginTest( )
    {
        super( "JunitTest5" );
	
    }
    
private void makeAccessWithData(String testMethod){
    	
    	MemoryData memoryData = WebdriverObject.getMemoryData();
    	WebDriver driver = WebdriverObject.getWebDriverInstance();
		try {
			
			Map<String, String> datatest = memoryData.getDatosEscenarioTest(testMethod);
			
			WebDriverWait waitForTree = new WebDriverWait(driver, Long.valueOf(10));
			waitForTree.until(ExpectedConditions.visibilityOfElementLocated(By.id("entryForm.user")));
			WebElement entryUserForm = waitForTree.until(presenceOfElementLocated(By.id("entryForm.user")));
			WebElement entryPaswdForm = driver.findElement(By.name("entryForm.password"));

			entryUserForm.sendKeys(datatest.get("entryForm.user"));
			entryPaswdForm.sendKeys(datatest.get("entryForm.password"));

			WebElement submitFormElement = driver.findElement(By.id(datatest.get(MemoryData.SUBMIT_ELEMENT)));
			submitFormElement.click();
			
			WebDriverWait waitForDivErrMsg = new WebDriverWait(driver, Long.valueOf(10));
			String expression = datatest.get(MemoryData.ELEMENT_2_EVALUATE);
			waitForDivErrMsg.until(ExpectedConditions.visibilityOfElementLocated(expression.startsWith("/")?By.xpath(expression):By.id(expression)));
			WebElement labelErr = waitForDivErrMsg.until(presenceOfElementLocated(expression.startsWith("/")?By.xpath(expression):By.id(expression)));
			
			Assert.assertTrue(labelErr.getText().contains(datatest.get(MemoryData.VALUE_2_EVALUATE)));
			
		} catch (Throwable exc) {
			System.out.println("Error in testLoginErrUser: " + exc.getMessage());
			exc.printStackTrace();
			
		} finally {
			WebdriverObject.reinitializeDriver();
		}
    }
	
    @Test
    public void testLoginErrUser() {
		try {
			makeAccessWithData("testLoginErrUser");
			
		} catch (Throwable exc) {
			System.out.println("Error in testLoginErrUser: " + exc.getMessage());
			exc.printStackTrace();
		}
	}
    
    @Test
    public void testLoginErrPass() {
		try {
			
			makeAccessWithData("testLoginErrPass");
		
		} catch (Throwable exc) {
			System.out.println("Error in testLoginErrPass: " + exc.getMessage());
			exc.printStackTrace();
		}
	}
    
    @Test
    public void testLoginSucess() {
		try {
			makeAccessWithData("testLoginSucess");
		} catch (Throwable exc) {
			System.out.println("Error in testLoginSucess: " + exc.getMessage());
			exc.printStackTrace();
		}
	}

}