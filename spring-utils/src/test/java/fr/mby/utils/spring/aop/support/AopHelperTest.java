/**
 * 
 */
package fr.mby.utils.spring.aop.support;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import fr.mby.utils.spring.aop.support.AopHelper;

/**
 * @author Maxime BOSSARD.
 *
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class AopHelperTest {

	/**
	 * Test first level of implementation.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSupportsType1() throws Exception {
		StringImpl stringImpl = new StringImpl();
		IntegerImpl integerImpl = new IntegerImpl();
		
		boolean result1 = AopHelper.supportsType(stringImpl, String.class, InterfaceTest.class);
		Assert.assertTrue("Should be true !", result1);
		
		boolean result2 = AopHelper.supportsType(stringImpl, Integer.class, InterfaceTest.class);
		Assert.assertFalse("Should be false !", result2);
		
		boolean result3 = AopHelper.supportsType(stringImpl, Long.class, InterfaceTest.class);
		Assert.assertFalse("Should be false !", result3);
		
		boolean result4 = AopHelper.supportsType(integerImpl, String.class, InterfaceTest.class);
		Assert.assertFalse("Should be false !", result4);
		
		boolean result5 = AopHelper.supportsType(integerImpl, Integer.class, InterfaceTest.class);
		Assert.assertTrue("Should be true !", result5);
		
		boolean result6 = AopHelper.supportsType(integerImpl, Long.class, InterfaceTest.class);
		Assert.assertFalse("Should be false !", result6);
	}
	
	/**
	 * Test second level of implementation.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSupportsType2() throws Exception {
		StringImpl stringImpl = new StringImpl2();
		IntegerImpl integerImpl = new IntegerImpl2();
		
		boolean result1 = AopHelper.supportsType(stringImpl, String.class, InterfaceTest.class);
		Assert.assertTrue("Should be true !", result1);
		
		boolean result2 = AopHelper.supportsType(stringImpl, Integer.class, InterfaceTest.class);
		Assert.assertFalse("Should be false !", result2);
		
		boolean result3 = AopHelper.supportsType(stringImpl, Long.class, InterfaceTest.class);
		Assert.assertFalse("Should be false !", result3);
		
		boolean result4 = AopHelper.supportsType(integerImpl, String.class, InterfaceTest.class);
		Assert.assertFalse("Should be false !", result4);
		
		boolean result5 = AopHelper.supportsType(integerImpl, Integer.class, InterfaceTest.class);
		Assert.assertTrue("Should be true !", result5);
		
		boolean result6 = AopHelper.supportsType(integerImpl, Long.class, InterfaceTest.class);
		Assert.assertFalse("Should be false !", result6);
	}
	
	/**
	 * Test second level of implementation.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSupportsType3() throws Exception {
		StringImpl2 stringImpl = new StringImpl2();
		IntegerImpl2 integerImpl = new IntegerImpl2();
		
		boolean result1 = AopHelper.supportsType(stringImpl, String.class, InterfaceTest.class);
		Assert.assertTrue("Should be true !", result1);
		
		boolean result2 = AopHelper.supportsType(stringImpl, Integer.class, InterfaceTest.class);
		Assert.assertFalse("Should be false !", result2);
		
		boolean result3 = AopHelper.supportsType(stringImpl, Long.class, InterfaceTest.class);
		Assert.assertFalse("Should be false !", result3);
		
		boolean result4 = AopHelper.supportsType(integerImpl, String.class, InterfaceTest.class);
		Assert.assertFalse("Should be false !", result4);
		
		boolean result5 = AopHelper.supportsType(integerImpl, Integer.class, InterfaceTest.class);
		Assert.assertTrue("Should be true !", result5);
		
		boolean result6 = AopHelper.supportsType(integerImpl, Long.class, InterfaceTest.class);
		Assert.assertFalse("Should be false !", result6);
	}
	
	/**
	 * Test different inheritence level.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSupportsType4() throws Exception {
		NumberImpl numberImpl = new NumberImpl();

		boolean result4 = AopHelper.supportsType(numberImpl, String.class, InterfaceTest.class);
		Assert.assertFalse("Should be false !", result4);
		
		boolean result5 = AopHelper.supportsType(numberImpl, Integer.class, InterfaceTest.class);
		Assert.assertTrue("Should be true !", result5);
		
		boolean result6 = AopHelper.supportsType(numberImpl, Number.class, InterfaceTest.class);
		Assert.assertTrue("Should be true !", result6);
	}
	
	/**
	 * Test different inheritence level with second level of implementation.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSupportsType5() throws Exception {
		NumberImpl2 numberImpl = new NumberImpl2();

		boolean result4 = AopHelper.supportsType(numberImpl, String.class, InterfaceTest.class);
		Assert.assertFalse("Should be false !", result4);
		
		boolean result5 = AopHelper.supportsType(numberImpl, Integer.class, InterfaceTest.class);
		Assert.assertTrue("Should be true !", result5);
		
		boolean result6 = AopHelper.supportsType(numberImpl, Number.class, InterfaceTest.class);
		Assert.assertTrue("Should be true !", result6);
	}
	
	private interface InterfaceTest<T> {
		T test();
	}
	
	private class StringImpl implements InterfaceTest<String> {

		@Override
		public String test() {
			return "test";
		}
		
	}
	
	private class StringImpl2 extends StringImpl {
		
	}
	
	private class IntegerImpl implements InterfaceTest<Integer> {

		@Override
		public Integer test() {
			return 42;
		}
		
	}
	
	private class IntegerImpl2 extends IntegerImpl {
		
	}
	
	private class NumberImpl implements InterfaceTest<Number> {

		@Override
		public Number test() {
			return 42L;
		}
		
	}
	
	private class NumberImpl2 extends NumberImpl {
		
	}
}
