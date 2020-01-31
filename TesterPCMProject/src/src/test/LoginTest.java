package src.test;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/***
 * Tests login feature
 */
public class LoginTest {
	
	
	static {
		System.setProperty("webdriver.gecko.driver", "/home/pedro/gecko/geckodriver");
	}
	
	@Test
	public void testLoginExists() {
		WebDriver driver = new FirefoxDriver();
        
        
        try {
            driver.get("http://localhost:8081/sg/prjManager");
            WebElement entryUserForm = driver.findElement(By.name("entryForm.user"));
            WebElement entryPaswdForm = driver.findElement(By.name("entryForm.password"));
                        
            Assert.assertTrue(entryUserForm.isDisplayed());
            Assert.assertTrue(entryPaswdForm.isDisplayed());
            
            entryUserForm.sendKeys("admin");
            entryPaswdForm.sendKeys("admin");
            
            WebElement submitFormElement = driver.findElement(By.id("submitForm"));
            submitFormElement.click();
             
            WebDriverWait wait = new WebDriverWait(driver, Long.valueOf(5));
            
            WebElement divPral = wait.until(presenceOfElementLocated(By.xpath("//div[@id='principal']")));
           
            Assert.assertTrue(divPral.isDisplayed());
            
            //System.out.println(divPral.getText());
         
            Assert.assertTrue(divPral.getText().contains("Bienvenid@ Administrador, comience a navegar por el árbol lateral de servicios"));
                        
        }catch (Exception exc) {
        	System.out.println("Error " + exc.getMessage());
        	//exc.printStackTrace();
        } finally {
            driver.quit();
        }
		
	}

}