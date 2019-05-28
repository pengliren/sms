package com.sms.server.net.udp;

/**
 * UDP Transport Outgoing Connection Base
 * @author pengliren
 *
 */
public abstract class UDPTransportOutgoingConnectionBase extends UDPTransportConnectionBase implements IUDPTransportOutgoingConnection {

	protected IUDPTransportOutgoing parent = null;

	public void close() {
		this.parent.disconnect(this);
	}

	public abstract void sendMessage(byte[] data, int pos, int len);
}
