/**
 * 
 */
package fr.mby.utils.common.reflect;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * AOP advisor which is design to add the implementation of an interface to a proxy.
 * 
 * @param <T> type of the interface to implement
 * @author Maxime Bossard - 2013
 *
 */
public class InterfaceImplementationAdvice implements MethodInterceptor {

	/** Implementation of the interface. */
	private final Object implementation;
	
	/** Type of the Interface this advisor will implement. */
	private final Class<?> interfaceType;
	
	/** Methods which this Interceptor will advise to implement the interface. */
	private final Method[] advisedMethods;

	public <T> InterfaceImplementationAdvice(final Class<T> interfaceType, final T implementation) {
		super();
		
		Assert.notNull(implementation, "No implementation provided !");
		
		this.implementation = implementation;
		this.interfaceType = interfaceType;
		this.advisedMethods = this.interfaceType.getDeclaredMethods();
		
		Assert.isTrue(this.interfaceType.isInterface(), "The paramerized type is not an interface !");
	}
	
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		final Object result;
		
		final Method method = invocation.getMethod();
		if (isAdvisedMethod(method)) {
			// Call advised implementation
			ReflectionUtils.makeAccessible(method);
			result = ReflectionUtils.invokeMethod(method, this.implementation, invocation.getArguments());
		} else {
			// Call on original object
			result = invocation.proceed();
		}

		return result;
	}

	/**
	 * Test if the method need to be advised by this interceptor.
	 * 
	 * @param invokedMethod
	 * @return true if it need to be advised
	 */
	protected boolean isAdvisedMethod(final Method invokedMethod) {
		for (Method advisedMethod : this.advisedMethods) {
			if (advisedMethod.equals(invokedMethod)) {
				return true;
			}
		}
		return false;
	}

}
