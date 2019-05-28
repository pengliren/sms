package com.sms.server.net.rtsp.codec;

import java.nio.charset.CharacterCodingException;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * RTSP Client Codec Factory
 * @author pengliren
 *
 */
public class RTSPClientCodecFactory implements ProtocolCodecFactory {

	protected RTSPRequestEncoder encoder;

	protected RTSPResponseDecoder decoder;
	
	public RTSPClientCodecFactory() throws CharacterCodingException {
		
		decoder = new RTSPResponseDecoder();
		encoder = new RTSPRequestEncoder();
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
