package pcm.test.selenium.initial;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

public class HomePage {

    private Selenium selenium;

    public HomePage(Selenium selenium) {
            if (!selenium.getTitle().equals("Home Page of logged in user")) {
                    throw new IllegalStateException("This is not Home Page of logged in user, current page" +
                                    "is: " +selenium.getLocation());
            }
    }

    public HomePage manageProfile() {
            // Page encapsulation to manage profile functionality
            return new HomePage(selenium);
    }

    /*More methods offering the services represented by Home Page
    of Logged User. These methods in turn might return more Page Objects
    for example click on Compose mail button could return ComposeMail class object*/

}