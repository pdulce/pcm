package pcm.test.selenium.initial;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

public class SauceLabsHomePageTest {

	static {
		System.setProperty("webdriver.gecko.driver", "/home/pedro/gecko/geckodriver");
	}

	@Test
	public void site_header_is_on_home_page() {

		//System.setProperty("webdriver.chrome.driver", "/opt/local/bin/chromedriver");
		WebDriver browser = new FirefoxDriver();
		browser.get("https://www.saucelabs.com");
		WebElement href = browser.findElement(By.xpath("//a[@href='/beta/login']"));
		assertTrue((href.isDisplayed()));

		browser.close();

	}

}