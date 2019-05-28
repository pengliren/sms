package com.sms.server.net.rtmp.event;

import org.apache.mina.core.buffer.IoBuffer;


/**
 * AMF3 stream send message.
 */
public class FlexStreamSend extends Notify {

	private static final long serialVersionUID = -4226252245996614504L;

	public FlexStreamSend() {}
	/**
	 * Create new stream send object.
	 * 
	 * @param data data
	 */
	public FlexStreamSend(IoBuffer data) {
		super(data);
	}
	
	/** {@inheritDoc} */
    @Override
	public byte getDataType() {
		return TYPE_FLEX_STREAM_SEND;
	}

}
