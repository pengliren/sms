package com.sms.server.net.rtsp.message;

import org.apache.mina.core.buffer.IoBuffer;

/**
 * RTSP Channe lData
 * @author pengliren
 *
 */
public class RTSPChannelData {

	private byte channel;
	
	private IoBuffer data;
	
	public RTSPChannelData(byte channel, IoBuffer data) {
		
		this.channel = channel;
		this.data = data;
	}

	public byte getChannel() {
		return channel;
	}

	public IoBuffer getData() {
		return data;
	}
	
	@Override
	public String toString() {
		
		return "data : " + data.limit() + " channel : " + channel;
	}
}
