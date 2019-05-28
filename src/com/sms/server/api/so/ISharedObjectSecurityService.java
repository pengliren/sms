package com.sms.server.api.so;

import java.util.Set;

import com.sms.server.api.IScopeService;

public interface ISharedObjectSecurityService extends IScopeService {

	/** 
	 * Name of a bean defining that scope service.
	 * */
	public static final String BEAN_NAME = "sharedObjectSecurityService";

	/**
	 * Add handler that protects shared objects.
	 * 
	 * @param handler Handler to add.
	 */
	public void registerSharedObjectSecurity(ISharedObjectSecurity handler);

	/**
	 * Remove handler that protects shared objects.
	 * 
	 * @param handler Handler to remove.
	 */
	public void unregisterSharedObjectSecurity(ISharedObjectSecurity handler);

	/**
	 * Get handlers that protect shared objects.
	 * 
	 * @return list of handlers
	 */
	public Set<ISharedObjectSecurity> getSharedObjectSecurity();
}
