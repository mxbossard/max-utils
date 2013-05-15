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

package fr.mby.spring.beans.factory.annotation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fr.mby.spring.beans.factory.IProxywiredManager;
import fr.mby.spring.beans.factory.ITest;

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
	}

	@Test
	@Ignore
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
	}

	@Test
	public void test3() throws Exception {
		Assert.assertNotNull("Beans were not proxywired !", this.iTestProxywiredCollection);
		Assert.assertEquals("Bad proxywired bean count !", 3, this.iTestProxywiredCollection.size());

		final Set<String> availableDependencies = this.proxywiredManager.viewAllDependencies(ITest.class);
		Assert.assertNotNull("Available dependencies null !", availableDependencies);
		Assert.assertEquals("Bad count of available dependencies !", 3, availableDependencies.size());

		final Set<String> proxywiredDependencies = this.proxywiredManager
				.viewProxywiredDependencies("fr.mby.spring.beans.factory.annotation.ProxywiredAnotationBeanPostProcessorTest.iTestProxywiredCollection");
		Assert.assertEquals("Bad default configuration of proxywired dependencies !", availableDependencies.size(),
				proxywiredDependencies.size());
	}

	@Test
	public void test4() throws Exception {
		// Default Proxywiring (all beans)
		Assert.assertNotNull("Beans were not proxywired !", this.iTestProxywiredCollection);
		Assert.assertEquals("Bad proxywired bean count !", 3, this.iTestProxywiredCollection.size());

		// Modify Proxywiring
		final LinkedHashSet<String> beanNames = new LinkedHashSet<String>(Arrays.asList("testC", "testA"));
		this.proxywiredManager
				.modifyProxywiredDepencies(
						"fr.mby.spring.beans.factory.annotation.ProxywiredAnotationBeanPostProcessorTest.iTestProxywiredCollection",
						beanNames);

		// Test viewAllDependencies
		final Set<String> availableDependencies = this.proxywiredManager.viewAllDependencies(ITest.class);
		Assert.assertNotNull("Available dependencies null !", availableDependencies);
		Assert.assertEquals("Bad count of available dependencies !", 3, availableDependencies.size());

		// Test viewProxywiredDependencies
		final Set<String> proxywiredDependencies = this.proxywiredManager
				.viewProxywiredDependencies("fr.mby.spring.beans.factory.annotation.ProxywiredAnotationBeanPostProcessorTest.iTestProxywiredCollection");
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

}
