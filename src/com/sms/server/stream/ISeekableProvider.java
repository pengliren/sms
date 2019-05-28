package com.sms.server.stream;

import com.sms.server.messaging.IProvider;

/**
 * Provider that is seekable
 */
public interface ISeekableProvider extends IProvider {
	public static final String KEY = ISeekableProvider.class.getName();

	/**
	 * Seek the provider to timestamp ts (in milliseconds).
	 * @param ts Timestamp to seek to
	 * @return Actual timestamp seeked to
	 */
	int seek(int ts);
}
