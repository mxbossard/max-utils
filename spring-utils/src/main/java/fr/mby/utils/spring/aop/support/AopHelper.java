/**
 * 
 */
package fr.mby.utils.spring.aop.support;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.GenericTypeResolver;

/**
 * @author Maxime BOSSARD.
 *
 */
public abstract class AopHelper {
	
	/** Logger. */
	private static final Logger LOG = LogManager.getLogger(AopHelper.class);

	/**
	 * Test if an object which implement or extend a parameterized object will be able to handle a specific type.
	 * Example : NumberList implements List<Number> { ... }
	 * NumberList can store any subtypes of Number : Number, Integer, Long, ...
	 * This method return true if called like this supportsType(numberList, Integer.class, List.class)
	 * This method return false if called like this supportsType(numberList, String.class, List.class)
	 * 
	 * @param object the instantiated object we want to test against
	 * @param type the type we want to test if the object can hanldle
	 * @param genericIfc the type of the parametrized object (not the parameter type)
	 * @return true if the object implementing the genericIfc supports the specified type
	 */
	public static <T> boolean supportsType(final Object object, final Class<?> type, final Class<?> genericIfc) {
		Class<?> typeArg = GenericTypeResolver.resolveTypeArgument(object.getClass(), genericIfc);
		if (typeArg == null || typeArg.equals(genericIfc)) {
			final Class<?> targetClass = AopUtils.getTargetClass(object);
			if (targetClass != object.getClass()) {
				typeArg = GenericTypeResolver.resolveTypeArgument(targetClass, genericIfc);
			}
		}

		final boolean test = typeArg == null || typeArg.isAssignableFrom(type);

		final String logMsg;
		if (test) {
			logMsg = "[{}] supports type: [{}] for genericIfc [{}].";
		} else {
			logMsg = "[{}] doesn't support type: [{}] for genericIfc [{}].";
		}
		
		AopHelper.LOG.debug(logMsg, object.getClass().getSimpleName(), type.getSimpleName(), genericIfc.getSimpleName());

		return test;
	}
}
