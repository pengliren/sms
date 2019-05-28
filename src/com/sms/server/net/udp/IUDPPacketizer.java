package com.sms.server.net.udp;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mina.core.buffer.IoBuffer;

import com.sms.server.api.stream.IStreamPacket;

/**
 * UDPPacketizer Inteface
 * @author pengliren
 *
 */
public interface IUDPPacketizer {

	public void handleStreamPacket(IStreamPacket packet);
	
	public void stop();
	
	public IUDPTransportOutgoingConnection getConnection();
	
	public void setVideoConfig(IoBuffer config);
	
	public void setAudioConfig(IoBuffer config);
	
	public IoBuffer getVideoConfig();
	
	public IoBuffer getAudioConfig();
	
	public AtomicInteger getFrameCount();
	
	public boolean isInit();
}
