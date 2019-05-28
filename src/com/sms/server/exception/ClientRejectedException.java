package com.sms.server.exception;

public class ClientRejectedException extends RuntimeException {
	private static final long serialVersionUID = 9204597649465357898L;

	@SuppressWarnings("all")
	private Object reason;

	/** Constructs a new ClientRejectedException. */
	public ClientRejectedException() {
		this("Client rejected");
	}

	/**
	 * Create new exception with given rejection reason
	 * 
	 * @param reason
	 *            Rejection reason
	 */
	public ClientRejectedException(Object reason) {
		super("Client rejected");
		this.reason = reason;
	}

	/**
	 * Getter for reason
	 * 
	 * @return Rejection reason
	 */
	public Object getReason() {
		return reason;
	}
}
