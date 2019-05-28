package com.sms.server.stream;

/**
 * Stream source that can be seeked in timeline
 */
public interface ISeekableStreamSource extends IStreamSource {
	/**
	 * Seek the stream source to timestamp ts (in milliseconds).
	 * @param ts Timestamp to seek to
	 * @return Actual timestamp seeked to
	 */
	int seek(int ts);
}
