package com.sms.server.stream;

import com.sms.server.api.IBasicScope;
import com.sms.server.messaging.IPipe;

/**
 * Broadcast scope is marker interface that represents object that works as basic scope and
 * has pipe connection event dispatching capabilities.
 */
public interface IBroadcastScope extends IBasicScope, IPipe {
	public static final String TYPE = "bs";

	public static final String STREAM_ATTRIBUTE = TRANSIENT_PREFIX
			+ "_publishing_stream";
}
