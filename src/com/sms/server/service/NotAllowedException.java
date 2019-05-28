package com.sms.server.service;

/**
 * Thrown when a client is not allowed to execute a method.
 */
public class NotAllowedException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7552833324276839926L;

	public NotAllowedException() {
		super();
	}
	
	public NotAllowedException(String message) {
		super(message);
	}
	
}
