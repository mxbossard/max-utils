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

import java.util.List;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ListableBeanFactory;

/**
 * ListableBeanFactory which can be dynamicaly configured. The configuration allow for a bean type to select the beans
 * wich will be returns by the ListableBeanFactory.
 * 
 * @author Maxime Bossard - 2013
 * 
 */
public interface DynamicalyConfigurableListableBeanFactory extends ListableBeanFactory {

	/**
	 * Get all bean names of a type.
	 * 
	 * @param type
	 * @return
	 * @throws BeansException
	 */
	String[] getAllBeanNamesOfType(final Class<?> type) throws BeansException;

	/**
	 * Get all beans of a type (without filtering).
	 * 
	 * @param type
	 * @return
	 * @throws BeansException
	 */
	<T> Map<String, T> getAllBeansOfType(final Class<T> type) throws BeansException;

	/**
	 * Add a type to the list of proxied types.
	 * 
	 * @param type
	 * @return
	 */
	<T> void addTypeToProxy(final Class<T> type);

	/**
	 * Add a type to the list of proxied types.
	 * 
	 * @param type
	 * @return
	 */
	public <T> void removeProxiedType(final Class<T> type);

	/**
	 * Retrieve the list of filtered bean names (the names of the beans which will are currently in use).
	 * 
	 * @param type
	 * @return
	 */
	List<String> getFilteredBeanNames(final Class<?> type);

	/**
	 * Configure a list of bean names accessible for a type.
	 * 
	 * @param type
	 * @param beanNames
	 */
	void configureBeanList(final Class<?> type, final List<String> beanNames);

	/**
	 * Setter of backingBeanFactory.
	 * 
	 * @param backingBeanFactory
	 *            the backingBeanFactory to set
	 */
	void setBackingBeanFactory(ListableBeanFactory backingBeanFactory);

}
