package com.sms.server.exception;
/**
 * Exception class than contains additional parameters to return to the client.
 */
public class ClientDetailsException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1908769505547253205L;

	/**
	 * Parameters to return to the client.
	 */
	private Object parameters;
	
	/**
	 * Also return stacktrace to client?
	 */
	private boolean stacktrace;
	
	/**
	 * Create new exception object from message and parameters. By default, no
	 * stacktrace is returned to the client.
	 * 
	 * @param message message
	 * @param params parameters for message
	 */
	public ClientDetailsException(String message, Object params) {
		this(message, params, false);
	}

	/**
	 * Create new exception object from message and parameters with optional stacktrace.
	 * 
	 * @param message message
	 * @param params parameters
	 * @param includeStacktrace whether or not to include a stack trace
	 */
	public ClientDetailsException(String message, Object params, boolean includeStacktrace) {
		super(message);
		this.parameters = params;
		this.stacktrace = includeStacktrace;
	}

	/**
	 * Get parameters to return to the client.
	 * 
	 * @return parameters
	 */
	public Object getParameters() {
		return parameters;
	}
	
	/**
	 * Should the stacktrace returned to the client?
	 * 
	 * @return stacktrace
	 */
	public boolean includeStacktrace() {
		return stacktrace;
	}
	
}
