package com.sms.server.api.stream;

import com.sms.server.api.IScopeHandler;

/**
 * A scope handler that is stream aware.
 */
public interface IStreamAwareScopeHandler extends IScopeHandler {
	/**
	 * A broadcast stream starts being published. This will be called
	 * when the first video packet has been received.
	 * 
	 * @param stream stream
	 */
	public void streamPublishStart(IBroadcastStream stream);

	/**
	 * A broadcast stream starts being recorded. This will be called
	 * when the first video packet has been received.
	 * 
	 * @param stream stream 
	 */
	public void streamRecordStart(IBroadcastStream stream);

	/**
	 * Notified when a broadcaster starts.
	 * 
	 * @param stream stream
	 */
	public void streamBroadcastStart(IBroadcastStream stream);

	/**
	 * Notified when a broadcaster closes.
	 * 
	 * @param stream stream
	 */
	public void streamBroadcastClose(IBroadcastStream stream);

	/**
	 * Notified when a subscriber starts.
	 * 
	 * @param stream stream
	 */
	public void streamSubscriberStart(ISubscriberStream stream);

	/**
	 * Notified when a subscriber closes.
	 * 
	 * @param stream stream
	 */
	public void streamSubscriberClose(ISubscriberStream stream);

	/**
	 * Notified when a play item plays.
	 * 
	 * @param stream stream
	 * @param item item
	 * @param isLive true if live
	 */
	public void streamPlayItemPlay(ISubscriberStream stream, IPlayItem item, boolean isLive);

	/**
	 * Notified when a play item stops.
	 * 
	 * @param stream stream
	 * @param item item
	 */
	public void streamPlayItemStop(ISubscriberStream stream, IPlayItem item);

	/**
	 * Notified when a play item pauses.
	 * 
	 * @param stream stream
	 * @param item item
	 * @param position position
	 */
	public void streamPlayItemPause(ISubscriberStream stream, IPlayItem item, int position);

	/**
	 * Notified when a play item resumes.
	 * 
	 * @param stream stream
	 * @param item item
	 * @param position position
	 */
	public void streamPlayItemResume(ISubscriberStream stream, IPlayItem item, int position);

	/**
	 * Notified when a play item seeks.
	 * 
	 * @param stream stream
	 * @param item item
	 * @param position position
	 */
	public void streamPlayItemSeek(ISubscriberStream stream, IPlayItem item, int position);	
	
	
}
