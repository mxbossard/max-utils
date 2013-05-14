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

package fr.mby.spring.beans.factory;

import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.config.DependencyDescriptor;

/**
 * @author Maxime BOSSARD.
 * 
 */
public interface IProxywiredManager {

	Object getProxywiredDependency(DependencyDescriptor descriptor, String beanName, Set<String> autowiredBeanNames,
			Object target);

	void modifyProxywiredDepencies(String proxywiredKey, Set<String> beanNames);

	Set<String> viewProxywiredDependencies(String proxywiredKey);

	Set<String> viewAllDependencies(Class<?> type);

	public interface IProxywiredManageable {

		void modifyProxywiredDependencies(Map<String, Object> dependencies);

		Set<String> viewProxywiredDependencies();

	}
}
