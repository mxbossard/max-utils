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

package fr.mby.utils.common.jpa;

import javax.persistence.EntityManagerFactory;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * @author Maxime Bossard - 2013
 * 
 */
public abstract class OsgiJpaHelper {

	/**
	 * Retrieve the EntityManagerFactory by its persistence unit name.
	 * 
	 * @param bundleContext
	 * @param unitName
	 * @return the EntityManagerFactory
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static EntityManagerFactory retrieveEmfByName(final BundleContext bundleContext,
			final String unitName) {
		ServiceReference[] refs = null;
		try {
			refs = bundleContext.getServiceReferences(EntityManagerFactory.class.getName(), "(osgi.unit.name="
					+ unitName + ")");
		} catch (final InvalidSyntaxException isEx) {
			throw new RuntimeException("Filter error", isEx);
		}
		return (refs == null) ? null : (EntityManagerFactory) bundleContext.getService(refs[0]);
	}

}
