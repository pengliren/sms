package com.sms.server.api.service;

public interface IServiceCall {

	/**
	 * Whether call was successful or not
	 * 
	 * @return	<code>true</code> on success, <code>false</code> otherwise
	 */
	public abstract boolean isSuccess();

	/**
	 * Returns service method name 
	 * 
	 * @return	Service method name as string
	 */
	public abstract String getServiceMethodName();

	/**
	 * Returns service name
	 * 
	 * @return	Service name
	 */
	public abstract String getServiceName();

	/**
	 * Returns array of service method arguments
	 * 
	 * @return	array of service method arguments
	 */
	public abstract Object[] getArguments();

	/**
	 * Get service call status
	 * 
	 * @return	service call status
	 */
	public abstract byte getStatus();
	
	/**
	 * Returns the time stamp at which this object was deserialized.
	 * 
	 * @return the readTime
	 */
	public long getReadTime();

	/**
	 * Returns the time stamp at which this object was serialized.
	 * 
	 * @return the writeTime
	 */
	public long getWriteTime();

	/**
	 * Get service call exception
	 * 
	 * @return	service call exception
	 */
	public abstract Exception getException();

	/**
	 * Sets status
	 * 
	 * @param status Status as byte
	 */
	public abstract void setStatus(byte status);

	/**
	 * Sets exception
	 * 
	 * @param exception Call exception
	 */
	public abstract void setException(Exception exception);
}
