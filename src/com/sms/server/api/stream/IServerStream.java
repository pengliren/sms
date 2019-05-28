package com.sms.server.api.stream;

/**
 * IServerStream has both IPlaylist and IBroadcastStream methods but add nothing
 * new. It represents a stream broadcasted from the server.
 */
public interface IServerStream extends IPlaylist, IBroadcastStream {

	/**
	 * Toggles the paused state.
	 */
	public void pause();
	
	/**
	 * Seek to a given position in the stream.
	 * 
	 * @param position new playback position in milliseconds
	 */
	public void seek(int position);

}
