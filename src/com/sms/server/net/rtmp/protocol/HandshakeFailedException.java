package com.sms.server.net.rtmp.protocol;


public class HandshakeFailedException extends ProtocolException {
	
	private static final long serialVersionUID = 8255789603304183796L;

    /**
     * Create handshake failed exception with given message
	 *
     * @param message message
     */
	public HandshakeFailedException(String message) {
		super(message);
	}
	
}
