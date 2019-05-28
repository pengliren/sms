package com.sms.server.messaging;

/**
 * Helper class for pipe structure.
 */
public class PipeUtils {
	/**
	 * Connect a provider/consumer with a pipe.
	 * 
	 * @param provider         Provider
	 * @param pipe             Pipe that used to estabilish connection
	 * @param consumer         Consumer
	 */
	public static void connect(IProvider provider, IPipe pipe,
			IConsumer consumer) {
		pipe.subscribe(provider, null);
		pipe.subscribe(consumer, null);
	}

	/**
	 * Disconnect a provider/consumer from a pipe.
	 * 
	 * @param provider         Provider
	 * @param pipe             Pipe to disconnect from
	 * @param consumer         Consumer
	 */
	public static void disconnect(IProvider provider, IPipe pipe,
			IConsumer consumer) {
		pipe.unsubscribe(provider);
		pipe.unsubscribe(consumer);
	}
}
