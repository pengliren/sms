package com.sms.server.stream;

import java.lang.ref.WeakReference;

import com.sms.server.api.stream.IClientStream;
import com.sms.server.api.stream.IStreamCapableConnection;

/**
 * Abstract base for client streams
 */
public abstract class AbstractClientStream extends AbstractStream implements IClientStream {

	/**
	 *  Stream identifier. Unique across server.
	 */
	private int streamId;
	
	/**
	 * Stream name of the broadcasting stream.
	 */
	private String broadcastStreamPublishName;

	/**
	 *  Connection that works with streams
	 */
	private WeakReference<IStreamCapableConnection> conn;

	/**
	 * Buffer duration in ms as requested by the client
	 */
	private int clientBufferDuration;

	/**
	 * Return stream id
	 * @return           Stream id
	 */
	public int getStreamId() {
		return streamId;
	}

	/**
	 * Return connection associated with stream
	 * @return           Stream capable connection object
	 */
	public IStreamCapableConnection getConnection() {
		return conn.get();
	}

	/**
	 * Setter for stream id
	 * @param streamId       Stream id
	 */
	public void setStreamId(int streamId) {
		this.streamId = streamId;
	}

	/**
	 * Setter for stream capable connection
	 * @param conn           IStreamCapableConnection object
	 */
	public void setConnection(IStreamCapableConnection conn) {
		this.conn = new WeakReference<IStreamCapableConnection>(conn);
	}

	/** {@inheritDoc} */
	public void setClientBufferDuration(int duration) {
		clientBufferDuration = duration;
	}

	/**
	 * Get duration in ms as requested by the client.
	 *
	 * @return value
	 */
	public int getClientBufferDuration() {
		return clientBufferDuration;
	}

	/**
	 * Sets the broadcasting streams name.
	 * 
	 * @param broadcastStreamPublishName name of the broadcasting stream
	 */
	public void setBroadcastStreamPublishName(String broadcastStreamPublishName) {
		this.broadcastStreamPublishName = broadcastStreamPublishName;
	}

	/** {@inheritDoc} */
	public String getBroadcastStreamPublishName() {
		return broadcastStreamPublishName;
	}
		
}
