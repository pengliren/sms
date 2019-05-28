package com.sms.server.api.so;

import com.sms.server.api.IBasicScope;
import com.sms.server.api.statistics.ISharedObjectStatistics;

/**
 * Serverside access to shared objects.
 * @author pengliren
 *
 */
public interface ISharedObject extends IBasicScope, ISharedObjectBase, ISharedObjectSecurityService {

	public static final String TYPE = "SharedObject";
	
	/**
	 * Prevent shared object from being released. Each call to <code>acquire</code>
	 * must be paired with a call to <code>release</code> so the SO isn't held
	 * forever.
	 * 
	 * This method basically is a noop for persistent SOs as their data is stored
	 * and they can be released without losing their contents.
	 */
	public void acquire();

	/**
	 * Check if shared object currently is acquired.
	 * 
	 * @return <code>true</code> if the SO is acquired, otherwise <code>false</code>
	 */
	public boolean isAcquired();

	/**
	 * Release previously acquired shared object. If the SO is non-persistent,
	 * no more clients are connected the SO isn't acquired any more, the data
	 * is released. 
	 */
	public void release();

	/**
	 * Return statistics about the shared object.
	 * 
	 * @return statistics
	 */
	public ISharedObjectStatistics getStatistics();
}
