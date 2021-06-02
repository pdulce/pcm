package org.gedeoner.testcase.junit5;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

import java.util.Map;

import org.gedeoner.testcase.resource.MemoryData;
import org.gedeoner.testcase.resource.WebdriverObject;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import org.openqa.selenium.By;
//import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import junit.framework.TestCase;

/***
 * Tests login feature for SeleniumHQ WebDriver
 */
public class LoginTest extends TestCase {

	/**
	 * Create the test case: from PC ISM modified
	 *
	 * @param testName name of the test case
	 */
	public LoginTest() {
		super("JunitTest5 ");
	}

	@Test
	public void testLoginSucess() {
		makeAccessWithData("testLoginSucess");
	}

	@Test
	public void testLoginErrUser() {
		makeAccessWithData("testLoginErrUser");
	}

	@Test
	public void testLoginErrPass() {
		makeAccessWithData("testLoginErrPass");
	}
	
	private void makeAccessWithData(String testMethod) {

		WebDriver driver = WebdriverObject.getWebDriverInstance();
		MemoryData memoryData =  MemoryData.getUniqueInstance();
		try {			
			Map<String, String> datatest = memoryData.getDatosEscenarioTest(testMethod);

			WebDriverWait waitForTree = new WebDriverWait(driver, Long.valueOf(5));
			waitForTree.until(ExpectedConditions.visibilityOfElementLocated(By.id("entryForm.user")));
			WebElement entryUserForm = waitForTree.until(presenceOfElementLocated(By.id("entryForm.user")));
			WebElement entryPaswdForm = driver.findElement(By.name("entryForm.password"));

			entryUserForm.sendKeys(datatest.get("entryForm.user"));
			entryPaswdForm.sendKeys(datatest.get("entryForm.password"));

			WebElement submitFormElement = driver.findElement(By.id(datatest.get(MemoryData.SUBMIT_ELEMENT)));
			submitFormElement.click();

			WebDriverWait waitForDivErrMsg = new WebDriverWait(driver, Long.valueOf(5));
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