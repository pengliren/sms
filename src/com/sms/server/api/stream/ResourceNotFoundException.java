package com.sms.server.api.stream;

public class ResourceNotFoundException extends Exception {
	private static final long serialVersionUID = -1963629259187714996L;

	/** Constructs a new ResourceNotFoundException. */
    public ResourceNotFoundException() {
		super();
	}

	public ResourceNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public ResourceNotFoundException(String message) {
		super(message);
	}

	public ResourceNotFoundException(Throwable cause) {
		super(cause);
	}

}
