package com.sms.server.api.stream;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import com.sms.server.api.statistics.IPlaylistSubscriberStreamStatistics;

/**
 * IPlaylistSubscriberStream has methods of both ISubscriberStream and IPlaylist
 * but adds nothing new
 */
public interface IPlaylistSubscriberStream extends ISubscriberStream, IPlaylist {

	/**
	 * Return statistics about this stream.
	 * 
	 * @return statistics
	 */
	public IPlaylistSubscriberStreamStatistics getStatistics();
	
	/**
	 * Returns the job executor.
	 * 
	 * @return executor
	 */
	public ScheduledThreadPoolExecutor getExecutor();
	
	/**
	 * Handles a change occurring on the stream.
	 * 
	 * @param state stream state that we are changing to or notifying of
	 * @param changed changed items
	 */	
	public void onChange(StreamState state, Object... changed);	
	
	/**
	 * Replaces an item in the list with another item.
	 * 
	 * @param oldItem
	 * @param newItem
	 * @return true if successful and false otherwise
	 */
	public boolean replace(IPlayItem oldItem, IPlayItem newItem);
}
