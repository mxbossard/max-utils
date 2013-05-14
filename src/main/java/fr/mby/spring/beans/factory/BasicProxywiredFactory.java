/**
 * 
 */
package fr.mby.spring.beans.factory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
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

	@SuppressWarnings("unchecked")
	public IProxywiredManageable proxy(DependencyDescriptor descriptor, Object target) {
		final IProxywiredManageable result;
		
		Class<?> type = descriptor.getDependencyType();
		
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
	protected IProxywiredManageable proxySingleDependency(Class<?> type, Object target) {
		SortedSet<Object> backingSet = new TreeSet<Object>();
		backingSet.add(target);
		TargetSource targetSource = new ProxywiredBean(type, backingSet);
		Class<?>[] proxtyInterfaces = new Class[]{type, IProxywiredManageable.class};
		ProxyFactory proxyFactory = new ProxyFactory(proxtyInterfaces);
		proxyFactory.setTargetSource(targetSource);
		
		return (IProxywiredManageable) proxyFactory.getProxy();
	}

	/**
	 * Proxy assured by a ProxywiredSet.
	 * 
	 * @param target
	 * @return
	 */
	protected IProxywiredManageable proxyDependencyCollection(Collection<Object> target) {
		return new ProxywiredSet2(Collection.class, target);
	}

	/**
	 * Proxy assured by a ProxywiredMap.
	 * 
	 * @param target
	 * @return
	 */
	protected IProxywiredManageable proxyDependencyMap(Map<String, Object> target) {
		return new ProxywiredMap2(Map.class, target);
	}
	
	private class ProxywiredBean extends ProxwyiredStruct<Object, Set<Object>> {

		protected ProxywiredBean(Class<?> type, Object target) {
			super(type, target);
		}

		@Override
		protected Set<Object> initStruct(Object target) {
			Set<Object> newSet = new HashSet<Object>(1);
			newSet.add(target);
			return newSet;
		}

		@Override
		protected Set<Object> modifyProxywiredDepenciesInternal(Map<String, Object> dependencies) {
			Set<Object> newSet = new HashSet<Object>(dependencies.values());
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

		protected ProxywiredSet(Collection<? extends Object> arg0) {
			super(arg0);
		}

		public void modifyProxywiredDepencies(Map<String, Object> dependencies) {	
			this.clear();
			this.addAll(dependencies.values());
		}
		
	}

	private class ProxywiredSet2 extends ProxwyiredStruct<Collection<Object>, Set<Object>> {

		protected ProxywiredSet2(Class<?> type, Collection<Object> target) {
			super(type, target);
		}

		@Override
		protected Set<Object> initStruct(Collection<Object> target) {
			return Collections.unmodifiableSet(new HashSet<Object>(target));
		}

		@Override
		protected Set<Object> modifyProxywiredDepenciesInternal(Map<String, Object> dependencies) {
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
		
		protected ProxywiredMap(Map<? extends String, ? extends Object> arg0) {
			super(16, 0.75f, 1);
			this.putAll(arg0);
		}

		public void modifyProxywiredDepencies(Map<String, Object> dependencies) {
			this.clear();
			this.putAll(dependencies);
		}
	}
	
	private class ProxywiredMap2 extends ProxwyiredStruct<Map<String, Object>, Map<String, Object>> {

		protected ProxywiredMap2(Class<?> type, Map<String, Object> target) {
			super(type, target);
		}

		@Override
		protected Map<String, Object> initStruct(Map<String, Object> target) {
			return Collections.unmodifiableMap(new HashMap<String, Object>(target));
		}

		@Override
		protected Map<String, Object> modifyProxywiredDepenciesInternal(Map<String, Object> dependencies) {
			return Collections.unmodifiableMap(new HashMap<String, Object>(dependencies));
		}

		@Override
		protected Map<String, Object> getTargetInternal() {
			return this.backingStore;
		}
		
	}
	
	private abstract class ProxwyiredStruct<T, S> implements TargetSource, IProxywiredManageable {

		private Class<?> type;
		
		protected S backingStore;
		
		private boolean lock = false;

		protected ProxwyiredStruct(Class<?> type, T target) {
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

		public Class<?> getTargetClass() {
			return this.type;
		}

		public boolean isStatic() {
			return false;
		}

		public Object getTarget() throws Exception {
			while(this.lock) {
				this.wait(10);
			}
			
			return this.getTargetInternal();
		}


		public void releaseTarget(Object target) throws Exception {
			// Nothing to do on release
		}

		public void modifyProxywiredDepencies(Map<String, Object> dependencies) {
			this.lock = true;
			this.backingStore = this.modifyProxywiredDepenciesInternal(dependencies);
			this.lock = false;
			this.notifyAll();
		}
		
	}
}
