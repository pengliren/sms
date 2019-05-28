package com.sms.server.api.stream;

import java.util.Set;

import com.sms.server.api.IScopeService;

/**
 * Service that supports protecting access to streams.
 */
public interface IStreamSecurityService extends IScopeService {

	/** 
	 * Name of a bean defining that scope service.
	 * */
	public static final String BEAN_NAME = "streamSecurityService";

	/**
	 * Add handler that protects stream publishing.
	 * 
	 * @param handler Handler to add.
	 */
	public void registerStreamPublishSecurity(IStreamPublishSecurity handler);
	
	/**
	 * Remove handler that protects stream publishing.
	 * 
	 * @param handler Handler to remove.
	 */
	public void unregisterStreamPublishSecurity(IStreamPublishSecurity handler);
	
	/**
	 * Get handlers that protect stream publishing.
	 * 
	 * @return list of handlers
	 */
	public Set<IStreamPublishSecurity> getStreamPublishSecurity();
	
	/**
	 * Add handler that protects stream playback.
	 * 
	 * @param handler Handler to add.
	 */
	public void registerStreamPlaybackSecurity(IStreamPlaybackSecurity handler);
	
	/**
	 * Remove handler that protects stream playback.
	 * 
	 * @param handler Handler to remove.
	 */
	public void unregisterStreamPlaybackSecurity(IStreamPlaybackSecurity handler);
	
	/**
	 * Get handlers that protect stream plaback.
	 * 
	 * @return list of handlers
	 */
	public Set<IStreamPlaybackSecurity> getStreamPlaybackSecurity();
	
}
