package com.sms.server.exception;

public class ClientNotFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3135070223941800751L;

    /**
     * Create exception from given string message
     * @param id id
     */
    public ClientNotFoundException(String id) {
		super("Client \"" + id + "\" not found.");
	}
}
