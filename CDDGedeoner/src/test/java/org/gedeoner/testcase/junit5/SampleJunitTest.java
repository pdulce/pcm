package org.gedeoner.testcase.junit5;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

/***
 * Tests login feature for SeleniumHQ WebDriver
 */
public class SampleJunitTest  {
	
	@Test
	public void setup() {		
		try {			
			Assert.assertTrue(Integer.parseInt("1") + Integer.parseInt("5") == 6);
		} catch (Throwable exc) {
			Assert.fail("Error in sample empty test :" + exc.getMessage());	
		}
	}

}