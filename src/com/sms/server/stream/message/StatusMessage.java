package com.sms.server.stream.message;

import com.sms.server.messaging.AbstractMessage;
import com.sms.server.net.rtmp.status.Status;

public class StatusMessage extends AbstractMessage {
	private Status body;

	/**
     * Getter for property 'body'.
     *
     * @return Value for property 'body'.
     */
    public Status getBody() {
		return body;
	}

	/**
     * Setter for property 'body'.
     *
     * @param body Value to set for property 'body'.
     */
    public void setBody(Status body) {
		this.body = body;
	}

}
