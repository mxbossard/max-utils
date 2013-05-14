/**
 * 
 */
package fr.mby.spring.beans.factory.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;

import fr.mby.spring.beans.factory.IProxywiredManager;

/**
 * @author Maxime BOSSARD.
 *
 */
public class ProxywiredAnnotationBeanPostProcessor extends AutowiredAnnotationBeanPostProcessor implements BeanFactoryAware {

	private static final Class<? extends Annotation> PROXY_ANNOTATION = Proxywired.class;

	private ConfigurableListableBeanFactory instrumentedBeanFactory;
	
	/**
	 * Copy paste from AutowiredAnnotationBeanPostProcessor with Proxywired type added.
	 */
	@SuppressWarnings("unchecked")
	protected ProxywiredAnnotationBeanPostProcessor() {
		super();
		
		final Set<Class<? extends Annotation>> autowiredAnnotationTypes =
				new LinkedHashSet<Class<? extends Annotation>>();
		autowiredAnnotationTypes.add(Autowired.class);
		autowiredAnnotationTypes.add(Value.class);
		autowiredAnnotationTypes.add(PROXY_ANNOTATION);
		ClassLoader cl = AutowiredAnnotationBeanPostProcessor.class.getClassLoader();
		try {
			autowiredAnnotationTypes.add((Class<? extends Annotation>) cl.loadClass("javax.inject.Inject"));
			logger.info("JSR-330 'javax.inject.Inject' annotation found and supported for autowiring");
		}
		catch (ClassNotFoundException ex) {
			// JSR-330 API not available - simply skip.
		}
		
		super.setAutowiredAnnotationTypes(autowiredAnnotationTypes);
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		if (!(beanFactory instanceof ConfigurableListableBeanFactory)) {
			throw new IllegalArgumentException(
					"AutowiredAnnotationBeanPostProcessor requires a ConfigurableListableBeanFactory");
		}

		final BeanFactory instrumentedBeanFactory = this.instrumentBeanFactory((ConfigurableListableBeanFactory) beanFactory);
		super.setBeanFactory(instrumentedBeanFactory);
	}

	/**
	 * Instrumentalize the bean factory to proxy what we need.
	 * 
	 * @param beanFactory
	 * @return
	 */
	protected ConfigurableListableBeanFactory instrumentBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		if (this.instrumentedBeanFactory == null) {
			ProxyFactory proxyFactory = new ProxyFactory(ConfigurableListableBeanFactory.class, 
					new ResolveDependencyMethodInterceptor());
			proxyFactory.setTarget(beanFactory);

			this.instrumentedBeanFactory = (ConfigurableListableBeanFactory) proxyFactory.getProxy();
		}
		
		return this.instrumentedBeanFactory;
	}

	private class ResolveDependencyMethodInterceptor implements MethodInterceptor {

		private static final String INTERCEPTED_METHOD_NAME = "resolveDependency";

		private IProxywiredManager manager;

		public Object invoke(MethodInvocation invocation) throws Throwable {
			Object result = invocation.proceed();
			
			// Init and test the method invokation
			if (this.isCorrectMethod(invocation) && isProxywiredAnnotationUsed(invocation)) {
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
		protected Object doProxy(MethodInvocation invocation, Object target) {
			Object[] args = invocation.getArguments();
			DependencyDescriptor descriptor = (DependencyDescriptor) args[0];
			String beanName = (String) args[1];
			Set<String> autowiredBeanNames = (Set<String>) args[2];
			
			return this.manager.getProxywiredDependency(descriptor, beanName, autowiredBeanNames, target);
		}

		/**
		 * Test if the wiring was called by a Proxywired annotation.
		 * 
		 * @param invocation
		 * @return
		 */
		protected boolean isProxywiredAnnotationUsed(MethodInvocation invocation) {
			DependencyDescriptor descriptor = (DependencyDescriptor) invocation.getArguments()[0];
			
			Annotation[] annotations = descriptor.getAnnotations();
			if (annotations != null) {
				for (Annotation annotation : annotations) {
					if (PROXY_ANNOTATION.equals(annotation.getClass())) {
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
		protected boolean isCorrectMethod(MethodInvocation invocation) {
			Method method = invocation.getMethod();
			
			boolean testName = INTERCEPTED_METHOD_NAME.equals(method.getName());
			
			return testName && testArgs(invocation);
		}
		
		/**
		 * Test if the invoked method was passed with the argument types we want.
		 * 
		 * @param invocation
		 * @return
		 */
		protected boolean testArgs(MethodInvocation invocation) {
			Object[] args = invocation.getArguments();
			return args != null && args[0] != null 
					&& DependencyDescriptor.class.isAssignableFrom(args[0].getClass())
					&& args[1] != null && String.class.isAssignableFrom(args[1].getClass())
					&& args[2] != null && Set.class.isAssignableFrom(args[2].getClass())
					&& args[3] != null && TypeConverter.class.isAssignableFrom(args[3].getClass());
		}

	}
	
}
