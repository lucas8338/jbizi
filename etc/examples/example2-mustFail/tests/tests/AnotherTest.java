package tests;

import ex.Calculate;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AnotherTest {
	@Test
	public void test1(){
		Assert.assertEquals(new Calculate(10, 2).sum(), 12);
	}
	
	@Test
	public void test2(){
		Assert.assertEquals(new Calculate(10, 10).sum(), 40);
	}
	
	@Test
	public void test3(){
		Assert.assertEquals(new Calculate(10, 20).sum(), 30);
	}
}
