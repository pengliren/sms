package com.sms.server.api.stream;

import org.apache.mina.core.buffer.IoBuffer;

/**
 * Packet containing stream data.
 */
public interface IStreamPacket {

	/**
	 * Type of this packet. This is one of the <code>TYPE_</code> constants.
	 * 
	 * @return the type
	 */
	public byte getDataType();
	
	/**
	 * Timestamp of this packet.
	 * 
	 * @return the timestamp in milliseconds
	 */
	public long getTimestamp();
	
	/**
	 * Packet contents.
	 * 
	 * @return the contents
	 */
	public IoBuffer getData();
	
}
