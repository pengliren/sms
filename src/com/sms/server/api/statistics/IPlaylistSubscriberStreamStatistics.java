package com.sms.server.api.statistics;

/**
 * Statistical informations about a stream that is subscribed by a client.
 */
public interface IPlaylistSubscriberStreamStatistics extends IStreamStatistics {

	/**
	 * Return total number of bytes sent to the client from this stream.
	 * 
	 * @return number of bytes
	 */
	public long getBytesSent();	
	
	/**
	 * Return the buffer duration as requested by the client.
	 * 
	 * @return the buffer duration in milliseconds
	 */
	public int getClientBufferDuration();
	
	/**
	 * Return estimated fill ratio of the client buffer.
	 * 
	 * @return fill ratio in percent
	 */
	public double getEstimatedBufferFill();
	
}
