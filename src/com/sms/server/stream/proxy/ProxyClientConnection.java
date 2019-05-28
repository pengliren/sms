package com.sms.server.stream.proxy;

import org.apache.mina.core.buffer.IoBuffer;

import com.sms.server.net.rtmp.RTMPConnection;
import com.sms.server.net.rtmp.codec.RTMP;
import com.sms.server.net.rtmp.message.Packet;

public class ProxyClientConnection extends RTMPConnection {

	public ProxyClientConnection() {
		super(PERSISTENT);
		this.state = new RTMP(RTMP.MODE_CLIENT);
	}

	@Override
	public void rawWrite(IoBuffer out) {

	}

	@Override
	public void write(Packet out) {

	}

	@Override
	protected void onInactive() {
		this.close();
	}

}
