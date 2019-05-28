package com.sms.server.api.stream;

/**
 * Listener that is notified about packets received from a stream.
 */
public interface IStreamListener {

	/**
	 * A packet has been received from a stream.
	 * 
	 * @param stream the stream the packet has been received for
	 * @param packet the packet received
	 */
	public void packetReceived(IBroadcastStream stream, IStreamPacket packet);
	
}
