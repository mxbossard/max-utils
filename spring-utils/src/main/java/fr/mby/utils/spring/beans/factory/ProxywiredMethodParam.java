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

import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;

import fr.mby.utils.spring.beans.factory.IProxywiredManager.IProxywiredIdentifier;

/**
 * @author Maxime Bossard - 2013
 * 
 */
public class ProxywiredMethodParam implements IProxywiredIdentifier {

	private final String key;

	protected ProxywiredMethodParam(final DependencyDescriptor descriptor, final String wiredClassName) {
		super();

		Assert.notNull(descriptor, "No DependencyDescriptor provided !");
		final MethodParameter methodParam = descriptor.getMethodParameter();
		Assert.notNull(methodParam, "DependencyDescriptor provided don't describe a Method parameter !");
		final String methodName = methodParam.getMethod().getName();
		final String paramName = methodParam.getParameterName();

		Assert.hasText(wiredClassName, "Wired class name cannot be found !");

		this.key = wiredClassName + "/" + methodName + "/" + paramName;
	}

	public ProxywiredMethodParam(final Class<?> wiredClass, final String methodName, final String paramName) {
		super();

		Assert.notNull(wiredClass, "No Wired class provided !");
		Assert.hasText(methodName, "No Method name provided !");
		Assert.hasText(paramName, "No Parameter name provided !");

		this.key = wiredClass.getName() + "/" + methodName + "/" + paramName;
	}

	@Override
	public String getKey() {
		return this.key;
	}

}
