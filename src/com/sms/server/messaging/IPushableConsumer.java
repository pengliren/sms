package com.sms.server.messaging;

import java.io.IOException;

/**
 * A consumer that supports event-driven message handling and message pushing through pipes.
 */
public interface IPushableConsumer extends IConsumer {
	public static final String KEY = IPushableConsumer.class.getName();

    /**
     * Pushes message through pipe
     *
     * @param pipe         Pipe
     * @param message      Message
     * @throws IOException if message could not be written
     */
    void pushMessage(IPipe pipe, IMessage message) throws IOException;
}
