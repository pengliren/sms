package com.sms.server.api.stream;

import com.sms.server.api.IScope;
import com.sms.server.api.IScopeService;

public interface IOnDemandStreamService extends IScopeService {

	public static String BEAN_NAME = "onDemandStreamService";

	/**
	 * Has the service an on-demand stream with the passed name?
	 *  
	 * @param scope the scope to check for the stream
	 * @param name the name of the stream
	 * @return true if the stream exists, false otherwise
	 */
	public boolean hasOnDemandStream(IScope scope, String name);

	/**
	 * Get a stream that can be used for playback of the on-demand stream
	 *  
	 * @param scope the scope to return the stream from
	 * @param name the name of the stream
	 * @return the on-demand stream
	 */
	public IOnDemandStream getOnDemandStream(IScope scope, String name);

}
