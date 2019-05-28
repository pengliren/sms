package com.sms.server.stream.timeshift;

import com.sms.io.ITagReader;

/**
 * FLV Record Reader
 * @author pengliren
 *
 */
public interface IRecordFLVReader extends ITagReader {

	public void seekByTs(long ts);
}
