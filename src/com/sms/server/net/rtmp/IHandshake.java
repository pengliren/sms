package com.sms.server.net.rtmp;

import org.apache.mina.core.buffer.IoBuffer;

public interface IHandshake {

	public IoBuffer doHandshake(IoBuffer input);

	public boolean validate(IoBuffer input);
}
