package pcm.test.selenium.initial;

import org.openqa.selenium.By;
import java.lang.annotation.Annotation;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

public class SignInPageTest{
	
	private WebElement selenium;

    public SignInPageTest(WebElement siteElement) {
    	this.selenium = siteElement;
	}


    @Test
    public void loginValidUser(String userName, String password) {
    	/**
		   	 if(!siteElement.findElement(By.tagName("title")).toString().equals("Sign in page")) {
		            throw new IllegalStateException("This is not sign in page, current page is: "
		                            +selenium.getLocation());
		   	 }
            selenium.type("usernamefield", userName);
            selenium.type("passwordfield", password);
            selenium.click("sign-in");
            selenium.waitForPageToLoad("waitPeriod");

            //return new HomePage(selenium);**/
    	Assert.assertTrue(selenium.findElement(By.id("entryForm.user")).isDisplayed());
    }

    
}