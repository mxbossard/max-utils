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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import fr.mby.utils.spring.beans.factory.support.IProxywiredFactory;

/**
 * @author Maxime Bossard - 2013
 * 
 */
public class BasicProxywiredManager implements IProxywiredManager, InitializingBean, BeanFactoryAware {

	/** Logger. */
	private static final Logger LOG = LogManager.getLogger(BasicProxywiredManager.class);

	private static final String PROXYWIRED_PREFS_PATH = "ProxywiredPreferences";

	private static final String WIRING_PREFS_SEPARATOR = ",";

	private ConfigurableListableBeanFactory beanFactory;

	private IProxywiredFactory proxywiredFactory;

	private IProxywiredPreferencesFactory proxywiredPreferencesFactory;

	private Preferences proxywiredPrefs;

	private final Map<String, IManageableProxywired> byIdStorage = new ConcurrentHashMap<String, IManageableProxywired>();

	private final Map<IManageableProxywired, IProxywiredIdentifier> idStorage = new ConcurrentHashMap<IManageableProxywired, IProxywiredIdentifier>();

	private final Map<Class<?>, Collection<IManageableProxywired>> byTypeStorage = new ConcurrentHashMap<Class<?>, Collection<IManageableProxywired>>();

	@Override
	public Object getProxywiredDependency(final DependencyDescriptor descriptor, final String beanName,
			final Set<String> autowiredBeanNames, final Object target) {
		final IManageableProxywired result;

		// final Class<?> type = this.getBeanType(descriptor, autowiredBeanNames);
		final IProxywiredIdentifier id = this.buildIdentifier(descriptor, beanName);

		// Try to find proxywired element in storage
		final IManageableProxywired alreadyProxy = this.byIdStorage.get(id.getKey());

		if (alreadyProxy == null) {
			result = this.initializeProxywiredDependency(descriptor, target, id, autowiredBeanNames);
		} else {
			result = alreadyProxy;
		}

		return result;
	}

	@Override
	public void modifyProxywiredDependencies(final IProxywiredIdentifier id, final LinkedHashSet<String> beanNames) {
		Assert.notNull(id, "No IProxywiredIdentifier provided !");

		final String proxywiredKey = id.getKey();
		Assert.hasText(proxywiredKey, "IProxywiredIdentifier cannot build a valid key !");

		final IManageableProxywired alreadyProxy = this.byIdStorage.get(proxywiredKey);

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
			this.modifyDependency(dependencies, alreadyProxy);
		} else {
			throw new IllegalStateException("Unable to found a Prowywired resource for this IProxywiredIdentifier !");
		}
	}

	@Override
	public void modifyAllProxywiredDepencies(final Class<?> proxywiredType, final LinkedHashSet<String> beanNames) {
		Assert.notNull(proxywiredType, "No Proxywired type provided !");

		final Collection<IManageableProxywired> sameTypeDependencies = this.byTypeStorage.get(proxywiredType);

		if (!CollectionUtils.isEmpty(sameTypeDependencies)) {
			// Build dependency to wire Map
			final LinkedHashMap<String, Object> dependencies = new LinkedHashMap<String, Object>();
			if (beanNames != null) {
				for (final String beanName : beanNames) {
					final Object bean = this.beanFactory.getBean(beanName);
					if (bean != null) {
						dependencies.put(beanName, bean);
					}
				}
			}

			// Modify all dependencies of this type
			for (final IManageableProxywired dependencyToModify : sameTypeDependencies) {
				this.modifyDependency(dependencies, dependencyToModify);
			}

		} else {
			throw new IllegalStateException("Unable to found a Prowywired resource for this Type !");
		}
	}

	@Override
	public Set<String> viewProxywiredDependencies(final IProxywiredIdentifier id) {
		Set<String> view = Collections.emptySet();
		final String proxywiredKey = id.getKey();

		if (proxywiredKey != null) {
			final IManageableProxywired alreadyProxy = this.byIdStorage.get(proxywiredKey);
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

		final Preferences prefs;
		if (this.proxywiredPreferencesFactory != null) {
			prefs = this.proxywiredPreferencesFactory.buildPreferences();
		} else {
			// Default preferences
			prefs = Preferences.systemRoot();
		}

		this.proxywiredPrefs = prefs.node(BasicProxywiredManager.PROXYWIRED_PREFS_PATH);
	}

	/**
	 * Initialize a proxywired dependency. Build it, register it storages, and configure it with prefs.
	 * 
	 * @param descriptor
	 * @param target
	 * @param id
	 * @param autowiredBeanNames
	 * @return
	 */
	protected IManageableProxywired initializeProxywiredDependency(final DependencyDescriptor descriptor,
			final Object target, final IProxywiredIdentifier id, final Set<String> autowiredBeanNames) {
		final IManageableProxywired result;

		// Build Proxywired dependency
		result = this.proxywiredFactory.proxy(descriptor, target);

		// Store it in "By Id storage"
		this.byIdStorage.put(id.getKey(), result);

		// Store it in "Id storage"
		this.idStorage.put(result, id);

		// Store it in "By Type storage"
		final Class<?> dependencyType = this.getBeanType(descriptor, autowiredBeanNames);
		Collection<IManageableProxywired> storedByTypes = this.byTypeStorage.get(dependencyType);
		if (storedByTypes == null) {
			// Initialize collection
			storedByTypes = new ArrayList<IManageableProxywired>();
			this.byTypeStorage.put(dependencyType, storedByTypes);
		}
		storedByTypes.add(result);

		// Try to load wiring preferences
		final String prefValue = this.proxywiredPrefs.get(id.getKey(), null);
		if (prefValue != null) {
			final String[] splittedValue = StringUtils.split(prefValue, BasicProxywiredManager.WIRING_PREFS_SEPARATOR);
			final List<String> wiringPref = Arrays.asList(splittedValue);
			final LinkedHashSet<String> beanNames = new LinkedHashSet<String>(wiringPref);

			// Initialize with prefs
			this.modifyProxywiredDependencies(id, beanNames);
		}
		return result;
	}

	/**
	 * Modifiy a dependency.
	 * 
	 * @param dependencies
	 * @param dependencyToModify
	 * @throws BackingStoreException
	 */
	protected void modifyDependency(final LinkedHashMap<String, Object> dependencies,
			final IManageableProxywired dependencyToModify) {
		// Update preferences
		final IProxywiredIdentifier id = this.idStorage.get(dependencyToModify);
		Assert.notNull(id, "Cannot found valid identifier for this dependency !");
		final String value = StringUtils.collectionToDelimitedString(dependencies.keySet(),
				BasicProxywiredManager.WIRING_PREFS_SEPARATOR);
		this.proxywiredPrefs.put(id.getKey(), value);
		try {
			this.proxywiredPrefs.flush();
		} catch (final BackingStoreException e) {
			BasicProxywiredManager.LOG.error("Preferences were not flushed !", e);
		}

		// Modify dependency
		dependencyToModify.modifyProxywiredDependencies(dependencies);
	}

	/**
	 * Build the identifier for a Proxywired dependency.
	 * 
	 * @param descriptor
	 *            the dependency descriptor
	 * @param wiredClassName
	 *            the class name of the in wich contain the annotation
	 * @return the identifier
	 */
	protected IProxywiredIdentifier buildIdentifier(final DependencyDescriptor descriptor, final String wiredClassName) {
		final IProxywiredIdentifier identifier;

		if (descriptor.getField() != null) {
			identifier = new ProxywiredField(descriptor, wiredClassName);
		} else if (descriptor.getMethodParameter() != null) {
			identifier = new ProxywiredMethodParam(descriptor, wiredClassName);
		} else {
			throw new IllegalStateException("Unkown Proxywiring method !");
		}

		return identifier;
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

	/**
	 * Getter of proxywiredPreferencesFactory.
	 * 
	 * @return the proxywiredPreferencesFactory
	 */
	public IProxywiredPreferencesFactory getProxywiredPreferencesFactory() {
		return this.proxywiredPreferencesFactory;
	}

	/**
	 * Setter of proxywiredPreferencesFactory.
	 * 
	 * @param proxywiredPreferencesFactory
	 *            the proxywiredPreferencesFactory to set
	 */
	public void setProxywiredPreferencesFactory(final IProxywiredPreferencesFactory proxywiredPreferencesFactory) {
		this.proxywiredPreferencesFactory = proxywiredPreferencesFactory;
	}

}
