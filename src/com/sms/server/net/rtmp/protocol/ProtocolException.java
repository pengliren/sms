package com.sms.server.net.rtmp.protocol;


public class ProtocolException extends RuntimeException {

	/**
	 * Base exception for all protocol exceptions.
	 */
	private static final long serialVersionUID = -5380844081848027068L;

    /**
     * Create protocol exception with given message.
	 *
     * @param message message
     */
    public ProtocolException(String message) {
		super(message);
	}

    /**
     * Create protocol exception with given message and cause.
	 *
     * @param message message
     * @param cause cause
     */
    public ProtocolException(String message, Throwable cause) {
    	super(message, cause);
    }

}
