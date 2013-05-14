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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.util.Assert;

import fr.mby.spring.beans.factory.IProxywiredManager;

/**
 * @author Maxime BOSSARD.
 * 
 */
public class ProxywiredAnnotationBeanPostProcessor extends AutowiredAnnotationBeanPostProcessor
		implements
			BeanFactoryAware,
			InitializingBean {

	private static final Class<? extends Annotation> PROXY_ANNOTATION = Proxywired.class;

	private ConfigurableListableBeanFactory originalBeanFactory;

	private ConfigurableListableBeanFactory instrumentedBeanFactory;

	private IProxywiredManager proxywiredManager;

	/**
	 * Copy paste from AutowiredAnnotationBeanPostProcessor with Proxywired type added.
	 */
	@SuppressWarnings("unchecked")
	protected ProxywiredAnnotationBeanPostProcessor() {
		super();

		final Set<Class<? extends Annotation>> autowiredAnnotationTypes = new LinkedHashSet<Class<? extends Annotation>>();
		autowiredAnnotationTypes.add(Autowired.class);
		autowiredAnnotationTypes.add(Value.class);
		autowiredAnnotationTypes.add(ProxywiredAnnotationBeanPostProcessor.PROXY_ANNOTATION);
		final ClassLoader cl = AutowiredAnnotationBeanPostProcessor.class.getClassLoader();
		try {
			autowiredAnnotationTypes.add((Class<? extends Annotation>) cl.loadClass("javax.inject.Inject"));
			this.logger.info("JSR-330 'javax.inject.Inject' annotation found and supported for autowiring");
		} catch (final ClassNotFoundException ex) {
			// JSR-330 API not available - simply skip.
		}

		super.setAutowiredAnnotationTypes(autowiredAnnotationTypes);
	}

	@Override
	public void setBeanFactory(final BeanFactory beanFactory) throws BeansException {
		if (!(beanFactory instanceof ConfigurableListableBeanFactory)) {
			throw new IllegalArgumentException(
					"AutowiredAnnotationBeanPostProcessor requires a ConfigurableListableBeanFactory");
		}

		this.originalBeanFactory = (ConfigurableListableBeanFactory) beanFactory;

		final BeanFactory instrumentedBeanFactory = this
				.instrumentBeanFactory((ConfigurableListableBeanFactory) beanFactory);
		super.setBeanFactory(instrumentedBeanFactory);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.proxywiredManager, "No IProxywiredManager configured !");
	}

	/**
	 * Instrumentalize the bean factory to proxy what we need.
	 * 
	 * @param beanFactory
	 * @return
	 */
	protected ConfigurableListableBeanFactory instrumentBeanFactory(final ConfigurableListableBeanFactory beanFactory) {
		if (this.instrumentedBeanFactory == null) {
			final ProxyFactory proxyFactory = new ProxyFactory(ConfigurableListableBeanFactory.class,
					new ResolveDependencyMethodInterceptor());
			proxyFactory.setTarget(beanFactory);

			this.instrumentedBeanFactory = (ConfigurableListableBeanFactory) proxyFactory.getProxy();
		}

		return this.instrumentedBeanFactory;
	}

	/**
	 * Getter of proxywiredManager.
	 * 
	 * @return the proxywiredManager
	 */
	public IProxywiredManager getProxywiredManager() {
		return this.proxywiredManager;
	}

	/**
	 * Setter of proxywiredManager.
	 * 
	 * @param proxywiredManager
	 *            the proxywiredManager to set
	 */
	public void setProxywiredManager(final IProxywiredManager proxywiredManager) {
		this.proxywiredManager = proxywiredManager;
	}

	private class ResolveDependencyMethodInterceptor implements MethodInterceptor {

		private static final String INTERCEPTED_METHOD_NAME = "resolveDependency";

		@Override
		public Object invoke(final MethodInvocation invocation) throws Throwable {
			Object result = null;

			try {
				result = invocation.proceed();
			} catch (final NoUniqueBeanDefinitionException e) {
				result = this.resolveNonUniqueDependency(invocation);
			}

			// Init and test the method invokation
			if (this.isCorrectMethod(invocation) && this.isProxywiredAnnotationUsed(invocation)) {
				result = this.doProxy(invocation, result);
			}

			return result;
		}

		/**
		 * Proxy the targeted dependency.
		 * 
		 * @param result
		 * @return
		 */
		@SuppressWarnings("unchecked")
		protected Object doProxy(final MethodInvocation invocation, final Object target) {
			final Object[] args = invocation.getArguments();
			final DependencyDescriptor descriptor = (DependencyDescriptor) args[0];
			final String beanName = (String) args[1];
			final Set<String> autowiredBeanNames = (Set<String>) args[2];

			return ProxywiredAnnotationBeanPostProcessor.this.getProxywiredManager().getProxywiredDependency(
					descriptor, beanName, autowiredBeanNames, target);
		}

		/**
		 * @param invocation
		 * @return
		 */
		protected Object resolveNonUniqueDependency(final MethodInvocation invocation) {
			final Object[] args = invocation.getArguments();
			final DependencyDescriptor descriptor = (DependencyDescriptor) args[0];
			final Class<?> type = descriptor.getDependencyType();

			final Map<String, ?> result = ProxywiredAnnotationBeanPostProcessor.this.originalBeanFactory
					.getBeansOfType(type);
			return result;
		}

		/**
		 * Test if the wiring was called by a Proxywired annotation.
		 * 
		 * @param invocation
		 * @return
		 */
		protected boolean isProxywiredAnnotationUsed(final MethodInvocation invocation) {
			final DependencyDescriptor descriptor = (DependencyDescriptor) invocation.getArguments()[0];

			final Annotation[] annotations = descriptor.getAnnotations();
			if (annotations != null) {
				for (final Annotation annotation : annotations) {
					if (ProxywiredAnnotationBeanPostProcessor.PROXY_ANNOTATION.isAssignableFrom(annotation.getClass())) {
						return true;
					}
				}
			}

			return false;
		}

		/**
		 * Test if the invoked method is the one we want to advice.
		 * 
		 * @param invocation
		 * @return
		 */
		protected boolean isCorrectMethod(final MethodInvocation invocation) {
			final Method method = invocation.getMethod();

			final boolean testName = ResolveDependencyMethodInterceptor.INTERCEPTED_METHOD_NAME
					.equals(method.getName());

			return testName && this.testArgs(invocation);
		}

		/**
		 * Test if the invoked method was passed with the argument types we want.
		 * 
		 * @param invocation
		 * @return
		 */
		protected boolean testArgs(final MethodInvocation invocation) {
			final Object[] args = invocation.getArguments();
			return args != null && args[0] != null && DependencyDescriptor.class.isAssignableFrom(args[0].getClass())
					&& args[1] != null && String.class.isAssignableFrom(args[1].getClass()) && args[2] != null
					&& Set.class.isAssignableFrom(args[2].getClass()) && args[3] != null
					&& TypeConverter.class.isAssignableFrom(args[3].getClass());
		}

	}

}
