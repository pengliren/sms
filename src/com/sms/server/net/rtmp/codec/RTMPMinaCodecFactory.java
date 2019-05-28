package com.sms.server.net.rtmp.codec;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * RTMP codec factory.
 */
public class RTMPMinaCodecFactory implements ProtocolCodecFactory {
	
    /**
     * RTMP Mina protocol decoder.
     */
	protected RTMPMinaProtocolDecoder decoder;
    /**
     * RTMP Mina protocol encoder.
     */
	protected RTMPMinaProtocolEncoder encoder;

    /**
     * Initialization. 
     * Create and setup of encoder/decoder and serializer/deserializer is handled by Spring.
     */
    public void init() {
	}
    
    public RTMPMinaCodecFactory() {
    	
    	encoder = new RTMPMinaProtocolEncoder();
    	decoder = new RTMPMinaProtocolDecoder();
	}

	/**
     * Setter for encoder.
     *
     * @param encoder  Encoder
     */
    public void setMinaEncoder(RTMPMinaProtocolEncoder encoder) {
		this.encoder = encoder;
    }

	/**
     * Setter for decoder
     *
     * @param decoder  Decoder
     */
    public void setMinaDecoder(RTMPMinaProtocolDecoder decoder) {
		this.decoder = decoder;
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
	 * 
	 * @return decoder
	 */
    public RTMPMinaProtocolDecoder getMinaDecoder() {
		return decoder;
	}

	/**
	 * 
	 * @return encoder
	 */
    public RTMPMinaProtocolEncoder getMinaEncoder() {
		return encoder;
	}	

}
