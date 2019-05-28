package com.sms.server.net.http.codec;

import java.nio.charset.CharacterCodingException;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecException;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import com.sms.server.net.http.message.HTTPMessage;
import com.sms.server.net.http.message.HTTPResponse;

/**
 * HTTP Response Encoder
 * @author pengliren
 *
 */
public class HTTPResponseEncoder extends HTTPMessageEncoder {

	public HTTPResponseEncoder() throws CharacterCodingException {
		super();
	}

	@Override
	public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws ProtocolCodecException {

		IoBuffer buf;
		try {
			buf = encodeBuffer(message);
			if (buf != null) {
				out.write(buf);
				out.mergeAll();
				out.flush();
			} 
		} catch (Exception e) {
			throw new ProtocolCodecException(e);
		}		
	}

	@Override
	protected void encodeInitialLine(IoBuffer buf, HTTPMessage message) throws Exception {

		HTTPResponse response = (HTTPResponse) message;
		buf.put(response.getProtocolVersion().toString().getBytes("ASCII"));
		buf.put(HTTPCodecUtil.SP);
		buf.put(String.valueOf(response.getStatus().getCode()).getBytes("ASCII"));
		buf.put(HTTPCodecUtil.SP);
		buf.put(String.valueOf(response.getStatus().getReasonPhrase()).getBytes("ASCII"));
		buf.put(HTTPCodecUtil.CR);
		buf.put(HTTPCodecUtil.LF);
	}

}
