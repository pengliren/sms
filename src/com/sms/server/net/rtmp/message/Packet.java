package com.sms.server.net.rtmp.message;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.apache.mina.core.buffer.IoBuffer;

import com.sms.server.net.rtmp.event.IRTMPEvent;

/**
 * 
 * @ClassName: Packet
 * @Description: RTMP packet. Consists of packet header, data and event context.
 * @author pengliren
 * 
 */
public class Packet implements Externalizable {

	/**
     * Header
     */
	private Header header;
    
	/**
     * RTMP event
     */
	private IRTMPEvent message;
    
	/**
     * Packet data
     */
	private IoBuffer data;

	public Packet() {
		data = null;
		header = null;
	}
    /**
     * Create packet with given header
     * @param header       Packet header
     */
    public Packet(Header header) {
		this.header = header;
		data = IoBuffer.allocate(header.getSize(), false);
		// Workaround for SN-19: BufferOverflowException
		// Size is checked in RTMPProtocolDecoder
		data.setAutoExpand(true);
	}

    /**
     * Create packet with given header and event context
     * @param header     RTMP header
     * @param event      RTMP message
     */
    public Packet(Header header, IRTMPEvent event) {
		this.header = header;
		this.message = event;
	}

	/**
     * Getter for header
     *
     * @return  Packet header
     */
    public Header getHeader() {
		return header;
	}

	/**
     * Setter for event context
     *
     * @param message  RTMP event context
     */
    public void setMessage(IRTMPEvent message) {
		this.message = message;
	}

	/**
     * Getter for event context
     *
     * @return RTMP event context
     */
    public IRTMPEvent getMessage() {
		return message;
	}

	/**
     * Setter for data
     *
     * @param data Packet data
     */
    public void setData(IoBuffer data) {
		this.data = data;
	}

	/**
     * Getter for data
     *
     * @return Packet data
     */
    public IoBuffer getData() {
		return data;
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		header = (Header) in.readObject();
		message = (IRTMPEvent) in.readObject();
		message.setHeader(header);
		message.setTimestamp(header.getTimer());
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(header);
		out.writeObject(message);
	}
}
