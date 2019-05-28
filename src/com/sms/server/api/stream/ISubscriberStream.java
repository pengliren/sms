package com.sms.server.api.stream;

import java.io.IOException;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * ISubscriberStream is a stream from subscriber's point of view. That is, it
 * provides methods for common stream operations like play, pause or seek.
 */
public interface ISubscriberStream extends IClientStream {
	/**
	 * Start playing.
	 * 
	 * @throws IOException if an IO error occurred while starting to play the stream
	 */
	void play() throws IOException;

	/**
	 * Pause at a position for current playing item.
	 * 
	 * @param position
	 *            Position for pause in millisecond.
	 */
	void pause(int position);

	/**
	 * Resume from a position for current playing item.
	 * 
	 * @param position
	 *            Position for resume in millisecond.
	 */
	void resume(int position);

	/**
	 * Stop playing.
	 */
	void stop();

	/**
	 * Seek into a position for current playing item.
	 * 
	 * @param position
	 *            Position for seek in millisecond.
	 * @throws OperationNotSupportedException if the stream doesn't support seeking.
	 */
	void seek(int position) throws OperationNotSupportedException;

	/**
	 * Check if the stream is currently paused.
	 * 
	 * @return stream is paused
	 */
	boolean isPaused();

	/**
	 * Should the stream send video to the client?
	 * 
	 * @param receive
	 */
	void receiveVideo(boolean receive);

	/**
	 * Should the stream send audio to the client?
	 * 
	 * @param receive
	 */
	void receiveAudio(boolean receive);
	
	/**
	 * Return the streams state enum.
	 * 
	 * @return current state
	 */
	public StreamState getState();	
	
	/**
	 * Sets the streams state enum.
	 * 
	 * @param state sets current state
	 */
	public void setState(StreamState state);

	/**
	 * Notification of state change and associated parameters.
	 * 
	 * @param state new state
	 * @param changed parameters associated with the change
	 */
	public void onChange(final StreamState state, final Object... changed);

	/**
	 * Returns the Executor.
	 * @return executor
	 */
	ScheduledThreadPoolExecutor getExecutor();		
	
}
