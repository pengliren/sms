package com.sms.server.net.rtp;

import org.apache.mina.core.buffer.IoBuffer;

/**
 * Base interface for RTP packets
 * 
 * @author pengliren
 */
public interface IRTPPacket {

	public byte getChannel();

	public void setChannel(byte channel);
	
	public IoBuffer toByteBuffer();
}
