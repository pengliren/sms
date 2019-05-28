package com.sms.server.net.rtmp.codec;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * RTMP codec factory creates RTMP encoders/decoders.
 */
public class RTMPCodecFactory implements ProtocolCodecFactory {


    /**
     * Mina protocol decoder for RTMP.
     */
    private RTMPMinaProtocolDecoder decoder;

    /**
     * Mina protocol encoder for RTMP.
     */
    private RTMPMinaProtocolEncoder encoder;

    /**
     * Initialization
     */
    public void init() {
		decoder = new RTMPMinaProtocolDecoder();
		encoder = new RTMPMinaProtocolEncoder();
	}

	/** {@inheritDoc} */
    public ProtocolDecoder getDecoder(IoSession session) {
		return decoder;
	}

	/** {@inheritDoc} */
    public ProtocolEncoder getEncoder(IoSession session) {
		return encoder;
	}

    /**
     * Returns the RTMP decoder.
     * 
     * @return decoder
     */
    public RTMPProtocolDecoder getRTMPDecoder() {
		return decoder.getDecoder();
	}

	/**
	 * Returns the RTMP encoder.
	 * 
	 * @return encoder
	 */
    public RTMPProtocolEncoder getRTMPEncoder() {
		return encoder.getEncoder();
	}    
    
}
