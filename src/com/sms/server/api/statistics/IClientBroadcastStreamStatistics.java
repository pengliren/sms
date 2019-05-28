package com.sms.server.api.statistics;


/**
 * Statistical informations about a stream that is broadcasted by a client.
 */
public interface IClientBroadcastStreamStatistics extends IStreamStatistics {

	/**
	 * Get the filename the stream is being saved as.
	 * 
	 * @return	The filename relative to the scope or <code>null</code>
	 * 			if the stream is not being saved. 
	 */
	public String getSaveFilename();

	/**
	 * Get stream publish name. Publish name is the value of the first parameter
	 * had been passed to <code>NetStream.publish</code> on client side in
	 * SWF.
	 * 
	 * @return	Stream publish name	
	 */
	public String getPublishedName();

	/**
	 * Return total number of subscribers.
	 * 
	 * @return number of subscribers
	 */
	public int getTotalSubscribers();
	
	/**
	 * Return maximum number of concurrent subscribers.
	 * 
	 * @return number of subscribers
	 */
	public int getMaxSubscribers();
	
	/**
	 * Return current number of subscribers.
	 * 
	 * @return number of subscribers
	 */
	public int getActiveSubscribers();
	
	/**
	 * Return total number of bytes received from client for this stream.
	 * 
	 * @return number of bytes
	 */
	public long getBytesReceived();
	
}
