package com.examples;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;


import org.junit.Assert;
import org.junit.jupiter.api.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import junit.framework.TestCase;

/***
 * Tests with SeleniumHQ WebDriver
 */
public class GedeonesQueryTest extends TestCase {


	/**
	 * Create the test case
	 *
	 * @param testName name of the test case
	 */
	public GedeonesQueryTest() {
		super("Query Gedeones");

	}

	@Test
	public void testLoginErrUser() {

		WebDriver driver = WebdriverObject.getWebDriverInstance();
		MemoryData memoryData = WebdriverObject.getWebDriverInstance();
		try {
			
			Map<String, String> datatest = memoryData.getDatosEscenarioTest("testLoginErrUser");
			
			WebElement entryUserForm = driver.findElement(By.name("entryForm.user"));
			WebElement entryPaswdForm = driver.findElement(By.name("entryForm.password"));

			entryUserForm.sendKeys(datatest.entry.get("entryForm.user"));
			entryPaswdForm.sendKeys(datatest.entry.get("entryForm.password"));

			WebElement submitFormElement = driver.findElement(By.id(datatest.entry.get(MemoryData.SUBMIT_ELEMENT)));
			submitFormElement.click();
			
			Assert.assertTrue(true);
			
		} catch (Exception exc) {
			System.out.println("Error in testLoginErrUser: " + exc.getMessage());
			exc.printStackTrace();
		}
	}
	
	@Test
	public void testLoginErrPass() {

		WebDriver driver = WebdriverObject.getWebDriverInstance();
		MemoryData memoryData = WebdriverObject.getWebDriverInstance();
		try {
			
			Map<String, String> datatest = memoryData.getDatosEscenarioTest("testLoginErrUser");
			
			WebElement entryUserForm = driver.findElement(By.name("entryForm.user"));
			WebElement entryPaswdForm = driver.findElement(By.name("entryForm.password"));

			entryUserForm.sendKeys(datatest.entry.get("entryForm.user"));
			entryPaswdForm.sendKeys(datatest.entry.get("entryForm.password"));

			WebElement submitFormElement = driver.findElement(By.id(datatest.entry.get(MemoryData.SUBMIT_ELEMENT)));
			submitFormElement.click();
			
			Assert.assertTrue(true);
			
		} catch (Exception exc) {
			System.out.println("Error in testLoginErrPass: " + exc.getMessage());
			exc.printStackTrace();
		}
	}
	
	
	@Test
	public void testLoginSucess() {
		WebDriver driver = WebdriverObject.getWebDriverInstance();
		try {

			// "dhtmlgoodies_treeNode20": id del nodo Gedeones cuando entramos como Administrador
			WebDriverWait waitForTree = new WebDriverWait(driver, Long.valueOf(5));
			waitForTree.until(ExpectedConditions.visibilityOfElementLocated(By.id("dhtmlgoodies_tree")));// o la Xpath
																											// "//UL[@id="dhtmlgoodies_tree"]
			WebElement arbolNavegacion = waitForTree.until(presenceOfElementLocated(By.id("dhtmlgoodies_tree")));
			Assert.assertTrue(arbolNavegacion.isDisplayed());

			WebElement seguimientoFolder = arbolNavegacion.findElement(By.xpath("//A[@id='dhtmlgoodies_treeNode20']"));
			Assert.assertTrue(seguimientoFolder.getText().contains("Seguimiento"));

			seguimientoFolder.click();

			WebElement hrefGEDEONES = seguimientoFolder.findElement(By.xpath("//A[@id='dhtmlgoodies_treeNode21']"));
			Assert.assertTrue(hrefGEDEONES.getText().contains("GEDEON"));
			hrefGEDEONES.click();

			WebDriverWait wait = new WebDriverWait(driver, Long.valueOf(20));
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("principal")));
			WebElement divPral = wait.until(presenceOfElementLocated(By.xpath("//div[@id='principal']")));
			// List<WebElement> countResults = divPral.findElements(By.tagName("LEGEND"));

			// Assert.assertTrue(countResults.size() > 0);

			Assert.assertTrue(divPral.getText().contains("Resultados del  1 al"));

		} catch (Exception exc) {
			System.out.println("Error in testLoginSucess:" + exc.getMessage());
			exc.printStackTrace();
		} finally {
			WebdriverObject.reinitializeDriver();
		}

	}

}