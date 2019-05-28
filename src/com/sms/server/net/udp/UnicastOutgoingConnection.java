package com.sms.server.net.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UnicastOutgingConnection
 * @author pengliren
 *
 */
public class UnicastOutgoingConnection extends UDPTransportOutgoingConnectionBase implements IUDPSender {

	private static final Logger log = LoggerFactory.getLogger(UnicastOutgoingConnection.class);
	
	public int connectorIndex = -1;
	public DatagramSocket socket;
	public UDPSender sender;
	
	public UnicastOutgoingConnection(IUDPTransportOutgoing udpTransportOutgoing, DatagramSocket datagramSocket, InetSocketAddress address, UDPSender udpSender, int connectorIdx) {
		this.parent = udpTransportOutgoing;
		this.socket = datagramSocket;
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
