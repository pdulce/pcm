package org.gedeoner.testcase.junit5;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import junit.framework.TestCase;

/***
 * Tests login feature for SeleniumHQ WebDriver
 */
public class SampleJunitTest extends TestCase {

	/**
	 * Create the test case: from PC ISM modified
	 *
	 * @param testName name of the test case
	 */
	public SampleJunitTest() {
		super("JunitTest5 ");
	}

	@Test
	public void testLoginSucess() {		
		try {			
			Assert.assertTrue(Integer.parseInt("1") + Integer.parseInt("5") == 6);
		} catch (Throwable exc) {
			Assert.fail("Error in test :" + exc.getMessage());	
		}
	}

}