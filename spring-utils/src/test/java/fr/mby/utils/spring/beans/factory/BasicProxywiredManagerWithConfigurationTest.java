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

package fr.mby.utils.spring.beans.factory;

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.configuration.Configuration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fr.mby.utils.spring.beans.factory.annotation.Proxywired;
import fr.mby.utils.test.TestInterface;

/**
 * @author Maxime Bossard - 2013
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:basicProxywiredManagerContext.xml")
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class BasicProxywiredManagerWithConfigurationTest {

	@Autowired
	private IProxywiredManager manager;

	@Autowired
	private Configuration managerConfiguration;

	@Proxywired
	private Collection<TestInterface> iTestProxywired;

	@Proxywired
	private Collection<TestInterface> iTestProxywiredToChange;

	private Collection<TestInterface> injectByMethodParam;

	/**
	 * Test Proxywired annotation on a method.
	 * 
	 * @param shapes
	 * @throws Exception
	 */
	@Proxywired
	public void proxywiredSetter(final Collection<TestInterface> impls) throws Exception {
		this.injectByMethodParam = impls;
	}

	@Test
	public void testConfiguredManager() throws Exception {
		Assert.assertNotNull("TestInterface collection should not be null !", this.iTestProxywired);
		Assert.assertEquals("Wrong TestInterface collection size !", 1, this.iTestProxywired.size());

		Assert.assertEquals("Wrong TestInterface implementation selected !", "TestB", this.iTestProxywired.iterator()
				.next().test());
	}

	@Test
	public void testFieldChangeManagerConfiguration() throws Exception {
		// First change => testC,testA
		this.managerConfiguration.setProperty(
				"fr.mby.utils.spring.beans.factory.BasicProxywiredManagerWithConfigurationTest.__field__.iTestProxywiredToChange."
						+ IProxywiredManager.WIRED_BEANS_CONFIG_KEY, "testC,testA");

		Assert.assertNotNull("TestInterface collection should not be null !", this.iTestProxywiredToChange);
		Assert.assertEquals("Wrong TestInterface collection size !", 2, this.iTestProxywiredToChange.size());

		final Iterator<TestInterface> iterator = this.iTestProxywiredToChange.iterator();
		Assert.assertEquals("Wrong TestInterface implementation selected !", "TestC", iterator.next().test());
		Assert.assertEquals("Wrong TestInterface implementation selected !", "TestA", iterator.next().test());

		// Second change => testB,testA,testC
		this.managerConfiguration.setProperty(
				"fr.mby.utils.spring.beans.factory.BasicProxywiredManagerWithConfigurationTest.__field__.iTestProxywiredToChange."
						+ IProxywiredManager.WIRED_BEANS_CONFIG_KEY, "testB,testA,testC");

		Assert.assertNotNull("TestInterface collection should not be null !", this.iTestProxywiredToChange);
		Assert.assertEquals("Wrong TestInterface collection size !", 3, this.iTestProxywiredToChange.size());

		final Iterator<TestInterface> iterator2 = this.iTestProxywiredToChange.iterator();
		Assert.assertEquals("Wrong TestInterface implementation selected !", "TestB", iterator2.next().test());
		Assert.assertEquals("Wrong TestInterface implementation selected !", "TestA", iterator2.next().test());
		Assert.assertEquals("Wrong TestInterface implementation selected !", "TestC", iterator2.next().test());
	}

	@Test
	public void testMethodParamChangeManagerConfiguration() throws Exception {
		// First change => testA,testC
		this.managerConfiguration.setProperty(
				"fr.mby.utils.spring.beans.factory.BasicProxywiredManagerWithConfigurationTest.proxywiredSetter().impls."
						+ IProxywiredManager.WIRED_BEANS_CONFIG_KEY, "testA,testC");

		Assert.assertNotNull("TestInterface collection should not be null !", this.injectByMethodParam);
		Assert.assertEquals("Wrong TestInterface collection size !", 2, this.injectByMethodParam.size());

		final Iterator<TestInterface> iterator = this.injectByMethodParam.iterator();
		Assert.assertEquals("Wrong TestInterface implementation selected !", "TestA", iterator.next().test());
		Assert.assertEquals("Wrong TestInterface implementation selected !", "TestC", iterator.next().test());

		// Second change => testB
		this.managerConfiguration.setProperty(
				"fr.mby.utils.spring.beans.factory.BasicProxywiredManagerWithConfigurationTest.proxywiredSetter().impls."
						+ IProxywiredManager.WIRED_BEANS_CONFIG_KEY, "testB");

		Assert.assertNotNull("TestInterface collection should not be null !", this.injectByMethodParam);
		Assert.assertEquals("Wrong TestInterface collection size !", 1, this.injectByMethodParam.size());

		final Iterator<TestInterface> iterator2 = this.injectByMethodParam.iterator();
		Assert.assertEquals("Wrong TestInterface implementation selected !", "TestB", iterator2.next().test());
	}

}
