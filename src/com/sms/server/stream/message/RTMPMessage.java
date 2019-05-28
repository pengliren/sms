package com.sms.server.stream.message;

import com.sms.server.messaging.AbstractMessage;
import com.sms.server.net.rtmp.event.IRTMPEvent;

/**
 * RTMP message
 */
public class RTMPMessage extends AbstractMessage {
	
	private final IRTMPEvent body;

	/**
	 * Creates a new rtmp message.
	 * 
	 * @param body value to set for property 'body'
	 */
	private RTMPMessage(IRTMPEvent body) {
		this.body = body;
	}
	
	/**
	 * Creates a new rtmp message.
	 * 
	 * @param body value to set for property 'body'
	 * @param eventTime updated timestamp
	 */
	private RTMPMessage(IRTMPEvent body, long eventTime) {
		this.body = body;
		this.body.setTimestamp(eventTime);
	}
	
	/**
	 * Return RTMP message body
	 *
	 * @return Value for property 'body'.
	 */
	public IRTMPEvent getBody() {
		return body;
	}

	/**
	 * Builder for RTMPMessage.
	 * 
	 * @param body event data
	 * @return Immutable RTMPMessage
	 */
	public final static RTMPMessage build(IRTMPEvent body) {
		return new RTMPMessage(body);
	}
	
	/**
	 * Builder for RTMPMessage.
	 * 
	 * @param body event data
	 * @param eventTime time value to set on the event body
	 * @return Immutable RTMPMessage
	 */
	public final static RTMPMessage build(IRTMPEvent body, long eventTime) {
		return new RTMPMessage(body, eventTime);
	}
	
}
