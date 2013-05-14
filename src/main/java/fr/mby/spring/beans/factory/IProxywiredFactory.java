/**
 * 
 */
package fr.mby.spring.beans.factory;

import org.springframework.beans.factory.config.DependencyDescriptor;

import fr.mby.spring.beans.factory.IProxywiredManager.IProxywiredManageable;

/**
 * @author Maxime BOSSARD.
 *
 */
public interface IProxywiredFactory {

	IProxywiredManageable proxy(DependencyDescriptor descriptor, Object target);
	
}
