package com.sms.server.net.udp;

/**
 * UDP Transport Incoming Connection (abstract class)
 * @author pengliren
 *
 */
public abstract class UDPTransportIncomingConnectionBase extends UDPTransportConnectionBase implements IUDPTransportIncomingConnection {

	protected IUDPTransportIncoming parent = null;
	
	@Override
	public void close() {
		parent.unbind(this);
	}
}
