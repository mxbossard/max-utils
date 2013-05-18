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

import org.apache.commons.configuration.Configuration;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;

import fr.mby.utils.spring.beans.factory.IProxywiredManager.IProxywiredIdentifier;

/**
 * @author Maxime Bossard - 2013
 * 
 */
public class ProxywiredMethodParam implements IProxywiredIdentifier {

	/** Configuration key in node path indicating that the node path describe a method parameter. */
	public static String METHOD_CONF_KEY = "()";

	/** Path identifying a Proxywired resource. */
	private String nodePath;

	protected ProxywiredMethodParam(final DependencyDescriptor descriptor, final String wiredClassName) {
		super();

		Assert.notNull(descriptor, "No DependencyDescriptor provided !");
		final MethodParameter methodParam = descriptor.getMethodParameter();
		Assert.notNull(methodParam, "DependencyDescriptor provided don't describe a Method parameter !");
		final String methodName = methodParam.getMethod().getName();
		final String paramName = methodParam.getParameterName();

		Assert.hasText(wiredClassName, "Wired class name cannot be found !");

		this.buildNodePath(wiredClassName, methodName, paramName);
	}

	public ProxywiredMethodParam(final Class<?> wiredClass, final String methodName, final String paramName) {
		super();

		Assert.notNull(wiredClass, "No Wired class provided !");
		Assert.hasText(methodName, "No Method name provided !");
		Assert.hasText(paramName, "No Parameter name provided !");

		this.buildNodePath(wiredClass.getName(), methodName, paramName);
	}

	public ProxywiredMethodParam(final String nodePath) {
		super();

		Assert.hasText(nodePath, "No nodePath provided !");
		this.nodePath = nodePath;
	}

	/**
	 * @param wiredClassName
	 * @param fieldName
	 */
	protected void buildNodePath(final String wiredClassName, final String methodName, final String fieldName) {
		this.nodePath = wiredClassName + "." + methodName + ProxywiredMethodParam.METHOD_CONF_KEY + "." + fieldName;
	}

	@Override
	public Configuration getConfigurationSubset(final Configuration allConfiguration) {
		return allConfiguration.subset(this.nodePath);
	}

	@Override
	public String toString() {
		return "ProxywiredMethodParam [nodePath=" + this.nodePath + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.nodePath == null) ? 0 : this.nodePath.hashCode());
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
		final ProxywiredMethodParam other = (ProxywiredMethodParam) obj;
		if (this.nodePath == null) {
			if (other.nodePath != null) {
				return false;
			}
		} else if (!this.nodePath.equals(other.nodePath)) {
			return false;
		}
		return true;
	}

}
