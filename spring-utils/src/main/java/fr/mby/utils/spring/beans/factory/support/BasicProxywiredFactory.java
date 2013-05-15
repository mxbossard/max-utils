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

package fr.mby.utils.spring.beans.factory.support;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;

import fr.mby.utils.spring.beans.factory.IProxywiredManager.IProxywiredManageable;

/**
 * @author Maxime BOSSARD - 2013.
 * 
 */
public class BasicProxywiredFactory implements IProxywiredFactory {

	@Override
	@SuppressWarnings("unchecked")
	public IProxywiredManageable proxy(final DependencyDescriptor descriptor, final Object target) {
		final IProxywiredManageable result;

		final Class<?> dependencyType = descriptor.getDependencyType();

		if (Map.class.isAssignableFrom(dependencyType) && dependencyType.isInterface()) {
			result = this.proxyDependencyMap((Map<String, Object>) target);
		} else if (List.class.isAssignableFrom(dependencyType) && dependencyType.isInterface()) {
			result = this.proxyDependencyList((List<Object>) target);
		} else if (Collection.class.isAssignableFrom(dependencyType) && dependencyType.isInterface()) {
			result = this.proxyDependencyCollection((Collection<Object>) target, dependencyType);
		} else if (dependencyType.isArray()) {
			// We can't do anything
			throw new IllegalStateException("You cannot use Proxywired annotation on an Array !");
		} else if (dependencyType.isInterface()) {
			result = this.proxySingleDependency(target, dependencyType);
		} else {
			throw new IllegalStateException("Dependency type not supported by this factory !");
		}

		return result;
	}

	/**
	 * Proxy assured by a ProxyFactory with a ProxywiredBean as TargetSource.
	 * 
	 * @param target
	 * @return
	 */
	protected IProxywiredManageable proxySingleDependency(final Object target, final Class<?> type) {
		final TargetSource targetSource = new ProxywiredBean(type, target);

		return this.proxywire(targetSource);
	}

	/**
	 * Proxy assured by a ProxywiredSet.
	 * 
	 * @param target
	 * @return
	 */
	protected IProxywiredManageable proxyDependencyList(final List<Object> target) {
		final TargetSource targetSource = new ProxywiredList(List.class, target);

		return this.proxywire(targetSource);
	}

	/**
	 * Proxy assured by a ProxywiredSet.
	 * 
	 * @param target
	 * @return
	 */
	protected IProxywiredManageable proxyDependencyCollection(final Collection<Object> target,
			final Class<?> dependencyType) {
		final TargetSource targetSource = new ProxywiredCollection(dependencyType, target);

		return this.proxywire(targetSource);
	}

	/**
	 * Proxy assured by a ProxywiredMap.
	 * 
	 * @param target
	 * @return
	 */
	protected IProxywiredManageable proxyDependencyMap(final Map<String, Object> target) {
		final TargetSource targetSource = new ProxywiredMap(Map.class, target);

		return this.proxywire(targetSource);
	}

	protected IProxywiredManageable proxywire(final TargetSource targetSource) {
		final Class<?>[] proxtyInterfaces = new Class[]{targetSource.getTargetClass(), IProxywiredManageable.class};
		final ProxyFactory proxyFactory = new ProxyFactory(proxtyInterfaces);
		proxyFactory.addAdvice(new ProxywiredManageableInterceptor((IProxywiredManageable) targetSource));
		proxyFactory.setTargetSource(targetSource);

		return (IProxywiredManageable) proxyFactory.getProxy();
	}

	private class ProxywiredBean extends ProxywiredStruct<Object> {

		protected ProxywiredBean(final Class<?> type, final Object target) {
			super(type, target);
		}

		@Override
		@SuppressWarnings("unchecked")
		protected Map<String, Object> initStruct(final Object target) {
			final Map<String, Object> newMap = new LinkedHashMap<String, Object>(1);

			if (Collection.class.isAssignableFrom(target.getClass())) {
				// If the target is a collection
				for (final Object obj : (Collection<Object>) target) {
					newMap.put(obj.toString(), obj);
				}
			} else if (Map.class.isAssignableFrom(target.getClass())) {
				// If the target is a collection
				for (final Object obj : ((Map<?, Object>) target).values()) {
					newMap.put(obj.toString(), obj);
				}
			} else {
				newMap.put(target.toString(), target);
			}

			return newMap;
		}

		@Override
		protected Object buildCachedProxy(final Map<String, Object> backingStore) {
			// Get the first element of the set
			final Object singleDependency = backingStore.values().iterator().next();
			return singleDependency;
		}

		@Override
		protected HashMap<String, Object> modifyBackingStore(final LinkedHashMap<String, Object> dependencies) {
			return new LinkedHashMap<String, Object>(dependencies);
		}

	}

	private class ProxywiredList extends ProxywiredStruct<List<Object>> {

		protected ProxywiredList(final Class<?> type, final List<Object> target) {
			super(type, target);
		}

		@Override
		protected Map<String, Object> initStruct(final List<Object> target) {
			final Map<String, Object> newMap = new LinkedHashMap<String, Object>(target.size());
			for (final Object obj : target) {
				newMap.put(obj.toString(), obj);
			}
			return newMap;
		}

		@Override
		protected List<Object> buildCachedProxy(final Map<String, Object> backingStore) {
			return Collections.unmodifiableList(new ArrayList<Object>(backingStore.values()));
		}

	}

	private class ProxywiredCollection extends ProxywiredStruct<Collection<Object>> {

		protected ProxywiredCollection(final Class<?> type, final Collection<Object> target) {
			super(type, target);
		}

		@Override
		protected Map<String, Object> initStruct(final Collection<Object> target) {
			final Map<String, Object> newMap = new LinkedHashMap<String, Object>(target.size());
			for (final Object obj : target) {
				newMap.put(obj.toString(), obj);
			}
			return newMap;
		}

		@Override
		protected Collection<Object> buildCachedProxy(final Map<String, Object> backingStore) {
			final Collection<Object> result;

			if (Set.class.isAssignableFrom(this.getTargetClass())) {
				result = Collections.unmodifiableSet(new LinkedHashSet<Object>(backingStore.values()));
			} else if (Collection.class.isAssignableFrom(this.getTargetClass())) {
				result = Collections.unmodifiableCollection(backingStore.values());
			} else {
				throw new IllegalStateException("Only the Set is supported as specialization for generic collection !");
			}

			return result;
		}

	}

	private class ProxywiredMap extends ProxywiredStruct<Map<String, Object>> {

		protected ProxywiredMap(final Class<?> type, final Map<String, Object> target) {
			super(type, target);
		}

		@Override
		protected Map<String, Object> initStruct(final Map<String, Object> target) {
			return new LinkedHashMap<String, Object>(target);
		}

		@Override
		protected Map<String, Object> buildCachedProxy(final Map<String, Object> backingStore) {
			return Collections.unmodifiableMap(backingStore);
		}

	}

	private abstract class ProxywiredStruct<T> implements TargetSource, IProxywiredManageable {

		private final Class<?> type;

		private Map<String, Object> backingStore;

		private Object cachedProxy;

		private boolean lock = false;

		protected ProxywiredStruct(final Class<?> type, final T target) {
			super();

			this.type = type;
			this.backingStore = this.initStruct(target);
			this.cachedProxy = this.buildCachedProxy(this.backingStore);
		}

		/**
		 * Build the backing Map on initialization.
		 * 
		 * @param target
		 */
		protected abstract Map<String, Object> initStruct(T target);

		/**
		 * Build the Target to return.
		 * 
		 * @return
		 */
		protected abstract T buildCachedProxy(Map<String, Object> backingStore);

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
			synchronized (this) {
				while (this.lock) {
					this.wait(10);
				}

				return this.cachedProxy;
			}
		}

		@Override
		public void releaseTarget(final Object target) throws Exception {
			// Nothing to do on release
		}

		@Override
		public void modifyProxywiredDependencies(final LinkedHashMap<String, Object> dependencies) {
			synchronized (this) {
				this.lock = true;
				this.backingStore = this.modifyBackingStore(dependencies);
				this.cachedProxy = this.buildCachedProxy(this.backingStore);
				this.lock = false;
				this.notifyAll();
			}
		}

		/**
		 * Modify the backing store following modifyProxywiredDependencies() call.
		 * 
		 * @param dependencies
		 * @return
		 */
		protected HashMap<String, Object> modifyBackingStore(final LinkedHashMap<String, Object> dependencies) {
			return new LinkedHashMap<String, Object>(dependencies);
		}

		@Override
		public Set<String> viewProxywiredDependencies() {
			return Collections.unmodifiableSet(this.backingStore.keySet());
		}

	}

	private class ProxywiredManageableInterceptor implements MethodInterceptor, IProxywiredManageable {

		private static final String INTERCEPTED_METHOD_NAME_1 = "modifyProxywiredDependencies";

		private static final String INTERCEPTED_METHOD_NAME_2 = "viewProxywiredDependencies";

		private final IProxywiredManageable internalManager;

		/**
		 * @param internalManager
		 */
		public ProxywiredManageableInterceptor(final IProxywiredManageable internalManager) {
			super();
			this.internalManager = internalManager;
		}

		@Override
		@SuppressWarnings("unchecked")
		public Object invoke(final MethodInvocation invocation) throws Throwable {
			Object result = null;

			if (this.isMethod1(invocation)) {
				final LinkedHashMap<String, Object> dependencies = (LinkedHashMap<String, Object>) invocation
						.getArguments()[0];
				this.modifyProxywiredDependencies(dependencies);
				result = null;
			} else if (this.isMethod2(invocation)) {
				result = this.viewProxywiredDependencies();
			} else {
				result = invocation.proceed();
			}

			return result;
		}

		@Override
		public void modifyProxywiredDependencies(final LinkedHashMap<String, Object> dependencies) {
			this.internalManager.modifyProxywiredDependencies(dependencies);
		}

		@Override
		public Set<String> viewProxywiredDependencies() {
			return this.internalManager.viewProxywiredDependencies();
		}

		/**
		 * Test if the invoked method is the one we want to advice.
		 * 
		 * @param invocation
		 * @return
		 */
		protected boolean isMethod1(final MethodInvocation invocation) {
			final Method method = invocation.getMethod();

			final boolean testName = ProxywiredManageableInterceptor.INTERCEPTED_METHOD_NAME_1.equals(method.getName());

			return testName;
		}

		/**
		 * Test if the invoked method is the one we want to advice.
		 * 
		 * @param invocation
		 * @return
		 */
		protected boolean isMethod2(final MethodInvocation invocation) {
			final Method method = invocation.getMethod();

			final boolean testName = ProxywiredManageableInterceptor.INTERCEPTED_METHOD_NAME_2.equals(method.getName());

			return testName;
		}

	}

}
