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

import java.lang.reflect.Field;
import java.util.prefs.Preferences;

import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.util.Assert;

import fr.mby.utils.spring.beans.factory.IProxywiredManager.IProxywiredIdentifier;

/**
 * @author Maxime Bossard - 2013
 * 
 */
public class ProxywiredField implements IProxywiredIdentifier {

	private final String nodePathName;

	protected ProxywiredField(final DependencyDescriptor descriptor, final String wiredClassName) {
		super();

		Assert.notNull(descriptor, "No DependencyDescriptor provided !");
		final Field field = descriptor.getField();
		Assert.notNull(field, "DependencyDescriptor provided don't describe a Field !");
		final String fieldName = field.getName();

		Assert.hasText(wiredClassName, "Wired class name cannot be found !");

		this.nodePathName = wiredClassName.replaceAll("\\.", "/") + "/__field__/" + fieldName;
	}

	public ProxywiredField(final Class<?> wiredClass, final String fieldName) {
		super();

		Assert.notNull(wiredClass, "No Wired class provided !");
		Assert.hasText(fieldName, "No Field name provided !");

		this.nodePathName = wiredClass.getName().replaceAll("\\.", "/") + "/__field__/" + fieldName;
	}

	@Override
	public Preferences getPreferencesNode(final Preferences proxywiredPreferences) {
		return proxywiredPreferences.node(this.nodePathName);
	}

	@Override
	public String toString() {
		return "ProxywiredField [nodePathName=" + this.nodePathName + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.nodePathName == null) ? 0 : this.nodePathName.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		final ProxywiredField other = (ProxywiredField) obj;
		if (this.nodePathName == null) {
			if (other.nodePathName != null) {
				return false;
			}
		} else if (!this.nodePathName.equals(other.nodePathName)) {
			return false;
		}
		return true;
	}

}
