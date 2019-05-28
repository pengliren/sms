package com.sms.server.net.rtsp.codec;

import java.nio.charset.CharacterCodingException;

import org.apache.mina.core.buffer.IoBuffer;

import com.sms.server.net.http.message.HTTPMessage;
import com.sms.server.net.http.message.HTTPResponse;

/**
 * RTSP Response Encoder
 * @author pengliren
 *
 */
public class RTSPResponseEncoder extends RTSPMessageEncoder {

	public RTSPResponseEncoder() throws CharacterCodingException {
		super();
	}

	@Override
	protected void encodeInitialLine(IoBuffer buf, HTTPMessage message) throws Exception {
		
		HTTPResponse response = (HTTPResponse) message;
        buf.put(response.getProtocolVersion().toString().getBytes("ASCII"));
        buf.put((byte) ' ');
        buf.put(String.valueOf(response.getStatus().getCode()).getBytes("ASCII"));
        buf.put((byte) ' ');
        buf.put(String.valueOf(response.getStatus().getReasonPhrase()).getBytes("ASCII"));
        buf.put((byte) '\r');
        buf.put((byte) '\n');
	}

}
