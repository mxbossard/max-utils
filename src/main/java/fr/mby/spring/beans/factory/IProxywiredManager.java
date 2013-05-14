/**
 * 
 */
package fr.mby.spring.beans.factory;

import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import org.springframework.beans.factory.config.DependencyDescriptor;

/**
 * @author Maxime BOSSARD.
 *
 */
public interface IProxywiredManager {

	Object getProxywiredDependency(DependencyDescriptor descriptor, String beanName,
			Set<String> autowiredBeanNames, Object target);
	
	void modifyProxywiredDepencies(Class<?> type, SortedSet<String> beanNames);
	
	SortedSet<String> getProxywiredDependencies(Class<?> type);
	
	SortedSet<String> getAllDependencies(Class<?> type);
	
	public interface IProxywiredManageable {
		
		void modifyProxywiredDepencies(Map<String, Object> dependencies);
		
	}
}
