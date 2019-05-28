package com.sms.server.api.service;

import java.util.Set;


public interface IPendingServiceCall extends IServiceCall {

	/**
	 * Returns service call result
	 * 
	 * @return	       Remote call result
	 */
	public abstract Object getResult();

	/**
     * Setter for property 'result'.
     *
     * @param result Value to set for property 'result'.
     */
    public abstract void setResult(Object result);

	/**
	 * Registers callback object usually represented as an anonymous class
	 * instance that implements IPendingServiceCallback interface.
	 *  
	 * @param callback Callback object
	 */
	public void registerCallback(IPendingServiceCallback callback);

	/**
	 * Unregisters callback object usually represented as an anonymous class
	 * instance that implements IPendingServiceCallback interface.
	 *  
	 * @param callback Callback object
	 */
	public void unregisterCallback(IPendingServiceCallback callback);

	/**
	 * Returns list of callback objects, usually callback object represented as
	 * an anonymous class instance that implements IPendingServiceCallback
	 * interface.
	 *  
     * @return         Set of pending operations callbacks
	 */
	public Set<IPendingServiceCallback> getCallbacks();
}
