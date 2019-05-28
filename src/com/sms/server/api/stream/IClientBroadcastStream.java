package com.sms.server.api.stream;

import com.sms.server.api.statistics.IClientBroadcastStreamStatistics;


/**
 * A broadcast stream that comes from client.
 */
public interface IClientBroadcastStream extends IClientStream, IBroadcastStream {

	/**
	 * Notify client that stream is ready for publishing.
	 */
	public void startPublishing();
	
	/**
	 * Return statistics about the stream.
	 * 
	 * @return statistics
	 */
	public IClientBroadcastStreamStatistics getStatistics();
	
}
