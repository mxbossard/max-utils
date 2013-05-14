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
import java.util.SortedSet;

import org.springframework.beans.factory.config.DependencyDescriptor;

/**
 * @author Maxime BOSSARD.
 * 
 */
public interface IProxywiredManager {

	Object getProxywiredDependency(DependencyDescriptor descriptor, String beanName, Set<String> autowiredBeanNames,
			Object target);

	void modifyProxywiredDepencies(Class<?> type, SortedSet<String> beanNames);

	SortedSet<String> getProxywiredDependencies(Class<?> type);

	SortedSet<String> getAllDependencies(Class<?> type);

	public interface IProxywiredManageable {

		void modifyProxywiredDepencies(Map<String, Object> dependencies);

	}
}