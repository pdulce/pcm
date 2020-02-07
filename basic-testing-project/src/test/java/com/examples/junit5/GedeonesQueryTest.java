package com.examples.junit5;

import java.util.HashMap;
import java.util.Map;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.examples.MemoryData;
import com.examples.WebdriverObject;


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
			WebElement arbolNavegacion = waitForTree.until(presenceOfElementLocated(expression.startsWith("/")?By.xpath(expression):By.id(expression)));
			Assert.assertTrue(arbolNavegacion.isDisplayed());

			expression = datatest.get("partialLink2");
			WebElement seguimientoFolder = arbolNavegacion.findElement(expression.startsWith("/")?By.xpath(expression):By.id(expression));
			Assert.assertTrue(seguimientoFolder.getText().contains("Seguimiento"));
			seguimientoFolder.click();//pinchamos para abrir la carpeta que contiene el nodo buscado
			
			expression = datatest.get("partialLink3");
			WebElement hrefGEDEONES = seguimientoFolder.findElement(expression.startsWith("/")?By.xpath(expression):By.id(expression));
			Assert.assertTrue(hrefGEDEONES.getText().contains("GEDEON"));
			hrefGEDEONES.click();//nodo del escenario buscado, pinchado			
			
			expression = datatest.get(MemoryData.ELEMENT_2_EVALUATE);
			WebDriverWait waitForDivResults = new WebDriverWait(driver, Long.valueOf(10));			
			waitForDivResults.until(ExpectedConditions.visibilityOfElementLocated(expression.startsWith("/")?By.xpath(expression):By.id(expression)));
			WebElement divResultados = waitForDivResults.until(presenceOfElementLocated(expression.startsWith("/")?By.xpath(expression):By.id(expression)));			
			Assert.assertTrue(divResultados.getText().contains(datatest.get(MemoryData.VALUE_2_EVALUATE)));
			
			/** consignar un valor en el input de C�d.Petici�n y jugar con el valor esperado si es el err�neo, y el v�lido-
			 * Entre los resultados, testear si est� el valor buscado, igual haciendo lo mismo buscando algo imposible de que exista.
			 */
			String searchingExpressions[] = datatest.get("incidenciasProyecto.id").split("#");
			WebElement entryPeticionID2search = driver.findElement(By.name("incidenciasProyecto.id"));
			entryPeticionID2search.sendKeys(searchingExpressions[0]);
			submitFormElement = driver.findElement(By.id("query"/*datatest.get(MemoryData.SUBMIT_ELEMENT)*/));
			submitFormElement.click();
			waitForDivResults = new WebDriverWait(driver, Long.valueOf(10));			
			waitForDivResults.until(ExpectedConditions.visibilityOfElementLocated(searchingExpressions[0].startsWith("/")?By.xpath(searchingExpressions[0]):By.id(searchingExpressions[0])));
			divResultados = waitForDivResults.until(presenceOfElementLocated(searchingExpressions[0].startsWith("/")?By.xpath(searchingExpressions[0]):By.id(searchingExpressions[0])));			
			Assert.assertTrue(divResultados.getText().contains(searchingExpressions[0]));
			
			if (searchingExpressions.length == 2){
				entryPeticionID2search = driver.findElement(By.name("incidenciasProyecto.id"));
				entryPeticionID2search.sendKeys(searchingExpressions[1]);
				submitFormElement = driver.findElement(By.id("query"));
				submitFormElement.click();
				
				waitForDivResults = new WebDriverWait(driver, Long.valueOf(10));			
				waitForDivResults.until(ExpectedConditions.visibilityOfElementLocated(searchingExpressions[1].startsWith("/")?By.xpath(searchingExpressions[1]):By.id(searchingExpressions[1])));
				divResultados = waitForDivResults.until(presenceOfElementLocated(searchingExpressions[1].startsWith("/")?By.xpath(searchingExpressions[1]):By.id(searchingExpressions[1])));			
				Assert.assertTrue(divResultados.getText().contains(searchingExpressions[1]));

			}
			
		} catch (Exception exc) {
			System.out.println("Error in testLoginSucess:" + exc.getMessage());
			exc.printStackTrace();
		} finally {
			WebdriverObject.reinitializeDriver();
		}
	
	}

}