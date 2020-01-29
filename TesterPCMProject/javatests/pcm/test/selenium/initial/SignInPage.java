package pcm.test.selenium.initial;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

public class SignInPage {
	
	private Selenium selenium;

    public SignInPage(Selenium selenium) {
            this.selenium = selenium;
            if(!selenium.getTitle().equals("Sign in page")) {
                    throw new IllegalStateException("This is not sign in page, current page is: "
                                    +selenium.getLocation());
            }
    }

    /**
     * Login as valid user
     *
     * @param userName
     * @param password
     * @return HomePage object
     */
    public HomePage loginValidUser(String userName, String password) {
            selenium.type("usernamefield", userName);
            selenium.type("passwordfield", password);
            selenium.click("sign-in");
            selenium.waitForPageToLoad("waitPeriod");

            return new HomePage(selenium);
    }
    
}