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

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.util.Assert;

import fr.mby.utils.spring.beans.factory.support.IProxywiredFactory;

/**
 * @author Maxime Bossard - 2013
 * 
 */
public class BasicProxywiredManager implements IProxywiredManager, InitializingBean, BeanFactoryAware {

	private ConfigurableListableBeanFactory beanFactory;

	private IProxywiredFactory proxywiredFactory;

	private final Map<String, IManageableProxywired> storage = new ConcurrentHashMap<String, IManageableProxywired>(16);

	@Override
	public Object getProxywiredDependency(final DependencyDescriptor descriptor, final String beanName,
			final Set<String> autowiredBeanNames, final Object target) {
		final IManageableProxywired result;

		// final Class<?> type = this.getBeanType(descriptor, autowiredBeanNames);
		final String proxywiredKey = this.getProxywiredKey(descriptor, beanName);

		final IManageableProxywired alreadyProxy = this.storage.get(proxywiredKey);

		if (alreadyProxy == null) {
			result = this.proxywiredFactory.proxy(descriptor, target);
			this.storage.put(proxywiredKey, result);
		} else {
			result = alreadyProxy;
		}

		return result;
	}

	@Override
	public void modifyProxywiredDepencies(final String proxywiredKey, final LinkedHashSet<String> beanNames) {
		if (proxywiredKey != null) {
			final IManageableProxywired alreadyProxy = this.storage.get(proxywiredKey);

			if (alreadyProxy != null) {
				final LinkedHashMap<String, Object> dependencies = new LinkedHashMap<String, Object>();
				if (beanNames != null) {
					for (final String beanName : beanNames) {
						final Object bean = this.beanFactory.getBean(beanName);
						if (bean != null) {
							dependencies.put(beanName, bean);
						}
					}
				}
				alreadyProxy.modifyProxywiredDependencies(dependencies);
			}
		}
	}

	@Override
	public Set<String> viewProxywiredDependencies(final String proxywiredKey) {
		Set<String> view = Collections.emptySet();

		if (proxywiredKey != null) {
			final IManageableProxywired alreadyProxy = this.storage.get(proxywiredKey);
			if (alreadyProxy != null) {
				view = alreadyProxy.viewProxywiredDependencies();
			}
		}

		return view;
	}

	@Override
	public Set<String> viewAllDependencies(final Class<?> type) {
		Set<String> view = Collections.emptySet();

		if (type != null) {
			final Map<String, ?> beans = this.beanFactory.getBeansOfType(type);
			if (beans != null) {
				view = beans.keySet();
			}
		}

		return view;
	}

	@Override
	public void setBeanFactory(final BeanFactory beanFactory) throws BeansException {
		if (!(beanFactory instanceof ConfigurableListableBeanFactory)) {
			throw new IllegalArgumentException(
					"AutowiredAnnotationBeanPostProcessor requires a ConfigurableListableBeanFactory");
		}
		this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.proxywiredFactory, "No IProxywiredFactory configured !");
	}

	protected String getProxywiredKey(final DependencyDescriptor descriptor, final String beanName) {
		return beanName + "." + descriptor.getDependencyName();
	}

	protected Class<?> getBeanType(final DependencyDescriptor descriptor, final Set<String> autowiredBeanNames) {
		final Class<?> result;

		final Field field = descriptor.getField();
		final Type fieldType = field.getGenericType();

		final Class<?> type = descriptor.getDependencyType();
		if (Collection.class.isAssignableFrom(type)) {
			final ParameterizedType parameterizedType = (ParameterizedType) fieldType;
			result = (Class<?>) parameterizedType.getActualTypeArguments()[0];
		} else if (Map.class.isAssignableFrom(type)) {
			final ParameterizedType parameterizedType = (ParameterizedType) fieldType;
			result = (Class<?>) parameterizedType.getActualTypeArguments()[1];
		} else if (type.isArray()) {
			// We can't do anything
			throw new IllegalStateException("You cannot use Proxywired annotation on an Array !");
		} else {
			result = type;
		}

		return result;
	}

	/**
	 * Getter of proxywiredFactory.
	 * 
	 * @return the proxywiredFactory
	 */
	public IProxywiredFactory getProxywiredFactory() {
		return this.proxywiredFactory;
	}

	/**
	 * Setter of proxywiredFactory.
	 * 
	 * @param proxywiredFactory
	 *            the proxywiredFactory to set
	 */
	public void setProxywiredFactory(final IProxywiredFactory proxywiredFactory) {
		this.proxywiredFactory = proxywiredFactory;
	}

}
