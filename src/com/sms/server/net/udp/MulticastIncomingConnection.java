package com.sms.server.net.udp;

/**
 * Multicast Incoming Connection
 * @author pengliren
 *
 */
public class MulticastIncomingConnection extends UDPTransportIncomingConnectionBase {

	public int connectorIndex = -1;
	public UDPSender sender;
	public MulticastListener listener;

	public MulticastIncomingConnection(IUDPTransportIncoming udpTransportIncoming, MulticastListener listener) {
		this.parent = udpTransportIncoming;
		this.listener = listener;
		this.isMulticast = true;
	}
}
