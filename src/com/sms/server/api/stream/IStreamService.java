package com.sms.server.api.stream;

import com.sms.server.api.IConnection;
import com.sms.server.api.IScopeService;

/**
 * This interface represents the stream methods that can be called throug RTMP.
 */
public interface IStreamService extends IScopeService {

	public static String BEAN_NAME = "streamService";

	/**
	 * Create a stream and return a corresponding id.
	 * 
	 * @return     ID of created stream
	 */
	public int createStream();

	/**
	 * Close the stream but not deallocate the resources.
	 * 
	 * @param connection Connection
	 * @param streamId  Stream id
	 */
	public void closeStream(IConnection connection, int streamId);

	/**
	 * Close the stream if not been closed.
	 * Deallocate the related resources.
	 * @param streamId          Stream id
	 */
	public void deleteStream(int streamId);

	/**
	 * Called by FMS.
	 * 
	 * @param streamId          Stream id
	 */
	public void initStream(int streamId);

	/**
	 * Called by FME.
	 * 
	 * @param streamName stream name
	 */
	public void releaseStream(String streamName);

	/**
	 * Delete stream
	 * @param conn            Stream capable connection
	 * @param streamId        Stream id
	 */
	public void deleteStream(IStreamCapableConnection conn, int streamId);

	/**
	 * Play stream without initial stop
	 * @param dontStop         Stoppage flag
	 */
	public void play(Boolean dontStop);

	/**
	 * Play stream with name
	 * @param name          Stream name
	 */
	public void play(String name);

	/**
	 * Play stream with name from start position
	 * @param name          Stream name
	 * @param start         Start position
	 */
	public void play(String name, int start);

	/**
	 * Play stream with name from start position and for given amount if time
	 * @param name          Stream name
	 * @param start         Start position
	 * @param length        Playback length
	 */
	public void play(String name, int start, int length);

	/**
	 * Publishes stream from given position for given amount of time
	 * @param name                      Stream published name
	 * @param start                     Start position
	 * @param length                    Playback length
	 * @param flushPlaylist             Flush playlist?
	 */
	public void play(String name, int start, int length, boolean flushPlaylist);

	/**
	 * Publishes stream with given name
	 * @param name             Stream published name
	 */
	public void publish(String name);

	/**
	 * Publishes stream with given name and mode
	 * @param name            Stream published name
	 * @param mode            Stream publishing mode
	 */
	public void publish(String name, String mode);

	/**
	 * Publish
	 * @param dontStop      Whether need to stop first
	 */
	public void publish(Boolean dontStop);

	/**
	 * Seek to position
	 * @param position         Seek position
	 */
	public void seek(int position);

	/**
	 * Pauses playback
	 * @param pausePlayback           Pause or resume flash
	 * @param position                Pause position
	 */
	public void pause(Boolean pausePlayback, int position);

	/**
	 * Undocumented Flash Plugin 10 call, assuming to be the alias to pause(boolean, int)
	 * 
	 * @param pausePlayback           Pause or resume flash
	 * @param position                Pause position
	 */
	public void pauseRaw(Boolean pausePlayback, int position);

	/**
	 * Can recieve video?
	 * @param receive       Boolean flag
	 */
	public void receiveVideo(boolean receive);

	/**
	 * Can recieve audio?
	 * @param receive       Boolean flag
	 */
	public void receiveAudio(boolean receive);

}
