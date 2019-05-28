package com.sms.server.api.stream;

import com.sms.server.messaging.IMessageInput;

/**
 * Playlist item. Each playlist item has name, start time, length in milliseconds and
 * message input source.
 */
public interface IPlayItem {
	/**
	 * Get name of item.
	 * The VOD or Live stream provider is found according to this name.
	 * @return the name
	 */
	String getName();

	/**
	 * Start time in milliseconds.
	 * 
	 * @return start time
	 */
	long getStart();

	/**
	 * Play length in milliseconds.
	 * 
	 * @return length in milliseconds
	 */
	long getLength();

	/**
	 * Get a message input for play.
	 * This object overrides the default algorithm for finding the appropriate VOD or Live stream provider according to
	 * the item name.
	 * 
	 * @return message input
	 */
	IMessageInput getMessageInput();
}
