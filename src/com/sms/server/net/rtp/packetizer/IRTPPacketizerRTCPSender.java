package com.sms.server.net.rtp.packetizer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;

/**
 * RTP Packetizer RTCP Sender Interface
 * @author pengliren
 *
 */
public interface IRTPPacketizerRTCPSender {

	public void init(InetSocketAddress address) throws SocketException;

	public void sendRTCP(IRTPPacketizer packetizer, long ts);
	
	public InetSocketAddress getAddress();
	
	public void sendRCTPMessage(byte[] data, int pos, int len) throws IOException;
	
	public void close();
}
