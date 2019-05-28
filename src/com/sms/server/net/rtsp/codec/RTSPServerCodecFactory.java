package com.sms.server.net.rtsp.codec;

import java.nio.charset.CharacterCodingException;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * RTSP Server Codec Factory
 * @author pengliren
 *
 */
public class RTSPServerCodecFactory implements ProtocolCodecFactory {

	protected RTSPRequestDecoder decoder;

	protected RTSPResponseEncoder encoder;

	public RTSPServerCodecFactory() throws CharacterCodingException {

		decoder = new RTSPRequestDecoder();
		encoder = new RTSPResponseEncoder();
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
