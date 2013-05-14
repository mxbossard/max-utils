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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;

import fr.mby.spring.beans.factory.IProxywiredManager.IProxywiredManageable;

/**
 * @author Maxime BOSSARD.
 * 
 */
public class BasicProxywiredFactory implements IProxywiredFactory {

	@Override
	@SuppressWarnings("unchecked")
	public IProxywiredManageable proxy(final DependencyDescriptor descriptor, final Object target) {
		final IProxywiredManageable result;

		final Class<?> type = descriptor.getDependencyType();

		if (Collection.class.isAssignableFrom(type) && type.isInterface()) {
			result = this.proxyDependencyCollection((Collection<Object>) target);
		} else if (Map.class.isAssignableFrom(type) && type.isInterface()) {
			result = this.proxyDependencyMap((Map<String, Object>) target);
		} else if (type.isArray()) {
			// We can't do anything
			throw new IllegalStateException("You cannot use Proxywired annotation on an Array !");
		} else {
			result = this.proxySingleDependency(type, target);
		}

		return result;
	}

	/**
	 * Proxy assured by a ProxyFactory with a ProxywiredBean as TargetSource.
	 * 
	 * @param target
	 * @return
	 */
	protected IProxywiredManageable proxySingleDependency(final Class<?> type, final Object target) {
		final SortedSet<Object> backingSet = new TreeSet<Object>();
		backingSet.add(target);
		final TargetSource targetSource = new ProxywiredBean(type, backingSet);
		final Class<?>[] proxtyInterfaces = new Class[]{type, IProxywiredManageable.class};
		final ProxyFactory proxyFactory = new ProxyFactory(proxtyInterfaces);
		proxyFactory.setTargetSource(targetSource);

		return (IProxywiredManageable) proxyFactory.getProxy();
	}

	/**
	 * Proxy assured by a ProxywiredSet.
	 * 
	 * @param target
	 * @return
	 */
	protected IProxywiredManageable proxyDependencyCollection(final Collection<Object> target) {
		return new ProxywiredSet2(Collection.class, target);
	}

	/**
	 * Proxy assured by a ProxywiredMap.
	 * 
	 * @param target
	 * @return
	 */
	protected IProxywiredManageable proxyDependencyMap(final Map<String, Object> target) {
		return new ProxywiredMap2(Map.class, target);
	}

	private class ProxywiredBean extends ProxwyiredStruct<Object, Set<Object>> {

		protected ProxywiredBean(final Class<?> type, final Object target) {
			super(type, target);
		}

		@Override
		protected Set<Object> initStruct(final Object target) {
			final Set<Object> newSet = new HashSet<Object>(1);
			newSet.add(target);
			return newSet;
		}

		@Override
		protected Set<Object> modifyProxywiredDepenciesInternal(final Map<String, Object> dependencies) {
			final Set<Object> newSet = new HashSet<Object>(dependencies.values());
			return newSet;
		}

		@Override
		protected Object getTargetInternal() {
			// Get the first element of the set
			return this.backingStore.iterator().next();
		}

	}

	private class ProxywiredSet extends CopyOnWriteArraySet<Object> implements IProxywiredManageable {

		/** Svuid. */
		private static final long serialVersionUID = 5227474294627660939L;

		protected ProxywiredSet(final Collection<? extends Object> arg0) {
			super(arg0);
		}

		@Override
		public void modifyProxywiredDepencies(final Map<String, Object> dependencies) {
			this.clear();
			this.addAll(dependencies.values());
		}

	}

	private class ProxywiredSet2 extends ProxwyiredStruct<Collection<Object>, Set<Object>> {

		protected ProxywiredSet2(final Class<?> type, final Collection<Object> target) {
			super(type, target);
		}

		@Override
		protected Set<Object> initStruct(final Collection<Object> target) {
			return Collections.unmodifiableSet(new HashSet<Object>(target));
		}

		@Override
		protected Set<Object> modifyProxywiredDepenciesInternal(final Map<String, Object> dependencies) {
			return Collections.unmodifiableSet(new HashSet<Object>(dependencies.values()));
		}

		@Override
		protected Set<Object> getTargetInternal() {
			return this.backingStore;
		}

	}

	private class ProxywiredMap extends ConcurrentHashMap<String, Object> implements IProxywiredManageable {

		/** Svuid. */
		private static final long serialVersionUID = 9138691669418541733L;

		protected ProxywiredMap(final Map<? extends String, ? extends Object> arg0) {
			super(16, 0.75f, 1);
			this.putAll(arg0);
		}

		@Override
		public void modifyProxywiredDepencies(final Map<String, Object> dependencies) {
			this.clear();
			this.putAll(dependencies);
		}
	}

	private class ProxywiredMap2 extends ProxwyiredStruct<Map<String, Object>, Map<String, Object>> {

		protected ProxywiredMap2(final Class<?> type, final Map<String, Object> target) {
			super(type, target);
		}

		@Override
		protected Map<String, Object> initStruct(final Map<String, Object> target) {
			return Collections.unmodifiableMap(new HashMap<String, Object>(target));
		}

		@Override
		protected Map<String, Object> modifyProxywiredDepenciesInternal(final Map<String, Object> dependencies) {
			return Collections.unmodifiableMap(new HashMap<String, Object>(dependencies));
		}

		@Override
		protected Map<String, Object> getTargetInternal() {
			return this.backingStore;
		}

	}

	private abstract class ProxwyiredStruct<T, S> implements TargetSource, IProxywiredManageable {

		private final Class<?> type;

		protected S backingStore;

		private boolean lock = false;

		protected ProxwyiredStruct(final Class<?> type, final T target) {
			super();

			this.type = type;
			this.backingStore = this.initStruct(target);
		}

		/**
		 * Build the backing Map on initialization.
		 * 
		 * @param target
		 */
		protected abstract S initStruct(T target);

		/**
		 * Build the backing Map with refreshed data.
		 * 
		 * @param target
		 */
		protected abstract S modifyProxywiredDepenciesInternal(Map<String, Object> dependencies);

		/**
		 * Build the Target to return.
		 * 
		 * @return
		 */
		protected abstract T getTargetInternal();

		@Override
		public Class<?> getTargetClass() {
			return this.type;
		}

		@Override
		public boolean isStatic() {
			return false;
		}

		@Override
		public Object getTarget() throws Exception {
			while (this.lock) {
				this.wait(10);
			}

			return this.getTargetInternal();
		}

		@Override
		public void releaseTarget(final Object target) throws Exception {
			// Nothing to do on release
		}

		@Override
		public void modifyProxywiredDepencies(final Map<String, Object> dependencies) {
			this.lock = true;
			this.backingStore = this.modifyProxywiredDepenciesInternal(dependencies);
			this.lock = false;
			this.notifyAll();
		}

	}
}
