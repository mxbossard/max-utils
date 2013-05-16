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

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.beans.factory.config.DependencyDescriptor;

/**
 * Manager for @Proxywired dependencies.
 * 
 * @author Maxime BOSSARD.
 * 
 */
public interface IProxywiredManager {

	/**
	 * Retrieve a Proxywired dependency for injection.
	 * 
	 * @param descriptor
	 * @param beanName
	 * @param autowiredBeanNames
	 * @param target
	 * @return
	 */
	Object getProxywiredDependency(DependencyDescriptor descriptor, String beanName, Set<String> autowiredBeanNames,
			Object target);

	/**
	 * Modify a Proxywired dependency.
	 * 
	 * @param proxywiredKey
	 * @param beanNames
	 */
	void modifyProxywiredDepencies(String proxywiredKey, LinkedHashSet<String> beanNames);

	/**
	 * Ordered view of all bean names currently in one Proxywired dependency.
	 * 
	 * @param proxywiredKey
	 * @return
	 */
	Set<String> viewProxywiredDependencies(String proxywiredKey);

	/**
	 * View of all bean names of a specific type currently laying in the BeanFactory.
	 * 
	 * @param type
	 * @return
	 */
	Set<String> viewAllDependencies(Class<?> type);

	/**
	 * Represent a Manageable Proxywired dependency.
	 * 
	 * @author Maxime Bossard - 2013.
	 * 
	 */
	public interface IProxywiredManageable {

		/**
		 * Modify the elements inside the Proxywired dependency.
		 * 
		 * @param dependencies
		 */
		void modifyProxywiredDependencies(LinkedHashMap<String, Object> dependencies);

		/**
		 * Oredered view currently wired in de Proxywired dependency.
		 * 
		 * @return
		 */
		Set<String> viewProxywiredDependencies();

	}

}
