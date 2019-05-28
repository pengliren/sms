package com.sms.server.net.udp;

import java.net.InetSocketAddress;

/**
 * UDP Transport Outgoing
 * @author pengliren
 *
 */
public interface IUDPTransportOutgoing {

	public IUDPTransportOutgoingConnection connect(UDPDatagramConfig config, InetSocketAddress address);
	
	public IUDPTransportOutgoingConnection connect(UDPDatagramConfig config, String host, int port);

	public void disconnect(IUDPTransportOutgoingConnection connection);

	public long getConnectionCount();
}
