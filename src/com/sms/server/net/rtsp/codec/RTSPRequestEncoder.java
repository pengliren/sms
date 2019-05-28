package com.sms.server.net.rtsp.codec;

import java.nio.charset.CharacterCodingException;

import org.apache.mina.core.buffer.IoBuffer;

import com.sms.server.net.http.message.HTTPMessage;
import com.sms.server.net.http.message.HTTPRequest;

/**
 * RTSP Request Encoder
 * @author pengliren
 *
 */
public class RTSPRequestEncoder extends RTSPMessageEncoder {

	public RTSPRequestEncoder() throws CharacterCodingException {
		super();
	}

	@Override
	protected void encodeInitialLine(IoBuffer buf, HTTPMessage message)
			throws Exception {
		
		HTTPRequest request = (HTTPRequest) message;
        buf.put(request.getMethod().toString().getBytes("ASCII"));
        buf.put((byte) ' ');
        buf.put(request.getUri().getBytes("ASCII"));
        buf.put((byte) ' ');
        buf.put(request.getProtocolVersion().toString().getBytes("ASCII"));
        buf.put((byte) '\r');
        buf.put((byte) '\n');
	}

}
