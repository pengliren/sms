package com.sms.server.api.stream;

import com.sms.server.api.IScope;
import com.sms.server.api.IScopeService;

public interface ISubscriberStreamService extends IScopeService {

	public static String BEAN_NAME = "subscriberStreamService";

	/**
	 * Returns a stream that can subscribe a broadcast stream with the given
	 * name using "IBroadcastStream.subscribe".
	 *  
	 * @param scope the scope to return the stream from
	 * @param name the name of the stream
	 * @return the stream object 
	 */
	public ISubscriberStream getSubscriberStream(IScope scope, String name);

}
