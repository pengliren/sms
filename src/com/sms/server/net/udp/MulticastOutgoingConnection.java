package com.sms.server.net.udp;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Multicast Outgoing Connection
 * @author pengliren
 *
 */
public class MulticastOutgoingConnection extends UDPTransportOutgoingConnectionBase implements IUDPSender {

	private static final Logger log = LoggerFactory.getLogger(MulticastOutgoingConnection.class);
	
	public int connectorIndex = -1;
	public MulticastSocket socket;
	public UDPSender sender;

	public MulticastOutgoingConnection(IUDPTransportOutgoing udpTransportOutgoing, MulticastSocket multicastSocket, InetSocketAddress address, UDPSender udpSender, int connectorIdx) {
		this.parent = udpTransportOutgoing;
		this.socket = multicastSocket;
		this.sender = udpSender;
		this.connectorIndex = connectorIdx;
		this.isMulticast = true;
		this.address = address;
	}

	@Override
	public synchronized void handleSendMessage(byte[] data, int pos, int len) {
		try {
			if (this.isOpen) {
				DatagramPacket packet = new DatagramPacket(data, pos, len, address.getAddress(), address.getPort());
				this.socket.send(packet);
			}
		} catch (Exception e) {
			log.error("handleSendMessage: {}", e.toString());
		}
	}

	@Override
	public synchronized void sendMessage(byte[] data, int pos, int len) {
		try {
			if (this.isOpen) {
				this.sender.handleMessage(this, data, pos, len);
			}
		} catch (Exception e) {
			log.error("sendMessage: {}", e.toString());
		}
	}
}
