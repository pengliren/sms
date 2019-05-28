package com.sms.server.messaging;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Output Endpoint for a provider to connect.
 */
public interface IMessageOutput {
	/**
	 * Push a message to this output endpoint. May block
	 * the pusher when output can't handle the message at
	 * the time.
	 * @param message Message to be pushed.
	 * @throws IOException If message could not be written.
	 */
	void pushMessage(IMessage message) throws IOException;

	/**
	 * Connect to a provider. Note that params passed has nothing to deal with
     *  NetConnection.connect in client-side Flex/Flash RIA.
	 * 
	 * @param provider       Provider
	 * @param paramMap       Parameters passed with connection
	 * @return <tt>true</tt> when successfully subscribed,
	 * <tt>false</tt> otherwise.
	 */
	boolean subscribe(IProvider provider, Map<String, Object> paramMap);

	/**
	 * Disconnect from a provider.
	 * 
	 * @param provider       Provider
	 * @return <tt>true</tt> when successfully unsubscribed,
	 * <tt>false</tt> otherwise.
	 */
	boolean unsubscribe(IProvider provider);

	/**
     * Getter for providers
     *
     * @return  Providers
     */
    List<IProvider> getProviders();

	/**
	 * Send OOB Control Message to all consumers on the other side of pipe.
	 * 
	 * @param provider
	 *            The provider that sends the message
	 * @param oobCtrlMsg
     *            Out-of-band control message
	 */
	void sendOOBControlMessage(IProvider provider, OOBControlMessage oobCtrlMsg);
}
