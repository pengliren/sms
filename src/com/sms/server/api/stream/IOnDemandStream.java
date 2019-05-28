package com.sms.server.api.stream;

/**
 * Extends stream to add methods for on demand access.
 */
public interface IOnDemandStream extends IStream {

	/**
	 * Start playback
	 */
	public void play();

	/**
	 * Start playback with a given maximum duration.
	 * 
	 * @param length maximum duration in milliseconds
	 */
	public void play(int length);

	/**
	 * Seek to the keyframe nearest to position
	 * 
	 * @param position position in milliseconds
	 */
	public void seek(int position);

	/**
	 * Pause the stream
	 */
	public void pause();

	/**
	 * Resume a paused stream
	 */
	public void resume();

	/**
	 * Stop the stream, this resets the position to the start
	 */
	public void stop();

	/**
	 * Is the stream paused
	 * 
	 * @return true if the stream is paused
	 */
	public boolean isPaused();

	/**
	 * Is the stream stopped
	 * 
	 * @return true if the stream is stopped
	 */
	public boolean isStopped();

	/**
	 * Is the stream playing
	 * 
	 * @return true if the stream is playing
	 */
	public boolean isPlaying();

}