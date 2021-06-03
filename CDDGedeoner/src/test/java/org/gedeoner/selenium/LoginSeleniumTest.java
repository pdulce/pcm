package org.gedeoner.selenium;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

import java.util.Map;

import org.gedeoner.testcase.resource.MemoryData;
import org.gedeoner.testcase.resource.WebdriverObject;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class LoginSeleniumTest {

	@Test
	public void setup() {
		makeAccessWithData("testLoginErrUser");
		makeAccessWithData("testLoginErrPass");
		makeAccessWithData("testLoginSucess");
	}
	
	private void makeAccessWithData(String testMethod) {

		WebDriver driver = WebdriverObject.getWebDriverInstance();
		MemoryData memoryData =  MemoryData.getUniqueInstance();
		
		try {			
			Map<String, String> datatest = memoryData.getDatosEscenarioTest(testMethod);
			
			driver.get(memoryData.getURL());
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
			waitForDivErrMsg.until(ExpectedConditions
					.visibilityOfElementLocated(expression.startsWith("/") ? By.xpath(expression) : By.id(expression)));
			WebElement labelErr = waitForDivErrMsg.until(
					presenceOfElementLocated(expression.startsWith("/") ? By.xpath(expression) : By.id(expression)));

			Assert.assertTrue(labelErr.getText().contentEquals(datatest.get(MemoryData.VALUE_2_EVALUATE)));

		} catch (Throwable exc) {
			Assert.fail("Error in " + testMethod + ": " + exc.getMessage());

		} finally {
			WebdriverObject.killDriverInstance();
		}
	}


}