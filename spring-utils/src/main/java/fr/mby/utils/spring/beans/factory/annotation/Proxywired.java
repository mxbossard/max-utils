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

package fr.mby.utils.spring.beans.factory.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used in conjunction with ProxywiredAnnotationBeanPostProcessor, the Proxywired will do the same job as Autowired. But
 * it will replace the injected bean by a proxy wich will allow us to dynamically modify the wired bean.
 * 
 * <p>
 * In case of a {@link java.util.Collection} or {@link java.util.Map} dependency type the collection will be replace by
 * a proxy collection. We will of course be able to modify the collection at runtime.
 * </p>
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
	 * <p>
	 * Defaults to <code>true</code>.
	 */
	boolean required() default true;

}
