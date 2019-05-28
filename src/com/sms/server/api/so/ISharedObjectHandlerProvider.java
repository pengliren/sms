package com.sms.server.api.so;

import com.sms.server.api.service.IServiceHandlerProvider;

/**
 * Supports registration and lookup of shared object handlers.
 * @author pengliren
 *
 */
public interface ISharedObjectHandlerProvider extends IServiceHandlerProvider {

	/**
	 * Register an object that provides methods which handle calls without
	 * a service name to a shared object.
	 * 
	 * @param handler the handler object
	 */
	public void registerServiceHandler(Object handler);

	/**
	 * Unregister the shared object handler for calls without a service name.
	 */
	public void unregisterServiceHandler(String name);
}
