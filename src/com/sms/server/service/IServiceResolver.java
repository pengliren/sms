package com.sms.server.service;

import com.sms.server.api.IScope;

/**
 * Interface for objects that resolve service names to services.
 * 
 * This is used by the ServiceInvoker to lookup the service to invoke
 * a method on.
 */
public interface IServiceResolver {

	/**
	 * Search for a service with the given name in the scope.
	 * 
	 * @param scope the scope to search in
	 * @param serviceName the name of the service
	 * @return the object implementing the service or <code>null</code> if
	 *         service doesn't exist
	 */
	public Object resolveService(IScope scope, String serviceName);

}
