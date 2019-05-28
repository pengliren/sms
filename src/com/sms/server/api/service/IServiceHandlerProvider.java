package com.sms.server.api.service;

import java.util.Set;

public interface IServiceHandlerProvider {

	/**
	 * Register an object that provides methods which can be called from a
	 * client.
	 * 
	 * <p>
	 * Example:<br>
	 * If you registered a handler with the name "<code>one.two</code>" that
	 * provides a method "<code>callMe</code>", you can call a method
	 * "<code>one.two.callMe</code>" from the client.</p>
	 * 
	 * @param name the name of the handler
	 * @param handler the handler object
	 */
	public void registerServiceHandler(String name, Object handler);

	/**
	 * Unregister service handler.
	 * 
	 * @param name the name of the handler
	 */
	public void unregisterServiceHandler(String name);

	/**
	 * Return a previously registered service handler.
	 * 
	 * @param name the name of the handler to return
	 * @return the previously registered handler
	 */
	public Object getServiceHandler(String name);

	/**
	 * Get list of registered service handler names.
	 * 
	 * @return the names of the registered handlers
	 */
	public Set<String> getServiceHandlerNames();
}
