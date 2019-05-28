package com.sms.server.api.stream;

/**
 * A stream that is bound to a client.
 */
public interface IClientStream extends IStream {

	public static final String MODE_READ = "read";

	public static final String MODE_RECORD = "record";

	public static final String MODE_APPEND = "append";

	public static final String MODE_LIVE = "live";
	
	public static final String MODE_PUBLISH = "publish";

	/**
	 * Get stream id allocated in a connection.
	 * 
	 * @return the stream id
	 */
	int getStreamId();

	/**
	 * Get connection containing the stream.
	 * 
	 * @return the connection object or <code>null</code> if the connection is no longer active
	 */
	IStreamCapableConnection getConnection();

	/**
	 * Set the buffer duration for this stream as requested by the client.
	 * 
	 * @param bufferTime duration in ms the client wants to buffer
	 */
	void setClientBufferDuration(int bufferTime);
	
	/**
	 * Get the buffer duration for this stream as requested by the client.
	 * 
	 * @return bufferTime duration in ms the client wants to buffer
	 */
	int getClientBufferDuration();	

	/**
	 * Returns the published stream name that this client is consuming.
	 * 
	 * @return stream name of stream being consumed
	 */
	String getBroadcastStreamPublishName();
	
}
