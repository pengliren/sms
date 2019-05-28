package com.sms.server.stream;

import com.sms.server.api.stream.IClientStream;
import com.sms.server.messaging.IMessageOutput;

/**
 * Service for consumer objects, used to get pushed messages at consumer endpoint.
 */
public interface IConsumerService {
	public static final String KEY = "consumerService";

    /**
     * Handles pushed messages
     *
     * @param stream       Client stream object
     * @return             Message object
     */
    IMessageOutput getConsumerOutput(IClientStream stream);
}
