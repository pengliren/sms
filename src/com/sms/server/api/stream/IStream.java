package com.sms.server.api.stream;

import com.sms.server.api.IScope;

/**
 * Base interface for stream objects.
 * A stream object is always associated with a scope. 
 */
public interface IStream {

	/**
	 * Get the name of the stream. The name is unique across the server. This is
	 * just an id of the stream and NOT the name that is used at client side to
	 * subscribe to the stream. For that name, use
	 * {@link IBroadcastStream#getPublishedName()}
	 * 
	 * @return the name of the stream
	 */
	public String getName();

	/**
	 * Get Codec info for a stream.
	 * 
	 * @return codec info
	 */
	IStreamCodecInfo getCodecInfo();

	/**
	 * Get the scope this stream is associated with.
	 * 
	 * @return scope object
	 */
	public IScope getScope();

	/**
	 * Start this stream.
	 */
	public void start();

	/**
	 * Stop this stream.
	 */
	public void stop();

	/**
	 * Close this stream.
	 */
	public void close();
	
	/**
	 * Returns the timestamp at which the stream was created.
	 * 
	 * @return creation timestamp
	 */
	public long getCreationTime();

}
