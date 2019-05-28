package com.sms.server.messaging;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface IMessageInput {

	/**
	 * Pull message from this input endpoint. Return
	 * w/o waiting.
	 * @return The pulled message or <tt>null</tt> if message is
	 * not available.
	 * @throws IOException on error
	 */
	IMessage pullMessage() throws IOException;

	/**
	 * Pull message from this input endpoint. Wait
	 * <tt>wait</tt> milliseconds if message is not available.
	 * @param wait milliseconds to wait when message is not
	 * available.
	 * @return The pulled message or <tt>null</tt> if message is
	 * not available.
	 */
	IMessage pullMessage(long wait);

	/**
	 * Connect to a consumer.
	 * 
	 * @param consumer         Consumer
	 * @param paramMap         Parameters map
	 * @return <tt>true</tt> when successfully subscribed,
	 * <tt>false</tt> otherwise.
	 */
	boolean subscribe(IConsumer consumer, Map<String, Object> paramMap);

	/**
	 * Disconnect from a consumer.
	 * 
	 * @param consumer    Consumer to disconnect
	 * @return <tt>true</tt> when successfully unsubscribed,
	 * <tt>false</tt> otherwise.
	 */
	boolean unsubscribe(IConsumer consumer);

	/**
     * Getter for consumers list.
     *
     * @return Consumers.
     */
    List<IConsumer> getConsumers();

	/**
	 * Send OOB Control Message to all providers on the other side of pipe.
	 * 
	 * @param consumer
	 *            The consumer that sends the message
	 * @param oobCtrlMsg
     *            Out-of-band control message
	 */
	void sendOOBControlMessage(IConsumer consumer, OOBControlMessage oobCtrlMsg);
}
