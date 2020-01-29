package pcm.test.selenium.initial;

import java.lang.annotation.Annotation;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/***
 * Tests login feature
 */
public class LoginTest {

	private WebElement selenium;

	public LoginTest(WebElement selenium_) {
		this.selenium = selenium_;
	}

	@Test
	public void testLoginExists() {
		// SignInPage signInPage = new SignInPage(seleniumWebDriverElement);
		// HomePage homePage = signInPage.loginValidUser("userName", "password");
		// Assert.assertTrue(webElem.isElementPresent("compose button"),
		// "Login was unsuccessful");
		Assert.assertTrue(selenium.findElement(By.id("entryForm.user")).isDisplayed());
	}

}