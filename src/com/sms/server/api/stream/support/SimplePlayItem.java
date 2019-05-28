package com.sms.server.api.stream.support;

import com.sms.server.api.stream.IPlayItem;
import com.sms.server.messaging.IMessageInput;

/**
 * Simple playlist item implementation
 */
public class SimplePlayItem implements IPlayItem {
	

	/**
	 * Playlist item name
	 */
	protected final String name;

	/**
	 * Start mark
	 */
	protected final long start;

	/**
	 * Length - amount to play
	 */
	protected final long length;
	
	/**
	 * Message source
	 */
	protected IMessageInput msgInput;

	private SimplePlayItem(String name) {
		this.name = name;
		this.start = -2L;
		this.length = -1L;
	}
	
	private SimplePlayItem(String name, long start, long length) {
		this.name = name;
		this.start = start;
		this.length = length;
	}

	/**
	 * Returns play item length in milliseconds
	 * 
	 * @return	Play item length in milliseconds
	 */
	public long getLength() {
		return length;
	}

	/**
	 * Returns IMessageInput object. IMessageInput is an endpoint for a consumer
	 * to connect.
	 * 
	 * @return	IMessageInput object
	 */
	public IMessageInput getMessageInput() {
		return msgInput;
	}

	/**
	 * Returns item name
	 * 
	 * @return	item name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns boolean value that specifies whether item can be played
	 */
	public long getStart() {
		return start;
	}

	/**
	 * Alias for getMessageInput
	 * 
	 * @return      Message input source
	 */
	public IMessageInput getMsgInput() {
		return msgInput;
	}

	/**
	 * Setter for message input
	 *
	 * @param msgInput Message input
	 */
	public void setMsgInput(IMessageInput msgInput) {
		this.msgInput = msgInput;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (int) (start ^ (start >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimplePlayItem other = (SimplePlayItem) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (start != other.start)
			return false;
		return true;
	}

	/**
	 * Builder for SimplePlayItem
	 * 
	 * @param name
	 * @return play item instance
	 */
	public static SimplePlayItem build(String name) {
		SimplePlayItem playItem = new SimplePlayItem(name);
		return playItem;
	}	
	
	/**
	 * Builder for SimplePlayItem
	 * 
	 * @param name
	 * @param start
	 * @param length
	 * @return play item instance
	 */
	public static SimplePlayItem build(String name, long start, long length) {
		SimplePlayItem playItem = new SimplePlayItem(name, start, length);
		return playItem;
	}
	
}
