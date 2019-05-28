package com.sms.server.api.stream;

import com.sms.server.api.IScope;

public interface IRtmpSampleAccess {
	
	public static String BEAN_NAME = "rtmpSampleAccess";

	/**
	 * Return true if sample access allowed on audio stream
	 * @param scope
	 * @return true if sample access allowed on audio stream
	 */
	public boolean isAudioAllowed(IScope scope);
	
	/**
	 * Return true if sample access allowed on video stream
	 * @param scope
	 * @return true if sample access allowed on video stream
	 */
	public boolean isVideoAllowed(IScope scope);
	
}
