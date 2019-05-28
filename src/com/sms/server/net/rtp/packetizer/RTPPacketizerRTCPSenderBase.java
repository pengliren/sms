package com.sms.server.net.rtp.packetizer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

/**
 * RTP Packetizer RTCP Sender Base
 * @author pengliren
 *
 */
public abstract class RTPPacketizerRTCPSenderBase implements IRTPPacketizerRTCPSender {

	protected IRTPPacketizer rtpPacketizer;
	
	private InetSocketAddress rtcpRemoteAddress;
	
	private DatagramSocket rtcpSocket;
	
	private boolean inited = false;
	
	@Override
	public void init(InetSocketAddress address) throws SocketException {
		
		rtcpRemoteAddress = address;
        rtcpSocket = new DatagramSocket();
        
        inited = true;
	}
	
	@Override
	public InetSocketAddress getAddress() {
	
		return this.rtcpRemoteAddress;
	}
	
	@Override
	public void sendRCTPMessage(byte[] data, int pos, int len) throws IOException {
		if (inited) {
			DatagramPacket dataPack = new DatagramPacket(data, pos, len, rtcpRemoteAddress.getAddress(), rtcpRemoteAddress.getPort());
			rtcpSocket.send(dataPack);
		}
	}
	
	@Override
	public void close() {
		if (rtcpSocket != null) {
			rtcpSocket.close();
		}
	}
}
