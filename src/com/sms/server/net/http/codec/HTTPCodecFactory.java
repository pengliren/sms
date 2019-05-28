package com.sms.server.net.http.codec;

import java.nio.charset.CharacterCodingException;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * HTTP Codec Factory
 * @author pengliren
 *
 */
public class HTTPCodecFactory implements ProtocolCodecFactory {

	protected HTTPRequestDecoder decoder;

	protected HTTPResponseEncoder encoder;
	
	public HTTPCodecFactory() throws CharacterCodingException {
		
		decoder = new HTTPRequestDecoder();
		encoder = new HTTPResponseEncoder();
	}
	
	@Override
	public ProtocolDecoder getDecoder(IoSession session) throws Exception {

		return decoder;
	}

	@Override
	public ProtocolEncoder getEncoder(IoSession session) throws Exception {
		
		return encoder;
	}

}
