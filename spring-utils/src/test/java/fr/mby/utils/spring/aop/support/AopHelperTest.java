/**
 * Copyright 2013 Maxime Bossard
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.mby.utils.spring.aop.support;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

/**
 * @author Maxime Bossard - 2013.
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
		final StringImpl stringImpl = new StringImpl();
		final IntegerImpl integerImpl = new IntegerImpl();

		final boolean result1 = AopHelper.supportsType(stringImpl, String.class, InterfaceTest.class);
		Assert.assertTrue("Should be true !", result1);

		final boolean result2 = AopHelper.supportsType(stringImpl, Integer.class, InterfaceTest.class);
		Assert.assertFalse("Should be false !", result2);

		final boolean result3 = AopHelper.supportsType(stringImpl, Long.class, InterfaceTest.class);
		Assert.assertFalse("Should be false !", result3);

		final boolean result4 = AopHelper.supportsType(integerImpl, String.class, InterfaceTest.class);
		Assert.assertFalse("Should be false !", result4);

		final boolean result5 = AopHelper.supportsType(integerImpl, Integer.class, InterfaceTest.class);
		Assert.assertTrue("Should be true !", result5);

		final boolean result6 = AopHelper.supportsType(integerImpl, Long.class, InterfaceTest.class);
		Assert.assertFalse("Should be false !", result6);
	}

	/**
	 * Test second level of implementation.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSupportsType2() throws Exception {
		final StringImpl stringImpl = new StringImpl2();
		final IntegerImpl integerImpl = new IntegerImpl2();

		final boolean result1 = AopHelper.supportsType(stringImpl, String.class, InterfaceTest.class);
		Assert.assertTrue("Should be true !", result1);

		final boolean result2 = AopHelper.supportsType(stringImpl, Integer.class, InterfaceTest.class);
		Assert.assertFalse("Should be false !", result2);

		final boolean result3 = AopHelper.supportsType(stringImpl, Long.class, InterfaceTest.class);
		Assert.assertFalse("Should be false !", result3);

		final boolean result4 = AopHelper.supportsType(integerImpl, String.class, InterfaceTest.class);
		Assert.assertFalse("Should be false !", result4);

		final boolean result5 = AopHelper.supportsType(integerImpl, Integer.class, InterfaceTest.class);
		Assert.assertTrue("Should be true !", result5);

		final boolean result6 = AopHelper.supportsType(integerImpl, Long.class, InterfaceTest.class);
		Assert.assertFalse("Should be false !", result6);
	}

	/**
	 * Test second level of implementation.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSupportsType3() throws Exception {
		final StringImpl2 stringImpl = new StringImpl2();
		final IntegerImpl2 integerImpl = new IntegerImpl2();

		final boolean result1 = AopHelper.supportsType(stringImpl, String.class, InterfaceTest.class);
		Assert.assertTrue("Should be true !", result1);

		final boolean result2 = AopHelper.supportsType(stringImpl, Integer.class, InterfaceTest.class);
		Assert.assertFalse("Should be false !", result2);

		final boolean result3 = AopHelper.supportsType(stringImpl, Long.class, InterfaceTest.class);
		Assert.assertFalse("Should be false !", result3);

		final boolean result4 = AopHelper.supportsType(integerImpl, String.class, InterfaceTest.class);
		Assert.assertFalse("Should be false !", result4);

		final boolean result5 = AopHelper.supportsType(integerImpl, Integer.class, InterfaceTest.class);
		Assert.assertTrue("Should be true !", result5);

		final boolean result6 = AopHelper.supportsType(integerImpl, Long.class, InterfaceTest.class);
		Assert.assertFalse("Should be false !", result6);
	}

	/**
	 * Test different inheritence level.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSupportsType4() throws Exception {
		final NumberImpl numberImpl = new NumberImpl();

		final boolean result4 = AopHelper.supportsType(numberImpl, String.class, InterfaceTest.class);
		Assert.assertFalse("Should be false !", result4);

		final boolean result5 = AopHelper.supportsType(numberImpl, Integer.class, InterfaceTest.class);
		Assert.assertTrue("Should be true !", result5);

		final boolean result6 = AopHelper.supportsType(numberImpl, Number.class, InterfaceTest.class);
		Assert.assertTrue("Should be true !", result6);
	}

	/**
	 * Test different inheritence level with second level of implementation.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSupportsType5() throws Exception {
		final NumberImpl2 numberImpl = new NumberImpl2();

		final boolean result4 = AopHelper.supportsType(numberImpl, String.class, InterfaceTest.class);
		Assert.assertFalse("Should be false !", result4);

		final boolean result5 = AopHelper.supportsType(numberImpl, Integer.class, InterfaceTest.class);
		Assert.assertTrue("Should be true !", result5);

		final boolean result6 = AopHelper.supportsType(numberImpl, Number.class, InterfaceTest.class);
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
