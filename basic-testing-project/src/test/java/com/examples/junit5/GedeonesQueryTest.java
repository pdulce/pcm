package com.examples.junit5;

import java.util.HashMap;
import java.util.Map;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.examples.MemoryData;


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
		
		WebDriver driver = WebdriverObject.getWebDriverInstance("Data.xlsx");
		MemoryData memoryData = WebdriverObject.getMemoryData();
		try {
			
			/*** BLOQUE PARA VALIDARTE CON EXITO ***/
			Map<String, String> datatest = new HashMap<String, String>();
			datatest.putAll(memoryData.getDatosEscenarioTest("testLoginSucess"));
			
			WebElement entryUserForm = driver.findElement(By.name("entryForm.user"));
			WebElement entryPaswdForm = driver.findElement(By.name("entryForm.password"));
			entryUserForm.sendKeys(datatest.get("entryForm.user"));
			entryPaswdForm.sendKeys(datatest.get("entryForm.password"));
			WebElement submitFormElement = driver.findElement(By.id(datatest.get(MemoryData.SUBMIT_ELEMENT)));
			submitFormElement.click();
			/*** FIN BLOQUE PARA VALIDARTE CON EXITO ***/
						
			/*** BLOQUE EXCLUSIVO DE LA PRUEBA O TEST QUE VAMOS A REALIZAR ***/
			datatest.clear();
			datatest.putAll(memoryData.getDatosEscenarioTest("testQueryEvent"));			
			String expression = datatest.get("partialLink1");
			WebDriverWait waitForTree = new WebDriverWait(driver, Long.valueOf(10));
			By by = expression.startsWith("/") ? By.xpath(expression) : By.id(expression);
			
			waitForTree.until(ExpectedConditions.visibilityOfElementLocated(by));
			WebElement arbolNavegacion = waitForTree.until(presenceOfElementLocated(by));
			Assert.assertTrue(arbolNavegacion.isDisplayed());

			expression = datatest.get("partialLink2");
			by = expression.startsWith("/") ? By.xpath(expression) : By.id(expression);
			WebElement seguimientoFolder = arbolNavegacion.findElement(by);
			Assert.assertTrue(seguimientoFolder.getText().contains("Seguimiento"));
			seguimientoFolder.click();//pinchamos para abrir la carpeta que contiene el nodo buscado
			
			expression = datatest.get("partialLink3");
			by = expression.startsWith("/") ? By.xpath(expression) : By.id(expression);
			WebElement hrefGEDEONES = seguimientoFolder.findElement(by);
			Assert.assertTrue(hrefGEDEONES.getText().contains("GEDEON"));
			hrefGEDEONES.click();//nodo del escenario buscado, pinchado	
			
			/**** FIRST CHECK OF RESULT VALUES SHOWN IN SCREEN ****/
			String element2Check = datatest.get(MemoryData.ELEMENT_2_EVALUATE);
			WebDriverWait waitForDivResults = new WebDriverWait(driver, Long.valueOf(10));
			by = element2Check.startsWith("/") ? By.xpath(element2Check) : By.id(element2Check);
			waitForDivResults.until(ExpectedConditions.visibilityOfElementLocated(by));
			WebElement resultadoElement = waitForDivResults.until(presenceOfElementLocated(by));
			Assert.assertTrue(!resultadoElement.getAttribute("value").isEmpty());
			
			String searchingExpressions[] = datatest.get("incidenciasProyecto.id").split("#");
			WebElement entryPeticionID2search = driver.findElement(By.name("incidenciasProyecto.id"));
			entryPeticionID2search.sendKeys(searchingExpressions[0]);
			submitFormElement = driver.findElement(By.id("query"));
			submitFormElement.click();
			
			/**** SECOND/LAST CHECK OF RESULT VALUES SHOWN IN SCREEN ****/			
			waitForDivResults = new WebDriverWait(driver, Long.valueOf(10));
			by = element2Check.startsWith("/") ? By.xpath(element2Check) : By.id(element2Check);
			waitForDivResults.until(ExpectedConditions.visibilityOfElementLocated(by));
			resultadoElement = waitForDivResults.until(presenceOfElementLocated(by));			
			Assert.assertTrue(resultadoElement.getAttribute("value").contains("incidenciasProyecto.id="+searchingExpressions[0]));
			
			if (searchingExpressions.length == 2){
				entryPeticionID2search = driver.findElement(By.name("incidenciasProyecto.id"));
				entryPeticionID2search.sendKeys(searchingExpressions[1]);
				submitFormElement = driver.findElement(By.id("query"));
				submitFormElement.click();
				
				waitForDivResults = new WebDriverWait(driver, Long.valueOf(10));			
				waitForDivResults.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//TABLE[@class='pcmTable']")));
				resultadoElement = waitForDivResults.until(presenceOfElementLocated(By.xpath("//TABLE[@class='pcmTable']")));			
				Assert.assertTrue(resultadoElement.getText().contains("No hay datos"));

			}
			
		} catch (Throwable exc) {
			System.out.println("Error in testLoginSucess:" + exc.getMessage());
			exc.printStackTrace();
		} finally {
			WebdriverObject.reinitializeDriver();
		}
	
	
	}

}