package org.gedeoner.selenium;

import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

import junit.framework.TestCase;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

/***
 * Tests with SeleniumHQ WebDriver
 */
public class MySeleniumTest extends TestCase{
	
	private WebDriver driver;
	static {
		//System.setProperty("webdriver.gecko.driver", "C:\\webtools\\geckodriver.exe");		
		/**ANOTHER WEB DRIVERS**/
		System.setProperty("webdriver.chrome.driver", "C:\\webtools\\chromedriver.exe");    
	}
	
	@AfterTest
	protected void tearDown(){
		driver.quit();
	}
	
	@Test (groups = { "login", "query"})	
	public void testQueryGedeones() {
		
		if (driver == null) {
			//driver = new FirefoxDriver();
			driver = new ChromeDriver();
		}
		System.out.println("testing queryGedeones WebDriver SELENIUM");		 	
		MemoryData memoryData =  MemoryData.getUniqueInstance();
		
		try {			
			/*** BLOQUE PARA VALIDARTE CON EXITO ***/
			Map<String, String> datatest = new HashMap<String, String>();
			datatest.putAll(memoryData.getDatosEscenarioTest("testLoginSucess"));

			driver.get(memoryData.getURL());
			WebDriverWait waitForHome = new WebDriverWait(driver, Long.valueOf(10));
			waitForHome.until(ExpectedConditions.visibilityOfElementLocated(By.id("entryForm.user")));
			WebElement entryUserForm = driver.findElement(By.name("entryForm.user"));
			WebElement entryPaswdForm = driver.findElement(By.name("entryForm.password"));
			entryUserForm.sendKeys(datatest.get("entryForm.user"));
			entryPaswdForm.sendKeys(datatest.get("entryForm.password"));
			WebElement submitFormElement = driver.findElement(By.id(datatest.get(MemoryData.SUBMIT_ELEMENT)));
			submitFormElement.click();
			/*** FIN BLOQUE PARA AUTENTICACION CON EXITO ***/
			
			WebDriverWait waitForMenu = new WebDriverWait(driver, Long.valueOf(10));			
			datatest.clear();
			datatest.putAll(memoryData.getDatosEscenarioTest("testQueryEvent"));
			String menuSuperior = datatest.get("partialLink1");
			waitForMenu.until(ExpectedConditions.visibilityOfElementLocated(By.id(menuSuperior)));
			WebElement menu = driver.findElement(By.id(menuSuperior));
			if (menu != null) {
				Assert.assertTrue(menu.isDisplayed());
			}
			
			String menuContainer = datatest.get("partialLink2");
			WebElement seguimientoFolder = menu.findElement(menuContainer.startsWith("/") ? By.xpath(menuContainer) : By.id(menuContainer));
			Assert.assertTrue(seguimientoFolder.getText().contains("Seguimiento"));
			seguimientoFolder.click();

			String menuEntry = datatest.get("partialLink3");
			WebElement hrefGEDEONES = seguimientoFolder
					.findElement(menuEntry.startsWith("/") ? By.xpath(menuEntry) : By.id(menuEntry));
			Assert.assertTrue(hrefGEDEONES.getText().contains("Peticiones"));
			hrefGEDEONES.click();

			WebDriverWait waitForDivResults = new WebDriverWait(driver, Long.valueOf(10));
			waitForDivResults.until(ExpectedConditions.visibilityOfElementLocated(By.id("principal")));
			WebElement divResultados = waitForDivResults.until(presenceOfElementLocated(By.id("principal")));
			Assert.assertTrue(divResultados.getText().contains("Resultados del  1 al  25"));

			WebElement entryPeticionID2search = driver.findElement(By.id("peticionesSel.id0"));
			Long idPeticion = Long.valueOf(entryPeticionID2search.getAttribute("value").split("=")[1]);
			Assert.assertTrue(idPeticion > 0);			
					
		} catch (Throwable exc) {
			Assert.fail("Error in queryGedeonesTest:" + exc.getMessage());
		}finally {
			driver.quit();
		}
	}
	
	/*@Test (groups = { "login"})	
	public void testLoginErrUser() {
		makeAccessWithData("testLoginErrUser");
	}
	
	@Test (groups = { "login"})	
	public void testLoginErrPass() {
		makeAccessWithData("testLoginErrPass");
	}

	
	@Test (groups = { "login"})	
	public void testLoginSucess() {
		makeAccessWithData("testLoginSucess");
	}*/

	
	
	private void makeAccessWithData(String testMethod) {

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
		}
	}

}