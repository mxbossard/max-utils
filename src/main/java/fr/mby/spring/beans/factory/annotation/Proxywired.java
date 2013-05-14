/**
 * 
 */
package fr.mby.spring.beans.factory.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used in conjunction with ProxywiredAnnotationBeanPostProcessor,
 * the Proxywired will do the same job as Autowired.
 * But it will replace the injected bean by a proxy wich will allow us to
 * dynamically modify the wired bean.
 * 
 * <p>In case of a {@link java.util.Collection} or {@link java.util.Map}
 * dependency type the collection will be replace by a proxy collection. 
 * We will of course be able to modify the collection at runtime. </p>
 * 
 * @author Maxime BOSSARD.
 *
 */
@Target({ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Proxywired {
	/**
	 * Declares whether the annotated dependency is required.
	 * <p>Defaults to <code>true</code>.
	 */
	boolean required() default true;
	
}
