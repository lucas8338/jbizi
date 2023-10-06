package tests;

import ex.Calculate;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestCalculate {
	@Test
	public void test1(){
		Assert.assertEquals(new Calculate(10, 2).sum(), 12);
	}
}
