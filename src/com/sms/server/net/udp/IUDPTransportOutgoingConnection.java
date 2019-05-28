package com.sms.server.net.udp;

import java.net.InetSocketAddress;

/**
 * UDP Transport Outgoing Connection
 * @author pengliren
 *
 */
public interface IUDPTransportOutgoingConnection {

	public boolean isMulticast();

	public InetSocketAddress getAddress();

	public void sendMessage(byte[] data, int pos, int len);

	public void close();

	public boolean isOpen();
}
