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

package fr.mby.spring.beans.factory;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.util.Assert;

/**
 * @author Maxime Bossard - 2013
 * 
 */
public class DynamicalyConfigurableProxyListableBeanFactory
		implements
			DynamicalyConfigurableListableBeanFactory,
			InitializingBean {

	private ListableBeanFactory backingBeanFactory;

	private Collection<Class<?>> typesToProxy;

	private Map<Class<?>, BeanList<?>> configuration;

	@Override
	public Object getBean(final String name) throws BeansException {
		return this.backingBeanFactory.getBean(name);
	}

	@Override
	public <T> T getBean(final String name, final Class<T> requiredType) throws BeansException {
		return this.backingBeanFactory.getBean(name, requiredType);
	}

	@Override
	public <T> T getBean(final Class<T> requiredType) throws BeansException {
		return this.backingBeanFactory.getBean(requiredType);
	}

	@Override
	public Object getBean(final String name, final Object... args) throws BeansException {
		return this.backingBeanFactory.getBean(name, args);
	}

	@Override
	public boolean containsBean(final String name) {
		return this.backingBeanFactory.containsBean(name);
	}

	@Override
	public boolean isSingleton(final String name) throws NoSuchBeanDefinitionException {
		return this.backingBeanFactory.isSingleton(name);
	}

	@Override
	public boolean isPrototype(final String name) throws NoSuchBeanDefinitionException {
		return this.backingBeanFactory.isPrototype(name);
	}

	@Override
	public boolean isTypeMatch(final String name, final Class<?> targetType) throws NoSuchBeanDefinitionException {
		return this.backingBeanFactory.isTypeMatch(name, targetType);
	}

	@Override
	public Class<?> getType(final String name) throws NoSuchBeanDefinitionException {
		return this.backingBeanFactory.getType(name);
	}

	@Override
	public String[] getAliases(final String name) {
		return this.backingBeanFactory.getAliases(name);
	}

	@Override
	public boolean containsBeanDefinition(final String beanName) {
		return this.backingBeanFactory.containsBeanDefinition(beanName);
	}

	@Override
	public int getBeanDefinitionCount() {
		return this.backingBeanFactory.getBeanDefinitionCount();
	}

	@Override
	public String[] getBeanDefinitionNames() {
		return this.backingBeanFactory.getBeanDefinitionNames();
	}

	@Override
	public String[] getBeanNamesForType(final Class<?> type) {
		final String[] allBeanNames = this.backingBeanFactory.getBeanNamesForType(type);
		return this.filterBeanNames(type, allBeanNames);
	}

	@Override
	public String[] getBeanNamesForType(final Class<?> type, final boolean includeNonSingletons,
			final boolean allowEagerInit) {
		final String[] allBeanNames = this.backingBeanFactory.getBeanNamesForType(type, includeNonSingletons,
				allowEagerInit);
		return this.filterBeanNames(type, allBeanNames);
	}

	@Override
	public <T> Map<String, T> getBeansOfType(final Class<T> type) throws BeansException {
		final Map<String, T> allBeans = this.backingBeanFactory.getBeansOfType(type);

		final Map<String, T> filteredBeans = this.filterBeans(type, allBeans);

		return filteredBeans;
	}

	@Override
	public <T> Map<String, T> getBeansOfType(final Class<T> type, final boolean includeNonSingletons,
			final boolean allowEagerInit) throws BeansException {
		final Map<String, T> allBeans = this.backingBeanFactory.getBeansOfType(type, includeNonSingletons,
				allowEagerInit);

		final Map<String, T> filteredBeans = this.filterBeans(type, allBeans);

		return filteredBeans;
	}

	@Override
	public Map<String, Object> getBeansWithAnnotation(final Class<? extends Annotation> annotationType)
			throws BeansException {
		return this.backingBeanFactory.getBeansWithAnnotation(annotationType);
	}

	@Override
	public <A extends Annotation> A findAnnotationOnBean(final String beanName, final Class<A> annotationType) {
		return this.backingBeanFactory.findAnnotationOnBean(beanName, annotationType);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.backingBeanFactory, "No backing ListableBeanFactory configured !");
	}

	@Override
	public String[] getAllBeanNamesOfType(final Class<?> type) throws BeansException {
		return this.backingBeanFactory.getBeanNamesForType(type);
	}

	@Override
	public <T> Map<String, T> getAllBeansOfType(final Class<T> type) throws BeansException {
		return this.backingBeanFactory.getBeansOfType(type);
	}

	@Override
	public <T> void addTypeToProxy(final Class<T> type) {
		if (!this.configuration.containsKey(type)) {
			this.configuration.put(type, new BeanList<T>());
		}
	}

	@Override
	public <T> void removeProxiedType(final Class<T> type) {
		this.configuration.remove(type);
	}

	@Override
	public void configureBeanList(final Class<?> type, final List<String> beanNames) {
		Assert.notNull(type, "No type provided !");
		Assert.notNull(beanNames, "No bean names list provided !");

		final BeanList<?> beanList = this.configuration.get(type);
		if (beanList != null) {
			beanList.refresh(beanNames);
		}
	}

	@Override
	public List<String> getFilteredBeanNames(final Class<?> type) {
		final List<String> beanNamesList;

		final BeanList<?> beanList = this.configuration.get(type);
		if (beanList != null) {
			beanNamesList = beanList.getBeanNames();
		} else {
			beanNamesList = Collections.emptyList();
		}

		return beanNamesList;
	}

	/**
	 * Getter of backingBeanFactory.
	 * 
	 * @return the backingBeanFactory
	 */
	public ListableBeanFactory getBackingBeanFactory() {
		return this.backingBeanFactory;
	}

	/**
	 * Setter of backingBeanFactory.
	 * 
	 * @param backingBeanFactory
	 *            the backingBeanFactory to set
	 */
	@Override
	public void setBackingBeanFactory(final ListableBeanFactory backingBeanFactory) {
		this.backingBeanFactory = backingBeanFactory;
	}

	@SuppressWarnings("unchecked")
	protected <T> BeanList<T> getFilteredBeans(final Class<T> type) {
		final BeanList<T> beanList = (BeanList<T>) this.configuration.get(type);

		return beanList;
	}

	/**
	 * Filter the bean names by applying the config.
	 * 
	 * @param type
	 * @param allBeanNames
	 * @return
	 */
	protected String[] filterBeanNames(final Class<?> type, final String[] allBeanNames) {
		final List<String> filteredBeanNames = Arrays.asList(allBeanNames);

		if (this.isToProxyBean(type)) {
			this.initProxyConfigForType(type);

			final List<String> configuredBeanNames = this.getFilteredBeanNames(type);
			filteredBeanNames.retainAll(configuredBeanNames);
		}

		return (String[]) filteredBeanNames.toArray();
	}

	/**
	 * Filter the beans by applying the config.
	 * 
	 * @param type
	 * @param allBeans
	 * @return
	 */
	protected <T> Map<String, T> filterBeans(final Class<T> type, final Map<String, T> allBeans) {
		final Map<String, T> filteredBeansMap = new HashMap<String, T>(allBeans);

		if (this.isToProxyBean(type)) {
			this.initProxyConfigForType(type);

			filteredBeansMap.clear();
			final BeanList<T> filteredBeanList = this.getFilteredBeans(type);
			if (filteredBeanList != null) {
				for (final Bean<T> filteredBean : filteredBeanList.getBeans()) {
					filteredBeansMap.put(filteredBean.beanName, filteredBean.bean);
				}
			}
		}

		return filteredBeansMap;
	}

	/**
	 * @param type
	 */
	protected void initProxyConfigForType(final Class<?> type) {
		if (type != null && !this.configuration.containsKey(type) && this.typesToProxy.contains(type)) {
			// If this type must be proxied but was not initialized

			final Map<String, ?> allBeansMap = this.getAllBeansOfType(type);
			if (allBeansMap != null) {
				this.configureBeanList(type, Arrays.asList(this.getAllBeanNamesOfType(type)));
			}
		}
	}

	/**
	 * Test if the bean must be proxy.
	 * 
	 * @param beanName
	 * @return
	 */
	protected boolean isToProxyBean(final String beanName) {
		final Object bean = this.getBean(beanName);

		if (bean != null && this.typesToProxy != null) {
			for (final Class<?> typeToProxy : this.typesToProxy) {
				if (typeToProxy != null && typeToProxy.isAssignableFrom(bean.getClass())) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Test if the bean must be proxy.
	 * 
	 * @param beanName
	 * @return
	 */
	protected boolean isToProxyBean(final Class<?> type) {
		if (type != null && this.typesToProxy != null) {
			for (final Class<?> typeToProxy : this.typesToProxy) {
				if (typeToProxy != null && typeToProxy.isAssignableFrom(type)) {
					return true;
				}
			}
		}

		return false;
	}

	private class BeanList<T> {

		private List<Bean<T>> backingList = new ArrayList<Bean<T>>(8);

		@SuppressWarnings("unchecked")
		public void refresh(final List<String> beanNames) {
			final List<Bean<T>> newBackingList;

			if (beanNames != null) {
				newBackingList = new ArrayList<Bean<T>>(beanNames.size());
				for (final String beanName : beanNames) {
					final Object bean = DynamicalyConfigurableProxyListableBeanFactory.this.getBean(beanName);
					newBackingList.add(new Bean<T>(beanName, (T) bean));
				}
			} else {
				newBackingList = Collections.emptyList();
			}

			// Swap the backing list
			this.backingList = newBackingList;
		}

		public List<String> getBeanNames() {
			final ArrayList<String> newList = new ArrayList<String>(this.backingList.size());

			for (final Bean<T> bean : this.backingList) {
				newList.add(bean.beanName);
			}

			return newList;
		}

		public List<Bean<T>> getBeans() {
			return new ArrayList<Bean<T>>(this.backingList);
		}

	}

	private class Bean<T> {

		private final String beanName;

		private final T bean;

		/**
		 * @param beanName
		 * @param bean
		 */
		public Bean(final String beanName, final T bean) {
			super();
			this.beanName = beanName;
			this.bean = bean;
		}

	}

}
