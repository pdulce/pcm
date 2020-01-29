package pcm.test.selenium.initial;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import junit.framework.Test;
import junit.framework.TestSuite;

public class Selenium {
	
	public Selenium() {
        WebDriver driver = new FirefoxDriver();
        WebDriverWait wait = new WebDriverWait(driver, Long.valueOf(5));
        WebElement siteElement = null;
        try {
        	String url2Test = "http://localhost:8081/sg/prjManager";
            driver.get(url2Test);
            siteElement = wait.until(presenceOfElementLocated(By.tagName("title")));
            System.out.println(siteElement.getAttribute("textContent"));
            
            TestSuite suite= new TestSuite();
            suite.addTest((Test) new SignInPageTest(siteElement));
            suite.addTest((Test) new LoginTest(siteElement));
            
            //do tests
        } finally {
            driver.quit();
        }
        
    }
	
	
	
	
}
