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

package fr.mby.utils.spring.beans.factory.annotation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fr.mby.utils.common.test.LoadRunner;
import fr.mby.utils.spring.beans.factory.IProxywiredManager;
import fr.mby.utils.spring.beans.factory.ProxywiredField;
import fr.mby.utils.test.ITest;

/**
 * @author Maxime Bossard - 2013
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:proxywiredAnotationBeanPostProcessorContext.xml")
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class ProxywiredAnotationBeanPostProcessorTest {

	@Autowired
	private Collection<ITest> iTestAutowired;

	@Proxywired
	private Collection<ITest> iTestProxywiredCollection;

	@Proxywired
	private List<ITest> iTestProxywiredList;

	@Proxywired
	private Set<ITest> iTestProxywiredSet;

	@Autowired
	private IProxywiredManager proxywiredManager;

	@Proxywired
	private ITest iTestSingleProxywired;

	@Proxywired
	@Qualifier("testB")
	private ITest iTestQualifiedSingleProxywired;

	/**
	 * Test injection and length.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test1() throws Exception {
		Assert.assertNotNull("Beans were not autowired !", this.iTestAutowired);
		Assert.assertEquals("Bad autowired bean count !", 3, this.iTestAutowired.size());

		Assert.assertNotNull("Beans were not proxywired !", this.iTestProxywiredCollection);
		Assert.assertEquals("Bad proxywired bean count !", 3, this.iTestProxywiredCollection.size());

		Assert.assertNotNull("Beans were not proxywired !", this.iTestProxywiredList);
		Assert.assertEquals("Bad proxywired bean count !", 3, this.iTestProxywiredList.size());

		Assert.assertNotNull("Beans were not proxywired !", this.iTestProxywiredSet);
		Assert.assertEquals("Bad proxywired bean count !", 3, this.iTestProxywiredSet.size());

		Assert.assertNotNull("Single bean was not proxywired !", this.iTestSingleProxywired);

		Assert.assertNotNull("Single bean was not proxywired !", this.iTestQualifiedSingleProxywired);
	}

	/**
	 * Test ordering of beans.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test2() throws Exception {
		Assert.assertNotNull("Beans were not autowired !", this.iTestAutowired);
		Assert.assertEquals("Bad autowired bean count !", 3, this.iTestAutowired.size());
		final Iterator<ITest> iterator1 = this.iTestAutowired.iterator();
		Assert.assertEquals("Bad proxywired ordering !", "TestA", iterator1.next().test());
		Assert.assertEquals("Bad proxywired ordering !", "TestB", iterator1.next().test());
		Assert.assertEquals("Bad proxywired ordering !", "TestC", iterator1.next().test());

		Assert.assertNotNull("Beans were not proxywired !", this.iTestProxywiredCollection);
		Assert.assertEquals("Bad proxywired bean count !", 3, this.iTestProxywiredCollection.size());
		final Iterator<ITest> iterator2 = this.iTestProxywiredCollection.iterator();
		Assert.assertEquals("Bad proxywired ordering !", "TestA", iterator2.next().test());
		Assert.assertEquals("Bad proxywired ordering !", "TestB", iterator2.next().test());
		Assert.assertEquals("Bad proxywired ordering !", "TestC", iterator2.next().test());

		Assert.assertNotNull("Beans were not proxywired !", this.iTestProxywiredList);
		Assert.assertEquals("Bad proxywired bean count !", 3, this.iTestProxywiredList.size());
		final Iterator<ITest> iterator3 = this.iTestProxywiredList.iterator();
		Assert.assertEquals("Bad proxywired ordering !", "TestA", iterator3.next().test());
		Assert.assertEquals("Bad proxywired ordering !", "TestB", iterator3.next().test());
		Assert.assertEquals("Bad proxywired ordering !", "TestC", iterator3.next().test());

		Assert.assertNotNull("Single bean was not proxywired !", this.iTestSingleProxywired);
		Assert.assertEquals("Bad proxywired ordering !", "TestA", this.iTestSingleProxywired.test());

		Assert.assertNotNull("Single bean was not proxywired !", this.iTestQualifiedSingleProxywired);
		Assert.assertEquals("Bad proxywired ordering !", "TestB", this.iTestQualifiedSingleProxywired.test());
	}

	/**
	 * Test ProxywiredManager.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test3() throws Exception {
		Assert.assertNotNull("Beans were not proxywired !", this.iTestProxywiredCollection);
		Assert.assertEquals("Bad proxywired bean count !", 3, this.iTestProxywiredCollection.size());

		final Set<String> availableDependencies = this.proxywiredManager.viewAllDependencies(ITest.class);
		Assert.assertNotNull("Available dependencies null !", availableDependencies);
		Assert.assertEquals("Bad count of available dependencies !", 3, availableDependencies.size());

		final Set<String> proxywiredDependencies = this.proxywiredManager
				.viewProxywiredDependencies(new ProxywiredField(this.getClass(), "iTestProxywiredCollection"));
		Assert.assertEquals("Bad default configuration of proxywired dependencies !", availableDependencies.size(),
				proxywiredDependencies.size());
	}

	/**
	 * Test Proxywired modification with ProxywiredManager.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test4() throws Exception {
		// Default Proxywiring (all beans)
		Assert.assertNotNull("Beans were not proxywired !", this.iTestProxywiredCollection);
		Assert.assertEquals("Bad proxywired bean count !", 3, this.iTestProxywiredCollection.size());

		// Modify Proxywiring
		final LinkedHashSet<String> beanNames = new LinkedHashSet<String>(Arrays.asList("testC", "testA"));
		this.proxywiredManager.modifyProxywiredDependencies(new ProxywiredField(this.getClass(),
				"iTestProxywiredCollection"), beanNames);

		// Test viewAllDependencies
		final Set<String> availableDependencies = this.proxywiredManager.viewAllDependencies(ITest.class);
		Assert.assertNotNull("Available dependencies null !", availableDependencies);
		Assert.assertEquals("Bad count of available dependencies !", 3, availableDependencies.size());

		// Test viewProxywiredDependencies
		final Set<String> proxywiredDependencies = this.proxywiredManager
				.viewProxywiredDependencies(new ProxywiredField(this.getClass(), "iTestProxywiredCollection"));
		Assert.assertNotNull("Proxywired dependencies null !", proxywiredDependencies);
		Assert.assertEquals("Bad default configuration of proxywired dependencies !", 2, proxywiredDependencies.size());
		final Iterator<String> iterator3 = proxywiredDependencies.iterator();
		Assert.assertEquals("Bad availableDependencies ordering !", "testC", iterator3.next());
		Assert.assertEquals("Bad availableDependencies ordering !", "testA", iterator3.next());

		// Test modified Proywiring
		Assert.assertNotNull("Beans were not proxywired !", this.iTestProxywiredCollection);
		Assert.assertEquals("Bad proxywired bean count !", 2, this.iTestProxywiredCollection.size());
		final Iterator<ITest> iterator4 = this.iTestProxywiredCollection.iterator();
		Assert.assertEquals("Bad proxywired ordering !", "TestC", iterator4.next().test());
		Assert.assertEquals("Bad proxywired ordering !", "TestA", iterator4.next().test());
	}

	/**
	 * Test Single Proxywired modification with ProxywiredManager.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test5() throws Exception {
		// Default Proxywiring (all beans)
		Assert.assertNotNull("Beans were not proxywired !", this.iTestSingleProxywired);
		Assert.assertEquals("Bad proxywired bean !", "TestA", this.iTestSingleProxywired.test());

		// Modify Proxywiring
		final LinkedHashSet<String> beanNames = new LinkedHashSet<String>(Arrays.asList("testC", "testA"));
		this.proxywiredManager.modifyProxywiredDependencies(new ProxywiredField(this.getClass(),
				"iTestSingleProxywired"), beanNames);

		// Test viewAllDependencies
		final Set<String> availableDependencies = this.proxywiredManager.viewAllDependencies(ITest.class);
		Assert.assertNotNull("Available dependencies null !", availableDependencies);
		Assert.assertEquals("Bad count of available dependencies !", 3, availableDependencies.size());

		// Test viewProxywiredDependencies
		final Set<String> proxywiredDependencies = this.proxywiredManager
				.viewProxywiredDependencies(new ProxywiredField(this.getClass(), "iTestSingleProxywired"));
		Assert.assertNotNull("Proxywired dependencies null !", proxywiredDependencies);
		Assert.assertEquals("Bad default configuration of proxywired dependencies !", 2, proxywiredDependencies.size());
		final Iterator<String> iterator3 = proxywiredDependencies.iterator();
		Assert.assertEquals("Bad availableDependencies ordering !", "testC", iterator3.next());
		Assert.assertEquals("Bad availableDependencies ordering !", "testA", iterator3.next());

		// Test modified Proywiring
		Assert.assertNotNull("Beans were not proxywired !", this.iTestSingleProxywired);
		Assert.assertEquals("Bad proxywired bean !", "TestC", this.iTestSingleProxywired.test());

	}

	/**
	 * Test Proxywired modification in mutithreaded environment.
	 * 
	 * @throws Exception
	 */
	@Test
	public void loadTest() throws Exception {
		@SuppressWarnings("unused")
		final LoadRunner<?, ?> loadRunner = new LoadRunner<ProxywiredAnotationBeanPostProcessorTest, Void>(this) {

			@Override
			protected Void loadTest(final ProxywiredAnotationBeanPostProcessorTest unitTest) throws Exception {
				unitTest.unitLoadTest();
				return null;
			}
		};

	}

	/**
	 * We will modify the proxywired been concurrently so it should always have 2 or 3 same beans wired.
	 * 
	 * @throws Exception
	 */
	protected void unitLoadTest() throws Exception {

		// Modify Proxywiring : 2 beans
		final LinkedHashSet<String> twoBeanNames = new LinkedHashSet<String>(Arrays.asList("testC", "testA"));
		this.proxywiredManager.modifyProxywiredDependencies(new ProxywiredField(this.getClass(),
				"iTestProxywiredCollection"), twoBeanNames);

		this.testSizeAndOrder(this.iTestProxywiredCollection);

		// Modify Proxywiring : 3 beans
		final LinkedHashSet<String> threeBeanNames = new LinkedHashSet<String>(Arrays.asList("testB", "testC", "testA"));
		this.proxywiredManager.modifyProxywiredDependencies(new ProxywiredField(this.getClass(),
				"iTestProxywiredCollection"), threeBeanNames);

		this.testSizeAndOrder(this.iTestProxywiredCollection);
	}

	protected void testSizeAndOrder(final Collection<ITest> proxywiredCol) {
		Assert.assertNotNull("Beans were not proxywired !", proxywiredCol);

		// Test size
		final int size = proxywiredCol.size();
		final boolean testSize = 3 == size || 2 == size;
		Assert.assertTrue("Bad proxywired bean count ! size: " + size, testSize);

		// Test ordering
		final Iterator<ITest> iterator = proxywiredCol.iterator();
		final String val1 = iterator.next().test();
		final String val2 = iterator.next().test();

		if ("TestC".equals(val1)) {
			Assert.assertEquals("Bad bean ordering : After TestC => TestA !", "TestA", val2);
		} else if ("TestB".equals(val1)) {
			Assert.assertEquals("Bad bean ordering : After TestB => TestC !", "TestC", val2);
		} else {
			Assert.fail("We should not found other bean than testC and testB !");
		}
	}

}
