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

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.commons.configuration.event.EventSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import fr.mby.utils.spring.beans.factory.support.IProxywiredFactory;

/**
 * @author Maxime Bossard - 2013
 * 
 */
public class BasicProxywiredManager
		implements
			IProxywiredManager,
			InitializingBean,
			DisposableBean,
			BeanFactoryAware,
			ConfigurationListener {

	/** Logger. */
	private static final Logger LOG = LogManager.getLogger(BasicProxywiredManager.class);

	private ConfigurableListableBeanFactory beanFactory;

	private IProxywiredFactory proxywiredFactory;

	private Configuration configuration;

	private final Map<IProxywiredIdentifier, IManageableProxywired> byIdStorage = new ConcurrentHashMap<IProxywiredIdentifier, IManageableProxywired>();

	private final Map<Class<?>, Collection<IManageableProxywired>> byTypeStorage = new ConcurrentHashMap<Class<?>, Collection<IManageableProxywired>>();

	@Override
	public Object getProxywiredDependency(final DependencyDescriptor descriptor, final String beanName,
			final Set<String> autowiredBeanNames, final Object target) {
		final IManageableProxywired result;

		final IProxywiredIdentifier identifier = this.buildIdentifier(descriptor, beanName);

		// Try to find proxywired element in storage
		final IManageableProxywired alreadyProxy = this.byIdStorage.get(identifier);

		if (alreadyProxy == null) {
			result = this.initializeConfiguredDependency(descriptor, target, identifier, autowiredBeanNames);
		} else {
			result = alreadyProxy;
		}

		return result;
	}

	@Override
	public void modifyProxywiredDependencies(final IProxywiredIdentifier identifier,
			final LinkedHashSet<String> beanNames) {
		Assert.notNull(identifier, "No IProxywiredIdentifier provided !");

		final IManageableProxywired alreadyProxy = this.byIdStorage.get(identifier);

		if (alreadyProxy != null) {
			// Build dependency to wire Map
			final LinkedHashMap<String, Object> dependencies = this.buildDependencyToWireMap(beanNames);

			this.modifyDependency(dependencies, alreadyProxy);
			BasicProxywiredManager.LOG.info(
					"Porxywired dependency with identifier: [{}] was modified with beans: [{}]", identifier, beanNames);
		} else {
			throw new IllegalStateException("Unable to found a Prowywired resource for this IProxywiredIdentifier !");
		}
	}

	@Override
	public void modifyAllProxywiredDepencies(final Class<?> proxywiredType, final LinkedHashSet<String> beanNames) {
		Assert.notNull(proxywiredType, "No Proxywired type provided !");

		final Collection<IManageableProxywired> sameTypeDependencies = this.byTypeStorage.get(proxywiredType);

		if (!CollectionUtils.isEmpty(sameTypeDependencies)) {
			final LinkedHashMap<String, Object> dependencies = this.buildDependencyToWireMap(beanNames);

			// Modify all dependencies of this type
			for (final IManageableProxywired dependencyToModify : sameTypeDependencies) {
				this.modifyDependency(dependencies, dependencyToModify);
				BasicProxywiredManager.LOG.info(
						"All Proxywired dependency of type: [{}] were modified with beans: [{}]",
						proxywiredType.getName(), beanNames);
			}

		} else {
			throw new IllegalStateException("Unable to found a Prowywired resource for this Type !");
		}
	}

	@Override
	public Set<String> viewProxywiredDependencies(final IProxywiredIdentifier identifier) {
		Assert.notNull(identifier, "No IProxywiredIdentifier provided !");

		Set<String> view = Collections.emptySet();

		final IManageableProxywired alreadyProxy = this.byIdStorage.get(identifier);
		if (alreadyProxy != null) {
			view = alreadyProxy.viewProxywiredDependencies();
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
	public void configurationChanged(final ConfigurationEvent event) {
		final String propertyName = event.getPropertyName();
		final Object propertyValue = event.getPropertyValue();

		if (propertyName != null && propertyName.endsWith("." + IProxywiredManager.WIRED_BEANS_CONFIG_KEY)) {
			// A Wired beans property changed
			if (String.class.isAssignableFrom(propertyValue.getClass())) {
				// Build bean names set
				final LinkedHashSet<String> beanNames = this.buildWiredBeanSetFromConfig((String) propertyValue);

				// Build proxywired resource identifier
				final int suffixPos = propertyName.lastIndexOf("." + IProxywiredManager.WIRED_BEANS_CONFIG_KEY);
				final String proxywiredNodePath = propertyName.substring(0, suffixPos);
				final IProxywiredIdentifier identifier = this.buildIdentifier(proxywiredNodePath);

				final IManageableProxywired dependencyToModify = this.byIdStorage.get(identifier);
				if (dependencyToModify != null) {
					final LinkedHashMap<String, Object> dependenciesToWire = this.buildDependencyToWireMap(beanNames);
					dependencyToModify.modifyProxywiredDependencies(dependenciesToWire);
					BasicProxywiredManager.LOG.info(
							"ConfigurationChangedEvent leaded to update Porxywired dependency: [{}] with beans: [{}]",
							identifier, beanNames);
				} else {
					BasicProxywiredManager.LOG.warn(
							"Unable to find a Proxywired dependency corresponding to indentifier: [{}] ", identifier);
				}

			} else {
				throw new IllegalStateException("Wired beans config value should be a list of string !");
			}
		} else {
			throw new IllegalStateException("ConfigurationChangedEvent with key: [" + propertyName + "] => value: ["
					+ propertyValue + "] cannot be processed !");
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.proxywiredFactory, "No IProxywiredFactory configured !");

		// Init configuration
		if (this.configuration != null) {
			if (EventSource.class.isAssignableFrom(this.configuration.getClass())) {
				final EventSource eventSource = (EventSource) this.configuration;
				eventSource.addConfigurationListener(this);
			}
		} else {

		}
	}

	@Override
	public void destroy() throws Exception {
		this.beanFactory = null;
		this.byIdStorage.clear();
		this.byTypeStorage.clear();
		this.configuration = null;
		this.proxywiredFactory = null;
	}

	/**
	 * Initialize a proxywired dependency. Build it, register it storages, and configure it with prefs.
	 * 
	 * @param descriptor
	 * @param target
	 * @param identifier
	 * @param autowiredBeanNames
	 * @return
	 */
	protected IManageableProxywired initializeConfiguredDependency(final DependencyDescriptor descriptor,
			final Object target, final IProxywiredIdentifier identifier, final Set<String> autowiredBeanNames) {
		final IManageableProxywired result;

		// Build Proxywired dependency
		result = this.proxywiredFactory.proxy(descriptor, identifier, target);

		// Store it in "By Id storage"
		this.byIdStorage.put(identifier, result);

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
		final LinkedHashSet<String> beanNames = this.readWiringConfiguration(identifier);

		// Initialize with prefs
		if (!CollectionUtils.isEmpty(beanNames)) {
			this.modifyProxywiredDependencies(identifier, beanNames);
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
		this.updateWiringConfiguration(dependencies, dependencyToModify);

		// Modify dependency
		dependencyToModify.modifyProxywiredDependencies(dependencies);
	}

	/**
	 * Read the wiring configuration.
	 * 
	 * @param identifier
	 * @return
	 */
	protected LinkedHashSet<String> readWiringConfiguration(final IProxywiredIdentifier identifier) {
		LinkedHashSet<String> beanNames = null;
		if (this.configuration != null) {
			final Configuration elementConfig = identifier.getConfigurationSubset(this.configuration);
			final String wiredBeansConfig = elementConfig.getString(IProxywiredManager.WIRED_BEANS_CONFIG_KEY, null);
			beanNames = this.buildWiredBeanSetFromConfig(wiredBeansConfig);
		}

		return beanNames;
	}

	/**
	 * Update the wiring configuration.
	 * 
	 * @param dependencies
	 * @param dependencyToModify
	 */
	protected void updateWiringConfiguration(final LinkedHashMap<String, Object> dependencies,
			final IManageableProxywired dependencyToModify) {
		if (this.configuration != null) {
			final IProxywiredIdentifier identifier = dependencyToModify.getIdentifier();
			// Internal method => Identifier cannot be null here !
			Assert.notNull(identifier, "Cannot found valid identifier for this dependency !");

			final String beanNames = StringUtils.collectionToDelimitedString(dependencies.keySet(),
					IProxywiredManager.WIRING_PREFS_SEPARATOR);

			final Configuration elementConfig = identifier.getConfigurationSubset(this.configuration);
			elementConfig.addProperty(IProxywiredManager.WIRED_BEANS_CONFIG_KEY, beanNames);
		}
	}

	/**
	 * Build a Set of bean names based on the wired beans config value.
	 * 
	 * @param wiredBeansConfig
	 *            wired beans config value
	 * @return the coresponding LinkedHashSet
	 */
	protected LinkedHashSet<String> buildWiredBeanSetFromConfig(final String wiredBeansConfig) {
		LinkedHashSet<String> beanNames = null;

		if (wiredBeansConfig != null) {
			final String[] splittedValue = wiredBeansConfig.split(IProxywiredManager.WIRING_PREFS_SEPARATOR);
			final List<String> wiringPref = Arrays.asList(splittedValue);
			beanNames = new LinkedHashSet<String>(wiringPref);
		}
		return beanNames;
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
		Assert.notNull(descriptor, "No DependencyDescriptor provided !");
		Assert.hasText(wiredClassName, "No wiredClassName provided !");

		IProxywiredIdentifier identifier = null;

		if (descriptor.getMethodParameter() != null) {
			identifier = new ProxywiredMethodParam(descriptor, wiredClassName);
		} else if (descriptor.getField() != null) {
			identifier = new ProxywiredField(descriptor, wiredClassName);
		} else {
			throw new IllegalStateException("Unkown Proxywiring method !");
		}

		return identifier;
	}

	/**
	 * Build the identifier for a Proxywired dependency.
	 * 
	 * @param descriptor
	 *            the dependency descriptor
	 * @param wiredClassName
	 *            the class name of the in wich contain the annotation
	 * @return the identifier cannot be null
	 */
	protected IProxywiredIdentifier buildIdentifier(final String nodePath) {
		Assert.hasText(nodePath, "No nodePath provided !");

		final IProxywiredIdentifier identifier;

		if (nodePath.contains(ProxywiredMethodParam.METHOD_CONF_KEY)) {
			identifier = new ProxywiredMethodParam(nodePath);
		} else if (nodePath.contains(ProxywiredField.FIELD_CONF_KEY)) {
			identifier = new ProxywiredField(nodePath);
		} else {
			throw new IllegalStateException("Unkown to build IProxywiredIdentifier for the node: [" + nodePath + "]");
		}

		return identifier;
	}

	/**
	 * Build the LinkedHashMap of beans to wire.
	 * 
	 * @param beanNames
	 *            the names of beans to wire
	 * @return
	 */
	protected LinkedHashMap<String, Object> buildDependencyToWireMap(final LinkedHashSet<String> beanNames) {
		final LinkedHashMap<String, Object> dependencies = new LinkedHashMap<String, Object>();
		if (beanNames != null) {
			for (final String beanName : beanNames) {
				final Object bean = this.beanFactory.getBean(beanName);
				if (bean != null) {
					dependencies.put(beanName, bean);
				} else {
					BasicProxywiredManager.LOG.warn("Cannot found bean with name: [{}]", beanName);
				}
			}
		}
		return dependencies;
	}

	/**
	 * Find Bean Type to inject (not the generic type like Collection or Set).
	 * 
	 * @param descriptor
	 * @param autowiredBeanNames
	 * @return
	 */
	protected Class<?> getBeanType(final DependencyDescriptor descriptor, final Set<String> autowiredBeanNames) {
		final Class<?> result;

		Type fieldType = null;

		final Field field = descriptor.getField();
		if (field == null) {
			// Annotation on the method
			final MethodParameter methodParameter = descriptor.getMethodParameter();
			if (methodParameter != null) {
				fieldType = methodParameter.getGenericParameterType();
			}
		} else {
			fieldType = field.getGenericType();
		}

		if (fieldType != null) {
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
		} else {
			throw new IllegalStateException("Unable to find the Bean type !");
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
	 * Getter of configuration.
	 * 
	 * @return the configuration
	 */
	public Configuration getConfiguration() {
		return this.configuration;
	}

	/**
	 * Setter of configuration.
	 * 
	 * @param configuration
	 *            the configuration to set
	 */
	public void setConfiguration(final Configuration configuration) {
		this.configuration = configuration;
	}

}
