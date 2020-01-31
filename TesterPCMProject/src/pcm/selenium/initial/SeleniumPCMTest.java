package pcm.test.selenium.initial;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;

@RunWith(Suite.class)
@SuiteClasses({/*SignInPageTest.class, LoginTest.class*/SauceLabsHomePageTest.class})
public class SeleniumPCMTest{
	
	public SeleniumPCMTest() {
        /*WebDriver driver = new FirefoxDriver();
        WebDriverWait wait = new WebDriverWait(driver, Long.valueOf(5));
        WebElement siteElement = null;
        try {
        	String url2Test = "http://localhost:8081/sg/prjManager";
            driver.get(url2Test);
            siteElement = wait.until(presenceOfElementLocated(By.tagName("title")));
            System.out.println(siteElement.getAttribute("textContent"));
            
            
            this.addTest((Test) new SignInPageTest(siteElement));
            this.addTest((Test) new LoginTest(siteElement));
            
            TestResult resultado = new TestResult();
            //do tests
            this.run(resultado);
            
            int erroneos = resultado.errorCount();
            System.out.println("tests erroneos " + erroneos);
            
        } finally {
            driver.quit();
        }*/
        
    }
	
	
	
}
