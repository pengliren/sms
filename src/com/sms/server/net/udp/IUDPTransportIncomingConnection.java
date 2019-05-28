package com.sms.server.net.udp;

import java.net.InetSocketAddress;

/**
 * UDPTransport Incoming Connection
 * @author pengliren
 *
 */
public interface IUDPTransportIncomingConnection {

	public boolean isMulticast();

	public InetSocketAddress getAddress();

	public void close();

	public boolean isOpen();
}
