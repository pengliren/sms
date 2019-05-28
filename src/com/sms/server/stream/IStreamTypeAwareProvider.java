package com.sms.server.stream;

import com.sms.server.messaging.IProvider;

/**
 * Interface for providers that know if they contain video frames.
 *
 */
public interface IStreamTypeAwareProvider extends IProvider {

	public static final String KEY = IStreamTypeAwareProvider.class.getName();

	/**
	 * Check if the provider contains video tags.
	 * 
	 * @return provider has video
	 */
	public boolean hasVideo();
	
}
