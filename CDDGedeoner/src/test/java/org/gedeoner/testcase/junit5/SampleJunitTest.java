package org.gedeoner.testcase.junit5;

import org.testng.Assert;
import org.testng.annotations.Test;	

public class SampleJunitTest{
	
	 @Test(groups = { "functest", "checkintest" })

	   public void testPrintMessage() {
	      System.out.println("testing testsuite JUNIT");
	      Assert.assertEquals("aaa", "aaa");
	   }

	
	@Test (groups = { "functest"})	
	public void sampleTest1() {		
		try {			
			Assert.assertTrue(Integer.parseInt("1") + Integer.parseInt("5") == 6);
		} catch (Throwable exc) {
			Assert.fail("Error in sample1 test :" + exc.getMessage());	
		}
	}
	
	@Test (groups = { "checkintest"})	
	public void sampleTest2() {		
		try {			
			Assert.assertTrue(Integer.parseInt("5") + Integer.parseInt("15") == 20);
		} catch (Throwable exc) {
			Assert.fail("Error in sample2 test :" + exc.getMessage());	
		}
	}

}