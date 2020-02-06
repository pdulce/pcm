package /*test.java.*/com.examples;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

import java.util.Map;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import junit.framework.TestCase;

public class JunitTest4Example extends TestCase{
	
	public JunitTest4Example(){
		super("JunitTest4Example");
	}
	
	
	private void makeAccessWithData(String testMethod){
    	
    	MemoryData memoryData = WebdriverObject.getMemoryData();
    	WebDriver driver = WebdriverObject.getWebDriverInstance();
		try {
			
			Map<String, String> datatest = memoryData.getDatosEscenarioTest(testMethod);
			
			WebElement entryUserForm = driver.findElement(By.name("entryForm.user"));
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
			
		} catch (Exception exc) {
			System.out.println("Error in testLoginErrUser: " + exc.getMessage());
			exc.printStackTrace();
			throw exc;
		}  	
    }
	
    
	public void tests() {
		try {
			makeAccessWithData("testLoginErrUser");
			makeAccessWithData("testLoginErrPass");
			makeAccessWithData("testLoginSucess");
		} catch (Exception exc) {
			System.out.println("Error in testLoginErrUser: " + exc.getMessage());
			exc.printStackTrace();
		}finally {
			WebdriverObject.reinitializeDriver();
		}
	}
	
	
}
