package com.sms.server.messaging;

import java.io.IOException;

/**
 * A provider that supports passive pulling of messages.
 */
public interface IPullableProvider extends IProvider {
	public static final String KEY = IPullableProvider.class.getName();

	IMessage pullMessage(IPipe pipe) throws IOException;

	IMessage pullMessage(IPipe pipe, long wait) throws IOException;
}
