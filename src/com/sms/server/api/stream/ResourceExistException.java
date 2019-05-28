package com.sms.server.api.stream;


public class ResourceExistException extends Exception {
	private static final long serialVersionUID = 443389396219999143L;

	/** Constructs a new ResourceExistException. */
    public ResourceExistException() {
		super();
	}

	public ResourceExistException(String message, Throwable cause) {
		super(message, cause);
	}

	public ResourceExistException(String message) {
		super(message);
	}

	public ResourceExistException(Throwable cause) {
		super(cause);
	}

}
