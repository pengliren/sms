package com.sms.server.api;

public interface IMappingStrategy {

	/**
	 * Map a name to the name of a service.
	 * 
	 * @param name name to map
	 * @return      The name of the service with the passed name
	 */
	public String mapServiceName(String name);

	/**
	 * Map a context path to the name of a scope handler.
	 * 
	 * @param contextPath context path to map
	 * @return      The name of a scope handler
	 */
	public String mapScopeHandlerName(String contextPath);

	/**
	 * Map a context path to a path prefix for resources.
	 * 
	 * @param contextPath context path to map
	 * @return      The path prefix for resources with the given name 
	 */
	public String mapResourcePrefix(String contextPath);
}
