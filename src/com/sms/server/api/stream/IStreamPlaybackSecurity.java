package com.sms.server.api.stream;

import com.sms.server.api.IScope;

/**
 * Interface for handlers that control access to stream playback.
 */
public interface IStreamPlaybackSecurity {

	/**
	 * Check if playback of a stream with the given name is allowed.
	 * 
	 * @param scope Scope the stream is about to be played back from.
	 * @param name Name of the stream to play.
	 * @param start Position to start playback from (in milliseconds).
	 * @param length Duration to play (in milliseconds).
	 * @param flushPlaylist Flush playlist?
	 * @return <code>True</code> if playback is allowed, otherwise <code>False</code>
	 */
	public boolean isPlaybackAllowed(IScope scope, String name, int start, int length, boolean flushPlaylist);
	
}
