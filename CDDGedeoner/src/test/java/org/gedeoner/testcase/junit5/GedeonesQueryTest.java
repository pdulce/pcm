package org.gedeoner.testcase.junit5;

import java.util.HashMap;
import java.util.Map;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

import org.gedeoner.testcase.resource.MemoryData;
import org.gedeoner.testcase.resource.WebdriverObject;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

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
	public void testQueryEvent() {

		WebDriver driver = WebdriverObject.getWebDriverInstance();
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
			
			
			/*entryPeticionID2search.sendKeys(searchingExpressions[0]);
			submitFormElement = driver.findElement(By.id("query"));
			submitFormElement.click();

			String element2Check = datatest.get(MemoryData.ELEMENT_2_EVALUATE);
			waitForDivResults = new WebDriverWait(driver, Long.valueOf(10));
			waitForDivResults.until(
					ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='" + element2Check + "']")));
			divResultados = waitForDivResults
					.until(presenceOfElementLocated(By.xpath("//input[@id='" + element2Check + "']")));
			Assert.assertTrue(divResultados.getAttribute("value").contains("peticiones.id=" + searchingExpressions[0]));

			if (searchingExpressions.length == 2) {
				entryPeticionID2search = driver.findElement(By.name("peticiones.id"));
				entryPeticionID2search.sendKeys(searchingExpressions[1]);
				submitFormElement = driver.findElement(By.id("query"));
				submitFormElement.click();

				waitForDivResults = new WebDriverWait(driver, Long.valueOf(10));
				waitForDivResults
						.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//TABLE[@class='pcmTable']")));
				divResultados = waitForDivResults
						.until(presenceOfElementLocated(By.xpath("//TABLE[@class='pcmTable']")));
				Assert.assertTrue(divResultados.getText().contains("No hay datos"));

			}*/

		} catch (Throwable exc) {
			Assert.fail("Error in testLoginSucess:" + exc.getMessage());
		} finally {
			WebdriverObject.killDriverInstance();
		}

	}

}