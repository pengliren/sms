package com.sms.server.api.stream;
/**
 * Represents all the states that a stream may be in at a requested
 * point in time.
 */
public enum StreamState {

	INIT, UNINIT, OPEN, CLOSED, STARTED, STOPPED, PLAYING, PAUSED, RESUMED, END, SEEK;
	
}
