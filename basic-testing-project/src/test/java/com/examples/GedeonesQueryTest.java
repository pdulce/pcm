package com.examples;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import junit.framework.TestCase;

/***
 * Tests login feature for SeleniumHQ WebDriver
 */
public class GedeonesQueryTest extends TestCase{
	
	static {
		System.setProperty("webdriver.gecko.driver", "/home/pedro/gecko/geckodriver");
	}
	
	 /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public GedeonesQueryTest( )
    {
        super( "loginTest" );
	
    }

	@Test
	public void testQuery() {
		WebDriver driver = WebdriverObject.getWebDriverInstance();
        try {
        	
            WebElement entryUserForm = driver.findElement(By.name("entryForm.user"));
            WebElement entryPaswdForm = driver.findElement(By.name("entryForm.password"));
                        
            entryUserForm.sendKeys("admin");
            entryPaswdForm.sendKeys("admin");
            
            WebElement submitFormElement = driver.findElement(By.id("submitForm"));
            submitFormElement.click();
            
            //"dhtmlgoodies_treeNode20": id del nodo Gedeones cuando entramos como Administrador
            WebDriverWait waitForTree = new WebDriverWait(driver, Long.valueOf(5));
            waitForTree.until(ExpectedConditions.visibilityOfElementLocated(By.id("dhtmlgoodies_tree")));//o la Xpath "//UL[@id="dhtmlgoodies_tree"]
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
            List<WebElement> countResults = divPral.findElements(By.tagName("LEGEND"));
            
            Assert.assertTrue(countResults.size() > 0);
            
            Assert.assertTrue(divPral.getText().contains("Resultados del  1 al"));
                        
        }catch (Exception exc) {
        	System.out.println("Error " + exc.getMessage());
        	exc.printStackTrace();
        } finally {
            driver.quit();
        }
		
	}

}