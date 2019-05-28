package com.sms.server.net.rtp;

/**
 * Base for RTP packets
 * 
 * @author pengliren
 */
public abstract class BaseRTPPacket implements IRTPPacket {

	protected byte channel;

	@Override
	public byte getChannel() {

		return channel;
	}

	@Override
	public void setChannel(byte channel) {

		this.channel = channel;
	}

}
