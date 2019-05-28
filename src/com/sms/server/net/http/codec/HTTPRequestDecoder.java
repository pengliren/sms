package com.sms.server.net.http.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecException;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import com.sms.server.net.http.message.DefaultHttpRequest;
import com.sms.server.net.http.message.HTTPMessage;
import com.sms.server.net.http.message.HTTPMethod;
import com.sms.server.net.http.message.HTTPVersion;

/**
 * HTTP Request Decoder
 * @author pengliren
 *
 */
public class HTTPRequestDecoder extends HTTPMessageDecoder {

	@Override
	protected HTTPMessage createMessage(String[] initialLine) throws Exception {

		return new DefaultHttpRequest(HTTPVersion.valueOf(initialLine[2]), HTTPMethod.valueOf(initialLine[0]), initialLine[1]);
	}

	@Override
	protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		
		try {
			boolean ret = false;
			while(in.remaining() > 0) {
				DecodeState obj = decodeBuffer(in);
				if(obj.getState() == DecodeState.ENOUGH) {
					ret = true;
					if(obj.getObject() != null) out.write(obj.getObject());
				} else {
					ret = false;
					break;
				}
			}
			return ret;
		} catch (Exception e) {
			throw new ProtocolCodecException(e);
		}
	}
}
